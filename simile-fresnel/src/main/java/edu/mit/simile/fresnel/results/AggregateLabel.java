package edu.mit.simile.fresnel.results;

import java.util.Iterator;

/**
 * Result of applying a label lens.  As formatting may be necessary, the selection
 * portion is stored and included in the formatting phase.  A string representation
 * is generated when needed.  This can render any given Result to a string and need
 * not be constructed with one to do so.
 * 
 * @author ryanlee
 */
public class AggregateLabel {
	/**
	 * Label lens result
	 */
	private Result _result;
	
	/**
	 * Base constructor
	 */
	public AggregateLabel() {
		super();
	}
	
	/**
	 * Constructor with a lens result.
	 * 
	 * @param result A <code>Result</code>
	 */
	public AggregateLabel(Result result) {
		super();
		this._result = result;
	}
	
	/**
	 * Renders a result as a string.
	 * I am considering labelFormat and propertyFormat irrelevant here.  Whatever could have
	 * been accomplished with propertyFormat when it comes to labels should be done instead
	 * with valueFormats since there is effectively no property info displayed.  resourceFormat
	 * (before and after) and valueFormat (all) are the only rendered content parts.
	 * 
	 * @param result A <code>Result</code>
	 * @return A <code>String</code> representation of the result
	 */
	protected String renderResult(Result result) {
		String out = "";
		String rBefore = result.getContents().getBefore();
		String rAfter = result.getContents().getAfter();
		if (null != rBefore) out += rBefore;
		for (Iterator<PropertyResult> pri = result.getProperties().propertyResultIterator(); pri.hasNext(); ) {
			PropertyResult pr = pri.next();
			String first = pr.getValues().getContents().getFirst();
			String before = pr.getValues().getContents().getBefore();
			String after = pr.getValues().getContents().getAfter();
			String last = pr.getValues().getContents().getLast();
						
			first = (null == first) ? before : first;
			last = (null == last) ? after : last;
			
			boolean firstRound = true;
			
			for (Iterator<ValueResult> vri = pr.getValues().valueResultIterator(); vri.hasNext(); ) {
				ValueResult vr = vri.next();
				
				if (firstRound) {
					if (null != first) {
						out += first;
					} else if (null == first && null != before) {
						out += before;
					} else {
						out += " ";
					}
					
					firstRound = false;
				} else {
					if (null != before) {
						out += before;
					} else {
						out += " ";
					}
				}
				
				if (vr.isResource()) {
					out += renderResult(vr.getResult());
				} else {
					out += vr.getTitle().getString();
				}
				
				if (vri.hasNext()) {
					if (null != after) {
						out += after;
					}
				} else {
					if (null == last && null != after) {
						out += after;
					} else if (null != last) {
						out += last;
					}
				}
			}
		}
		if (null != rAfter) out += rAfter;
		return out;	
	}
	
	/**
	 * Retrieves the result this label is based on.
	 * 
	 * @return A <code>Result</code>
	 */
	public Result getResult() {
		return this._result;
	}
	
	/**
	 * Sets the result this label should be based on.
	 * 
	 * @param result A <code>Result</code>
	 */
	public void setResult(Result result) {
		this._result = result;
	}
	
	/**
	 * Render this label as a string based on the base result.
	 * 
	 * @return A <code>String</code> rendering
	 */
	public String getString() {
		return renderResult(this._result).trim();
	}
}
