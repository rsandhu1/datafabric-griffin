#!/bin/bash

# Have we got the one parametre that we need?
# (Install target directory name.)
if [ $# -lt 1 ] ; then
    echo "Syntax: $0 GRIFFIN_HOME"
    exit 0
fi

# This is where Griffin will be installed to.
GRIFFIN_HOME=$1
echo "Installing griffin into $GRIFFIN_HOME ..."

# Prepare startup configuration and put it into place
cp bin/griffin /etc/init.d/
sed "s@GRIFFIN_HOME_PLACE_HOLDER@$GRIFFIN_HOME@g" \
    bin/etc-default-griffin > /etc/default/griffin

# Make the target directories and install Griffin.
mkdir -p $GRIFFIN_HOME
mkdir $GRIFFIN_HOME/logs
mkdir $GRIFFIN_HOME/lib
mkdir $GRIFFIN_HOME/plugins
cp dist/griffin.jar $GRIFFIN_HOME  
cp dist/*-plugin.jar $GRIFFIN_HOME/plugins
cp lib/main/*.jar $GRIFFIN_HOME/lib

# Prepare and copy the logging configuration.
sed "s@GRIFFIN_HOME_PLACE_HOLDER@$GRIFFIN_HOME@g" \
    src/main/resources/log4j.properties > $GRIFFIN_HOME/log4j.properties

echo "Done."
echo "**************************"
echo "Now, make sure adequate griffin-ctx.xml (possibly griffin-users.xml)"
echo "are copied to $GRIFFIN_HOME, as well as xxx-plugin.jar and JARs"
echo "required for it to $GRIFFIN_HOME/plugins."
echo "**************************"
exit 0
