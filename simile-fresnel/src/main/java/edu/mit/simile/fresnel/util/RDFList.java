package edu.mit.simile.fresnel.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import edu.mit.simile.fresnel.FresnelUtilities;

/**
 * Convenience class for rdf:List resources.
 * 
 * @author ryanlee
 */
public class RDFList {
	/**
	 * Data graph containing this rdf:List
	 */
	private Repository _source;
	/**
	 * Resource identifying this rdf:List in the graph
	 */
	private Resource _origin;
	
	/**
	 * Constructor based on graph and rdf:List resource.  Check using isRDFList() and checkValid()
	 * before calling constructor.
	 * 
	 * @param in Data <code>Graph</code>
	 * @param subject The rdf:List <code>Resource</code>
	 */
	public RDFList(Repository in, Resource subject) {
		this._source = in;
		this._origin = subject;
	}
	
	/**
	 * Checks if a resource is actually an rdf:List
	 * 
	 * @param in Data <code>Graph</code>
	 * @param subject The <code>Resource</code> to check
	 * @return True if resource is an rdf:List, false if not
	 */
	public static boolean isRDFList(Repository in, Resource subject) {
		return checkValid(in, subject);
	}
	
	/**
	 * As rdf:List is recursively defined, check at each step that the rest
	 * of the list at this stage is still valid.
	 * 
	 * @return True if this part of the rdf:List is a valid list, false if not
	 */
	public boolean isValid() {
		return checkValid(this._source, this._origin);
	}
	
	/**
	 * Iterator for moving through the list; synonymous with RDFListIterator(this).
	 * 
	 * @return An <code>Iterator</code> through the rdf:List
	 */
	public RDFListIterator iterator() {
		return new RDFListIterator(this);
	}
	
	/**
	 * Checks if the rdf:List is empty.
	 * 
	 * @return True if empty, false otherwise
	 */
	public boolean isEmpty() {
		return (this._origin instanceof URI && ((URI) this._origin).equals(RDF.NIL));
	}
	
	/**
	 * Convenience method to throw exceptions if attempting to access something in
	 * the face of an rdf:nil value in the list.
	 * 
	 * @param condition A <code>String</code> explaining the potential problem
	 * @throws EmptyListException If rdf:List is currently at rdf:nil (no more list to view)
	 */
	public void checkNotNil(String condition) throws EmptyListException {
		if (isEmpty())
			throw new EmptyListException(condition);
	}
	
	/**
	 * Convert the rdf:List to a Java List and work with it in that form instead.
	 * 
	 * @return A <code>List</code>
	 */
	public List<Value> asJavaList() {
		List<Value> l = new ArrayList<Value>();
		
		for (RDFListIterator i = iterator(); i.hasNext(); ) {
			l.add(i.next());
		}
		
		return l;
	}
	
	/**
	 * Returns the head (rdf:first) of a rdf:List; car to those familiar with Lisp
	 * 
	 * @return The value of rdf:first in this rdf:List
	 * @throws EmptyListException If the list is actually empty
	 */
	public Value getHead() throws EmptyListException {
		//if (s_checkValid) {
		checkValid(this._source, this._origin);
		//}
		
		checkNotNil("Tried to get the head of an empty list");
		
		return FresnelUtilities.getSinglePropertyValue(this._source, this._origin, RDF.FIRST);
	}
	
	/**
	 * Returns the remainder (rdf:rest) of a rdf:list; cdr to those familiar with Lisp
	 * 
	 * @return The value of rdf:rest in this rdf:List
	 * @throws EmptyListException If the list is actually empty
	 */
	public RDFList getTail() throws EmptyListException {
		//if (s_checkValid) {
		checkValid(this._source, this._origin);
		//}
		
		checkNotNil("Tried to get the tail of an empty list");
		
		Resource tail = (Resource) FresnelUtilities.getSinglePropertyValue(this._source, this._origin, RDF.REST);
		return new RDFList(this._source, tail);
	}
    
	/**
	 * Checks that an rdf:List is valid; call before constructing
	 * 
	 * @param in Data source <code>Graph</code>
	 * @param subject Potential rdf:List <code>Resource</code>
	 * @return True if a valid rdf:List, false otherwise
	 */
	protected static boolean checkValid(Repository in, Resource subject) {
		if (!(subject instanceof URI) || !((URI) subject).equals(RDF.NIL)) {
			return(checkValidProperty(in, subject, RDF.FIRST) && checkValidProperty(in, subject, RDF.REST));
		} else {
			return true;
		}
	}
	
	/**
	 * Checks that the properties making up an rdf:List point to only one value at a time
	 * 
	 * @param in Data source <code>Graph</code>
	 * @param subject The rdf:List <code>Resource</code> to check
	 * @param p The property <code>URI</code> (normally rdf:first or rdf:rest)
	 * @return True if subject/property has only one value, false otherwise
	 */
	private static boolean checkValidProperty(Repository in, Resource subject, URI p) {
		int count = 0;
		
		try {
			RepositoryConnection conn = in.getConnection();
            RepositoryResult<Statement> j = conn.getStatements(subject, p, null, false);
			try {
				while ( j.hasNext() && count <= 2 ) { 
					count++;
					j.next();
				}
			} finally {
				j.close();
			}
			conn.close();
		} catch (RepositoryException e) {
			// TODO: how to handle exception
		}
		
		// exactly one value is expected
		return (count == 1);
	}
	
	/**
	 * For iterating through the rdf:List construct.
	 * 
	 * @author ryanlee
	 */
	protected class RDFListIterator implements Iterator {
		/**
		 * Tracks the value of rdf:first during iteration
		 */
		private RDFList _head;
		/**
		 * Tracks the previous value of rdf:first during iteration
		 */
		private RDFList _seen;
		
		/**
		 * Constructor based on an rdf:List
		 * 
		 * @param head The <code>RDFList</code>
		 */
		protected RDFListIterator(RDFList head) {
			this._head = head;
		}

		/**
		 * Checks if anything remains in the rdf:List.
		 * 
		 * @return True if anything other than rdf:nil is at the present marker in the rdf:List, false
		 *         otherwise
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return !this._head.isEmpty();
		}

		/**
		 * Fetches the next element in the rdf:List
		 * 
		 * @return An <code>Object</code>
		 * @see java.util.Iterator#next()
		 */
		public Value next() {
			try {
				this._seen = this._head;
				this._head = this._head.getTail();
				
				return this._seen.getHead();
			} catch (EmptyListException e) {
				throw new NoSuchElementException(e.toString());
			}
		}

		/**
		 * A no-op.
		 * 
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			throw new UnsupportedOperationException("remove() not implemented in RDFListIterator");
		}
	}
}
