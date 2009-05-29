package de.fuberlin.wiwiss.marbles.loading;

import java.util.List;

import org.apache.commons.httpclient.HttpMethod;
import org.openrdf.model.Graph;

import de.fuberlin.wiwiss.ng4j.semwebclient.DereferencingTask;

/**
 * The dereferencing result contains informations about the
 * success or failure of a DereferencingTasks execution.
 * 
 * @author Tobias Gauﬂ
 * 
 * Adapted to the Apache HTTP Client and Sesame by Christian Becker
 */
public class DereferencingResult {
	public final static int STATUS_OK = 0;
	public final static int STATUS_PARSING_FAILED = -1;
	public final static int STATUS_MALFORMED_URL = -2;
	public final static int STATUS_UNABLE_TO_CONNECT = -3;
	public final static int STATUS_NEW_URIS_FOUND = -4;

	private DereferencingTask task;
	private int resultCode;
	private Graph resultData;
	private Exception resultException;
	private List urilist = null;
	private HttpMethod method;

	public DereferencingResult(DereferencingTask task, int resultCode, 
			HttpMethod method, Graph resultData, Exception resultException) {
		this.task = task;
		this.resultCode = resultCode;
		this.resultData = resultData;
		this.resultException = resultException;
		this.method = method;
	}
	
	public DereferencingResult(DereferencingTask task, int resultCode, List urilist) {
		this.task = task;
		this.resultCode = resultCode;
		this.urilist = urilist;
		
	}

	public DereferencingTask getTask() {
		return this.task;
	}
	
	public int getResultCode() {
		return this.resultCode;
	}
	
	public Graph getResultData() {
		return this.resultData;
	}
	
	public void setResultData(Graph resultData) {
		this.resultData = resultData;
	}
	
	public String getURI() {
		return this.task.getURI();
	}

	public String getErrorMessage() {
		if (this.resultException == null) {
			return null;
		}
		return this.resultException.getMessage();
	}
	
	public boolean isSuccess() {
		return this.resultCode == DereferencingResult.STATUS_OK;
	}
	
	public List getUriList(){
		return this.urilist;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public void setMethod(HttpMethod method) {
		this.method = method;
	}

	public Exception getResultException() {
		return resultException;
	}

	public void setResultException(Exception resultException) {
		this.resultException = resultException;
	}

	public void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}
}
