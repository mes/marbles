/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.query.parser.serql.ast.ASTProjectionElem;
import org.openrdf.query.parser.serql.ast.ASTSelect;
import org.openrdf.query.parser.serql.ast.ASTString;
import org.openrdf.query.parser.serql.ast.ASTVar;
import org.openrdf.query.parser.serql.ast.Node;
import org.openrdf.query.parser.serql.ast.SyntaxTreeBuilderTreeConstants;
import org.openrdf.query.parser.serql.ast.VisitorException;


/**
 * Processes projection aliases, verifying that the specified aliases are unique
 * and generating aliases for the elements for which no alias has been
 * specified but that do require one.
 * 
 * @author Arjohn Kampman
 */
class ProjectionAliasProcessor extends ASTVisitorBase {

	@Override
	public Object visit(ASTSelect node, Object data)
		throws VisitorException
	{
		// Iterate over all projection elements to retrieve the defined aliases
		Set<String> aliases = new HashSet<String>();
		List<Node> unaliasedNodes = new ArrayList<Node>();

		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			Node projElem = node.jjtGetChild(i);

			assert projElem instanceof ASTProjectionElem : "child node is not a projection element";

			Object result = projElem.jjtAccept(this, aliases);

			if (result instanceof String) {
				String alias = (String)result;

				boolean isUnique = aliases.add(alias);

				if (!isUnique) {
					throw new VisitorException("Duplicate projection element aliases: '" + alias + "'");
				}
			}
			else {
				unaliasedNodes.add(projElem);
			}
		}

		// Iterate over the unaliased nodes and generate aliases for them
		int aliasNo = 1;
		for (Node projElem : unaliasedNodes) {
			Node exprNode = projElem.jjtGetChild(0);

			if (exprNode instanceof ASTVar) {
				String varName = ((ASTVar)exprNode).getName();
				
				if (!aliases.contains(varName)) {
					// No need to generate an alias for this element
					aliases.add(varName);
					continue;
				}
			}

			// Generate unique alias for projection element
			String alias;
			while (aliases.contains(alias = "_" + aliasNo++)) {
				// try again
			}

			aliases.add(alias);

			ASTString aliasNode = new ASTString(SyntaxTreeBuilderTreeConstants.JJTSTRING);
			aliasNode.setValue(alias);
			aliasNode.jjtSetParent(projElem);
			projElem.jjtAppendChild(aliasNode);
		}

		return data;
	}

	@Override
	public Object visit(ASTProjectionElem node, Object data)
		throws VisitorException
	{
		// Only visit alias node
		if (node.jjtGetNumChildren() >= 2) {
			return node.jjtGetChild(1).jjtAccept(this, data);
		}

		return data;
	}

	@Override
	public String visit(ASTString node, Object data)
		throws VisitorException
	{
		return node.getValue();
	}
}
