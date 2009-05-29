/*
 * Created on Mar 16, 2005
 */
package edu.mit.simile.fresnel.selection;

import info.aduna.collections.iterators.CloseableIterator;

import java.util.Iterator;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import edu.mit.simile.fresnel.FresnelUtilities;
import edu.mit.simile.fresnel.util.Container;
import edu.mit.simile.fresnel.util.RDFList;


/**
 * Selects resources based on their rdf:type.
 * 
 * @author ryanlee
 */
public class TypeSelector implements ISelector {
	/**
	 * The internal rdfs:Resource representing the desired rdf:type.
	 */
	private Resource _type;
	
	/**
	 * Do not use for statements.
	 */
	private final boolean _statements = false;
	
	/**
	 * Use for resources.
	 */
	private final boolean _resources = true;
	
	/**
	 * Do not use for nodes.
	 */
	private final boolean _nodes = false;
	
	/**
	 * Build a TypeSelector from an object of rdf:type.
	 * 
	 * @param type A <code>Resource</code> representing a schema class
	 */
	public TypeSelector(Resource type) {
		this._type = type;
	}
	
	/**
	 * Returns the type this selector is based on.
	 * 
	 * @return A <code>Resource</code>
	 */
	public Resource getType() {
		return this._type;
	}

	/**
	 * Don't use this.
	 * 
	 * @see edu.mit.simile.fresnel.selection.ISelector#selectStatements(Repository, Resource)
	 */
	public CloseableIterator<Statement> selectStatements(Repository in, Resource selected)
	throws InvalidResultSetException {
		throw new InvalidResultSetException("TypeSelector does not select statements");
	}
	
	/**
	 * @see edu.mit.simile.fresnel.selection.ISelector#selectResources(Repository)
	 */
	public Iterator<Resource> selectResources(Repository in)
	throws InvalidResultSetException {
		return FresnelUtilities.listSubjectsWithProperty(in, RDF.TYPE, this._type);
	}
	
	/**
	 * Don't use this.
	 * 
	 * @see edu.mit.simile.fresnel.selection.ISelector#selectNodes(Repository)
	 */
	public Iterator<Value> selectNodes(Repository in) throws InvalidResultSetException {
		throw new InvalidResultSetException("TypeSelector does not select nodes");
	}
	
	/**
	 * @see edu.mit.simile.fresnel.selection.ISelector#canSelectStatements()
	 */
	public boolean canSelectStatements() {
		return this._statements;
	}
	
	/**
	 * @see edu.mit.simile.fresnel.selection.ISelector#canSelectResources()
	 */
	public boolean canSelectResources() {
		return this._resources;
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
		return this._statements;
	}
	
	/**
	 * @see edu.mit.simile.fresnel.selection.ISelector#canSelect(Repository, Resource)
	 */
	public boolean canSelect(Repository in, Resource selected) {
		boolean stmtExists = false;
		try {
			RepositoryConnection conn = in.getConnection();
			stmtExists = conn.hasStatement(selected, RDF.TYPE, this._type, true);
			conn.close();
		} catch (RepositoryException e) {
			// TODO: how to handle exception
		}
		return ((this._type.equals(RDF.LIST) && RDFList.isRDFList(in, selected)) ||
				((this._type.equals(RDF.ALT) || this._type.equals(RDF.BAG) || this._type.equals(RDF.SEQ)) && Container.isContainer(in, selected)) ||
				stmtExists);
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
		String state = "   [ISelector:TypeSelector " + super.toString() + "]\n";
		state += "   " + this._type + "\n";
		return state;
	}
}
