/*
 * Created on Apr 12, 2005
 */
package edu.mit.simile.fresnel.selection;

/**
 * Thrown when a resource is referred to in one place but is not described
 * any further (in a parse-able way) within the same model.
 * 
 * @author ryanlee
 */
public class ResourceNotFoundException extends Exception {
    /**
     * Serializable UID
     */
    static final long serialVersionUID = 8823470289237816653L;

    /**
     * Prefix for exception messages
     */
    private final static String PREFIX = "Resource referred to but not found: ";

    /**
     * Constructor for exceptions
     * 
     * @param mesg <code>String</code> description further explaning the issue
     */
    public ResourceNotFoundException(String mesg) {
        super(PREFIX + mesg);
    }
}
