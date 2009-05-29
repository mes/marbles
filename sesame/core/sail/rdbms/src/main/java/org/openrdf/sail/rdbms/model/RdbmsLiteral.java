/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.model;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;

/**
 * Wraps a {@link LiteralImpl} providing an internal id and version.
 * 
 * @author James Leigh
 * 
 */
public class RdbmsLiteral extends RdbmsValue implements Literal {

	private static final long serialVersionUID = -8213249522968522279L;

	private Literal lit;
	private URI predicate = null;

	public RdbmsLiteral(Literal lit, URI predicate) {
		this.lit = lit;
		this.predicate = predicate;
	}

	public RdbmsLiteral(Number id, Integer version, Literal lit, URI predicate) {
		super(id, version);
		this.lit = lit;
		this.predicate = predicate;
	}

	public boolean booleanValue() {
		return lit.booleanValue();
	}

	public byte byteValue() {
		return lit.byteValue();
	}

	public XMLGregorianCalendar calendarValue() {
		return lit.calendarValue();
	}

	public BigDecimal decimalValue() {
		return lit.decimalValue();
	}

	public double doubleValue() {
		return lit.doubleValue();
	}

	public float floatValue() {
		return lit.floatValue();
	}

	public URI getDatatype() {
		return lit.getDatatype();
	}

	public URI getPredicate() {
		return predicate;
	}

	public void setPredicate(URI predicate) {
		this.predicate = predicate;
	}

	public String getLabel() {
		return lit.getLabel();
	}

	public String getLanguage() {
		return lit.getLanguage();
	}

	public BigInteger integerValue() {
		return lit.integerValue();
	}

	public int intValue() {
		return lit.intValue();
	}

	public long longValue() {
		return lit.longValue();
	}

	public short shortValue() {
		return lit.shortValue();
	}

	public String stringValue() {
		return lit.stringValue();
	}

	@Override
	public String toString() {
		return lit.toString();
	}

	@Override
	public boolean equals(Object other) {
		return lit.equals(other);
	}

	@Override
	public int hashCode() {
		return lit.hashCode();
	}
}
