/*
 * Created on Mar 16, 2005
 */
package edu.mit.simile.fresnel;

import org.openrdf.model.Resource;

/**
 * An abstract superclass for representing owl:Classes in the
 * Fresnel vocabulary.
 * 
 * @author ryanlee
 */
public abstract class FresnelResource {
    /**
     * The resource in the Fresnel core schema.
     */
    protected static Resource _schemaResource;
    
    /**
     * Returns the Fresnel schema resource.
     * 
     * @return A <code>Resource</code>
     */
    public static Resource getSchemaResource() {
        return _schemaResource;
    }
}
