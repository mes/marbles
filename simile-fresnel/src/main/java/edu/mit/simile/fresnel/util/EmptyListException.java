package edu.mit.simile.fresnel.util;

/**
 * Thrown when a RDF collection is unexpectedly empty
 * 
 * @author ryanlee
 */
public class EmptyListException extends Exception {
	/**
	 * Exception message
	 */
	private static final String INFO = "RDF:List is empty: ";
	/**
	 * Serialization ID
	 */
	private static final long serialVersionUID = 6557862592827593183L;
	
	/**
	 * Constructor with details
	 * 
	 * @param msg <code>String</code> with further details
	 */
	public EmptyListException(String msg) {
		super(INFO + msg);
	}
}
