package edu.mit.simile.fresnel.selection;

import java.util.Iterator;
import java.util.Vector;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * Selector based on a SPARQL query.
 * 
 * @author Jörg Koch
 */
public class SPARQLSelector implements ISelector {
	/**
	 * The original SPARQL query.
	 */
	private String _SPARQL;

	/**
	 * Create an SPARQL selector based on its string representation.
	 * 
	 * @param query
	 *          The <code>String</code> representation of the SPARQL query
	 */
	public SPARQLSelector(String query, String namespaces) {
		this._SPARQL = namespaces;
		this._SPARQL = this._SPARQL.concat(query);
	}

	/**
	 * Select statements for the currently selected resource. The ?instance variable is
	 * called instance as a convention and is replaced by the currently selected resource.
	 * 
	 * @see edu.mit.simile.fresnel.selection.ISelector#selectStatements(Repository, Resource)
	 */
	public Iterator<Statement> selectStatements(Repository in, Resource selected) throws InvalidResultSetException {
		Vector<Statement> statements = new Vector<Statement>();
		String filteredQuery;
		try {
			RepositoryConnection conn = in.getConnection();

			if (selected instanceof BNode) {
				filteredQuery = _SPARQL.replace("?instance", "_:" + selected.toString());
			} else {
				filteredQuery = _SPARQL.replace("?instance", "<" + selected.toString() + ">");
			}

			GraphQueryResult graphResult = conn.prepareGraphQuery(QueryLanguage.SPARQL, filteredQuery).evaluate();
			while (graphResult.hasNext()) {
				statements.add(graphResult.next());
			}
			graphResult.close();
			conn.close();
		} catch (RepositoryException e) {
			throw new InvalidResultSetException("Repository exception encountered selecting statements", e);
		} catch (MalformedQueryException e) {
			throw new InvalidResultSetException("Bad query encountered selecting statements using " + this._SPARQL, e);
		} catch (QueryEvaluationException e) {
			throw new InvalidResultSetException("Query evaluation exception encountered selecting statements using " + this._SPARQL, e);
		}

		return statements.iterator();
	}

	/**
	 * @see edu.mit.simile.fresnel.selection.ISelector#canSelectStatements()
	 */
	public boolean canSelectStatements() {
		return true;
	}

	/**
	 * @see edu.mit.simile.fresnel.selection.ISelector#canSelect(Repository, Resource)
	 */
	public boolean canSelect(Repository in, Resource selected) {
		try {
			Iterator<Resource> ri = selectResources(in);
			while (ri.hasNext()) {
				if (ri.next().equals(selected))
					return true;
			}
		} catch (InvalidResultSetException e) {
			// TODO these exceptions should be logged.
			return false;
		}
		return false;
	}

	/**
	 * @see edu.mit.simile.fresnel.selection.ISelector#canSelect(Repository, Resource, URI)
	 */
	public boolean canSelect(Repository in, Resource selected, URI prop) {
		try {
			Iterator<Statement> si = selectStatements(in, selected);
			while (si.hasNext()) {
				if (si.next().getPredicate().equals(prop))
					return true;
			}
		} catch (InvalidResultSetException e) {
			// TODO these exceptions should be logged.
			return false;
		}
		return false;
	}

	/**
	 * SPARQL queries used within Fresnel must always return exactly one node
	 * set, meaning that only one variable is allowed in the query's SELECT clause.
	 * 
	 * @see edu.mit.simile.fresnel.selection.ISelector#selectResources(Repository)
	 */
	public Iterator<Resource> selectResources(Repository in) throws InvalidResultSetException {
		Vector<Resource> resources = new Vector<Resource>();
		RepositoryConnection conn;
		try {
			conn = in.getConnection();

			TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, this._SPARQL);
			TupleQueryResult result = tupleQuery.evaluate();

			String firstBindingName = result.getBindingNames().get(0);

			while (result.hasNext()) {
				Value uri = result.next().getBinding(firstBindingName).getValue();
				if (uri instanceof URI) {
					resources.add((Resource) uri);
				}
			}

			result.close();
			conn.close();
		} catch (RepositoryException e) {
			throw new InvalidResultSetException("Repository exception encountered selecting resources", e);
		} catch (MalformedQueryException e) {
			throw new InvalidResultSetException("Bad query encountered selecting resources using " + this._SPARQL, e);
		} catch (QueryEvaluationException e) {
			throw new InvalidResultSetException("Query evaluation exception encountered selecting resources using " + this._SPARQL, e);
		}

		return resources.iterator();
	}

	/**
	 * @see edu.mit.simile.fresnel.selection.ISelector#canSelectResources()
	 */
	public boolean canSelectResources() {
		return true;
	}

	/**
	 * @see edu.mit.simile.fresnel.selection.ISelector#selectNodes(Repository)
	 */
	public Iterator<Value> selectNodes(Repository in) throws InvalidResultSetException {
		Vector<Value> values = new Vector<Value>();
		RepositoryConnection conn;
		try {
			conn = in.getConnection();

			TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, this._SPARQL);
			TupleQueryResult result = tupleQuery.evaluate();

			String firstBindingName = result.getBindingNames().get(0);

			while (result.hasNext()) {
				Value uri = result.next().getBinding(firstBindingName).getValue();
				if (uri instanceof URI) {
					values.add(uri);
				}
			}

			result.close();
			conn.close();
		} catch (RepositoryException e) {
			throw new InvalidResultSetException("Repository exception encountered selecting nodes", e);
		} catch (MalformedQueryException e) {
			throw new InvalidResultSetException("Bad query encountered selecting nodes using " + this._SPARQL, e);
		} catch (QueryEvaluationException e) {
			throw new InvalidResultSetException("Query evaluation exception encountered selecting nodes using " + this._SPARQL, e);
		}

		return values.iterator();
	}

	/**
	 * @see edu.mit.simile.fresnel.selection.ISelector#canSelectNodes()
	 */
	public boolean canSelectNodes() {
		return true;
	}

	/**
	 * This method will always return false.
	 * 
	 * @see edu.mit.simile.fresnel.selection.ISelector#canSelect(Repository, Value)
	 */
	public boolean canSelect(Repository in, Value selected) {
		return false;
	}

}
