/*
 * Created on Mar 22, 2005
 */
package edu.mit.simile.fresnel.purpose;

import java.util.Iterator;

import edu.mit.simile.fresnel.util.WrappedVector;

/**
 * A set of <code>Purpose</code>s.
 * 
 * @author ryanlee
 */
public class PurposeSet extends WrappedVector<Purpose> {   
    /**
     * Returns a PurposeIterator instead of a normal iterator.
     * 
     * @return A <code>PurposeIterator</code>
     */
    public Iterator<Purpose> purposeIterator() {
        return this._base.iterator();
    }

    /**
     * Adds a purpose to the existing set.
     * 
     * @param purpose A <code>Purpose</code>
     * @return Success or failure
     */
    public boolean addPurpose(Purpose purpose) {
        return this._base.add(purpose);
    }

    /**
     * Removes a purpose from the existing set.
     * 
     * @param purpose A <code>Purpose</code>
     * @return Success or failure
     */
    public boolean removePurpose(Purpose purpose) {
        return this._base.remove(purpose);
    }

    /**
     * Add all elements from a purpose set into this one.
     * 
     * @param arg0 A <code>PurposeSet</code>
     * @return Success or failure
     */
    public boolean addPurposeSet(PurposeSet arg0) {
        return this._base.addAll(arg0._base);
    }
    
    /**
     * Replace WrappedVector contains method to do specific <code>Purpose</code>
     * equality checking.
     * 
     * @param purpose The <code>Purpose</code> component to check
     * @return True if contained in set, false if not.
     */
    public boolean contains(Purpose purpose) {
        for (Iterator<Purpose> it = this.purposeIterator(); it.hasNext(); ) {
            if (purpose.equals(it.next())) return true;
        }
        return false;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String state = "  [PurposeSet " + super.toString() + "]\n";
        Iterator<Purpose> it = this.purposeIterator();
        while (it.hasNext()) {
            Purpose purpose = it.next();
            state += purpose;
        }
        return state;
    }
}
