/*
 * Created on Mar 29, 2005
 */
package edu.mit.simile.fresnel.selection;

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
import edu.mit.simile.fresnel.configuration.Configuration;
import edu.mit.simile.vocabularies.FresnelCore;


/**
 * Selection rules for one specific resource.  Not materially
 * different from a Lens, this only exists for ease of development.
 * 
 * @author ryanlee
 */
public class InstanceLens extends Lens {
    /**
     * Is an instance lens.
     */
    private static final boolean IS_INSTANCE = true;
    
	/**
	 * Context for determining FSL first location step type.
	 */
	protected final static short _fslContext = FSLPath.NODE_STEP;

	/**
     * Empty constructor.
     */
    public InstanceLens() {
        super();
    }
    
    /**
     * Construct lens based on identifier.
     * 
     * @param selected  A <code>Resource</code>
     */
    public InstanceLens(Resource selected) {
        super(selected);
    }
    
    /**
     * Construct lens based on lensDomain.
     * 
     * @param domains A <code>DomainSet</code>
     */
    public InstanceLens(Set<ISelector> domains) {
        super(domains);
    }
    
    /**
     * Parses a lens from a model into a <code>InstanceLens</code> object.
     * 
     * @param in The configuration source <code>Repository</code>
     * @param subject The <code>Resource</code> defining the lens
     * @param configuration The <code>Configuration</code> that's parsing out this lens
     * @throws ParsingException If semantic errors in use of Fresnel vocabulary exist
     * @throws UnresolvableException If cardinality constraints on certain properties are violated
     */
    public InstanceLens(Repository in, Resource subject, Configuration configuration) 
    throws ParsingException, UnresolvableException {
    		super(in,subject,configuration);
	}

	/**
     * Test for whether a lens is an instance lens.
     * 
     * @return True if this is an instance (always true)
     */
    public boolean isInstance() {
        return IS_INSTANCE;
    }
    
    /**
     * Parse out domain information (depends on the kind of lens).
     * 
     * @param in The <code>Repository</code> containing configuration information
     * @param selected The <code>Resource</code> identifying the lens
     * @param conf The <code>Configuration</code> parsing this lens
     */
    protected void parseDomain(Repository in, Resource selected, Configuration conf) throws RepositoryException {
    	RepositoryConnection conn = in.getConnection();
        RepositoryResult<Statement> domainsI = conn.getStatements(selected, FresnelCore.instanceLensDomain, (Value) null, false);
        while (domainsI.hasNext()) {
            ISelector domain = null;
            Value domainNode = domainsI.next().getObject();
            if (domainNode instanceof Resource) {
                domain = new InstanceSelector((Resource) domainNode);
            } else if (domainNode instanceof Literal) {
                Literal domainL = (Literal) domainNode;
                if (domainL.getDatatype().equals(FresnelCoreTypes.fslSelector)) {
                    domain = new FSESelector(domainL.getLabel(), _fslContext, conf.getNamespaceMap());
                } else if (domainL.getDatatype().equals(FresnelCoreTypes.sparqlSelector)) {
                    domain = new SPARQLSelector(domainL.getLabel(), conf.getNamespaces());
                }
            }
            addDomain(domain);
        }
        domainsI.close();
        conn.close();
    }
}
