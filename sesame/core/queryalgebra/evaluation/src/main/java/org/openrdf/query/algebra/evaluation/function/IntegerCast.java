/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function;

import java.math.BigInteger;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.util.QueryEvaluationUtil;

/**
 * A {@link Function} that tries to cast its argument to an <tt>xsd:integer</tt>.
 * 
 * @author Arjohn Kampman
 */
public class IntegerCast implements Function {

	public String getURI() {
		return XMLSchema.INTEGER.toString();
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 1) {
			throw new ValueExprEvaluationException("xsd:integer cast requires exactly 1 argument, got "
					+ args.length);
		}

		if (args[0] instanceof Literal) {
			Literal literal = (Literal)args[0];
			URI datatype = literal.getDatatype();

			if (QueryEvaluationUtil.isStringLiteral(literal)) {
				String integerValue = XMLDatatypeUtil.collapseWhiteSpace(literal.getLabel());
				if (XMLDatatypeUtil.isValidInteger(integerValue)) {
					return valueFactory.createLiteral(integerValue, XMLSchema.INTEGER);
				}
			}
			else if (datatype != null) {
				if (datatype.equals(XMLSchema.INTEGER)) {
					return literal;
				}
				else if (XMLDatatypeUtil.isNumericDatatype(datatype)) {
					// FIXME: decimals, floats and doubles must be processed
					// separately, see
					// http://www.w3.org/TR/xpath-functions/#casting-from-primitive-to-primitive
					try {
						BigInteger integerValue = literal.integerValue();
						return valueFactory.createLiteral(integerValue.toString(), XMLSchema.INTEGER);
					}
					catch (NumberFormatException e) {
						throw new ValueExprEvaluationException(e.getMessage(), e);
					}
				}
				else if (datatype.equals(XMLSchema.BOOLEAN)) {
					try {
						return valueFactory.createLiteral(literal.booleanValue() ? "1" : "0", XMLSchema.INTEGER);
					}
					catch (IllegalArgumentException e) {
						throw new ValueExprEvaluationException(e.getMessage(), e);
					}
				}
			}
		}

		throw new ValueExprEvaluationException("Invalid argument for xsd:integer cast: " + args[0]);
	}
}
