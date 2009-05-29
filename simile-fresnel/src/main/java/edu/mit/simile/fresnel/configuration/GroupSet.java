/*
 * Created on Apr 5, 2005
 */
package edu.mit.simile.fresnel.configuration;

import java.util.Iterator;

import org.openrdf.model.URI;

import edu.mit.simile.fresnel.util.WrappedVector;

/**
 * Wraps a vector with convenience methods for accessing groups.  Groups are
 * added in increasing lexicographic order.
 * 
 * @author ryanlee
 */
public class GroupSet extends WrappedVector<Group> {
    /**
     * Returns a GroupIterator instead of a normal iterator.
     * 
     * @return A <code>GroupIterator</code>
     */
    public Iterator<Group> groupIterator() {
        return this._base.iterator();
    }

    /**
     * Adds a group to the existing set.
     * 
     * @param group A <code>Group</code>
     * @return Success or failure
     */
    public boolean add(Group group) {
        boolean out = false;
        int i = 0;
        int size = this._base.size();
        for ( ; i < size; i++) {
            Group compare = this._base.get(i);
            // this assumes you won't declare an anonymous group; it wouldn't make
            // sense to anyways, but caveat emptor
            if (((URI) compare.getIdentifier()).toString().compareTo(((URI) group.getIdentifier()).toString()) < 0) {
                this._base.insertElementAt(group, i);
                out = true;
                break;
            }
        }
        if (size == i) out = this._base.add(group);
        return out;
    }
}
