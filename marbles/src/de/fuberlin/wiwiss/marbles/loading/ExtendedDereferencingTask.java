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

import de.fuberlin.wiwiss.ng4j.semwebclient.DereferencingTask;

/**
 * Extends {@link DereferencingTask} with <code>redirectStep</code>, 
 * <code>done</code> and <code>forceReload</code> properties
 * 
 * @author Christian Becker
 */

public class ExtendedDereferencingTask extends DereferencingTask {
	
	private boolean done = false;
	private int redirectStep;
	private boolean forceReload;
	
	public int getRedirectStep() {
		return redirectStep;
	}

	public void setRedirectStep(int redirectStep) {
		this.redirectStep = redirectStep;
	}

	/**
	 * Constructs an <code>ExtendedDereferencingTask</code>
	 * @param listener
	 * @param uri
	 * @param step	The distance from the focal resource
	 * @param redirectStep	The number of redirects performed in the course of this individual request
	 * @param forceReload	Set this to true if the URL should be loaded even if a valid copy is already in the cache
	 */
	public ExtendedDereferencingTask(DereferencingListener listener,
			String uri, int step, int redirectStep, boolean forceReload) {
		super(listener, uri, step);
		this.redirectStep = redirectStep;
		this.forceReload = forceReload;
	}

	/**
	 * @return	true, if the URL has been loaded
	 */
	public boolean isDone() {
		return done;
	}

	/**
	 * @param	done	Set this to true when the URL has been loaded
	 */
	public void setDone(boolean done) {
		this.done = done;
	}
	
	/**
	 * @return	true, if the URL should be loaded even if a valid copy is already in the cache
	 */
	public boolean isForceReload() {
		return forceReload;
	}
}
