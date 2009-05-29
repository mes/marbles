package edu.mit.simile.fresnel.results;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Statement object results as selected and formatted by Fresnel lenses and formats.
 * 
 * @author ryanlee
 */
public class ValueResult implements ResultConstants {
	/**
	 * If the object is a resource, the corresponding result
	 */
	private Result _base;
	/**
	 * If the object is a literal, its value
	 */
	private Literal _value;
	/**
	 * A human-friendly title for the object
	 */
	private Title _title;
	/**
	 * How to render the output
	 */
	private String _kind;
	/**
	 * Styling for the object
	 */
	private String _styles;
	/**
	 * The property that points to this object
	 */
	private PropertyResult _parent;
	/**
	 * Source URIs for the value's triple
	 */	
	private List<Resource> _sources;
	/**
	 * Alias URIs that were added (if the object is a resource)
	 */	
	private List<Resource> _aliases;
	/**
	 * All possible alias URIs
	 */	
	private List<Resource> _possibleAliases;
	
	private void init() {
		this._base = null;
		this._value = null;
		this._parent = null;
		this._sources = new ArrayList<Resource>();
		this._aliases = new ArrayList<Resource>();
		this._possibleAliases = new ArrayList<Resource>();		
	}
	
	/**
	 * Constructor if the value is a resource
	 * 
	 * @param base Resource <code>Result</code>
	 * @param parent Parent <code>PropertyResult</code>
	 */
	public ValueResult(Result base, PropertyResult parent, Resource source) {
		init();
		this._base = base;
		this._parent = parent;
		addSource(source);
	}	
	
	/**
	 * Constructor if the value is a literal
	 * 
	 * @param lit Value <code>Literal</code>
	 * @param parent Parent <code>PropertyResult</code>
	 */
	public ValueResult(Literal lit, PropertyResult parent, Resource source) {
		init();
		this._value = lit;
		this._title = new Title(lit.getLabel());
		this._parent = parent;
		addSource(source);
	}

	/**
	 * Constructor if the value is a literal given as a string
	 * 
	 * @param lit Value <code>String</code>
	 * @param parent Parent <code>PropertyResult</code>
	 */
	public ValueResult(String lit, PropertyResult parent, Resource source) {
		init();
		this._title = new Title(lit);
		this._parent = parent;
		addSource(source);
	}
	
	/**
	 * Retrieves the parent property pointing to this value
	 * 
	 * @return A <code>PropertyResult</code>
	 */
	public PropertyResult getParent() {
		return this._parent;
	}
	
	/**
	 * Whether the value is a resource or not.
	 * 
	 * @return True if a resource, false if a literal
	 */
	public boolean isResource() {
		return (null != this._base);
	}
	
	/**
	 * Retrieves the resource's result if the value is a resource
	 * 
	 * @return A <code>Result</code>
	 */
	public Result getResult() {
		return this._base;
	}
	
	/**
	 * Retrieves the literal if the value is a literal
	 * 
	 * @return A <code>Literal</code>
	 */
	public Literal getValue() {
		return this._value;
	}
	
	/**
	 * Retrieves the title of the value
	 * 
	 * @return A <code>Title</code>
	 */
	public Title getTitle() {
		return this._title;
	}
	
	/**
	 * Retrieves style classes for the value
	 * 
	 * @return A <code>String</code>
	 */
	public String getStyles() {
		return this._styles;
	}
	
	/**
	 * Retrieves the display type for the value
	 * 
	 * @return A <code>String</code> representation
	 */
	public String getOutputType() {
		return this._kind;
	}
	
	/**
	 * Retrieves the sources for the value
	 * 
	 * @return A <code>URI</code> representation
	 */
	public List<Resource> getSources() {
		return this._sources;
	}
	
	public void addSource(Resource source) {
		if (!this._sources.contains(source))
			this._sources.add(source);
	}
	
	public void addSources(List<Resource> sources) {
		for (Resource source : sources)
			addSource(source);
	}
	
	public void addAlias(Resource alias) {
		if (!this._aliases.contains(alias))
			this._aliases.add(alias);
	}
	
	public List<Resource> getPossibleAliases() {
		return this._possibleAliases;
	}
	
	public void addPossibleAlias(Resource alias) {
		if (!this._possibleAliases.contains(alias))
			this._possibleAliases.add(alias);
	}	
	
	/**
	 * Sets style classes for the value.
	 * 
	 * @param styles A <code>String</code>
	 */
	public void setStyles(String styles) {
		this._styles = styles;
	}
	
	
	/**
	 * Sets the output type for the value.
	 * 
	 * @param kind A <code>String</code>
	 */
	public void setOutputType(String kind) {
		this._kind = kind;
	}
	
	/**
	 * Renders the value to an element in the Fresnel XML tree output.
	 * 
	 * @param doc A <code>Document</code> for creating elements
	 * @return An <code>Element</code> representation of a value, e.g.:
	 *         &lt;value&gt;&lt;title&gt; ... &lt;/title&gt; ... &lt;/value&gt;
	 */
	public Element render(Document doc) {
		Element out = doc.createElementNS(INTERMEDIATE_NS, "value");
		if (getStyles() != null) out.setAttribute("class", getStyles());
		if (getOutputType() != null) out.setAttribute("output-type", getOutputType());
		if (!isResource()) {
			Element title = doc.createElementNS(INTERMEDIATE_NS, "title");
			title.appendChild(doc.createTextNode(getTitle().getString()));
			out.appendChild(title);
		} else {
			out.appendChild(getResult().render(doc));
		}
		
		for (Resource sourceResource : this._sources) {
			Element sourceURI = doc.createElementNS(INTERMEDIATE_NS, "sourceURI");
			sourceURI.appendChild(doc.createTextNode(sourceResource.toString()));
			
			Element source = doc.createElementNS(INTERMEDIATE_NS, "source");
			source.appendChild(sourceURI);
			
			out.appendChild(source);
		}
		
		for (Resource aliasResource : this._aliases) {
			Element aliasURI = doc.createElementNS(INTERMEDIATE_NS, "alias");
			aliasURI.appendChild(doc.createTextNode(aliasResource.toString()));
			
			out.appendChild(aliasURI);
		}		
		return out;
	}
}
