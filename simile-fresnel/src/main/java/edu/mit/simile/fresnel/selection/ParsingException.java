/*
 * Created on Mar 22, 2005
 */
package edu.mit.simile.fresnel.selection;

/**
 * Thrown when an error in setup is made in the Fresnel configuration
 * <code>Graph</code>
 * 
 * @author ryanlee
 */
public class ParsingException extends Exception {
    /**
     * Serializable UID
     */
    private static final long serialVersionUID = -800061611802971064L;

    /**
     * Prefix exception messages with a little more info.
     */
    private static final String _prefix = "Cannot parse this model: ";

    /**
     * Constructs an exception with a more specific explanation.
     * 
     * @param reason The <code>String</code> explanation.
     */
    public ParsingException(String reason) {
        super(_prefix + reason);
    }
}
