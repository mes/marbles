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

import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.repository.RepositoryConnection;

import de.fuberlin.wiwiss.marbles.loading.CacheController;

/**
 * Data providers supply data for a given URI from individual web sources.
 * 
 * @author Christian Becker
 */
public interface DataProvider {
	/**
	 * Retrieves the URL to query the remote data provider about a given resource 
	 * @param resource	The resource for which information is requested
	 * @return	The URL to query
	 */
	public org.apache.commons.httpclient.URI getQueryURL(Resource resource);

	/**
	 * Processes an RDF result provided by search engine providers for further URLs that are to be loaded
	 * @param cacheController	The CacheController used to resolve redirects
	 * @param conn	A connection to a repository with the returned RDF result
	 * @param	resource	The resource for which information is requested 
	 * @return	A list of URLs or <code>null</code> if this functionality is not implemented
	 */
	public List<org.apache.commons.httpclient.URI> getURLsFromData(CacheController cacheController, RepositoryConnection conn, Resource resource);
}