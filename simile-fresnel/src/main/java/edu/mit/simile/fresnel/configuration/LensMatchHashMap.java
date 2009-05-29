/*
 * Created on Apr 14, 2005
 */
package edu.mit.simile.fresnel.configuration;

import org.openrdf.model.Resource;

import edu.mit.simile.fresnel.util.WrappedHashMap;


/**
 * A hash map of lens matches to resources in a data model.
 * 
 * @author ryanlee
 */
public class LensMatchHashMap extends WrappedHashMap<Resource, LensMatchSet> {
    /**
     * Convenience method for returning a LensMatchSet in the hash map.
     * 
     * @param key The <code>Object</code> key, normally a <code>Resource</code>
     * @return The associated <code>LensMatchSet</code>, or null if non-existent
     */
    public LensMatchSet getMatch(Resource key) {
        return get(key);
    }
    
    /**
     * Enters a hash map entry for the given key and LensMatchSet.
     * 
     * @param key The <code>Object</code> key, normally a <code>Resource</code>
     * @param match The <code>LensMatchSet</code>
     */
    public void putMatch(Resource key, LensMatchSet match) {
        put(key, match);
    }
}
