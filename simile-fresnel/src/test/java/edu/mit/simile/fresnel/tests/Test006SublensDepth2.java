package edu.mit.simile.fresnel.tests;

import edu.mit.simile.fresnel.results.Selection;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

public class Test006SublensDepth2 extends BaseTest {
	final static protected Logger logger = Logger.getLogger(Test006SublensDepth2.class);
	
	private final static File _onto = new File("data/ontologies/foaf.rdf");
	private final static File _data = new File("data/test6/in.rdf");
	private final static File _config = new File("data/test6/config.n3");
	private final static File _out = new File("data/test6/out.xml");
		
	public Test006SublensDepth2(String name) {
		super(name);
	}
	
	protected void setInputFiles() {
		data = _data;
		onto = _onto;
		config = _config;
		out = _out;
	}
	
	public void testParsing() throws Exception {
		assertFalse(conf.hasWarnings());
		if (conf.hasWarnings())
			logger.error(conf.getWarningsString());
	}
	
	public void testSelection() throws Exception {
		Selection selected = conf.select(dataRepo);
		selected = conf.format(dataRepo, selected);
		Document o = selected.render();
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document expected = db.parse(out);
		
		assertXMLEqual(expected, o);
		
		confRepo.shutDown();
		ontoRepo.shutDown();
		dataRepo.shutDown();
	}
}
