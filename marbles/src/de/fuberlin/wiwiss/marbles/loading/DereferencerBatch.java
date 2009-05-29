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

import info.aduna.iteration.Iterations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import de.fuberlin.wiwiss.marbles.Constants;
import de.fuberlin.wiwiss.marbles.dataproviders.DataProvider;

/**
 * Starting with one URL, the DereferencerBatch handles the nested retrieval of data
 * by following known predicates in retrieved data, and processing retrieval results 
 * with data providers.
 *  
 * @author Christian Becker
 */
public class DereferencerBatch implements DereferencingListener {
	
	private List<ExtendedDereferencingTask> pendingTasks = new ArrayList<ExtendedDereferencingTask>();
	private List<URI> retrievedURLs = new ArrayList<URI>();
	private CacheController cacheController;
	private Resource mainResource;
	private DereferencingTaskQueue uriQueue;
	private Collection<DataProvider> dataProviders;
	private int maxSteps;
	
	/**
	 * Constructs a new <code>DereferencerBatch</code>
	 * @param cacheController
	 * @param uriQueue
	 * @param dataProviders
	 * @param mainResource
	 * @param maxSteps
	 */
	public DereferencerBatch(CacheController cacheController, DereferencingTaskQueue uriQueue, Collection<DataProvider> dataProviders, Resource mainResource, int maxSteps) {
		this.cacheController = cacheController;
		this.mainResource = mainResource;
		this.uriQueue = uriQueue;
		this.dataProviders = dataProviders;
		this.maxSteps = maxSteps;
	}

	/**
	 * Loads URL if not yet loaded
	 * 
	 * @param url	The URL to load
	 * @param step	The distance from the focal resource
	 * @param redirectCount	The number of redirects performed in the course of this individual request
	 * @param forceReload	Set this to true if the URL should be loaded even if a valid copy is already in the cache
	 * @throws URIException
	 */
	public void loadURL(URI url, int step, int redirectCount, boolean forceReload) throws URIException {
		if (step > maxSteps)
			return;
		
		/* Cut off local names from URI */ 
		url.setFragment("");

		if (retrievedURLs.contains(url)) /* force reload doesn't apply on batch level, as they are short-lived and this could cause infinite loops */
			return;
		
		if (!forceReload && cacheController.hasURLData(url.toString())) {
			/* Treat as retrieved when reading from cache */
			retrievedURLs.add(url);
			
			String redirect = cacheController.getCachedRedirect(url.toString());
			
			/* Process a cached redirect */
			if (redirect != null) {
				URI redirectUrl = new URI(url, redirect, true);
				loadURL(redirectUrl, step, redirectCount + 1, forceReload);
			}
			else {
				/* Data is already loaded; try to find new links within it */
				try {
					org.openrdf.model.URI sesameUri = new URIImpl(url.toString()); 
					processLinks(step + 1, sesameUri);
				}
				catch (IllegalArgumentException e) {
					e.printStackTrace();
				}
			}
		}
		else {
			/* No data about this URL; get it */
			ExtendedDereferencingTask task = new ExtendedDereferencingTask(this, url.toString(), step, redirectCount, forceReload);
			if (uriQueue.addTask(task)) {
				pendingTasks.add(task);
				retrievedURLs.add(url);
			}
		}
	}
	
	/**
	 * Determines whether requests are pending below a specified step level
	 * @param maxLevel	Maximum step level to consider
	 * @return	true, if requests are pending
	 */
	public boolean hasPending(int maxLevel) {
		boolean pending = false;
		
		for (ExtendedDereferencingTask task : pendingTasks) {
			if (task.getStep() <= maxLevel && !task.isDone()) {
				pending = true;
				break;
			}
		}
		return pending;
	}
	
	/**
	 * Determines whether any requests are pending
	 * @return	true, if requests are pending
	 */
	public boolean hasPending() {
		return hasPending(Integer.MAX_VALUE);
	}
	
	/*
	 * TODO Determine whether a retrieval batch was executed successfully
	 * Problem: To do this, {@link DereferencingResult} should be a member of {@link DereferencingTask}, not vice versa 
	 */
	/*public boolean wasSuccess() {
		boolean success = true;
		
		for (ExtendedDereferencingTask task : pendingTasks) {
			if (task.isDone() && task.) {
				pending = true;
				break;
			}
		}
		return pending;
	}*/
	
	/**
	 * Called by {@link DereferencerThread} once data has been retrieved.
	 * Handles insertion into cache, processes redirects, and initiates following of known links
	 * for the retrieved URL using {@link #processLinks(int, Resource...)} 
	 */
	public void dereferenced(DereferencingResult result) {
		ExtendedDereferencingTask task = (ExtendedDereferencingTask) result.getTask();
		
		/* Add to cache - including header data for redirects */
		cacheController.addURLData(result.getURI(), result.getResultData(), result.getMethod());

		/* Handle known redirect */
		if (null != result.getMethod() && null != result.getMethod().getStatusLine()) /* against NullPointerException with getStatusCode() */ {
			int resultCode = result.getMethod().getStatusCode();
			if (HttpStatusCodes.isRedirect(resultCode)) {
				Header locationHeader;
		        if (null != (locationHeader = result.getMethod().getResponseHeader("location"))) {
					try {
						loadURL(new URI(new URI(result.getURI(), true), locationHeader.getValue(), true), task.getStep(), task.getRedirectStep() + 1, task.isForceReload());
					} catch (URIException e) {
						e.printStackTrace();
					} 
		        }
			}
		}

		task.setDone(true);
		
		/* Wake up parent */
		synchronized(this) {
			notify();
		}
		
		/* find new links */
		if (result.isSuccess())
			processLinks(task.getStep() + 1, new URIImpl(result.getURI()));
	}
	
	/**
	 * Identifies known links from loaded data and submits them to <code>{@link #loadURL(URI, int, int, boolean)}</code> 
	 * @param step	Current step level
	 * @param contexts	Contexts that are to be considered to find links
	 */
	public void processLinks(int step, Resource ... contexts) {
		if (step > maxSteps)
			return;
		
		RepositoryConnection conn = null;
		try {
			conn = cacheController.getDataRepository().getConnection();
			for (org.openrdf.model.URI predicate : Constants.interestingPredicates) {
				List<Statement> statementsList;
				RepositoryResult<Statement> statements = conn.getStatements(mainResource, predicate, null /* obj */, true /* includeInferred */, contexts);
				statementsList = Iterations.addAll(statements, new ArrayList<Statement>());
				statements.close();
				
				/* Also include inverse properties */
				statements = conn.getStatements(null, predicate, mainResource, true /* includeInferred */, contexts);
				Iterations.addAll(statements, statementsList);
				statements.close();
				
				List<URI> urlsToBeFetched = new ArrayList<URI>();
	
				for (Statement st : statementsList) {
				      Value obj = (st.getSubject().equals(mainResource) ? st.getObject() : st.getSubject());
				      if (obj instanceof org.openrdf.model.URI && !urlsToBeFetched.contains(obj.toString()))
						try {
							urlsToBeFetched.add(new URI(obj.toString(), true));
						} catch (URIException e) {
							e.printStackTrace();
						} catch (NullPointerException e) {
							e.printStackTrace();
						}
				}
				
				/* Ask data providers */
				for (DataProvider p : dataProviders) {
					List<URI> newURLs = p.getURLsFromData(cacheController, conn, mainResource);
					if (newURLs != null)
						urlsToBeFetched.addAll(newURLs);
				}
				
				/* Load URLs */
				for (URI url : urlsToBeFetched) {
						try {
							loadURL(url, step, 0 /* redirectStep */, false);
						} catch (URIException e) {
							e.printStackTrace();
						}
				}
			}
		}
		catch (RepositoryException e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (conn != null)
					conn.close();
			} catch (RepositoryException e) {
				e.printStackTrace();
			}
		}
		
	}

	public List<URI> getRetrievedURLs() {
		return retrievedURLs;
	}
}
