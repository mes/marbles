/*
 * Created on Mar 29, 2005
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
 * Selects one specific resource.
 * 
 * @author ryanlee
 */
public class InstanceSelector implements ISelector {
    /**
     * The resource this selector selects.
     */
    private Resource _resource;
    
    /**
     * Can return statements.
     */
    private boolean _statements = true;
    
    /**
     * Can return resources.
     */
    private boolean _resources = true;
    
    /**
     * Cannot return nodes.
     */
    private boolean _nodes = false;
    
    /**
     * Create a new InstanceSelector.
     * 
     * @param resource The <code>Resource</code> the instance selector selects
     */
    public InstanceSelector(Resource resource) {
        this._resource = resource;
    }
    
    /**
     * Returns the resource this instance selector is based on.
     * 
     * @return A <code>Resource</code>
     */
    public Resource getInstance() {
    		return this._resource;
    }
    
    /**
     * @see edu.mit.simile.fresnel.selection.ISelector#selectStatements(Repository, Resource)
     */
    public Iterator<Statement> selectStatements(Repository in, Resource selected) throws InvalidResultSetException {
    	Vector<Statement> results = null;
    	if (selected.equals(this._resource)) {
    		try {
    			RepositoryConnection conn = in.getConnection();
    			RepositoryResult<Statement> it  = conn.getStatements(selected, (URI) null, (Value) null, true);
    			while ( it.hasNext() ) {
    				results.add(it.next());
    			}
    			it.close();
    			
        		/* Inverse statements */
                it = conn.getStatements(null, null, selected, true);
        		while (it.hasNext()) {
        			results.add(it.next());
        		}
        		it.close();
    			
    			conn.close();
    		} catch (RepositoryException e) {
    			throw new InvalidResultSetException(e.getMessage());
    		}
    	}
    	return results.iterator();
    }

    /**
     * @see edu.mit.simile.fresnel.selection.ISelector#canSelectStatements()
     */
    public boolean canSelectStatements() {
        return this._statements;
    }

    /**
     * @see edu.mit.simile.fresnel.selection.ISelector#selectResources(Repository)
     */
    public Iterator<Resource> selectResources(Repository in) throws InvalidResultSetException {
        Vector<Resource> resources = new Vector<Resource>();
        resources.add(this._resource);
        return resources.iterator();
    }

    /**
     * @see edu.mit.simile.fresnel.selection.ISelector#canSelectResources()
     */
    public boolean canSelectResources() {
        return this._resources;
    }

    /**
     * @see edu.mit.simile.fresnel.selection.ISelector#selectNodes(Repository)
     */
    public Iterator<Value> selectNodes(Repository in) throws InvalidResultSetException {
        throw new InvalidResultSetException("");
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
		boolean stmtExists = false;
		try {
			RepositoryConnection conn = in.getConnection();
			stmtExists = conn.hasStatement(selected, prop, (Value) null, true);
			conn.close();
		} catch (RepositoryException e) {
			// TODO: how to handle exception
		}
        return (selected.equals(this._resource) && stmtExists);
	}
	
	/**
     * @see edu.mit.simile.fresnel.selection.ISelector#selectStatements(Repository, Resource)
     */
    public boolean canSelect(Repository in, Resource selected) {
		boolean stmtExists = false;
		try {
			RepositoryConnection conn = in.getConnection();
			stmtExists = conn.hasStatement(selected, (URI) null, (Value) null, true);
			conn.close();
		} catch (RepositoryException e) {
			// TODO: how to handle exception
		}
        return (selected.equals(this._resource) && stmtExists);
    }

    /**
     * @see edu.mit.simile.fresnel.selection.ISelector#canSelect(Repository, Value)
     */
    public boolean canSelect(Repository in, Value selected) {
        return this._nodes;
    }
}
