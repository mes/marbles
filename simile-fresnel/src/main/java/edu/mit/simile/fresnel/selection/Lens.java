/*
 * Created on Mar 16, 2005
 */
package edu.mit.simile.fresnel.selection;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import fr.inria.jfresnel.fsl.FSLPath;

import edu.mit.simile.fresnel.FresnelCoreTypes;
import edu.mit.simile.fresnel.FresnelResource;
import edu.mit.simile.fresnel.configuration.Configuration;
import edu.mit.simile.fresnel.purpose.Purpose;
import edu.mit.simile.fresnel.util.RDFList;
import edu.mit.simile.vocabularies.FresnelCore;

/**
 * Java representation of the Fresnel owl:Class :Lens.
 * As per the schema, a Lens specifies what parts of a model
 * to select via the :lensDomain (resources encompassed by the
 * ISelector) and which properties of matching resources to
 * display via the :showProperties set.
 * 
 * @author ryanlee
 */
public class Lens extends FresnelResource {
	/**
	 * Context for determining FSL first location step type.
	 */
	protected final static short _fslContext = FSLPath.NODE_STEP;

	/**
     * Resource identifying this lens.
     */
    protected Resource _identifier;
    
    /**
     * Match the domain to a model to choose resources for display.
     */
    protected Set<ISelector> _domains;
    
    /**
     * Specify the purpose of the lens.
     */
    protected Set<Purpose> _purposes;
    
    /**
     * The set of properties to display for each matching resource.
     */
    protected PropertySet _show;
    
    /**
     * The set of properties to display for each matching resource.
     */
    protected PropertySet _hide;
    
    /**
     * Resource defined in the schema.
     */
    protected static final Resource _schemaResource = FresnelCore.Lens;
    
    /**
     * Is not an instance lens by default.
     */
    private static final boolean IS_INSTANCE = false;
    
    /**
     * Empty constructor.
     */
    public Lens() {
        this._purposes = new HashSet<Purpose>();
        this._show = new PropertySet();
        this._hide = new PropertySet();
        this._domains = new HashSet<ISelector>();
    }
    
    /**
     * Parses a lens from a model into a <code>Lens</code> object.
     * 
     * @param in The configuration source <code>Grap</code>
     * @param selected The <code>Resource</code> defining the lens
     * @param conf The <code>Configuration</code> that's parsing out this lens
     * @throws ParsingException If semantic errors in use of Fresnel vocabulary exist
     * @throws UnresolvableException If cardinality constraints on certain properties are violated
     */
    public Lens(Repository in, Resource selected, Configuration conf)
    throws ParsingException, UnresolvableException {
    	this(selected);
        
        try {
            // Parse domain
            parseDomain(in, selected, conf);
            
        	RepositoryConnection conn = in.getConnection();

        	// Set of purposes
            RepositoryResult<Statement> purposesI = conn.getStatements(selected, FresnelCore.purpose, (Value) null, false);
        	try {
        		while (purposesI.hasNext()) {
        			Value purposeNode = purposesI.next().getObject();
        			if (purposeNode instanceof Resource) {
        				Resource purposeRes = (Resource) purposeNode;
        				addPurpose(new Purpose(purposeRes));
        			} else {
        				throw new ParsingException(purposeNode.toString() + "could not be used as a :Purpose");
        			}
        		}
        	} finally {
        		purposesI.close();
        	}

        	// Parse the property selections
            RepositoryResult<Statement> showI = conn.getStatements(selected, FresnelCore.showProperties, (Value) null, false);
        	try {
        		if (showI.hasNext()) {
        			Statement showStmt = showI.next();
        			Value showsNode = showStmt.getObject();
        			if (showI.hasNext()) throw new UnresolvableException("More than one :showProperties value available");
        			if (showsNode instanceof Resource && RDFList.isRDFList(in, (Resource) showsNode)) {
        				RDFList showsRDFList = new RDFList(in, (Resource) showsNode);
        				if (showsRDFList.isValid()) {
        					setShowProperties(PropertySet.showParse(this, in, showsRDFList, conf));
        				} else {
        					throw new ParsingException(showsNode.toString() + "is not a valid rdf:List");
        				}
        			} else {
        				addShowProperty(PropertySet.showPropertyParse(this, in, showsNode, conf));
        			}
        		}
        	} finally {
        		showI.close();
        	}

        	// Parse the property hiding selections
            RepositoryResult<Statement> hideI = conn.getStatements(selected, FresnelCore.hideProperties, (Value) null, false);
        	try {
        		if (hideI.hasNext()) {
        			Statement hideStmt = (Statement) hideI.next();
        			Value hidesNode = hideStmt.getObject();
        			if (hideI.hasNext()) throw new UnresolvableException("More than one :hideProperties value available");
        			if (hidesNode instanceof Resource && RDFList.isRDFList(in, (Resource) hidesNode)) {
        				RDFList hidesRDFList = new RDFList(in, (Resource) hidesNode);
        				if (hidesRDFList.isValid()) {
        					setHideProperties(PropertySet.hideParse(this, in, hidesRDFList, conf));
        				} else {
        					throw new ParsingException(hidesNode.toString() + "is not a valid rdf:List");
        				}
        			} else {
        				addHideProperty(PropertySet.hidePropertyParse(this, in, hidesNode, conf));
        			}
        		}
        	} finally {
        		hideI.close();
        	}
            
            conn.close();
        } catch (RepositoryException e) {
        	throw new UnresolvableException("Problem connecting to repository: " + e.getLocalizedMessage());
        }
    }
    
    /**
     * Construct lens based on identifier.
     * 
     * @param id A <code>Resource</code>
     */
    public Lens(Resource id) {
        this();
        setIdentifier(id);
    }
    
    /**
     * Construct lens based on lensDomain.
     * 
     * @param domains A <code>DomainSet</code>
     */
    public Lens(Set<ISelector> domains) {
        this();
        setDomainSet(domains);
    }
    
    /**
     * Returns the resource identifying the lens.
     * 
     * @return A <code>Resource</code>
     */
    public Resource getIdentifier() {
        return this._identifier;
    }
    
    /**
     * Returns the lens domain.
     * 
     * @return A <code>ISelector</code>
     */
    public Set<ISelector> getDomainSet() {
        return this._domains;
    }
    
    /**
     * Returns the purpose of the lens.
     * 
     * @return A <code>Purpose</code>
     */
    public Set<Purpose> getPurposes() {
        return this._purposes;
    }
    
    /**
     * Returns the set of properties used for display.
     * 
     * @return A <code>PropertySet</code> of displayed predicates and values
     */
    public PropertySet getShowProperties() {
        return this._show;
    }
    
    /**
     * Returns the set of properties used for hiding things in display.
     * 
     * @return A <code>PropertySet</code> of hidden predicates and values
     */
    public PropertySet getHideProperties() {
        return this._hide;
    }
    
    /**
     * Sets the resource identifying this lens.
     * 
     * @param identifier A unique <code>Resource</code>
     */
    protected void setIdentifier(Resource identifier) {
        this._identifier = identifier;
    }
    
    /**
     * Sets the lens domain.
     * 
     * @param domains A <code>Set</code> of <code>ISelector</code>s
     */
    protected void setDomainSet(Set<ISelector> domains) {
        this._domains = domains;
    }
    
    /**
     * Sets the set of properties to display.
     * 
     * @param show A <code>PropertySet</code>
     */
    protected void setShowProperties(PropertySet show) {
        this._show = show;
    }
    
    /**
     * Sets the set of properties to hide.
     * 
     * @param hide A <code>PropertySet</code>
     */
    protected void setHideProperties(PropertySet hide) {
        this._hide = hide;
    }
    
    /**
     * Adds a lens domain
     * 
     * @param domain An <code>ISelector</code>
     */
    protected void addDomain(ISelector domain) {
        this._domains.add(domain);
    }
    
    /**
     * Adds a lens purpose.
     * 
     * @param purpose A <code>Purpose</code>
     */
    protected void addPurpose(Purpose purpose) {
        this._purposes.add(purpose);
    }
    
    /**
     * Add a property to be shown.
     * 
     * @param show An <code>ISelector</code>
     */
    protected void addShowProperty(ISelector show) {
        this._show.add(show);
    }

    /**
     * Add a property to be hidden.
     * 
     * @param hide An <code>ISelector</code>
     */
    protected void addHideProperty(ISelector hide) {
        this._hide.add(hide);
    }
    
    /**
     * Check if this lens has a particular purpose.
     * 
     * @param check A <code>Purpose</code> to check against
     * @return A <code>boolean</code> indicator
     */
    public boolean hasPurpose(Purpose check) {
    	for (Purpose purpose : this._purposes) {
    		if (purpose.equals(check)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * Check if this lens has a default purpose.
     * 
     * @return A <code>boolean</code> indicator
     */
    public boolean isDefault() {
        Purpose defaultP = new Purpose(FresnelCore.defaultLens);
        return hasPurpose(defaultP);
    }
    
    /**
     * Check if this lens is an instance lens.
     * 
     * @return False if not an instance domain lens, true if so.
     */
    public boolean isInstance() {
        return IS_INSTANCE;
    }
    
    /**
     * Parse out domain information (depends on the kind of lens) and add to the lens.
     * 
     * @param in The <code>Repository</code> containing configuration information
     * @param selected The <code>Resource</code> identifying the lens
     * @param conf The <code>Configuration</code> parsing this lens
     */
    protected void parseDomain(Repository in, Resource selected, Configuration conf) throws RepositoryException {
    	RepositoryConnection conn = in.getConnection();
        RepositoryResult<Statement> domainsI = conn.getStatements(selected, FresnelCore.classLensDomain, (Value) null, false);
    	try {
    		while (domainsI.hasNext()) {
    			ISelector domain = null;
    			Value domainNode = domainsI.next().getObject();
    			if (domainNode instanceof Resource) {
    				// This is a type selector
    				domain = new TypeSelector((Resource) domainNode);
    			} else if (domainNode instanceof Literal) {
    				Literal domainL = (Literal) domainNode;
    				// TODO: catch bad expressions?  throw exceptions?
    				if (domainL.getDatatype().equals(FresnelCoreTypes.fslSelector)) {
    					domain = new FSESelector(domainL.getLabel(), _fslContext, conf.getNamespaceMap());
    				} else if (domainL.getDatatype().equals(FresnelCoreTypes.sparqlSelector)) {
    					domain = new SPARQLSelector(domainL.getLabel(), conf.getNamespaces());
    				}
    			}
    			addDomain(domain);
    		}
    	} finally {
    		domainsI.close();
    	}
    	conn.close();
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String state = " [Lens " + super.toString() + "]\n";
        state += " " + this._identifier + "\n";
        state += "\n Domain(s):\n";
        state += this._domains;
        state += "\n Purpose(s):\n";
        state += this._purposes;
        state += "\n Show Properties:\n";
        state += this._show;
        state += "\n Hide Properties:\n";
        state += this._hide;
        state += "\n ====== [end lens] \n";
        return state;
    }
}
