package edu.mit.simile.fresnel.facets;

import java.util.Iterator;
import java.util.Vector;

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
 * Container for facets, reflecting the fresnel:FacetSet member of the fresnel faceting extension
 * vocabulary.
 * 
 * @author ryanlee
 */
public class FacetSet extends WrappedVector<Facet> {
	/**
	 * Set of types these facets can be used with.
	 */
	protected TypeSet _forTypes;
	
	/**
	 * Resource identifying this FacetSet.
	 */
	protected Resource _identifier;
	
	/**
	 * Set of facets to hide.
	 */
	protected Vector<Facet> _hides;
	
	/**
	 * Resource defined in the schema.
	 */
	protected static final Resource _schemaResource = Facets.FacetSet;
	
	/**
	 * Constructor for initializing wrapped vector.
	 */
	public FacetSet() {
		super();
		this._hides = new Vector<Facet>();
	}
	
	/**
	 * Constructor based on the resource identifying the facet set.
	 * 
	 * @param id A <code>Resource</code>
	 */
	public FacetSet(Resource id) {
		this();
		this._identifier = id;
	}
	
	/**
	 * Retrieves the resource identifying this facet set.
	 * 
	 * @return A <code>Resource</code>
	 */
	public Resource getIdentifier() {
		return this._identifier;
	}
	
	/**
	 * Retrieves an iterator through the set of types this facet set can be used for.
	 * 
	 * @return A <code>TypeIterator</code>
	 */
	public Iterator<Resource> getForTypes() {
		return this._forTypes.iterator();
	}
	
	/**
	 * Sets the set of types this facet set can be use for.
	 * 
	 * @param types A <code>TypeSet</code>
	 */
	public void setForTypes(TypeSet types) {
		this._forTypes = types;
	}
	
	/**
	 * Returns a FacetIterator (instead of a normal iterator) through this set of facets.
	 * 
	 * @return A <code>FacetIterator</code>
	 */
	public Iterator<Facet> facetIterator() {
		return this._base.iterator();
	}
	
	/**
	 * Returns a FacetIterator through the hidden facets.
	 * 
	 * @return A <code>FacetIterator</code>
	 */
	public Iterator<Facet> hideIterator() {
		return this._hides.iterator();
	}
	
	/**
	 * Check if this facet set's type set is actually intended to be used for all types
	 * and not just a specific set.
	 * 
	 * @return True if for all types, false otherwise.
	 */
	public boolean isForAll() {
		return this._forTypes.isForAll();
	}
	
	/**
	 * Adds a facet to the existing set.
	 * 
	 * @param facet A <code>Facet</code>
	 * @return Success or failure
	 */
	public boolean addFacet(Facet facet) {
		return this._base.add(facet);
	}
	
	/**
	 * Adds a facet to the hidden facet list.
	 * 
	 * @param facet A <code>Facet</code>
	 * @return Sucess or failure
	 */
	public boolean addHide(Facet facet) {
		return this._hides.add(facet);
	}
	
	/**
	 * Removes a facet from the existing set.
	 * 
	 * @param facet A <code>Facet</code>
	 * @return Success or failure
	 */
	public boolean removeFacet(Facet facet) {
		return this._base.remove(facet);
	}
	
	/**
	 * Removes a hidden facet from the existing set.
	 * 
	 * @param facet A <code>Facet</code>
	 * @return Success or failure
	 */
	public boolean removeHide(Facet facet) {
		return this._hides.remove(facet);
	}
	
	/**
	 * Add all elements from a facet set into this one.
	 * 
	 * @param arg0 A <code>FacetSet</code>
	 * @return Success or failure
	 */
	public boolean addFacetSet(FacetSet arg0) {
		return (this._base.addAll(arg0._base) && this._hides.addAll(arg0._hides));
	}
	
	/**
	 * Parses a configuration graph for facet set information on the given resource.
	 * 
	 * @param source Configuration source <code>Repository</code>
	 * @param focus <code>Resource</code> to parse
	 * @return A <code>FacetSet</code>
	 */
	public static FacetSet parse(Repository source, Resource focus)
	throws UnresolvableException, ParsingException {
		FacetSet out = new FacetSet(focus);
		try {
			RepositoryConnection conn = source.getConnection();
            RepositoryResult<Statement> facetI = conn.getStatements(focus, Facets.facets, (Value) null, false);
			if (facetI.hasNext()) {
				Statement facetStmt = (Statement) facetI.next();
				Value facetsNode = facetStmt.getObject();
				if (facetI.hasNext()) throw new UnresolvableException("More than one :facets value available");
				if (facetsNode instanceof Resource && RDFList.isRDFList(source, (Resource) facetsNode)) {
					RDFList facetRDFList = new RDFList(source, (Resource) facetsNode);
					if (facetRDFList.isValid()) {
						for (Iterator listI = facetRDFList.iterator(); listI.hasNext(); ) {
							Resource facetNode = (Resource) listI.next();
							Facet facet = new Facet(facetNode);
							out.addFacet(facet);
						}
					} else {
						throw new ParsingException(facetsNode.toString() + "is not a valid rdf:List");
					}
				}
			}
			facetI.close();

            RepositoryResult<Statement> hidesI = conn.getStatements(focus, Facets.hides, (Value) null, false);
			if (hidesI.hasNext()) {
				Statement facetStmt = hidesI.next();
				Value facetsNode = facetStmt.getObject();
				if (hidesI.hasNext()) throw new UnresolvableException("More than one :hides value available");
				if (facetsNode instanceof Resource && RDFList.isRDFList(source, (Resource) facetsNode)) {
					RDFList facetRDFList = new RDFList(source, (Resource) facetsNode);
					if (facetRDFList.isValid()) {
						for (Iterator listI = facetRDFList.iterator(); listI.hasNext(); ) {
							Resource facetNode = (Resource) listI.next();
							Facet facet = new Facet(facetNode);
							out.addHide(facet);
						}
					} else {
						throw new ParsingException(facetsNode.toString() + "is not a valid rdf:List");
					}
				}
			}
			hidesI.close();
			conn.close();
		} catch (RepositoryException e) {
			throw new UnresolvableException("Problem connecting to repository: " + e.getLocalizedMessage());
		}

		TypeSet forTypes = TypeSet.parse(source, focus);
		out.setForTypes(forTypes);

		return out;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String state = "[FacetSet " + super.toString() + "]\n";
		state += "   " + this._forTypes.toString();
		Iterator<Facet> it = this.facetIterator();
		while (it.hasNext()) {
			Facet select = it.next();
			state += "     [Facet " + select.getIdentifier() + "]\n";
		}
		it = this.hideIterator();
		while (it.hasNext()) {
			Facet select = it.next();
			state += "     [Hide facet " + select.getIdentifier() + "]\n";
		}
		return state;
	}
}
