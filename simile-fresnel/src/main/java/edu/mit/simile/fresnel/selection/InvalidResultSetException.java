/*
 * Created on Mar 16, 2005
 */
package edu.mit.simile.fresnel.selection;

/**
 * Thrown by an <code>ISelector</code> if used to obtain a result set
 * the selector does not know how to select.
 * 
 * @author ryanlee
 */
public class InvalidResultSetException extends Exception {
    /**
     * Serializable UID
     */
    private static final long serialVersionUID = -3213356925704037650L;

    /**
     * Prefix exception messages with a little more info.
     */
    private static final String _prefix = "Cannot generate this kind of result set: ";

    /**
     * Constructs an exception with a more specific explanation.
     * 
     * @param reason The <code>String</code> explanation.
     */
    public InvalidResultSetException(String reason) {
        super(_prefix + reason);
    }
    
    public InvalidResultSetException(String reason, Exception e) {
    	super(_prefix + reason, e);
    }
}
