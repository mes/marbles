/*
 * Created on Mar 29, 2005
 */
package edu.mit.simile.fresnel.configuration;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import edu.mit.simile.fresnel.selection.Lens;

/**
 * Potential matches for lenses that can render the given resource.  Wraps
 * lens matches in a LensSet.
 * 
 * @author ryanlee
 */
public class LensMatchSet {
    /**
     * The resource in the data model being matched against.
     */
    private Resource _focus;
    
    /**
     * Ordered set of all applicable lenses
     */
    private List<Lens> _all;
    
    /**
     * Ordered set of applicable instance lenses
     */
    private List<Lens> _instances;
    
    /**
     * Ordered set of applicable default lenses
     */
    private List<Lens> _defaults;
    
    /**
     * Ordered set of class type lenses
     */
    private List<Lens> _types;
    
    /**
     * Constructor based on the resource requiring matches.
     * 
     * @param focus A <code>Resource</code> with lens matches.
     */
    public LensMatchSet(Resource focus) {
        this._focus = focus;
        this._all = new Vector<Lens>();
        this._instances = new Vector<Lens>();
        this._defaults = new Vector<Lens>();
        this._types = new Vector<Lens>();
    }
    
    /**
     * Adds a lens to its ordered set and the wrapped vector of lenses.
     * 
     * @param lens A matching <code>Lens</code>
     * @return A boolean indicating success
     */
    public boolean add(Lens lens) {
        // resolving multiple occurences by taking first uri...
        if (lens.isInstance()) {
        	if (lens.isDefault()) {
        		this._instances.add(0, lens);
        	} else {
        		// instance
        		int i = 0;
        		int size = this._instances.size();
        		for ( ; i < size; i++) {
        			Lens compare = this._instances.get(i);
        			if (((URI) compare.getIdentifier()).toString().compareTo(((URI) lens.getIdentifier()).toString()) < 0) {
        				this._instances.add(i, lens);
        				break;
        			}
        		}
        		if (size == i) this._instances.add(lens);
        	}
        } else if (lens.isDefault()) {
            // default
            int i = 0;
            int size = this._defaults.size();
            for ( ; i < size; i++) {
                Lens compare = (Lens) this._defaults.get(i);
                if (((URI) compare.getIdentifier()).toString().compareTo(((URI) lens.getIdentifier()).toString()) < 0) {
                    this._defaults.add(i, lens);
                    break;
                }
            }
            if (size == i) this._defaults.add(lens);
        } else {
            // class
            int i = 0;
            int size = this._types.size();
            for ( ; i < size; i++) {
                Lens compare = (Lens) this._types.get(i);
                if (((URI) compare.getIdentifier()).toString().compareTo(((URI) lens.getIdentifier()).toString()) < 0) {
                    this._types.add(i, lens);
                    break;
                }
            }
            if (size == i) this._types.add(lens);
        }
        return this._all.add(lens);
    }
    
    /**
     * Whether this set is empty or not.
     * 
     * @return True if set is empty.
     */
    public boolean isEmpty() {
    	return this._all.isEmpty();
    }
    
    /**
     * Iterate through all lens matches.
     * 
     * @return An <code>Iterator</code> of <code>Lens</code>es
     */
    public Iterator<Lens> lensIterator() {
    	return this._all.iterator();
    }
    
    /**
     * Instance domain lenses associated with this set of matches.
     * 
     * @return A <code>LensSet</code>
     */
    public List<Lens> getInstanceLenses() {
        return this._instances;
    }
    
    /**
     * Class domain lenses associated with this set of matches.
     * 
     * @return A <code>LensSet</code>
     */
    public List<Lens> getClassLenses() {
        return this._types;
    }
    
    /**
     * Returns the resource these lens are supposed to match.
     * 
     * @return A <code>Resource</code>
     */
    public Resource getResource() {
    		return this._focus;
    }
    
    /**
     * Returns the top ranked match for the resource amongst all applicable lenses
     * 
     * @return A matching <code>Lens</code>
     */
    public Lens topMatch() {
        Lens out = null;
        if (this._instances.size() > 0) out = (Lens) this._instances.get(0);
        else if (this._defaults.size() > 0) out = (Lens) this._defaults.get(0);
        else if (this._types.size() > 0) out = (Lens) this._types.get(0);
        return out;
    }
}
