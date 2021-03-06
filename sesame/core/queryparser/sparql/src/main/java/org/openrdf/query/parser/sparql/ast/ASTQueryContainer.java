/* Generated By:JJTree: Do not edit this line. ASTQueryContainer.java */

package org.openrdf.query.parser.sparql.ast;

import java.util.List;

public class ASTQueryContainer extends SimpleNode {

	public ASTQueryContainer(int id) {
		super(id);
	}

	public ASTQueryContainer(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public ASTBaseDecl getBaseDecl() {
		return super.jjtGetChild(ASTBaseDecl.class);
	}

	public List<ASTPrefixDecl> getPrefixDeclList() {
		return super.jjtGetChildren(ASTPrefixDecl.class);
	}

	public ASTQuery getQuery() {
		return super.jjtGetChild(ASTQuery.class);
	}
}
