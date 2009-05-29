/*
 * Created on Mar 22, 2005
 */
package edu.mit.simile.fresnel.util;

import java.util.Iterator;

/**
 * Wraps an iterator; inheriting classes may implement convenience getX methods.
 * 
 * @author ryanlee
 */
public abstract class WrappedIterator<E> implements Iterator {
    /**
     * The wrapped <code>Iterator</code>
     */
    protected Iterator<E> _base;
    
    /**
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        return this._base.hasNext();
    }

    /**
     * @see java.util.Iterator#next()
     */
    public E next() {
        return this._base.next();
    }

    /**
     * @see java.util.Iterator#remove()
     */
    public void remove() {
        this._base.remove();
    }
}
