/*
 *   Copyright (c) 2009, MediaEvent Services GmbH & Co. KG
 *   http://mediaeventservices.com
 *   
 *   This file is part of Marbles.
 *
 *   Marbles is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Marbles is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Marbles.  If not, see <http://www.gnu.org/licenses/>.
 *   
 */
package de.fuberlin.wiwiss.marbles;

import java.io.OutputStream;
import java.io.Writer;
import java.util.HashMap;

import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.rdfxml.RDFXMLWriter;

/**
 * An {@link RDFXMLWriter} that skips duplicate statements.
 * <code>RDFXMLWriterUnique</code> checks for exactly duplicated statements.
 * Furthermore, it accepts only statements for one resource of an <code>owl:sameAs</code>
 * alias group, thereby assuming that all predicates, including <code>owl:sameAs</code>, 
 * are mirrored among all group members (this is the expected behavior of SameAsInferencer).
 * 
 * Note: The filtering algorithm requires <code>owl:sameAs</code> statements to be passed before any filtering can take place.
 *  
 * @author Christian Becker
 */
public class RDFXMLWriterUnique extends RDFXMLWriter {
	
	Graph statementsWritten;
	HashMap<Value, Boolean> acceptedResources;
	RepositoryConnection conn;

	/**
	 * Creates a new <code>RDFXMLWriterUnique</code> that will write to the supplied <code>OutputStream</code>.
	 * 
	 * @param out The OutputStream to write the RDF/XML document to.
	 */
	public RDFXMLWriterUnique(OutputStream out) {
		super(out);
		statementsWritten = new GraphImpl();
		acceptedResources = new HashMap<Value, Boolean>();
	}

	/**
	 * Creates a new RDFXMLWriterUnique that will write to the supplied Writer.
	 * 
	 * @param writer The Writer to write the RDF/XML document to.
	 */
	public RDFXMLWriterUnique(Writer writer) {
		super(writer);
		statementsWritten = new GraphImpl();
		acceptedResources = new HashMap<Value, Boolean>();
	}

	@Override
	public void handleStatement(Statement st) throws RDFHandlerException {
		/* Check for duplicate statement - Sesame RDBMS provides these despite usage of the <code>DISTINCT</code> keyword */
		if (statementsWritten.contains(st))
			return;
		
		Boolean isAccepted = acceptedResources.get(st.getSubject());
		if (isAccepted != null && !isAccepted)
			return;
		
		/* Handle <code>owl:sameAs</code> statement - accepting this resource, but not its alias */
		if (st.getPredicate().equals(OWL.SAMEAS)) {
			acceptedResources.put(st.getSubject(), true);
			acceptedResources.put(st.getObject(), false);
		}
		
		statementsWritten.add(st);
		super.handleStatement(st);
	}
}