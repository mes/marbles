/*
 * Created on May 10, 2005
 */
package edu.mit.simile.fresnel.format;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;

import edu.mit.simile.fresnel.selection.UnresolvableException;
import edu.mit.simile.vocabularies.FresnelCore;

/**
 * Represents valid values for the Fresnel style term fresnel:label,
 * either a user-defined string replacement for a property label or
 * a resource switching on or off any textual label for the property.
 * 
 * @author ryanlee
 */
public class PropertyLabel {
    /**
     * The user-defined label
     */
    private String _string;
    
    /**
     * If the label is shown or not
     */
    private boolean _display;
    
    /**
     * Constructor (default display is always to be shown)
     */
    public PropertyLabel() {
        this._display = true;
    }
    
    /**
     * Retrieves the user-defined property label
     * 
     * @return The <code>String</code> label, or null if none
     */
    public String getString() {
        return this._string;
    }
    
    /**
     * Retrieves if the label should be shown or not
     * 
     * @return True if it should be, false if not
     */
    public boolean isShown() {
        return this._display;
    }
    
    /**
     * Retrieves if the label value is a string
     * 
     * @return True if the label value is a user-defined string, false if not
     */
    public boolean isString() {
        return (null != this._string);
    }
    
    /**
     * Sets the user-defined string value.
     * 
     * @param label A <code>String</code>
     */
    public void setString(String label) {
        this._string = label;
    }
    
    /**
     * Sets if the label is displayed or not.
     * 
     * @param show A <code>boolean</code>, true if the label should be displayed, false if not.
     */
    public void setIsShown(boolean show) {
        this._display = show;
    }
    
    /**
     * Parses the object of a fresnel:label statement into a PropertyLabel object.
     * 
     * @param label The <code>Value</code> from a configuration model that is the object
     *              of a fresnel:label statement.
     * @return A new <code>PropertyLabel</code>
     */
    public static PropertyLabel parse(Value label) throws UnresolvableException {
        PropertyLabel out = new PropertyLabel();
        if (label instanceof Resource) {
            Resource labelRes = (Resource) label;
            if (labelRes.equals(FresnelCore.show)) {
                out.setIsShown(true);
            } else if (labelRes.equals(FresnelCore.none)) {
                out.setIsShown(false);
            } else {
                throw new UnresolvableException("Cannot resolve value of fresnel:label: " + labelRes);
            }
        } else if (label instanceof Literal) {
            out.setString(((Literal) label).getLabel());
        } else {
            throw new UnresolvableException("Cannot resolve value of fresnel:label: " + label);
        }
        return out;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String state = "  [PropertyLabel " + super.toString() + "]: ";
        if (isString()) state += "\"" + getString() + "\"";
        else if (isShown()) state += "is displayed";
        else state += "is not displayed";
        state += "\n";
        return state;
    }
}
