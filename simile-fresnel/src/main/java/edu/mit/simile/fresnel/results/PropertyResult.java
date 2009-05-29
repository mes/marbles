package edu.mit.simile.fresnel.results;

import edu.mit.simile.fresnel.selection.ISelector;

import org.openrdf.model.URI;
import org.openrdf.repository.Repository;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents property selection and formatting results.
 * 
 * @author ryanlee
 */
public class PropertyResult implements ResultConstants {
	/**
	 * Property the result describes
	 */
	private URI _origin;
	/**
	 * Human friendly label
	 */
	private Title _title;
	/**
	 * Associated style classes
	 */
	private String _styles;
	/**
	 * Associated formatting strings
	 */
	private ContentSet _contents;
	/**
	 * Whether to show the property's label or not
	 */
	private boolean _showLabel;
	/**
	 * Formatting for the property's label
	 */
	private ContentSet _labelContents;
	/**
	 * Styles for the property's label
	 */
	private String _labelStyles;
	/**
	 * Values associated with the property
	 */
	private ValueResultSet _values;
	/**
	 * The selector used to select the property
	 */
	private ISelector _selector;
	/**
	 * The parent resource result
	 */
	private Result _parent;

	/**
	 * Whether this property result denotes an inverse relationship
	 */
	private boolean _inverse;	
	
	/**
	 * 
	 * @param prop
	 * @param selector
	 * @param parent
	 */
	public PropertyResult(URI prop, ISelector selector, Result parent, boolean inverse) {
		this._origin = prop;
		this._values = new ValueResultSet();
		this._contents = new ContentSet();
		this._labelContents = new ContentSet();
		this._showLabel = true;
		this._selector = selector;
		this._parent = parent;
		this._inverse = inverse;
	}
	
	public PropertyResult(URI prop, ISelector selector, Result parent) {
		this._origin = prop;
		this._values = new ValueResultSet();
		this._contents = new ContentSet();
		this._labelContents = new ContentSet();
		this._showLabel = true;
		this._selector = selector;
		this._parent = parent;
		this._inverse = false;
	}

	/**
	 * Retrieves the property URI
	 * 
	 * @return The property <code>URI</code>
	 */
	public URI getOrigin() {
		return this._origin;
	}
	
	/**
	 * Retrieves the selecting selector
	 * 
	 * @return An <code>ISelector</code>
	 */
	public ISelector getSelector() {
		return this._selector;
	}
	
	/**
	 * Retrieves the result for the subject using this property
	 * 
	 * @return A <code>Result</code>
	 */
	public Result getParent() {
		return this._parent;
	}
	
	/**
	 * Returns a string representation of the property URI
	 * 
	 * @return A <code>String</code> URI
	 */
	public String getURI() {
		return this._origin.toString();
	}
	
	/**
	 * Retrieves the title for the property
	 * 
	 * @return A <code>Title</code>
	 */
	public Title getTitle() {
		return this._title;
	}
	
	/**
	 * Retrieves styling for the property
	 * 
	 * @return A <code>String</code>
	 */
	public String getStyles() {
		return this._styles;
	}
	
	/**
	 * Retrieves content formatting for the property
	 * 
	 * @return A <code>ContentSet</code>
	 */
	public ContentSet getContents() {
		return this._contents;
	}
	
	/**
	 * Retrieves styling for the property's label
	 * 
	 * @return A <code>String</code>
	 */
	public String getLabelStyles() {
		return this._labelStyles;
	}
	
	/**
	 * Retrieves content formatting for the property's label
	 * 
	 * @return A <code>ContentSet</code>
	 */
	public ContentSet getLabelContents() {
		return this._labelContents;
	}
	
	/**
	 * Returns the values this property points to.
	 * 
	 * @return A <code>ValueResultSet</code>
	 */
	public ValueResultSet getValues() {
		return this._values;
	}
	
	/**
	 * Whether this property's label should be shown.
	 * 
	 * @return True if it should be, false if not.
	 */
	public boolean showLabel() {
		return this._showLabel;
	}
	
	/**
	 * Sets the human-friendly title.
	 * 
	 * @param title A <code>Title</code>
	 */
	public void setTitle(Title title) {
		this._title = title;
	}
	
	/**
	 * Sets the property's styling information.
	 * 
	 * @param style A <code>String</code>
	 */
	public void setStyles(String style) {
		this._styles = style;
	}
	
	/**
	 * Sets the property's content formatting.
	 * 
	 * @param contents A <code>ContentSet</code>
	 */
	public void setContents(ContentSet contents) {
		this._contents = contents;
	}
	
	/**
	 * Sets the property label's styling.
	 * 
	 * @param style A <code>String</code>
	 */
	public void setLabelStyles(String style) {
		this._labelStyles = style;
	}
	
	/**
	 * Sets the property label's content formatting.
	 * 
	 * @param contents A <code>ContentSet</code>
	 */
	public void setLabelContents(ContentSet contents) {
		this._labelContents = contents;
	}
	
	/**
	 * Sets whether the label should be shown.
	 * 
	 * @param show True if it should be shown, false if not
	 */
	public void setShowLabel(boolean show) {
		this._showLabel = show;
	}
	
	/**
	 * Add another value to this property's value result set
	 * 
	 * @param val A <code>ValueResult</code>
	 * @param in  Data repository
	 * @return Success or failure
	 */
	public boolean addValue(ValueResult val, Repository in) {
		return this._values.addValueResult(val, in);
	}
	
	/**
	 * Add another value to this property's value result set
	 * 
	 * @param val A <code>ValueResult</code>
	 * @return Success or failure
	 */
	public boolean addValue(ValueResult value) {
		return addValue(value, null);
	}

	
	/**
	 * If this property result originated from the source graph.
	 * 
	 * @return True
	 */
	public boolean isInModel() {
		return true;
	}
	
	/**
	 * Render the property result and all its children into a part of the Fresnel XML
	 * output tree.
	 * 
	 * @param doc A <code>Document</code> to create elements.
	 * @return An <code>Element</code> representing the property, e.g.:
	 *         &lt;property&gt;&lt;label&gt; ... &lt;/label&gt; ... &lt/property&gt;
	 */
	public Element render(Document doc) {
		Element out = doc.createElementNS(INTERMEDIATE_NS, "property");
		out.setAttribute("class", getStyles());
		out.setAttribute("uri", getURI());
		out.setAttribute("inverse", Boolean.toString(isInverse()));
		Element content = getContents().render(doc);
		if (content.hasChildNodes()) out.appendChild(content);
		if (showLabel()) {
			Element label = doc.createElementNS(INTERMEDIATE_NS, "label");
			label.setAttribute("class", getLabelStyles());
			label.appendChild(getLabelContents().render(doc));
			Element title = doc.createElementNS(INTERMEDIATE_NS, "title");
			// this should already be taken care of by selection.applyLabelFormat, but hey,
			// why not more code...
			if (null == getTitle() || getTitle().isFallback()) {
				// Happens to be null in the fresnel:member case - SBD
				if (null != getLabelContents().getReplacement())
					title.appendChild(doc.createTextNode(getLabelContents().getReplacement()));
				label.appendChild(title);
				out.appendChild(label);				
			} else {
				title.appendChild(doc.createTextNode(getTitle().getString()));
				label.appendChild(title);
				out.appendChild(label);
			}
		}
		out.appendChild(getValues().render(doc));
		return out;
	}
	
	/**
	 * If this property result denotes an inverse relationship
	 * 
	 * @return True
	 */
	public boolean isInverse() {
		return _inverse;
	}
}
