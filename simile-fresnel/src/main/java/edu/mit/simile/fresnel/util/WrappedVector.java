/*
 * Created on Mar 30, 2005
 */
package edu.mit.simile.fresnel.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

/**
 * Wraps a vector to make it easier to write convenience methods (addX, removeX, etc.).
 * 
 * @author ryanlee
 */
public abstract class WrappedVector<E> {
    /**
     * Base vector that the wrapper manipulates.
     */
    protected Vector<E> _base;
    
    /**
     * Initialize the vector.
     */
    public WrappedVector() {
        this._base = new Vector<E>();
    }
    
    /**
     * @see java.util.Collection#size()
     */
    public int size() {
        return this._base.size();
    }
    
    /**
     * @see java.util.Collection#isEmpty()
     */
    public boolean isEmpty() {
        return this._base.isEmpty();
    }

    /**
     * @see java.util.Collection#contains(java.lang.Object)
     */
    public boolean contains(E arg0) {
        return this._base.contains(arg0);
    }

    /**
     * @see java.util.Collection#iterator()
     */
    public Iterator<E> iterator() {
        return this._base.iterator();
    }

    /**
     * @see java.util.Collection#toArray()
     */
    public Object[] toArray() {
        return this._base.toArray();
    }

    /**
     * @see java.util.Collection#toArray(java.lang.Object[])
     */
    public E[] toArray(E[] arg0) {
        return this._base.toArray(arg0);
    }

    /**
     * Returns the underlying vector.
     * 
     * @return A <code>Vector</code>
     */
    public Vector<E> toVector() {
        return this._base;
    }
    
    /**
     * @see java.util.Vector#get(int)
     */
    public E get(int i) {
        return this._base.get(i);
    }
    
    /**
     * @see java.util.Collection#add(java.lang.Object)
     */
    public boolean add(E obj) {
        return this._base.add(obj);
    }

    /**
     * @see java.util.Vector#insertElementAt(java.lang.Object, int)
     */
    public void insertElementAt(E obj, int index) {
        this._base.insertElementAt(obj, index);
    }
    
    /**
     * @see java.util.Collection#remove(java.lang.Object)
     */
    public boolean remove(E obj) {
        return this._base.remove(obj);
    }

    /**
     * @see java.util.Collection#addAll(java.util.Collection)
     */
    public boolean addAll(Collection<E> arg0) {
        return this._base.addAll(arg0);
    }

    /**
     * @see java.util.Collection#clear()
     */
    public void clear() {
        this._base.clear();
    }
}
