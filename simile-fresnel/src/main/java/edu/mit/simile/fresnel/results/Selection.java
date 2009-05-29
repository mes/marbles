package edu.mit.simile.fresnel.results;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import edu.mit.simile.fresnel.FresnelUtilities;
import edu.mit.simile.fresnel.configuration.Configuration;
import edu.mit.simile.fresnel.configuration.Group;
import edu.mit.simile.fresnel.configuration.LensMatchSet;
import edu.mit.simile.fresnel.format.Format;
import edu.mit.simile.fresnel.format.FormatDescription;
import edu.mit.simile.fresnel.format.Style;
import edu.mit.simile.fresnel.purpose.Purpose;
import edu.mit.simile.fresnel.selection.AllPropertiesSelector;
import edu.mit.simile.fresnel.selection.FSESelector;
import edu.mit.simile.fresnel.selection.ISelector;
import edu.mit.simile.fresnel.selection.InvalidResultSetException;
import edu.mit.simile.fresnel.selection.Lens;
import edu.mit.simile.fresnel.selection.PropertyDescription;
import edu.mit.simile.fresnel.selection.PropertySelector;
import edu.mit.simile.fresnel.selection.PropertySet;
import edu.mit.simile.vocabularies.FresnelCore;

/**
 * The output of lens selection which can then be formatted and finally
 * rendered to an XML output.
 * 
 * @author ryanlee
 */
public class Selection implements ResultConstants {	
	/**
	 * The configuration that generated this selection
	 */
	private Configuration _conf;
	
	/**
	 * The subRepository of data selected in the selection process
	 */
	private Repository _model;
	
	/**
	 * A subRepository of data only relating to its types, independent of what is actually selected
	 */
	private Repository _typesModel;
	
	/**
	 * Statements expected but not found in results; no way to match selectors but through
	 * model API...
	 */
	private Repository _notModel;
	
	/**
	 * Unused; will specify order of results if ever implemented
	 */
	//private Vector _order;
	
	/**
	 * The set of all primary results generated during selection
	 */
	private List<Result> _results;
	
	/**
	 * A hash of all results generated in selection keyed by resource
	 */
	private ResultHashMap<Resource, Result> _resultModelHash;
	
	/**
	 * A hash of all property results generated in selection keyed by property URI
	 */
	private ResultHashMap<URI, PropertyResult> _propertyResultModelHash;

	/**
	 * A hash of all value results generated in selection keyed by value
	 */
	private ResultHashMap<Value, ValueResult> _valueResultModelHash;
	
	/**
	 * Language preference
	 */
	private String _langPref;

	/**
	 * Format should apply to a resource
	 */
	public static final int RESOURCE = 0;
	/**
	 * Format should apply to a property
	 */
	public static final int PROPERTY = 1;
	/**
	 * Format should apply to a label
	 */
	public static final int LABEL    = 2;
	/**
	 * Format should apply to a value
	 */
	public static final int VALUE    = 3;
	/**
	 * dc:title URI, because it's easier to put it here
	 */
	public static final String DCTITLE = "http://purl.org/dc/elements/1.1/title";
	
	/**
	 * Constructor based on a configuration.
	 * 
	 * @param conf A <code>Configuration</code>
	 */
	public Selection(Configuration conf) {
		this._conf = conf;
		this._model = new SailRepository(new MemoryStore());
		this._typesModel = new SailRepository(new MemoryStore());
		this._notModel = new SailRepository(new MemoryStore());
		try {
			this._model.initialize();
			this._typesModel.initialize();
			this._notModel.initialize();
		} catch (RepositoryException e) {
			System.err.println("Failed to initialize repositories");
		}
		//this._order = new Vector();
		this._results = new LinkedList<Result>();
		this._resultModelHash = new ResultHashMap<Resource, Result>();
		this._propertyResultModelHash = new ResultHashMap<URI, PropertyResult>();
		this._valueResultModelHash = new ResultHashMap<Value, ValueResult>();
		this._langPref = "en";
	}
	
	/**
	 * Retrieves a subRepository created to reflect all the things which were requested in the
	 * configuration and found in the data source.
	 * 
	 * @return A <code>Repository</code>
	 */
	public Repository getModel() {
		return this._model;
	}
	
	/**
	 * Retrieves a subRepository created to reflect all the things which were requested
	 * in configuration but not actually found in the data source.
	 * 
	 * @return A <code>Repository</code>
	 */
	public Repository getNotModel() {
		return this._notModel;
	}
	
	/**
	 * Retrieves a subRepository with just typing information.
	 * 
	 * @return A <code>Repository</code> consisting solely of rdf:type statements
	 */
	public Repository getTypesModel() {
		return this._typesModel;
	}
	
	/**
	 * Retrieves the hash of all results generated during selection.
	 * 
	 * @return A <code>ResultHashMap</code>
	 */
	public ResultHashMap<Resource, Result> getResourceHash() {
		return this._resultModelHash;
	}
	
	/**
	 * Retrieves the hash of all property results generated during selection
	 * 
	 * @return A <code>ResultHashMap</code>
	 */
	public ResultHashMap<URI, PropertyResult> getPropertyHash() {
		return this._propertyResultModelHash;
	}
	
	/**
	 * Retrieves the hash of all value results generated during selection
	 * 
	 * @return A <code>ResultHashMap</code>
	 */
	public ResultHashMap<Value, ValueResult> getValueHash() {
		return this._valueResultModelHash;
	}

	/*
	 * 
	 * @param params
	 */
	/*
	public void reorder(Object[] params) {
		// TODO write this with input on how to do ordering selection
		this._order.clear();
	}
	*/
	
	/**
	 * Add to the list of primary results
	 * 
	 * @param r A <code>Result</code>
	 */
	public void addPrimary(Result r) {
		this._results.add(r);
	}
	
	/**
	 * Sets preferred language for labels
	 * @param langPref
	 */
	public void setLangPref(String langPref) {
		this._langPref = langPref;
	}
	
	/**
	 * Select data using a lens.
	 * 
	 * @param group The <code>Group</code> to work with
	 * @param in Data source <code>Repository</code>
	 * @param lens The <code>Lens</code> to select with
	 * @param focus Subject <code>Resource</code>
	 * @param current Present recursion depth, an <code>int</code>
	 * @param max Maximum recursion depth, an <code>int</code>
	 * @return A selection <code>Result</code>
	 */
	public Result applyLens(Group group, Repository in, Lens lens, Resource focus, int current, int max) {
		Result r = new Result(FresnelUtilities.dupResource(focus), group, lens, in);
		try {
			Iterator<Statement> it = r.getTypesStatements();
			RepositoryConnection conn = this._typesModel.getConnection();
			conn.setAutoCommit(false);
			while(it.hasNext()) {
				Statement s = it.next();
				conn.add(s.getSubject(), s.getPredicate(), s.getObject(), s.getContext());
			}
			conn.commit();
			conn.setAutoCommit(true);
			conn.close();
		} catch (RepositoryException sue) {
			System.err.println("Could not add to repository: " + sue);
			return null;
		}
 		PropertySet finalProperties = resolvePropertySet(lens, in, focus);
		Iterator<ISelector> focusPSI = finalProperties.iterator();
		r.setTitle(resolveLabel(in, focus));
		this._resultModelHash.putResult(FresnelUtilities.dupResource(focus), r);
		while (focusPSI.hasNext()) {
			ISelector selector = focusPSI.next();
			if (selector.canSelectStatements()) {
				try {
					Iterator<Statement> selects = selector.selectStatements(in, focus);
					if (!selects.hasNext() &&
							(selector instanceof PropertySelector || selector instanceof PropertyDescription)) {
						if (selector instanceof PropertySelector) {
							URI predicate = ((PropertySelector) selector).getProperty();
							PropertyResult pr = r.getProperties().lookup(predicate);
							if (null == pr) {
								NoSuchPropertyResult nspr = new NoSuchPropertyResult(FresnelUtilities.dupURI(predicate), selector, r);
								nspr.setTitle(resolveLabel(in, predicate));
								r.addProperty(nspr);
								this._propertyResultModelHash.putResult(FresnelUtilities.dupURI(predicate), nspr);
								RepositoryConnection nconn = this._notModel.getConnection();
								nconn.add(FresnelUtilities.dupResource(focus), FresnelUtilities.dupURI(predicate), new LiteralImpl("empty"));
								nconn.close();
							} // if it's not null, there's nothing to do
						} else {
							Iterator<Statement> pi = ((PropertyDescription) selector).getProperty().selectStatements(in, focus);
							if (pi.hasNext()) {
								URI predicate = pi.next().getPredicate();
								PropertyResult pr = r.getProperties().lookup(predicate);
								if (null == pr) {
									NoSuchPropertyResult nspr = new NoSuchPropertyResult(FresnelUtilities.dupURI(predicate), selector, r);
									nspr.setTitle(resolveLabel(in, predicate));
									r.addProperty(nspr);
									this._propertyResultModelHash.putResult(FresnelUtilities.dupURI(predicate), nspr);
									RepositoryConnection nconn = this._notModel.getConnection();
									nconn.add(FresnelUtilities.dupResource(focus), FresnelUtilities.dupURI(predicate), new LiteralImpl("empty"));
									nconn.close();
								} // if it's not null, there's nothing to do
							}
						}
					}
					while (selects.hasNext()) {
						Statement selected = selects.next();
						RepositoryConnection mconn = this._model.getConnection();
						mconn.add(selected.getSubject(), selected.getPredicate(), selected.getObject(), selected.getContext());
						mconn.commit();
						mconn.close();
						URI predicate;

						boolean inverse = !selected.getSubject().equals(focus);
						Value object = inverse ? selected.getSubject() : selected.getObject();

						if (selector instanceof PropertyDescription && ((PropertyDescription)selector).getProperty() instanceof FSESelector) {							
							predicate = new URIImpl("fsl://" + ((FSESelector)((PropertyDescription)selector).getProperty()).get_fse());
						}
						else
							predicate = selected.getPredicate();
						
						PropertyResult pr = r.getProperties().lookup(predicate, inverse);

						if (null == pr) {
							pr = new PropertyResult(FresnelUtilities.dupURI(predicate), selector, r, inverse);
							pr.setTitle(resolveLabel(in, predicate));
							r.addProperty(pr);
						//	this._propertyResultModelHash.putResult(FresnelUtilities.dupURI(predicate), pr);
						} else {
							// if somehow this property was already marked as NoSuchProperty, then
							// replace it with a real result
							if (!pr.isInModel()) {
								r.getProperties().removePropertyResult(pr);
								pr = new PropertyResult(FresnelUtilities.dupURI(predicate), selector, r);
								pr.setTitle(resolveLabel(in, predicate));
								r.addProperty(pr);
						//		this._propertyResultModelHash.putResult(FresnelUtilities.dupURI(predicate), pr);
							}
						}
						
						// cb   
						this._propertyResultModelHash.putResult(FresnelUtilities.dupURI(predicate), pr);
						if (object instanceof Resource) {
							Resource objResource = (Resource) object;
							Result subr = new Result(FresnelUtilities.dupResource(objResource), group, lens, in);
							Iterator<Statement> types = r.getTypesStatements();
							RepositoryConnection tmconn = this._typesModel.getConnection();
							tmconn.setAutoCommit(false);
							while(types.hasNext()) {
								Statement s = types.next();
								tmconn.add(s.getSubject(), s.getPredicate(), s.getObject(), s.getContext());
							}
							tmconn.commit();
							tmconn.setAutoCommit(true);
							tmconn.close();
							if (selector instanceof PropertyDescription) {
								PropertyDescription selectorPD = (PropertyDescription) selector;
								if (current == max) {
									// if we've hit the limit, stop with sublensing
									subr.setTitle(resolveLabel(in, objResource));
								} else {									
									// pick a sublens to apply to the resource
									int newdepth = (selectorPD.getDepth() + current < max) ? selectorPD.getDepth() + current : max;
									Lens sublens = null;
									Iterator<Lens> sublensesIt = selectorPD.getSublensesIterator();
									boolean match = false;
									matched:
									while (sublensesIt.hasNext()) {
										sublens = sublensesIt.next();
										Iterator<ISelector> domainIt = sublens.getDomainSet().iterator();
										while (domainIt.hasNext()) {
											ISelector subdomain = domainIt.next();
											if (subdomain.canSelect(in, objResource)) {
												subr = applyLens(group, in, sublens, objResource, current + 1, newdepth);
												match = true;
												break matched;
											}
										}
									}
									if (!match) {
										// if none of the specified sublenses fit, check for other matches
										LensMatchSet lenses = this._conf.getLensMatches().getMatch(objResource);
										if (null != lenses && !lenses.isEmpty()) {
											sublens = lenses.topMatch();
											subr = applyLens(group, in, sublens, objResource, current + 1, max);
										} else {
											// or if no lenses fit, turn it into a label
											subr.setTitle(resolveLabel(in, objResource));
										}
									}
								}
							} else {
								subr.setTitle(resolveLabel(in, objResource));
							}
							this._resultModelHash.putResult(FresnelUtilities.dupResource(objResource), subr);
							ValueResult vr = new ValueResult(subr, pr, selected.getContext());
							this._valueResultModelHash.putResult(FresnelUtilities.dupResource(objResource), vr);
							pr.addValue(vr,in);
						} /* instanceof Resource */ 
						else {
							Literal objLiteral = (Literal) object;
							ValueResult vr = new ValueResult(objLiteral.getLabel(), pr, selected.getContext());
							this._valueResultModelHash.putResult((Literal) FresnelUtilities.dupValue(objLiteral), vr);
							pr.addValue(vr);
						}
					}
				} catch (InvalidResultSetException e) {
					//
				} catch (RepositoryException sue) {
					System.err.println("Could not add to repository: " + sue);
					return null;
				}
			}
		}
		return r;
	}
	
	/**
	 * Dispatching method to apply a format depending which type is being formatted
	 * 
	 * @param group The <code>Group</code> to work with
	 * @param format The <code>Format</code> to use
	 * @param focus The <code>Object</code> to format
	 * @param type The <code>int</code> type as specified by the class constants
	 * @return Success or failure
	 */
	public boolean applyFormat(Group group, Format format, Object focus, int type, Repository in) {
		// by contrast, do not use a formatting if it's not in the right group
		boolean success = true;
		switch(type) {
			case RESOURCE:
				applyFormat(group, format, (Resource) focus);
				break;
			case PROPERTY:
				applyFormat(group, format, (URI) focus, in);
				break;
			case LABEL:
				applyLabelFormat(group, format, (PropertyResult) focus);
				break;
			case VALUE:
				applyValueFormat(group, format, (PropertyResult) focus);
				break;
			default:
				success = false;
				break;			
		}
		return success;
	}
	
	/**
	 * Applies a label lens to get a result in return.
	 * 
	 * @param lens The label <code>Lens</code>
	 * @param in Data source <code>Repository</code>
	 * @param focus The selected <code>Resource</code>
	 * @param current The current recursion depth, an <code>int</code>
	 * @param max The maximum recursion depth, an <code>int</code>
	 * @return An appropriate <code>Result</code> that can be rendered as a label
	 */
	public Result applyLabelLens(Lens lens, Repository in, Resource focus, int current, int max) {
		Result r = new Result(FresnelUtilities.dupResource(focus), null, lens, in);
		try {
			Iterator<Statement> it = r.getTypesStatements();
			RepositoryConnection conn = this._typesModel.getConnection();
			conn.setAutoCommit(false);
			while (it.hasNext()) {
				Statement s = it.next();
				conn.add(s.getSubject(), s.getPredicate(), s.getObject(), s.getContext());
			}
			conn.commit();
			conn.setAutoCommit(true);
			conn.close();
		} catch (RepositoryException sue) {
			System.err.println("Could not add to repository: " + sue);
			return null;
		}
		PropertySet finalProperties = resolvePropertySet(lens, in, focus);
		Iterator<ISelector> focusPSI = finalProperties.iterator();
		this._resultModelHash.putResult(FresnelUtilities.dupResource(focus), r);
		while (focusPSI.hasNext()) {
			ISelector selector = focusPSI.next();
			if (selector.canSelectStatements()) {
				try {
					Iterator<Statement> selects = selector.selectStatements(in, focus);
					while (selects.hasNext()) {
						Statement selected = selects.next();
						RepositoryConnection mconn = this._model.getConnection();
						mconn.add(FresnelUtilities.dupResource(selected.getSubject()),
								FresnelUtilities.dupURI(selected.getPredicate()),
								FresnelUtilities.dupValue(selected.getObject()));
						mconn.commit();
						mconn.close();
						URI prop = FresnelUtilities.dupURI(selected.getPredicate());
						Value object = selected.getObject();
						PropertyResult pr = r.getProperties().lookup(prop);
						if (null == pr) {
							pr = new PropertyResult(prop, selector, r);
							r.addProperty(pr);
						}
						if (object instanceof Resource) {
							Resource objResource = (Resource) object;
							if (selector instanceof PropertyDescription) {
								PropertyDescription selectorPD = (PropertyDescription) selector;
								if (current == max) {
									ValueResult vr = new ValueResult(resolveLabel(in, objResource, false).getString(), pr, selected.getContext());
									pr.addValue(vr);
									this._propertyResultModelHash.putResult(prop, pr);
								} else {
									// pick a sublens to apply to the resource
									int newdepth = (selectorPD.getDepth() + current < max) ? selectorPD.getDepth() : max;
									Lens sublens = null;
									Iterator<Lens> sublensesIt = selectorPD.getSublensesIterator();
									boolean matched = false;
									matching:
									while (sublensesIt.hasNext()) {
										sublens = sublensesIt.next();
										Iterator<ISelector> domainIt = sublens.getDomainSet().iterator();
										while (domainIt.hasNext()) {
											ISelector subdomain = domainIt.next();
											if (subdomain.canSelect(in, objResource)) {
												Result subr = applyLabelLens(sublens, in, objResource, current + 1, newdepth);
												ValueResult vr = new ValueResult(subr, pr, selected.getContext());
												pr.addValue(vr);
												this._propertyResultModelHash.putResult(prop, pr);
												matched = true;
												break matching;
											}
										}
									}
									
									if (!matched) {
										// if none of the specified sublenses fit, check for other matches
										LensMatchSet lenses = this._conf.getLensMatches().getMatch(objResource);
										if (null != lenses) {
											sublens = lenses.topMatch();
											Result subr = applyLabelLens(sublens, in, objResource, current + 1, max);
											ValueResult vr = new ValueResult(subr, pr, selected.getContext());
											pr.addValue(vr);
											this._propertyResultModelHash.putResult(prop, pr);
										} else {
											// or if no lenses fit, turn it into a label
											ValueResult vr = new ValueResult(resolveLabel(in, objResource, false).getString(), pr, selected.getContext());
											pr.addValue(vr);
											this._propertyResultModelHash.putResult(prop, pr);
										}
									}
								}
							} else {
								// if not a sublens situation, check for other matches
								LensMatchSet lenses = this._conf.getLensMatches().getMatch(objResource);
								if (null != lenses) {
									Result subr = applyLabelLens(lenses.topMatch(), in, objResource, current + 1, max);
									ValueResult vr = new ValueResult(subr, pr, selected.getContext());
									pr.addValue(vr);
									this._propertyResultModelHash.putResult(prop, pr);
								} else {
									// or if no lenses fit, turn it into a label
									ValueResult vr = new ValueResult(resolveLabel(in, objResource, false).getString(), pr, selected.getContext());
									pr.addValue(vr);
									this._propertyResultModelHash.putResult(prop, pr);
								}
							}
						} else {
							Literal objLiteral = (Literal) object;
							ValueResult vr = new ValueResult(objLiteral.getLabel(), pr, selected.getContext());
							pr.addValue(vr);
							this._propertyResultModelHash.putResult(prop, pr);
						}
					}
				} catch (InvalidResultSetException e) {
					//
				} catch (RepositoryException sue) {
					System.err.println("Could not add to repository: " + sue);
					return null;
				}
			}
		}
		return r;
	}
	
	/**
	 * Look in the source and ontologies model for a resource's label.
	 * 
	 * @param in Data source <code>Repository</code>
	 * @param focus The <code>Resource</code> whose label is being sought
	 * @param lens If the algorithm should look into lenses for labelling information or not
	 * @return A <code>Title</code> label for the resource
	 */
	protected Title resolveLabel(Repository in, Resource focus, boolean lens, String label) {
		if (null != label) return new Title(label);
		
		Resource focusType = (Resource) FresnelUtilities.getType(in, focus);
		// this is ugly, but to not waste resources, it really needs the 'return' statements to
		// return ASAP
		LensMatchSet lenses = this._conf.getLensMatches().getMatch(focus);
		LensMatchSet instanceLenses = this._conf.getInstanceLensMatches().getMatch(focus);
		LensMatchSet classLenses = this._conf.getClassLensMatches().getMatch(focusType);

		if (lens) {
			if (null != lenses) {
				// find label lens for resource
				Iterator<Lens> instancesIt = lenses.getInstanceLenses().iterator();
				while (instancesIt.hasNext()) {
					Lens check = instancesIt.next();
					if (check.hasPurpose(new Purpose(FresnelCore.labelLens))) {	
						AggregateLabel labels = new AggregateLabel(applyLabelLens(check, in, focus, 0, MAXIMUM_LENS_DEPTH));
						if (labels.getString().trim().equals("")) 
							continue;
						else 
							return new Title(labels);
					}
				}
				
				// find label lens for resource's type
				Iterator<Lens> classesIt = lenses.getClassLenses().iterator();
				while (classesIt.hasNext()) {
					Lens check = classesIt.next();
					if (check.hasPurpose(new Purpose(FresnelCore.labelLens))) {
						AggregateLabel labels = new AggregateLabel(applyLabelLens(check, in, focus, 0, MAXIMUM_LENS_DEPTH));
						if (labels.getString().trim().equals("")) 
							continue;
						else
							return new Title(labels);
					}
				}
			}

			if (null != instanceLenses) {
				// find label lens for resource
				Iterator<Lens> instancesIt = instanceLenses.getInstanceLenses().iterator();
				while (instancesIt.hasNext()) {
					Lens check = instancesIt.next();
					if (check.hasPurpose(new Purpose(FresnelCore.labelLens))) {	
						AggregateLabel labels = new AggregateLabel(applyLabelLens(check, in, focus, 0, MAXIMUM_LENS_DEPTH));
						if (labels.getString().trim().equals("")) 
							continue;
						else 
							return new Title(labels);
					}
				}
			}
			if (null != classLenses) {
				// find label lens for resource
				Iterator<Lens> classesIt = classLenses.getClassLenses().iterator();
				while (classesIt.hasNext()) {
					Lens check = classesIt.next();
					if (check.hasPurpose(new Purpose(FresnelCore.labelLens))) {
						AggregateLabel labels = new AggregateLabel(applyLabelLens(check, in, focus, 0, MAXIMUM_LENS_DEPTH));
						if (labels.getString().trim().equals("")) 
							continue;
						else
							return new Title(labels);
					}
				}
			}
		}
		
		Iterator<Statement> labelIt = this._conf.getOntologyLabels(focus, RDFS.LABEL);
		while (labelIt.hasNext()) {
			Statement labelSt = labelIt.next();
			if (((Literal) labelSt.getObject()).getLabel().trim().equals(""))
				continue;
			else {
				return new Title(((Literal) labelSt.getObject()).getLabel());
			}
		}
		
		labelIt = this._conf.getOntologyLabels(focus, new URIImpl(DCTITLE));
		while (labelIt.hasNext()) {
			Statement labelSt = labelIt.next();
			if (((Literal) labelSt.getObject()).getLabel().trim().equals(""))
				continue;
			else {
				return new Title(((Literal) labelSt.getObject()).getLabel());
			}
		}
		
		/* Get labels from the input repository */
		try {
			String resLabel = null;
			boolean matchesPreferredLanguage = false;
			
			String[] knownLabels = {
					"http://xmlns.com/foaf/0.1/name",
					"http://www.w3.org/2000/01/rdf-schema#label",
					"http://purl.org/dc/elements/1.1/title",
					"http://usefulinc.com/ns/doap#name",
					"http://www.geonames.org/ontology#name"
			};
			
			RepositoryConnection conn = in.getConnection();

			for (String labelPredicate : knownLabels) {
				RepositoryResult<Statement> it = conn.getStatements(focus, new URIImpl(labelPredicate), (Value) null, true);
				while (!matchesPreferredLanguage && it.hasNext()) {
					Statement st = it.next();
					if (st != null && st.getObject() instanceof Literal) {
						Literal lit = ((Literal)st.getObject());
						if (lit.getLanguage() != null && lit.getLanguage().equals(_langPref))
							matchesPreferredLanguage = true;
						
						/* Take the first label, or the first one in the preferred language */
						if (resLabel == null || matchesPreferredLanguage)
							resLabel = lit.getLabel();						
					}
				}

				it.close();
				if (matchesPreferredLanguage)
					break;
			}

			conn.close();
			
			if (resLabel != null)
				return new Title(resLabel);
		}
		catch (RepositoryException e) {
			e.printStackTrace();
		}

		if (!(focus instanceof BNode)) {
			// try to truncate the URI?
			return new Title(((URI) focus).toString(), true);
		} else
			return new Title(((BNode)focus).getID(), true);			//return new Title("(anonymous node)", true);
	}
	
	/**
	 * Default behavior for label resolution is to look into label lenses.
	 * 
	 * @param in Data source <code>Repository</code>
	 * @param focus The <code>Resource</code> whose label is being sought
	 * @param lens <code>boolean</code> indicator of whether to use lenses or not.
	 * @return A <code>Title</code> label for the resource
	 */
	protected Title resolveLabel(Repository in, Resource focus, boolean lens) {
		return resolveLabel(in, focus, lens, null);
	}
	
	/**
	 * Default behavior for label resolution is to look into label lenses.
	 * 
	 * @param in The data <code>Repository</code>
	 * @param focus The <code>Resource</code> whose label is being sought
	 * @return A <code>Title</code> label for the resource
	 */
	protected Title resolveLabel(Repository in, Resource focus) {
		return resolveLabel(in, focus, true, null);
	}
	
	/**
	 * Resolve the property sets to a final ordered list of what will show up.
	 * 
	 * @param lens The <code>Lens</code> with selection information
	 * @param in The <code>Repository</code> with all the data
	 * @param focus The <code>Resource</code> to resolve
	 * @return A <code>PropertySet</code> of properties to select.
	 */
	protected PropertySet resolvePropertySet(Lens lens, Repository in, Resource focus) {
		PropertySet properties = new PropertySet();
		PropertySet hide = lens.getHideProperties();
		PropertySet show = lens.getShowProperties();
		if (show.seenAllProperties()) {
			try {
				RepositoryConnection conn = in.getConnection();
                RepositoryResult<Statement> focusProperties = conn.getStatements(focus, (URI) null, (Value) null, true);
                RepositoryResult<Statement> focusPropertiesInverse = conn.getStatements(null, (URI) null, focus, true);
				Iterator<ISelector> showPSI = show.iterator();
				while (showPSI.hasNext()) {
					ISelector selector = showPSI.next();
					if (AllPropertiesSelector.isAllProperties(selector)) {
						// get all properties and check if in hidden properties
						while (focusProperties.hasNext() || focusPropertiesInverse.hasNext()) {
							Statement st = (focusProperties.hasNext() ? focusProperties.next() : focusPropertiesInverse.next());
							URI maybeAdd = FresnelUtilities.dupURI(st.getPredicate());
							PropertySelector maybeAddSelector = new PropertySelector(maybeAdd);
							if (hide.size() > 0) {
								if (!hide.containsSelector(maybeAddSelector)
										&& !properties.containsSelector(maybeAddSelector)) {
									properties.addSelector(maybeAddSelector);
								}
							} else {
								if (!properties.containsSelector(maybeAddSelector)) {
									properties.addSelector(maybeAddSelector);
								}
							}
						}
					} else {
						if (selector instanceof PropertySelector) {
							if (!properties.containsSelector((PropertySelector) selector)) {
								properties.addSelector(selector);
							}
						} else {
							properties.addSelector(selector);						
						}
					}
				}
				focusProperties.close();
				focusPropertiesInverse.close();
				conn.close();
			} catch (RepositoryException e) {
				// TODO: how to handle this exception
			}
		} else {
			properties = show;
		}
		return properties;
	}

	/**
	 * Apply formatting to a resource.
	 * 
	 * @param grouping Use from this <code>Group</code>
	 * @param format Use this <code>Format</coe>
	 * @param focus On this <code>Resource</code>
	 */
	protected void applyFormat(Group grouping, Format format, Resource focus) {
		// lookup the results that use this resource focus and apply this to all of them
		Iterator<Result> it = this._resultModelHash.getResultIterator(focus);
		while (it.hasNext()) {
			Result r = (Result) it.next();

			// get any group-scope styling
			if (grouping.getResourceStyle().size() > 0) {
				Iterator<Style> sit = grouping.getResourceStyle().iterator();
				String classes = "";
				while(sit.hasNext()) {
					// N.B.: if styles were ever expanded to be more than strings, here would
					// be a good place to make that adaptation
					classes += sit.next().getString();
				}
				r.setStyles(classes.trim());
			}
			
			// get any group-scope content
			if (grouping.getResourceFormat().size() > 0) {
				// there should only be one FormatDescription...
				Iterator<FormatDescription> fdit = grouping.getResourceFormat().iterator();
				if (fdit.hasNext()) {
					r.setContents(fdit.next().getContentSet());
				}
			}
			
			// process format argument for style
			if (format.getResourceStyle().size() > 0) {
				Iterator<Style> sit = format.getResourceStyle().iterator();
				String classes = r.getStyles() + " ";
				while(sit.hasNext()) {
					// N.B.: if styles were ever expanded to be more than strings, here would
					// be a good place to make that adaptation
					classes += sit.next().getString();
				}
				r.setStyles(classes.trim());
			}
			
			// process format argument for content
			if (format.getResourceFormat().size() > 0) {
				// there should only be one FormatDescription...
				Iterator<FormatDescription> fdit = format.getResourceFormat().iterator();
				if (fdit.hasNext()) {
					r.setContents(fdit.next().getContentSet());
				}
			}
		}
	}
	
	/**
	 * Apply formatting to a property result.
	 * 
	 * @param grouping Use from this <code>Group</code>
	 * @param format Use this <code>Format</code>
	 * @param focus Apply to this property <code>URI</code>
	 */
	protected void applyFormat(Group grouping, Format format, URI focus, Repository in) {
		Iterator<PropertyResult> it = this._propertyResultModelHash.getResultIterator(focus);
		while (it.hasNext()) {
			PropertyResult r = (PropertyResult) it.next();

			// get any group-global styling
			if (grouping.getPropertyStyle().size() > 0) {
				Iterator<Style> sit = grouping.getPropertyStyle().iterator();
				String classes = "";
				while(sit.hasNext()) {
					// N.B.: if styles were ever expanded to be more than strings, here would
					// be a good place to make that adaptation
					classes += sit.next().getString() + " ";
				}
				r.setStyles(classes.trim());
			}

			// get any group-scope content
			if (grouping.getPropertyFormat().size() > 0) {
				// there should only be one FormatDescription...
				Iterator<FormatDescription> fdit = grouping.getPropertyFormat().iterator();
				if (fdit.hasNext()) {
					r.setContents(fdit.next().getContentSet());
				}
			}

			// get any lens-specified (property description) formatting, use instead of
			// passed in format if necessary
			Lens parent = r.getParent().getLens();
			Iterator<ISelector> psi = parent.getShowProperties().iterator();
			while (psi.hasNext()) {
				ISelector check = psi.next();
				if (check.canSelect(in, r.getParent().getOrigin(), r.getOrigin())) {
					if (check instanceof PropertyDescription) {
						PropertyDescription pd = (PropertyDescription) check;
						if (pd.isGroupUse()) {
							// pick which format to use...
							Group groupFormats = (Group) pd.getUse();
							Iterator<Format> fi = groupFormats.getFormats().iterator();
							while (fi.hasNext()) {
								Format potential = fi.next();
								for (Iterator<ISelector> di = potential.getDomainSet().iterator(); di.hasNext(); ) {
									ISelector potentialCheck = di.next();
									if (potentialCheck.canSelect(getModel(), r.getParent().getOrigin(), r.getOrigin())) {
										format = potential;
										break;
									}
								}
							}
						} else {
							if (null != pd.getUse()) {
								format = (Format) pd.getUse();
								break;
							}
						}
					}
				}
			}

			// process format argument for style
			if (null != format.getPropertyStyle()) {
				Iterator<Style> sit = format.getPropertyStyle().iterator();
				String classes = (r.getStyles() != null) ? r.getStyles() + " " : "";
				while(sit.hasNext()) {
					// N.B.: if styles were ever expanded to be more than strings, here would
					// be a good place to make that adaptation
					classes += sit.next().getString() + " ";
				}
				r.setStyles(classes.trim());
			}
			
			// process format argument for content
			if (null != format.getPropertyFormat()) {
				// there should only be one FormatDescription...
				Iterator<FormatDescription> fdit = format.getPropertyFormat().iterator();
				if (fdit.hasNext()) {
					r.setContents(fdit.next().getContentSet());
				}
			}
						
			// call label and value format using the right format
			applyFormat(grouping, format, r, LABEL, in);
			applyFormat(grouping, format, r, VALUE, in);
		}
	}

	/**
	 * Apply formatting to property labels.
	 * 
	 * @param grouping Use those found in this <code>Group</code>
	 * @param format Use this <code>Format</code>
	 * @param pr Associated with this <code>PropertyResult</code>
	 */
	protected void applyLabelFormat(Group grouping, Format format, PropertyResult pr) {
		// get any group-global styling
		if (grouping.getLabelStyle().size() > 0) {
			Iterator<Style> it = grouping.getLabelStyle().iterator();
			String classes = "";
			while(it.hasNext()) {
				// N.B.: if styles were ever expanded to be more than strings, here would
				// be a good place to make that adaptation
				classes += it.next().getString() + " ";
			}
			pr.setLabelStyles(classes.trim());
		}
		
		// get any group-scope content
		if (grouping.getLabelFormat().size() > 0) {
			// there should only be one FormatDescription...
			Iterator<FormatDescription> fdit = grouping.getLabelFormat().iterator();
			if (fdit.hasNext()) {
				pr.setLabelContents(fdit.next().getContentSet());
			}
		}
		
		// process format argument for style
		if (null != format.getLabelStyle()) {
			Iterator<Style> sit = format.getLabelStyle().iterator();
			String classes = (pr.getLabelStyles() != null) ? pr.getLabelStyles() + " " : "";
			while(sit.hasNext()) {
				// N.B.: if styles were ever expanded to be more than strings, here would
				// be a good place to make that adaptation
				classes += sit.next().getString() + " ";
			}
			pr.setLabelStyles(classes.trim());
		}
		
		// process format argument for content
		if (null != format.getLabelFormat()) {
			// there should only be one FormatDescription...
			Iterator<FormatDescription> fdit = format.getLabelFormat().iterator();
			if (fdit.hasNext()) {
				pr.setContents(fdit.next().getContentSet());
			}
		}
		
		// change the label depending on fresnel:label settings
		if (!format.getLabel().isShown()) {
			pr.setShowLabel(false);
		} else if (format.getLabel().isShown() && format.getLabel().isString()) {
			pr.setTitle(new Title(format.getLabel().getString()));
		}
		
		//handle missing label title
		if ((null == pr.getTitle() || pr.getTitle().getString().trim().equals(""))
				&& (null != pr.getContents().getReplacement()
						&& !pr.getContents().getReplacement().trim().equals(""))) {
			pr.setTitle(new Title(pr.getContents().getReplacement()));
		}
	}
	
	/**
	 * Apply formatting to a set of values.
	 * 
	 * @param grouping Use those found in this <code>Group</code>
	 * @param format Use this <code>Format</code>
	 * @param pr Start from this <code>PropertyResult</code>
	 */
	protected void applyValueFormat(Group grouping, Format format, PropertyResult pr) {
		String classes = "";
		// get any group-global styling
		if (grouping.getValueStyle().size() > 0) {
			Iterator<Style> it = grouping.getValueStyle().iterator();
			while(it.hasNext()) {
				// N.B.: if styles were ever expanded to be more than strings, here would
				// be a good place to make that adaptation
				classes += it.next().getString();
			}
			classes = classes.trim();
			Iterator<ValueResult> vrit = pr.getValues().valueResultIterator();
			while (vrit.hasNext()) {
				vrit.next().setStyles(classes);
			}
		}
		
		// get any group-scope content
		if (grouping.getValueFormat().size() > 0) {
			// there should only be one FormatDescription...
			Iterator<FormatDescription> fdit = grouping.getValueFormat().iterator();
			if (fdit.hasNext()) {
				pr.getValues().setContents(fdit.next().getContentSet());
			}
		}
		
		// process format argument for style
		if (null != format.getValueStyle()) {
			Iterator<Style> sit = format.getValueStyle().iterator();
			classes += " ";
			while(sit.hasNext()) {
				// N.B.: if styles were ever expanded to be more than strings, here would
				// be a good place to make that adaptation
				classes += sit.next().getString();
			}
			Iterator<ValueResult> vrit = pr.getValues().valueResultIterator();
			while (vrit.hasNext()) {
				vrit.next().setStyles(classes.trim());
			}
		}
		
		// process format argument for content
		if (null != format.getValueFormat()) {
			// there should only be one FormatDescription...
			Iterator<FormatDescription> fdit = format.getValueFormat().iterator();
			if (fdit.hasNext()) {
				pr.getValues().setContents(fdit.next().getContentSet());
			}
		}
		
		// indicate what type the value is based on fresnel:value
		String kind = null;
		if (format.getValueType().isImage()) {
			kind = "image";
		} else if (format.getValueType().isURI()) {
			kind = "uri";
		} else if (format.getValueType().isLink()) {
			kind = "link";
		}
		Iterator<ValueResult> vrit = pr.getValues().valueResultIterator();
		while (vrit.hasNext()) {
			vrit.next().setOutputType(kind);
		}
	}

	/**
	 * Renders the entire selection to the Fresnel XML tree output as DOM.
	 * 
	 * @return A <code>Document</code>, e.g.: &lt;results&gt; ... &lt/results&gt;
	 */
	public Document render() throws ParserConfigurationException {
		// TODO: ordering
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document out = db.newDocument();
		Element root = out.createElementNS(INTERMEDIATE_NS, "results");
		for(Iterator<Result> it = this._results.iterator(); it.hasNext(); ) {
			root.appendChild(it.next().render(out));
		}
		out.appendChild(root);
		return out;
	}
	
	// Ideally this would simply generate events without bothering to render the document
	// to a DOM model first.  Ideally.
	/**
	 * Renders the entire selection into a SAX stream of events conforming to the
	 * Fresnel output XML Schema.
	 * 
	 * @param handler For handling SAX events
	 * @throws SAXException
	 */
	public void events(ContentHandler handler) throws SAXException {
		try {
			Document d = render();
			DOMSource in = new DOMSource(d);
			SAXResult res = new SAXResult(handler);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer serial = tf.newTransformer();
			serial.setOutputProperty(OutputKeys.INDENT, "yes");
			serial.transform(in, res);
		} catch (Exception e) {
			throw new SAXException(e);
		}
	}
}
