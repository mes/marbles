/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.helpers;

import org.openrdf.query.algebra.QueryModelNode;

/**
 * QueryModelVisitor implementation that "prints" a tree representation of a
 * query model. The tree representations is printed to an internal character
 * buffer and can be retrieved using {@link #getTreeString()}. As an
 * alternative, the static utility method {@link #printTree(QueryModelNode)} can
 * be used.
 */
public class QueryModelTreePrinter extends QueryModelVisitorBase<RuntimeException> {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	/*-----------*
	 * Constants *
	 *-----------*/

	public static String printTree(QueryModelNode node) {
		QueryModelTreePrinter treePrinter = new QueryModelTreePrinter();
		node.visit(treePrinter);
		return treePrinter.getTreeString();
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	private String indentString = "   ";

	private StringBuilder buf;

	private int indentLevel = 0;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public QueryModelTreePrinter() {
		buf = new StringBuilder(256);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public String getTreeString() {
		return buf.toString();
	}

	@Override
	protected void meetNode(QueryModelNode node)
	{
		for (int i = 0; i < indentLevel; i++) {
			buf.append(indentString);
		}

		buf.append(node.getSignature());
		buf.append(LINE_SEPARATOR);

		indentLevel++;

		super.meetNode(node);

		indentLevel--;
	}
}
