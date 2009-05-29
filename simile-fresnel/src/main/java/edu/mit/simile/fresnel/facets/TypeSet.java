package edu.mit.simile.fresnel.facets;

import java.util.Iterator;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import edu.mit.simile.fresnel.selection.ParsingException;
import edu.mit.simile.fresnel.selection.UnresolvableException;
import edu.mit.simile.fresnel.util.RDFList;
import edu.mit.simile.fresnel.util.WrappedVector;
import edu.mit.simile.vocabularies.Facets;

/**
 * Each set of facets can be related to a set of classes they should be used with.
 * This is the set of target classes for one facet set.
 * 
 * @author ryanlee
 */
public class TypeSet extends WrappedVector<Resource> {
	/**
	 * If this is intended for any and all classes.
	 */
	private boolean _forAll;
	
	/**
	 * Constructor for initializing wrapped vector.
	 */
	public TypeSet() {
		super();
		this._forAll = false;
	}
	
	/**
	 * Generally used to create a target class set of 'all;' to use false
	 * as the parameter is redundant with the empty constructor.
	 * 
	 * @param all Set true if target class set is all classes.
	 */
	public TypeSet(boolean all) {
		super();
		this._forAll = all;
	}
	
	/**
	 * Check if this is for all classes.
	 * 
	 * @return True if for all classes
	 */
	public boolean isForAll() {
		return this._forAll;
	}
		
	/**
	 * Returns a TypIterator instead of a normal iterator.
	 * 
	 * @return A <code>TypeIterator</code>
	 */
	public Iterator<Resource> iterator() {
		return this._base.iterator();
	}
	
	
	/**
	 * Adds a type to the existing set.
	 * 
	 * @param type A <code>Resource</code>
	 * @return Success or failure
	 */
	public boolean addType(Resource type) {
		return this._base.add(type);
	}
	
	/**
	 * Removes a type from the existing set.
	 * 
	 * @param type A <code>Resource</code>
	 * @return Success or failure
	 */
	public boolean removeType(Resource type) {
		return this._base.remove(type);
	}
	
	/**
	 * Add all elements from a type set into this one.
	 * 
	 * @param arg0 A <code>TypeSet</code>
	 * @return Success or failure
	 */
	public boolean addTypeSet(TypeSet arg0) {
		return this._base.addAll(arg0._base);
	}
	
	/**
	 * Parse configuration for type set information.
	 * 
	 * @param source Data <code>Repository</code>
	 * @param focus <code>Resource</code> to parse
	 * @return A configured <code>TypeSet</code>
	 */
	public static TypeSet parse(Repository source, Resource focus)
	throws UnresolvableException, ParsingException {
		TypeSet out = new TypeSet();
		try {
			RepositoryConnection conn = source.getConnection();
            RepositoryResult<Statement> typeI = conn.getStatements(focus, Facets.types, (Value) null, false);
			try {
				if (typeI.hasNext()) {
					Statement typeStmt = typeI.next();
					Value typesNode = typeStmt.getObject();
					if (typeI.hasNext()) throw new UnresolvableException("More than one :types value available");
					if (typesNode instanceof Resource && RDFList.isRDFList(source, (Resource) typesNode)) {
						RDFList typeRDFList = new RDFList(source, (Resource) typesNode);
						if (typeRDFList.isValid()) {
							for (Iterator listI = typeRDFList.iterator(); listI.hasNext(); ) {
								Resource typeNode = (Resource) listI.next();
								out.addType(typeNode);
							}
						} else {
							throw new ParsingException(typesNode.toString() + "is not a valid rdf:List");
						}
					} else if (typesNode.equals(Facets.allTypes)) {
						out = new TypeSet(true);
					}
				}
			} finally {
				typeI.close();
			}
			conn.close();
		} catch (RepositoryException e) {
			throw new UnresolvableException("Problem connecting to repository: " + e.getLocalizedMessage());
		}
		return out;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String state = "[TypeSet " + super.toString() + "]\n";
		Iterator<Resource> it = this.iterator();
		while (it.hasNext()) {
			Resource select = it.next();
			state += "     [Type " + select + "]\n";
		}
		return state;
	}
}
