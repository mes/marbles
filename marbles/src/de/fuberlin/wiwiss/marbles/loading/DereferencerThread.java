package de.fuberlin.wiwiss.marbles.loading;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.params.HttpParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.OpenRDFUtil;
import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;

import de.fuberlin.wiwiss.ng4j.semwebclient.DereferencingTask;
import de.fuberlin.wiwiss.ng4j.semwebclient.LimitedInputStream;

/**
 * The DereferencerThread executes a given DereferencingTask. It opens a
 * HttpURLConnection, creates an InputStream and tries to parse it. If the
 * Thread is finished it delivers the retrieval result.
 * 
 * @author Tobias Gau�
 * Adapted to the Apache HTTP Client and Sesame by Christian Becker
 */
public class DereferencerThread extends Thread {
	private final static int MAX_REDIRECTS = 3;
	
	private DereferencingTask task = null;

	private boolean stopped = false;
	private int maxfilesize = -1;
	private Log log = LogFactory.getLog(DereferencerThread.class);
	private HttpClient httpClient;
	private SpongerProvider spongerProvider;

	public DereferencerThread(HttpClient httpClient, SpongerProvider spongerProvider) {
		this.httpClient = httpClient;
		this.spongerProvider = spongerProvider;
	}

	public void run() {
		this.log.debug("Thread started.");
		while (!this.stopped) {
			if (hasTask()) {
				DereferencingResult result = dereference();
				deliver(result);
				clearTask();
			}
			try {
				synchronized (this) {
					if (this.stopped) {
						break;
					}
					wait();
				}
			} catch (InterruptedException ex) {
				// Happens when the thread is stopped
			}
		}
		this.log.debug("Thread stopped.");
	}

	/**
	 * @return Returns true if the DereferencerThread is available for new
	 *         tasks.
	 */
	public synchronized boolean isAvailable() {
		return !hasTask() && !this.stopped;
	}

	/**
	 * Starts to execute the DereferencingTask task. Returns true if the
	 * retrieval process is started false if the thread is unable to execute the
	 * task.
	 * 
	 * @param task
	 *            The task to execute.
	 * @return
	 */
	public synchronized boolean startDereferencingIfAvailable(
			DereferencingTask task) {
		if (!isAvailable()) {
			return false;
		}
		this.task = task;
		this.notify();
		return true;
	}

	/**
	 * @return Returns true if the DereferencerThread is busy false if not.
	 */
	private boolean hasTask() {
		return task != null;
	}

	/**
	 * Clears the DereferencerThreads tasks.
	 */
	private synchronized void clearTask() {
		task = null;
	}

	/**
	 * Creates a new DereferencingResult which contains information about the
	 * retrieval failure.
	 * 
	 * @param errorCode
	 *            the error code
	 * @param exception
	 *            the thrown exception
	 * @return
	 */
	private DereferencingResult createNewUrisResult(int errorCode, ArrayList urilist) {
		return new DereferencingResult(task, errorCode, urilist);
	}	
	
	
	/**
	 * Delivers the retrieval result.
	 * 
	 * @param result
	 */
	private /*synchronized*/ void deliver(DereferencingResult result) {
		if (stopped) {
			return;
		}
		task.getListener().dereferenced(result);
	}

	private HttpMethod getURL(String uriString, boolean externallyObtained) throws HttpException, IOException, URIException {
		GetMethod method = new GetMethod();
		org.apache.commons.httpclient.URI uri = new org.apache.commons.httpclient.URI(uriString, true);
		method.setURI(uri);

		/* Set read timeout based on step level */
		HttpMethodParams params = new HttpMethodParams();
		params.setSoTimeout(task.getStep() == 0 ? 30 * 1000 : 15 * 1000);
		method.setParams(params);
		
		method.setRequestHeader(
				"Accept", 
				"application/rdf+xml;q=1,"
				+ "text/xml;q=0.6,text/rdf+n3;q=0.9,"
				+ "application/octet-stream;q=0.5,"
				+ "application/xml;q=0.5,"
				+ "text/plain;q=0.5,application/x-turtle;q=0.5,"
				+ "application/x-trig;q=0.5,"
				+ "application/xhtml+xml;q=0.5,"
				+ "text/html;q=0.5"
				);
		
		method.setRequestHeader("User-Agent", "marbles/0.9 (http://dbpedia.org/marbles)");
				
		/* Own redirect handling in order to tie it in with caching etc. */
		method.setFollowRedirects(false);

		/* a little security by obscurity */
		if (!(externallyObtained && (method.getURI().getHost().contains("localhost")
				|| method.getURI().getHost().contains("127.")
				|| method.getURI().getHost().contains("10."))))
			httpClient.executeMethod(method);
		
		log.debug((method.getStatusLine() != null ? method.getStatusCode() : "(fail)") + " " + uriString);
//			       + " (" + (contentType != null ? contentType.getValue() : "(null)") + ")");
		
		return method;
	}
	
	private DereferencingResult dereference() {
		DereferencingResult result = new DereferencingResult(task, 0, null /* method */, null /* graph */, null /* exception */);
		HttpMethod method = null;
		
		try {
			method = getURL(task.getURI(), true);
			result.setMethod(method);
			Header contentType = method.getResponseHeader("Content-Type");

			/* Interpret response for successful requests */
			if (HttpStatusCodes.isSuccess(method.getStatusCode())) {		
				RDFFormat format = guessFormat(contentType != null ? contentType.getValue() : null);
				Graph g = null;

				try {
					g = parseRdf(method, format);
				} catch (Exception e) {
					/* Couldn't parse this - try it again via the Sponger proxy. */
					if (spongerProvider != null) {
						method.releaseConnection();
						method = getURL(spongerProvider.getQueryURL(task.getURI()), false);
						result.setMethod(method);
						g = parseRdf(method, format);
					}
				}
				
				result.setResultData(g);
			} else if (!HttpStatusCodes.isRedirect(method.getStatusCode()))
				result.setResultCode(DereferencingResult.STATUS_PARSING_FAILED);
		} catch (URIException e) {
			e.printStackTrace();
			log.debug(e.getMessage());
			result.setResultCode(DereferencingResult.STATUS_MALFORMED_URL);
			result.setResultException(e);
		} catch (HttpException e) {
			log.debug(e.getMessage());
			result.setResultCode(DereferencingResult.STATUS_PARSING_FAILED);
			result.setResultException(e);
		}
		catch (IOException e) {
			log.debug(e.getMessage());
			result.setResultCode(DereferencingResult.STATUS_UNABLE_TO_CONNECT);
			result.setResultException(e);
		} catch (Exception e) {
			log.debug(e.getMessage());
			result.setResultCode(DereferencingResult.STATUS_PARSING_FAILED);
			result.setResultException(e);
		}
		finally {
			if (method != null)
				method.releaseConnection();
		}
		
		return result;
	}

	/**
	 * Parses an RDF String.
	 * @throws IOException 
	 * @throws RDFHandlerException 
	 * @throws RDFParseException 
	 */
	private Graph parseRdf(HttpMethod method, RDFFormat format) throws RDFParseException, RDFHandlerException, IOException {
		LimitedInputStream lis = new LimitedInputStream(method.getResponseBodyAsStream(), maxfilesize);
		Graph graph = new GraphImpl();
		URI urlContext = graph.getValueFactory().createURI(task.getURI().toString());
		
		addData(graph, lis, format, task.getURI().toString(), urlContext);
		
		return graph;
	}
	
	/* Adapted from Sesame's RepositoryConnectionBase.addInputStreamOrReader() */
	private void addData(Graph graph, InputStream is, RDFFormat dataFormat, String baseURI, Resource... contexts) throws RDFParseException, RDFHandlerException, IOException
	 {
		OpenRDFUtil.verifyContextNotNull(contexts);

		RDFParser rdfParser = Rio.createParser(dataFormat, graph.getValueFactory());

		rdfParser.setVerifyData(true);
		rdfParser.setStopAtFirstError(true);
		rdfParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);

		RDFGraphInserter rdfInserter = new RDFGraphInserter(graph, contexts);
		rdfParser.setRDFHandler(rdfInserter);
		rdfParser.parse(is, baseURI);
	}		

	/**
	 * Tries to guess an RDF Format from a connection.
	 * 
	 * @return
	 */
	private RDFFormat guessFormat(String contentType) {
		if (contentType == null
				|| ContentTypes.isRDFXML(contentType))
			return RDFFormat.RDFXML;
		else if (ContentTypes.isRDFN3(contentType))
			return RDFFormat.N3;
		else if (ContentTypes.isRDFTTL(contentType))
			return RDFFormat.TURTLE;
		else
			return RDFFormat.RDFXML; /* worth a try... */
	}

	/**
	 * Stops the UriConnector from retrieving the URI.
	 */
	public synchronized void stopThread() {
		stopped = true;
		interrupt();
	}
	
	public synchronized void setMaxfilesize(int size){
		maxfilesize = size;
	}
}
