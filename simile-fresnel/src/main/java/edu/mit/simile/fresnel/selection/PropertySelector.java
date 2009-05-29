/*
 * Created on Mar 17, 2005
 */
package edu.mit.simile.fresnel.selection;

import java.util.Iterator;
import java.util.Vector;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

/**
 * Selects statements from a graph based on matching the property.
 * 
 * @author ryanlee
 */
public class PropertySelector implements ISelector {  
    /**
     * The resource (a rdf:Property) to select on.
     */
    private URI _property;
    
    /**
     * Does return statements.
     */
    private boolean _statements = true;
    
    /**
     * Doesn't return resources.
     */
    private boolean _resources = false;
    
    /**
     * Doesn't return nodes.
     */
    private boolean _nodes = false;
    
    /**
     * Constructor with an rdf:Property.
     * 
     * @param prop A property <code>URI</code>
     */
    public PropertySelector(URI prop) {
        this._property = prop;
    }
    
    /**
     * Retrieves the property this selector is based on.
     * 
     * @return A property <code>URI</code>
     */
    public URI getProperty() {
        return this._property;
    }
    
    /**
     * @see edu.mit.simile.fresnel.selection.ISelector#selectStatements(Repository, Resource)
     */
    public Iterator<Statement> selectStatements(Repository in, Resource selected)
            throws InvalidResultSetException {
    	// extremely inefficient, but i'm not certain it's kosher to pass on a CloseableIterator
    	// with the connection unclosed; or with the connection closed - it seems they should only
    	// be opened/used in instances where the connection can also be closed in the same block;
    	// so TODO: figure out whether a ClosableIterator can be passed around or whether this
    	// reading of the iterator's contents to make another iterator is basically the only choice
		Vector<Statement> out = new Vector<Statement>();
    	try {
    		RepositoryConnection conn = in.getConnection();
            RepositoryResult<Statement> it = conn.getStatements(selected, getProperty(), (Value) null, true);
    		while (it.hasNext()) {
    			out.add(it.next());
    		}
    		it.close();
    		
    		/* Inverse statements */
            it = conn.getStatements(null, getProperty(), selected, true);
    		while (it.hasNext()) {
    			out.add(it.next());
    		}
    		it.close();
    		conn.close();
    	} catch (RepositoryException e) {
    		throw new InvalidResultSetException("Problem connecting to repository: " + e.getLocalizedMessage());
    	}
    	return out.iterator();
    }

    /**
     * Do not use this.
     * 
     * @see edu.mit.simile.fresnel.selection.ISelector#selectResources(Repository)
     */
    public Iterator<Resource> selectResources(Repository in)
            throws InvalidResultSetException {
        throw new InvalidResultSetException("PropertySelector cannot be used to select resources");
    }

    /**
     * Do not use this.
     * 
     * @see edu.mit.simile.fresnel.selection.ISelector#selectNodes(Repository)
     */
    public Iterator<Value> selectNodes(Repository in) throws InvalidResultSetException {
        throw new InvalidResultSetException("PropertySelector cannot be used to select object nodes");
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
		boolean out = false;
		try {
			RepositoryConnection conn = in.getConnection();
			out = prop.equals(getProperty()) && 
				(conn.hasStatement(selected, prop, (Value) null, true)
						|| conn.hasStatement(null, prop, selected, true));
			conn.close();
		} catch (RepositoryException e) {
			// TODO: how to handle exception
		}
        return out;
	}
	
    /**
     * @see edu.mit.simile.fresnel.selection.ISelector#canSelect(Repository, Resource)
     */
    public boolean canSelect(Repository in, Resource selected) {
		boolean out = false;
		try {
			RepositoryConnection conn = in.getConnection();
			out = conn.hasStatement(selected, getProperty(), (Value) null, true)
					|| conn.hasStatement(null, getProperty(), selected, true);
			conn.close();
		} catch (RepositoryException e) {
			// TODO: how to handle exception
		}
        return out;
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
        String state = "   [ISelector:PropertySelector " + super.toString() + "]\n";
        state += "   " + this._property + "\n";
        return state;
    }
}
