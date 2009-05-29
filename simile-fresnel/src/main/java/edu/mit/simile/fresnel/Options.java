package edu.mit.simile.fresnel;

import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Options {
	private final Pattern _switchesPattern = Pattern.compile("^-([odc])=?(n3|rdf)?$");
	private boolean _debug;
	private Vector<FileOptions> _confFiles;
	private Vector<FileOptions> _ontFiles;
	private Vector<FileOptions> _dataFiles;
	
	public static final int CONFIGURATION_TYPE = 0;
	public static final int ONTOLOGY_TYPE = 1;
	public static final int DATA_TYPE = 2;
		
	public Options() {
		this._debug = false;
		this._confFiles = new Vector<FileOptions>();
		this._ontFiles = new Vector<FileOptions>();
		this._dataFiles = new Vector<FileOptions>();
	}
	
	public boolean isDebug() {
		return this._debug;
	}
	
	public Iterator getConfigurationFiles() {
		return this._confFiles.iterator();
	}
	
	public Iterator getOntologyFiles() {
		return this._ontFiles.iterator();
	}

	public Iterator getDataFiles() {
		return this._dataFiles.iterator();
	}

	public boolean validateSwitch(String option) {
        Matcher m = this._switchesPattern.matcher(option);
        return m.matches();
	}
	
	public void addArgument(String option, String file) throws Exception {
		Matcher m = this._switchesPattern.matcher(option);
		m.matches();
		String format = formatArg("");
		if (null != m.group(2))
			format = formatArg(m.group(2));			
		int type = typeArg(m.group(1));
		FileOptions f = new FileOptions(format, file);
		switch(type) {
		case CONFIGURATION_TYPE:
			this._confFiles.add(f);
			break;
		case ONTOLOGY_TYPE:
			this._ontFiles.add(f);
			break;
		case DATA_TYPE:
			this._dataFiles.add(f);
			break;
		}
	}
	
	public void setDebug(boolean debug) {
		this._debug = debug;
	}
	
	public static void usage() {
		System.err.println("usage: fresnel [--debug] <-c|-o|-d>=<format> <file> ...");
		System.err.println(" <format> is one of: [rdf|n3]");
		System.err.println(" You MUST use AT LEAST one of -c, -o, and -d");
		System.err.println(" -c indicates a configuration file");
		System.err.println(" -o indicates an ontology file");
		System.err.println(" -d indicates a data file");
		System.err.println(" Example: fresnel -c=n3 file:///home/you/fresnel.n3 \\");
		System.err.println("                  -o=rdf http://example.org/ontology \\");
		System.err.println("                  -o=rdf http://ont.example.com/another \\");
		System.err.println("                  -d=rdf http://you.example.org/data.rdf");
	}
	
	protected static int typeArg(String option) throws Exception {
		if (option.equals("c")) {
			return CONFIGURATION_TYPE;
		} else if (option.equals("o")) {
			return ONTOLOGY_TYPE;
		} else if (option.equals("d")) {
			return DATA_TYPE;
		} else {
			throw new Exception("Unrecognized option type: " + option);
		}
	}
	
	protected static String formatArg(String flag) throws Exception {
		if (flag.equals("rdf")) {
			return "RDFXML";
		} else if (flag.equals("n3")) {
			return "TURTLE";
		} else if (flag.equals("")) {
			// by default, assume RDF/XML is the transport syntax
			return "RDFXML";
		} else {
			throw new Exception("Unrecognized format flag: " + flag);
		}
	}
	
	protected static Options parseOptions(String[] args) throws Exception {
		Options opts = new Options();
		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("-")) {
				if (args[i].equals("--debug")) {
					opts.setDebug(true);
				} else if (opts.validateSwitch(args[i])) {
					opts.addArgument(args[i], args[++i]);
				} else {
					throw new Exception("Unrecognized switch: " + args[i]);
				}
			}
		}
		if (!opts.getConfigurationFiles().hasNext()) {
			throw new Exception("No configuration files given, cannot process");
		}
		if (!opts.getOntologyFiles().hasNext()) {
			throw new Exception("No ontology files given, cannot process");			
		}
		if (!opts.getDataFiles().hasNext()) {
			throw new Exception("No data files given, cannot process");			
		}		
		return opts;
	}
}
