package edu.mit.simile.fresnel.results;

/**
 * Represents the title of a result.  Can be a result of guessing at a title,
 * known here as a fallback - generally this is a URI or something suboptimal
 * that the system is guessing at for human readability.
 * 
 * @author ryanlee
 */
public class Title {
	/**
	 * An as-yet unrendered label selection based on more complex labelling rules.
	 */
	private AggregateLabel _unrendered;
	
	/**
	 * A plain string title.
	 */
	private String _title;
	
	/**
	 * If this title is a backup case, if no other title in the model could be found.
	 */
	private boolean _fallback = false;
	
	/**
	 * Constructor based on label lens result.
	 * 
	 * @param title An <code>AggregateLabel</code>
	 */
	public Title(AggregateLabel title) {
		this._unrendered = title;
	}
	
	/**
	 * Constructor based on label lens result and whether it was a fallback or not.
	 * 
	 * @param title An <code>AggregateLabel</code>
	 * @param fallback A <code>boolean</code>
	 */
	public Title(AggregateLabel title, boolean fallback) {
		this._unrendered = title;
		this._fallback = fallback;
	}

	/**
	 * Constructor based on plain string
	 * 
	 * @param title A <code>String</code>
	 */
	public Title(String title) {
		this._title = title;
		this._fallback = false;
	}
	
	/**
	 * Constructor based on plain string and whether it was a fallback or not.
	 * 
	 * @param title A <code>String</code>
	 * @param fallback A <code>boolean</code>
	 */
	public Title(String title, boolean fallback) {
		this._title = title;
		this._fallback = fallback;
	}
	
	/**
	 * Whether the title is a fallback title or not.
	 * 
	 * @return True if fallback, false if not.
	 */
	public boolean isFallback() {
		return this._fallback;
	}
	
	/**
	 * Render the title as a string regardless of the underlying data structure; checks
	 * for an AggregateLabel before a String.
	 * 
	 * @return A <code>String</code> representation of the title
	 */
	public String getString() {
		return (null != this._unrendered) ? this._unrendered.getString() : this._title;
	}
	
	/**
	 * Sets the plain text version of the title.
	 * 
	 * @param title A <code>String</code> title
	 */
	public void setString(String title) {
		this._title = title;
	}
	
	/**
	 * Sets if the title was the result of falling back on algorithmic guessing at a
	 * human friendly label.
	 * 
	 * @param fallback A <code>boolean</code>, true if a fallback, false otherwise
	 */
	public void setFallback(boolean fallback) {
		this._fallback = fallback;
	}
}
