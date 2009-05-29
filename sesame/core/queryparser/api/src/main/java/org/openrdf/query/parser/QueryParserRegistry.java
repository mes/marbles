/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser;

import info.aduna.lang.service.ServiceRegistry;

import org.openrdf.query.QueryLanguage;

/**
 * A registry that keeps track of the available {@link QueryParserFactory}s.
 * 
 * @author Arjohn Kampman
 */
public class QueryParserRegistry extends ServiceRegistry<QueryLanguage, QueryParserFactory> {

	private static QueryParserRegistry defaultRegistry;

	/**
	 * Gets the default QueryParserRegistry.
	 * 
	 * @return The default registry.
	 */
	public static synchronized QueryParserRegistry getInstance() {
		if (defaultRegistry == null) {
			defaultRegistry = new QueryParserRegistry();
		}

		return defaultRegistry;
	}

	public QueryParserRegistry() {
		super(QueryParserFactory.class);
	}

	@Override
	protected QueryLanguage getKey(QueryParserFactory factory) {
		return factory.getQueryLanguage();
	}
}