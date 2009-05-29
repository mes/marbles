package edu.mit.simile.fresnel.results;

/**
 * Constants for use in results computation.
 * 
 * @author ryanlee
 */
public interface ResultConstants {
	/**
	 * Namespace for the intermediate XML representation of Fresnel output.
	 */
	public static final String INTERMEDIATE_NS = "http://www.w3.org/2004/09/fresnel-tree";
	
	/**
	 * Somewhat arbitrary limit to nested lens calls.
	 */
	public static final int MAXIMUM_LENS_DEPTH = 3;
}
