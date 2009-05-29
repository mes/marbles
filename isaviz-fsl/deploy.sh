mvn -Dmaven.test.skip=true package
cp target/isaviz-fsl-* ../simile-fresnel/target
cp target/isaviz-fsl-* ../Marbles/web/WEB-INF/lib
