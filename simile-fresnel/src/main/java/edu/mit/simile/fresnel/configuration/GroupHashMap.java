/*
 * Created on Apr 5, 2005
 */
package edu.mit.simile.fresnel.configuration;

import java.util.Map;

import org.openrdf.model.Resource;

import edu.mit.simile.fresnel.util.WrappedHashMap;

/**
 * Wraps a HashMap to provide convenience methods for dealing with groups.
 * 
 * @author ryanlee
 */
public class GroupHashMap extends WrappedHashMap<Resource, Group> {
    /**
     * @see java.util.HashMap#HashMap()
     */
    public GroupHashMap() {
        super();
    }
    
    /**
     * @see java.util.HashMap#HashMap(int)
     */
    public GroupHashMap(int initialCapacity) throws IllegalArgumentException {
        super(initialCapacity);
    }
    
    /**
     * @see java.util.HashMap#HashMap(int, float)
     */
    public GroupHashMap(int initialCapacity, float loadFactor) throws IllegalArgumentException {
        super(initialCapacity, loadFactor);
    }
    
    /**
     * @see java.util.HashMap#HashMap(java.util.Map)
     */
    public GroupHashMap(Map<Resource, Group> m) throws NullPointerException {
        super(m);
    }

    /**
     * Gets the group based on a hash key, generally the Resource identifying the group.
     * 
     * @param key The <code>Object</code> of the hash lookup, normally a <code>Resource</code>
     * @return The <code>Group</code> value associated with the hash key.
     */
    public Group getGroup(Object key) {
        if (this._base.containsKey(key))
            return (Group) this._base.get(key);
        else
            return null;
    }
    
    /**
     * Puts a <code>Group</code> into the hash based on its resource identifier.
     * 
     * @param key The <code>Object</code> of the hash lookup, normally a <code>Resource</code>
     * @param group The <code>Group</code> to add to the hash.
     * @return The added <code>Group</code>
     */
    public Group putGroup(Resource key, Group group) {
        this._base.put(key, group);
        return group;
    }
}
