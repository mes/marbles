/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.algebra;

import org.openrdf.query.algebra.MathExpr.MathOp;
import org.openrdf.sail.rdbms.algebra.base.BinarySqlOperator;
import org.openrdf.sail.rdbms.algebra.base.RdbmsQueryModelVisitorBase;
import org.openrdf.sail.rdbms.algebra.base.SqlExpr;

/**
 * The SQL subtraction (-) expression.
 * 
 * @author James Leigh
 * 
 */
public class SqlMathExpr extends BinarySqlOperator {

	private MathOp op;

	public SqlMathExpr(SqlExpr leftArg, MathOp op, SqlExpr rightArg) {
		super(leftArg, rightArg);
		this.op = op;
	}

	public MathOp getOperator() {
		return op;
	}

	@Override
	public <X extends Exception> void visit(RdbmsQueryModelVisitorBase<X> visitor)
		throws X
	{
		visitor.meet(this);
	}
}
