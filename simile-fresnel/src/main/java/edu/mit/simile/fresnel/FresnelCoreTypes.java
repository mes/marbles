package edu.mit.simile.fresnel;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 * RDF datatypes used in the Fresnel Core vocabulary.
 * 
 * @author ryanlee
 */
public class FresnelCoreTypes {
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://www.w3.org/2004/09/fresnel#";

    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = new URIImpl( NS );

    public static final URI fslSelector = new URIImpl(NAMESPACE + "fslSelector");
    public static final URI sparqlSelector = new URIImpl(NAMESPACE + "sparqlSelector");
    public static final URI styleClass = new URIImpl(NAMESPACE + "styleClass");
    public static final URI stylingInstructions = new URIImpl(NAMESPACE + "stylingInstructions");
}
