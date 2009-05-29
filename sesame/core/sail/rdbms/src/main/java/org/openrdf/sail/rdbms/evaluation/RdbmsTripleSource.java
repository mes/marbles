/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.evaluation;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.sail.SailException;
import org.openrdf.sail.rdbms.RdbmsTripleRepository;
import org.openrdf.sail.rdbms.RdbmsValueFactory;
import org.openrdf.sail.rdbms.model.RdbmsResource;
import org.openrdf.sail.rdbms.model.RdbmsURI;
import org.openrdf.sail.rdbms.model.RdbmsValue;

/**
 * Proxies request to a {@link RdbmsTripleRepository}.
 * 
 * @author James Leigh
 * 
 */
public class RdbmsTripleSource implements TripleSource {
	private RdbmsTripleRepository triples;

	public RdbmsTripleSource(RdbmsTripleRepository triples) {
		super();
		this.triples = triples;
	}

	public RdbmsValueFactory getValueFactory() {
		return triples.getValueFactory();
	}

	public CloseableIteration getStatements(Resource subj, URI pred, Value obj,
			Resource... contexts) throws QueryEvaluationException {
		try {
			RdbmsValueFactory vf = triples.getValueFactory();
			RdbmsResource s = vf.asRdbmsResource(subj);
			RdbmsURI p = vf.asRdbmsURI(pred);
			RdbmsValue o = vf.asRdbmsValue(obj, pred);
			RdbmsResource[] c = vf.asRdbmsResource(contexts);
			return triples.find(s, p, o, false, c);
		} catch (SailException e) {
			throw new QueryEvaluationException(e);
		}
	}

}
