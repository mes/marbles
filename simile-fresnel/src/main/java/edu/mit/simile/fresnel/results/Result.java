package edu.mit.simile.fresnel.results;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.mit.simile.fresnel.FresnelUtilities;
import edu.mit.simile.fresnel.configuration.Group;
import edu.mit.simile.fresnel.selection.Lens;

/**
 * Lens selection and formatting result for a resource.
 * 
 * @author ryanlee
 */
public class Result implements ResultConstants {
	/**
	 * Identifies the resource
	 */
	private Resource _origin;
	/**
	 * Label for the resource
	 */
	private Title _title;
	/**
	 * Content formatting
	 */
	private ContentSet _contents;
	/**
	 * Styling classes
	 */
	private String _styles;
	/**
	 * Lens used to select data
	 */
	private Lens _lens;
	/**
	 * Group lens belongs to
	 */
	private Group _group;
	/**
	 * Set of properties's results associated with this resource
	 */
	private PropertyResultSet _properties;
	/**
	 * Set of rdf:type values associated with this resource
	 */
	private Set<Resource> _types;
	
	/**
	 * Constructor based on resource with lens and group used to select it
	 * 
	 * @param focus The <code>Resource</code> this result is for
	 * @param group The <code>Group</code> of Fresnel configurations used to select and format it
	 * @param lens The <code>Lens</code> used to select data
	 */
	public Result(Resource focus, Group group, Lens lens, Repository in) {
		this._origin = focus;
		this._properties = new PropertyResultSet();
		this._contents = new ContentSet();
		this._lens = lens;
		this._group = group;
		this._types = new HashSet<Resource>();
		setTypes(in);
	}
	
	/**
	 * The identifying resource
	 * 
	 * @return Identifying <code>Resource</code>
	 */
	public Resource getOrigin() {
		return this._origin;
	}
	
	/**
	 * The lens used to select this resource and its data.
	 * 
	 * @return Selecting <code>Lens</code>
	 */
	public Lens getLens() {
		return this._lens;
	}
	
	/**
	 * The group the lens and subsequent formatting belongs to
	 * 
	 * @return Parent <code>Group</code>
	 */
	public Group getGroup() {
		return this._group;
	}
	
	/**
	 * A container for a human friendly label for the result
	 * 
	 * @return A <code>Title</code>
	 */
	public Title getTitle() {
		return this._title;
	}
	
	/**
	 * Retrieves the resource for this result as a URI.
	 * 
	 * @return The resource's <code>URI</code>
	 */
	public String getURI() {
		return this._origin.toString();
		// TODO: may not be a URI...what then?
/*		if ((this._origin instanceof BNode))
			return ((URI) this._origin).toString();
		else
			return ((URI) this._origin).toString();*/
	}
	
	/**
	 * Retrieves formatting for the resource.
	 * 
	 * @return A <code>ContentSet</code>
	 */
	public ContentSet getContents() {
		return this._contents;
	}
	
	/**
	 * Retrieves styling classes for the resource.
	 * 
	 * @return A <code>String</code>
	 */
	public String getStyles() {
		return this._styles;
	}
	
	/**
	 * Retrieves the properties' results associated with the resource
	 * 
	 * @return A <code>PropertyResultSet</code>
	 */
	public PropertyResultSet getProperties() {
		return this._properties;
	}
	
	/**
	 * Fetches rdf:type values for the resource
	 * 
	 * @return A <code>RdfTypeSet</code>
	 */
	public Set<Resource> getTypes() {
		return this._types;
	}
	
	/**
	 * Return type statements as a statement iterator.
	 * 
	 * @return A <code>StatementIterator</code>.
	 */
	public Iterator<Statement> getTypesStatements() {
		Vector<Statement> out = new Vector<Statement>();
		for (Resource r : getTypes()) {
			out.add(new StatementImpl(getOrigin(), RDF.TYPE, r));			
		}
		return out.iterator();
	}
	
	/**
	 * Sets rdf:type values for the resource
	 * 
	 * @param in Data <code>Repository</code>
	 */
	public void setTypes(Repository in) {
		try {
			RepositoryConnection conn = in.getConnection();
            RepositoryResult<Statement> si = conn.getStatements(getOrigin(), RDF.TYPE, null, true);
			while (si.hasNext()) {
				Statement s = si.next();
				this._types.add((Resource) FresnelUtilities.dupValue(s.getObject()));
			}
			si.close();
			conn.close();
		} catch (RepositoryException e) {
			// TODO: how to handle this exception
		}
	}
	
	/**
	 * Sets the human friendly title.
	 * 
	 * @param title A <code>Title</code>
	 */
	public void setTitle(Title title) {
		this._title = title;
	}
	
	/**
	 * Sets the style class.
	 * 
	 * @param style A <code>String</code>
	 */
	public void setStyles(String style) {
		this._styles = style;
	}
	
	/**
	 * Sets the formatting for the result.
	 * 
	 * @param contents A <code>ContentSet</code>
	 */
	public void setContents(ContentSet contents) {
		this._contents = contents;
	}
	
	/**
	 * Adds a property result to the existing set of property results
	 * 
	 * @param prop A <code>PropertyResult</code>
	 */
	public void addProperty(PropertyResult prop) {
		this._properties.addPropertyResult(prop, this);
	}
	
	/**
	 * Renders the result and everything attached to it to an element in
	 * the Fresnel XML tree output.
	 * 
	 * @param doc A <code>Document</code> for creating elements.
	 * @return An <code>Element</code> for the result, e.g.:
	 *         &lt;resource&gt;&lt;title&gt;I'm a result&lt/title&gt; ... &lt;/resource&gt;
	 */
	public Element render(Document doc) {
		Element out = doc.createElementNS(INTERMEDIATE_NS, "resource");
		out.setAttribute("class", getStyles());
		out.setAttribute("uri", getURI());
		if (getGroup().hasCssLinks()) {
			for (Iterator linkIt = getGroup().getCssLinks().iterator(); linkIt.hasNext(); ) {
				Element link = doc.createElementNS(INTERMEDIATE_NS, "link");
				link.appendChild(doc.createTextNode((String) linkIt.next()));
				out.appendChild(link);
			}
		}
		Element title = doc.createElementNS(INTERMEDIATE_NS, "title");
		if (null != getTitle())
			title.appendChild(doc.createTextNode(getTitle().getString()));
		else
			title.appendChild(doc.createTextNode("(untitled)"));
		out.appendChild(title);
		Element content = getContents().render(doc);
		if (content.hasChildNodes()) out.appendChild(content);
		for(Iterator<PropertyResult> it = getProperties().propertyResultIterator(); it.hasNext() ; ) {
			PropertyResult pr = it.next();
			Element prop = null;
			if (pr.isInModel())
				prop = pr.render(doc);
			else
				prop = ((NoSuchPropertyResult) pr).render(doc);
			if (null != prop)
				out.appendChild(prop);
		}
		return out;
	}
}
