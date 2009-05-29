package edu.mit.simile.fresnel.facets;

import org.openrdf.model.Resource;
import edu.mit.simile.fresnel.FresnelResource;
import edu.mit.simile.vocabularies.Facets;

/**
 * A facet as described by the Fresnel faceting extension vocabulary.
 * 
 * @author ryanlee
 */
public class Facet extends FresnelResource {
	
	/**
	 * Resource represented by this facet.
	 */
	private Resource _identifier;
	
	/**
     * Resource defined in the schema.
     */
    protected static final Resource _schemaResource = Facets.Facet;
	
	/**
	 * Construct a facet based on a resource identifier
	 * 
	 * @param identifier A <code>Resource</code>
	 */
	public Facet(Resource identifier) {
		this._identifier = identifier;
	}

	/**
	 * Retrieve the Resource (property) representing this facet.
	 * 
	 * @return A <code>Resource</code>
	 */
	public Resource getIdentifier() {
		return this._identifier;
	}
}
