#!/bin/bash

#if [ $# -lt 1 ] ; then
#      echo "Syntax: $0 GRIFFIN_HOME"
#      exit 0
#fi

GRIFFIN_HOME=`pwd`
echo "Installing griffin into $GRIFFIN_HOME..."
sed "s@GRIFFIN_HOME_PLACE_HOLDER@$GRIFFIN_HOME@g" log4j.properties > log4j.p
mv log4j.p log4j.properties
cp griffin /etc/init.d/
sed "s@GRIFFIN_HOME_PLACE_HOLDER@$GRIFFIN_HOME@g" etc-default-griffin > /etc/default/griffin
echo "Done."
exit 0
