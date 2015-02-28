#!/bin/sh

if [ $# -eq 0 ] || [ "$1" = "demo" ]; then
  if [ `hg qnext` == "uispec" ]; then
    hg qpush
  fi;
export JAVA_HOME=$JAVA_1_6_HOME
mvn clean install -Dmaven.test.skip.exec=true -am -pl picsou/picsou -Pgen-demo
fi;

if [ $# -eq 0 ] || [ "$1" == "version" ]; then
   if hg identify | grep uispec; then
      hg qpop
   fi;
export JAVA_HOME=$JAVA_1_5_HOME
mvn clean install -Dmaven.test.skip.exec=true -Pgen-version
fi;
