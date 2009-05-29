package edu.mit.simile.fresnel.tests;

import java.io.File;

import org.apache.log4j.Logger;
import org.custommonkey.xmlunit.XMLTestCase;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

import edu.mit.simile.fresnel.FresnelUtilities;
import edu.mit.simile.fresnel.configuration.Configuration;

public abstract class BaseTest extends XMLTestCase {
	protected final static Logger logger = Logger.getLogger(BaseTest.class);
	
	protected File data;
	protected File onto;
	protected File config;
	protected File out;
	
	protected Repository confRepo = null, ontoRepo = null, dataRepo = null;
	protected Configuration conf;
	
	protected abstract void setInputFiles();
	
	public BaseTest(String name) {
		super(name);
	}
	
	public void setUp() {
		setInputFiles();
		try {
			confRepo = new SailRepository(new MemoryStore());
			ontoRepo = new SailRepository(new MemoryStore());
			dataRepo = new SailRepository(new MemoryStore());
			confRepo.initialize();
			ontoRepo.initialize();
			dataRepo.initialize();
		} catch (Exception e) {
			assertTrue(false);
			logger.error(e.getMessage());
		}
		
		try {
			FresnelUtilities.read(confRepo, "file:///" + config.getAbsolutePath(), "TURTLE");
			FresnelUtilities.read(dataRepo, "file:///" + data.getAbsolutePath(), "RDFXML");
			FresnelUtilities.read(ontoRepo, "file:///" + onto.getAbsolutePath(), "RDFXML");
			conf = new Configuration(confRepo, ontoRepo);
		} catch (Exception e) {
			assertTrue(false);
			logger.error(e.getMessage());
		}
	}
}
