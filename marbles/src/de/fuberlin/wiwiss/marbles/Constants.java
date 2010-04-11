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

import java.util.HashMap;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDFS;

/**
 * Commonly used constants 
 * @author Christian Becker
 */

public final class Constants {
	public final static String userAgent = "marbles/1.0 (http://marbles.sourceforge.net)";
	
	/* External namespaces */
    public final static String nsDBpedia = "http://dbpedia.org/resource/";
    public final static String nsDBpediaProp = "http://dbpedia.org/property/";
    public final static String nsFOAF = "http://xmlns.com/foaf/0.1/";
    public final static String nsDOAP = "http://usefulinc.com/ns/doap#";
    public final static String nsDC = "http://purl.org/dc/elements/1.1/";
    public final static String nsSWRC = "http://swrc.ontoware.org/ontology#";
    public final static String nsSindiceVocab = "http://sindice.com/vocab/search#";
    public final static String nsReview = "http://purl.org/stuff/rev#";
    public final static String nsDrugbank = "http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/";
    public final static String nsDailyMed = "http://www4.wiwiss.fu-berlin.de/dailymed/resource/dailymed/";
    public final static String nsDiseasome = "http://www4.wiwiss.fu-berlin.de/diseasome/resource/diseasome/"; 

    /**
     * @see http://www.w3.org/TR/HTTP-in-RDF/
     */
    public final static String nsHTTP = "http://www.w3.org/2006/http#";
   
    /* Application-internal namespaces */
    public final static String nsFresnelExt = "http://beckr.org/fresnel/";
    public final static String nsFresnelView = "http://beckr.org/fresnelview/";
    public final static String nsSessionBase = "http://beckr.org/DBpediaMobile/sessions/";
    public final static String nsBaseGraphs = "http://beckr.org/graphs/";
    public final static String contextCacheData = "http://beckr.org/DBpediaMobile/cachedata";
    
    /**
     * Interesting predicates to follow
     */
	public final static URI[] interestingPredicates = { 
		OWL.SAMEAS,
		RDFS.SEEALSO,
		new URIImpl(nsDBpediaProp + "hasPhotoCollection"),
		new URIImpl(nsFOAF + "knows"),
		new URIImpl(nsFOAF + "currentProject"), 
		new URIImpl(nsFOAF + "made"),
		new URIImpl(nsDOAP + "maintainer"), 
		new URIImpl(nsDC + "creator"),
		new URIImpl(nsSWRC + "participant"),
		new URIImpl(nsReview + "hasReview"),
		new URIImpl(nsDrugbank + "possibleDiseaseTarget"),
		new URIImpl(nsDrugbank + "target"),
		new URIImpl(nsDailyMed + "representedOrganization"),
		new URIImpl(nsDailyMed + "producesDrug"),
		new URIImpl(nsDiseasome + "possibleGenericDrug"),
		new URIImpl(nsDiseasome + "diseaseSubtypeOf")
	};
	
	/**
	 * Icon identifiers for known datasources
	 */
	public final static HashMap<String,String> knownSources = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
	        put("http://dbpedia.org/", "dbpedia");
	        put("http://dbpedia.org:", "dbpedia");
	        put("http://www4.wiwiss.fu-berlin.de/flickrwrappr/", "flickr");
	        put("http://sws.geonames.org/", "geonames");
	        put("http://revyu.com/", "revyu");
	        put("http://www4.wiwiss.fu-berlin.de/eurostat/", "eurostat");
	        put("http://sindice.com/", "sindice");
	        put("http://api.sindice.com/", "sindice");
	        put("http://demo.sindice.com/", "sindice");
	        put("http://beckr.org/DBpediaMobile/", "dbpm");
	        put("http://iws.seu.edu.cn/services/falcons/", "falcons");
	    }
	};
	
	/**
	 * Number of colored source icons, used to cycle them
	 */
	public final static int numSourceColorIcons = 7;
	
	public static boolean isBaseUrl(String url) {
		return url.startsWith(nsBaseGraphs);		
	}
}
