/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.transaction.operations;

import info.aduna.lang.ObjectUtil;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * Operation that removes the namespace for a specific prefix.
 * 
 * @author Arjohn Kampman
 */
public class RemoveNamespaceOperation implements TransactionOperation {

	private String prefix;

	public RemoveNamespaceOperation() {
	}

	public RemoveNamespaceOperation(String prefix) {
		setPrefix(prefix);
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void execute(RepositoryConnection con)
		throws RepositoryException
	{
		con.removeNamespace(prefix);
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof RemoveNamespaceOperation) {
			RemoveNamespaceOperation o = (RemoveNamespaceOperation)other;
			return ObjectUtil.nullEquals(getPrefix(), o.getPrefix());
		}

		return false;
	}

	@Override
	public int hashCode()
	{
		return ObjectUtil.nullHashCode(getPrefix());
	}
}
