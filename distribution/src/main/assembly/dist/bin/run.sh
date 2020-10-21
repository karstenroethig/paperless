#!/bin/sh

DIRNAME=`dirname "$0"`

# Setup APP_HOME
APP_HOME=`cd "$DIRNAME/.." >/dev/null; pwd`

# Read an optional running configuration file
if [ "x$RUN_CONF" = "x" ]; then
    RUN_CONF="$DIRNAME/run.conf"
fi
if [ -r "$RUN_CONF" ]; then
    . "$RUN_CONF"
fi

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
        JAVA="$JAVA_HOME/bin/java"
    else
        JAVA="java"
    fi
fi

# Display our environment
echo "========================================================================="
echo ""
echo "  ${app.name.pretty} Bootstrap Environment"
echo ""
echo "  Home Directory: $APP_HOME"
echo ""
echo "  JAVA: $JAVA"
echo ""
echo "  JAVA_OPTS: $JAVA_OPTS"
echo ""
echo "========================================================================="
echo ""

eval \"$JAVA\" $JAVA_OPTS \
	-Djava.io.tmpdir=\""$APP_HOME"/temp/\" \
	-Dlogging.config=\""$APP_HOME"/config/logback-spring.xml\" -Dlogging.path=\""$APP_HOME"/log\" \
	-jar \""$APP_HOME"/lib/${app.name.id}.jar\" \
	--spring.config.additional-location=\""$APP_HOME"/config/\"
