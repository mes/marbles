/*
 * Created on Mar 30, 2005
 */
package edu.mit.simile.fresnel.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Basis for wrapping a HashMap; extensible with convenience methods.
 * 
 * @author ryanlee
 */
public abstract class WrappedHashMap<E, T> {
    /**
     * The wrapped HashMap.
     */
    protected HashMap<E, T> _base;
    
    /**
     * @see java.util.HashMap#HashMap()
     */
    public WrappedHashMap() {
        this._base = new HashMap<E, T>();
    }
    
    /**
     * @see java.util.HashMap#HashMap(int)
     */
    public WrappedHashMap(int initialCapacity) throws IllegalArgumentException {
        this._base = new HashMap<E, T>(initialCapacity);
    }
    
    /**
     * @see java.util.HashMap#HashMap(int, float)
     */
    public WrappedHashMap(int initialCapacity, float loadFactor) throws IllegalArgumentException {
        this._base = new HashMap<E, T>(initialCapacity, loadFactor);
    }
    
    /**
     * @see java.util.HashMap#HashMap(java.util.Map)
     */
    public WrappedHashMap(Map<E, T> m) throws NullPointerException {
        this._base = new HashMap<E, T>(m);
    }
    
    /**
     * @see java.util.HashMap#clear()
     */
    public void clear() {
        this._base.clear();
    }
    
    /**
     * @see java.util.HashMap#containsKey(Object)
     */
    public boolean containsKey(E key) {
        return this._base.containsKey(key);
    }
    
    /**
     * @see java.util.HashMap#containsValue(Object)
     */
    public boolean containsValue(T value) {
        return this._base.containsValue(value);
    }
    
    /**
     * @see java.util.HashMap#entrySet()
     */
    public Set entrySet() {
        return this._base.entrySet();
    }
    
    /**
     * @see java.util.HashMap#get(Object)
     */
    public T get(E key) {
        return this._base.get(key);
    }
    
    /**
     * @see java.util.HashMap#isEmpty()
     */
    public boolean isEmpty() {
        return this._base.isEmpty();
    }
    
    /**
     * @see java.util.HashMap#keySet()
     */
    public Set keySet() {
        return this._base.keySet();
    }
    
    /**
     * @see java.util.HashMap#put(Object, Object)
     */
    public T put(E key, T value) {
        return this._base.put(key, value);
    }
    
    /**
     * @see java.util.HashMap#putAll(Map)
     */
    public void putAll(Map<E, T> m) throws NullPointerException {
        this._base.putAll(m);
    }
    
    /**
     * @see java.util.HashMap#remove(Object)
     */
    public T remove(E key) {
        return this._base.remove(key);
    }
    
    /**
     * @see java.util.HashMap#size()
     */
    public int size() {
        return this._base.size();
    }
    
    /**
     * @see java.util.HashMap#values()
     */
    public Collection<T> values() {
        return this._base.values();
    }
}
