/*
 * Created on Mar 16, 2005
 */
package edu.mit.simile.fresnel.selection;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import fr.inria.jfresnel.fsl.FSLPath;

import edu.mit.simile.fresnel.FresnelCoreTypes;
import edu.mit.simile.fresnel.configuration.Configuration;
import edu.mit.simile.fresnel.util.RDFList;
import edu.mit.simile.fresnel.util.WrappedVector;
import edu.mit.simile.vocabularies.FresnelCore;

import java.util.Iterator;

/**
 * Ordered set of selectors describing a set of properties concerning an instance of
 * a specific rdf:type.
 * 
 * @author ryanlee
 */
public class PropertySet extends WrappedVector<ISelector> {
    /**
     * Resource defined by the schema.
     */
    protected static final Resource _schemaResource = FresnelCore.PropertySet;
    
	/**
	 * Context for determining FSL first location step type.
	 */
	protected final static short _fslContext = FSLPath.ARC_STEP;

    /**
     * Track fresnel:allProperties usage.
     */
    private boolean _seenAllProperties = false;
    
    /**
     * Empty constructor.
     */
    public PropertySet() {
        super();
    }
    
    /**
     * Retrieves the schema resource defined in this class.
     * 
     * @return A <code>Resource</code>
     */
    public static Resource getSchemaResource() {
        return _schemaResource;
    }
    
    /**
     * Add new properties to the set.  Though any ISelector can be used, only
     * PropertySelectors and their inheritors and property-describing query
     * mechanisms should be used.
     * 
     * @param selector An <code>ISelector</code>
     */
    public void addSelector(ISelector selector) {
        this._base.add(selector);
    }
    
    public boolean containsSelector(PropertySelector selector) {
    		boolean out = false;
    		for (Iterator<ISelector> psi = iterator(); psi.hasNext(); ) {
    			ISelector next = psi.next();
    			if (next instanceof PropertySelector) {
    				out = out ^ ((PropertySelector) next).getProperty().equals(selector.getProperty());
    			}
    		}
    		return out;
    }
    
    /**
     * Returns an iterator over the contents of the property set.
     * 
     * @return An <code>Iterator</code>
     */
    public Iterator<ISelector> iterator() {
        return this._base.iterator();
    }
    
    /**
     * Retrieves whether or not fresnel:allProperties has been used in this
     * property set yet.
     * 
     * @return A <code>boolean</code>
     */
    public boolean seenAllProperties() {
        return this._seenAllProperties;
    }
    
    /**
     * Sets the value of whether or not fresnel:allProperties has been used.
     * 
     * @param seen A <code>boolean</code> indicator.
     */
    public void setSeenAllProperties(boolean seen) {
        this._seenAllProperties = seen;
    }
    
    /**
     * Parses an rdf:List of selection mechanisms for a showProperties list.
     *  
     * @param lens A <code>Lens</code> (the lens being parsed)
     * @param in A <code>Repository</code>
     * @param list A <code>RDFList</code>
     * @param conf The <code>Configuration</code> that's parsing this list
     * @return A new <code>PropertySet</code>
     * @throws ParsingException When invalid property lists are presented
     */
    public static PropertySet showParse(Lens lens, Repository in, RDFList list, Configuration conf) throws ParsingException, UnresolvableException {
        return parse(lens, in, list, true, conf);
    }
    
    /**
     * Parses an rdf:List of selection mechanisms for a hideProperties list.
     * 
     * @param lens A <code>Lens</code> (the lens being parsed)
     * @param in A <code>Repository</code>
     * @param list A <code>RDFList</code>
     * @param conf The <code>Configuration</code> that's parsing this list
     * @return A new <code>PropertySet</code>
     * @throws ParsingException When invalid property lists are presented
     */
    public static PropertySet hideParse(Lens lens, Repository in, RDFList list, Configuration conf) throws ParsingException, UnresolvableException {
        return parse(lens, in, list, false, conf);
    }

    /**
     * Parses an rdf:List of selection mechanisms.
     *  
     * @param lens A <code>Lens</code> (the lens being parsed)
     * @param in A <code>Model</code>
     * @param list A <code>Repository</code>
     * @param allowAllProperties Whether the fresnel:allProperties property set is allowed
     *        in this list
     * @param conf The <code>Configuration</code> that's parsing this list
     * @return A new <code>PropertySet</code>
     * @throws ParsingException When invalid property lists are presented
     */
    private static PropertySet parse(Lens lens, Repository in, RDFList list, boolean allowAllProperties, Configuration conf)
    	throws ParsingException, UnresolvableException {
        PropertySet out = new PropertySet();
        for (Iterator listI = list.iterator(); listI.hasNext(); ) {
            Value selectorNode = (Value) listI.next();
            ISelector selector = propertyParse(lens, in, selectorNode, allowAllProperties, conf);
            if (AllPropertiesSelector.isAllProperties(selector)) {
                if (out.seenAllProperties()) {
                    throw new ParsingException("allProperties token used more than once");
                } else {
                    out.add(selector);
                    out.setSeenAllProperties(true);
                }
            } else {
                out.add(selector);
            }
    		}
        return out;
    }
    
    /**
     * Parses a Repository node into the appropriate selector for a shown property.
     * 
     * @param lens A <code>Lens</code> (the lens being parsed)
     * @param in A <code>Repository</code>
     * @param selector The node in the Repository acting as a selector
     * @param conf The <code>Configuration</code> that's parsing this list
     * @return An <code>ISelector</code>
     * @throws ParsingException When invalid property lists are presented
     */
    public static ISelector showPropertyParse(Lens lens, Repository in, Value selector, Configuration conf)
    	throws ParsingException, UnresolvableException {
        return propertyParse(lens, in, selector, true, conf);
    }
    
    /**
     * Parses a Repository node into the appropriate selector for a hidden property.
     * 
     * @param lens A <code>Lens</code> (the lens being parsed)
     * @param in A <code>Repository</code>
     * @param selector The node in the Repository acting as a selector
     * @param conf The <code>Configuration</code> that's parsing this list
     * @return An <code>ISelector</code>
     * @throws ParsingException When invalid property lists are presented
     */
    public static ISelector hidePropertyParse(Lens lens, Repository in, Value selector, Configuration conf)
    	throws ParsingException, UnresolvableException {
        return propertyParse(lens, in, selector, false, conf);
    }

    /**
     * Parses a Repository node into the appropriate selector.
     * 
     * @param lens A <code>Lens</code> (the lens being parsed)
     * @param in A <code>Repository</code>
     * @param selector The node in the Repository acting as a selector
     * @param allowAllProperties Whether the fresnel:allProperties property set is allowed
     *        in this list
     * @param conf The <code>Configuration</code> that's parsing this list
     * @return An <code>ISelector</code>
     * @throws ParsingException When invalid property lists are presented
     */
    private static ISelector propertyParse(Lens lens, Repository in, Value selector, boolean allowAllProperties, Configuration conf)
    	throws UnresolvableException, ParsingException {
        ISelector out = null;
        if (selector instanceof Resource) {
            Resource selectorResource = (Resource) selector;
    		boolean stmtExists = false;
    		try {
    			RepositoryConnection conn = in.getConnection();
    			stmtExists = conn.hasStatement(selectorResource, FresnelCore.property, (Value) null, false);
    			conn.close();
    		} catch (RepositoryException e) {
    			// TODO: how to handle exception
    		}
            if (selectorResource.equals(AllPropertiesSet.getSchemaResource()) &&
                    allowAllProperties) {
                out = new AllPropertiesSelector();
            } else if (selectorResource.equals(AllPropertiesSet.getSchemaResource()) &&
                        !allowAllProperties) {
                throw new ParsingException(selectorResource.toString() + 
                        " cannot use fresnel:allProperties property set");
            } else if (selectorResource.equals(FresnelCore.member)) {
                out = new ContainerMemberSelector();
            } else if (selectorResource instanceof BNode || stmtExists) {
                // This is a :PropertyDescription
                try {
                    out = PropertyDescription.parse(lens, in, selectorResource, conf);
                } catch (ResourceNotFoundException e) {
                    // turn into a parsing exception
                    throw new ParsingException("Cannot find a resource referred to in the PropertyDescription " + selectorResource);
                }
            } else {
                URI selectorProperty = (URI) selectorResource;
                out = new PropertySelector(selectorProperty);
            }
        } else if (selector instanceof Literal) {
            // This is a string-based selector
            Literal selectorLiteral = (Literal) selector;
            if (selectorLiteral.getDatatype().equals(FresnelCoreTypes.fslSelector)) {
                out = new FSESelector(selectorLiteral.getLabel(), _fslContext, conf.getNamespaceMap());
            } else if (selectorLiteral.getDatatype().equals(FresnelCoreTypes.sparqlSelector)) {
                out = new SPARQLSelector(selectorLiteral.getLabel(), conf.getNamespaces());
            }
        } else {
            throw new ParsingException("Could not determine how to parse " + selector);
        }
        return out;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String state = "  [PropertySet " + super.toString() + "]\n";
        Iterator<ISelector> it = this.iterator();
        while (it.hasNext()) {
            ISelector select = it.next();
            state += select;
        }
        return state;
    }
}
