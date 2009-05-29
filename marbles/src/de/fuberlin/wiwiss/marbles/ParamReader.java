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
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

/**
 * A {@link Reader} wrapper that supports transparent parameter replacement.
 * Text contains parameters identified by double hash signs, for example: 
 * 
 * <code>
 * 	&lt;tag xml:lang="##lang##"/&gt;
 * </code>
 * 
 * A corresponding parameter "lang" may then be used to provide a replacement that is
 * transparently substituted.
 *
 * @author Christian Becker
 */

public class ParamReader extends Reader {
	
	private HashMap<String, String> parameters;
	private BufferedReader baseReader;
	private String currentLine = new String();
	public int linePos = 0;

	/**
	 * Constructs a new <code>ParamReader</code>
	 * @param parameters	A map with parameter names as keys and their replacements as values
	 * @param baseReader	The underyling reader
	 */
	public ParamReader(HashMap<String,String> parameters, BufferedReader baseReader) {
		super();
		this.parameters = parameters;
		this.baseReader = baseReader;
	}

	@Override
	public void close() throws IOException {
		baseReader.close();
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		int bytesRead = 0;
		
		if (linePos >= currentLine.length()) {
			linePos = 0;
			currentLine = baseReader.readLine();
			if (currentLine == null)
				return -1;
			currentLine += "\n";
			for (String key : parameters.keySet()) {
				String value = parameters.get(key);
				currentLine = currentLine.replace("##" + key + "##", value);
			}
		}
		
		int numToBeRead = Math.min(currentLine.length() - linePos, len);
		currentLine.getChars(linePos, linePos + numToBeRead, cbuf, off);

		linePos += numToBeRead;
		bytesRead += numToBeRead;
		return bytesRead;
	}
}
