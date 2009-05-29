/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.util;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.Compare.CompareOp;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;

/**
 * @author Arjohn Kampman
 */
public class QueryEvaluationUtil {

	/**
	 * Determines the effective boolean value (EBV) of the supplied value as
	 * defined in the <a href="http://www.w3.org/TR/rdf-sparql-query/#ebv">SPARQL
	 * specification</a>:
	 * <ul>
	 * <li>The EBV of any literal whose type is xsd:boolean or numeric is false
	 * if the lexical form is not valid for that datatype (e.g.
	 * "abc"^^xsd:integer).
	 * <li>If the argument is a typed literal with a datatype of xsd:boolean,
	 * the EBV is the value of that argument.
	 * <li>If the argument is a plain literal or a typed literal with a datatype
	 * of xsd:string, the EBV is false if the operand value has zero length;
	 * otherwise the EBV is true.
	 * <li>If the argument is a numeric type or a typed literal with a datatype
	 * derived from a numeric type, the EBV is false if the operand value is NaN
	 * or is numerically equal to zero; otherwise the EBV is true.
	 * <li> All other arguments, including unbound arguments, produce a type
	 * error.
	 * </ul>
	 * 
	 * @param value
	 *        Some value.
	 * @return The EBV of <tt>value</tt>.
	 * @throws ValueExprEvaluationException
	 *         In case the application of the EBV algorithm results in a type
	 *         error.
	 */
	public static boolean getEffectiveBooleanValue(Value value)
		throws ValueExprEvaluationException
	{
		if (value instanceof Literal) {
			Literal literal = (Literal)value;
			String label = literal.getLabel();
			URI datatype = literal.getDatatype();

			if (datatype == null || datatype.equals(XMLSchema.STRING)) {
				return label.length() > 0;
			}
			else if (datatype.equals(XMLSchema.BOOLEAN)) {
				if ("true".equals(label) || "1".equals(label)) {
					return true;
				}
				else {
					// also false for illegal values
					return false;
				}
			}
			else if (datatype.equals(XMLSchema.DECIMAL)) {
				try {
					String normDec = XMLDatatypeUtil.normalizeDecimal(label);
					return !normDec.equals("0.0");
				}
				catch (IllegalArgumentException e) {
					return false;
				}
			}
			else if (XMLDatatypeUtil.isIntegerDatatype(datatype)) {
				try {
					String normInt = XMLDatatypeUtil.normalize(label, datatype);
					return !normInt.equals("0");
				}
				catch (IllegalArgumentException e) {
					return false;
				}
			}
			else if (XMLDatatypeUtil.isFloatingPointDatatype(datatype)) {
				try {
					String normFP = XMLDatatypeUtil.normalize(label, datatype);
					return !normFP.equals("0.0E0") && !normFP.equals("NaN");
				}
				catch (IllegalArgumentException e) {
					return false;
				}
			}
		}

		throw new ValueExprEvaluationException();
	}

	public static boolean compare(Value leftVal, Value rightVal, CompareOp operator)
		throws ValueExprEvaluationException
	{
		if (leftVal instanceof Literal && rightVal instanceof Literal) {
			// Both left and right argument is a Literal
			return compareLiterals((Literal)leftVal, (Literal)rightVal, operator);
		}
		else {
			// All other value combinations
			switch (operator) {
				case EQ:
					return valuesEqual(leftVal, rightVal);
				case NE:
					return !valuesEqual(leftVal, rightVal);
				default:
					throw new ValueExprEvaluationException(
							"Only literals with compatible, ordered datatypes can be compared using <, <=, > and >= operators");
			}
		}
	}

	private static boolean valuesEqual(Value leftVal, Value rightVal) {
		return leftVal != null && rightVal != null && leftVal.equals(rightVal);
	}

	public static boolean compareLiterals(Literal leftLit, Literal rightLit, CompareOp operator)
		throws ValueExprEvaluationException
	{
		// type precendence:
		// - simple literal
		// - numeric
		// - xsd:boolean
		// - xsd:dateTime
		// - xsd:string
		// - RDF term (equal and unequal only)

		URI leftDatatype = leftLit.getDatatype();
		URI rightDatatype = rightLit.getDatatype();

		Integer compareResult = null;

		if (QueryEvaluationUtil.isStringLiteral(leftLit) && QueryEvaluationUtil.isStringLiteral(rightLit)) {
			compareResult = leftLit.getLabel().compareTo(rightLit.getLabel());
		}
		else if (leftDatatype != null && rightDatatype != null) {
			URI commonDatatype = null;

			if (leftDatatype.equals(rightDatatype)) {
				commonDatatype = leftDatatype;
			}
			else if (XMLDatatypeUtil.isNumericDatatype(leftDatatype)
					&& XMLDatatypeUtil.isNumericDatatype(rightDatatype))
			{
				// left and right arguments have different datatypes, try to find a
				// more general, shared datatype
					if (leftDatatype.equals(XMLSchema.DOUBLE) || rightDatatype.equals(XMLSchema.DOUBLE)) {
						commonDatatype = XMLSchema.DOUBLE;
					}
					else if (leftDatatype.equals(XMLSchema.FLOAT) || rightDatatype.equals(XMLSchema.FLOAT)) {
						commonDatatype = XMLSchema.FLOAT;
					}
					else if (leftDatatype.equals(XMLSchema.DECIMAL) || rightDatatype.equals(XMLSchema.DECIMAL)) {
						commonDatatype = XMLSchema.DECIMAL;
					}
					else {
						commonDatatype = XMLSchema.INTEGER;
					}
				}

			if (commonDatatype != null) {
				try {
					if (commonDatatype.equals(XMLSchema.DOUBLE)) {
						compareResult = Double.compare(leftLit.doubleValue(), rightLit.doubleValue());
					}
					else if (commonDatatype.equals(XMLSchema.FLOAT)) {
						compareResult = Float.compare(leftLit.floatValue(), rightLit.floatValue());
					}
					else if (commonDatatype.equals(XMLSchema.DECIMAL)) {
						compareResult = leftLit.decimalValue().compareTo(rightLit.decimalValue());
					}
					else if (XMLDatatypeUtil.isIntegerDatatype(commonDatatype)) {
						compareResult = leftLit.integerValue().compareTo(rightLit.integerValue());
					}
					else if (commonDatatype.equals(XMLSchema.BOOLEAN)) {
						Boolean leftBool = Boolean.valueOf(leftLit.booleanValue());
						Boolean rightBool = Boolean.valueOf(rightLit.booleanValue());
						compareResult = leftBool.compareTo(rightBool);
					}
					else if (XMLDatatypeUtil.isCalendarDatatype(commonDatatype)) {
						XMLGregorianCalendar left = leftLit.calendarValue();
						XMLGregorianCalendar right = rightLit.calendarValue();

						compareResult = left.compare(right);

						// Note: XMLGregorianCalendar.compare() returns compatible
						// values
						// (-1, 0, 1) but INDETERMINATE needs special treatment
						if (compareResult == DatatypeConstants.INDETERMINATE) {
							throw new ValueExprEvaluationException("Indeterminate result for date/time comparison");
						}
					}
					else if (commonDatatype.equals(XMLSchema.STRING)) {
						compareResult = leftLit.getLabel().compareTo(rightLit.getLabel());
					}
				}
				catch (IllegalArgumentException e) {
					// One of the basic-type method calls failed, try syntactic match
					// before throwing an error
					if (leftLit.equals(rightLit)) {
						switch (operator) {
							case EQ:
								return true;
							case NE:
								return false;
						}
					}

					throw new ValueExprEvaluationException(e);
				}
			}
		}

		if (compareResult != null) {
			// Literals have compatible ordered datatypes
			switch (operator) {
				case LT:
					return compareResult.intValue() < 0;
				case LE:
					return compareResult.intValue() <= 0;
				case EQ:
					return compareResult.intValue() == 0;
				case NE:
					return compareResult.intValue() != 0;
				case GE:
					return compareResult.intValue() >= 0;
				case GT:
					return compareResult.intValue() > 0;
				default:
					throw new IllegalArgumentException("Unknown operator: " + operator);
			}
		}
		else {
			// All other cases, e.g. literals with languages, unequal or
			// unordered datatypes, etc. These arguments can only be compared
			// using the operators 'EQ' and 'NE'. See SPARQL's RDFterm-equal
			// operator

			boolean literalsEqual = leftLit.equals(rightLit);

			if (!literalsEqual) {
				if (leftDatatype != null && rightDatatype != null
						&& XMLDatatypeUtil.isCalendarDatatype(leftDatatype)
						&& XMLDatatypeUtil.isCalendarDatatype(rightDatatype))
				{
					// left and right arguments have different date/time datatypes,
					// these are always unequal
				}
				else if (leftDatatype != null && rightLit.getLanguage() == null || rightDatatype != null
						&& leftLit.getLanguage() == null)
				{
				// For literals with unsupported datatypes we don't know if their
				// values are equal
					throw new ValueExprEvaluationException("Unable to compare literals with unsupported types");
				}
			}

			switch (operator) {
				case EQ:
					return literalsEqual;
				case NE:
					return !literalsEqual;
				case LT:
				case LE:
				case GE:
				case GT:
					throw new ValueExprEvaluationException(
							"Only literals with compatible, ordered datatypes can be compared using <, <=, > and >= operators");
				default:
					throw new IllegalArgumentException("Unknown operator: " + operator);
			}
		}
	}

	/**
	 * Checks whether the supplied value is a "simple literal" as defined in the
	 * SPARQL spec. A "simple literal" is a literal without a language tag or a
	 * datatype.
	 */
	public static boolean isSimpleLiteral(Value v) {
		if (v instanceof Literal) {
			return isSimpleLiteral((Literal)v);
		}

		return false;
	}

	/**
	 * Checks whether the supplied literal is a "simple literal" as defined in
	 * the SPARQL spec. A "simple literal" is a literal without a language tag or
	 * a datatype.
	 */
	public static boolean isSimpleLiteral(Literal l) {
		return l.getLanguage() == null && l.getDatatype() == null;
	}

	/**
	 * Checks whether the supplied literal is a "string literal". A "string
	 * literal" is either a {@link #isSimpleLiteral(Literal) simple literal} or a
	 * literal with datatype {@link XMLSchema#STRING xsd:string}.
	 */
	public static boolean isStringLiteral(Literal l) {
		URI datatype = l.getDatatype();

		if (datatype == null) {
			return l.getLanguage() == null;
		}
		else {
			return datatype.equals(XMLSchema.STRING);
		}
	}
}
