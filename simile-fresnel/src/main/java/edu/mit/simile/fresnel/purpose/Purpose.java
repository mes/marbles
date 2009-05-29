/*
 * Created on Mar 16, 2005
 */
package edu.mit.simile.fresnel.purpose;

import edu.mit.simile.fresnel.FresnelResource;
import edu.mit.simile.vocabularies.FresnelCore;

import org.openrdf.model.Resource;

/**
 * Represents the :Purpose class.
 * 
 * @author ryanlee
 */
public class Purpose extends FresnelResource {
    /**
     * The resource identifying the purpose.
     */
    private Resource _identifier;
    
    /**
     * Resource described in the schema.
     */
    protected static Resource _schemaResource = FresnelCore.Purpose;
    
    /**
     * Purpose constructor using the identifier
     * 
     * @param id A <code>Resource</code>
     */
    public Purpose(Resource id) {
        this._identifier = id;
    }
    
    /**
     * This purpose's identifier.
     * 
     * @return A <code>Resource</code>
     */
    public Resource getIdentifier() {
        return this._identifier;
    }
    
    /**
     * Redefining equals test
     * 
     * @param p2 Another <code<Purpose</code>
     * @return True if equal, false if not
     */
    public boolean equals(Purpose p2) {
        return this.getIdentifier().equals(p2.getIdentifier());
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String state = "   [Purpose " + super.toString() + "]\n";
        state += "   " + this._identifier + "\n";
        return state;
    }
}
