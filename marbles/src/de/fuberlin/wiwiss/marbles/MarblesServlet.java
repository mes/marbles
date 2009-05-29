/*
 *   Copyright (c) 2009, MediaEvent Services GmbH & Co. KG
 *   http://mediaeventservices.com
 *   
 *   This file is part of Marbles.
 *
 *   Marbles is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Marbles is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Marbles.  If not, see <http://www.gnu.org/licenses/>.
 *   
 */
package de.fuberlin.wiwiss.marbles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailBase;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.nativerdf.NativeStore;
import org.openrdf.sail.rdbms.mysql.MySqlStore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.fuberlin.wiwiss.marbles.dataproviders.DataProvider;
import de.fuberlin.wiwiss.marbles.loading.CacheController;
import de.fuberlin.wiwiss.marbles.loading.ContentTypes;
import de.fuberlin.wiwiss.marbles.loading.SemanticWebClient;
import de.fuberlin.wiwiss.marbles.loading.SpongerProvider;

import edu.mit.simile.fresnel.configuration.Configuration;
import edu.mit.simile.fresnel.configuration.NoResultsException;
import edu.mit.simile.fresnel.purpose.Purpose;
import edu.mit.simile.fresnel.results.Selection;

/**
 * Implements REST services to view, discover, load and clear URIs/URLs, as well as a SPARQL endpoint 
 * 
 * @author Christian Becker
 *
 * @web.servlet
 *   name="MarblesServlet"
 *   display-name="MarblesServlet" 
 *
 * @web.servlet-mapping
 *   url-pattern="/marbles"
 *  
 */
 public class MarblesServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
	static final long serialVersionUID = 1L; 
	    
    /* Parameters read from web.xml */
	
	/**
	 * Path to data directory with subdirectories for fresnel configuration ("fresnel"),
	 * built-in ontologies ("ontologies") and XSLT transformations ("xsl"), absolute or relative to the Marbles app directory
	 */
	private String dataRoot;
	
	/**
	 * Full HTTP path to assets in the "web" directory
	 */
	private URL assetsURL = null;
	
	/**
	 * HTTP URL of this service
	 */
	private URL serviceURL = null;

    /**
     * The directory that contains the fresnel configuration
     */
	private final String fresnelDirectory = "fresnel";

	/**
     * The directory that contains ontologies to load
     */
    private final String ontologiesDirectory = "ontologies";

    /**
     * The directory that contains the XSLT stylesheet
     */
    private final String xslDirectory = "xsl";
    
    /**
     * The filename of the XSLT stylesheet to use
     */
    private final String xslTransformation = "to-xhtml.xsl";
        
	private Logger logger;
	
	private CacheController cacheController;
	private SemanticWebClient semwebClient;
	
	SailRepository confRepository = null, ontoRepository = null, dataRepository = null, metaDataRepository = null;
	SameAsInferencer sameAsInferencer = null;
	ValueFactory valueFactory = null;
	
	/**
	 * Converts a relative path to an absolute, if necessary
	 * @param	directory
	 * @returns	absolutized path, or null if invalid
	 */
	private String getAbsolutePath(String directory, ServletContext context) {
		if (directory != null && !new File(directory).isDirectory() && new File(context.getRealPath(directory)).isDirectory())
			return context.getRealPath(directory);
		else
			return directory;
	}
		
	/**
	 * Reads servlet configuration, initializes Sesame repositories and loads ontologies
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		ServletContext context = config.getServletContext();
		
		dataRoot = getAbsolutePath(config.getInitParameter("dataRoot"), context);
		
		if (! (dataRoot != null && new File(dataRoot).isDirectory()))
			throw new ServletException("Invalid dataRoot " + (dataRoot == null ? "(null)" : dataRoot));
		
		try {
			if (config.getInitParameter("assetsURL") != null)
				assetsURL = new URL(config.getInitParameter("assetsURL"));
		}
		catch (Exception e) {
			throw new ServletException("Invalid assetsURL " + config.getInitParameter("assetsURL"));
		}

		try {
			if (config.getInitParameter("serviceURL") != null)
				serviceURL = new URL(config.getInitParameter("serviceURL"));
		}
		catch (Exception e) {
			throw new ServletException("Invalid serviceURL " + config.getInitParameter("serviceURL"));
		}

		org.apache.log4j.BasicConfigurator.configure();
		
		boolean enableDebugging = (config.getInitParameter("enableDebugging") != null
									&& config.getInitParameter("enableDebugging").equalsIgnoreCase("true"));

		Logger.getRootLogger().setLevel(enableDebugging ? Level.DEBUG : Level.WARN);

		/* Set up HTTP Client logging */
// Commented out at this should be configured on the server level		
//		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.Log4JLogger");
//		System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
//		System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire.header", "debug");
//		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");		
		
		/* Set up Sesame MySQL RDBMS */
		try {
			confRepository = new SailRepository(new MemoryStore());
			confRepository.initialize();
			
			ontoRepository = new SailRepository(new NativeStore(new File(getAbsolutePath(config.getInitParameter("ontologyStore"), context))));
			ontoRepository.initialize();

			SailBase baseStore = null;
			
			if (config.getInitParameter("mysqlDb") != null
					&& config.getInitParameter("mysqlServer") != null
					&& config.getInitParameter("mysqlUser") != null
					&& config.getInitParameter("mysqlPass") != null) { 
				MySqlStore myStore = new MySqlStore(config.getInitParameter("mysqlDb"));
				myStore.setServerName(config.getInitParameter("mysqlServer"));
				myStore.setUser(config.getInitParameter("mysqlUser"));
				myStore.setPassword(config.getInitParameter("mysqlPass"));
				myStore.setMaxNumberOfTripleTables(16);
				//myStore.setIndexed(true);
				baseStore = myStore;
			} else {
				baseStore = new NativeStore(new File(getAbsolutePath(config.getInitParameter("cacheStore"), context)));
			}
			
			/* SameAsInferencer requires an InferencerConnection, which is provided by the native store */ 
			sameAsInferencer = new SameAsInferencer(baseStore);
			sameAsInferencer.setAutoInference(false);
			
			dataRepository = new SailRepository(sameAsInferencer);
			dataRepository.initialize();

			metaDataRepository = new SailRepository(new NativeStore(new File(getAbsolutePath(config.getInitParameter("metadataStore"), context))));
			metaDataRepository.initialize();
			
			RepositoryConnection ontoConn = ontoRepository.getConnection();
			ValueFactory ontoValueFactory = ontoRepository.getValueFactory();
			
			/* Load ontologies */
			File ontoDir = new File(dataRoot + "/" + ontologiesDirectory);
			for (File f : ontoDir.listFiles(new RDFFilenameFilter())) {
				URI ontoResource = ontoValueFactory.createURI("file://" + f.getName());

				/* Only load new ontologies */
				if (!ontoConn.hasStatement(null, null, null, false, ontoResource)) {
					try {
						loadTriples(ontoRepository, f, null /* parameters */, ontoResource);
					}
					catch (Exception e) {
						System.err.println("Error loading " + f.getName());
						e.printStackTrace();
					}
				}
			}
			ontoConn.close();
		
			valueFactory = dataRepository.getValueFactory();

			/* Set up external data providers */
			ArrayList<DataProvider> dataProviders = new ArrayList<DataProvider>();
			if (config.getInitParameter("dataProviders") != null) {
				String[] providers = config.getInitParameter("dataProviders").split(",");
				for (String dsName : providers) {
					try {
						DataProvider d = (DataProvider) (this.getClass().getClassLoader().loadClass(dsName)).newInstance();
						dataProviders.add(d);
					}
					catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
	        }
			
			/* Set up Virtuoso Sponger */
			SpongerProvider spongerProvider = null;
			if (config.getInitParameter("spongerServiceURL") != null) {
				spongerProvider = new SpongerProvider(config.getInitParameter("spongerServiceURL"));
			}
			
			cacheController = new CacheController(dataRepository, metaDataRepository);
			semwebClient = new SemanticWebClient(cacheController, spongerProvider, dataProviders);
		} catch (Exception e) { 
			e.printStackTrace();
		}
	}

	/**
	 * Shuts down repositories
	 */
	@Override
	public void destroy() {
		try {
			if (dataRepository != null)
				dataRepository.shutDown();
			
			if (metaDataRepository != null)
				metaDataRepository.shutDown();
			
			if (ontoRepository != null)
				ontoRepository.shutDown();
			
			if (confRepository != null)
				confRepository.shutDown();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.destroy();
	}
	
	/**
	 * Loads fresnel configuration, substituting the "##lang##"
	 * parameter for a specified language
	 * 
	 * @param language	Language tag to use, e.g. "en" or "de"
	 * @throws Exception
	 */
	private void loadFresnelConfig(String language) throws Exception {
		RepositoryConnection configConn = confRepository.getConnection();
		configConn.remove((Resource)null, null, null);
		configConn.commit();
		configConn.close();
		
		HashMap<String,String> parameters = new HashMap<String,String>();
		parameters.put("lang", language);
		
		File fresnelDir = new File(dataRoot + "/" + fresnelDirectory);
		for (File f : fresnelDir.listFiles(new RDFFilenameFilter())) {
				loadTriples(confRepository, f, parameters);
		}
	}
	
	/**
	 * Handles HTTP GET and POST requests
	 *  
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	protected void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException  {
		response.setContentType("text/html");
		response.setCharacterEncoding("utf-8");
		
		/* Try to prevent spidering */
		response.setHeader("X-Robots-Tag", "nofollow");
		OutputStream outputStream = response.getOutputStream();
		
		/* Call REST services */
		if (request.getParameter("do") != null) {
			if (request.getParameter("do").equals("clear")
					&& request.getParameter("url") != null) {
				clearData(request.getParameter("url"), outputStream);
			}
			else if (request.getParameter("do").equals("load")
					&& request.getParameter("url") != null) {
				loadData(request.getParameter("url"), outputStream);
				}
			else if (request.getParameter("do").equals("discover")
					&& request.getParameter("uri") != null) {
				discoverResource(request.getParameter("uri"), outputStream);
			}
		}
		else if (request.getParameter("query") != null) {
			sparqlQuery(request, response, outputStream); /* recognize SPARQL queries using the <code>query</code> parameter */
		}
		else
			fresnelView(request, response); /* default to view generation */
	}
	
	/**
	 * Removes an URL from the cache (<code>clear</code>) 
	 * @param url	The URL to remove
	 * @param outputStream	Output stream to communicate the result to
	 */
	private void clearData(String url, OutputStream outputStream) {
		cacheController.removeData(url);
		PrintWriter writer = new PrintWriter(outputStream);
		writer.print("ok");
		writer.close();
	}

	/**
	 * (Re-)loads an URL into the cache (<code>load</code>) 
	 * @param url	The URL to load
	 * @param outputStream	Output stream to communicate the result to
	 */
	private void loadData(String url, OutputStream outputStream) {
		 try {
			semwebClient.loadURL(new org.apache.commons.httpclient.URI(url, true), true /* wait */);
		} catch (URIException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
		PrintWriter writer = new PrintWriter(outputStream);
		writer.print("ok");
		writer.close();
	}

	/**
	 * Tries to dereference the URI and queries data providers for data;
	 * then follows known predicates (<code>discover</code>)
	 *  
	 * @param uri	The resource of interest 
	 * @param outputStream	Output stream to communicate the result to
	 */
	public void discoverResource(String uri, OutputStream outputStream) {
		Resource mainResource = valueFactory.createURI(uri);
		List<org.apache.commons.httpclient.URI> retrievedURLs = semwebClient.discoverResource(mainResource, false /* don't wait */);
		try {
			sameAsInferencer.addInferredForResource(mainResource);
		} catch (SailException e) {
			e.printStackTrace();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		
		/* List retrieved URLs for debugging purposes */
		PrintWriter writer = new PrintWriter(outputStream);
		if (retrievedURLs != null) {
			for (org.apache.commons.httpclient.URI url : retrievedURLs) {
				writer.println(url.toString() + "<br/>");
			}
		}
		writer.close();
	}

	/**
	 * Handles a SPARQL query. 
	 * The endpoint supports the <code>SELECT</code>, <code>CONSTRUCT</code> and
	 * <code>DESCRIBE</code> query forms; triple serialization is limited to the <code>RDF/XML<code> format.
	 * The indication of graphs using the <code>defaultgraphuri</code> and <code>namedgraphuri</code>
	 * elements is not supported; however graphs may be indicated in the query text using
	 * <code>FROM</code> and <code>FROM NAMED</code> keywords
	 * 
	 * @param request
	 * @param response
	 * @param outputStream
	 */
	private void sparqlQuery(HttpServletRequest request, HttpServletResponse response, OutputStream outputStream) {
		RepositoryConnection conn = null;
		String query = request.getParameter("query");
		
		try {
			conn = dataRepository.getConnection();
			Query q = conn.prepareQuery(QueryLanguage.SPARQL, query);
			
			if (q instanceof TupleQuery) {
				/* <code>SELECT</code> form */
				response.setContentType("application/sparql-results+xml");
				SPARQLResultsXMLWriter sparqlWriter = new SPARQLResultsXMLWriter(outputStream);
				((TupleQuery) q).evaluate(sparqlWriter);
			}
			else if (q instanceof GraphQuery) {
				/* <code>CONSTRUCT</code> and <code>DESCRIBE</code> forms */
				response.setContentType("application/rdf+xml");
				RDFXMLWriterUnique rdfXmlWriter = new RDFXMLWriterUnique(outputStream);
				((GraphQuery) q).evaluate(rdfXmlWriter);
			}
		} catch (MalformedQueryException e) {
			/* Report errors using HTTP 400 Bad Request and provide error details */
			//response.setStatus(400);
			PrintWriter printWriter = new PrintWriter(outputStream);
			printWriter.write("Unable to parse query: " + e.getMessage());
			printWriter.close();
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {		    
		    if (conn != null)
				try {
					conn.close();
				} catch (RepositoryException e) {
					e.printStackTrace();
				}
		}
	}
	
	/**
	 * Discovers an URI and renders a given view for it (<code>view</code>).
	 *
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	public void fresnelView(HttpServletRequest request, HttpServletResponse response) throws IOException  {
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream() , "UTF-8"));
		String errorString = null;
		Selection selected = null;
		List<org.apache.commons.httpclient.URI> retrievedURLs = null;
		Resource focalResource = null;
		Configuration conf = null;
		String redirectLocation = null;
		
		try {
			/* Reload the Fresnel configuration using the provided language */
			String langPref = (request.getParameter("lang") != null ? request.getParameter("lang") : "en");
			loadFresnelConfig(langPref);

			conf = new Configuration(confRepository, ontoRepository);
			
			/* Create the focal resource */
			if (request.getParameter("uri") != null) {
				String uriString = request.getParameter("uri");
				if (uriString.startsWith("_:")) /* blank node */
					focalResource = valueFactory.createBNode(uriString.substring(2));
				else
					focalResource = valueFactory.createURI(uriString);
					
				/* Collect data about the focal resource */
				if (request.getParameter("skipload") == null) {
					retrievedURLs = semwebClient.discoverResource(focalResource, true /* wait */);		
				} /* skip */
				
				/* Initiate manual owl:sameAs inference */
				if (request.getParameter("skipInference") == null) {
					sameAsInferencer.addInferredForResource(focalResource);
				}
			
				if (conf.hasWarnings())
					writer.println(conf.getWarningsString());
				
				Purpose purpose = null;
				
				/* Look up the requested lens purpose */
				if (request.getParameter("purpose") != null && (!request.getParameter("purpose").equals("defaultPurpose")))
					purpose = new Purpose(new URIImpl(Constants.nsFresnelExt + request.getParameter("purpose")));
				else
					purpose = new Purpose(new URIImpl("http://www.w3.org/2004/09/fresnel#defaultLens")); /* this must be provided, or a random lens is chosen */

				try {
					/* Perform Fresnel selection using the requested display purpose and language */
					selected = conf.select(dataRepository, focalResource, purpose, langPref);
		
					/* Perform Fresnel formatting */
					selected = conf.format(dataRepository, selected);
				}
				catch (NoResultsException e) {
					
			        /* 
			         * If no results are found, redirect the user to the resource
			         * if it is not an RDF document.
			         * This code is not reached when there already is some data about the resource.
			         */
			        RepositoryConnection metaDataConn = null;
			    	try {
			    		metaDataConn = metaDataRepository.getConnection();
			    		
						/* Manual support for one level of redirects */
			    		String resourceRedirect = cacheController.getCachedHeaderDataValue(metaDataConn, focalResource, "location");
			    		Resource resourceURI = (resourceRedirect == null ? focalResource : new URIImpl(resourceRedirect));
	
			    		/* Get target content type */
			    		String contentType = cacheController.getCachedHeaderDataValue(metaDataConn, resourceURI, "content-type");
						if (contentType != null && !ContentTypes.isRDF(contentType)) {
							redirectLocation = focalResource.toString();
						}
					} catch (RepositoryException re) {
						re.printStackTrace();
					} catch (IllegalArgumentException ie) {
						ie.printStackTrace();
					}
					finally {
						try {
							if (metaDataConn != null)
								metaDataConn.close();
							}
						catch (RepositoryException re) {
							re.printStackTrace();
						}
					}
				}
			} /* uri != null */
		} catch (Exception e) {
			e.printStackTrace();
			errorString = e.getMessage();
		}	
		
		/* Output */
		try {
			/* Handle redirection to non-RDF data */
			if (redirectLocation != null) {
				response.setHeader("Location", redirectLocation);
				response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
			}
			else { /* Perform XSL output */
				
				/*
				 * When there are no results, we still need a selection object in 
				 * order to render the fresnel tree
				 */
				if (selected == null)
					selected = new Selection(conf);
				
				Document fresnelTree = selected.render();
				addSources(fresnelTree, retrievedURLs);
				
				/* Prepare XSLT */
				StreamSource styleSource = new StreamSource(new File(dataRoot + "/" + xslDirectory + "/" + xslTransformation));
				net.sf.saxon.TransformerFactoryImpl tf = new net.sf.saxon.TransformerFactoryImpl();
				Transformer styleTransformer = tf.newTransformer(styleSource);
				
				/* Debug output */
				if (request.getParameter("debug") != null) {
					/* debug output: fresnel tree */
					StringWriter stringWriter = new StringWriter();
					Transformer treeTransformer = tf.newTransformer();
					treeTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
					treeTransformer.transform(new DOMSource(fresnelTree), new StreamResult(stringWriter));
					styleTransformer.setParameter("fresnelTree", stringWriter.getBuffer().toString());
				}
				
				/* Apply parameters */
				styleTransformer.setParameter("assetsURL", assetsURL != null ? assetsURL.toString() : "");
				styleTransformer.setParameter("serviceURL", serviceURL != null ? serviceURL.toString() : "");
				styleTransformer.setParameter("errorString", errorString);
				styleTransformer.setParameter("mainResource", focalResource != null ? focalResource.toString() : "");
				if (request.getParameter("purpose") != null)
					styleTransformer.setParameter("purpose", request.getParameter("purpose"));
				else
					styleTransformer.setParameter("purpose", "defaultPurpose");
				
				if (request.getParameter("mobile") != null)
					styleTransformer.setParameter("isMobile", request.getParameter("mobile"));
				
				HashMap<String, String[]> newParameters = new HashMap<String,String[]>(request.getParameterMap());
				
				if (!newParameters.containsKey("lang"))
					newParameters.put("lang", new String[]{"en"});
				
				for (Object key : new String[]{"purpose", "uri"}) {
					newParameters.remove(key);
				}
				
				String parameterString = "";
				
				/* Serialize parameters for use in HTML links and forms */
				for (Object key : newParameters.keySet()) {
					parameterString += (parameterString.equals("") ? "" : "&") + key + "=" + ((String[]) newParameters.get(key))[0];
				}
				
				styleTransformer.setParameter("sessionParams", parameterString);
							
				/* Perform rendering */
				StreamResult res = new StreamResult(writer);
				styleTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
				styleTransformer.transform(new DOMSource(fresnelTree), res);
			}
		} catch (Exception e) {
			e.printStackTrace();
			writer.print(e.getMessage());
		}
		finally {
		}
	}
 	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException  {
		handleRequest(request, response);
	}   

	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		handleRequest(request, response);
	}   
	
	/**
	 * Reads data from a URL in a given syntax into a Sesame repository;
	 * supports {@link ParamReader}
	 * 
	 * @param store Add RDF to this <code>LocalRepository</code>
	 * @param url The <code>String</code> location of the data
	 * @param syntax The <code>String</code> syntax of the data
	 * @throws Exception For any problems encountered during read of data from the URL
	 */
	public static void loadTriples(Repository store, File file, HashMap<String,String> parameters, Resource ... context) throws Exception {
		RDFFormat format = null;
		if (file.getName().endsWith(".nt"))
				format = RDFFormat.NTRIPLES;
		else if (file.getName().endsWith(".rdf"))
				format = RDFFormat.RDFXML;
		else if (file.getName().endsWith(".n3"))
			format = RDFFormat.N3;
		else if (file.getName().endsWith(".ttl"))
			format = RDFFormat.TURTLE;
		else
			throw new ServletException("No RDF format known for " + file.getName());
	
		String baseURI = "file://" + file.getAbsoluteFile();
		RepositoryConnection conn = store.getConnection();
		
		BufferedReader fileReader = new BufferedReader(new FileReader(file));
		if (parameters != null) {
			ParamReader paramReader = new ParamReader(parameters, fileReader);
			conn.add(paramReader, baseURI, format, context);
		}
		else
			conn.add(fileReader, baseURI, format, context);

		conn.commit();
		conn.close();
	}

	/**
	 * Filename filter that accepts only file extensions commonly used for RDF data, namely
	 * <code>.nt</code>, <code>.rdf</code>, <code>.ttl</code>, <code>.n3</code>
	 */
	private class RDFFilenameFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			return (!name.startsWith(".")) && (name.endsWith(".nt") || name.endsWith(".rdf") || name.endsWith(".ttl") || name.endsWith(".n3"));
		}
	}

	/**
	 * Enhances source data with consistently colored icons;
	 * adds detailed source list to Fresnel output
	 * 
	 * @param doc	The Fresnel tree 
	 */
	private void addSources(Document doc, List<org.apache.commons.httpclient.URI> retrievedURLs) {
		int colorIndex = 0;
		HashMap<String,Source> sources = new HashMap<String,Source>();
		
		NodeList nodeList = doc.getElementsByTagName("source");
		int numNodes = nodeList.getLength();
		
        for (int i=0; i < numNodes; i++) {
        	Node node = nodeList.item(i);
        	String uri = node.getFirstChild().getFirstChild().getNodeValue();
        	
        	Source source;
        	
        	/* Get source, create it if necessary */
        	if (null == (source = sources.get(uri))) {
        		source = new Source(uri);
        		colorIndex = source.determineIcon(colorIndex);
        		sources.put(uri, source);
        	}
        	
        	/* Enhance source reference with icon */
        	Element sourceIcon = doc.createElementNS(Constants.nsFresnelView, "sourceIcon");
			sourceIcon.appendChild(doc.createTextNode(source.getIcon()));
    		node.appendChild(sourceIcon);
		}
                
        /* Supplement source list with retrieved URLs */
        if (retrievedURLs != null)
        	for (org.apache.commons.httpclient.URI uri : retrievedURLs) {
	        	Source source;
	        	if (null == (source = sources.get(uri.toString()))) {
	        		source = new Source(uri.toString());
	        		colorIndex = source.determineIcon(colorIndex);
	        		sources.put(uri.toString(), source);
	        	}
        }
        
        /* Provide list of sources */
        RepositoryConnection metaDataConn = null;
    	try {
    		metaDataConn = metaDataRepository.getConnection();
	        Element sourcesElement = doc.createElementNS(Constants.nsFresnelView, "sources");
	        for (String uri : sources.keySet()) {
	        	Source source = sources.get(uri);
	        	sourcesElement.appendChild(source.toElement(doc, cacheController, metaDataConn));
	        }
	        
	        Node results = doc.getFirstChild();
	        results.appendChild(sourcesElement);
	        
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (metaDataConn != null)
					metaDataConn.close();
				}
			catch (RepositoryException e) {
				e.printStackTrace();
			}
		}
	}
}
