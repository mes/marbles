/*
 * Created on May 10, 2005
 */
package edu.mit.simile.fresnel.format;

import edu.mit.simile.fresnel.selection.UnresolvableException;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;

/**
 * Values of fresnel:*Style properties, CSS-like strings in core but possibly
 * otherwise in extensions.
 * 
 * @author ryanlee
 */
public class Style {    
    /**
     * The value of an opaque styling string
     */
    private String _string;
    
    /**
     * The value is a string
     */
    private boolean _isString;
    
    /**
     * Retrieves the styling string.
     * 
     * @return The <code>String</code> value or null if none.
     */
    public String getString() {
        return this._string;
    }
    
    /**
     * Sets the styling string.
     * 
     * @param style A <code>String</code>
     */
    public void setStyleString(String style) {
        this._string = style;
        setType(true);
    }
    
    /**
     * Sets whether the value is a string or a style description object.
     * 
     * @param isString True if the value is a string, false if not
     */
    protected void setType(boolean isString) {
        this._isString = isString;
    }
    
    /**
     * Convenience method for whether the value is a string or a style description object.
     * 
     * @return A <code>boolean</code> that is true if the value is a string, false if not
     */
    public boolean isString() {
        return this._isString;
    }
    
    /**
     * Parses the object of a fresnel:*Style statement and returns a new StyleValue object
     * 
     * @param style The <code>Value</code> in a configuration model with the style value
     * @return A <code>StyleValue</code> object
     */
    public static Style parse(Value style) throws UnresolvableException {
        Style out = new Style();
        if (style instanceof Literal) {
            out.setStyleString(((Literal) style).getLabel());
        } else {
            throw new UnresolvableException("Cannot determine what to do with style value: " + style);
        }
        return out;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String state = "   [StyleValue " + super.toString() + "]: ";
        if (isString()) state += "\"" + getString() + "\"";
        return state;
    }
}
