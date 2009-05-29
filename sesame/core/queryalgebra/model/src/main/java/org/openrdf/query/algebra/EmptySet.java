/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.Collections;
import java.util.Set;

/**
 * A tuple expression that contains zero solutions.
 */
public class EmptySet extends QueryModelNodeBase implements TupleExpr {

	public Set<String> getBindingNames() {
		return Collections.emptySet();
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public EmptySet clone() {
		return (EmptySet)super.clone();
	}
}
