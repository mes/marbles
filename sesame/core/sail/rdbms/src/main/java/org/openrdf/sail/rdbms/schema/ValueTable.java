/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.schema;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Manages the rows in a value table. These tables have two columns: an internal
 * id column and a value column.
 * 
 * @author James Leigh
 * 
 */
public class ValueTable {

	public static int BATCH_SIZE = 8 * 1024;

	public static final long NIL_ID = 0; // TODO

	private static final String[] PKEY = { "id" };

	private static final String[] VALUE_INDEX = { "value" };

	private int length = -1;

	private int sqlType;

	private int idType;

	private String INSERT;

	private String INSERT_SELECT;

	private String EXPUNGE;

	private RdbmsTable table;

	private RdbmsTable temporary;

	private ValueBatch batch;

	private BlockingQueue<Batch> queue;

	private boolean indexingValues;

	private PreparedStatement insertSelect;

	public void setQueue(BlockingQueue<Batch> queue) {
		this.queue = queue;
	}

	public boolean isIndexingValues() {
		return indexingValues;
	}

	public void setIndexingValues(boolean indexingValues) {
		this.indexingValues = indexingValues;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getSqlType() {
		return sqlType;
	}

	public void setSqlType(int sqlType) {
		this.sqlType = sqlType;
	}

	public int getIdType() {
		return idType;
	}

	public void setIdType(int sqlType) {
		this.idType = sqlType;
	}

	public RdbmsTable getRdbmsTable() {
		return table;
	}

	public void setRdbmsTable(RdbmsTable table) {
		this.table = table;
	}

	public RdbmsTable getTemporaryTable() {
		return temporary;
	}

	public void setTemporaryTable(RdbmsTable temporary) {
		this.temporary = temporary;
	}

	public String getName() {
		return table.getName();
	}

	public long size() {
		return table.size();
	}

	public int getBatchSize() {
		return BATCH_SIZE;
	}

	public void initialize()
		throws SQLException
	{
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ").append(getInsertTable().getName());
		sb.append(" (id, value) VALUES (?, ?)");
		setINSERT(sb.toString());
		sb.delete(0, sb.length());
		sb.append("DELETE FROM ").append(table.getName()).append("\n");
		sb.append("WHERE 1=1 ");
		EXPUNGE = sb.toString();
		if (temporary != null) {
			sb.delete(0, sb.length());
			sb.append("INSERT INTO ").append(table.getName());
			sb.append(" (id, value) SELECT DISTINCT id, value FROM ");
			sb.append(temporary.getName()).append(" tmp\n");
			sb.append("WHERE NOT EXISTS (SELECT id FROM ").append(table.getName());
			sb.append(" val WHERE val.id = tmp.id)");
			INSERT_SELECT = sb.toString();
		}
		if (!table.isCreated()) {
			createTable(table);
			table.primaryIndex(PKEY);
			if (isIndexingValues()) {
				table.index(sqlType, VALUE_INDEX);
			}
		}
		else {
			table.count();
		}
		if (temporary != null && !temporary.isCreated()) {
			createTemporaryTable(temporary);
		}
	}

	public void close()
		throws SQLException
	{
		if (insertSelect != null) {
			insertSelect.close();
		}
		if (temporary != null) {
			temporary.close();
		}
		table.close();
	}

	public synchronized void insert(Number id, Object value)
		throws SQLException, InterruptedException
	{
		ValueBatch batch = getValueBatch();
		if (isExpired(batch)) {
			batch = newValueBatch();
			initBatch(batch);
		}
		batch.setObject(1, id);
		batch.setObject(2, value);
		batch.addBatch();
		queue(batch);
	}

	public ValueBatch getValueBatch() {
		return this.batch;
	}

	public boolean isExpired(ValueBatch batch) {
		if (batch == null || batch.isFull())
			return true;
		return queue == null || !queue.remove(batch);
	}

	public ValueBatch newValueBatch() {
		return new ValueBatch();
	}

	public void initBatch(ValueBatch batch)
		throws SQLException
	{
		batch.setTable(table);
		batch.setBatchStatement(prepareInsert(INSERT));
		batch.setMaxBatchSize(getBatchSize());
		if (temporary != null) {
			batch.setTemporary(temporary);
			if (insertSelect == null) {
				insertSelect = prepareInsertSelect(INSERT_SELECT);
			}
			batch.setInsertStatement(insertSelect);
		}
	}

	public void queue(ValueBatch batch)
		throws SQLException, InterruptedException
	{
		this.batch = batch;
		if (queue == null) {
			batch.flush();
		}
		else {
			queue.put(batch);
		}
	}

	public void optimize()
		throws SQLException
	{
		table.optimize();
	}

	public boolean expunge(String condition)
		throws SQLException
	{
		synchronized (table) {
			int count = table.executeUpdate(EXPUNGE + condition);
			if (count < 1)
				return false;
			table.modified(0, count);
			return true;
		}
	}

	public List<Long> maxIds(int shift, int mod)
		throws SQLException
	{
		String column = "id";
		StringBuilder expr = new StringBuilder();
		expr.append("MOD((").append(column);
		expr.append(" >> ").append(shift);
		expr.append(") + ").append(mod).append(", ");
		expr.append(mod);
		expr.append(")");
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ").append(expr);
		sb.append(", MAX(").append(column);
		sb.append(")\nFROM ").append(getName());
		sb.append("\nGROUP BY ").append(expr);
		String query = sb.toString();
		PreparedStatement st = table.prepareStatement(query);
		try {
			ResultSet rs = st.executeQuery();
			try {
				List<Long> result = new ArrayList<Long>();
				while (rs.next()) {
					result.add(rs.getLong(2));
				}
				return result;
			}
			finally {
				rs.close();
			}
		}
		finally {
			st.close();
		}
	}

	public String sql(int type, int length) {
		switch (type) {
			case Types.VARCHAR:
				if (length > 0)
					return "VARCHAR(" + length + ")";
				return "TEXT";
			case Types.LONGVARCHAR:
				if (length > 0)
					return "LONGVARCHAR(" + length + ")";
				return "TEXT";
			case Types.BIGINT:
				return "BIGINT";
			case Types.INTEGER:
				return "INTEGER";
			case Types.SMALLINT:
				return "SMALLINT";
			case Types.FLOAT:
				return "FLOAT";
			case Types.DOUBLE:
				return "DOUBLE";
			case Types.DECIMAL:
				return "DECIMAL";
			case Types.BOOLEAN:
				return "BOOLEAN";
			case Types.TIMESTAMP:
				return "TIMESTAMP";
			default:
				throw new AssertionError("Unsupported SQL Type: " + type);
		}
	}

	@Override
	public String toString() {
		return getName();
	}
	
	protected RdbmsTable getInsertTable() {
		RdbmsTable tmp = getTemporaryTable();
		if (tmp == null) {
			tmp = getRdbmsTable();
		}
		return tmp;
	}

	protected PreparedStatement prepareInsert(String sql)
		throws SQLException
	{
		return table.prepareStatement(sql);
	}

	protected PreparedStatement prepareInsertSelect(String sql)
		throws SQLException
	{
		return table.prepareStatement(sql);
	}

	protected void createTable(RdbmsTable table)
		throws SQLException
	{
		StringBuilder sb = new StringBuilder();
		sb.append("  id ").append(sql(idType, -1)).append(" NOT NULL,\n");
		sb.append("  value ").append(sql(sqlType, length));
		sb.append(" NOT NULL\n");
		table.createTable(sb);
	}

	protected void createTemporaryTable(RdbmsTable table)
		throws SQLException
	{
		StringBuilder sb = new StringBuilder();
		sb.append("  id ").append(sql(idType, -1)).append(" NOT NULL,\n");
		sb.append("  value ").append(sql(sqlType, length));
		sb.append(" NOT NULL\n");
		table.createTemporaryTable(sb);
	}

	public void setINSERT(String insert) {
		this.INSERT = insert;
	}
}
