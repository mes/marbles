/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

public class ASTTupleUnion extends ASTTupleQuerySet {

	private boolean distinct = true;

	public ASTTupleUnion(int id) {
		super(id);
	}

	public ASTTupleUnion(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public boolean isDistinct() {
		return distinct;
	}

	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}

	@Override
	public String toString()
	{
		String result = super.toString();

		if (distinct) {
			result += " (distinct)";
		}

		return result;
	}
}
