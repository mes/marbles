/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.evaluation;

import java.util.List;

/**
 * Facilitates the building of a SQL query.
 * 
 * @author James Leigh
 * 
 */
public class SqlQueryBuilder {
	private QueryBuilderFactory factory;
	private boolean distinct;
	private SqlExprBuilder select;
	private SqlJoinBuilder from;
	private StringBuilder group = new StringBuilder();
	private SqlExprBuilder order;
	private SqlQueryBuilder union;
	private Integer offset;
	private Integer limit;

	public SqlQueryBuilder(QueryBuilderFactory factory) {
		super();
		this.factory = factory;
		select = factory.createSqlExprBuilder();
		order = factory.createSqlExprBuilder();
	}

	public List<Object> findParameters(List<Object> parameters) {
		parameters.addAll(select.getParameters());
		if (from != null) {
			from.findParameters(parameters);
		}
		if (union != null) {
			union.findParameters(parameters);
		}
		parameters.addAll(order.getParameters());
		return parameters;
	}

	public void distinct() {
		distinct = true;
	}

	public SqlExprBuilder select() {
		if (!select.isEmpty())
			select.append(",\n ");
		return select;
	}

	public SqlJoinBuilder from(String table, String alias) {
		assert from == null : alias;
		return from = factory.createSqlJoinBuilder(table, alias);
	}

	public SqlJoinBuilder from(String alias) {
		assert from == null : alias;
		return from = factory.createSqlJoinBuilder(null, alias);
	}

	public SqlExprBuilder filter() {
		assert from != null;
		return from.on();
	}

	public SqlQueryBuilder groupBy(String... expressions) {
		for (String expr : expressions) {
			if (group.length() == 0) {
				group.append("\nGROUP BY ");
			} else {
				group.append(", ");
			}
			group.append(expr);
		}
		return this;
	}

	public SqlQueryBuilder union() {
		assert union == null : union;
		return union = factory.createSqlQueryBuilder();
	}

	public boolean isEmpty() {
		return select.isEmpty() && from == null;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (union != null && !union.isEmpty()) {
			sb.append("SELECT * FROM (");
		}
		sb.append("SELECT ");
		/* TODO Workaround: Always make results distinct, so that LIMIT can be applied in a meaningful way
		 * Reduces the number of results from of the based_near sample query from 1,300 to 60
		 * 
		 * - This is not caused by duplicate DB entries, cf. SELECT * FROM [LABEL_VALUES, URI_VALUES etc.]id IN (208531, 805506008, 1603, 805312806, 208432,208433)
		 * - Neither by the modified behavior of always joining on LONG_LABEL_URIs etc. (they are empty).
		 * - Thus the cause probably lies with a join on NULL columns somewhere
		 */  
//		if (distinct) {
			sb.append("DISTINCT ");
//		}
		if (select.isEmpty()) {
			sb.append("*");
		} else {
			sb.append(select.toSql());
		}
		if (from != null) {
			sb.append("\nFROM ").append(from.getFromClause());
			if (!from.on().isEmpty()) {
				sb.append("\nWHERE ");
				sb.append(from.on().toSql());
			}
		}
		sb.append(group);
		if (union != null && !union.isEmpty()) {
			sb.append("\nUNION ALL ");
			sb.append(union.toString());
		}
		if (!order.isEmpty()) {
			sb.append("\nORDER BY ").append(order.toSql());
		}
		if (limit != null) {
			sb.append("\nLIMIT ").append(limit);
		}
		if (offset != null) {
			sb.append("\nOFFSET ").append(offset);
		}
		if (union != null && !union.isEmpty()) {
			sb.append(") mysqlfix ");
		}
		return sb.toString();
	}

	public SqlExprBuilder orderBy() {
		if (!order.isEmpty())
			order.append(",\n ");
		return order;
	}

	public void offset(Integer offset) {
		this.offset = offset;
		if (limit == null) {
			limit = Integer.MAX_VALUE;
		}
	}

	public void limit(Integer limit) {
		this.limit = limit;
	}
}
