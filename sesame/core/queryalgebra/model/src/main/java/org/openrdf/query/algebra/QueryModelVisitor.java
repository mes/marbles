/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * An interface for query model visitors, implementing the Visitor pattern. Core
 * query model nodes will call their type-specific method when
 * {@link QueryModelNode#visit(QueryModelVisitor)} is called. The method
 * {@link #meetOther(QueryModelNode)} is provided as a hook for foreign query
 * model nodes.
 */
public interface QueryModelVisitor<X extends Exception> {

	public void meet(QueryRoot node)
		throws X;

	public void meet(And node)
		throws X;

	public void meet(BNodeGenerator node)
		throws X;

	public void meet(Bound node)
		throws X;

	public void meet(Compare node)
		throws X;

	public void meet(CompareAll node)
		throws X;

	public void meet(CompareAny node)
		throws X;

	public void meet(Count node)
		throws X;

	public void meet(Datatype node)
		throws X;

	public void meet(Difference node)
		throws X;

	public void meet(Distinct node)
		throws X;

	public void meet(EmptySet node)
		throws X;

	public void meet(Exists node)
		throws X;

	public void meet(Extension node)
		throws X;

	public void meet(ExtensionElem node)
		throws X;

	public void meet(FunctionCall node)
		throws X;

	public void meet(Group node)
		throws X;

	public void meet(GroupElem node)
		throws X;

	public void meet(In node)
		throws X;

	public void meet(Intersection node)
		throws X;

	public void meet(IsBNode node)
		throws X;

	public void meet(IsLiteral node)
		throws X;

	public void meet(IsResource node)
		throws X;

	public void meet(IsURI node)
		throws X;

	public void meet(Join node)
		throws X;

	public void meet(Label node)
		throws X;

	public void meet(Lang node)
		throws X;

	public void meet(LangMatches node)
		throws X;

	public void meet(Like node)
		throws X;

	public void meet(LocalName node)
		throws X;

	public void meet(MathExpr node)
		throws X;

	public void meet(Max node)
		throws X;

	public void meet(Min node)
		throws X;

	public void meet(MultiProjection node)
		throws X;

	public void meet(Namespace node)
		throws X;

	public void meet(Not node)
		throws X;

	public void meet(LeftJoin node)
		throws X;

	public void meet(Or node)
		throws X;

	public void meet(Order node)
		throws X;

	public void meet(OrderElem node)
		throws X;

	public void meet(Projection node)
		throws X;

	public void meet(ProjectionElemList node)
		throws X;

	public void meet(ProjectionElem node)
		throws X;

	public void meet(Regex node)
		throws X;

	public void meet(Slice node)
		throws X;

	public void meet(SameTerm node)
		throws X;

	public void meet(Filter node)
		throws X;

	public void meet(SingletonSet node)
		throws X;

	public void meet(StatementPattern node)
		throws X;

	public void meet(Str node)
		throws X;

	public void meet(Union node)
		throws X;

	public void meet(ValueConstant node)
		throws X;

	public void meet(Var node)
		throws X;

	public void meetOther(QueryModelNode node)
		throws X;
}
