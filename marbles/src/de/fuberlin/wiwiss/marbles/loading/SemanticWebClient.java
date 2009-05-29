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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.repository.Repository;

import de.fuberlin.wiwiss.marbles.dataproviders.DataProvider;
import de.fuberlin.wiwiss.marbles.dataproviders.RevyuProvider;
import de.fuberlin.wiwiss.marbles.dataproviders.SindiceProvider;

/**
 * Provides functionalities to load URLs and to discover related data by means of data providers.
 * 
 * @author Christian Becker
 */
public class SemanticWebClient {
	
	/**
	 * Number of seconds to wait to load an URL 
	 */
	final int CONNECTION_TIMEOUT = 20;

	/**
	 * Number of seconds to wait for additional data once focal resource is loaded
	 */
	final int TIME_LIMIT_ADDITIONAL = 3;

	private Collection<DataProvider> dataProviders;
	
	private DereferencingTaskQueue uriQueue;
	private HttpClient httpClient;	
	private CacheController cacheController;

	/**
	 * Constructs a new <code>SemanticWebClient</code>
	 * 
	 * @param cacheController
	 * @param spongerProvider
	 * @param dataProviders
	 */
	public SemanticWebClient(CacheController cacheController, SpongerProvider spongerProvider, Collection<DataProvider> dataProviders) {
		this.cacheController = cacheController; 
		this.dataProviders = dataProviders;

		/* Set connection parameters */
		HttpConnectionManagerParams httpManagerParams = new HttpConnectionManagerParams();
		httpManagerParams.setConnectionTimeout(CONNECTION_TIMEOUT * 1000);
		httpManagerParams.setTcpNoDelay(true);
		httpManagerParams.setStaleCheckingEnabled(true);

		MultiThreadedHttpConnectionManager httpManager = new MultiThreadedHttpConnectionManager();
		httpManager.setParams(httpManagerParams);

		httpClient = new HttpClient(httpManager);
		uriQueue = new DereferencingTaskQueue(httpClient, spongerProvider, 10 /* maxThreads */, 500 * 1024 /* maxFileSize */);
	}
	
	/**
	 * Builds list of URLs to be loaded to learn more about a resource.
	 * Uses data providers.
	 * 
	 * @param resource
	 */
	private List<URI> getURLsForResource(Resource resource) {
		List<URI> urls = new ArrayList<URI>();
		
		/* Dereference the resource itself */
		try {
			if (!(resource instanceof BNode))
				urls.add(new URI(resource.toString(), true));
		} catch (URIException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
		/* and ask the data providers */
		for (DataProvider provider : dataProviders) {
			URI queryURL;
			if (null != (queryURL = provider.getQueryURL(resource)))
				urls.add(queryURL);
		}
		
		return urls;
	}
	
	/**
	 * Initiates a {@link DereferencerBatch} to retrieve data for a given resource 
	 * 
	 * @param resource
	 * @return List of URLs queries in the process; these may be looked up in the metadata store for details
	 */
	public List<URI> discoverResource(Resource resource, boolean wait) {
		List<URI>urlsToBeFetched = getURLsForResource(resource);
		DereferencerBatch dereferencerBatch = new DereferencerBatch(cacheController, uriQueue, dataProviders, resource, 1 /* maxSteps */);
		
		/* provide URLs to dereferencer */ 
		for (URI url : urlsToBeFetched) {
			try {
				dereferencerBatch.loadURL(url, 0 /* step */, 0 /* redirect step */, false /* don't force reload */);
			} catch (URIException e) {
				e.printStackTrace();
			}
		}
		
		/* Initiate link retrieval from any previous data */
		dereferencerBatch.processLinks(1);
		
		/* Wait loop with timeout */
		long timeStarted = System.currentTimeMillis();
	
		if (wait) {
			synchronized (dereferencerBatch) {
		       while (dereferencerBatch.hasPending(0)
		    		   || ((System.currentTimeMillis() - timeStarted < TIME_LIMIT_ADDITIONAL * 1000) && dereferencerBatch.hasPending()))
		       	{
				      try {
				    	  dereferencerBatch.wait(100);
				      }
				      catch ( InterruptedException e ) {
		 	          }
		       	}
			}
		}
		
		/*
		 * We stop waiting here so that the data retrieved so far can be shown to the client.
		 * Nonetheless, retrieval is not canceled - the client could refresh at a later time to get it
		 * (AJAX automation would make a lot of sense here), and additional information can be incorporated
		 * into subsequent views 
		 */
		return dereferencerBatch.getRetrievedURLs();
	}
	
	/**
	 * Loads a given URL into the cache using a {@link DereferencerBatch}
	 * 
	 * @param url	The URL to be loaded
	 * @param wait	If true, the method returns after the request has been processed
	 */
	public void loadURL(URI url, boolean wait) {
		DereferencerBatch dereferencerBatch = new DereferencerBatch(cacheController, uriQueue, dataProviders, null, 0 /* maxSteps (!) */);
		
		/* Provide URLs to dereferencer */ 
		try {
			dereferencerBatch.loadURL(url, 0 /* step */, 0 /* redirect step */, true /* force reload */);
		} catch (URIException e1) {
			e1.printStackTrace();
			return;
		}
				
		if (wait) {
			synchronized (dereferencerBatch) {
		       while (dereferencerBatch.hasPending())
		       	{
				      try {
				    	  dereferencerBatch.wait(100);
				      }
				      catch ( InterruptedException e ) {
		 	          }
		       	}
			}
		}
	}
}
