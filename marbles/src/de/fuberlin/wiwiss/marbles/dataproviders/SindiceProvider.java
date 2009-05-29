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

import info.aduna.iteration.Iterations;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import de.fuberlin.wiwiss.marbles.Constants;
import de.fuberlin.wiwiss.marbles.loading.CacheController;

/**
 * Queries Sindice.com for URLs that contain data about a given resource
 * 
 * @author Christian Becker
 */

public class SindiceProvider implements DataProvider {
	
	/**
	 * The URL of Sindice's lookup service 
	 */
	private final String SERVICE_URL = "http://api.sindice.com/v2/search?qt=term&q=";
	
	/**
	 * Generates the lookup URL for a given resource
	 * @param resource	The resource for which information is requested
	 * @return	Sindice lookup URL
	 */
	private String getSindiceLookupURL(Resource resource) {
		try {
			if (!(resource instanceof BNode))
				return SERVICE_URL + URLEncoder.encode(resource.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public org.apache.commons.httpclient.URI getQueryURL(Resource resource) {
		org.apache.commons.httpclient.URI queryURL = null;
			try {
				String sindiceLookupURL = getSindiceLookupURL(resource);
				if (sindiceLookupURL != null)
					queryURL =  new org.apache.commons.httpclient.URI(sindiceLookupURL, true /* is now escaped!! */);
			} catch (URIException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			return queryURL;
	}

	public List<URI> getURLsFromData(CacheController cacheController, RepositoryConnection conn, Resource resource) {
		String lookupURL;
		Resource lookupURLContext;
		List<Statement> statementsList = null;
		List<URI> urlsToBeFetched = new ArrayList<URI>();
		
		if (null == (lookupURL = getSindiceLookupURL(resource)))
			return null;

		String redirectURL = cacheController.getCachedRedirect(lookupURL);
		
		try {
			lookupURLContext = new URIImpl(redirectURL != null ? redirectURL : lookupURL);
		}
		catch (IllegalArgumentException e) {
			e.printStackTrace();
			return null;
		}
		
		RepositoryResult<Statement> statements;
		try {
			statements = conn.getStatements(null, new URIImpl(Constants.nsSindiceVocab + "link"), null, true, lookupURLContext);
			statementsList = Iterations.addAll(statements, new ArrayList<Statement>());
			statements.close();
		} catch (RepositoryException e1) {
			e1.printStackTrace();
			return null;
		}
		
		for (Statement st : statementsList) {
		      Value obj = st.getObject();
		      if (obj instanceof org.openrdf.model.URI && !urlsToBeFetched.contains(obj.toString()))
				try {
					urlsToBeFetched.add(new URI(obj.toString(), true));
				} catch (URIException e) {
					e.printStackTrace();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
		}		
		
		return urlsToBeFetched;
	}
}