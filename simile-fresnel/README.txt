
                +-------------------------------------+
                |       SIMILE Fresnel Engine         |
                +-------------------------------------+

  What is this?
  ------------

Fresnel is an RDF ontology for displaying RDF.

This is the home of BOTH the SIMILE Fresnel Engine and a coordination
point for the Fresnel effort.  Files related to Fresnel coordination
can be found in the ontologies/, docs/, and examples/ directory.

The SIMILE engine is concerned with the remaining files; this README
is concerned only with the engine.  See the following for more on the
ontology:

  http://www.w3.org/2005/04/fresnel-info/

  What is the current status?
  ---------------------------

Fresnel is currently being integrated into Longwell, SIMILE's RDF
browser.  It is ostensibly complete in regards to the Fresnel Core
vocabulary, but without test cases this cannot be asserted too
strongly.

  How do I use it?
  ---------------

For users, you'll need to have Maven 2.x installed.  Maven manages
JAR dependencies. See http://maven.apache.org

To build with Maven 2, run

  % mvn package

If you don't want to build it yourself, you can consult SIMILE's
Maven repository instead:

  http://simile.mit.edu/maven/

  Deployment Notes
  ----------------

Fresnel generates two .jar files for deployment to the SIMILE Maven
repository, fresnel and fresnel-vocabularies.  The dependencies were
manually bootstrapped into place; if you're interested, for whatever
reason, in the more complex build process involving generating the
source code and jar for vocabularies, use the bootstrap.xml POM file.

To generate sources, run

  % mvn -f bootstrap.xml generate-sources

Repository maintainers' note: to deploy vocabularies, run:

  % mvn deploy:deploy-file -DpomFile=vocab.xml \
      -Dfile=target/vocabularies.jar \
      -DrepositoryId=simile.mit.edu \
      -Durl=scpexe://simile.mit.edu/var/maven

To deploy the engine, run:

  % mvn deploy 

  Credits
  -------

Emmanuel Pietriga wrote the FSL engine code on which the Fresnel
engine relies for interpreting and implementing FSL selection.  FSL
for IsaViz can be found at:
  http://www.lri.fr/~pietriga/2005/11/fsl/

Many thanks to those who submitted patches: Steve Dunham and Barry Kim.

					    Ryan Lee <ryanlee at w3.org>
