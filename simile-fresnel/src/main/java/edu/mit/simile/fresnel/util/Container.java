package edu.mit.simile.fresnel.util;

import java.util.Iterator;
import java.util.Vector;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

/**
 * Convenience class representing a generic RDF container (super class of bag, alt, seq).
 * 
 * @author ryanlee
 */
public class Container {
	/**
	 * The graph containing this container
	 */
	
	private Repository _source;
	/**
	 * The resource identifying this container
	 */
	private Resource _origin;
	
	/**
	 * Constructor based on graph and container resource.  Check using isXXX() methods before
	 * calling constructor.
	 * 
	 * @param in The data <code>Graph</code>
	 * @param subject The container's identifying <code>Resource</code>
	 */
	public Container(Repository in, Resource subject) {
		this._source = in;
		this._origin = subject;
	}
	
	/**
	 * Whether a resource is any type of RDF container
	 * 
	 * @param in Data <code>Graph</code>
	 * @param subject <code>Resource</code> to examine
	 * @return True if a container, false if not
	 */
	public static boolean isContainer(Repository in, Resource subject) {
		return (isBag(in, subject) || isAlt(in, subject) || isSeq(in, subject));
	}
	
	/**
	 * Whether a resource is a rdf:Bag
	 * 
	 * @param in Data <code>Graph</code>
	 * @param subject <code>Resource</code> to examine
	 * @return True if a bag, false if not
	 */
	public static boolean isBag(Repository in, Resource subject) {
		return is(in, subject, RDF.BAG);
	}
	
	/**
	 * Whether a resource is a rdf:Alt
	 * 
	 * @param in Data <code>Graph</code>
	 * @param subject <code>Resource</code> to examine
	 * @return True if an alt, false if not
	 */
	public static boolean isAlt(Repository in, Resource subject) {
		return is(in, subject, RDF.ALT);
	}

	/**
	 * Whether a resource is a rdf:Seq
	 * 
	 * @param in Data <code>Graph</code>
	 * @param subject <code>Resource</code> to examine
	 * @return True if a seq, false if not
	 */
	public static boolean isSeq(Repository in, Resource subject) {
		return is(in, subject, RDF.SEQ);
	}

	/**
	 * Checks the type of a resource against a suggested type; no inferencing.
	 * 
	 * @param in Data <code>Graph</code>
	 * @param subject The <code>Resource</code> to compare
	 * @param type A <code>URI</code> for the type to check against
	 * @return True if the resource is rdf:type of the type, false if not
	 */
	public static boolean is(Repository in, Resource subject, URI type) {
		boolean out = false;
		try {
			RepositoryConnection conn = in.getConnection();
			out = conn.hasStatement(subject, RDF.TYPE, type, false);
			conn.close();
		} catch (RepositoryException e) {
			// TODO: how to handle exception
		}
		return out;
	}

	/**
	 * Iterator through the container's members.  Synonymous with listContainerMembers()
	 * 
	 * @return An <code>Iterator</code>
	 */
	public Iterator iterator() {
		return listContainerMembers();
	}
	
	/**
	 * Iterator through the container's members.  Synonymous with iterator()

	 * @return An <code>Iterator</code>
	 */
	public Iterator<Resource> listContainerMembers() {
		Vector<Resource> result = new Vector<Resource>();
		try {
			RepositoryConnection conn = this._source.getConnection();
            RepositoryResult<Statement> iter = conn.getStatements(this._origin, (URI) null, (Value) null, false); 
			int maxOrdinal = 0;
			while (iter.hasNext()) {
				Statement stmt = iter.next();
				URI element = stmt.getPredicate();
				if (!element.getLocalName().startsWith("_"))
					continue;

				int ordinal = Integer.parseInt(element.getLocalName().substring(1));
				if (ordinal != 0) {
					if (ordinal > maxOrdinal) {
						maxOrdinal = ordinal;
						result.setSize(ordinal);
					}
					result.setElementAt((Resource) stmt.getObject(), ordinal-1);
				}
			}
			iter.close();
			conn.close();
		} catch (RepositoryException e) {
			// TODO: how to handle exception
		}
		return result.iterator();
	}  
}
