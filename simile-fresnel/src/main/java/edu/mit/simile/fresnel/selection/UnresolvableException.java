/*
 * Created on Mar 22, 2005
 */
package edu.mit.simile.fresnel.selection;

/**
 * Thrown when too many options are made available, making for a
 * non-deterministic choice in resolution.
 * 
 * @author ryanlee
 */
public class UnresolvableException extends Exception {
    /**
     * Serializable UID
     */
    private static final long serialVersionUID = 5561075899364932087L;
    
    /**
     * Prefix exception messages with a little more info.
     */
    private static final String _prefix = "Cannot resolve which options to use: ";

    /**
     * Constructs an exception using a specific message.
     * 
     * @param reason The <code>String</code> explanation of the exception.
     */
    public UnresolvableException(String reason) {
        super(_prefix + reason);
    }
}
