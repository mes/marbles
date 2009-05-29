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

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.fuberlin.wiwiss.marbles.loading.CacheController;
import de.fuberlin.wiwiss.marbles.loading.HttpStatusCodes;

/**
 * Represents a source graph whose status can be rendered as an element in a DOM tree 
 *
 * @author Christian Becker
 */

public class Source {
	
	/**
	 * Cached response header fields that are passed as attributes of the element
	 */
	public String[] exportedCacheFields = {"date", "responseCode"};
	
	/**
	 * The URI of the source graph
	 */
	private String uri;
	
	/**
	 * Icon identifier (without file extension)
	 */
	private String icon = null;
	
	public Source(String uri) {
		this.uri = uri;
	}

	/**
	 * Exports status data as a DOM <code>Element</code>
	 * @param doc
	 * @param cacheController
	 * @param metaDataConn
	 * @return
	 * @throws RepositoryException
	 */
	public Element toElement(Document doc, CacheController cacheController, RepositoryConnection metaDataConn) throws RepositoryException {
		Element sourceElement = doc.createElementNS(Constants.nsFresnelView, "source");
		
		addTextNode(doc, sourceElement, "uri", uri);
		addTextNode(doc, sourceElement, "icon", icon);
		
		if (cacheController.hasURLData(uri)) {
			for (String field : exportedCacheFields) {
		    	String value = cacheController.getCachedHeaderDataValue(metaDataConn, uri, field);
				addTextNode(doc, sourceElement, field, value);
			}
			
			/* Parse resultCode */
			String reponseCodeString = cacheController.getCachedHeaderDataValue(metaDataConn, new URIImpl(uri), "responseCode");
			int responseCode = (reponseCodeString == null ? 0 : Integer.parseInt(reponseCodeString)); 
			addTextNode(doc, sourceElement, "status", HttpStatusCodes.toString(responseCode));
		}
		else
			addTextNode(doc, sourceElement, "status", Constants.isBaseUrl(uri) ? "pre-loaded" : "pending");
		
    	return sourceElement;
	}
	
	/**
	 * Determines an icon to be used for this data source using {@link Constants#knownSources},
	 * resorting to colored icons if the source is unknown
	 * 
	 * @param colorIndex
	 * @return
	 */
	public int determineIcon(int colorIndex) {
    	if (null != icon)
    		return colorIndex;
    	
    	/* Try to find identifier from knownSources */
    	for (String source : Constants.knownSources.keySet()) {
    		if (uri.startsWith(source)) {
    			icon = Constants.knownSources.get(source);
    			return colorIndex;
    		}
    	}
    	
    	/* Alternatively, assign rotating colored icon */
    	icon = Integer.toString((colorIndex++ % Constants.numSourceColorIcons)+1);
    	return colorIndex;
	}

	private void addTextNode(Document doc, Element sourceElement, String field, String value) {
    	Element element = doc.createElementNS(Constants.nsFresnelView, field);
    	element.setTextContent(value);
    	sourceElement.appendChild(element);
	}
	
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Source && uri.equals(((Source)obj).getUri()));
	}	
	
	public String getUri() {
		return uri;
	}
	
	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}
}
