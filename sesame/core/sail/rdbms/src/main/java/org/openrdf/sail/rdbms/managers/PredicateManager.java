package org.openrdf.sail.rdbms.managers;

import java.sql.SQLException;
import java.util.Map;

import info.aduna.collections.LRUMap;

import org.openrdf.sail.rdbms.model.RdbmsURI;

public class PredicateManager {

	private UriManager uris;

	private Map<Number, String> predicates = new LRUMap<Number, String>(64);

	public void setUriManager(UriManager uris) {
		this.uris = uris;
	}

	public Number getIdOfPredicate(RdbmsURI uri)
		throws SQLException, InterruptedException
	{
		Number id = uris.getInternalId(uri);
		synchronized (predicates) {
			predicates.put(id, uri.stringValue());
		}
		return id;
	}

	public String getPredicateUri(Number id) {
		synchronized (predicates) {
			return predicates.get(id);
		}
	}

	public void remove(Number id) {
		synchronized (predicates) {
			predicates.remove(id);
		}
	}
}
