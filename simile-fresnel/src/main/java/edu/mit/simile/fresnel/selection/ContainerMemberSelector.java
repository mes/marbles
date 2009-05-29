/*
 * Created on Apr 27, 2005
 */
package edu.mit.simile.fresnel.selection;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import edu.mit.simile.fresnel.util.Container;
import edu.mit.simile.fresnel.util.RDFList;
import edu.mit.simile.vocabularies.FresnelCore;

/**
 * Only handles fresnel:member as a property selector.
 * 
 * @author ryanlee
 */
public class ContainerMemberSelector implements ISelector {
    /**
     * Can return statements.
     */
    private final boolean _statements = true;
    
    /**
     * Cannot return resources.
     */
    private final boolean _resources = false;
    
    /**
     * Cannot return nodes.
     */
    private final boolean _nodes = false;

    /**
     * Selects statements matching fresnel:member out of container and list item
     * membership statements.  Assumes containers and lists are mutually exclusive.
     * 
     * @see edu.mit.simile.fresnel.selection.ISelector#selectStatements(Repository, Resource)
     */
    public Iterator<Statement> selectStatements(Repository in, Resource selected)
            throws InvalidResultSetException {
        // put statements in a vector and create a statement iterator based off the vector iterator
        Vector<Statement> statements = new Vector<Statement>();
        URI mem = (URI) FresnelCore.member;
        if (Container.isContainer(in, selected)) {
            Container con = new Container(in, selected);
            for (Iterator it = con.iterator(); it.hasNext(); ) {
                statements.add(new StatementImpl(selected, mem, (Value) it.next()));
            }
        } else if (RDFList.isRDFList(in, selected)) {
            List list = new RDFList(in, selected).asJavaList();
            for (Iterator it = list.iterator(); it.hasNext(); ) {
                statements.add(new StatementImpl(selected, mem, (Value) it.next()));
            }
        }
        return statements.iterator();
    }

    /**
     * @see edu.mit.simile.fresnel.selection.ISelector#canSelectStatements()
     */
    public boolean canSelectStatements() {
        return this._statements;
    }

    /**
     * TODO: probably this will go unused, but it doesn't make much sense as is;
     *       if this only selects fresnel:member-ish stuff, then an arbitrary
     *       property isn't going to be selected by this selector...
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
     * @see edu.mit.simile.fresnel.selection.ISelector#selectResources(Repository)
     */
    public Iterator<Resource> selectResources(Repository in)
            throws InvalidResultSetException {
        throw new InvalidResultSetException("ContainerMemberSelector cannot select resources");
    }

    /**
     * @see edu.mit.simile.fresnel.selection.ISelector#canSelectResources()
     */
    public boolean canSelectResources() {
        return this._resources;
    }

    /**
     * @see edu.mit.simile.fresnel.selection.ISelector#canSelect(Repository, Resource)
     */
    public boolean canSelect(Repository in, Resource selected) {
        return (Container.isContainer(in, selected) || RDFList.isRDFList(in, selected));
    }

    /**
     * @see edu.mit.simile.fresnel.selection.ISelector#selectNodes(Repository)
     */
    public Iterator<Value> selectNodes(Repository in) throws InvalidResultSetException {
        throw new InvalidResultSetException("ContainerMemberSelector cannot select nodes");
    }

    /**
     * @see edu.mit.simile.fresnel.selection.ISelector#canSelectNodes()
     */
    public boolean canSelectNodes() {
        return this._nodes;
    }

    /**
     * @see edu.mit.simile.fresnel.selection.ISelector#canSelect(Repository, Value)
     */
    public boolean canSelect(Repository in, Value selected) {
        return this._nodes;
    }

}
