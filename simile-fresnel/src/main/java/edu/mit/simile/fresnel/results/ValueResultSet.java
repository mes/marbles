package edu.mit.simile.fresnel.results;

import info.aduna.iteration.Iterations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.mit.simile.fresnel.util.WrappedVector;

/**
 * Set of ValueResults.
 * 
 * @author ryanlee
 */
public class ValueResultSet extends WrappedVector<ValueResult> implements ResultConstants {
	/**
	 * Contents which may apply to the overall set of values.
	 */
	private ContentSet _contents;
	
	/**
	 * Base constructor
	 */
	public ValueResultSet() {
		super();
		this._contents = new ContentSet();
	}
	
	/**
	 * Returns the set of possible configured contents
	 * 
	 * @return A <code>ContentSet</code>
	 */
	public ContentSet getContents() {
		return this._contents;
	}
	
	/**
	 * Sets the contents for the value result set
	 * 
	 * @param contents A <code>ContentSet</code>
	 */
	public void setContents(ContentSet contents) {
		this._contents = contents;
	}
	
	/**
	 * Returns a ValueResultIterator instead of a normal iterator.
	 * 
	 * @return A <code>ValueResultIterator</code>
	 */
	public Iterator<ValueResult> valueResultIterator() {
		return this._base.iterator();
	}
	
	/**
	 * Adds a value result to the existing set.
	 * 
	 * @param value A <code>ValueResult</code>
	 * @param in    Data repository
	 * @return Success or failure
	 */
	public boolean addValueResult(ValueResult value, Repository in) {
		if (value.getResult() != null) {
			/* Value is a resource */
			RepositoryConnection conn = null;
			try {
				conn = in.getConnection();
				
				for (ValueResult existingValue : this._base) {
					if (existingValue.getResult() == null)
						continue;
					
					/* If the same value already exists, don't add it but just provide the origin */
					if (existingValue.getResult().getOrigin().equals(value.getResult().getOrigin())) {
						existingValue.addSources(value.getSources());
						return true;
					}
					
					/* If the value is owl:sameAs, don't add it but provide an alias */
					if (existingValue.getPossibleAliases().contains(value.getResult().getOrigin())) {
						existingValue.addAlias(value.getResult().getOrigin());
						return true;
					}
				}	
				
				/* Didn't return, so value will be added. Enhance it with possible aliases. */ 
				RepositoryResult<Statement> result = conn.getStatements(value.getResult().getOrigin(), OWL.SAMEAS, null, true);
				RepositoryResult<Statement> inverseResult = conn.getStatements(null, OWL.SAMEAS, value.getResult().getOrigin(), true);
				
				while (result.hasNext() || inverseResult.hasNext()) {
					boolean regular = result.hasNext();
					Statement st = (regular ? result.next() : inverseResult.next());
					value.addPossibleAlias(regular ? (Resource)st.getObject() : st.getSubject());
				}
			}
			catch (RepositoryException e) {
				e.printStackTrace();
			}
			finally {
				try {
					if (conn != null)
							conn.close();
				}
				catch (RepositoryException e) {
					e.printStackTrace();
				}
			}
			

		}
		else {
			/* It's a literal. If the same value already exists, don't add it but just provide the origin */
			for (ValueResult existingValue : this._base) {
				if ((existingValue.getValue() != null && value.getValue() != null && existingValue.getValue().equals(value.getValue()))
						|| (existingValue.getTitle() != null && value.getTitle() != null && existingValue.getTitle().getString().equals(value.getTitle().getString()))) {
					existingValue.addSources(value.getSources());
					return true;
				}
			}
		}
		
		return this._base.add(value);
	}
	
	/**
	 * Adds a value result to the existing set.
	 * 
	 * @param value A <code>ValueResult</code>
	 * @return Success or failure
	 */
	public boolean addValueResult(ValueResult value) {
		return addValueResult(value, null);
	}
	
	/**
	 * Removes a value result from the existing set.
	 * 
	 * @param value A <code>ValueResult</code>
	 * @return Success or failure
	 */
	public boolean removeValueResult(ValueResult value) {
		return this._base.remove(value);
	}
	
	/**
	 * Add all elements from a value result set into this one.
	 * 
	 * @param arg0 A <code>ValueResultSet</code>
	 * @return Success or failure
	 */
	public boolean addValueResultSet(ValueResultSet arg0) {
		return this._base.addAll(arg0._base);
	}
	
	/**
	 * Replace WrappedVector contains method to do specific <code>ValueResult</code>
	 * equality checking.
	 * 
	 * @param value The <code>ValueResult</code> component to check
	 * @return True if contained in set, false if not.
	 */
	public boolean contains(ValueResult value) {
		for (Iterator<ValueResult> it = this.valueResultIterator(); it.hasNext(); ) {
			if (value.equals(it.next())) return true;
		}
		return false;
	}
	
	/**
	 * Render the set as part of a DOM.
	 * 
	 * @param doc A <code>Document</code> for creating elements.
	 * @return An <code>Element</code>, e.g.: &lt;values&gt; ... &lt;/values&gt;
	 */
	public Element render(Document doc) {
		Element out = doc.createElementNS(INTERMEDIATE_NS, "values");
		Element content = getContents().render(doc); 
		if (content.hasChildNodes()) out.appendChild(content);
		for(Iterator<ValueResult> it = valueResultIterator(); it.hasNext(); ) {
			out.appendChild(it.next().render(doc));
		}
		return out;
	}
}
