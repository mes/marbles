package edu.mit.simile.fresnel.format;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import fr.inria.jfresnel.fsl.FSLPath;

import edu.mit.simile.fresnel.FresnelCoreTypes;
import edu.mit.simile.fresnel.configuration.Configuration;
import edu.mit.simile.fresnel.purpose.Purpose;
import edu.mit.simile.fresnel.selection.FSESelector;
import edu.mit.simile.fresnel.selection.ISelector;
import edu.mit.simile.fresnel.selection.ParsingException;
import edu.mit.simile.fresnel.selection.SPARQLSelector;
import edu.mit.simile.fresnel.selection.TypeSelector;
import edu.mit.simile.fresnel.selection.UnresolvableException;
import edu.mit.simile.vocabularies.FresnelCore;

/**
 * Format matching an rdf:type value.
 * 
 * @author ryanlee
 */
public class ClassFormat extends Format {
	/**
	 * Context for determining FSL first location step type.
	 */
	protected final static short _fslContext = FSLPath.NODE_STEP;
	
	/**
	 * Constructor based on the resource identifier of the format.
	 * 
	 * @param id A <code>Resource</code>
	 */
	public ClassFormat(Resource id) {
		super(id);
	}
	
	/**
	 * Parses a resource into a valid fresnel:Format object.
	 * 
	 * @param in The <code>Repository</code> the configuration is derived from
	 * @param selected The fresnel:Format <code>Resource</code>
	 * @param conf The <code>Configuration</code> to add to
	 * @return A new <code>Format</code>
	 * @throws UnresolvableException When the wrong count for a predicate is present
	 * @throws ParsingException When any other syntax error occurs
	 */
	public static Format parse(Repository in, Resource selected, Configuration conf)
	throws UnresolvableException, ParsingException {
		ClassFormat out = new ClassFormat(selected);
		
		try {
			// Parse domain
			ClassFormat.parseDomain(out, in, selected, conf);
			
			RepositoryConnection conn = in.getConnection();

			// Set of purposes
            RepositoryResult<Statement> purposesI = conn.getStatements(selected, FresnelCore.purpose, (Value) null, false);
			try {
				while (purposesI.hasNext()) {
					Value purposeNode = purposesI.next().getObject();
					if (purposeNode instanceof Resource) {
						Resource purposeRes = (Resource) purposeNode;
						out.addPurpose(new Purpose(purposeRes));
					} else {
						throw new ParsingException(purposeNode.toString() + "could not be used as a :Purpose");
					}
				}
			} finally {
				purposesI.close();
			}

			// resource style
            RepositoryResult<Statement> resourceStyleI = conn.getStatements(selected, FresnelCore.resourceStyle, (Value) null, false);
			while (resourceStyleI.hasNext()) {
				Statement resourceStyleS = resourceStyleI.next();
				Value styleObj = resourceStyleS.getObject();
				out.addResourceStyle(Style.parse(styleObj));
			}
			resourceStyleI.close();

			// resource format
            RepositoryResult<Statement> resourceFormatI = conn.getStatements(selected, FresnelCore.resourceFormat, (Value) null, false);
			while (resourceFormatI.hasNext()) {
				Statement resourceFormatS = resourceFormatI.next();
				Resource formatObj = (Resource) resourceFormatS.getObject();
				out.addResourceFormat(FormatDescription.parse(in, formatObj));
			}
			resourceFormatI.close();

			conn.close();
		} catch (RepositoryException e) {
			throw new UnresolvableException("Problem connecting to repository: " + e.getLocalizedMessage());
		}
		
		return out;
	}
	
	/**
	 * Parse out domain information and add to the Format.
	 * 
	 * @param out The <code>Format</code> to modify
	 * @param in The <code>Repository</code> containing configuration information
	 * @param selected The <code>Resource</code> identifying the style
	 * @param conf The <code>Configuration</code> parsing this format
	 */
	protected static void parseDomain(Format out, Repository in, Resource selected, Configuration conf) throws RepositoryException {
		RepositoryConnection conn = in.getConnection();
        RepositoryResult<Statement> domainsI = conn.getStatements(selected, FresnelCore.classFormatDomain, (Value) null, false);
		while (domainsI.hasNext()) {
			ISelector domain = null;
			Value domainNode = domainsI.next().getObject();
			if (domainNode instanceof Resource) {
				// This is a type selector
				domain = new TypeSelector((URI) domainNode);
			} else if (domainNode instanceof Literal) {
				Literal domainL = (Literal) domainNode;
				// TODO: catch bad expressions?  throw exceptions?
				if (domainL.getDatatype().equals(FresnelCoreTypes.fslSelector)) {
					domain = new FSESelector(domainL.getLabel(), _fslContext, conf.getNamespaceMap());
				} else if (domainL.getDatatype().equals(FresnelCoreTypes.sparqlSelector)) {
					domain = new SPARQLSelector(domainL.getLabel(), conf.getNamespaces());
				}
			}
			out.addDomain(domain);
		}
		domainsI.close();
		conn.close();
	}
}
