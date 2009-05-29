/*   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005-2007.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: FSLSesameHierarchyStore.java 59 2007-11-06 16:41:17Z epietrig $
 */ 

package fr.inria.jfresnel.fsl.sesame;

import fr.inria.jfresnel.fsl.*;

import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Iterator;
import java.util.List;
import java.io.File;
import java.net.URL;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Value;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;

/**Contains all class and property hierarchies (retrieved from available RDFS/OWL document)*/

public class FSLSesameHierarchyStore extends FSLHierarchyStore {

    static final String X_var = "x";
    static final String Y_var = "y";

    Hashtable tmpHierarchy;

    public FSLSesameHierarchyStore(){
	super();
	tmpHierarchy = new Hashtable();
    }

    /** add the classes and properties of an ontology into the store
     *@param docURI the ontology's URI
     *@param locationPath an alternative local path where the ontology can be found (null if none); cam be given as a relative path
     */
    public void addOntology(String docURI, String locationPath){
	Repository r = new SailRepository(new MemoryStore());
	try {
	    r.initialize();
	    RepositoryConnection c = r.getConnection();
	    if (DEBUG){
		System.out.println("Retrieving ontology "+docURI);
	    }
	    try {
 		c.add(new URL(docURI), docURI, RDFFormat.RDFXML);
	    }
	    catch (Exception ex1){
		System.err.println("FSLSesameEvaluator: Warning: Failed to load RDF data from " + docURI);
		if (locationPath != null){
		    try {
 			c.add(new File(locationPath), locationPath, RDFFormat.RDFXML);
		    }
		    catch (Exception ex2){
			System.err.println("FSLSesameEvaluator: Error: Failed to load RDF data from " + locationPath);
			if (DEBUG){ex2.printStackTrace();}
		    }
		}
	    }
	    processOntology(c);
	}
	catch (OpenRDFException ex0){
	    System.err.println("Error processing ontology: "+docURI);
	    if (DEBUG){ex0.printStackTrace();}
	}
    }
    
    void processOntology(RepositoryConnection c) throws RepositoryException, QueryEvaluationException {
	String queryString = "SELECT * FROM {" + X_var + "} <http://www.w3.org/2000/01/rdf-schema#subClassOf> {" + Y_var + "}";
	try {
	    TupleQueryResult sss = c.prepareTupleQuery(QueryLanguage.SERQL, queryString).evaluate();
	    processSubsumptionStatements(sss);
	    sss.close();
	    processHierarchy(classHierarchy);
	}
	catch (MalformedQueryException ex){
	    System.err.println("Error: malformed SeRQL query \n" + queryString);
	    if (DEBUG){ex.printStackTrace();}
	}
	queryString = "SELECT * FROM {" + X_var + "} <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> {" + Y_var + "}";
	try {
	    TupleQueryResult sss = c.prepareTupleQuery(QueryLanguage.SERQL, queryString).evaluate();
	    processSubsumptionStatements(sss);
	    sss.close();
	    processHierarchy(propertyHierarchy);
	}
	catch (MalformedQueryException ex){
	    System.err.println("Error: malformed SeRQL query \n" + queryString);
	    if (DEBUG){ex.printStackTrace();}
	}
	c.close();
    }

    void processHierarchy(Hashtable hierarchy){
	Object uri;
	Vector ancestors;
	for (Enumeration e=tmpHierarchy.keys();e.hasMoreElements();){
	    uri = e.nextElement();
	    ancestors = new Vector();
	    getAncestors(uri, ancestors);
	    if (ancestors.size() > 0){
		String[] ancestorList = new String[ancestors.size()];
		for (int i=0;i<ancestorList.length;i++){
		    ancestorList[i] = (String)ancestors.elementAt(i);
		}
		hierarchy.put(uri, ancestorList);
	    }
	}
	tmpHierarchy.clear();
    }

    void getAncestors(Object uri, Vector ancestors){
	Vector ancestorsToBeProcessed = (Vector)tmpHierarchy.get(uri);
	if (ancestorsToBeProcessed != null){
	    Object ancestorURI;
	    for (int i=0;i<ancestorsToBeProcessed.size();i++){
		ancestorURI = ancestorsToBeProcessed.elementAt(i);
		if (!ancestors.contains(ancestorURI)){
		    ancestors.add(ancestorURI);
		    getAncestors(ancestorURI, ancestors);
		}
	    }
	}
    }
    
    void processSubsumptionStatements(TupleQueryResult sss) throws QueryEvaluationException {
	BindingSet bs;
	while (sss.hasNext()){
	    bs = sss.next();
	    Value aSubType = bs.getValue(X_var);
	    Value aType = bs.getValue(Y_var);
	    if (aSubType instanceof URI && aType instanceof URI){
		String classURI = aType.toString();
		String subTypeURI = aSubType.toString();
		Vector v;
		if (tmpHierarchy.containsKey(subTypeURI)){
		    v = (Vector)tmpHierarchy.get(subTypeURI);
		    if (!v.contains(classURI)){v.add(classURI);}
		}
		else {
		    v = new Vector();
		    v.add(classURI);
		    tmpHierarchy.put(subTypeURI, v);
		}
	    }
	}
    }

    /* main */

    public static void main(String[] args){
	FSLSesameHierarchyStore fhs = new FSLSesameHierarchyStore();
	fhs.addOntology("http://www.lri.fr/~pietriga/IsaViz/test/fsl-hierarchy-test-model.rdfs",
			"tests/fsl-hierarchy-test-model.rdfs");
// 	fhs.addOntology("http://www.lri.fr/~pietriga/IsaViz/test/fsl_hie_p_test.rdfs",
// 			"file:///Users/epietrig/projects/WWW/2001/10/IsaViz/test/fsl_hie_p_test.rdfs");
	fhs.printClassHierarchy();
	fhs.printPropertyHierarchy();
    }

}
