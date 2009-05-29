package edu.mit.simile.fresnel.configuration;

import java.util.List;
import java.util.Vector;

import org.openrdf.model.URI;

import edu.mit.simile.fresnel.format.Format;

/**
 * Potential matches for formats that can format the given property.
 * 
 * @author ryanlee
 */
public class PropertyFormatMatchSet {
	/**
	 * The property in the data model being matched against.
	 */
	private URI _focusProp;
		
	/**
	 * Ordered set of applicable property formats
	 */
	private List<Format> _properties;
	
	/**
	 * Constructor based on the property requiring matches.
	 * 
	 * @param focus A property <code>URI</code> with lens matches.
	 */
	public PropertyFormatMatchSet(URI focus) {
		this._focusProp = focus;
		this._properties = new Vector<Format>();
	}
	
	/**
	 * Adds a format to its ordered set and the wrapped vector of properties.
	 * 
	 * @param format A matching <code>Format</code>
	 * @return A boolean indicating success
	 */
	public boolean addPropertyFormat(Format format) {
		return this._properties.add(format);
	}
	
	/**
	 * Returns the property these formats are supposed to match.
	 * 
	 * @return A <code>URI</code>
	 */
	public URI getProperty() {
		return this._focusProp;
	}
	
	/**
	 * Returns the top ranked match for the resource amongst all applicable formats
	 * 
	 * @return A matching <code>Format</code>
	 */
	public Format topMatch() {
		Format out = null;
		if (this._properties.size() > 0) out = (Format) this._properties.get(0);
		return out;
	}
}
