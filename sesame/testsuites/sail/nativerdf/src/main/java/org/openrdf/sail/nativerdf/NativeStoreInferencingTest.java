/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.io.File;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;

import info.aduna.io.FileUtil;

import org.openrdf.sail.InferencingTest;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;

public class NativeStoreInferencingTest extends TestCase {

	private static File dataDir;

	public static Test suite()
		throws SailException, IOException
	{
		dataDir = FileUtil.createTempDir("nativestore");
		Sail sailStack = new NativeStore(dataDir, "spoc,posc");
		sailStack = new ForwardChainingRDFSInferencer(sailStack);
		return InferencingTest.suite(sailStack);
	}

	@Override
	protected void finalize()
		throws Throwable
	{
		if (dataDir != null) {
			FileUtil.deleteDir(dataDir);
			dataDir = null;
		}

		super.finalize();
	}
}
