/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.io.File;
import java.io.IOException;

import org.openrdf.sail.nativerdf.btree.RecordIterator;

/**
 * A cache for fixed size byte array records. This cache uses a temporary file
 * to store the records. This file is deleted upon calling {@link #discard()}.
 * 
 * @author Arjohn Kampman
 */
abstract class RecordCache {

	/*-----------*
	 * Constants *
	 *-----------*/

	protected final File cacheFile;

	protected final long maxRecords;

	/*-----------*
	 * Variables *
	 *-----------*/

	private long recordCount;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public RecordCache(File cacheDir)
		throws IOException
	{
		this(cacheDir, Long.MAX_VALUE);
	}

	public RecordCache(File cacheDir, long maxRecords)
		throws IOException
	{
		this.cacheFile = File.createTempFile("records", ".tmp", cacheDir);
		this.maxRecords = maxRecords;
		this.recordCount = 0;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void discard()
		throws IOException
	{
		cacheFile.delete();
	}

	public final boolean isValid() {
		return recordCount < maxRecords;
	}

	public final void storeRecord(byte[] data)
		throws IOException
	{
		if (isValid()) {
			storeRecordInternal(data);
			recordCount++;
		}
	}

	public final void storeRecords(RecordCache other)
		throws IOException
	{
		if (isValid()) {
			RecordIterator recIter = other.getRecords();
			try {
				byte[] record;

				while ((record = recIter.next()) != null) {
					storeRecordInternal(record);
				}
			}
			finally {
				recIter.close();
			}
		}
	}

	public abstract void storeRecordInternal(byte[] data)
		throws IOException;

	public final RecordIterator getRecords() {
		if (isValid()) {
			return getRecordsInternal();
		}

		throw new IllegalStateException();
	}

	public abstract RecordIterator getRecordsInternal();

	public final long getRecordCount() {
		if (isValid()) {
			return recordCount;
		}

		throw new IllegalStateException();
	}
}
