package edu.mit.simile.fresnel.configuration;

import java.util.List;
import java.util.Vector;

import org.openrdf.model.Resource;

import edu.mit.simile.fresnel.format.Format;

/**
 * Potential matches for formats that can format the given resource.  Wraps
 * format matches in a FormatSet.
 * 
 * @author ryanlee
 */
public class ResourceFormatMatchSet {
	/**
	 * The resource in the data model being matched against.
	 */
	private Resource _focusRes;
	
	/**
	 * Ordered set of applicable instance formats
	 */
	private List<Format> _instances;
	
	/**
	 * Ordered set of class type formats
	 */
	private List<Format> _types;
		
	/**
	 * Constructor based on the resource requiring matches.
	 * 
	 * @param focus A <code>Resource</code> with lens matches.
	 */
	public ResourceFormatMatchSet(Resource focus) {
		this._focusRes = focus;
		this._instances = new Vector<Format>();
		this._types = new Vector<Format>();
	}
	
	/**
	 * Adds a format to its ordered set and the wrapped vector of formats.
	 * 
	 * @param format A matching <code>Format</code>
	 * @return A boolean indicating success
	 */
	public boolean addInstanceFormat(Format format) {
		return this._instances.add(format);
	}
	
	/**
	 * Adds a format to its ordered set and the wrapped vector of formats.
	 * 
	 * @param format A matching <code>Format</code>
	 * @return A boolean indicating success
	 */
	public boolean addClassFormat(Format format) {
		return this._types.add(format);
	}
	
	/**
	 * Instance domain formats associated with this set of matches.
	 * 
	 * @return A <code>FormatSet</code>
	 */
	public List<Format> getInstanceFormats() {
		return this._instances;
	}
	
	/**
	 * Class domain formats associated with this set of matches.
	 * 
	 * @return A <code>FormatSet</code>
	 */
	public List<Format> getClassFormats() {
		return this._types;
	}
	
	/**
	 * The resource the formats match.
	 * 
	 * @return A <code>Resource</code>
	 */
	public Resource getResource() {
		return this._focusRes;
	}
	
	/**
	 * Returns the top ranked match for the resource amongst all applicable formats
	 * 
	 * @return A matching <code>Format</code>
	 */
	public Format topMatch() {
		Format out = null;
		if (this._instances.size() > 0) out = (Format) this._instances.get(0);
		else if (this._types.size() > 0) out = (Format) this._types.get(0);			
		return out;
	}
}
