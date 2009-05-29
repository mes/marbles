/*
 *   Copyright (c) 2009, MediaEvent Services GmbH & Co. KG
 *   http://mediaeventservices.com
 *   
 *   This file is part of Marbles.
 *
 *   Marbles is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Marbles is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Marbles.  If not, see <http://www.gnu.org/licenses/>.
 *   
 */
package de.fuberlin.wiwiss.marbles;

import info.aduna.iteration.CloseableIteration;

import java.util.Iterator;

import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailConnectionListener;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailWrapper;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.sail.inferencer.InferencerConnectionWrapper;

/**
 * Mirrors statements among URI aliases that are linked by <code>owl:sameAs</code> triples.
 * 
 * In automatic mode (<code>setAutoInference(true)</code>; default), new statements are processed as they are added:
 * New statements with owl:sameAs predicates cause statements pertaining to
 * the two resources (i.e. subject and object) to be applied to one another.
 * New triples with predicates other than owl:sameAs initiate a lookup for URI Aliases, to
 * whom the statement will then be applied.
 * 
 * In manual mode (<code>setAutoInference(false)</code>), the method <code>addInferredForResource</code>
 * may be used to selectively initiate inferencing for a given resource. 
 * 
 * Note: Because the inferencer does not implement handling for the removal of triples, 
 * inferred statements are not removed.
 * 
 * @author Christian Becker
 */
public class SameAsInferencer extends SailWrapper {
	
	private Graph newStatements;
	private InferencerConnection con;
	
	private Resource[] contexts = new Resource[]{};

	/**
	 * Constructor
	 * @param baseSail
	 */
	public SameAsInferencer(Sail baseSail) {
		super(baseSail);
	}

	/**
	 * Limits the generation of inferred statements to specific contexts 
	 * 
	 * @param context	List of contexts. An empty list may be passed to permit all contexts.
	 * @return 
	 */
	public void setContext(Resource ... context) {
		this.contexts = context; 
	}
	
	@Override
	public SailConnection getConnection()
		throws SailException {
		try {
			InferencerConnection con = (InferencerConnection)super.getConnection();
			this.con = new SameAsInferencerConnection(con);
			return this.con;
		}
		catch (ClassCastException e) {
			throw new SailException(e);
		}
	}
	
	public boolean hasNewStatements() {
		return newStatements != null && !newStatements.isEmpty();
	}
	
	public boolean doInferencing(InferencerConnection con)
	throws SailException {
		if (!hasNewStatements()) {
			/* There's nothing to do */
			return false;
		}
		
		Graph iterationStatements = newStatements;
		newStatements = new GraphImpl();
		
		Iterator<Statement> iter = iterationStatements.iterator();
		while (iter.hasNext()) {
			Statement s = iter.next();
			if (s.getPredicate().equals(OWL.SAMEAS) && s.getObject() instanceof Resource)
				processSameAs(con, s.getSubject(), (Resource) s.getObject());
		    else
				processRegularStatement(con, s);
		}
		return true;
	}
	
	/**
	 * Manually initiates inferencing for a given resource
	 *  
	 * @param base	The resource for which aliases are to be found
	 * @throws SailException
	 * @throws RepositoryException
	 */
	public void addInferredForResource(Resource base) throws SailException, RepositoryException {
		/* Find alias resources */
		SailConnection userConn = this.getConnection();
		CloseableIteration<? extends Statement, SailException> iterA = userConn.getStatements(base, OWL.SAMEAS, null, false, contexts);
	    CloseableIteration<? extends Statement, SailException> iterB = userConn.getStatements(null, OWL.SAMEAS, base, false, contexts);

		while (iterA.hasNext() || iterB.hasNext()) {
			boolean isA = iterA.hasNext();
			Statement st = isA ? iterA.next() : iterB.next();
			Value obj = isA ? st.getObject() : st.getSubject();
	      
			if (obj instanceof Resource) {
				processSameAs((InferencerConnection) userConn, base, (Resource) obj);
			}
		}
		userConn.commit();
		userConn.close();
	}
	
	/**
	 * Processes an <code>owl:sameAs</code> statement
	 * 
	 * @param con
	 * @param base
	 * @param alias
	 * @throws SailException
	 */
	public void processSameAs(InferencerConnection con, Resource base, Resource alias) throws SailException {
			/* Add inferred statements for all statements where base or alias are subject */ 
    	    CloseableIteration<? extends Statement, SailException> iterA = con.getStatements(base, null, null, false /* don't include inferred statements ! */, contexts);
       	    CloseableIteration<? extends Statement, SailException> iterB = con.getStatements(alias, null, null, false /* don't include inferred statements ! */, contexts);
       	    	    
			while (iterA.hasNext() || iterB.hasNext()) {
				boolean isA = iterA.hasNext();
				Statement resSt = isA ? iterA.next() : iterB.next();
				
		    	/* Add as statements of the base resource */
				Resource subject = isA ? alias : base;
				
				/* Prevent self-reference */ 
				if (!(resSt.getPredicate().equals(OWL.SAMEAS) && subject.equals(resSt.getObject())))
					con.addInferredStatement(subject, resSt.getPredicate(), resSt.getObject(), resSt.getContext());
				else
					con.addInferredStatement(subject, OWL.SAMEAS, isA ? base : alias, resSt.getContext()); 	/* instead, mirror owl:sameAs */
			}

			/* Add inferred statements for all statements where base or alias are object */ 
    	    CloseableIteration<? extends Statement, SailException> iterC = con.getStatements(null, null, base, false /* don't include inferred statements ! */, contexts);
       	    CloseableIteration<? extends Statement, SailException> iterD = con.getStatements(null, null, alias, false /* don't include inferred statements ! */, contexts);
       	    	    
			while (iterC.hasNext() || iterD.hasNext()) {
				boolean isC = iterC.hasNext();
				Statement resSt = isC ? iterC.next() : iterD.next();
				
		    	/* Add as statements of the base resource */
				Resource obj = isC ? alias : base;
				
				/* Prevent self-reference */ 
				if (!(resSt.getPredicate().equals(OWL.SAMEAS) && obj.equals(resSt.getSubject())))
					con.addInferredStatement(resSt.getSubject(), resSt.getPredicate(), obj, resSt.getContext());
				else
					con.addInferredStatement(isC ? base : alias, OWL.SAMEAS, obj, resSt.getContext()); 	/* instead, mirror owl:sameAs */
			}
	}
	
	/**
	 * Processes statements other than <code>owl:sameAs</code>
	 *  
	 * @param con
	 * @param st
	 * @throws SailException
	 */
	public void processRegularStatement(InferencerConnection con, Statement st) throws SailException {
    	/* Find aliases for the subject */ 
	    CloseableIteration<? extends Statement, SailException> iterA = con.getStatements(st.getSubject(), OWL.SAMEAS, null, false /* don't include inferred statements ! */, contexts);
	    CloseableIteration<? extends Statement, SailException> iterB = con.getStatements(null, OWL.SAMEAS, st.getSubject(), false /* don't include inferred statements ! */, contexts);
	    
	    while (iterA.hasNext() || iterB.hasNext()) {
			boolean isA = iterA.hasNext();
			Statement resSt = isA ? iterA.next() : iterB.next();
			Value obj = isA ? resSt.getObject() : resSt.getSubject();
		      
	    	/* Add as statements of the base resource */
			if (obj instanceof Resource) {
				/* Prevent self-reference */
				if (!(st.getPredicate().equals(OWL.SAMEAS) && ((Resource)obj).equals(st.getObject())))
					con.addInferredStatement((Resource)obj, st.getPredicate(), st.getObject(), st.getContext());
				else
					con.addInferredStatement((Resource)obj, OWL.SAMEAS, isA ? resSt.getSubject() : resSt.getObject(), st.getContext()); 	/* instead, mirror owl:sameAs */
			}
		}
	    
	    if (!(st.getObject() instanceof Resource))
	    	return;
	    
    	/* Find aliases for the object */ 
	    CloseableIteration<? extends Statement, SailException> iterC = con.getStatements(null, OWL.SAMEAS, st.getObject(), false /* don't include inferred statements ! */, contexts);
	    CloseableIteration<? extends Statement, SailException> iterD = con.getStatements((Resource) st.getObject(), OWL.SAMEAS, null, false /* don't include inferred statements ! */, contexts);
	    
	    while (iterC.hasNext() || iterD.hasNext()) {
			boolean isC = iterC.hasNext();
			Statement resSt = isC ? iterC.next() : iterD.next();
			Resource subj = isC ? resSt.getSubject() : (Resource)resSt.getObject();
		      
	    	/* Add as statements of the base resource */
			/* Prevent self-reference */
			if (!(st.getPredicate().equals(OWL.SAMEAS) && subj.equals(st.getObject())))
				con.addInferredStatement(st.getSubject(), st.getPredicate(), subj, st.getContext());
			else
				con.addInferredStatement(isC ? (Resource)resSt.getObject() : resSt.getSubject(), OWL.SAMEAS, subj, st.getContext()); 	/* instead, mirror owl:sameAs */
		}	    
	}
	
	boolean autoInference = true;
	
	public void setAutoInference(boolean autoInference) {
		this.autoInference = autoInference;
	}
	
	private class SameAsInferencerConnection extends InferencerConnectionWrapper implements SailConnectionListener {
		
		public SameAsInferencerConnection(InferencerConnection con) {
			super(con);
			con.addConnectionListener(this);
		}
			
		@Override
		public void commit() throws SailException {
			super.commit();
			if (doInferencing(this))
				super.commit();
		}
		
		public void statementAdded(Statement st) {
			if (!autoInference)
				return;

			if (contexts.length > 0) {
				boolean found = false;
				for (Resource context : contexts) {
					if (st.getContext().equals(context)) {
						found = true;
						break;
					}
				}
				if (!found)
					return;
			}
			
			if (newStatements == null) {
				newStatements = new GraphImpl();
			}
			
			newStatements.add(st);
		}
		
		/**
		 * Not implemented
		 */
		public void statementRemoved(Statement st) {
		}
	} // end inner class
}
