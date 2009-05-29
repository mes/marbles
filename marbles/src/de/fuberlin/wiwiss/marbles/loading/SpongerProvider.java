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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Allows to utilize the Sponger functionalities of a local Virtuoso Server
 * installation, which allow RDF extraction from a multitude of formats including GRDDL,
 * RDFa and Microformats
 * 
 * @author Christian Becker
 */
public class SpongerProvider {
	
	/**
	 * The URL of the sponger service 
	 */
	private String spongerServiceURL;
	
	/**
	 * Constructs a new <code>SpongerProvider</code>
	 * @param spongerServiceURL	The URL of the sponger service
	 */
	public SpongerProvider(String spongerServiceURL) {
		this.spongerServiceURL = spongerServiceURL;
	}
		
	/**
	 * Generates the lookup URL for a given resource
	 * @param resource	The resource for which information is requested
	 * @return	Sponger lookup URL
	 */
	public String getQueryURL(String resource) {
		String queryURL = null;
		try {
			queryURL = spongerServiceURL + "?force=rdf&url=" + URLEncoder.encode(resource, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return queryURL;
	}
}