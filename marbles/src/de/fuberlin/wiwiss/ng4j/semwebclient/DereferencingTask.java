package de.fuberlin.wiwiss.ng4j.semwebclient;

import de.fuberlin.wiwiss.marbles.loading.DereferencingListener;

/**
 * A DereferencingTask represents a URI which has to be retrieved. 
 */
public class DereferencingTask {
	private DereferencingListener listener;
	private String uri;
	private int step;
	
	public DereferencingTask(DereferencingListener listener, String uri, int step) {
		this.listener = listener;
		this.step = step;
		this.uri  = uri;
	}

	public DereferencingListener getListener() {
		return this.listener;
	}
	
	public int getStep(){
		return this.step;
	}
	
	public String getURI(){
		return this.uri;
	}
}
