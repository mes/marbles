/*
 * Created on May 5, 2005
 */
package edu.mit.simile.fresnel.configuration;


/**
 * Exception thrown when no results can be generated from the configuration.
 * 
 * @author ryanlee
 */
public class NoResultsException extends Exception {
	/**
	 * Serialization UID
	 */
    private static final long serialVersionUID = 2003091921949622938L;
    
    /**
     * Base message
     */
    private static String PREFIX = "No results from selection: ";
    
    /**
     * Constructor with details
     * 
     * @param msg A <code>String</code> of details
     */
    public NoResultsException(String msg) {
        super(PREFIX + msg);
    }
}
