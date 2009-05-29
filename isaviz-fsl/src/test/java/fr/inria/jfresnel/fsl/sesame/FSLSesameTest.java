/*   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005-2007.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: FSLSesameTest.java 90 2007-11-07 15:25:55Z luong $
 */ 

package fr.inria.jfresnel.fsl.sesame;

import fr.inria.jfresnel.fsl.*;

import java.io.*;
import java.util.Vector;

import junit.framework.*;

import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.rio.RDFFormat;

public class FSLSesameTest extends TestCase {
    
    protected static String MODEL_FILE_PATH = "src/test/resources/fsl-test-model.rdf";

    protected FSLSesameEvaluator fse;
    protected Repository sesameRepository;
    protected RepositoryConnection connection;
    protected FSLNSResolver nsr;
    protected FSLHierarchyStore fhs;

    public FSLSesameTest(String name){
	super(name);
    }
    
    /* performed before each test */
    protected void setUp(){
	/*initializing the namespace prefix mappings used in the tests*/
	nsr = new FSLNSResolver();
	nsr.addPrefixBinding("dc", "http://purl.org/dc/elements/1.1/");
	nsr.addPrefixBinding("xsd", "http://www.w3.org/2001/XMLSchema#");
	nsr.addPrefixBinding("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
	nsr.addPrefixBinding("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
	nsr.addPrefixBinding("foaf", "http://xmlns.com/foaf/0.1/");
	nsr.addPrefixBinding("pim", "http://www.w3.org/2000/10/swap/pim/contact#");
	nsr.addPrefixBinding("air", "http://www.daml.org/2001/10/html/airport-ont#");
	/*initializing the RDFS hierarchy*/
	fhs = new FSLSesameHierarchyStore();
	/*loading the RDF data from test file in the Sesame store*/
	sesameRepository = new SailRepository(new MemoryStore());
	try {
	    sesameRepository.initialize();
	    connection = sesameRepository.getConnection();
	    File f = new File(MODEL_FILE_PATH);
	    connection.add(f, f.toURL().toString(), RDFFormat.RDFXML);
	}
	catch (Exception ex){
	    System.err.println("Error during test setup:");
	    ex.printStackTrace();
	}
	fse = new FSLSesameEvaluator(nsr, fhs);
	fse.setRepository(sesameRepository);
    }

    /* performed after each test */
    protected void tearDown(){
	try {
	    connection.close();
	}
	catch (RepositoryException ex){
	    System.err.println("Error during test tear down:");
	    ex.printStackTrace();
	}
    }

    /* tests */

    public void testNumberOfResources(){
	String path = "*";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 26);
    }

    public void testNumberOfLiterals(){
	String path = "text()";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 39);
    }

    public void testNumberOfStatements(){
	String path = "*";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.ARC_STEP));
	assertEquals(pathInstances.size(), 80);
    }

    public void testNumberOfPersons(){
	String path = "foaf:Person";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 17);
    }

    public void testNumberOfFOAFknows(){
	String path = "foaf:knows";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.ARC_STEP));
	assertEquals(pathInstances.size(), 16);
    }

    public void testCount1(){
	String path = "foaf:Person[count(foaf:knows) > 5]";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 1);
    }

    public void testCount2(){
	String path = "foaf:Person[count(foaf:knows) < 5]";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 16);
    }

    public void testLocalName(){
	String path = "*[local-name(.) = \"mbox_sha1sum\"]";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.ARC_STEP));
	assertEquals(pathInstances.size(), 18);
    }

    public void testNamespaceURI1(){
	String path = "*[namespace-uri(.) != 'http://xmlns.com/foaf/0.1/']";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.ARC_STEP));
	assertEquals(pathInstances.size(), 23);
    }

    public void testNamespaceURI2(){
	String path = "*[namespace-uri(.) != 'http://xmlns.com/foaf/0.1/' and namespace-uri(.) != 'http://www.w3.org/1999/02/22-rdf-syntax-ns#']";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.ARC_STEP));
	assertEquals(pathInstances.size(), 5);
    }

    public void testURI(){
	String path = "*[uri(.) = \"http://www.daml.org/cgi-bin/airport?CDG\"]";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 1);
    }

    public void testExp(){
	String path = "*[uri(.) = exp('foaf:Person')]";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 1);
    }

    public void testLiteralValue(){
	String path = "*[literal-value(foaf:name) = 'Emmanuel Pietriga']";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 1);
    }

    public void testStartsWith(){
	String path = "*[starts-with(foaf:name/text(), 'Eric')]";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 2);
    }

    public void testNestedFunctionCalls(){
	String path = "*[starts-with(literal-value(foaf:name), 'Eric')]";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 2);
    }

    public void testConcat(){
	String path = "*[foaf:name/text() = concat(\"Emmanuel\", \" Pietriga\")]";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 1);
    }

    public void testSubstringBefore(){
	String path = "*[substring-before(foaf:name/text(), \" \") = \"Emmanuel\"]";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 1);
    }

    public void testSubstringAfter(){
	String path = "*[substring-after(foaf:name/text(), \" \") = \"Pietriga\"]";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 1);
    }

    public void testSubstring1(){
	String path = "*[substring(foaf:name/text(), 10) = \"Pietriga\"]";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 1);
    }

    public void testSubstring2(){
	String path = "*[substring(foaf:name/text(), 10, 2) = \"Pi\"]";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 1);
    }

    public void testStringLength(){
	String path = "*[string-length(foaf:name/text()) = 11]";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 3);
    }

    public void testNumber1(){
	String path = "*[number(\"11\") = 11]";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 26);
    }

    public void testNumber2(){
	String path = "*[number(\"0.789\") = 0.789]";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 26);
    }

    public void testNumber3(){
	String path = "*[number(\"-100.789\") = -100.789]";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 26);
    }

    public void testBoolean(){
	String path = "*[boolean(foaf:name) or boolean(rdf:type)]";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 18);
    }

    public void testTrueFalse(){
	String path = "*[true() != false()]";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 26);
    }

    public void test5stepPathEndingWithLiteral(){
	String path = "*/*/*/*/text()";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 34);
    }

    public void test5stepPathEndingWithResource(){
	String path = "*/*/*/*/*";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 17);
    }
    
    public void testArcEndingAtResource1(){
	String path = "*/*";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.ARC_STEP));
	assertEquals(pathInstances.size(), 41);
    }

    public void testArcEndingAtResource2(){
	String path = "*/in::*";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 41);
    }

    public void testArcEndingAtLiteral1(){
	String path = "*/text()";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.ARC_STEP));
	assertEquals(pathInstances.size(), 39);
    }

    public void testArcEndingAtLiteral2(){
	String path = "text()/in::*";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 39);
    }

    public void testOneStepPathWithTwoPredicates(){
	String path = "foaf:Person[foaf:knows/foaf:Person and foaf:name/text() = \"Emmanuel Pietriga\"]";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 1);
    }

    public void testComplexPath1(){
	String path = "foaf:Person[foaf:knows/foaf:Person and foaf:name/text() = \"Emmanuel Pietriga\"]/air:nearestAirport/air:Airport[air:iataCode/text() = 'CDG']";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 1);
    }

    public void testComplexPath2(){
	String path = "foaf:Person[foaf:knows/foaf:Person and foaf:name/text() = \"Emmanuel Pietriga\"]/air:nearestAirport/air:Airport[air:iataCode/text() = 'CDG']/airport:iataCode/text()";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 1);
    }

    public void testComplexPathWithManyInstances(){
	String path = "foaf:Person/foaf:knows/*/rdf:type/*[uri(.) = exp(\"foaf:Person\")]/in::rdf:type";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 272);
    }

    public void testEvenMoreComplexPathWithNotSoManyInstances(){
	String path = "foaf:Person/foaf:knows/*/rdf:type/*[uri(.) = exp(\"foaf:Person\")]/in::rdf:type/*/foaf:name[starts-with(text(), \"Eric\")]";
	Vector pathInstances = fse.evaluatePath(FSLPath.pathFactory(path, nsr, FSLPath.NODE_STEP));
	assertEquals(pathInstances.size(), 32);
    }

    public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTestSuite(FSLSesameTest.class);
	return suite;
    }

}