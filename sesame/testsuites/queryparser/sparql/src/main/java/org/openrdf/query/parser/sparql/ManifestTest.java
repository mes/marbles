/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.sparql;

import java.net.URL;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;

public class ManifestTest extends TestCase {

	static final Logger logger = LoggerFactory.getLogger(ManifestTest.class);

	private static final boolean REMOTE = false;

	public static final String MANIFEST_FILE;

	static {
		if (REMOTE) {
			MANIFEST_FILE = "http://www.w3.org/2001/sw/DataAccess/tests/data-r2/manifest-evaluation.ttl";
		}
		else {
			MANIFEST_FILE = ManifestTest.class.getResource("/testcases-dawg/data-r2/manifest-evaluation.ttl").toString();
		}
	}

	public ManifestTest(String name) {
		super(name);
	}

	public static TestSuite suite()
		throws Exception
	{
		return suite(new SPARQLQueryTest.Factory());
	}

	public static TestSuite suite(SPARQLQueryTest.Factory factory)
		throws Exception
	{
		TestSuite suite = new TestSuite();

		Repository manifestRep = new SailRepository(new MemoryStore());
		manifestRep.initialize();
		RepositoryConnection con = manifestRep.getConnection();

		con.add(new URL(MANIFEST_FILE), MANIFEST_FILE, RDFFormat.TURTLE);

		String query = "SELECT DISTINCT manifestFile " + "FROM {x} rdf:first {manifestFile} "
				+ "USING NAMESPACE " + "  mf = <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#>, "
				+ "  qt = <http://www.w3.org/2001/sw/DataAccess/tests/test-query#>";

		TupleQueryResult manifestResults = con.prepareTupleQuery(QueryLanguage.SERQL, query, MANIFEST_FILE).evaluate();

		while (manifestResults.hasNext()) {
			BindingSet bindingSet = manifestResults.next();
			String manifestFile = bindingSet.getValue("manifestFile").toString();
			suite.addTest(SPARQLQueryTest.suite(manifestFile, factory));
		}

		manifestResults.close();
		con.close();
		manifestRep.shutDown();

		logger.info("Created aggregated test suite with " + suite.countTestCases() + " test cases.");
		return suite;
	}
}
