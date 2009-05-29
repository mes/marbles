/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

import org.openrdf.query.algebra.Compare.CompareOp;

public class ASTCompOperator extends SimpleNode {

	private CompareOp value;

	public ASTCompOperator(int id) {
		super(id);
	}

	public ASTCompOperator(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public CompareOp getValue() {
		return value;
	}

	public void setValue(CompareOp operator) {
		this.value = operator;
	}
}
