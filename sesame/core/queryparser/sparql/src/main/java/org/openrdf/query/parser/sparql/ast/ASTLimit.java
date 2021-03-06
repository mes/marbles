/* Generated By:JJTree: Do not edit this line. ASTLimitClause.java */

package org.openrdf.query.parser.sparql.ast;

public class ASTLimit extends SimpleNode {

	private int value;

	public ASTLimit(int id) {
		super(id);
	}

	public ASTLimit(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	@Override
	public String toString()
	{
		return super.toString() + " (" + value + ")";
	}
}
