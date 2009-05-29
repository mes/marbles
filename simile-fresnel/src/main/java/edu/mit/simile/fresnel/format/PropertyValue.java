/*
 * Created on May 10, 2005
 */
package edu.mit.simile.fresnel.format;

import edu.mit.simile.fresnel.selection.UnresolvableException;
import edu.mit.simile.vocabularies.FresnelCore;

import org.openrdf.model.Resource;
import org.openrdf.model.Value;

/**
 * Representing valid values for fresnel:value statements: either the value should be
 * displayed as a URI instead of with its label, should be displayed as an image, or
 * should be displayed as some other generic kind of resource.
 * 
 * @author ryanlee
 */
public class PropertyValue {
    /**
     * If the value should be displayed as a URI
     */
    private boolean _uri;
    
    /**
     * If the value should be displayed as an image
     */
    private boolean _image;
    
    /**
     * If the value should be displayed as a link
     */
    private boolean _link;
    
    /**
     * Constructor; the default value is a label, not any of these cases
     */
    public PropertyValue() {
        super();
        this._uri = false;
        this._image = false;
        this._link = false;
    }
    
    /**
     * Retrieves if the value should be displayed as a URI
     * 
     * @return True if so, false if not
     */
    public boolean isURI() {
        return this._uri;
    }
    
    /**
     * Retrieves if the value should be displayed as an image
     * 
     * @return True if so, false if not
     */
    public boolean isImage() {
        return this._image;
    }
    
    /**
     * Retrieves if the value should be displayed as a link
     * 
     * @return True if so, false if not
     */
    public boolean isLink() {
        return this._link;
    }
    
    /**
     * Sets if the value should be displayed as a URI
     * 
     * @param isURI True if so, false if not
     */
    public void setIsURI(boolean isURI) {
        this._uri = isURI;
    }
    
    /**
     * Sets if the value should be displayed as an image
     * 
     * @param isImage True if so, false if not
     */
    public void setIsImage(boolean isImage) {
        this._image = isImage;
    }
    
    /**
     * Sets if the value should be displayed as a replaced resource
     * 
     * @param isLink True if so, false if not
     */
    public void setIsLink(boolean isLink) {
        this._link = isLink;
    }
    
    /**
     * Parses an <code>RDFNode</code> into a <code>PropertyValue</code> object.
     * 
     * @param style A <code>Value</code> from a graph that is the object of a fresnel:value
     *              statement.
     * @return A new <code>PropertyValue</code> object
     */
    public static PropertyValue parse(Value style) throws UnresolvableException {
        PropertyValue out = new PropertyValue();
        if (style instanceof Resource) {
            Resource styleRes = (Resource) style;
            if (styleRes.equals(FresnelCore.uri)) {
                out.setIsURI(true);
            } else if (styleRes.equals(FresnelCore.image)) {
                out.setIsImage(true);
            } else if (styleRes.equals(FresnelCore.externalLink)) {
                out.setIsLink(true);
            } else {
                throw new UnresolvableException("Cannot resolve value of fresnel:property: " + styleRes);                
            }
        } else {
            throw new UnresolvableException("Cannot resolve value of fresnel:property: " + style);
        }
        return out;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String state = "  [PropertyValue " + super.toString() + "]: ";
        if (isURI()) state += "as URI";
        else if (isImage()) state += "as image";
        else if (isLink()) state += "as external link";
        else state += "as normal text";
        state += "\n";
        return state;
    }
}
