/*
 * Created on Mar 16, 2005
 */
package edu.mit.simile.fresnel.format;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import fr.inria.jfresnel.fsl.FSLPath;

import edu.mit.simile.fresnel.FresnelCoreTypes;
import edu.mit.simile.fresnel.FresnelResource;
import edu.mit.simile.fresnel.configuration.Configuration;
import edu.mit.simile.fresnel.purpose.Purpose;
import edu.mit.simile.fresnel.purpose.PurposeSet;
import edu.mit.simile.fresnel.selection.AllPropertiesSelector;
import edu.mit.simile.fresnel.selection.AllPropertiesSet;
import edu.mit.simile.fresnel.selection.FSESelector;
import edu.mit.simile.fresnel.selection.ISelector;
import edu.mit.simile.fresnel.selection.ParsingException;
import edu.mit.simile.fresnel.selection.PropertySelector;
import edu.mit.simile.fresnel.selection.SPARQLSelector;
import edu.mit.simile.fresnel.selection.UnresolvableException;
import edu.mit.simile.vocabularies.FresnelCore;

/**
 * Represents a fresnel:Format.
 * 
 * @author ryanlee
 */
public class Format extends FresnelResource{
    /**
     * Resource defined in the schema.
     */
    protected static Resource _schemaResource = FresnelCore.Format;
    
	/**
	 * Context for determining FSL first location step type.
	 */
	protected final static short _fslContext = FSLPath.NODE_STEP;

	/**
     * Resource identifying this format.
     */
    private Resource _identifier;
    
    /**
     * Match the domain to a model to choose properties for formatting.
     */
    private Set<ISelector> _domains;
    
    /**
     * Specify the purposes of the format.
     */
    private PurposeSet _purposes;
    
    /**
     * The property label of the property's format
     */
    private PropertyLabel _label;
    
    /**
     * The value type of the property's format
     */
    private PropertyValue _value;
    
    /**
     * The styling for a resource
     */
    private Set<Style>_resourceStyle;
    
    /**
     * The styling for a property
     */
    private Set<Style> _propertyStyle;
        
    /**
     * The styling for a label
     */
    private Set<Style> _labelStyle;
    
    /**
     * The styling for a value
     */
    private Set<Style> _valueStyle;
        
    /**
     * The formatting for a resource
     */
    private Set<FormatDescription> _resourceFormat;
        
    /**
     * The formatting for a property
     */
    private Set<FormatDescription> _propertyFormat;
        
    /**
     * The formatting for a label
     */
    private Set<FormatDescription> _labelFormat;
    
    /**
     * The formatting for a value
     */
    private Set<FormatDescription> _valueFormat;

    /**
     * Constructor with identifying resource
     * 
     * @param id A <code>Resource</code>
     */
    public Format(Resource id) {
        setIdentifier(id);
        this._domains = new HashSet<ISelector>();
        this._purposes = new PurposeSet();
        this._label = new PropertyLabel();
        this._value = new PropertyValue();
        this._resourceStyle = new HashSet<Style>();
        this._propertyStyle = new HashSet<Style>();
        this._labelStyle = new HashSet<Style>();
        this._valueStyle = new HashSet<Style>();
        this._resourceFormat = new HashSet<FormatDescription>();
        this._propertyFormat = new HashSet<FormatDescription>();
        this._labelFormat = new HashSet<FormatDescription>();
        this._valueFormat = new HashSet<FormatDescription>();
    }
    
    /**
     * Retrieves the identifying resource
     * 
     * @return A <code>Resource</code> identifier
     */
    public Resource getIdentifier() {
        return this._identifier;
    }
    
    /**
     * Returns the format domains.
     * 
     * @return A <code>DomainSet</code>
     */
    public Set<ISelector> getDomainSet() {
        return this._domains;
    }
    
    /**
     * Returns the purposes of the format.
     * 
     * @return A <code>PurposeSet</code>
     */
    public PurposeSet getPurposes() {
        return this._purposes;
    }
    
    /**
     * Retrieves the label object
     * 
     * @return A <code>PropertyLabel</code>
     */
    public PropertyLabel getLabel() {
        return this._label;
    }
    
    /**
     * Retrieves the value type object
     * 
     * @return A <code>PropertyValue</code>
     */
    public PropertyValue getValueType() {
        return this._value;
    }
    
    /**
     * Retrieves the style values associated with a resource
     * 
     * @return A <code>StyleSet</code>
     */
    public Set<Style> getResourceStyle() {
        return this._resourceStyle;
    }    
    /**
     * Retrieves the style values associated with a property
     * 
     * @return A <code>StyleSet</code>
     */
    public Set<Style> getPropertyStyle() {
        return this._propertyStyle;
    }
    
    /**
     * Retrieves the style values associated with a label
     * 
     * @return A <code>StyleSet</code>
     */
    public Set<Style> getLabelStyle() {
        return this._labelStyle;
    }
    
    /**
     * Retrieves the style values associated with a value
     * 
     * @return A <code>StyleSet</code>
     */
    public Set<Style> getValueStyle() {
        return this._valueStyle;
    }
    
    /**
     * Retrieves the format values associated with a resource
     * 
     * @return A <code>FormatDescriptionSet</code>
     */
    public Set<FormatDescription> getResourceFormat() {
        return this._resourceFormat;
    }
    
    /**
     * Retrieves the format values associated with a property
     * 
     * @return A <code>FormatDescriptionSet</code>
     */
    public Set<FormatDescription> getPropertyFormat() {
        return this._propertyFormat;
    }
    
    /**
     * Retrieves the format values associated with a label
     * 
     * @return A <code>FormatDescriptionSet</code>
     */
    public Set<FormatDescription> getLabelFormat() {
        return this._labelFormat;
    }
    
    /**
     * Retrieves the format values associated with a value
     * 
     * @return A <code>FormatDescriptionSet</code>
     */
    public Set<FormatDescription> getValueFormat() {
        return this._valueFormat;
    }
    
    /**
     * Sets the resource identifying this format.
     * 
     * @param identifier A unique <code>Resource</code>
     */
    protected void setIdentifier(Resource identifier) {
        this._identifier = identifier;
    }
    
    /**
     * Adds an applicable domain to the existing set of domains
     * 
     * @param domain An appropriate <code>ISelector</code>
     * @return True if the addition succeeded, false if not
     */
    public boolean addDomain(ISelector domain) {
        return this._domains.add(domain);
    }
    
    /**
     * Adds a purpose to the existing set of purposes.
     * 
     * @param purpose A <code>Purpose</code>
     * @return True if the addition succeeded, false if not
     */
    public boolean addPurpose(Purpose purpose) {
        return this._purposes.addPurpose(purpose);
    }

    /**
     * Sets the label for the format
     * 
     * @param label A <code>PropertyLabel</code>
     */
    protected void setLabel(PropertyLabel label) {
        this._label = label;
    }
    
    /**
     * Sets the value type for the format
     * 
     * @param value A <codde>PropertyValue</code>
     */
    protected void setValueType(PropertyValue value) {
        this._value = value;
    }
    
    /**
     * Sets the styling for a resource
     * 
     * @param style A <code>StyleSet</code>
     */
    protected void setResourceStyle(Set<Style> style) {
        this._resourceStyle = style;
    }    
    /**
     * Sets the styling for a property
     * 
     * @param style A <code>StyleSet</code>
     */
    protected void setPropertyStyle(Set<Style> style) {
        this._propertyStyle = style;
    }

    /**
     * Sets the styling for a label
     * 
     * @param style A <code>StyleSet</code>
     */
    protected void setLabelStyle(Set<Style> style) {
        this._labelStyle = style;
    }

    /**
     * Sets the styling for a value
     * 
     * @param style A <code>StyleSet</code>
     */
    protected void setValueStyle(Set<Style> style) {
        this._valueStyle = style;
    }
    
    /**
     * Adds a styling for a resource
     * 
     * @param style A <code>Style</code>
     */
    protected void addResourceStyle(Style style) {
        this._resourceStyle.add(style);
    }

    /**
     * Adds a styling for a property
     * 
     * @param style A <code>Style</code>
     */
    protected void addPropertyStyle(Style style) {
        this._propertyStyle.add(style);
    }

    /**
     * Adds a styling for a label
     * 
     * @param style A <code>Style</code>
     */
    protected void addLabelStyle(Style style) {
        this._labelStyle.add(style);
    }

    /**
     * Adds a styling for a value
     * 
     * @param style A <code>Style</code>
     */
    protected void addValueStyle(Style style) {
        this._valueStyle.add(style);
    }
    
    /**
     * Sets the formatting for a resource
     * 
     * @param fd A <code>FormatDescriptionSet</code>
     */
    protected void setResourceFormat(Set<FormatDescription> fd) {
        this._resourceFormat = fd;
    }
    
    /**
     * Sets the formatting for a property
     * 
     * @param fd A <code>FormatDescriptionSet</code>
     */
    protected void setPropertyFormat(Set<FormatDescription> fd) {
        this._propertyFormat = fd;
    }

    /**
     * Sets the formatting for a label
     * 
     * @param fd A <code>FormatDescriptionSet</code>
     */
    protected void setLabelFormat(Set<FormatDescription> fd) {
        this._labelFormat = fd;
    }

    /**
     * Sets the formatting for a value
     * 
     * @param fd A <code>FormatDescriptionSet</code>
     */
    protected void setValueFormat(Set<FormatDescription> fd) {
        this._valueFormat = fd;
    }
    
    /**
     * Adds a formatting for a resource
     * 
     * @param format A <code>FormatDescription</code>
     */
    protected void addResourceFormat(FormatDescription format) {
        this._resourceFormat.add(format);
    }    

    /**
     * Adds a formatting for a property
     * 
     * @param format A <code>FormatDescription</code>
     */
    protected void addPropertyFormat(FormatDescription format) {
        this._propertyFormat.add(format);
    }

    /**
     * Adds a formatting for a label
     * 
     * @param format A <code>FormatDescription</code>
     */
    protected void addLabelFormat(FormatDescription format) {
        this._labelFormat.add(format);
    }

    /**
     * Adds a formatting for a value
     * 
     * @param format A <code>FormatDescription</code>
     */
    protected void addValueFormat(FormatDescription format) {
        this._valueFormat.add(format);
    }
    
    /**
     * Check if this style has a particular purpose.
     * 
     * @param check A <code>Purpose</code> to check against
     * @return A <code>boolean</code> indicator
     */
    public boolean hasPurpose(Purpose check) {
        return this._purposes.contains(check);
    }
    
    /**
     * Parses a resource into a valid fresnel:Format object.
     * 
     * @param in The <code>Repository</code> the configuration is derived from
     * @param selected The fresnel:Format <code>Resource</code>
     * @param conf The <code>Configuration</code> to add to
     * @return A new <code>Format</code>
     * @throws UnresolvableException When the wrong count for a predicate is present
     * @throws ParsingException When any other syntax error occurs
     */
    public static Format parse(Repository in, Resource selected, Configuration conf)
    throws UnresolvableException, ParsingException {
        Format out = new Format(selected);
        
        try {
            // Parse domain
            parseDomain(out, in, selected, conf);
            
        	RepositoryConnection conn = in.getConnection();

        	// Set of purposes
            RepositoryResult<Statement> purposesI = conn.getStatements(selected, FresnelCore.purpose, (Value) null, false);
        	try {
        		while (purposesI.hasNext()) {
        			Value purposeNode = purposesI.next().getObject();
        			if (purposeNode instanceof Resource) {
        				Resource purposeRes = (Resource) purposeNode;
        				out.addPurpose(new Purpose(purposeRes));
        			} else {
        				throw new ParsingException(purposeNode.toString() + "could not be used as a :Purpose");
        			}
        		}
        	} finally {
        		purposesI.close();
        	}

        	// label
            RepositoryResult<Statement> labelI = conn.getStatements(selected, FresnelCore.label, (Value) null, false);
        	try {
        		if (labelI.hasNext()) {
        			Statement labelS = labelI.next();
        			if (labelI.hasNext()) throw new ParsingException("Too many fresnel:label statements; only one allowed: " + selected);
        			Value labelObj = labelS.getObject();
        			out.setLabel(PropertyLabel.parse(labelObj));
        		}
        	} finally {
        		labelI.close();
        	}

        	// value
            RepositoryResult<Statement> valueI = conn.getStatements(selected, FresnelCore.value, (Value) null, false);
        	try {
        		if (valueI.hasNext()) {
        			Statement valueS = valueI.next();
        			if (valueI.hasNext()) throw new ParsingException("Too many fresnel:value statements; only one allowed: " + selected);
        			Value valueObj = valueS.getObject();
        			out.setValueType(PropertyValue.parse(valueObj));
        		}
        	} finally {
        		valueI.close();
        	}

        	// property style
            RepositoryResult<Statement> propertyStyleI = conn.getStatements(selected, FresnelCore.propertyStyle, (Value) null, false);
        	while (propertyStyleI.hasNext()) {
        		Statement propertyStyleS = propertyStyleI.next();
        		Value styleObj = propertyStyleS.getObject();
        		out.addPropertyStyle(Style.parse(styleObj));
        	}
        	propertyStyleI.close();

        	// label style
            RepositoryResult<Statement> labelStyleI = conn.getStatements(selected, FresnelCore.labelStyle, (Value) null, false);
        	while (labelStyleI.hasNext()) {
        		Statement labelStyleS = labelStyleI.next();
        		Value styleObj = labelStyleS.getObject();
        		out.addLabelStyle(Style.parse(styleObj));
        	}
        	labelStyleI.close();

        	// value style
            RepositoryResult<Statement> valueStyleI = conn.getStatements(selected, FresnelCore.valueStyle, (Value) null, false);
        	while (valueStyleI.hasNext()) {
        		Statement valueStyleS = valueStyleI.next();
        		Value styleObj = valueStyleS.getObject();
        		out.addValueStyle(Style.parse(styleObj));
        	}
        	valueStyleI.close();

        	// property format
            RepositoryResult<Statement> propertyFormatI = conn.getStatements(selected, FresnelCore.propertyFormat, (Value) null, false);
        	while (propertyFormatI.hasNext()) {
        		Statement propertyFormatS = propertyFormatI.next();
        		Resource formatObj = (Resource) propertyFormatS.getObject();
        		out.addPropertyFormat(FormatDescription.parse(in, formatObj));
        	}
        	propertyFormatI.close();

        	// label format
            RepositoryResult<Statement> labelFormatI = conn.getStatements(selected, FresnelCore.labelFormat, (Value) null, false);
        	while (labelFormatI.hasNext()) {
        		Statement labelFormatS = labelFormatI.next();
        		Resource formatObj = (Resource) labelFormatS.getObject();
        		out.addLabelFormat(FormatDescription.parse(in, formatObj));
        	}
        	labelFormatI.close();

        	// value format
            RepositoryResult<Statement> valueFormatI = conn.getStatements(selected, FresnelCore.valueFormat, (Value) null, false);
        	while (valueFormatI.hasNext()) {
        		Statement valueFormatS = valueFormatI.next();
        		Resource formatObj = (Resource) valueFormatS.getObject();
        		out.addValueFormat(FormatDescription.parse(in, formatObj));
        	}
        	valueFormatI.close();

        	conn.close();
        } catch (RepositoryException e) {
        	throw new UnresolvableException("Problem connecting to repository: " + e.getLocalizedMessage());
        }
        
        return out;
    }
    
    /**
     * Parse out domain information and add to the Format.
     * 
     * @param out The <code>Format</code> to modify
     * @param in The <code>Repository</code> containing configuration information
     * @param selected The <code>Resource</code> identifying the style
     * @param conf The <code>Configuration</code> parsing this format
     */
    protected static void parseDomain(Format out, Repository in, Resource selected, Configuration conf) throws RepositoryException {
    	RepositoryConnection conn = in.getConnection();
        RepositoryResult<Statement> domainsI = conn.getStatements(selected, FresnelCore.propertyFormatDomain, (Value) null, false);
    	while (domainsI.hasNext()) {
    		ISelector domain = null;
    		Value domainNode = domainsI.next().getObject();
    		if (domainNode instanceof Resource) {
    			if (((Resource) domainNode).equals(AllPropertiesSet.getSchemaResource())) {
    				domain = new AllPropertiesSelector();
    			} else {
    				// This is a property selector
    				domain = new PropertySelector((URI) domainNode);
    			}
    		} else if (domainNode instanceof Literal) {
    			Literal domainL = (Literal) domainNode;
    			// TODO: catch bad expressions?  throw exceptions?
    			if (domainL.getDatatype().equals(FresnelCoreTypes.fslSelector)) {
    				domain = new FSESelector(domainL.getLabel(), _fslContext, conf.getNamespaceMap());
    			} else if (domainL.getDatatype().equals(FresnelCoreTypes.fslSelector)) {
    				domain = new SPARQLSelector(domainL.getLabel(), conf.getNamespaces());
    			}
    		}
    		out.addDomain(domain);
    	}     
    	domainsI.close();
    	conn.close();
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String state = " [Format " + super.toString() + "]\n";
        state += "  " + this._identifier + "\n";
        state += "\n Domain(s):\n";
        state += this._domains;
        state += "\n Purpose(s):\n";
        state += this._purposes;
        state += "\n Property Type:\n";
        if (null != this._label) state += this._label;
        if (null != this._value) state += this._value;
        state += "\n Styling(s):\n";
        if (0 != this._resourceStyle.size()) state += this._resourceStyle + " [resource]\n";
        if (0 != this._propertyStyle.size()) state += this._propertyStyle + " [property]\n";
        if (0 != this._labelStyle.size()) state += this._labelStyle + " [label]\n";
        if (0 != this._valueStyle.size()) state += this._valueStyle + " [value]\n";
        state += "\n Content format(s):\n";
        if (0 != this._resourceFormat.size()) state += this._resourceFormat + " [resource]\n";
        if (0 != this._propertyFormat.size()) state += this._propertyFormat + " [property]\n";
        if (0 != this._labelFormat.size()) state += this._labelFormat + " [label]\n";
        if (0 != this._valueFormat.size()) state += this._valueFormat + " [value]\n";
        state += " ====== [end format]";
        return state;
    }
}
