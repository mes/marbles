#mvn -Dmaven.test.skip=true package assembly:directory source:aggregate
mvn -Dmaven.test.skip=true package assembly:directory
cd target
cp ~/Desktop/Diplomarbeit/sesame-2.0/META-INF/services/* openrdf-sesame-2.1-SNAPSHOT-onejar.dir/META-INF/services/
cd openrdf-sesame-2.1-SNAPSHOT-onejar.dir 
jar cmf META-INF/MANIFEST.MF ../openrdf-sesame-2.1-SNAPSHOT-onejar.jar *
cd ../..
cp target/openrdf-sesame-2.1-SNAPSHOT-onejar.jar ../../Marbles/web/WEB-INF/lib/
cp target/openrdf-sesame-2.1-SNAPSHOT-onejar.jar ../../SesameTool/lib/ 
