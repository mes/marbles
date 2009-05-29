/*
 * Created on Mar 16, 2005
 */
package edu.mit.simile.fresnel.format;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import edu.mit.simile.fresnel.FresnelResource;
import edu.mit.simile.fresnel.FresnelUtilities;
import edu.mit.simile.fresnel.results.ContentSet;
import edu.mit.simile.vocabularies.FresnelCore;

/**
 * Content positioning for a fresnel:Format, one of the valid objects for a fresnel:*Style statement.
 * 
 * @author ryanlee
 */
public class FormatDescription extends FresnelResource {
	/**
	 * Resource from Fresnel schema
	 */
	protected static Resource _schemaResource = FresnelCore.FormatDescription;
	
	/**
	 * Likely an anonymous node; identifying resource for this style description
	 */
	private Resource _identifier;
	
	/**
	 * The string to put before any content of a list of values
	 */
	private String _contentFirst;
	
	/**
	 * The string to put before each value in a list of values
	 */
	private String _contentBefore;
	
	/**
	 * The string to put after any content in a list of values
	 */
	private String _contentAfter;
	
	/**
	 * The string to put after all content of a list of values
	 */
	private String _contentLast;
	
	/**
	 * The string to use if no values are present
	 */
	private String _contentNoValue;
	
	/**
	 * Empty constructor using identifying resource.
	 * 
	 * @param id A <code>Resource</code> that is a fresnel:StyleDescription
	 */
	public FormatDescription(Resource id) {
		setIdentifier(id);
	}
	
	/**
	 * General constructor taking all content position strings.
	 * 
	 * @param id A <code>Resource</code> that is a fresnel:StyleDescription
	 * @param first <code>String</code> for contentFirst
	 * @param before <code>String</code> for contentBefore
	 * @param after <code>String</code> for contentAfter
	 * @param last <code>String</code> for contentLast
	 */
	public FormatDescription(Resource id, String first, String before, String after, String last) {
		setIdentifier(id);
		setContentFirst(first);
		setContentBefore(before);
		setContentAfter(after);
		setContentLast(last);
		setContentNoValue(null);
	}
	
	/**
	 * Retreives the resource identifier
	 * 
	 * @return A <code>Resource</code>
	 */
	public Resource getIdentifier() {
		return this._identifier;
	}
	
	/**
	 * Retrieves the :contentFirst string
	 * 
	 * @return A <code>String</code>
	 */
	public String getContentFirst() {
		return this._contentFirst;
	}
	
	/**
	 * Retrieves the :contentBefore string
	 * 
	 * @return A <code>String</code>
	 */
	public String getContentBefore() {
		return this._contentBefore;
	}
	
	/**
	 * Retrieves the :contentAfter string
	 * 
	 * @return A <code>String</code>
	 */
	public String getContentAfter() {
		return this._contentAfter;
	}
	
	/**
	 * Retrieves the :contentLast string
	 * 
	 * @return A <code>String</code>
	 */
	public String getContentLast() {
		return this._contentLast;
	}
	
	/**
	 * Retrieves the :contentNoValue string
	 * 
	 * @return A <code>String</code>
	 */
	public String getContentNoValue() {
		return this._contentNoValue;
	}
	
	/**
	 * Constructs and returns a set of contents.
	 * 
	 * @return A <code>ContentSet</code>
	 */
	public ContentSet getContentSet() {
		return new ContentSet(getContentFirst(),
				getContentBefore(),
				getContentAfter(),
				getContentLast(),
				getContentNoValue());
	}
	
	/**
	 * Sets the identifier for this format description
	 * 
	 * @param id A <code>Resource</code>
	 */
	public void setIdentifier(Resource id) {
		this._identifier = id;
	}
	
	/**
	 * Sets the :contentFirst string
	 * 
	 * @param content A <code>String</code>
	 */
	public void setContentFirst(String content) {
		this._contentFirst = content;
	}
	
	/**
	 * Sets the :contentBefore string
	 * 
	 * @param content A <code>String</code>
	 */
	public void setContentBefore(String content) {
		this._contentBefore = content;
	}
	
	/**
	 * Sets the :contentAfter string
	 * 
	 * @param content A <code>String</code>
	 */
	public void setContentAfter(String content) {
		this._contentAfter = content;
	}
	
	/**
	 * Sets the :contentLast string
	 * 
	 * @param content A <code>String</code>
	 */
	public void setContentLast(String content) {
		this._contentLast = content;
	}
	
	/**
	 * Sets the :contentNoValue string
	 * 
	 * @param content A <code>String</code>
	 */
	public void setContentNoValue(String content) {
		this._contentNoValue = content;
	}
	
	/**
	 * Parses a resource and its associated :content* statements into a style description object.
	 * 
	 * @param container The data containing <code>Repository</code>
	 * @param id The <code>Resource</code> to start parsing from.
	 * @return A new <code>StyleDescription</code> object.
	 */
	public static FormatDescription parse(Repository container, Resource id) {
		FormatDescription out = new FormatDescription(id);
		try {
			RepositoryConnection conn = container.getConnection();
			if (conn.hasStatement(id, FresnelCore.contentAfter, (Value) null, false)) {
				out.setContentAfter(((Literal) FresnelUtilities.getSinglePropertyValue(container, id, FresnelCore.contentAfter)).getLabel());
			}
			if (conn.hasStatement(id, FresnelCore.contentBefore, (Value) null, false)) {
				out.setContentBefore(((Literal) FresnelUtilities.getSinglePropertyValue(container, id, FresnelCore.contentBefore)).getLabel());
			}
			if (conn.hasStatement(id, FresnelCore.contentLast, (Value) null, false)) {
				out.setContentLast(((Literal) FresnelUtilities.getSinglePropertyValue(container, id, FresnelCore.contentLast)).getLabel());
			}
			if (conn.hasStatement(id, FresnelCore.contentFirst, (Value) null, false)) {
				out.setContentFirst(((Literal) FresnelUtilities.getSinglePropertyValue(container, id, FresnelCore.contentFirst)).getLabel());            
			}
			if (conn.hasStatement(id, FresnelCore.contentNoValue, (Value) null, false)) {
				out.setContentNoValue(((Literal) FresnelUtilities.getSinglePropertyValue(container, id, FresnelCore.contentNoValue)).getLabel());
			}
			conn.close();
		} catch (RepositoryException e) {
			// ignore, add no new formats
		}
		return out;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String state = "   [FormatDescription " + super.toString() + "]\n";
		if (null != this.getContentFirst()) state += "    First: \"" + this.getContentFirst() + "\"\n";
		if (null != this.getContentBefore()) state += "    Before: \"" + this.getContentBefore() + "\"\n";
		if (null != this.getContentAfter()) state += "    After: \"" + this.getContentAfter() + "\"\n";
		if (null != this.getContentLast()) state += "    Last: \"" + this.getContentLast() + "\"\n";
		if (null != this.getContentNoValue()) state += "    NoValue: \"" + this.getContentNoValue() + "\"\n";
		state += "\n";
		return state;
	}
}
