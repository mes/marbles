/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.util;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;

/**
 * An RDFHandler that adds RDF data to a repository.
 * 
 * @author jeen
 */
public class RDFInserter extends RDFHandlerBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The connection to use for the add operations.
	 */
	private RepositoryConnection con;

	/**
	 * The contexts to add the statements to. If this variable is a non-empty
	 * array, statements will be added to the corresponding contexts.
	 */
	private Resource[] contexts = new Resource[0];

	/**
	 * Flag indicating whether blank node IDs should be preserved.
	 */
	private boolean preserveBNodeIDs;

	/**
	 * Map that stores namespaces that are reported during the evaluation of the
	 * query. Key is the namespace prefix, value is the namespace name.
	 */
	private Map<String, String> namespaceMap;

	/**
	 * Map used to keep track of which blank node IDs have been mapped to which
	 * BNode object in case preserveBNodeIDs is false.
	 */
	private Map<String, BNode> bNodesMap;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new RDFInserter object that preserves bnode IDs and that does
	 * not enforce any context upon statements that are reported to it.
	 * 
	 * @param con
	 *        The connection to use for the add operations.
	 */
	public RDFInserter(RepositoryConnection con) {
		this.con = con;
		preserveBNodeIDs = true;
		namespaceMap = new HashMap<String, String>();
		bNodesMap = new HashMap<String, BNode>();
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Sets whether this RDFInserter should preserve blank node IDs.
	 * 
	 * @param preserveBNodeIDs
	 *        The new value for this flag.
	 */
	public void setPreserveBNodeIDs(boolean preserveBNodeIDs) {
		this.preserveBNodeIDs = preserveBNodeIDs;
	}

	/**
	 * Checks whether this RDFInserter preserves blank node IDs.
	 */
	public boolean preservesBNodeIDs() {
		return preserveBNodeIDs;
	}

	/**
	 * Enforces the supplied contexts upon all statements that are reported to
	 * this RDFInserter.
	 * 
	 * @param contexts
	 *        the contexts to use. Use an empty array (not null!) to indicate no
	 *        context(s) should be enforced.
	 */
	public void enforceContext(Resource... contexts) {
		OpenRDFUtil.verifyContextNotNull(contexts);
		this.contexts = contexts;
	}

	/**
	 * Checks whether this RDFInserter enforces its contexts upon all statements
	 * that are reported to it.
	 * 
	 * @return <tt>true</tt> if it enforces its contexts, <tt>false</tt>
	 *         otherwise.
	 */
	public boolean enforcesContext() {
		return contexts.length != 0;
	}

	/**
	 * Gets the contexts that this RDFInserter enforces upon all statements that
	 * are reported to it (in case <tt>enforcesContext()</tt> returns
	 * <tt>true</tt>).
	 * 
	 * @return A Resource[] identifying the contexts, or <tt>null</tt> if no
	 *         contexts is enforced.
	 */
	public Resource[] getContexts() {
		return contexts;
	}

	@Override
	public void endRDF()
		throws RDFHandlerException
	{
		for (Map.Entry<String, String> entry : namespaceMap.entrySet()) {
			String prefix = entry.getKey();
			String name = entry.getValue();

			try {
				if (con.getNamespace(prefix) == null) {
					con.setNamespace(prefix, name);
				}
			}
			catch (RepositoryException e) {
				throw new RDFHandlerException(e);
			}
		}

		namespaceMap.clear();
		bNodesMap.clear();
	}

	@Override
	public void handleNamespace(String prefix, String name) {
		// FIXME: set namespaces directly when they are properly handled wrt
		// rollback
		// don't replace earlier declarations
		if (prefix != null && prefix.trim().length() > 0 && !namespaceMap.containsKey(prefix)) {
			namespaceMap.put(prefix, name);
		}
	}

	@Override
	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		Resource subj = st.getSubject();
		URI pred = st.getPredicate();
		Value obj = st.getObject();
		Resource ctxt = st.getContext();

		if (!preserveBNodeIDs) {
			if (subj instanceof BNode) {
				subj = mapBNode((BNode)subj);
			}

			if (obj instanceof BNode) {
				obj = mapBNode((BNode)obj);
			}

			if (!enforcesContext() && ctxt instanceof BNode) {
				ctxt = mapBNode((BNode)ctxt);
			}
		}

		try {
			if (enforcesContext()) {
				con.add(subj, pred, obj, contexts);
			}
			else {
				con.add(subj, pred, obj, ctxt);
			}
		}
		catch (RepositoryException e) {
			throw new RDFHandlerException(e);
		}
	}

	/**
	 * Maps the supplied BNode, which comes from the data, to a new BNode object.
	 * Consecutive calls with equal BNode objects returns the same object
	 * everytime.
	 * 
	 * @throws RepositoryException
	 */
	private BNode mapBNode(BNode bNode) {
		BNode result = bNodesMap.get(bNode.getID());

		if (result == null) {
			result = con.getRepository().getValueFactory().createBNode();
			bNodesMap.put(bNode.getID(), result);
		}

		return result;
	}
}
