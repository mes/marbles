/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.ntriples;

import java.io.InputStream;

import junit.framework.TestCase;

import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.RDFHandlerBase;

/**
 * JUnit test for the N-Triples parser.
 * 
 * @author Arjohn Kampman
 */
public class NTriplesParserTest extends TestCase {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static String NTRIPLES_TEST_URL = "http://www.w3.org/2000/10/rdf-tests/rdfcore/ntriples/test.nt";

	private static String NTRIPLES_TEST_FILE = "/testcases/ntriples/test.nt";

	/*---------*
	 * Methods *
	 *---------*/

	public void testNTriplesFile()
		throws Exception
	{
		NTriplesParser turtleParser = new NTriplesParser();
		turtleParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);
		turtleParser.setRDFHandler(new RDFHandlerBase());

		InputStream in = NTriplesParser.class.getResourceAsStream(NTRIPLES_TEST_FILE);
		try {
			turtleParser.parse(in, NTRIPLES_TEST_URL);
		}
		catch (RDFParseException e) {
			fail("Failed to parse N-Triples test document: " + e.getMessage());
		}
		finally {
			in.close();
		}
	}
}
