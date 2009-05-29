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
package de.fuberlin.wiwiss.marbles.loading;

import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.rio.helpers.RDFHandlerBase;

/**
 * An RDF Handler that adds all processed statements to a graph
 * @author Christian Becker
 */
public class RDFGraphInserter extends RDFHandlerBase {
	
	private Graph graph;
	private Resource[] contexts;
	
	public RDFGraphInserter(Graph graph, Resource ... contexts) {
		this.graph = graph;
		this.contexts = contexts;
	}
	
	public void handleStatement(Statement st) {
		graph.add(st.getSubject(), st.getPredicate(), st.getObject(), contexts);
	}
}
