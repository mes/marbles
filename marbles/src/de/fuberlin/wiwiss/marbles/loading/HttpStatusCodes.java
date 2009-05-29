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
package de.fuberlin.wiwiss.marbles.loading;

import org.apache.commons.httpclient.HttpStatus;

/**
 * Provides functionalities to work with HTTP status codes  
 * @author Christian Becker
 *
 */
public class HttpStatusCodes {

	/**
	 * Known redirection codes 
	 */
	private final static int[] knownRedirects = { 
		HttpStatus.SC_MOVED_PERMANENTLY, 
		HttpStatus.SC_MOVED_TEMPORARILY,
		HttpStatus.SC_SEE_OTHER,
		HttpStatus.SC_TEMPORARY_REDIRECT };
	
	/**
	 * Determines whether a given status code indicates a redirect
	 *  
	 * @param statusCode	The status code of interest
	 * @return	true, if the status code indicates a redirect
	 */
	public static boolean isRedirect(int statusCode) {
		for (int code : knownRedirects) {
			if (code == statusCode)
				return true;
		}
		
		return false;
	}
	
	/**
	 * Determines whether a given status code indicates a successful operation
	 *  
	 * @param statusCode	The status code of interest
	 * @return	true, if the status code indicates success
	 */
	public static boolean isSuccess(int statusCode) {
		return (statusCode >= 200 && statusCode <= 299);
	}
	
	/**
	 * Provides an extremely simplified string representation
	 * of the status code's meaning
	 * 
	 * @param statusCode	The status code of interest
	 * @return	"success", "redirect" or "failed"
	 */
	public static String toString(int statusCode) {
		return (isSuccess(statusCode) ? "success"
				: (isRedirect(statusCode) ? "redirect"
						: "failed"));
	}
}