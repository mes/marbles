/*
 * Created on Mar 16, 2005
 */
package edu.mit.simile.fresnel.configuration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import fr.inria.jfresnel.fsl.FSLNSResolver;

import edu.mit.simile.fresnel.facets.Facet;
import edu.mit.simile.fresnel.facets.FacetSet;
import edu.mit.simile.fresnel.format.ClassFormat;
import edu.mit.simile.fresnel.format.Format;
import edu.mit.simile.fresnel.format.InstanceFormat;
import edu.mit.simile.fresnel.purpose.Purpose;
import edu.mit.simile.fresnel.results.ResultConstants;
import edu.mit.simile.fresnel.results.Selection;
import edu.mit.simile.fresnel.selection.ISelector;
import edu.mit.simile.fresnel.selection.InstanceLens;
import edu.mit.simile.fresnel.selection.InstanceSelector;
import edu.mit.simile.fresnel.selection.InvalidResultSetException;
import edu.mit.simile.fresnel.selection.Lens;
import edu.mit.simile.fresnel.selection.ParsingException;
import edu.mit.simile.fresnel.selection.ResourceNotFoundException;
import edu.mit.simile.fresnel.selection.TypeSelector;
import edu.mit.simile.fresnel.selection.UnresolvableException;
import edu.mit.simile.vocabularies.Facets;
import edu.mit.simile.vocabularies.FresnelCore;
import edu.mit.simile.vocabularies.Namespaces;

/**
 * Takes a Fresnel configuration RDF model and makes it useful for
 * selecting and styling other RDF models.
 * 
 * @author ryanlee
 */
public class Configuration implements ResultConstants {
	/**
	 * URI for the default group, if no groups exist in the configuration
	 */
	private final String DEFAULT_GROUP_URI = "urn:simile.mit.edu:fresnel:defaultgroup";

	/**
	 * Resource for default group URI
	 */
	private final Resource DEFAULT_GROUP = new URIImpl(DEFAULT_GROUP_URI);

	/**
	 * URI for the default group, if no groups exist in the configuration
	 */
	private final String DEFAULT_FORMAT_URI = "urn:simile.mit.edu:fresnel:defaultformat";

	/**
	 * Resource for default group URI
	 */
	private final Resource DEFAULT_FORMAT = new URIImpl(DEFAULT_FORMAT_URI);

	/**
	 * Original RDF model of the configuration
	 */
	private Repository _source;

	/**
	 * RDF model containing ontology information such as subclass and label relationships.
	 */
	private Repository _ontologies;

	/**
	 * List of lenses
	 */
	private Set<Lens> _lenses;

	/**
	 * List of styles
	 */
	private Set<Format> _formats;

	/**
	 * EXTENSION hash of all facet sets
	 */
	private FacetSetHashMap _facets;

	/**
	 * List of lens groups
	 */
	private GroupHashMap _groups;

	/**
	 * Set of groups in increasing string order
	 */
	private Set<Resource> _groupOrder;

	/**
	 * List of lens to resource matches
	 */
	private LensMatchHashMap _lensMatches;

	/**
	 * Hash of class domain lenses based on the instance
	 */
	private LensMatchHashMap _instanceLensMatches;

	/**
	 * Hash of class domain lenses based on the domain
	 */
	private LensMatchHashMap _classLensMatches;

	/**
	 * Hash of resource format matches to resources based on the domain
	 */
	private HashMap<Resource, ResourceFormatMatchSet> _formatResourceMatches;

	/**
	 * Hash of property format matches based on the domain
	 */
	private HashMap<Resource, PropertyFormatMatchSet> _formatPropertyMatches;

	/**
	 * Group used in selection, should also be used for formatting.
	 */
	private Group _mainGroup;

	/**
	 * Hash of everything by config model resource
	 */
	private HashMap<Resource, Object> _all;

	/**
	 * Gathers warnings for reporting to the user
	 */
	private WarningGatherer _warnings;

	/**
	 * Map of namespaces to abbreviations for FSL.
	 */
	private FSLNSResolver _nsmap;

	/**
	 * String of namespaces for SPARQL queries.
	 */
	private String _namespaces;

	/**
	 * Constructor, initializer based on RDF graph of configuration; needs other data,
	 * cannot be called on its own.
	 */
	private Configuration(Repository source) throws ParsingException, UnresolvableException {
		this._source = source;
		this._lenses = new HashSet<Lens>();
		this._formats = new HashSet<Format>();
		this._facets = new FacetSetHashMap();
		this._groupOrder = new HashSet<Resource>();
		// TODO: find a way to set initial capacity correctly
		this._groups = new GroupHashMap();
		this._lensMatches = new LensMatchHashMap();
		this._instanceLensMatches = new LensMatchHashMap();
		this._classLensMatches = new LensMatchHashMap();
		this._formatResourceMatches = new HashMap<Resource, ResourceFormatMatchSet>();
		this._formatPropertyMatches = new HashMap<Resource, PropertyFormatMatchSet>();
		this._all = new HashMap<Resource, Object>();
		this._warnings = new WarningGatherer();
		this._nsmap = new FSLNSResolver();
		this.parse();
	}

	/**
	 * Constructs a Configuration object based on an RDF model of configuration and a model
	 * of ontology data.
	 * 
	 * @param source The source <code>Model</code>
	 * @param ontologies The ontology source <code>Model</code>
	 * @throws ParsingException If errors are encountered in the configuration semantics that
	 *                          do not conform to the Fresnel ontology
	 * @throws UnresolvableException If errors are encountered in the number of options made
	 *                               available for certain properties, leading to a choice this
	 *                               code is not qualified to make
	 */
	public Configuration(Repository source, Repository ontologies) throws ParsingException, UnresolvableException {
		this(source);
		this._ontologies = ontologies;
	}

	/**
	 * Internal method to accrue warnings
	 * 
	 * @param warning An <code>Exception</code> to gather
	 */
	private void gather(Exception warning) {
		this._warnings.addWarning(warning);
	}

	/**
	 * Whether any warnings were accrued
	 * 
	 * @return True if warnings were encountered, false if not
	 */
	public boolean hasWarnings() {
		return (this._warnings.size() > 0);
	}

	/**
	 * Retrieve the warnings as a string message
	 * 
	 * @return A <code>String</code> of accrued warnings.
	 */
	public String getWarningsString() {
		return this._warnings.toString();
	}

	/**
	 * Parses a model into the Configuration's internal structure, based on data supplied
	 * to the constructor.  Heavy lifter.
	 * 
	 * @throws ParsingException If errors are encountered in the configuration semantics that
	 *                          do not conform to the Fresnel ontology
	 * @throws UnresolvableException If errors are encountered in the number of options made
	 *                               available for certain properties, leading to a choice this
	 *                               code is not qualified to make
	 */
	private void parse() throws ParsingException, UnresolvableException {
		try {
			RepositoryConnection conn = this._source.getConnection();

			// EXTENSION for FSL namespace mapping - not part of core Fresnel vocabulary
			// Namespaces for SPARQL are put into a String
			StringBuffer namespaces = new StringBuffer();
			RepositoryResult<Statement> it = conn.getStatements((Resource) null, Namespaces.abbreviated, (Value) null, false);
			while (it.hasNext()) {
				Statement nsStatement = (Statement) it.next();
				URI ns = (URI) nsStatement.getSubject();
				Literal abbrev = (Literal) nsStatement.getObject();
				this._nsmap.addPrefixBinding(abbrev.getLabel(), ns.toString());
				namespaces.append("prefix ");
				namespaces.append(abbrev.getLabel() + ":");
				namespaces.append("<" + ns.toString() + ">");
			}
			it.close();
			
			this._namespaces = namespaces.toString();

			it = conn.getStatements((Resource) null, FresnelCore.instanceLensDomain, (Value) null, false);
			while (it.hasNext()) {
				try {
					Statement lensResourceStatement = (Statement) it.next();
					InstanceLens lens = new InstanceLens(this._source, lensResourceStatement.getSubject(), this);
					addInstanceLens(lens);
					if (!this._all.containsKey(lens.getIdentifier())) addLens(lens);
				} catch (ParsingException e) {
					gather(e);
				} catch (UnresolvableException u) {
					gather(u);
				}
			}
			it.close();

			it = conn.getStatements((Resource) null, FresnelCore.classLensDomain, (Value) null, false);
			while (it.hasNext()) {
				try {
					Statement lensResourceStatement = (Statement) it.next();
					Lens lens = new Lens(this._source, lensResourceStatement.getSubject(), this);
					if (!this._all.containsKey(lens.getIdentifier())) addLens(lens);
				} catch (ParsingException e) {
					gather(e);
				} catch (UnresolvableException u) {
					gather(u);
				}
			}
			it.close();

			it = conn.getStatements((Resource) null, FresnelCore.propertyFormatDomain, (Value) null, false);
			while (it.hasNext()) {
				try {
					Statement formatResourceStatement = (Statement) it.next();
					Format format = Format.parse(this._source, formatResourceStatement.getSubject(), this);
					if (!this._all.containsKey(format.getIdentifier())) addPropertyFormat(format);
				} catch (ParsingException e) {
					gather(e);
				} catch (UnresolvableException u) {
					gather(u);
				}
			}
			it.close();

			it = conn.getStatements((Resource) null, FresnelCore.instanceFormatDomain, (Value) null, false);
			while (it.hasNext()) {
				try {
					Statement formatResourceStatement = (Statement) it.next();
					InstanceFormat format = (InstanceFormat) InstanceFormat.parse(this._source, formatResourceStatement.getSubject(), this);
					if (!this._all.containsKey(format.getIdentifier())) addInstanceFormat(format);
				} catch (ParsingException e) {
					gather(e);
				} catch (UnresolvableException u) {
					gather(u);
				}
			}
			it.close();

			it = conn.getStatements((Resource) null, FresnelCore.classFormatDomain, (Value) null, false);
			while (it.hasNext()) {
				try {
					Statement formatResourceStatement = (Statement) it.next();
					ClassFormat format = (ClassFormat) ClassFormat.parse(this._source, formatResourceStatement.getSubject(), this);
					if (!this._all.containsKey(format.getIdentifier())) addClassFormat(format);
				} catch (ParsingException e) {
					gather(e);
				} catch (UnresolvableException u) {
					gather(u);
				}
			}
			it.close();

			// EXTENSION for declaring facets - not part of core Fresnel vocabulary
			this._facets.clear();
			it = conn.getStatements((Resource) null, Facets.facets, (Value) null, false);
			while (it.hasNext()) {
				try {
					Statement facetStatement = (Statement) it.next();
					FacetSet facetset = FacetSet.parse(this._source, facetStatement.getSubject());
					if (!this._all.containsKey(facetset.getIdentifier())) addFacetSet(facetset);
				} catch (Exception e) {
					gather(e);
				}
			}
			it.close();

			// also catch even if it only has to do with hiding
			it = conn.getStatements((Resource) null, Facets.hides, (Value) null, false);
			while (it.hasNext()) {
				try {
					Statement facetStatement = (Statement) it.next();
					FacetSet facetset = FacetSet.parse(this._source, facetStatement.getSubject());
					if (!this._all.containsKey(facetset.getIdentifier())) addFacetSet(facetset);
				} catch (Exception e) {
					gather(e);
				}
			}
			it.close();

			// get a list of unique resources that are a group by direct inference
			it = conn.getStatements((Resource) null, FresnelCore.group, (Value) null, false);
			while (it.hasNext()) {
				try {
					Statement groupStatement = (Statement) it.next();
					Resource groupMember = groupStatement.getSubject();
					Resource groupResource = null;
					Value groupValue = groupStatement.getObject();
					if (groupValue instanceof Resource)
						groupResource = (Resource) groupValue;
					else
						throw new UnresolvableException("Group referred to is not a resource: " + groupValue);
					boolean exists = this._groups.containsKey(groupResource);
					Group group = exists ?
							(Group) this._groups.get(groupResource) :
								Group.parse(this._source, groupResource, this);

							try {
								group.addLens(lensLookup(groupMember));
							} catch (ResourceNotFoundException e) {
								try {
									group.addFormat(formatLookup(groupMember));
								} catch (ResourceNotFoundException re) {
									throw new ParsingException("Could not find useful parsing information for resource: " + ((URI) groupMember).toString());
								}
							}

							// Group only needs to be parsed once, don't add it if it already exists
							if (!exists) {
								addGroup(group);
							}
				} catch (ParsingException e) {
					gather(e);
				} catch (UnresolvableException u) {
					gather(u);
				}
			}
			it.close();
			conn.close();
		} catch (RepositoryException e) {
			throw new UnresolvableException("Problem connecting to repository: " + e.getLocalizedMessage());
		}
	}

	/**
	 * Internal method for adding instance lenses consistently.
	 * 
	 * @param lens <code>InstanceLens</code> to add to configuration
	 */
	private void addInstanceLens(Lens lens) {
		this._lenses.add(lens);
		this._all.put(lens.getIdentifier(), lens);

		for (Iterator<ISelector> di = lens.getDomainSet().iterator(); di.hasNext(); ) {
			ISelector domain = di.next();
			// TODO other valid selector types may need to be integrated, such as FSE/SPARQL selectors
			if (domain instanceof InstanceSelector) {
				InstanceSelector is = (InstanceSelector) domain;
				LensMatchSet lms = this._instanceLensMatches.getMatch(is.getInstance());
				if (null == lms) {
					lms = new LensMatchSet(is.getInstance());
				}
				lms.add(lens);
				this._instanceLensMatches.putMatch(is.getInstance(), lms);
			}
		}
	}

	/**
	 * Internal method for adding lenses consistently.
	 * 
	 * @param lens <code>Lens</code> to add to configuration.
	 */
	private void addLens(Lens lens) {
		this._lenses.add(lens);
		this._all.put(lens.getIdentifier(), lens);

		for (Iterator<ISelector> di = lens.getDomainSet().iterator(); di.hasNext(); ) {
			TypeSelector ts = (TypeSelector) di.next();
			LensMatchSet lms = this._classLensMatches.getMatch(ts.getType());
			if (null == lms) {
				lms = new LensMatchSet(ts.getType());
			}
			lms.add(lens);
			this._classLensMatches.putMatch(ts.getType(), lms);
		}
	}

	/**
	 * Internal method for adding formats consistently.
	 * 
	 * @param style <code>Format</code> to add to configuration.
	 */
	private void addPropertyFormat(Format format) {
		this._formats.add(format);
		this._all.put(format.getIdentifier(), format);        
	}

	/**
	 * Internal method for adding instance formats consistently.
	 * 
	 * @param style <code>Format</code> to add to configuration.
	 */
	private void addInstanceFormat(Format format) {
		this._formats.add(format);
		this._all.put(format.getIdentifier(), format);        
	}

	/**
	 * Internal method for adding class formats consistently.
	 * 
	 * @param style <code>Format</code> to add to configuration.
	 */
	private void addClassFormat(Format format) {
		this._formats.add(format);
		this._all.put(format.getIdentifier(), format);        
	}

	/**
	 * EXTENSION for declaring facets
	 * 
	 * @param fs A set of facets
	 */
	private void addFacetSet(FacetSet fs) {
		if (fs.isForAll())
			this._facets.setDefaultSet(fs);
		else {
			for (Iterator<Resource> ti = fs.getForTypes(); ti.hasNext(); ) {
				this._facets.putFacetSet(ti.next(), fs);
			}
		}
		this._all.put(fs.getIdentifier(), fs);
	}

	/**
	 * Internal method for adding groups consistently.
	 * 
	 * @param group <code>Group</code> to add to configuration.
	 */
	private void addGroup(Group group) {
		this._groups.put(group.getIdentifier(), group);
		this._groupOrder.add(group.getIdentifier());
		this._all.put(group.getIdentifier(), group);        
	}

	/**
	 * Selects based on lens matches, outputs an intermediate XML tree.
	 * 
	 * @param in The data <code>Repository</code>
	 * @return A subgraph <code>Selection</code>
	 * @throws ParserConfigurationException When a problem with the XML parsing code is encountered
	 * @throws NoResultsException When no results can be generated from the given configuration and data
	 */
	public Selection select(Repository in, String langPref) throws NoResultsException, ParserConfigurationException {
		Selection answer = null;

		Group group = null;
		if (this._groups.size() > 0) {
			// this just takes the first group if it's undefined;
			// a smarter way to do it would be to do some fast analysis on which
			// group matches the most resources
			for (Iterator<Resource> groupIt = this._groupOrder.iterator(); groupIt.hasNext(); ) {
				Resource groupRes = (Resource) groupIt.next();
				Group groupCandidate = this._groups.getGroup(groupRes);
				if (groupCandidate.getLenses().size() > 0) {
					group = groupCandidate;
					break;
				}
			}
		}

		if (null == group) {
			// put everything in a default group if no groups containing lenses exists
			group = new Group(DEFAULT_GROUP);
			for (Lens l : this._lenses) {
				group.addLens(l);
			}

			for (Format f : this._formats) {
				group.addFormat(f);
			}
		}

		answer = select(in, group, langPref);
		return answer;
	}

	/**
	 * Selects based on lens matches and a group of interest, outputs an intermediate XML tree.
	 * 
	 * @param in The input data <code>Model</code>
	 * @param grouping The <code>Group</code> of lenses and styles to use
	 * @return A subgraph <code>Selection</code>
	 * @throws ParserConfigurationException When a problem with the XML parsing code is encountered
	 * @throws NoResultsException When no results can be generated from the given configuration and data
	 */
	public Selection select(Repository in, Group grouping, String langPref) throws ParserConfigurationException, NoResultsException {
		this._mainGroup = grouping;
		// immediate exception if no lenses exist
		if (this._lenses.size() == 0)
			throw new NoResultsException("There are no lenses defined!");

		// execute every lens for pre-selection, make a lensMatch for every resource
		// that gets selected.  keep adding to the lensMatch per resource until all
		// matches are found (all lenses are executed).
		Vector<Resource> validStarts = new Vector<Resource>();
		if (grouping.hasPrimaries()) {
			Iterator<ISelector> primariesIt = grouping.getPrimaries().iterator();
			while (primariesIt.hasNext()) {
				ISelector primary = primariesIt.next();
				if (primary.canSelectResources()) {
					try {
						Iterator<Resource> valids = primary.selectResources(in);
						while (valids.hasNext()) {
							validStarts.add(valids.next());
						}
					} catch (InvalidResultSetException e) {
						// not a valid exception at this point
					}
				}
			}
		} else {
			validStarts = null;
		}

		Iterator<Lens> li = grouping.getLenses().iterator();
		while (li.hasNext()) {
			Lens lens = li.next();
			Iterator<ISelector> di = lens.getDomainSet().iterator();
			while (di.hasNext()) {
				ISelector select = di.next();
				if (select.canSelectResources()) {
					try {
						Iterator<Resource> ri = select.selectResources(in);
						while (ri.hasNext()) {
							Resource res = (Resource) ri.next();
							if (null == validStarts || (null != validStarts && validStarts.contains(res))) {
								if (this._lensMatches.containsKey(res)) {
									LensMatchSet match = this._lensMatches.getMatch(res);
									match.add(lens);
								} else {
									LensMatchSet match = new LensMatchSet(res);
									match.add(lens);
									this._lensMatches.putMatch(res, match);
								}
							}
						}
					} catch (InvalidResultSetException e) { 
						//
					}
				}
			}
		}

		// exception if no matches
		if (this._lensMatches.size() == 0)
			throw new NoResultsException("No lenses matching the data could be found.");

		Selection answer = new Selection(this);
		answer.setLangPref(langPref);

		Iterator<?> resources = this._lensMatches.keySet().iterator();
		while (resources.hasNext()) {
			Resource subject = (Resource) resources.next();
			LensMatchSet match = this._lensMatches.getMatch(subject);
			Lens best = match.topMatch();
			answer.addPrimary(answer.applyLens(grouping, in, best, subject, 0, MAXIMUM_LENS_DEPTH));
		}

		return answer;
	}

	/**
	 * Select specific resource based on lens matches, outputs a Fresnel-specific Selection.
	 * 
	 * @param in The data <code>Repository</code>
	 * @param focus The specific <code>Resource</code> to use Fresnel on
	 * @return A subgraph <code>Selection</code>
	 * @throws ParserConfigurationException When a problem with the XML parsing code is encountered
	 * @throws NoResultsException When no results can be generated from the given configuration and data
	 */
	public Selection select(Repository in, Resource focus, String langPref) throws NoResultsException, ParserConfigurationException {
		Selection answer = null;

		Group group = null;
		if (this._groups.size() > 0) {
			// this just takes the first group if it's undefined;
			// a smarter way to do it would be to do some fast analysis on which
			// group matches the most resources
			for (Iterator<Resource> groupIt = this._groupOrder.iterator(); groupIt.hasNext(); ) {
				Resource groupRes = (Resource) groupIt.next();
				Group groupCandidate = this._groups.getGroup(groupRes);
				if (groupCandidate.getLenses().size() > 0) {
					group = groupCandidate;
					break;
				}
			}
		}

		if (null == group) {
			// put everything in a default group if no groups containing lenses exists
			group = new Group(DEFAULT_GROUP);
			for (Lens l : this._lenses) {
				group.addLens(l);
			}

			for (Format f : this._formats) {
				group.addFormat(f);
			}
		}

		answer = select(in, focus, group, langPref);
		return answer;
	}

	/**
	 * Internal selection mechanism, returns matches.
	 */
	private LensMatchSet _select(Repository in, Resource focus, Group grouping) throws ParserConfigurationException, NoResultsException {
		this._mainGroup = grouping;
		// immediate exception if no lenses exist
		if (this._lenses.size() == 0)
			throw new NoResultsException("There are no lenses defined!");

		// execute every lens for pre-selection, make a lensMatch for every resource
		// that gets selected.  keep adding to the lensMatch per resource until all
		// matches are found (all lenses are executed).
		Vector<Resource> validStarts = new Vector<Resource>();
		if (grouping.hasPrimaries()) {
			Iterator<ISelector> primariesIt = grouping.getPrimaries().iterator();
			while (primariesIt.hasNext()) {
				ISelector primary = primariesIt.next();
				if (primary.canSelectResources()) {
					try {
						Iterator<Resource> valids = primary.selectResources(in);
						while (valids.hasNext()) {
							validStarts.add(valids.next());
						}
					} catch (InvalidResultSetException e) {
						// not a valid exception at this point
					}
				}
			}
		} else {
			validStarts = null;
		}

		LensMatchSet match = null;

		Iterator<Lens> li = grouping.getLenses().iterator();
		while (li.hasNext()) {
			Lens lens = li.next();
			Iterator<ISelector> di = lens.getDomainSet().iterator();
			while (di.hasNext()) {
				ISelector select = di.next();
				if (select.canSelectResources()) {
					if (select.canSelect(in, focus)) {
						if (null == validStarts || (null != validStarts && validStarts.contains(focus))) {
							if (this._lensMatches.containsKey(focus)) {
								match = this._lensMatches.getMatch(focus);
								match.add(lens);
							} else {
								match = new LensMatchSet(focus);
								match.add(lens);
								this._lensMatches.putMatch(focus, match);
							}
						}
					}
				}
			}
		}

		return match;
	}	

	/**
	 * Selects based on lens matches and a group of interest and a lens purpose, outputs an intermediate XML tree.
	 * 
	 * @param in The input data <code>Model</code>
	 * @param focus The specific <code>Resource</code> to use Fresnel on
	 * @param grouping The <code>Group</code> of lenses and styles to use
	 * @param purpose The <code>Purpose</code> of the lens
	 * @return A subgraph <code>Selection</code>
	 * @throws ParserConfigurationException When a problem with the XML parsing code is encountered
	 * @throws NoResultsException When no results can be generated from the given configuration and data
	 */
	public Selection select(Repository in, Resource focus, Group grouping, Purpose purpose, String langPref) throws ParserConfigurationException, NoResultsException {
		if (null == purpose)
			return select(in, focus, grouping, langPref);

		LensMatchSet match = _select(in, focus, grouping);

		// exception if no matches
		if (null == match)
			throw new NoResultsException("No lenses matching the data could be found.");

		Selection answer = new Selection(this);
		answer.setLangPref(langPref);

		Lens best = match.topMatch();

		// check purposes
		for (Iterator<Lens> pli = match.lensIterator(); pli.hasNext(); ) {
			Lens potential = pli.next();
			if (potential.hasPurpose(purpose)) {
				best = potential;
				break;
			}
		}
		answer.addPrimary(answer.applyLens(grouping, in, best, focus, 0, MAXIMUM_LENS_DEPTH));

		return answer;
	}
	
	/**
	 * Selects based on lens matches and a group of interest and a lens purpose, outputs an intermediate XML tree.
	 * 
	 * @param in The input data <code>Model</code>
	 * @param focus The specific <code>Resource</code> to use Fresnel on
	 * @param grouping The <code>Group</code> of lenses and styles to use
	 * @param purpose The <code>Purpose</code> of the lens
	 * @return A subgraph <code>Selection</code>
	 * @throws ParserConfigurationException When a problem with the XML parsing code is encountered
	 * @throws NoResultsException When no results can be generated from the given configuration and data
	 */
	public Selection select(Repository in, Resource focus, Purpose purpose, String langPref) throws ParserConfigurationException, NoResultsException {
		if (null == purpose)
			return select(in, focus, langPref);

		Group group = null;
		if (this._groups.size() > 0) {
			// this just takes the first group if it's undefined;
			// a smarter way to do it would be to do some fast analysis on which
			// group matches the most resources
			for (Iterator<Resource> groupIt = this._groupOrder.iterator(); groupIt.hasNext(); ) {
				Resource groupRes = (Resource) groupIt.next();
				Group groupCandidate = this._groups.getGroup(groupRes);
				if (groupCandidate.getLenses().size() > 0) {
					group = groupCandidate;
					break;
				}
			}
		}

		if (null == group) {
			// put everything in a default group if no groups containing lenses exists
			group = new Group(DEFAULT_GROUP);
			for (Lens l : this._lenses) {
				group.addLens(l);
			}

			for (Format f : this._formats) {
				group.addFormat(f);
			}
		}
		
		return select(in, focus, group, purpose, langPref);
	}	

	/**
	 * Selects based on lens matches and a group of interest, outputs an intermediate XML tree.
	 * 
	 * @param in The input data <code>Model</code>
	 * @param focus The specific <code>Resource</code> to use Fresnel on
	 * @param grouping The <code>Group</code> of lenses and styles to use
	 * @return A subgraph <code>Selection</code>
	 * @throws ParserConfigurationException When a problem with the XML parsing code is encountered
	 * @throws NoResultsException When no results can be generated from the given configuration and data
	 */
	public Selection select(Repository in, Resource focus, Group grouping, String langPref) throws ParserConfigurationException, NoResultsException {
		LensMatchSet match = _select(in, focus, grouping);

		// exception if no matches
		if (null == match)
			throw new NoResultsException("No lenses matching the data could be found.");

		Selection answer = new Selection(this);
		answer.setLangPref(langPref);
		
		Lens best = match.topMatch();
		answer.addPrimary(answer.applyLens(grouping, in, best, focus, 0, MAXIMUM_LENS_DEPTH));

		return answer;
	}

	/**
	 * Formats the subgraph selected by a select() call according to Fresnel formatting
	 * configuration.
	 * 
	 * @param in The data <code>Repository</code>
	 * @param select The previously generated <code>Selection</code>
	 * @return A <code>Selection</code> with formatting information.
	 */
	public Selection format(Repository in, Selection select) {
		Group grouping = this._mainGroup;
		// RESOURCES
		// execute every format for pre-selection, make a formatMatch for every resource
		// that gets selected.  keep adding to the lensMatch per resource until all
		// matches are found (all formats are executed).
		Iterator<Format> fi = grouping.getFormats().iterator();
		Format defaultFormat = new Format(DEFAULT_FORMAT);
		while (fi.hasNext()) {
			Format format = fi.next();
			
			Iterator<ISelector> di = format.getDomainSet().iterator();
			while (di.hasNext()) {
				ISelector selects = di.next();
				if (selects.canSelectResources()) {
					/*
					 * Only ClassFormat and InstanceFormat select resources,
					 * so the vast amount of property formats can be skipped
					 */
					if (!(format instanceof ClassFormat || format instanceof InstanceFormat))
						continue;
					
					try {
						Iterator<Resource> ri = selects.selectResources(in);
						while (ri.hasNext()) {
							Resource res = ri.next();
							if (this._formatResourceMatches.containsKey(res)) {
								ResourceFormatMatchSet match = this._formatResourceMatches.get(res);
								match.addClassFormat(format);
							} else {
								ResourceFormatMatchSet match = new ResourceFormatMatchSet(res);
								match.addClassFormat(format);
								this._formatResourceMatches.put(res, match);
							}
						}
					} catch (InvalidResultSetException e) {
						// TODO
					}
				} else if (selects.canSelectStatements()) {
					// inefficient - should really ask the model via sparql or something
					// about all the predicates it contains
					try {
						Iterator<Statement> si = selects.selectStatements(select.getModel(), null);
						while (si.hasNext()) {
							URI prop = si.next().getPredicate();
							if (this._formatPropertyMatches.containsKey(prop)) {
								PropertyFormatMatchSet match = this._formatPropertyMatches.get(prop);
								match.addPropertyFormat(format);
							} else {
								PropertyFormatMatchSet match = new PropertyFormatMatchSet(prop);
								match.addPropertyFormat(format);
								this._formatPropertyMatches.put(prop, match);
							}
						}
						si = selects.selectStatements(select.getNotModel(), null);
						while (si.hasNext()) {
							URI prop = si.next().getPredicate();
							if (this._formatPropertyMatches.containsKey(prop)) {
								PropertyFormatMatchSet match = this._formatPropertyMatches.get(prop);
								match.addPropertyFormat(format);
							} else {
								PropertyFormatMatchSet match = new PropertyFormatMatchSet(prop);
								match.addPropertyFormat(format);
								this._formatPropertyMatches.put(prop, match);
							}
						}
					} catch (InvalidResultSetException e) { ; }
				}
			}
		}		

		// er...in retrospect, subjects is the wrong word - it's any resource, be it subject or
		// object with no predicates of its own
		Iterator<?> subjects = select.getResourceHash().keySet().iterator();
		while (subjects.hasNext()) {
			Resource subject = (Resource) subjects.next();
			ResourceFormatMatchSet match = this._formatResourceMatches.get(subject);
			select.applyFormat(grouping, (null != match) ? match.topMatch() : defaultFormat, subject, Selection.RESOURCE, in);
		}

		// must go through *all* the properties!

		// some class of stuff says this property would have been selected if it existed (not sure how
		// this goes with FSL...), then when formatting comes along, fill it in with contentNoValue if
		// a Format exists; if not, show nothing

		Iterator<?> properties = select.getPropertyHash().keySet().iterator();
		while (properties.hasNext()) {
			URI subject = (URI) properties.next();
			PropertyFormatMatchSet match = this._formatPropertyMatches.get(subject);
			select.applyFormat(grouping,  (null != match) ? match.topMatch() : defaultFormat, subject, Selection.PROPERTY, in);
		}

		return select;
	}

	/**
	 * Find an existing lens for re-use in another context outside straight parsing,
	 * such as sublens parsing; parse and add the lens if not found.
	 * 
	 * @param identifier The <code>Resource</code> identifying the lens
	 * @return The found <code>Lens</code>
	 * @throws ParsingException If the lens is not found anywhere or if there are problems parsing the lens
	 * @throws UnresolvableException If there are problems parsing the lens
	 */
	public Lens lensLookup(Resource identifier) throws ParsingException, UnresolvableException, ResourceNotFoundException {
		Lens out = null;
		if (this._all.containsKey(identifier) && this._lenses.contains(this._all.get(identifier)))
			out = (Lens) this._all.get(identifier);
		else {
			RepositoryConnection conn = null;
			try {
				conn = this._source.getConnection();
				if (conn.hasStatement(identifier, FresnelCore.instanceLensDomain, (Value) null, false)) {
					out = new InstanceLens(this._source, identifier, this);
					addInstanceLens(out);
				} else if (conn.hasStatement(identifier, FresnelCore.classLensDomain, (Value) null, false)) {
					out = new Lens(this._source, identifier, this);
					addLens(out);
				} else {
					throw new ResourceNotFoundException("Explicitly named lens not found in configuration");
				}
			} catch (RepositoryException e) {
				throw new UnresolvableException("Problem connecting to repository: " + e.getLocalizedMessage());
			}
			finally {
				try {
					conn.close();
				}
				catch (RepositoryException e) {
				}
			}
		}
		return out;
	}

	/**
	 * Lookup an existing group or parse a new one into the configuration
	 * 
	 * @param identifier The <code>Resource</code> identifier
	 * @return A <code>Group</code>
	 * @throws ParsingException If a parsing error occurs in creating a new group
	 * @throws UnresolvableException If an unresolvable error occurs in creating a new group
	 */
	public Group groupLookupOrAdd(Resource identifier) throws ParsingException, UnresolvableException, ResourceNotFoundException {
		Group out = null;
		if (this._all.containsKey(identifier) && this._groups.containsKey(identifier))
			out = (Group) this._all.get(identifier);
		else {
			RepositoryConnection conn = null;
			try {
				conn = this._source.getConnection();
				if (conn.hasStatement((Resource) null, FresnelCore.group, (Value) identifier, false)) {
					out = Group.parse(this._source, identifier, this);
					addGroup(out);
				} else {
					throw new ResourceNotFoundException("Explicitly named group not found in configuration");
				}
			} catch (RepositoryException e) {
				throw new UnresolvableException("Problems connecting to repository: " + e.getLocalizedMessage());
			}
			finally {
				if (conn != null)
					try {
						conn.close();
					}
				catch (RepositoryException e) {
					e.printStackTrace();
				}
			}
		}
		return out;
	}

	/**
	 * Only looks up the group identifier, returns null if no group found.
	 * 
	 * @param identifier A <code>Resource</code>
	 * @return The <code>Group</code> or null if not found
	 */
	public Group groupLookup(Resource identifier) {
		Group out = null;
		if (this._all.containsKey(identifier) && this._groups.containsKey(identifier))
			out = (Group) this._all.get(identifier);
		return out;
	}

	/**
	 * Retreives the group currently used by default
	 * 
	 * @return A <code>Group</code>
	 */
	public Group getCurrentGroup() {
		Group group = this._mainGroup;
		if (null == group) {
			if (this._groups.size() > 0) {
				// this just takes the first group if it's undefined;
				// a smarter way to do it would be to do some fast analysis on which
				// group matches the most resources
				for (Iterator<Resource> groupIt = this._groupOrder.iterator(); groupIt.hasNext(); ) {
					Resource groupRes = (Resource) groupIt.next();
					Group groupCandidate = this._groups.getGroup(groupRes);
					if (groupCandidate.getLenses().size() > 0) {
						group = groupCandidate;
						break;
					}
				}
			}

			if (null == group) {
				// put everything in a default group if no groups containing lenses exists
				group = new Group(DEFAULT_GROUP);
			}
		}

		return group;
	}

	/**
	 * Lookup an existing format or parse it into configuration if it does not exist.
	 * 
	 * @param identifier The <code>Format</code> identifier to lookup
	 * @return A <code>Format</code>
	 * @throws ParsingException If there are parsing errors in making a new format
	 * @throws UnresolvableException If there are unresolvable errors in making a new format
	 */
	public Format formatLookup(Resource identifier) throws ParsingException, UnresolvableException, ResourceNotFoundException {
		Format out = null;
		if (this._all.containsKey(identifier) && this._formats.contains(this._all.get(identifier)))
			out = (Format) this._all.get(identifier);
		else {
			try {
				RepositoryConnection conn = this._source.getConnection();
				if (conn.hasStatement(identifier, FresnelCore.propertyFormatDomain, (Value) null, false)) {
					out = Format.parse(this._source, identifier, this);
					addPropertyFormat(out);
				} else if (conn.hasStatement(identifier, FresnelCore.instanceFormatDomain, (Value) null, false)) {
					out = InstanceFormat.parse(this._source, identifier, this);
					addInstanceFormat(out);
				} else if (conn.hasStatement(identifier, FresnelCore.classFormatDomain, (Value) null, false)) {
					out = ClassFormat.parse(this._source, identifier, this);
					addClassFormat(out);
				} else {
					throw new ResourceNotFoundException("Explicitly named style not found in configuration");
				}
				conn.close();
			} catch (RepositoryException e) {
				throw new UnresolvableException("Problem connecting to repository: " + e.getLocalizedMessage());
			}
		}
		return out;
	}

	/**
	 * Fetch all the groups in the configuration, probably for UI purposes
	 * 
	 * @return A <code>Vector</code> of configured group values
	 */
	public Vector<Group> groups() {
		return new Vector<Group>(this._groups.values());
	}

	/**
	 * EXTENSION method for finding facets per class
	 *
	 * @param forClasses A <code>List</code> of <code>Resource</code>s
	 * @return A <code>List</code> of <code>Resource</code>s
	 */
	public List<Resource> facets(Set<?> forClasses) {
		Vector<Resource> out = new Vector<Resource>();
		if (this._facets.hasDefaultSet()) {
			for (Iterator<Facet> fi = this._facets.getDefaultSet().facetIterator(); fi.hasNext(); ) {
				out.add(fi.next().getIdentifier());
			}
		}

		if (null != forClasses) {
			for (Iterator<?> it = forClasses.iterator(); it.hasNext(); ) {
				Iterator<FacetSet> fsi = this._facets.getFacetSetIterator((Resource) it.next());
				if (null != fsi) {
					while (fsi.hasNext()) {
						for (Iterator<Facet> fi = fsi.next().facetIterator(); fi.hasNext(); ) {
							Facet nextF = fi.next();
							if (!out.contains(nextF))
								out.add(nextF.getIdentifier());
						}
					}
				}
			}
		}

		return out;
	}

	/**
	 * EXTENSION method for finding hidden facets per class
	 *
	 * @param forClasses A <code>List</code> of <code>Resource</code>s
	 * @return A <code>List</code> of <code>Resource</code>s
	 */
	public List<Resource> hiddenFacets(Set<?> forClasses) {
		Vector<Resource> out = new Vector<Resource>();
		if (this._facets.hasDefaultSet()) {
			for (Iterator<Facet> fi = this._facets.getDefaultSet().hideIterator(); fi.hasNext(); ) {
				out.add(fi.next().getIdentifier());
			}
		}

		if (null != forClasses) {
			for (Iterator<?> it = forClasses.iterator(); it.hasNext(); ) {
				Iterator<FacetSet> fsi = this._facets.getFacetSetIterator((Resource) it.next());
				if (null != fsi) {
					while (fsi.hasNext()) {
						for (Iterator<Facet> fi = fsi.next().hideIterator(); fi.hasNext(); ) {
							Facet nextF = fi.next();
							if (!out.contains(nextF))
								out.add(nextF.getIdentifier());
						}
					}
				}
			}
		}

		return out;
	}

	/**
	 * Get the parsed namespace / abbreviation map for FSL.
	 * 
	 * @return An <code>FSLNSResolver</code>
	 */
	public FSLNSResolver getNamespaceMap() {
		return this._nsmap;
	}

	/**
	 * Get the namespaces of the configuration for SPARQL queries.
	 * 
	 * @return A Set<Namespace> set.
	 */
	public String getNamespaces() {
		return this._namespaces;
	}

	/**
	 * Retrieve the hash of all lens and resource matches.
	 * 
	 * @return A <code>LensMatchHashMap</code>
	 */
	public LensMatchHashMap getLensMatches() {
		return this._lensMatches;
	}

	/**
	 * Retreive the hash of all instance lens and resource matches.
	 * 
	 * @return A <code>LensMatchHashMap</code>
	 */
	public LensMatchHashMap getInstanceLensMatches() {
		return this._instanceLensMatches;
	}

	/**
	 * Retreive the hash of all class lens and resource matches.
	 * 
	 * @return A <code>LensMatchHashMap</code>
	 */
	public LensMatchHashMap getClassLensMatches() {
		return this._classLensMatches;
	}

	/**
	 * Queries the internal ontology graph for a set of labels for a resource based on a given
	 * labelling property.
	 * 
	 * @param subject The subject <code>Resource</code> to find labels for
	 * @param labelPropertyURI The property <code>URI</code> to find objects of
	 * @return A <code>StatementIterator</code> with statements whose objects are labels of the subject
	 */
	public Iterator<Statement> getOntologyLabels(Resource subject, URI labelPropertyURI) {
		if (this._ontologies != null) {
			// TODO: problematic
			Vector<Statement> out = new Vector<Statement>();
			try {
				RepositoryConnection conn = this._ontologies.getConnection();
				RepositoryResult<Statement> it = conn.getStatements(subject, labelPropertyURI, (Value) null, false);
				while (it.hasNext()) {
					out.add(it.next());
				}
				it.close();
				conn.close();
			} catch (RepositoryException e) {
				// TODO: how to handle exception
			}
			return out.iterator();
		} else {
			return null;
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String state = super.toString() + "\n";
		state += " Lens count: " + this._lenses.size() + "\n";
		state += " Group count: " + this._groups.size() + "\n";
		state += " Format count: " + this._formats.size() + "\n";
		state += " Facet count: " + this._facets.size() + "\n";

		Iterator<Lens> lensIt = this._lenses.iterator();
		Iterator<Group> groupIt = this._groups.values().iterator();
		Iterator<Format> styleIt = this._formats.iterator();

		state += "\n[Lenses]\n";

		while (lensIt.hasNext()) {
			state += "\n" + lensIt.next() + "\n";
		}

		state += "\n[Groups]\n";

		while (groupIt.hasNext()) {
			state += "\n" + groupIt.next() + "\n";
		}

		state += "\n[Formats]\n";

		while (styleIt.hasNext()) {
			state += "\n" + styleIt.next() + "\n";
		}

		state += "\n[Facets]\n";
		state += "\n" + this._facets.toString() + "\n";

		return state;
	}
}
