/*
 * Created on Mar 17, 2005
 */
package edu.mit.simile.fresnel.selection;

import java.util.HashMap;
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
import fr.inria.jfresnel.fsl.FSLHierarchyStore;
import fr.inria.jfresnel.fsl.FSLNSResolver;
import fr.inria.jfresnel.fsl.FSLPath;
import fr.inria.jfresnel.fsl.sesame.FSLSesameEvaluator;

/**
 * Selector based on a Fresnel Selector Expression, composed from
 * the Fresnel Selector Language.  canSelect(Resource) and canSelect(Value)
 * are indeterminate for this selector; with no Repository source, it's impossible
 * to tell, so false is returned by default.
 * 
 * @author ryanlee
 */
public class FSESelector implements ISelector {	
	/**
	 * The original FSE.
	 */
	private String _fse;
	
	/**
	 * The context of the FSE
	 */
	private short _use;
	
	/**
	 * Evaluates FSE's into paths
	 */
	private FSLSesameEvaluator _eval;
	
	/**
	 * Namespace resolver for path evaluator
	 */
	private FSLNSResolver _nsr;

	/**
	 * Hierarchy store path evaluator; expected to do nothing for now.
	 */
	private FSLHierarchyStore _fhs;
	
	/**
	 * Create an FSESelector based on its string representation.
	 * 
	 * @param fse The <code>String</code> representation of the FSE
	 */
	public FSESelector(String fse, short use, FSLNSResolver nsr) {
 		this._fse = fse;
		this._use = use;
		this._nsr = nsr;
		this._fhs = new FSLHierarchyStore();
		this._eval = new FSLSesameEvaluator(this._nsr, this._fhs);
	}
	
	/**
	 * This may or may not work.  If the path expression is not compatible with selecting statements
	 * based on the starting point of the selected parameter, an exception will be thrown.
	 * 
	 * @see edu.mit.simile.fresnel.selection.ISelector#selectStatements(Repository, Resource)
	 */
	public Iterator<Statement> selectStatements(Repository in, Resource selected) throws InvalidResultSetException {
		this._eval.setRepository(in);
		HashMap<Statement, Object> start = new HashMap<Statement, Object>();
		try {
			RepositoryConnection conn = in.getConnection();
			RepositoryResult<Statement> results = conn.getStatements(selected, null, null, true);
			while (results.hasNext()) {
				start.put(results.next(), null);
			}
			conn.close();
		} catch (RepositoryException e) {
			throw new InvalidResultSetException("Repository exception encountered generating starting points: " + this._fse);            
		}
		FSLPath p = FSLPath.pathFactory(this._fse, this._nsr, this._use);
        Vector<Statement> statements = new Vector<Statement>();
		try {
            Vector<Vector<Object>> paths = this._eval.evaluatePath(p, start);

            for (int i = 0; i < paths.size() ; i++ ) {
				Vector<Object> v = paths.get(i);
				statements.add((Statement) v.get(v.size()-1));
			}
		} catch (ClassCastException e) {
			throw new InvalidResultSetException("This expression does not return statements: " + this._fse);			
		}
		this._eval.unsetRepository();
		return statements.iterator();
	}
	
	/**
	 * @see edu.mit.simile.fresnel.selection.ISelector#selectResources(Repository)
	 */
	public Iterator<Resource> selectResources(Repository in) throws InvalidResultSetException {
		this._eval.setRepository(in);
		FSLPath p = FSLPath.pathFactory(this._fse, this._nsr, this._use);
		Vector<Vector<Object>> paths = this._eval.evaluatePath(p);
		Vector<Resource> resources = new Vector<Resource>();
		try {
			for (int i = 0; i < paths.size() ; i++ ) {
				Vector<Object> v = paths.get(i);
				resources.add((Resource) v.get(v.size()-1));
			}
		} catch (ClassCastException e) {
			throw new InvalidResultSetException("This expression does not return resources: " + this._fse);			
		}
		this._eval.unsetRepository();
		return resources.iterator();
	}
	
	/**
	 * @see edu.mit.simile.fresnel.selection.ISelector#selectNodes(Repository)
	 */
	public Iterator<Value> selectNodes(Repository in) throws InvalidResultSetException {
		this._eval.setRepository(in);
		FSLPath p = FSLPath.pathFactory(this._fse, this._nsr, this._use);
		Vector<Vector<Object>> paths = this._eval.evaluatePath(p);
		Vector<Value> values = new Vector<Value>();
		try {
			for (int i = 0; i < paths.size() ; i++ ) {
				Vector<Object> v = paths.get(i);
				values.add((Value) v.get(v.size()-1));
			}
		} catch (ClassCastException e) {
			throw new InvalidResultSetException("This expression does not return values: " + this._fse);			
		}
		this._eval.unsetRepository();
		return values.iterator();
	}
	
	/**
	 * @see edu.mit.simile.fresnel.selection.ISelector#canSelectStatements()
	 */
	public boolean canSelectStatements() {
		return true;
	}
	
	/**
	 * @see edu.mit.simile.fresnel.selection.ISelector#canSelectResources()
	 */
	public boolean canSelectResources() {
		return true;
	}
	
	/**
	 * @see edu.mit.simile.fresnel.selection.ISelector#canSelectNodes()
	 */
	public boolean canSelectNodes() {
		return true;
	}
	
	/**
	 * @see edu.mit.simile.fresnel.selection.ISelector#canSelect(Repository, Resource, URI)
	 */
	public boolean canSelect(Repository in, Resource selected, URI prop) {
//		if (prop.toString().equals("fsl://"+_fse))
//				return true;
		return (prop.toString().equals("fsl://"+_fse));
//		try {
//			Iterator<Statement> si = selectStatements(in, selected);
//			while (si.hasNext()) {
//				if (si.next().getPredicate().equals(prop)) return true;
//			}
//		} catch (InvalidResultSetException e) {
//			return false;
//		}
//		return false;
	}
	
	/**
	 * @see edu.mit.simile.fresnel.selection.ISelector#canSelect(Repository, Resource)
	 */
	public boolean canSelect(Repository in, Resource selected) {
		if (_fse.equals("*")) /* will iterate all resources otherwise */
			return true;
		try {
			Iterator<Resource> ri = selectResources(in);
			while (ri.hasNext()) {
				if (ri.next().equals(selected)) return true;
			}
		} catch (InvalidResultSetException e) {
			// TODO these exceptions should be logged.
			return false;
		}
		return false;
	}
	
	/**
	 * This method will always return false.
	 * 
	 * @see edu.mit.simile.fresnel.selection.ISelector#canSelect(Repository, Value)
	 */
	public boolean canSelect(Repository in, Value selected) {
		return false;		
	}

	public String get_fse() {
		return _fse;
	}
}
