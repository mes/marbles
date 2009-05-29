/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The UNION set operator, which return the union of the result sets of two
 * tuple expressions.
 */
public class Union extends BinaryTupleOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Union() {
	}

	/**
	 * Creates a new union operator that operates on the two specified arguments.
	 * 
	 * @param leftArg
	 *        The left argument of the union operator.
	 * @param rightArg
	 *        The right argument of the union operator.
	 */
	public Union(TupleExpr leftArg, TupleExpr rightArg) {
		super(leftArg, rightArg);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Set<String> getBindingNames() {
		Set<String> bindingNames = new LinkedHashSet<String>(16);
		bindingNames.addAll(getLeftArg().getBindingNames());
		bindingNames.addAll(getRightArg().getBindingNames());
		return bindingNames;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public Union clone() {
		return (Union)super.clone();
	}
}
