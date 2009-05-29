/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.algebra.factories;

import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.sail.rdbms.algebra.base.SqlExpr;
import org.openrdf.sail.rdbms.exceptions.UnsupportedRdbmsOperatorException;

/**
 * Boolean SQL expression factory. This factory can convert a number of core
 * algebra nodes into an SQL expression.
 * 
 * @author James Leigh
 * 
 */
public class SqlExprFactory {

	private BNodeExprFactory bnode;

	private BooleanExprFactory bool;

	private DatatypeExprFactory datatype;

	private LabelExprFactory label;

	private LanguageExprFactory language;

	private NumericExprFactory numeric;

	private TimeExprFactory time;

	private URIExprFactory uri;

	private ZonedExprFactory zoned;

	private HashExprFactory hash;
	private PointExprFactory point;

	public void setBNodeExprFactory(BNodeExprFactory bnode) {
		this.bnode = bnode;
	}

	public void setBooleanExprFactory(BooleanExprFactory bool) {
		this.bool = bool;
	}

	public void setDatatypeExprFactory(DatatypeExprFactory datatype) {
		this.datatype = datatype;
	}

	public void setLabelExprFactory(LabelExprFactory label) {
		this.label = label;
	}

	public void setLanguageExprFactory(LanguageExprFactory language) {
		this.language = language;
	}

	public void setNumericExprFactory(NumericExprFactory numeric) {
		this.numeric = numeric;
	}

	public void setTimeExprFactory(TimeExprFactory time) {
		this.time = time;
	}

	public void setURIExprFactory(URIExprFactory uri) {
		this.uri = uri;
	}

	public void setZonedExprFactory(ZonedExprFactory zoned) {
		this.zoned = zoned;
	}

	public void setHashExprFactory(HashExprFactory hash) {
		this.hash = hash;
	}
	
	public void setPointExprFactory(PointExprFactory point) {
		this.point = point;
	}	

	public SqlExpr createBNodeExpr(ValueExpr arg)
		throws UnsupportedRdbmsOperatorException
	{
		return bnode.createBNodeExpr(arg);
	}

	public SqlExpr createBooleanExpr(ValueExpr arg)
		throws UnsupportedRdbmsOperatorException
	{
		return bool.createBooleanExpr(arg);
	}

	public SqlExpr createLabelExpr(ValueExpr arg)
		throws UnsupportedRdbmsOperatorException
	{
		return label.createLabelExpr(arg);
	}

	public SqlExpr createLanguageExpr(ValueExpr arg)
		throws UnsupportedRdbmsOperatorException
	{
		return language.createLanguageExpr(arg);
	}

	public SqlExpr createNumericExpr(ValueExpr arg)
		throws UnsupportedRdbmsOperatorException
	{
		return numeric.createNumericExpr(arg);
	}

	public SqlExpr createTimeExpr(ValueExpr arg)
		throws UnsupportedRdbmsOperatorException
	{
		return time.createTimeExpr(arg);
	}

	public SqlExpr createZonedExpr(ValueExpr arg)
		throws UnsupportedRdbmsOperatorException
	{
		return zoned.createZonedExpr(arg);
	}

	public SqlExpr createDatatypeExpr(ValueExpr arg)
		throws UnsupportedRdbmsOperatorException
	{
		return datatype.createDatatypeExpr(arg);
	}

	public SqlExpr createUriExpr(ValueExpr arg)
		throws UnsupportedRdbmsOperatorException
	{
		return uri.createUriExpr(arg);
	}

	public SqlExpr createHashExpr(ValueExpr arg)
		throws UnsupportedRdbmsOperatorException
	{
		return hash.createHashExpr(arg);
	}
	
	public SqlExpr createPointExpr(ValueExpr arg)
	throws UnsupportedRdbmsOperatorException
	{
		return point.createPointExpr(arg);
	}	
}
