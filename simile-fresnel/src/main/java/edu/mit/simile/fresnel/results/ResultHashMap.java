package edu.mit.simile.fresnel.results;

import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import edu.mit.simile.fresnel.util.WrappedHashMap;

/**
 * Maps resources to sets of results for that resource.
 * 
 * @author ryanlee
 */
public class ResultHashMap<T, E> extends WrappedHashMap<T, Vector<E>> {
    /**
     * @see java.util.HashMap#HashMap()
     */
    public ResultHashMap() {
        super();
    }
    
    /**
     * @see java.util.HashMap#HashMap(int)
     */
    public ResultHashMap(int initialCapacity) throws IllegalArgumentException {
        super(initialCapacity);
    }
    
    /**
     * @see java.util.HashMap#HashMap(int, float)
     */
    public ResultHashMap(int initialCapacity, float loadFactor) throws IllegalArgumentException {
        super(initialCapacity, loadFactor);
    }
    
    /**
     * @see java.util.HashMap#HashMap(Map)
     */
    public ResultHashMap(Map<T, Vector<E>> m) throws NullPointerException {
        super(m);
    }
    
    /**
     * Gets a result set iterator based on a hash key, normally a resource.
     * 
     * @param key The <code>Object</code> of the hash lookup, normally a <code>Resource</code>
     * @return The <code>Iterator</code> value associated with the hash key.
     */
    public Iterator<E> getResultIterator(T key) {
        if (this._base.containsKey(key))
            return (this._base.get(key)).iterator();
        else
            return null;
    }
    
    /**
     * Gets the entire results set based on a hash key, normally a resource.
     * 
     * @param key The <code>Object</code> of the hash lookup, normally a <code>Resource</code>
     * @return The <code>Vector</code> value associated with the hash key.
     */
    public Vector<E> getResultSet(T key) {
        if (this._base.containsKey(key))
            return this._base.get(key);
        else
            return null;
    }
    
    /**
     * Puts a <code>Result</code> into a <code>Vector</code> associated with a 
     * hash key, normally a <code>Resource</code>
     * 
     * @param key The <code>Object</code> of the hash lookup, normally a <code>Resource</code>
     * @param val A <code>Vector</code> of <code>Result</code>s
     * @return The added <code>Vector</code>
     */
    public Object putResult(T key, E val) {
        if (this._base.containsKey(key)) {
            Vector<E> set = this._base.get(key);
            set.add(val);
            this._base.put(key, set);
        } else {
            Vector<E> set = new Vector<E>();
            set.add(val);
            this._base.put(key, set);
        }
        return val;
    }
}
