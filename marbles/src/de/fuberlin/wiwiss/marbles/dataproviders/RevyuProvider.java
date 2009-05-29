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
package de.fuberlin.wiwiss.marbles.dataproviders;

import org.openrdf.model.Resource;

/**
 * Queries Revyu.com for reviews about a given resource
 * 
 * @author Christian Becker
 */
public class RevyuProvider extends SPARQLProviderBase {
	
	@Override
	protected String getEndpoint() {
		return "http://revyu.com/sparql";
	}
	
	@Override
	protected String getQuery(Resource resource) {
	  /**
	   * Gets everything about a thing and its review from Revyu's RDF store
	   * Note that some content, e.g. labels, are generated dynamically when
	   * the respective item is dereferenced, so that <code>hasReview</code>
	   * predicates need to be followed in order to retrieve all information 
	   */
	  String query = 
		  "PREFIX rev: <http://purl.org/stuff/rev#>\n" +
		  "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +  
		  "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
		  
		  "CONSTRUCT { ?rev ?p ?o . ?thing ?q ?r }\n" +
		  "WHERE {\n" + 
		  "	?thing owl:sameAs <" + resource.toString() + "> .\n" + 
		  " ?thing rev:hasReview ?rev .\n" +
		  " ?rev ?p ?o .\n" +
		  " ?thing ?q ?r";
	   
	  return query;
	}
}