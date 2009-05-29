package org.openrdf.sail.rdbms.mysql;

import java.sql.SQLException;

import org.openrdf.sail.rdbms.schema.RdbmsTable;
import org.openrdf.sail.rdbms.schema.ValueTableFactory;

public class MySqlTable extends RdbmsTable {

	public MySqlTable(String name) {
		super(name);
	}

	@Override
	protected String buildCreateTransactionalTable(CharSequence columns) {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ").append(getName());
		sb.append(" (\n").append(columns).append(")");
		sb.append(" engine = MyISAM"); /* using MyISAM here although it is not transactional, as mixing of InnoDB and MyISAM tables yields bad index selection orders */
		return sb.toString();
	}

	@Override
	protected String buildLongIndex(int sqlType, String... columns) {
		return "SELECT NULL";
		/*StringBuilder sb = new StringBuilder();
		sb.append("CREATE INDEX ").append(buildIndexName(columns));
		sb.append(" ON ").append(getName());
		sb.append(" (value(1024))");
		return sb.toString();
	*/}

	@Override
	protected String buildOptimize()
		throws SQLException
	{
		return "SELECT NULL"; //	return "OPTIMIZE TABLE " + getName();
	}

	@Override
	protected String buildDropIndex(String name) {
		return "SELECT NULL"; /*StringBuilder sb = new StringBuilder();
		sb.append("DROP INDEX ").append(name);
		sb.append(" ON ").append(getName());
		return sb.toString();*/
	}
	
	protected String buildIndex(int sqlType, String... columns) {
		return "SELECT NULL"; /* StringBuilder sb = new StringBuilder();
		sb.append("CREATE " + (sqlType == ValueTableFactory.POINT_TYPE ? "SPATIAL " : "") + "INDEX ").append(buildIndexName(columns));
		sb.append(" ON ").append(getName()).append(" (");
		for (int i = 0; i < columns.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(columns[i]);
		}
		sb.append(")");
		return sb.toString();
	*/}	
}
