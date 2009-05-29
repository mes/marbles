package edu.mit.simile.fresnel.configuration;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.openrdf.model.Resource;

import edu.mit.simile.fresnel.facets.FacetSet;
import edu.mit.simile.fresnel.util.WrappedHashMap;

/**
 * Wrapped HashMap of facet sets, keyed by the type the set of facets applies to.
 * 
 * @author ryanlee
 */
public class FacetSetHashMap extends WrappedHashMap<Resource, List<FacetSet>> {
	/**
	 * The default facet set applicable to all types, if provided.
	 */
	private FacetSet _forAll;
	
	/**
	 * @see java.util.HashMap#HashMap()
	 */
	public FacetSetHashMap() {
		super();
	}
	
	/**
	 * @see java.util.HashMap#HashMap(int)
	 */
	public FacetSetHashMap(int initialCapacity) throws IllegalArgumentException {
		super(initialCapacity);
	}
	
	/**
	 * @see java.util.HashMap#HashMap(int, float)
	 */
	public FacetSetHashMap(int initialCapacity, float loadFactor)
	throws IllegalArgumentException {
		super(initialCapacity, loadFactor);
	}
	
	/**
	 * @see java.util.HashMap#HashMap(Map)
	 */
	public FacetSetHashMap(Map<Resource, List<FacetSet>> m) throws NullPointerException {
		super(m);
	}
	
	/**
	 * Gets an iterator based on a hash key, normally the resource identifying a specific
	 * rdf:type value.
	 * 
	 * @param key The <code>Object</code> of the hash lookup, normally a <code>Resource</code>
	 * @return The <code>FacetSetIterator</code> value associated with the hash key.
	 */
	public Iterator<FacetSet> getFacetSetIterator(Resource key) {
		if (this._base.containsKey(key))
			return (this._base.get(key)).iterator();
		else
			return null;
	}
	
	/**
	 * If a default set of facets applicable to all types exists.
	 * 
	 * @return True if a default set exists, false if not.
	 */
	public boolean hasDefaultSet() {
		return (this._forAll != null);
	}
	
	/**
	 * Fetch the default set of facets; check first with hasDefaultSet()
	 * 
	 * @return A <code>FacetSet</code>
	 */
	public FacetSet getDefaultSet() {
		return this._forAll;
	}
	
	/**
	 * Set the default set of facets.
	 * 
	 * @param fs A <code>FacetSet</code>
	 */
	public void setDefaultSet(FacetSet fs) {
		this._forAll = fs;
	}
	
	/**
	 * Gets the entire format set based on a hash key, normally a rdf:type value.
	 * 
	 * @param key The <code>Object</code> of the hash lookup, normally a <code>Resource</code>
	 * @return The <code>FormatSetList</code> value associated with the hash key.
	 */
	public List<FacetSet> getFacetSetList(Resource key) {
		if (this._base.containsKey(key))
			return this._base.get(key);
		else
			return null;
	}
	
	/**
	 * Puts a <code>FacetSet</code> into a <code>FacetSetList</code> associated with a 
	 * hash key, normally a rdf:type value.
	 * 
	 * @param key The <code>Object</code> of the hash lookup, normally a <code>Resource</code>
	 * @param facets The <code>FacetSet</code> to add to the hash.
	 * @return The added <code>FacetSet</code>
	 */
	public FacetSet putFacetSet(Resource key, FacetSet facets) {
		if (facets.isForAll() && this._forAll == null) {
			this._forAll = facets;
		} else {
			if (this._base.containsKey(key)) {
				List<FacetSet> set = this._base.get(key);
				set.add(facets);
				this._base.put(key, set);
			} else {
				Vector<FacetSet> set = new Vector<FacetSet>();
				set.add(facets);
				this._base.put(key, set);
			}
		}
		return facets;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String out = "[FacetSetHashMap " + super.toString() + "]\n";
		for (Iterator it = this._base.keySet().iterator(); it.hasNext(); ) {
			out += this._base.get(it.next()).toString();
		}
		return out;
	}
}
