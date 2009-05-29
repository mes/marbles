/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.dataset;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.Query;
import org.openrdf.repository.sail.SailQuery;

/**
 * @author Arjohn Kampman
 */
abstract class DatasetQuery implements Query {

	protected final DatasetRepositoryConnection con;

	protected final SailQuery sailQuery;

	protected DatasetQuery(DatasetRepositoryConnection con, SailQuery sailQuery) {
		this.con = con;
		this.sailQuery = sailQuery;
	}

	public final BindingSet getBindings() {
		return sailQuery.getBindings();
	}

	public final Dataset getDataset() {
		return sailQuery.getDataset();
	}

	public final boolean getIncludeInferred() {
		return sailQuery.getIncludeInferred();
	}

	public final void removeBinding(String name) {
		sailQuery.removeBinding(name);
	}

	public final void setBinding(String name, Value value) {
		sailQuery.setBinding(name, value);
	}

	public final void setDataset(Dataset dataset) {
		sailQuery.setDataset(dataset);
	}

	public final void setIncludeInferred(boolean includeInferred) {
		sailQuery.setIncludeInferred(includeInferred);
	}

	@Override
	public String toString() {
		return sailQuery.toString();
	}
}
