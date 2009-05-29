package edu.mit.simile.fresnel.results;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Configured strings that may surround or interleave with parts of the output,
 * corresponding to values of the properties that can be used with a fresnel:FormatDescription.
 * 
 * @author ryanlee
 */
public class ContentSet implements ResultConstants {
	/**
	 * Text prefacing the entire set of data.
	 */
	private String _first;
	/**
	 * Text prefacing each individual element of a set of data.
	 */
	private String _before;
	/**
	 * Text following each individual element of a set of data.
	 */
	private String _after;
	/**
	 * Text following the entire set of data.
	 */
	private String _last;
	/**
	 * Text standing in place of non-existent data.
	 */
	private String _replace;
	
	/**
	 * Base constructor, all strings will be null.
	 */
	public ContentSet() {
		this._first = null;
		this._before = null;
		this._after = null;
		this._last = null;
		this._replace = null;
	}
	
	/**
	 * Construct a set with the given strings.
	 * 
	 * @param first Set prefacing <code>String</code>
	 * @param before Element prefacing <code>String</code>
	 * @param after Element following <code>String</code>
	 * @param last Set following <code>String</code>
	 * @param replace Missing data replacement <code>String</code>
	 */
	public ContentSet(String first,
			String before,
			String after,
			String last,
			String replace) {
		this._first = first;
		this._before = before;
		this._after = after;
		this._last = last;
		this._replace = replace;		
	}
	
	/**
	 * Returns set prefacing string.
	 * 
	 * @return A <code>String</code>
	 */
	public String getFirst() {
		return this._first;
	}
	
	/**
	 * Returns element prefacing string.
	 * 
	 * @return A <code>String</code>
	 */
	public String getBefore() {
		return this._before;
	}
	
	/**
	 * Returns element following string.
	 * 
	 * @return A <code>String</code>
	 */
	public String getAfter() {
		return this._after;
	}
	
	/**
	 * Returns set following string.
	 * 
	 * @return A <code>String</code>
	 */
	public String getLast() {
		return this._last;
	}
	
	/**
	 * Returns missing data replacement string.
	 * 
	 * @return A <code>String</code>
	 */
	public String getReplacement() {
		return this._replace;
	}

	/**
	 * Sets the set prefacing string
	 * 
	 * @param first A <code>String</code>
	 */
	public void setFirst(String first) {
		this._first = first;
	}

	/**
	 * Sets the element prefacing string.
	 * 
	 * @param before A <code>String</code>
	 */
	public void setBefore(String before) {
		this._before = before;
	}

	/**
	 * Sets the element following string.
	 * 
	 * @param after A <code>String</code>
	 */
	public void setAfter(String after) {
		this._after = after;
	}

	/**
	 * Sets the set following string.
	 * 
	 * @param last A <code>String</code>
	 */
	public void setLast(String last) {
		this._last = last;
	}

	/**
	 * Sets the missing data replacement string.
	 * 
	 * @param replace A <code>String</code>
	 */
	public void setReplacement(String replace) {
		this._replace = replace;
	}

	/**
	 * Render the set to an element and allow whatever process the Fresnel output
	 * to deal with the XML tree form to place the contents as needed.  Keeps the
	 * output from bloat, but requires post-processing to place the strings in the
	 * correct position.
	 * 
	 * @param doc A <code>Document</code> for creating elements
	 * @return An <code>Element</code> representing the content, e.g.:
	 *         &lt;content&gt;&lt;first&gt;I go first&lt;/first&gt; ... &lt;/content&gt;
	 */
	public Element render(Document doc) {
		Element out = doc.createElementNS(INTERMEDIATE_NS, "content");
		if (this._first != null) {
			Element part = doc.createElementNS(INTERMEDIATE_NS, "first");
			part.appendChild(doc.createTextNode(this._first));
			out.appendChild(part);
		}
		if (this._before != null) {
			Element part = doc.createElementNS(INTERMEDIATE_NS, "before");
			part.appendChild(doc.createTextNode(this._before));
			out.appendChild(part);
		}
		if (this._after != null) {
			Element part = doc.createElementNS(INTERMEDIATE_NS, "after");
			part.appendChild(doc.createTextNode(this._after));
			out.appendChild(part);
		}
		if (this._last != null) {
			Element part = doc.createElementNS(INTERMEDIATE_NS, "last");
			part.appendChild(doc.createTextNode(this._last));
			out.appendChild(part);
		}
		return out;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String out = "";
		out += "[ContentSet " + super.toString() + "]\n";
		out += "> first: " + this._first + "\n";
		out += "> before: " + this._before + "\n";
		out += "> after: " + this._after + "\n";
		out += "> last: " + this._last + "\n";
		out += "> replace: " + this._replace + "\n";
		out += "[end ContentSet]\n";
		return out;
	}
}
