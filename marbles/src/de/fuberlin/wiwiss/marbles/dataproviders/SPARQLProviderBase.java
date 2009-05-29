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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.commons.httpclient.URIException;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.repository.RepositoryConnection;

import de.fuberlin.wiwiss.marbles.loading.CacheController;

/**
 * Abstract class to connect SPARQL data providers
 * 
 * @author Christian Becker
 */
public abstract class SPARQLProviderBase implements DataProvider {
	
	/**
	 * @return	The service's SPARQL endpoint URL
	 */
	protected abstract String getEndpoint();
	
	/**
	 * Generates a SPARQL query to retrieve appropriate data from the data source
	 * for the requested resource
	 * 
	 * @param resource	The resource for which information is requested
	 * @return	SPARQL query text
	 */
	protected abstract String getQuery(Resource resource);
	
	public org.apache.commons.httpclient.URI getQueryURL(Resource resource) {
		org.apache.commons.httpclient.URI queryURL = null;
			try {
				if (!(resource instanceof BNode))
					queryURL =  new org.apache.commons.httpclient.URI(getEndpoint() + "?query=" + URLEncoder.encode(getQuery(resource), "UTF-8"), true /* is now escaped!! */);
			} catch (URIException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return queryURL;
	}
	
	/**
	 * Not implemented
	 */
	public List<org.apache.commons.httpclient.URI> getURLsFromData(CacheController cacheController, RepositoryConnection conn, Resource resource) {
		return null;
	}
}