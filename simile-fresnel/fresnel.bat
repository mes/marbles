set JAVA_HOME="C:\programme\java\jdk1.6.0_02"
set FRESNEL_HOME="C:\work\work\projekte\diplomarbeit\simile-fresnel"
::set JAVA_OPTIONS="-Xms32M -Xmx512M"
set JAVA_OPTIONS=

set JAVA="%JAVA_HOME%/bin/java"
set ENDORSED_LIBS="%FRESNEL_HOME%/lib/endorsed"
set ENDORSED="-Djava.endorsed.dirs=%ENDORSED_LIBS%"
set PARSER=-Dorg.xml.sax.parser=org.apache.xerces.parsers.SAXParser
set CP="`echo %FRESNEL_HOME%/target/*.jar | tr ' ' %PATHSEP%`"

%JAVA% %JAVA_OPTIONS% -cp %CP% %ENDORSED% %PARSER% edu.mit.simile.fresnel.Fresnel %ARGS%