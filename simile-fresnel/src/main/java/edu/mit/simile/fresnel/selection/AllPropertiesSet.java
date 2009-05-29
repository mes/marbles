/*
 * Created on Mar 17, 2005
 */
package edu.mit.simile.fresnel.selection;

import edu.mit.simile.vocabularies.FresnelCore;

import org.openrdf.model.Resource;

/**
 * All the properties of a resource, a special case for easily selecting
 * all properties using the resource fresnel:allProperties.  As a Java class,
 * this is never really used except for its getSchemaResource() method.
 * 
 * @author ryanlee
 */
public class AllPropertiesSet extends PropertySet {
    /**
     * Resource defined by the schema.
     */
    protected static Resource _schemaResource = FresnelCore.allProperties;
    
    /**
     * Retrieves the schema resource this class is based on.
     * 
     * @return A <code>Resource</code>
     */
    public static Resource getSchemaResource() {
        return _schemaResource;
    }
}
