package edu.mit.simile.fresnel.results;

import java.util.HashMap;
import java.util.Iterator;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import edu.mit.simile.fresnel.util.WrappedVector;

/**
 * Set of PropertyResults.
 * 
 * @author ryanlee
 */
public class PropertyResultSet extends WrappedVector<PropertyResult> {
	/**
	 * Maps property URIs to their corresponding PropertyResults
	 */
	private HashMap<InvertableURI, PropertyResult> _used;
	
	/**
	 * Initialize a set.
	 */
	public PropertyResultSet() {
		super();
		this._used = new HashMap<InvertableURI, PropertyResult>();
	}
	
	/**
	 * Returns a PropertyResultIterator instead of a normal iterator.
	 * 
	 * @return A <code>PropertyResultIterator</code>
	 */
	public Iterator<PropertyResult> propertyResultIterator() {
		return this._base.iterator();
	}
	
	/**
	 * Adds a property result to the existing set.
	 * 
	 * @param property A <code>PropertyResult</code>
	 * @return Success or failure
	 */
	public boolean addPropertyResult(PropertyResult property, Result parent) {
		this._used.put(new InvertableURI(property.getOrigin(), property.isInverse()), property);
		return this._base.add(property);
	}
	
	/**
	 * Removes a property result from the existing set.
	 * 
	 * @param property A <code>PropertyResult</code>
	 * @return Success or failure
	 */
	public boolean removePropertyResult(PropertyResult property) {
		this._used.remove(property);
		return this._base.remove(property);
	}
	
	/**
	 * Add all elements from a property result set into this one.
	 * 
	 * @param arg0 A <code>PropertyResultSet</code>
	 * @return Success or failure
	 */
	public boolean addPropertyResultSet(PropertyResultSet arg0) {
		for(Iterator<PropertyResult> it = arg0.propertyResultIterator(); it.hasNext() ;) {
			PropertyResult pr = it.next();
			this._used.put(new InvertableURI(pr.getOrigin(), pr.isInverse()), pr);
		}
		return this._base.addAll(arg0._base);
	}
	
	/**
	 * Replace WrappedVector contains method to do specific <code>PropertyResult</code>
	 * equality checking.
	 * 
	 * @param property The <code>PropertyResult</code> component to check
	 * @return True if contained in set, false if not.
	 */
	public boolean contains(PropertyResult property) {
		for (Iterator<PropertyResult> it = this.propertyResultIterator(); it.hasNext(); ) {
			if (property.equals(it.next())) return true;
		}
		return false;
	}
	
	/**
	 * Checks if this set contains a property URI and return its PropertyResult if so.
	 * 
	 * @param prop The property <code>URI</code> to lookup
	 * @return Corresponding <code>PropertyResult</code> or null if non-existent.
	 * 
	 * @author Christian Becker
	 */
	public PropertyResult lookup(URI prop, boolean inverse) {
		if (this._used.containsKey(new InvertableURI(prop, inverse))) {
			return (PropertyResult) this._used.get(new InvertableURI(prop, inverse));
		} else {
			return null;
		}
	}
	
	/**
	 * Checks if this set contains a property URI and return its PropertyResult if so.
	 * 
	 * @param prop The property <code>URI</code> to lookup
	 * @return Corresponding <code>PropertyResult</code> or null if non-existent.
	 * 
	 * @author Christian Becker
	 */
	public PropertyResult lookup(URI prop) {
		return lookup(prop, false);
	}
	
	/**
	 * Permits properties to be inverted. An inverse property where the focal resource acts as the RDF object. 
	 * @author Christian Becker
	 */
	private class InvertableURI {
		private Resource uri;
		private boolean inverse;
		
		public InvertableURI(Resource uri, boolean inverse) {
			this.uri = uri;
			this.inverse = inverse;
		}

		@Override
		public boolean equals(Object obj) {
			return (obj instanceof InvertableURI
					&& ((InvertableURI)obj).getUri().equals(getUri())
					&& ((InvertableURI)obj).isInverse() == isInverse());
		}

		public Resource getUri() {
			return uri;
		}

		public boolean isInverse() {
			return inverse;
		}

		@Override
		public int hashCode() {
			return uri.toString().hashCode() * (inverse ? -1 : 1);
		}
		
		
	}
}
