package edu.mit.simile.fresnel.results;

import edu.mit.simile.fresnel.selection.ISelector;

import org.openrdf.model.URI;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * For properties that are specified as ones to be displayed but did not
 * exist in the result model.
 * 
 * @author ryanlee
 */
public class NoSuchPropertyResult extends PropertyResult implements ResultConstants {	
	/**
	 * Constructor based on property, selection mechanism, and parent resource result.
	 * 
	 * @param prop A property <code>URI</code>
	 * @param selector The selectin <code>ISelector</code>
	 * @param parent The parent <code>Resource</code>
	 */
	public NoSuchPropertyResult(URI prop, ISelector selector, Result parent) {
		super(prop, selector, parent);
	}
	
	/**
	 * Whether the property is in the model or not; always false here.
	 * 
	 * @return <code>boolean</code> false
	 */
	public boolean isInModel() {
		return false;
	}
	
	/**
	 * Only render this if there is some contentNoValue given for it.
	 * 
	 * @param doc A <code>Document</code> for creating elements
	 * @return An <code>Element</code> representing the property
	 * @see PropertyResult#render(Document)
	 */
	public Element render(Document doc) {
		Element out = null;
		if (null != getContents().getReplacement()) {
			ValueResult noContent = new ValueResult(getContents().getReplacement(), (PropertyResult) this, null);
			ValueResultSet values = new ValueResultSet();
			values.addValueResult(noContent);
			out = doc.createElementNS(INTERMEDIATE_NS, "property");
			out.setAttribute("class", getStyles());
			out.setAttribute("uri", getURI());
			Element content = getContents().render(doc);
			if (content.hasChildNodes()) out.appendChild(content);
			if (showLabel()) {
				Element label = doc.createElementNS(INTERMEDIATE_NS, "label");
				label.setAttribute("class", getLabelStyles());
				label.appendChild(getLabelContents().render(doc));
				Element title = doc.createElementNS(INTERMEDIATE_NS, "title");
				if (null == getTitle() || getTitle().isFallback()) {
					title.appendChild(doc.createTextNode(getLabelContents().getReplacement()));
					label.appendChild(title);
					out.appendChild(label);				
				} else {
					title.appendChild(doc.createTextNode(getTitle().getString()));
					label.appendChild(title);
					out.appendChild(label);
				}
			}
			out.appendChild(values.render(doc));
		}
		return out;
	}
}
