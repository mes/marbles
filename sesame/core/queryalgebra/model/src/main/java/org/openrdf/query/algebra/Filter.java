/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * The FILTER operator, as defined in <a
 * href="http://www.w3.org/TR/rdf-sparql-query/#defn_algFilter">SPARQL Query
 * Language for RDF</a>. The FILTER operator filters specific results from the
 * underlying tuple expression based on a configurable condition.
 * 
 * @author Arjohn Kampman
 */
public class Filter extends UnaryTupleOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	private ValueExpr condition;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Filter() {
	}

	public Filter(TupleExpr arg, ValueExpr condition) {
		super(arg);
		setCondition(condition);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public ValueExpr getCondition() {
		return condition;
	}

	public void setCondition(ValueExpr condition) {
		assert condition != null : "condition must not be null";
		condition.setParentNode(this);
		this.condition = condition;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		condition.visit(visitor);
		super.visitChildren(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement)
	{
		if (condition == current) {
			setCondition((ValueExpr)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public Filter clone() {
		Filter clone = (Filter)super.clone();
		clone.setCondition(getCondition().clone());
		return clone;
	}
}
