/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.mysql;

import java.sql.SQLException;
import java.sql.Types;

import org.openrdf.sail.rdbms.schema.ValueTable;
import org.openrdf.sail.rdbms.schema.ValueTableFactory;

/**
 * 
 * @author James Leigh
 */
public class MySqlValueTable extends ValueTable {

	private static final String FEILD_COLLATE = " CHARACTER SET utf8 COLLATE utf8_bin";

	@Override
	public String sql(int type, int length) {
		if (type == ValueTableFactory.POINT_TYPE)
			return "POINT";
		
		String declare = super.sql(type, length);
		if (type == Types.VARCHAR) {
			return declare + FEILD_COLLATE;
		}
		else if (type == Types.LONGVARCHAR) {
			return "LONGTEXT" + FEILD_COLLATE;
		}
		else {
			return declare;
		}
	}
	
	@Override
	public void initialize() throws SQLException {
		super.initialize();

		/* For POINT type, insert using MySQL WKT functions */
		if (getSqlType() == ValueTableFactory.POINT_TYPE) {
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ").append(getInsertTable().getName());
			sb.append(" (id, value) VALUES (?, PointFromText(CONCAT('POINT(', ?, ')')))");
			setINSERT(sb.toString());
			/* No need to change INSERT_SELECT, as it will is constructed using the POINT datatype and will also use this INSERT statement */
		}
	}

}
