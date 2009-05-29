/*
 * Created on Apr 27, 2005
 */
package edu.mit.simile.fresnel;

import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

import edu.mit.simile.fresnel.configuration.Configuration;
import edu.mit.simile.fresnel.results.Selection;

import java.util.Iterator;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

/**
 * Example use of Fresnel from the command line.
 * 
 * @author ryanlee
 */
public class Fresnel {
	public static void main(String[] args) {
		if (args.length < 6) {
			Options.usage();
			System.exit(0);
		}
		
		/**
		 * Debug mode prints out text summaries of what Fresnel configuration has been read in
		 * in addition to any processing results.
		 */
		Options opts = null;
		try {
			opts = Options.parseOptions(args);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			Options.usage();
			System.exit(0);
		}
		
		Repository confRepo = null, ontoRepo = null, dataRepo = null;
		try {
			confRepo = new SailRepository(new MemoryStore());
			ontoRepo = new SailRepository(new MemoryStore());
			dataRepo = new SailRepository(new MemoryStore());
			confRepo.initialize();
			ontoRepo.initialize();
			dataRepo.initialize();
		} catch (Exception e) { ; }
		
		try {
			System.err.println("reading configuration...");					
			for(Iterator it = opts.getConfigurationFiles(); it.hasNext(); ) {
				FileOptions f = (FileOptions) it.next();
				FresnelUtilities.read(confRepo, f.getFile(), f.getFormat());
			}

			System.err.println("reading ontologies...");
			for(Iterator it = opts.getOntologyFiles(); it.hasNext(); ) {
				FileOptions f = (FileOptions) it.next();
				FresnelUtilities.read(ontoRepo, f.getFile(), f.getFormat());
			}

			System.err.println("reading data...");
			for(Iterator it = opts.getDataFiles(); it.hasNext(); ) {
				FileOptions f = (FileOptions) it.next();
				FresnelUtilities.read(dataRepo, f.getFile(), f.getFormat());
			}
		} catch (Exception e) { 
			System.err.println("Problems reading input data: " + e.toString());
			e.printStackTrace();
		}
		
		try {
			Configuration conf = new Configuration(confRepo, ontoRepo);
			if (conf.hasWarnings()) System.err.println(conf.getWarningsString());
			Selection selected = conf.select(dataRepo);
			selected = conf.format(dataRepo, selected);
			Document out = selected.render();
			if (opts.isDebug()) {
				System.out.println(conf);
			} else {
				DOMSource in = new DOMSource(out); 
				StreamResult res = new StreamResult(System.out);
				TransformerFactory tf = TransformerFactory.newInstance();
				Transformer serial = tf.newTransformer();
				serial.setOutputProperty(OutputKeys.INDENT, "yes");
				serial.transform(in, res);
				System.err.println("done");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
