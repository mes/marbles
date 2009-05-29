/*
 * Created on Mar 16, 2005
 */
package edu.mit.simile.fresnel.selection;

import java.util.Iterator;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.Repository;

/**
 * Interface for generic selection of certain pieces of a model.  This
 * query mechanism must cover several styles of selection queries even
 * though not all mechanisms can return all types of results.
 * 
 * TODO: This can't possibly be sufficient for extended vocabulary tasks - is
 * it extensible enough?
 * 
 * @author ryanlee
 */
public interface ISelector {
    /**
     * Returns triples in response to a selection query.
     * 
     * @param in Data <code>Repository</code>
     * @param selected <code>Resource</code> subject of statements
     * @return A <code>StmtIterator</code>
     */
    public Iterator<Statement> selectStatements(Repository in, Resource selected) throws InvalidResultSetException;
    
    /**
     * Evaluates whether this ISelector can select statements.
     * 
     * @return A <code>boolean</code>
     */
    public boolean canSelectStatements();
    
    /**
     * Evaluates whether this ISelector can select this specific resource-property statement.
     * 
     * @param in Data <code>Repository</code>
     * @param selected A <code>Resource</code>
     * @param prop A <code>Property</code>
     * @return True if selectable, false if not.
     */
    public boolean canSelect(Repository in, Resource selected, URI prop);
    
    /**
     * Returns a list of resources in response to a selection query.
     * 
     * @param in Data <code>Repository</code>
     * @return A <code>ResIterator</code>
     */
    public Iterator<Resource> selectResources(Repository in) throws InvalidResultSetException;

    /**
     * Evaluates whether this ISelector can select resources.
     * 
     * @return A <code>boolean</code>
     */
    public boolean canSelectResources();
    
    /**
     * Evalutes whether this ISelector can select this specific resource; semantics depend
     * on canSelectStatements / canSelectResources - if both, meaning is irrelevant; if not
     * able to select one, then indicates the other.
     * 
     * @param in The data <code>Repository</code>
     * @param selected A <code>Resource</code>
     * @return True if selectable, false if not.
     */
    public boolean canSelect(Repository in, Resource selected);

    /**
     * Returns a list of nodes in response to a selection query.
     * 
     * @param in The data <code>Repository</code>
     * @return A <code>NodeIterator</code>
     */
    public Iterator<Value> selectNodes(Repository in) throws InvalidResultSetException;
    
    /**
     * Evaluates whether this ISelector can select nodes.
     * 
     * @return A <code>boolean</code>
     */
    public boolean canSelectNodes();

    /**
     * Evalutes if this ISelector can select this precise node.
     * 
     * @param in The data <code>Repository</code>
     * @param selected An <code>RDFNode</code>
     * @return True if selectable, false if not.
     */
    public boolean canSelect(Repository in, Value selected);
}
