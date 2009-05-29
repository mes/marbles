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

/**
 * Functionalities to identify RDF content types 
 * @author Christian Becker
 */

public class ContentTypes {
	
	/**
	 * @param contentType
	 * @return	true, if the provided content type is indicative of RDF/XML content
	 */
	public static boolean isRDFXML(String contentType) {
		return (contentType.startsWith("application/rdf+xml")
				|| contentType.startsWith("text/xml")
				|| contentType.startsWith("text/rdf+xml")
				|| contentType.startsWith("application/xml")
				|| contentType.startsWith("application/rss+xml")
				/*|| contentType.startsWith("text/plain")*/);
	}
	
	/**
	 * @param contentType
	 * @return	true, if the provided content type is indicative of RDF/N3 content
	 */
	public static boolean isRDFN3(String contentType) {
		return (contentType.startsWith("application/n3")
				|| contentType.startsWith("text/rdf+n3"));
	}
	
	/**
	 * @param contentType
	 * @return	true, if the provided content type is indicative of RDF/TTL content
	 */
	public static boolean isRDFTTL(String contentType) {
		return (contentType.startsWith("application/turtle")
				|| contentType.startsWith("application/x-turtle"));
	}

	/**
	 * @param contentType
	 * @return	true, if the provided content type is indicative of RDF content
	 */
	public static boolean isRDF(String contentType) {
		return isRDFXML(contentType) || isRDFN3(contentType) || isRDFTTL(contentType);
	}
}
