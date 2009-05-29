package edu.mit.simile.fresnel;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Vector;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;

/**
 * Various RDF processing shorthands for Fresnel.
 * 
 * @author ryanlee
 */
public class FresnelUtilities {
	/**
	 * Reads data from a URL in a given syntax into a Sesame repository.
	 * 
	 * @param store Add RDF to this <code>LocalRepository</code>
	 * @param url The <code>String</code> location of the data
	 * @param syntax The <code>String</code> syntax of the data
	 * @throws Exception For any problems encountered during read of data from the URL
	 */
	public static void read(Repository store, String url, String syntax) throws Exception {
		RDFFormat format = null;
		if (syntax.equals("TURTLE") || syntax.equals("N3"))
			format = RDFFormat.TURTLE;
		else if (syntax.equals("RDFXML"))
			format = RDFFormat.RDFXML;
		else if (syntax.equals("NTRIPLES"))
			format = RDFFormat.NTRIPLES;
		if (null != format) {
			java.net.URL myRDFData = new java.net.URL(url);
			String baseURI = url;
			RepositoryConnection conn = store.getConnection();
			InputStream is = myRDFData.openStream();
			conn.add(is, baseURI, format);
			is.close();
			conn.commit();
			conn.close();
		}
	}
	
	/**
	 * Fetch resources into an iterator that match the property-object pair from the given graph.
	 * 
	 * @param in The source <code>Graph</code>
	 * @param property The <code>URI</code> property to match
	 * @param object The <code>Value</code> object to match
	 * @return A Sesame <code>ResourceIterator</code>
	 */
	public static Iterator<Resource> listSubjectsWithProperty(Repository in, URI property, Value object) {
		Vector<Resource> resources = new Vector<Resource>();
		try {
			RepositoryConnection conn = in.getConnection();
            RepositoryResult<Statement> it = conn.getStatements((Resource) null, property, object, true);
			while ( it.hasNext() ) {
				Resource subject = dupResource(it.next().getSubject());
				if (!resources.contains(subject)) {
					resources.add(subject);
				}
			}
			it.close();
			conn.close();
		} catch (RepositoryException e) {
			// TODO: how to handle this exception
		}
		return resources.iterator();
	}
	
	/**
	 * Fetch the object value of a resource's property from the given graph.  Only
	 * returns the first, which is an arbitrary selection. 
	 * 
	 * @param in The source <code>Graph</code>
	 * @param subject The subject <code>Resource</code>
	 * @param property The property <code>URI</code>
	 * @return A sesame <code>Value</code>
	 */
	public static Value getSinglePropertyValue(Repository in, Resource subject, URI property) {
		Value out = null;
		try {
			RepositoryConnection conn = in.getConnection();
            RepositoryResult<Statement> it = conn.getStatements(subject, property, (Value) null, true);
			if (it.hasNext()) {
				out = dupValue(it.next().getObject());				
            }
			it.close();
			conn.close();
		} catch (RepositoryException e) {
			// TODO: how to handle this exception
		}
		return out;
	}
	
	/**
	 * Fetch the rdf:type of a resource from the given graph.  Only returns the first
	 * which is an arbitrary selection. 
	 * 
	 * @param in The source <code>Repository</code>
	 * @param subject The subject <code>Resource</code>
	 * @return A <code>Value</code>
	 */
	public static Value getType(Repository in, Resource subject) {
		return getSinglePropertyValue(in, subject, RDF.TYPE);
	}
	
	/**
	 * Copy a value so the original can be finalized without locking problems.
	 * 
	 * @param v A <code>Value</code>
	 * @return A copy of the original <code>Value</code>
	 */
	public static Value dupValue(Value v) {
		if (v instanceof Literal)
			return new LiteralImpl(((Literal) v).getLabel(), ((Literal) v).getDatatype());
		
		return dupResource((Resource) v);
	}
	
	/**
	 * Copy a resource so the original can be finalized without locking problems.
	 * 
	 * @param r A <code>Resource</code>
	 * @return A copy of the original <code>Resource</code>
	 */
	public static Resource dupResource(Resource r) {
		if (r instanceof BNode)
			return dupBNode((BNode) r);
		
		return dupURI((URI) r);
	}
	
	/**
	 * Copy a URI so the original can be finalized without locking problems.
	 * 
	 * @param u A <code>URI</code>
	 * @return A copy of the original <code>URI</code>
	 */
	public static URI dupURI(URI u) {
		return new URIImpl(u.toString());
	}
	
	/**
	 * Copy a BNode so the original can be finalized without locking problems.
	 * 
	 * @param b A <code>BNode</code>
	 * @return A copy of the original <code>BNode</code>
	 */
	public static BNode dupBNode(BNode b) {
		return new BNodeImpl(b.getID());
	}
}
