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
 * A {@link Function} that tries to cast its argument to an <tt>xsd:double</tt>.
 * 
 * @author Arjohn Kampman
 */
public class DoubleCast implements Function {

	public String getURI() {
		return XMLSchema.DOUBLE.toString();
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 1) {
			throw new ValueExprEvaluationException("xsd:double cast requires exactly 1 argument, got "
					+ args.length);
		}

		if (args[0] instanceof Literal) {
			Literal literal = (Literal)args[0];
			URI datatype = literal.getDatatype();

			if (QueryEvaluationUtil.isStringLiteral(literal)) {
				String doubleValue = XMLDatatypeUtil.collapseWhiteSpace(literal.getLabel());
				if (XMLDatatypeUtil.isValidDouble(doubleValue)) {
					return valueFactory.createLiteral(doubleValue, XMLSchema.DOUBLE);
				}
			}
			else if (datatype != null) {
				if (datatype.equals(XMLSchema.DOUBLE)) {
					return literal;
				}
				else if (XMLDatatypeUtil.isNumericDatatype(datatype)) {
					// FIXME: doubles must be processed separately, see
					// http://www.w3.org/TR/xpath-functions/#casting-from-primitive-to-primitive
					try {
						double doubleValue = literal.doubleValue();
						return valueFactory.createLiteral(doubleValue);
					}
					catch (NumberFormatException e) {
						throw new ValueExprEvaluationException(e.getMessage(), e);
					}
				}
				else if (datatype.equals(XMLSchema.BOOLEAN)) {
					try {
						return valueFactory.createLiteral(literal.booleanValue() ? 1.0 : 0.0);
					}
					catch (IllegalArgumentException e) {
						throw new ValueExprEvaluationException(e.getMessage(), e);
					}
				}
			}
		}

		throw new ValueExprEvaluationException("Invalid argument for xsd:double cast: " + args[0]);
	}
}
