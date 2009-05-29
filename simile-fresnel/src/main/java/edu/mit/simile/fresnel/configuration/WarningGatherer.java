package edu.mit.simile.fresnel.configuration;

import java.util.Iterator;
import java.util.Vector;

/**
 * Gathers warnings during parse of configuration graph.
 * 
 * @author ryanlee
 */
public class WarningGatherer {
	/**
	 * Contains warnings.
	 */
	private Vector<Exception> _warnings;
	
	/**
	 * Constructor
	 */
	public WarningGatherer() {
		this._warnings = new Vector<Exception>();
	}
	
	/**
	 * Add a warning to the gathered set
	 * 
	 * @param warning An <code>Exception</code>
	 */
	public void addWarning(Exception warning) {
		this._warnings.add(warning);
	}
	
	/**
	 * Returns the number of warnings gathered
	 * 
	 * @return The <code>int</code> number of warnings
	 */
	public int size() {
		return this._warnings.size();
	}
	
	/**
	 * Convert warnings to a string, surrounding each individual warning with the
	 * begin and end strings.
	 * 
	 * @param begin Prefix <code>String</code> per warning
	 * @param end Suffix <code>String</code> per warning
	 * @return A <code>String</code> of all warnings interleaved with begin and end strings
	 */
	public String toString(String begin, String end) {
		String out = "";
		for (Iterator it = this._warnings.iterator(); it.hasNext(); ) {
			out += begin + ((Exception) it.next()).toString() + end + "\n";
		}
		return out;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String out = "";
		for (Iterator it = this._warnings.iterator(); it.hasNext(); ) {
			out += ((Exception) it.next()).toString() + "\n";
		}
		return out;
	}
}
