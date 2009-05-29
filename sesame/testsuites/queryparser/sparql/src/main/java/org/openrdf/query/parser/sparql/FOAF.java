/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.sparql;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Constants for FOAF primitives and for the FOAF namespace.
 */
public class FOAF {

	public static final String NAMESPACE = "http://xmlns.com/foaf/0.1/";

	public final static URI PERSON;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		PERSON = factory.createURI(FOAF.NAMESPACE, "Person");
	}
}
