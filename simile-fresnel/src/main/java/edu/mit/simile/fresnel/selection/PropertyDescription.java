/*
 * Created on Apr 11, 2005
 */
package edu.mit.simile.fresnel.selection;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import edu.mit.simile.fresnel.FresnelCoreTypes;
import edu.mit.simile.fresnel.FresnelResource;
import edu.mit.simile.fresnel.configuration.Configuration;
import edu.mit.simile.fresnel.configuration.Group;
import edu.mit.simile.fresnel.format.Format;
import edu.mit.simile.vocabularies.FresnelCore;
import fr.inria.jfresnel.fsl.FSLPath;

/**
 * Used for describing more about how a property is selected
 * in a lens; core terms involve the sublens and which style
 * should directly be used.
 * 
 * @author ryanlee
 */
public class PropertyDescription extends FresnelResource implements ISelector {
    /**
     * Resource of schema class
     */
    protected static final Resource _schemaResource = FresnelCore.PropertyDescription;
    
    /**
     * Identifier for a PropertyDescription instance.
     */
    private Resource _id;
    
  	/**
  	 * The selector for the property the description describes.
  	 */
  	private ISelector _propertySelector;
    
    /**
     * The set of sublenses that should be used with the property's objects.
     */
    private Set<Lens> _sublenses;
    
    /**
     * The maximum recursion depth the sublens application can reach.
     */
    private int _depth;
    
    /**
     * Default maximum recursion depth.
     */
    private static final int DEFAULT_MAX_DEPTH = 3;
    
    /**
     * The group of styles that should be used with this description.
     */
    private Group _groupUse;
    
    /**
     * The single lens that should be used with this description.
     */
    private Format _formatUse;
    
    /**
     * This selector class can return statements.
     */
    private boolean _statements = true;
    
    /**
     * This selector class cannot return resources.
     */
    private boolean _resources = false;
    
    /**
     * This selector class cannot return nodes.
     */
    private boolean _nodes = false;
    
    /**
     * Context for determining FSL first location step type.
     */
    protected final static short _fslContext = FSLPath.ARC_STEP;

    /**
     * Constructor taking an identifier for the property description.
     * 
     * @param identifier A unique <code>Resource</code>
     */
    public PropertyDescription(Resource identifier) {
        setIdentifier(identifier);
        this._sublenses = new HashSet<Lens>();
    }
    
    /**
     * Returns the instance identifier.
     * 
     * @return A <code>Resource</code> identifier.
     */
    public Resource getIdentifier() {
        return this._id;
    }
    
  	/**
  	 * Returns the property selector being described.
  	 * 
  	 * @return A property <code>ISelector</code>
  	 */
  	public ISelector getProperty() {
  		return this._propertySelector;
  	}
    
    /**
     * Returns the maximum recursion depth that can be attained in sublensing.
     * 
     * @return An <code>int</code> depth.
     */
    public int getDepth() {
        return this._depth;
    }
    
    /**
     * Returns the set of sublenses associated with this description.
     * 
     * @return A <code>LensSet</code> of sublenses.
     */
    public Set<Lens> getSublenses() {
        return this._sublenses;
    }
    
    /**
     * Return an iterator over sublenses.
     * 
     * @return A <code>LensIterator</code>
     */
    public Iterator<Lens> getSublensesIterator() {
        return this._sublenses.iterator();
    }
    
    /**
     * Return the style usage for the property.
     * 
     * @return An <code>Object</code>
     * @see edu.mit.simile.fresnel.selection.PropertyDescription#isGroupUse()
     */
    public Object getUse() {
        return (null != this._groupUse) ? (Object) this._groupUse : (Object) this._formatUse;
    }
    
    /**
     * Returns whether or not the usage is a group or a single style.
     * 
     * @return A <code>boolean</code>
     */
    public boolean isGroupUse() {
        return (null != this._groupUse);
    }
    
    /**
     * Returns whether or not there is a usage associated with this description.
     * 
     * @return A <code>boolean</code>
     */
    public boolean hasUse() {
        return (null != this._groupUse || null != this._formatUse);
    }
    
    /**
     * Returns whether or not there are sublenses associated with this description.
     * 
     * @return A <code>boolean</code>
     */
    public boolean hasSublens() {
        return (null != this._sublenses);
    }
    
    /**
     * Sets the identifier for the instance of a description.
     * 
     * @param id A unique <code>Resource</code>
     */
    protected void setIdentifier(Resource id) {
        this._id = id;
    }
    
  	/**
  	 * Sets the property selector this instance describes
  	 * 
  	 * @param prop A property <code>ISelector</code>
  	 */
  	public void setProperty(ISelector propertySelector) {
  		this._propertySelector = propertySelector;
  	}
    
    /**
     * Sets the maximum recursion depth.
     * 
     * @param depth An <code>int</code>
     */
    public void setDepth(int depth) {
        this._depth = depth;
    }
    
    /**
     * Sets the sublens set used in this description.
     * 
     * @param sub A <code>LensSet</code>
     */
    public void setSublenses(Set<Lens> sub) {
        this._sublenses = sub;
    }
    
    /**
     * Appends a sublens to the set of sublenses.
     * 
     * @param sub A <code>Lens</code>
     */
    public void addSublens(Lens sub) {
        this._sublenses.add(sub);
    }
    
    /**
     * Sets the style group to use with the property description.
     * 
     * @param use A <code>Group</code> with styles
     */
    public void setGroupUse(Group use) {
    	this._groupUse = use;
    }

    /**
     * Sets the format to use with the property description.
     * 
     * @param use A <code>Format</code>
     */
    public void setFormatUse(Format use) {
    	this._formatUse = use;
    }

    /**
     * Parses a fresnel:PropertyDescription.
     * 
     * @param lens A <code>Lens</code> (the lens being parsed)
     * @param in The <code>Repository</code> containing the configuration
     * @param selected The <code>Resource</code> that is a property description
     * @param conf The <code>Configuration</code>, for lookups of existing resources
     * @return A <code>PropertyDescription</code>
     * @throws UnresolvableException When too many predicates are used beyond their
     *         OWL cardinality restriction 
     * @throws ParsingException When an improper model form is used in configuration
     */
    public static PropertyDescription parse(Lens lens, Repository in, Resource selected, Configuration conf)
    throws UnresolvableException, ParsingException, ResourceNotFoundException {
    	PropertyDescription out = new PropertyDescription(selected);
    	try {
    		RepositoryConnection conn = in.getConnection();
            RepositoryResult<Statement> pdSI = conn.getStatements(selected, FresnelCore.property, (Value) null, false);
    		while (pdSI.hasNext()) {
    			Statement stmt = (Statement) pdSI.next();
    			if (pdSI.hasNext())
    				throw new UnresolvableException("More than one fresnel:property predicate used.");

    			Value obj = stmt.getObject();
    			ISelector selector = null;

  				// Evaluate the selector type for a property
  				if (obj instanceof Resource) {
  					// This is a type selector
  					selector = new PropertySelector((URI) obj);
  					out.setProperty(selector);
  				} else if (obj instanceof Literal) {
  					Literal domainL = (Literal) obj;
  					// TODO: catch bad expressions? throw exceptions?
  					if (domainL.getDatatype().equals(FresnelCoreTypes.fslSelector)) {
  						selector = new FSESelector(domainL.getLabel(), _fslContext, conf.getNamespaceMap());
  						out.setProperty(selector);
  					}
  					if (domainL.getDatatype().equals(FresnelCoreTypes.sparqlSelector)) {
  						selector = new SPARQLSelector(domainL.getLabel(), conf.getNamespaces());
  						out.setProperty(selector);
  					}
  				} else {
    				throw new ParsingException("Expected but did not find a resource value for fresnel:property");
    			}
    		}
    		pdSI.close();

    		pdSI = conn.getStatements(selected, FresnelCore.sublens, (Value) null, false);
    		while (pdSI.hasNext()) {
    			Statement stmt = (Statement) pdSI.next();
    			Value obj = stmt.getObject();
    			if (obj instanceof Resource) {
    				Resource objRes = (Resource) obj;
    				Lens sublens = (!objRes.equals(lens.getIdentifier())) ? conf.lensLookup(objRes) : lens;
    				out.addSublens(sublens);
    			} else if (obj instanceof Literal) {
    				// you could implement a fresnel-querying component for finding lenses here
    				// we chose not to
    			} else {
    				throw new ResourceNotFoundException("Expected but did not find a resource value for fresnel:sublens");
    			}            
    		}
    		pdSI.close();

    		pdSI = conn.getStatements(selected, FresnelCore.depth, (Value) null, false);
    		if (!pdSI.hasNext()) out.setDepth(DEFAULT_MAX_DEPTH);
    		while (pdSI.hasNext()) {
    			Statement stmt = (Statement) pdSI.next();
    			if (pdSI.hasNext())
    				throw new UnresolvableException("More than one fresnel:depth predicate used.");

    			Value obj = stmt.getObject();
    			if (obj instanceof Literal && Integer.parseInt(((Literal) stmt.getObject()).getLabel()) >= 0) {
    				out.setDepth(Integer.parseInt(((Literal) stmt.getObject()).getLabel()));
    			} else {
    				throw new ParsingException("Expected but did not find a non-negative integer value for fresnel:depth");
    			}
    		}
    		pdSI.close();

    		pdSI = conn.getStatements(selected, FresnelCore.use, (Value) null, false);
    		while (pdSI.hasNext()) {
    			Statement stmt = (Statement) pdSI.next();
    			if (pdSI.hasNext())
    				throw new UnresolvableException("More than one fresnel:use predicate used.");

    			Value obj = stmt.getObject();
    			if (obj instanceof Resource) {
    				Resource objRes = (Resource) obj;
    				try {
    					Group objGroup = conf.groupLookupOrAdd(objRes);
    					out.setGroupUse(objGroup);
    				} catch (ResourceNotFoundException e) {
    					// if not a group, try a format
    					try {
    						Format objFormat = conf.formatLookup(objRes);
    						out.setFormatUse(objFormat);
    					} catch (ResourceNotFoundException re) {
    						// if not a style, there's nothing to fall back to
    						throw new ParsingException("Failed to find useful description of fresnel:use object: " + ((URI) objRes).toString());
    					}
    				}
    			} else {
    				throw new ParsingException("Expected but did not find a resource value for fresnel:use");
    			}
    		}
    		pdSI.close();
    		conn.close();
    	} catch (RepositoryException e) {
        	// TODO: how to handle this exception
        }
        return out;
    }

    /**
     * @see edu.mit.simile.fresnel.selection.ISelector#selectStatements(Repository, Resource)
     */
    public Iterator<Statement> selectStatements(Repository in, Resource selected) throws InvalidResultSetException {
    	
    	Iterator<Statement> si = _propertySelector.selectStatements(in, selected);
    		
    	return si;
    }

    /**
     * @see edu.mit.simile.fresnel.selection.ISelector#canSelectStatements()
     */
    public boolean canSelectStatements() {
        return this._statements;
    }

    /**
     * @see edu.mit.simile.fresnel.selection.ISelector#selectResources(Repository)
     */
    public Iterator<Resource> selectResources(Repository in) throws InvalidResultSetException {
        throw new InvalidResultSetException("PropertyDescription cannot be used to select resources");
    }

    /**
     * @see edu.mit.simile.fresnel.selection.ISelector#canSelectResources()
     */
    public boolean canSelectResources() {
        return this._resources;
    }

    /**
     * @see edu.mit.simile.fresnel.selection.ISelector#selectNodes(Repository)
     */
    public Iterator<Value> selectNodes(Repository in) throws InvalidResultSetException {
        throw new InvalidResultSetException("PropertyDescription cannot be used to select object nodes");
    }

    /**
     * @see edu.mit.simile.fresnel.selection.ISelector#canSelectNodes()
     */
    public boolean canSelectNodes() {
        return this._nodes;
    }

    /**
     * @see edu.mit.simile.fresnel.selection.ISelector#canSelect(Repository, Resource, URI)
     */
	public boolean canSelect(Repository in, Resource selected, URI prop) {
		boolean out = false;
		
		out = _propertySelector.canSelect(in, selected, prop);

		return out;
	}
	
    /**
     * @see edu.mit.simile.fresnel.selection.ISelector#canSelect(Repository, Resource)
     */
    public boolean canSelect(Repository in, Resource selected) {
			boolean out = false;
			
			out = _propertySelector.canSelect(in, selected);
			
	    return out;
    }

    /**
     * @see edu.mit.simile.fresnel.selection.ISelector#canSelect(Repository, Value)
     */
    public boolean canSelect(Repository in, Value selected) {
        return this._nodes;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String state = "   [ISelector:PropertyDescription " + super.toString() + "]\n";
        state += "   " + this._id + "\n";
        state += "   property_selector: " + this._propertySelector.toString() + "\n";
        state += "   depth: " + this._depth + "\n";
        state += "   " + this._sublenses + "\n";
        return state;
    }
}
