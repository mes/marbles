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

import java.util.Date;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.SailException;
import org.openrdf.sail.inferencer.InferencerConnection;

import de.fuberlin.wiwiss.marbles.Constants;

/**
 * Implements caching of data retrieved from HTTP URLs using a Sesame repository
 * 
 * Uses HTTP in RDF namespace, but currently does not follow the ontology as
 * this would require deeper nesting with b-nodes, which is an overcomplication
 * for the current use cases
 * 
 * @see http://www.w3.org/TR/HTTP-in-RDF/
 * @author Christian Becker
 */
public class CacheController {
	
	/**
	 * The repository that holds retrieved data using URLs as graphs (contexts)  
	 */
	private SailRepository dataRepository;
	
	/**
	 * The repository that holds metadata about the contents of the {@link #dataRepository}
	 */
	private SailRepository metaDataRepository;
	
	private URI contextCacheDataURI;
	
	/**
	 * Response header fields that are to be stored in the metadata cache
	 */
	private final static String[] cachedHeaderFields = {"cache-control", "expires", "pragma", "location", "content-type"};
	
	/**
	 * Constructs a new <code>CacheController</code>
	 * @param dataRepository
	 * @param metaDataRepository
	 */
	public CacheController(SailRepository dataRepository, SailRepository metaDataRepository) {
		this.dataRepository = dataRepository;
		this.metaDataRepository = metaDataRepository;
		
		contextCacheDataURI = metaDataRepository.getValueFactory().createURI(Constants.contextCacheData);
	}
	
	/**
	 * Removes data for a given URL
	 * @param url	The URL whose data is to be removed
	 */
	public void removeData(String url) {
		/* Prevent deletion of base graphs */
		if (Constants.isBaseUrl(url))
			return;
		
		RepositoryConnection dataConn = null;
		RepositoryConnection metaDataConn = null;
		InferencerConnection inferencerConn = null;
		
		try {
			dataConn = dataRepository.getConnection();
			dataConn.setAutoCommit(false);
			inferencerConn = (InferencerConnection) dataRepository.getSail().getConnection();
			metaDataConn = metaDataRepository.getConnection();
			metaDataConn.setAutoCommit(false);

			URI urlDataContext = dataRepository.getValueFactory().createURI(url);
			URI urlInferencerContext = dataRepository.getSail().getValueFactory().createURI(url);
			URI urlMetadata = metaDataRepository.getValueFactory().createURI(url);

			inferencerConn.removeInferredStatement((Resource)null, null, null, urlInferencerContext);
			/* 
			 * Because inferencerConnection now holds the transaction lock on the store,
			 * we need to commit changes first or we'll run into a deadlock when removing statements
			 * using dataConn. They could be removed using inferencerConn; but the problem
			 * would remain for the adding of statements.
			 */ 
			inferencerConn.commit();			
			dataConn.remove((Resource)null, null, null, urlDataContext);
			metaDataConn.remove(urlMetadata, null, null, contextCacheDataURI);

			/* Commit */
			dataConn.commit();
			inferencerConn.commit();
			metaDataConn.commit();
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (SailException e) {
			e.printStackTrace();
		}
		finally {
			if (dataConn != null)
				try {
					dataConn.close();
				} catch (RepositoryException e) {
					e.printStackTrace();
				}
			if (metaDataConn != null)
				try {
					metaDataConn.close();
				} catch (RepositoryException e) {
					e.printStackTrace();
				}
			if (inferencerConn != null)
				try {
					inferencerConn.close();
				} catch (SailException e) {
					e.printStackTrace();
				}
		}
	}
	
	/**
	 * Adds retrieved URL data to the cache
	 * @param url	The URL that was retrieved
	 * @param data	The retrieved data
	 * @param method	Used to obtain metadata
	 */
	
	public void addURLData(String url, Graph data, HttpMethod method) {
		RepositoryConnection dataConn = null;
		InferencerConnection inferencerConn = null;
		RepositoryConnection metaDataConn = null;

		try {
			dataConn = dataRepository.getConnection();
			dataConn.setAutoCommit(false);
			inferencerConn = (InferencerConnection) dataRepository.getSail().getConnection();
			metaDataConn = metaDataRepository.getConnection();
			metaDataConn.setAutoCommit(false);

			URI urlDataContext = dataRepository.getValueFactory().createURI(url);
			URI urlInferencerContext = dataRepository.getValueFactory().createURI(url);
			URI urlMetadata = metaDataRepository.getValueFactory().createURI(url);
			
			/* Remove cached data and previous metadata */
			inferencerConn.removeInferredStatement((Resource)null, null, null, urlInferencerContext);
			/* 
			 * Because inferencerConnection now holds the transaction lock on the store,
			 * we need to commit changes first or we'll run into a deadlock when removing statements
			 * using dataConn. They could be removed using inferencerConn; but the problem
			 * would remain for the adding of statements.
			 */ 
			inferencerConn.commit();
			dataConn.remove((Resource)null, null, null, urlDataContext);
			metaDataConn.remove(urlMetadata, null, null, contextCacheDataURI);
			
			/* Add retrieved data */
			if (data != null)
				dataConn.add(data);
			
			/* Add metadata */
			if (method != null) {
				for (String headerField : cachedHeaderFields) {
					Header header;
					
					if (null != (header = method.getResponseHeader(headerField))) {
						metaDataConn.add(urlMetadata, 
								metaDataRepository.getValueFactory().createURI(Constants.nsHTTP, headerField),
								metaDataRepository.getValueFactory().createLiteral(header.getValue()),
								contextCacheDataURI);
					}
				}
			
				/* Add status code */
				if (null != method.getStatusLine()) /* or we'll run into a NullPointerException when calling getStatusCode() */ 
					metaDataConn.add(urlMetadata, 
							metaDataRepository.getValueFactory().createURI(Constants.nsHTTP, "responseCode"),
							metaDataRepository.getValueFactory().createLiteral(method.getStatusCode()),
							contextCacheDataURI);
			}
			
			/* We'll make use of the date header to specify when the document was retrieved */
			metaDataConn.add(urlMetadata, 
					metaDataRepository.getValueFactory().createURI(Constants.nsHTTP, "date"),
					metaDataRepository.getValueFactory().createLiteral(DateUtil.formatDate(new Date())),
					contextCacheDataURI);
						
			/* Commit */
			dataConn.commit();
			inferencerConn.commit();
			metaDataConn.commit();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (SailException e) {
			e.printStackTrace();
		}
		finally {
			if (dataConn != null)
				try {
					dataConn.close();
				} catch (RepositoryException e) {
					e.printStackTrace();
				}
			if (inferencerConn != null)
				try {
					inferencerConn.close();
				} catch (SailException e) {
					e.printStackTrace();
				}				
			if (metaDataConn != null)
				try {
					metaDataConn.close();
				} catch (RepositoryException e) {
					e.printStackTrace();
				}
		}
	}

	/**
	 * Determines whether the cache holds a valid copy of an URL's data  
	 * @param url	The URL of interest
	 * @return	true, if a valid copy is present
	 */
	public boolean hasURLData(String url) {
		boolean hasData = false;
		RepositoryConnection metaDataConn = null;
		
		try {
			metaDataConn = metaDataRepository.getConnection();
			URI metaUrlContext = metaDataRepository.getValueFactory().createURI(url);

			Date now = new Date();
			
			/* This is always set, so if it's not set, the URL has not been loaded */
			String date = getCachedHeaderDataValue(metaDataConn, metaUrlContext, "date");
			if (date == null)
				return false;
			
			Date dateRetrieved = DateUtil.parseDate(date);
			
			/*
			 * Due to performance considerations, don't retrieve an URL
			 * twice within 24 hours (response headers are deliberately ignored here!!)
			 */
			if (dateRetrieved.getTime() + 1000 * 60 * 60 * 24 > now.getTime())
				return true;
						
			/*
			 * Check several caching indicators
			 * @see http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
			 */
			String pragma = getCachedHeaderDataValue(metaDataConn, metaUrlContext, "pragma");
			if ((pragma != null 
					&& pragma.equalsIgnoreCase("no-cache")))
					return false;
					
			Header cacheControlHeader = getCachedHeaderData(metaDataConn, metaUrlContext, "cache-control");
			if (cacheControlHeader != null) {
				for (HeaderElement element : cacheControlHeader.getElements()) {
					if (element.getName().equalsIgnoreCase("private")
							|| element.getName().equalsIgnoreCase("no-cache")
							|| element.getName().equalsIgnoreCase("no-store")
							|| element.getName().equalsIgnoreCase("must-revalidate")
							|| element.getName().equalsIgnoreCase("proxy-revalidate"))
						return false;

					if (element.getName().equalsIgnoreCase("max-age") || element.getName().equalsIgnoreCase("s-max-age")) {
						try {
							long maxAge = Long.parseLong(element.getValue());
							Date expiryDate = new Date(dateRetrieved.getTime() + maxAge * 1000);
							if (now.after(expiryDate))
								return false;
						}
						catch (NumberFormatException e) {
							e.printStackTrace();
						}
					}
				}
			}
			
			String expires = getCachedHeaderDataValue(metaDataConn, metaUrlContext, "expires");
			if (expires != null) {
				Date expiryDate = DateUtil.parseDate(expires);
				if (now.after(expiryDate))
					return false;
			}

			hasData = true;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (DateParseException e) {
			e.printStackTrace();
		}
		finally {
			if (metaDataConn != null)
				try {
					metaDataConn.close();
				} catch (RepositoryException e) {
					e.printStackTrace();
				}
		}

		return hasData;
	}
	
	/**
	 * Provides redirection targets from the cache
	 * 
	 * @param uri	The URI of interest
	 * @return	The redirection target, or <code>null</code> if there is none
	 */
	public String getCachedRedirect(String uri) {
		RepositoryConnection metaDataConn = null;
		String redirectLocation = null;
		
		try {
			metaDataConn = metaDataRepository.getConnection();
			Header header = getCachedHeaderData(metaDataConn, uri, "location");
			if (header != null)
				redirectLocation = header.getValue();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (metaDataConn != null)
					metaDataConn.close();
			} catch (RepositoryException e) {
				e.printStackTrace();
			}
		}
		return redirectLocation;
	}
	
	/**
	 * Retrieves a cached response header field from the metadata cache
	 *  
	 * @param metaDataConn	A connection to the metadata repository
	 * @param mainResource	The resource of interest
	 * @param headerField	The header field of interest
	 * @return	Header data
	 * @throws RepositoryException
	 */
	public Header getCachedHeaderData(RepositoryConnection metaDataConn, Resource mainResource, String headerField) throws RepositoryException {
		Header header = null;
		RepositoryResult<Statement> results = metaDataConn.getStatements(mainResource,
						   dataRepository.getValueFactory().createURI(Constants.nsHTTP, headerField),
						   null, false, contextCacheDataURI);
		
		if (results.hasNext()) {
			Statement st = results.next();
			if (st.getObject() instanceof Literal)
				header = new Header(headerField, ((Literal)st.getObject()).getLabel());
		}

		results.close();
		return header;
	}
	
	/**
	 * Retrieves a cached response header field from the metadata cache
	 * 
	 * @param metaDataConn	A connection to the metadata repository
	 * @param url			The resource of interest
	 * @param headerField	The header field of interest
	 * @return	Header data
	 * @throws RepositoryException
	 */
	public Header getCachedHeaderData(RepositoryConnection metaDataConn, String url, String headerField) throws RepositoryException {
		try {
			URI metaUrlContext = metaDataRepository.getValueFactory().createURI(url);
			return getCachedHeaderData(metaDataConn, metaUrlContext, headerField);
		}
		catch (IllegalArgumentException e) {
			e.printStackTrace();
			return null;
		}
	}	
	

	/**
	 * Retrieves the value of a cached response header field from the metadata cache
	 * @param metaDataConn	A connection to the metadata repository
	 * @param mainResource	The resource of interest
	 * @param headerField	The header field of interest
	 * @return	Header data
	 * @throws RepositoryException
	 */
	public String getCachedHeaderDataValue(RepositoryConnection metaDataConn, Resource mainResource, String headerField) throws RepositoryException {
		Header header = getCachedHeaderData(metaDataConn, mainResource, headerField);
		return (header == null ? null : header.getValue()); 
	}

	/**
	 * Retrieves the value of a cached response header field from the metadata cache
	 * 
	 * @param metaDataConn	A connection to the metadata repository
	 * @param url			The resource of interest
	 * @param headerField	The header field of interest
	 * @return	Header data
	 * @throws RepositoryException
	 */
	public String getCachedHeaderDataValue(RepositoryConnection metaDataConn, String url, String headerField) throws RepositoryException {
		Header header = getCachedHeaderData(metaDataConn, url, headerField);
		return (header == null ? null : header.getValue()); 
	}

	/**
	 * @return	The repository that holds retrieved data using URLs as graphs (contexts)  
	 */
	public Repository getDataRepository() {
		return dataRepository;
	}

	/**
	 * @return	The repository that holds metadata about the contents of the {@link #dataRepository}
	 */
	public Repository getMetaDataRepository() {
		return metaDataRepository;
	}
}