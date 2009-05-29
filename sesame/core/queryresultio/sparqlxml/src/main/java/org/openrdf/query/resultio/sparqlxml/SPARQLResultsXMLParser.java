/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio.sparqlxml;

import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.BINDING_NAME_ATT;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.BINDING_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.BNODE_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.LITERAL_DATATYPE_ATT;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.LITERAL_LANG_ATT;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.LITERAL_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.RESULT_SET_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.RESULT_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.URI_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.VAR_NAME_ATT;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.VAR_TAG;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xml.sax.SAXException;

import info.aduna.xml.SimpleSAXAdapter;
import info.aduna.xml.SimpleSAXParser;
import info.aduna.xml.XMLReaderFactory;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultParserBase;

/**
 * Parser for reading tuple query results formatted as SPARQL Results Documents.
 * See <a href="http://www.w3.org/TR/rdf-sparql-XMLres/">SPARQL Query Results
 * XML Format</a> for the definition of this format. The parser assumes that
 * the XML is wellformed.
 */
public class SPARQLResultsXMLParser extends TupleQueryResultParserBase {

	/*-------------*
	 * Construtors *
	 *-------------*/

	/**
	 * Creates a new parser for the SPARQL Query Results XML Format that will use
	 * an instance of {@link ValueFactoryImpl} to create Value objects.
	 */
	public SPARQLResultsXMLParser() {
		super();
	}

	/**
	 * Creates a new parser for the SPARQL Query Results XML Format that will use
	 * the supplied ValueFactory to create Value objects.
	 */
	public SPARQLResultsXMLParser(ValueFactory valueFactory) {
		super(valueFactory);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public TupleQueryResultFormat getTupleQueryResultFormat() {
		return TupleQueryResultFormat.SPARQL;
	}

	public void parse(InputStream in)
		throws IOException, QueryResultParseException, TupleQueryResultHandlerException
	{
		try {
			SimpleSAXParser simpleSAXParser = new SimpleSAXParser(XMLReaderFactory.createXMLReader());

			simpleSAXParser.setListener(new SPARQLResultsParser());

			simpleSAXParser.parse(in);
		}
		catch (SAXException e) {
			Exception wrappedExc = e.getException();

			if (wrappedExc instanceof QueryResultParseException) {
				throw (QueryResultParseException)wrappedExc;
			}
			else if (wrappedExc instanceof TupleQueryResultHandlerException) {
				throw (TupleQueryResultHandlerException)wrappedExc;
			}
			else {
				throw new QueryResultParseException(wrappedExc);
			}
		}
	}

	/*---------------------------------*
	 * Inner class SPARQLResultsParser *
	 *---------------------------------*/

	private class SPARQLResultsParser extends SimpleSAXAdapter {

		/*-----------*
		 * Variables *
		 *-----------*/

		/**
		 * The variable names that are specified in the header.
		 */
		private List<String> bindingNames;

		/**
		 * The most recently parsed binding name.
		 */
		private String currentBindingName;

		/**
		 * The most recently parsed value.
		 */
		private Value currentValue;

		/**
		 * The bound variables for the current result.
		 */
		private MapBindingSet currentSolution;

		/*---------*
		 * Methods *
		 *---------*/

		@Override
		public void startDocument()
			throws SAXException
		{
			bindingNames = new ArrayList<String>();
			currentValue = null;
		}

		@Override
		public void endDocument()
			throws SAXException
		{
			try {
				handler.endQueryResult();
			}
			catch (TupleQueryResultHandlerException e) {
				throw new SAXException(e);
			}
		}

		@Override
		public void startTag(String tagName, Map<String, String> atts, String text)
			throws SAXException
		{
			if (BINDING_TAG.equals(tagName)) {
				currentBindingName = atts.get(BINDING_NAME_ATT);

				if (currentBindingName == null) {
					throw new SAXException(BINDING_NAME_ATT + " attribute missing for " + BINDING_TAG + " element");
				}
			}
			else if (URI_TAG.equals(tagName)) {
				try {
					currentValue = valueFactory.createURI(text);
				}
				catch (IllegalArgumentException e) {
					// Malformed URI
					throw new SAXException(e.getMessage());
				}
			}
			else if (BNODE_TAG.equals(tagName)) {
				currentValue = valueFactory.createBNode(text);
			}
			else if (LITERAL_TAG.equals(tagName)) {
				String xmlLang = atts.get(LITERAL_LANG_ATT);
				String datatype = atts.get(LITERAL_DATATYPE_ATT);

				if (datatype != null) {
					try {
						currentValue = valueFactory.createLiteral(text, valueFactory.createURI(datatype));
					}
					catch (IllegalArgumentException e) {
						// Illegal datatype URI
						throw new SAXException(e.getMessage());
					}
				}
				else if (xmlLang != null) {
					currentValue = valueFactory.createLiteral(text, xmlLang);
				}
				else {
					currentValue = valueFactory.createLiteral(text);
				}
			}
			else if (RESULT_TAG.equals(tagName)) {
				currentSolution = new MapBindingSet(bindingNames.size());
			}
			else if (VAR_TAG.equals(tagName)) {
				String varName = atts.get(VAR_NAME_ATT);

				if (varName == null) {
					throw new SAXException(VAR_NAME_ATT + " missing for " + VAR_TAG + " element");
				}

				bindingNames.add(varName);
			}
			else if (RESULT_SET_TAG.equals(tagName)) {
				try {
					handler.startQueryResult(bindingNames);
				}
				catch (TupleQueryResultHandlerException e) {
					throw new SAXException(e);
				}
			}
		}

		@Override
		public void endTag(String tagName)
			throws SAXException
		{
			if (BINDING_TAG.equals(tagName)) {
				if (currentValue == null) {
					throw new SAXException("Value missing for " + BINDING_TAG + " element");
				}

				currentSolution.addBinding(currentBindingName, currentValue);

				currentBindingName = null;
				currentValue = null;
			}
			else if (RESULT_TAG.equals(tagName)) {
				try {
					handler.handleSolution(currentSolution);
					currentSolution = null;
				}
				catch (TupleQueryResultHandlerException e) {
					throw new SAXException(e);
				}
			}
		}
	}
}
