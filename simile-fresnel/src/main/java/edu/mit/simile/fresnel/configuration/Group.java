/*
 * Created on Apr 5, 2005
 */
package edu.mit.simile.fresnel.configuration;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

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
import edu.mit.simile.fresnel.format.Format;
import edu.mit.simile.fresnel.format.FormatDescription;
import edu.mit.simile.fresnel.format.Style;
import edu.mit.simile.fresnel.selection.FSESelector;
import edu.mit.simile.fresnel.selection.ISelector;
import edu.mit.simile.fresnel.selection.Lens;
import edu.mit.simile.fresnel.selection.ParsingException;
import edu.mit.simile.fresnel.selection.SPARQLSelector;
import edu.mit.simile.fresnel.selection.TypeSelector;
import edu.mit.simile.fresnel.selection.UnresolvableException;
import edu.mit.simile.fresnel.util.DC;
import edu.mit.simile.fresnel.util.RDFList;
import edu.mit.simile.vocabularies.FresnelCore;
import edu.mit.simile.vocabularies.FresnelExtended;

/**
 * Represents a grouping of lenses and/or styles that will work
 * consistently with one another.
 * 
 * @author ryanlee
 */
public class Group extends FresnelResource {
	/**
	 * Resource defined in the schema.
	 */
	protected final static Resource _schemaResource = FresnelCore.Group;
	
	/**
	 * Context for determining FSL first location step type.
	 */
	protected final static short _fslContext = FSLPath.NODE_STEP;
	
	/**
	 * Resource identifying this group.
	 */
	protected Resource _identifier;
	
	/**
	 * Name for this group.
	 */
	protected String _title;
	
	/**
	 * Lenses that act as part of this group.
	 */
	private Set<Lens> _lenses;
	
	/**
	 * Lenses in this group that act as primaries.
	 */
	private Set<ISelector> _primaries;
	
	/**
	 * Formats that act as part of this group.
	 */
	private Set<Format> _formats;
	
	/**
	 * The styling for a resource
	 */
	private Set<Style> _resourceStyle;
	
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
	 * Links to a CSS stylesheet in a vector form.
	 */
	private Vector<String> _cssLinks;
	
	/**
	 * Empty constructor.
	 */
	public Group() {
		this._lenses = new HashSet<Lens>();
		this._formats = new HashSet<Format>();
		this._primaries = new HashSet<ISelector>();
		this._resourceStyle = new HashSet<Style>();
		this._propertyStyle = new HashSet<Style>();
		this._labelStyle = new HashSet<Style>();
		this._valueStyle = new HashSet<Style>();
		this._resourceFormat = new HashSet<FormatDescription>();
		this._propertyFormat = new HashSet<FormatDescription>();
		this._labelFormat = new HashSet<FormatDescription>();
		this._valueFormat = new HashSet<FormatDescription>();
		this._cssLinks = new Vector<String>();
	}
	
	/**
	 * Constructs a group with a <code>Resource</code> identifier.
	 * 
	 * @param id A <code>Resource</code> that is a group.
	 */
	public Group(Resource id) {
		this();
		this._identifier = id;
	}
	
	/**
	 * Retrieves the identifier of the group.
	 * 
	 * @return A <code>Resource</code>
	 */
	public Resource getIdentifier() {
		return this._identifier;
	}
	
	/**
	 * Retrieves a set of lenses associated with a group.
	 * 
	 * @return A <code>LensSet</code> of coordinated lenses.
	 */
	public Set<Lens> getLenses() {
		return this._lenses;
	}
	
	/**
	 * Retreives the set of primary classes associated with a group.
	 * 
	 * @return A <code>DomainSet</code> of resources considered primary.
	 */
	public Set<ISelector> getPrimaries() {
		return this._primaries;
	}
	
	/**
	 * Retrieves a set of formats associated with a group.
	 * 
	 * @return A <code>FormatSet</code> of coordinated formats.
	 */
	public Set<Format> getFormats() {
		return this._formats;
	}
	
	/**
	 * Retreives the set of default resource styles, applied to all group resources
	 * 
	 * @return A <code>StyleSet</code>
	 */
	public Set<Style> getResourceStyle() {
		return this._resourceStyle;
	}
	
	/**
	 * Retreives the set of default property styles, applied to all group properties
	 * 
	 * @return A <code>StyleSet</code>
	 */
	public Set<Style> getPropertyStyle() {
		return this._propertyStyle;
	}
	
	/**
	 * Retreives the set of default label styles, applied to all group properties
	 * 
	 * @return A <code>StyleSet</code>
	 */
	public Set<Style> getLabelStyle() {
		return this._labelStyle;
	}
	
	/**
	 * Retreives the set of default value styles, applied to all group properties
	 * 
	 * @return A <code>StyleSet</code>
	 */
	public Set<Style> getValueStyle() {
		return this._valueStyle;
	}
	
	/**
	 * Retrieves the format values associated with resources
	 * 
	 * @return A <code>FormatDescriptionSet</code>
	 */
	public Set<FormatDescription> getResourceFormat() {
		return this._resourceFormat;
	}
	
	/**
	 * Retrieves the format values associated with properties
	 * 
	 * @return A <code>FormatDescriptionSet</code>
	 */
	public Set<FormatDescription> getPropertyFormat() {
		return this._propertyFormat;
	}
	
	/**
	 * Retrieves the format values associated with labels
	 * 
	 * @return A <code>FormatDescriptionSet</code>
	 */
	public Set<FormatDescription> getLabelFormat() {
		return this._labelFormat;
	}
	
	/**
	 * Retrieves the format values associated with values
	 * 
	 * @return A <code>FormatDescriptionSet</code>
	 */
	public Set<FormatDescription> getValueFormat() {
		return this._valueFormat;
	}
	
	/**
	 * Returns the provided links to CSS stylesheets 
	 * 
	 * @return A <code>Vector</code> of <code>String</code>s
	 */
	public Vector<String> getCssLinks() {
		return this._cssLinks;
	}
	
	/**
	 * Retrieves whether this group has primary classes associated with it.
	 * 
	 * @return True if more than one primary class is set, false if not.
	 */
	public boolean hasPrimaries() {
		return (this._primaries.size() > 0);
	}
	
	/**
	 * Retrieves whether this group has any CSS stylesheets associated with it.
	 * 
	 * @return True if any stylesheet links are given, false if not.
	 */
	public boolean hasCssLinks() {
		return (this._cssLinks.size() > 0);
	}
	
	/**
	 * Returns the name of the group.
	 * 
	 * @return The <code>String</code> name
	 */
	public String getTitle() {
		String title = this._title;
		if (null == title) {
			if (getIdentifier() instanceof URI)
				title = ((URI) getIdentifier()).getLocalName();
			else
				title = "[untitled group]";
		}
		return title;
	}
	
	/**
	 * Sets the name for the group.
	 * 
	 * @param title A <code>String</code>
	 */
	public void setTitle(String title) {
		this._title = title;
	}
	
	/**
	 * Sets the bundle of lenses for a group.
	 * 
	 * @param set A <code>LensSet</code>
	 */
	public void setLenses(Set<Lens> set) {
		this._lenses = set;
	}
	
	/**
	 * Adds another lens to the existing bundle of lenses.
	 * 
	 * @param lens A <code>Lens</code>
	 */
	public void addLens(Lens lens) {
		this._lenses.add(lens);
	}
	
	/**
	 * Removes a lens from the existing bundle of lenses.
	 * 
	 * @param lens A <code>Lens</code>
	 */
	public void removeLens(Lens lens) {
		this._lenses.remove(lens);
	}
	
	/**
	 * Sets the bundle of formats for this group.
	 * 
	 * @param set A <code>FormatSet</code>
	 */
	public void setFormats(Set<Format> set) {
		this._formats = set;
	}
	
	/**
	 * Adds a format to the existing style bundle.
	 * 
	 * @param format A <code>Format</code>
	 */
	public void addFormat(Format format) {
		this._formats.add(format);
	}
	
	/**
	 * Removes a format from the existing style bundle.
	 * 
	 * @param format A <code>Format</code>
	 */
	public void removeFormat(Format format) {
		this._formats.remove(format);
	}
	
	/**
	 * Sets the primary classes for the group.
	 * 
	 * @param primaries A <code>DomainSet</code> of all primary classes for the group.
	 */
	public void setPrimaries(Set<ISelector> primaries) {
		this._primaries = primaries;
	}
	
	/**
	 * Adds another primary to the existing set of primaries.
	 * 
	 * @param primary An <code>ISelector</code>
	 */
	public void addPrimary(ISelector primary) {
		this._primaries.add(primary);
	}
	
	/**
	 * Removes a primary from the existing set of primaries.
	 * 
	 * @param primary An existing <code>ISelector</code>
	 */
	public void removePrimary(ISelector primary) {
		this._primaries.remove(primary);
	}
	
	/**
	 * Sets the styling for resources
	 * 
	 * @param style A <code>StyleSet</code>
	 */
	protected void setResourceStyle(Set<Style> style) {
		this._resourceStyle = style;
	}
	
	/**
	 * Sets the styling for properties
	 * 
	 * @param style A <code>StyleSet</code>
	 */
	protected void setPropertyStyle(Set<Style> style) {
		this._propertyStyle = style;
	}
	
	/**
	 * Sets the styling for labels
	 * 
	 * @param style A <code>StyleSet</code>
	 */
	protected void setLabelStyle(Set<Style> style) {
		this._labelStyle = style;
	}
	
	/**
	 * Sets the styling for values
	 * 
	 * @param style A <code>StyleSet</code>
	 */
	protected void setValueStyle(Set<Style> style) {
		this._valueStyle = style;
	}
	
	/**
	 * Adds a styling for resources
	 * 
	 * @param style A <code>Style</code>
	 */
	protected void addResourceStyle(Style style) {
		this._resourceStyle.add(style);
	}
	
	/**
	 * Adds a styling for properties
	 * 
	 * @param style A <code>Style</code>
	 */
	protected void addPropertyStyle(Style style) {
		this._propertyStyle.add(style);
	}
	
	/**
	 * Adds a styling for labels
	 * 
	 * @param style A <code>Style</code>
	 */
	protected void addLabelStyle(Style style) {
		this._labelStyle.add(style);
	}
	
	/**
	 * Adds a styling for values
	 * 
	 * @param style A <code>Style</code>
	 */
	protected void addValueStyle(Style style) {
		this._valueStyle.add(style);
	}
	
	/**
	 * Sets the formatting for resources
	 * 
	 * @param fd A <code>FormatDescriptionSet</code>
	 */
	protected void setResourceFormat(Set<FormatDescription> fd) {
		this._resourceFormat = fd;
	}
	
	/**
	 * Sets the formatting for properties
	 * 
	 * @param fd A <code>FormatDescriptionSet</code>
	 */
	protected void setPropertyFormat(Set<FormatDescription> fd) {
		this._propertyFormat = fd;
	}
	
	/**
	 * Sets the formatting for labels
	 * 
	 * @param fd A <code>FormatDescriptionSet</code>
	 */
	protected void setLabelFormat(Set<FormatDescription> fd) {
		this._labelFormat = fd;
	}
	
	/**
	 * Sets the formatting for values
	 * 
	 * @param fd A <code>FormatDescriptionSet</code>
	 */
	protected void setValueFormat(Set<FormatDescription> fd) {
		this._valueFormat = fd;
	}
	
	/**
	 * Adds a formatting for resources
	 * 
	 * @param format A <code>FormatDescription</code>
	 */
	protected void addResourceFormat(FormatDescription format) {
		this._resourceFormat.add(format);
	}    
	
	/**
	 * Adds a formatting for properties
	 * 
	 * @param format A <code>FormatDescription</code>
	 */
	protected void addPropertyFormat(FormatDescription format) {
		this._propertyFormat.add(format);
	}
	
	/**
	 * Adds a formatting for labels
	 * 
	 * @param format A <code>FormatDescription</code>
	 */
	protected void addLabelFormat(FormatDescription format) {
		this._labelFormat.add(format);
	}
	
	/**
	 * Adds a formatting for values
	 * 
	 * @param format A <code>FormatDescription</code>
	 */
	protected void addValueFormat(FormatDescription format) {
		this._valueFormat.add(format);
	}
	
	/**
	 * Adds a CSS stylesheet link.
	 * 
	 * @param link A <code>String</code> URL
	 */
	protected void addCssLink(String link) {
		this._cssLinks.add(link);
	}
	
	/**
	 * Parses a model based on the resource into a group object.
	 * 
	 * @param in The source <code>Repository</code>
	 * @param selected The identifying <code>Resource</code>
	 * @param conf The base <code>Configuration</code>
	 * @return A new <code>Group</code>
	 * @throws UnresolvableException If too many values are found for a property where only a certain number are allowed
	 * @throws ParsingException If semantic errors are found in the group configuration
	 */
	public static Group parse(Repository in, Resource selected, Configuration conf) throws UnresolvableException, ParsingException {
		Group out = new Group(selected);
		try {
			RepositoryConnection conn = in.getConnection();

			// name for the group
			RepositoryResult<Statement> titleI = conn.getStatements(selected, DC.title, (Value) null, false);
			while (titleI.hasNext()) {
				Statement titleS = titleI.next();
				Literal titleObj = (Literal) titleS.getObject();
				out.setTitle(titleObj.getLabel());
			}
			titleI.close();

			// resource style
			RepositoryResult<Statement> resourceStyleI = conn.getStatements(selected, FresnelCore.resourceStyle, (Value) null, false);
			while (resourceStyleI.hasNext()) {
				Statement resourceStyleS = resourceStyleI.next();
				Value styleObj = resourceStyleS.getObject();
				out.addResourceStyle(Style.parse(styleObj));
			}
			resourceStyleI.close();

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

			// resource format
			RepositoryResult<Statement> resourceFormatI = conn.getStatements(selected, FresnelCore.resourceFormat, (Value) null, false);
			while (resourceFormatI.hasNext()) {
				Statement resourceFormatS = resourceFormatI.next();
				Resource formatObj = (Resource) resourceFormatS.getObject();
				out.addResourceFormat(FormatDescription.parse(in, formatObj));
			}
			resourceFormatI.close();

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
				Resource formatObj = valueFormatS.getSubject();
				out.addValueFormat(FormatDescription.parse(in, formatObj));
			}
			valueFormatI.close();

			// EXTENDED: stylesheet link
			RepositoryResult<Statement> cssLinkI = conn.getStatements(selected, FresnelExtended.stylesheetLink, (Value) null, false);
			while (cssLinkI.hasNext()) {
				Statement cssLinkS = cssLinkI.next();
				Value linkObj = cssLinkS.getObject();
				if (linkObj instanceof Literal)
					out.addCssLink(((Literal) linkObj).getLabel());
				else
					out.addCssLink(((URI) linkObj).toString());
			}
			cssLinkI.close();

			RepositoryResult<Statement> groupPrimaries = conn.getStatements(selected, FresnelCore.primaryClasses, (Value) null, false);
			if (groupPrimaries.hasNext()) {
				Statement primaryStmt = groupPrimaries.next();
				if (groupPrimaries.hasNext()) throw new UnresolvableException("Too many fresnel:primaryClass predicates used, only one expected.");
				Value primaryObj = primaryStmt.getObject();
				if (primaryObj instanceof Resource && RDFList.isRDFList(in, (Resource) primaryObj)) {
					List primaries = new RDFList(in, (Resource) primaryObj).asJavaList();
					Iterator primaryDomains = primaries.iterator();
					while (primaryDomains.hasNext()) {
						Value domainNode = (Value) primaryDomains.next();
						ISelector domain = null;
						if (domainNode instanceof Resource) {
							// This is a type selector
							domain = new TypeSelector((Resource) domainNode);
						} else if (domainNode instanceof Literal) {
							Literal domainL = (Literal) domainNode;
							// TODO: catch bad expressions?  throw exceptions?
							if (domainL.getDatatype().equals(FresnelCoreTypes.fslSelector)) {
								domain = new FSESelector(domainL.getLabel(), _fslContext, conf.getNamespaceMap());
							} else if (domainL.getDatatype().equals(FresnelCoreTypes.sparqlSelector)) {
								domain = new SPARQLSelector(domainL.getLabel(), conf.getNamespaces());
							}                        
						} else {
							throw new ParsingException("Could not read member fresnel:primaryClass rdf:List.");                        
						}
						out.addPrimary(domain);
					}
				} else {
					throw new ParsingException("Could not read object of fresnel:primaryClass predicate, expecting an rdf:List.");
				}
			}
			groupPrimaries.close();
			conn.close();
		} catch (RepositoryException e) {
			throw new UnresolvableException("Problem connecting to repository: " + e.getLocalizedMessage());
		}

		return out;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String state = " [Group " + super.toString() + "]\n";
		state += " " + this._identifier + "\n";
		state += this._primaries;
		state += "  " + this._lenses;
		state += this._formats;
		if (0 != this._resourceStyle.size()) state += this._resourceStyle + "  [resource]\n";
		if (0 != this._propertyStyle.size()) state += this._propertyStyle + "  [property]\n";
		if (0 != this._labelStyle.size()) state += this._labelStyle + "  [label]\n";
		if (0 != this._valueStyle.size()) state += this._valueStyle + "  [value]\n";
		state += "\n Content format(s):\n";
		if (0 != this._resourceFormat.size()) state += this._resourceFormat + " [resource]\n";
		if (0 != this._propertyFormat.size()) state += this._propertyFormat + " [property]\n";
		if (0 != this._labelFormat.size()) state += this._labelFormat + " [label]\n";
		if (0 != this._valueFormat.size()) state += this._valueFormat + " [value]\n";
		state += " ====== [end group]";
		return state;
	}
}
