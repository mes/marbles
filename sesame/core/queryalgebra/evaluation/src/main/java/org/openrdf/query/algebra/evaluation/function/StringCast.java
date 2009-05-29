/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.util.QueryEvaluationUtil;

/**
 * A {@link Function} that tries to cast its argument to an <tt>xsd:string</tt>.
 * 
 * @author Arjohn Kampman
 */
public class StringCast implements Function {

	public String getURI() {
		return XMLSchema.STRING.toString();
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 1) {
			throw new ValueExprEvaluationException("xsd:string cast requires exactly 1 argument, got " + args.length);
		}

		Value value = args[0];
		if (value instanceof URI) {
			return valueFactory.createLiteral(value.toString(), XMLSchema.STRING);
		}
		else if (value instanceof Literal) {
			Literal literal = (Literal)value;
			URI datatype = literal.getDatatype();

			if (QueryEvaluationUtil.isSimpleLiteral(literal)) {
				return valueFactory.createLiteral(literal.getLabel(), XMLSchema.STRING);
			}
			else if (datatype != null) {
				if (datatype.equals(XMLSchema.STRING)) {
					return literal;
				}
				else if (XMLDatatypeUtil.isNumericDatatype(datatype) || datatype.equals(XMLSchema.BOOLEAN)
						|| datatype.equals(XMLSchema.DATETIME))
				{
					// FIXME: conversion to xsd:string is much more complex than
					// this, see
					// http://www.w3.org/TR/xpath-functions/#casting-from-primitive-to-primitive
					return valueFactory.createLiteral(literal.getLabel(), XMLSchema.STRING);
				}
			}
		}

		throw new ValueExprEvaluationException("Invalid argument for xsd:string cast: " + value);
	}
}
