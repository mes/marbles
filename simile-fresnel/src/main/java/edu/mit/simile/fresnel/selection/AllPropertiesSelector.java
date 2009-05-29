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
 * Selects all properties of a given resource.
 * 
 * @author ryanlee
 */
public class AllPropertiesSelector implements ISelector {
    /**
     * Does select statements.
     */
    private boolean _statements = true;
    
    /**
     * Does not select resources.
     */
    private boolean _resources = false;
    
    /**
     * Does not select nodes.
     */
    private boolean _nodes = false;
    
    /**
     * Compares other ISelectors to the AllPropertiesSelector to check if it is
     * an AllPropertiesSelector.
     * 
     * @param selector An <code>ISelector</code> for comparison
     * @return The <code>boolean</code> comparison result
     */
    public static boolean isAllProperties(ISelector selector) {
        return selector.getClass().equals(AllPropertiesSelector.class);
    }
    
    /**
     * @see edu.mit.simile.fresnel.selection.ISelector#selectStatements(Repository, Resource)
     */
    public Iterator<Statement> selectStatements(Repository in, Resource selected)
            throws InvalidResultSetException {
    	// TODO: problematic
    	Vector<Statement> out = new Vector<Statement>();
    	try {
    		RepositoryConnection conn = in.getConnection();
            RepositoryResult<Statement> it = conn.getStatements(selected, (URI) null, (Value) null, true);
    		while (it.hasNext()) {
    			out.add(it.next());
    		}
    		it.close();
    		
    		/* Inverse statements */
            it = conn.getStatements(null, null, selected, true);
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
     * Don't use this.
     * 
     * @see edu.mit.simile.fresnel.selection.ISelector#selectResources(Repository)
     */
    public Iterator<Resource> selectResources(Repository in)
            throws InvalidResultSetException {
        throw new InvalidResultSetException("AllPropertiesSelector does not select resources");
    }

    /**
     * Don't use this.
     * 
     * @see edu.mit.simile.fresnel.selection.ISelector#selectNodes(Repository)
     */
    public Iterator<Value> selectNodes(Repository in) throws InvalidResultSetException {
        throw new InvalidResultSetException("AllPropertiesSelector does not select nodes");
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
			out = conn.hasStatement(selected, prop, (Value) null, true);
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
			out = conn.hasStatement(selected, (URI) null, (Value) null, true);
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
        String state = "   [ISelector:AllPropertiesSelector " + super.toString() + "]\n";
        return state;
    }

}
