/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * Main interface for all query model nodes.
 */
public interface QueryModelNode extends Cloneable {

	/**
	 * Visits this node. The node reports itself to the visitor with the proper
	 * runtime type.
	 */
	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X;

	/**
	 * Visits the children of this node. The node calls
	 * {@link #visit(QueryModelVisitor)} on all of its child nodes.
	 */
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X;

	/**
	 * Gets the node's parent.
	 * 
	 * @return The parent node, if any.
	 */
	public QueryModelNode getParentNode();

	/**
	 * Sets the node's parent.
	 * 
	 * @param parent
	 *        The parent node for this node.
	 */
	public void setParentNode(QueryModelNode parent);

	/**
	 * Replaces one of the child nodes with a new node.
	 * 
	 * @param current
	 *        The current child node.
	 * @param replacement
	 *        The new child node.
	 * @throws IllegalArgumentException
	 *         If <tt>current</tt> is not one of node's children.
	 * @throws ClassCastException
	 *         If <tt>replacement</tt> is of an incompatible type.
	 */
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement);

	/**
	 * Substitutes this node with a new node in the query model tree.
	 * 
	 * @param replacement
	 *        The new node.
	 * @throws IllegalStateException
	 *         If this node does not have a parent node.
	 * @throws ClassCastException
	 *         If <tt>replacement</tt> is of an incompatible type.
	 */
	public void replaceWith(QueryModelNode replacement);

	/**
	 * Returns an indented print of the node tree, starting from this node.
	 */
	public String toString();

	/**
	 * Returns the signature of this query model node. Signatures normally
	 * include the node's name and any parameters, but not parent or child nodes.
	 * This method is used by {@link #toString()}.
	 * 
	 * @return The node's signature, e.g. <tt>SLICE (offset=10, limit=10)</tt>.
	 */
	public String getSignature();

	/**
	 * Returns a (deep) clone of this query model node. This method recursively
	 * clones the entire node tree, starting from this nodes.
	 * 
	 * @return A deep clone of this query model node.
	 */
	public QueryModelNode clone();
}
