/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * An abstract superclass for binary tuple operators which, by definition, has
 * two arguments.
 */
public abstract class BinaryTupleOperator extends QueryModelNodeBase implements TupleExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The operator's left argument.
	 */
	protected TupleExpr leftArg;

	/**
	 * The operator's right argument.
	 */
	protected TupleExpr rightArg;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public BinaryTupleOperator() {
	}

	/**
	 * Creates a new binary tuple operator.
	 * 
	 * @param leftArg
	 *        The operator's left argument, must not be <tt>null</tt>.
	 * @param rightArg
	 *        The operator's right argument, must not be <tt>null</tt>.
	 */
	public BinaryTupleOperator(TupleExpr leftArg, TupleExpr rightArg) {
		setLeftArg(leftArg);
		setRightArg(rightArg);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the left argument of this binary tuple operator.
	 * 
	 * @return The operator's left argument.
	 */
	public TupleExpr getLeftArg() {
		return leftArg;
	}

	/**
	 * Sets the left argument of this binary tuple operator.
	 * 
	 * @param leftArg
	 *        The (new) left argument for this operator, must not be
	 *        <tt>null</tt>.
	 */
	public void setLeftArg(TupleExpr leftArg) {
		assert leftArg != null : "leftArg must not be null";
		leftArg.setParentNode(this);
		this.leftArg = leftArg;
	}

	/**
	 * Gets the right argument of this binary tuple operator.
	 * 
	 * @return The operator's right argument.
	 */
	public TupleExpr getRightArg() {
		return rightArg;
	}

	/**
	 * Sets the right argument of this binary tuple operator.
	 * 
	 * @param rightArg
	 *        The (new) right argument for this operator, must not be
	 *        <tt>null</tt>.
	 */
	public void setRightArg(TupleExpr rightArg) {
		assert rightArg != null : "rightArg must not be null";
		rightArg.setParentNode(this);
		this.rightArg = rightArg;
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		leftArg.visit(visitor);
		rightArg.visit(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement)
	{
		if (leftArg == current) {
			setLeftArg((TupleExpr)replacement);
		}
		else if (rightArg == current) {
			setRightArg((TupleExpr)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public BinaryTupleOperator clone() {
		BinaryTupleOperator clone = (BinaryTupleOperator)super.clone();
		clone.setLeftArg(getLeftArg().clone());
		clone.setRightArg(getRightArg().clone());
		return clone;
	}
}
