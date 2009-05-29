/*   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2005-2006.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: FSLHierarchyTest.java 86 2007-11-07 14:34:28Z luong $
 */ 

package fr.inria.jfresnel.fsl.sesame;

import java.io.*;
import java.util.Vector;

import fr.inria.jfresnel.fsl.sesame.*;

import junit.framework.*;

public class FSLSesameHierarchyTest extends TestCase {
    
    protected FSLSesameHierarchyStore fshs;

    public FSLSesameHierarchyTest(String name){
	super(name);
    }
    
    /* init */
    protected void setUp(){
	fshs = new FSLSesameHierarchyStore();
	fshs.addOntology("http://jfresnel.gforge.inria.fr/tests/fsl-hierarchy-test-model.rdfs",
			 "src/test/resources/fsl-hierarchy-test-model.rdfs");
    }

    /* tests */

    public void testSesameClassHierarchy1(){
	assertTrue(fshs.isSubclassOf("http://jfresnel.gforge.inria.fr/tests/fsl-hierarchy-test-model.rdfs#ClassD",
				     "http://jfresnel.gforge.inria.fr/tests/fsl-hierarchy-test-model.rdfs#ClassC"));
    }

    public void testSesameClassHierarchy2(){
	assertTrue(fshs.isSubclassOf("http://jfresnel.gforge.inria.fr/tests/fsl-hierarchy-test-model.rdfs#ClassD",
				     "http://jfresnel.gforge.inria.fr/tests/fsl-hierarchy-test-model.rdfs#ClassA"));
    }

    public void testSesameClassHierarchy3(){
	assertTrue(fshs.isSubclassOf("http://jfresnel.gforge.inria.fr/tests/fsl-hierarchy-test-model.rdfs#ClassC",
				     "http://jfresnel.gforge.inria.fr/tests/fsl-hierarchy-test-model.rdfs#ClassA"));
    }

    public void testSesameClassHierarchy4(){
	assertTrue(fshs.isSubclassOf("http://jfresnel.gforge.inria.fr/tests/fsl-hierarchy-test-model.rdfs#ClassB",
				     "http://jfresnel.gforge.inria.fr/tests/fsl-hierarchy-test-model.rdfs#ClassA"));
    }

    public void testSesamePropHierarchy1(){
	assertTrue(fshs.isSubpropertyOf("http://jfresnel.gforge.inria.fr/tests/fsl-hierarchy-test-model.rdfs#PropD",
					"http://jfresnel.gforge.inria.fr/tests/fsl-hierarchy-test-model.rdfs#PropC"));
    }

    public void testSesamePropHierarchy2(){
	assertTrue(fshs.isSubpropertyOf("http://jfresnel.gforge.inria.fr/tests/fsl-hierarchy-test-model.rdfs#PropD",
					"http://jfresnel.gforge.inria.fr/tests/fsl-hierarchy-test-model.rdfs#PropA"));
    }

    public void testSesamePropHierarchy3(){
	assertTrue(fshs.isSubpropertyOf("http://jfresnel.gforge.inria.fr/tests/fsl-hierarchy-test-model.rdfs#PropC",
					"http://jfresnel.gforge.inria.fr/tests/fsl-hierarchy-test-model.rdfs#PropA"));
    }

    public void testSesamePropHierarchy4(){
	assertTrue(fshs.isSubpropertyOf("http://jfresnel.gforge.inria.fr/tests/fsl-hierarchy-test-model.rdfs#PropB",
					"http://jfresnel.gforge.inria.fr/tests/fsl-hierarchy-test-model.rdfs#PropA"));
    }

    public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTestSuite(FSLSesameHierarchyTest.class);
	return suite;
    }

}