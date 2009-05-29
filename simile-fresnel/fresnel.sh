#!/bin/sh
#
# Configuration variables
#
# JAVA_HOME
#   Home of Java installation.
#
# JAVA_OPTIONS
#   Extra options to pass to the JVM
#
# FRESNEL_HOME
#   Home of Fresnel installation.

ARGS="$*"

# ----- Verify and Set Required Environment Variables -------------------------

if [ "$JAVA_HOME" = "" ] ; then
  echo You must set JAVA_HOME to point at your Java Development Kit installation
  exit 1
fi

if [ "$JAVA_OPTIONS" = "" ] ; then
  JAVA_OPTIONS='-Xms32M -Xmx512M'
fi

if [ "$FRESNEL_HOME" = "" ] ; then
    FRESNEL_HOME="."
fi

if [ ! -d $FRESNEL_HOME/target ] ; then
    echo "Couldn't find jars for Fresnel, please set the FRESNEL_HOME directory"
    exit 1
fi

# ----- Set platform specific variables

PATHSEP=":";
case "`uname`" in
   CYGWIN*) PATHSEP=";" ;;
esac

# ----- Set Local Variables ( used to minimize cut/paste) ---------------------

JAVA="$JAVA_HOME/bin/java"
ENDORSED_LIBS="$FRESNEL_HOME/lib/endorsed"
ENDORSED="-Djava.endorsed.dirs=$ENDORSED_LIBS"
PARSER=-Dorg.xml.sax.parser=org.apache.xerces.parsers.SAXParser
CP="`echo $FRESNEL_HOME/target/*.jar | tr ' ' $PATHSEP`"

# ----- Do the action ----------------------------------------------------------

$JAVA $JAVA_OPTIONS -cp $CP $ENDORSED $PARSER edu.mit.simile.fresnel.Fresnel $ARGS

exit 0
