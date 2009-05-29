/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.trix;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xml.sax.SAXException;

import info.aduna.xml.SimpleSAXAdapter;
import info.aduna.xml.SimpleSAXParser;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFParserBase;

import static org.openrdf.rio.trix.TriXConstants.*;

/**
 * A parser that can parse RDF files that are in the <a
 * href="http://www.w3.org/2004/03/trix/">TriX format</a>.
 * 
 * @author Arjohn Kampman
 */
public class TriXParser extends RDFParserBase {

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new TriXParser that will use a {@link ValueFactoryImpl} to
	 * create objects for resources, bNodes, literals and statements.
	 */
	public TriXParser() {
		super();
	}

	/**
	 * Creates a new TriXParser that will use the supplied ValueFactory to create
	 * objects for resources, bNodes, literals and statements.
	 * 
	 * @param valueFactory
	 *        A ValueFactory.
	 */
	public TriXParser(ValueFactory valueFactory) {
		super(valueFactory);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public final RDFFormat getRDFFormat() {
		return RDFFormat.TRIX;
	}

	public void parse(InputStream in, String baseURI)
		throws IOException, RDFParseException, RDFHandlerException
	{
		parse(in);
	}

	public void parse(Reader reader, String baseURI)
		throws IOException, RDFParseException, RDFHandlerException
	{
		parse(reader);
	}

	private void parse(Object inputStreamOrReader)
		throws IOException, RDFParseException, RDFHandlerException
	{
		try {
			rdfHandler.startRDF();

			SimpleSAXParser saxParser = new SimpleSAXParser();
			saxParser.setPreserveWhitespace(true);
			saxParser.setListener(new TriXSAXHandler());

			if (inputStreamOrReader instanceof InputStream) {
				saxParser.parse((InputStream)inputStreamOrReader);
			}
			else {
				saxParser.parse((Reader)inputStreamOrReader);
			}

			rdfHandler.endRDF();
		}
		catch (SAXException e) {
			Exception wrappedExc = e.getException();

			if (wrappedExc instanceof RDFParseException) {
				throw (RDFParseException)wrappedExc;
			}
			else if (wrappedExc instanceof RDFHandlerException) {
				throw (RDFHandlerException)wrappedExc;
			}
			else {
				reportFatalError(wrappedExc);
			}
		}
	}

	/*----------------------------*
	 * Inner class TriXSAXHandler *
	 *----------------------------*/

	private class TriXSAXHandler extends SimpleSAXAdapter {

		private Resource currentContext;

		private boolean parsingContext;

		private List<Value> valueList;

		public TriXSAXHandler() {
			currentContext = null;
			valueList = new ArrayList<Value>(3);
		}

		@Override
		public void startTag(String tagName, Map<String, String> atts, String text)
			throws SAXException
		{
			try {
				if (tagName.equals(URI_TAG)) {
					valueList.add(createURI(text));
				}
				else if (tagName.equals(BNODE_TAG)) {
					valueList.add(createBNode(text));
				}
				else if (tagName.equals(PLAIN_LITERAL_TAG)) {
					String lang = atts.get(LANGUAGE_ATT);
					valueList.add(createLiteral(text, lang, null));
				}
				else if (tagName.equals(TYPED_LITERAL_TAG)) {
					String datatype = atts.get(DATATYPE_ATT);

					if (datatype == null) {
						reportError(DATATYPE_ATT + " attribute missing for typed literal");
						valueList.add(createLiteral(text, null, null));
					}
					else {
						URI dtURI = createURI(datatype);
						valueList.add(createLiteral(text, null, dtURI));
					}
				}
				else if (tagName.equals(TRIPLE_TAG)) {
					if (parsingContext) {
						try {
							// First triple in a context, valueList can contain
							// context information
							if (valueList.size() > 1) {
								reportError("At most 1 resource can be specified for the context");
							}
							else if (valueList.size() == 1) {
								try {
									currentContext = (Resource)valueList.get(0);
								}
								catch (ClassCastException e) {
									reportError("Context identifier should be a URI or blank node");
								}
							}
						}
						finally {
							parsingContext = false;
							valueList.clear();
						}
					}
				}
				else if (tagName.equals(CONTEXT_TAG)) {
					parsingContext = true;
				}
			}
			catch (RDFParseException e) {
				throw new SAXException(e);
			}
		}

		@Override
		public void endTag(String tagName)
			throws SAXException
		{
			try {
				if (tagName.equals(TRIPLE_TAG)) {
					reportStatement();
				}
				else if (tagName.equals(CONTEXT_TAG)) {
					currentContext = null;
				}
			}
			catch (RDFParseException e) {
				throw new SAXException(e);
			}
			catch (RDFHandlerException e) {
				throw new SAXException(e);
			}
		}

		private void reportStatement()
			throws RDFParseException, RDFHandlerException
		{
			try {
				if (valueList.size() != 3) {
					reportError("exactly 3 values are required for a triple");
					return;
				}

				Resource subj;
				URI pred;
				Value obj;

				try {
					subj = (Resource)valueList.get(0);
				}
				catch (ClassCastException e) {
					reportError("First value for a triple should be a URI or blank node");
					return;
				}

				try {
					pred = (URI)valueList.get(1);
				}
				catch (ClassCastException e) {
					reportError("Second value for a triple should be a URI");
					return;
				}

				obj = valueList.get(2);

				Statement st = createStatement(subj, pred, obj, currentContext);
				rdfHandler.handleStatement(st);
			}
			finally {
				valueList.clear();
			}
		}
	} // end inner class TriXSAXHandler
}
