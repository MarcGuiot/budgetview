#!/bin/sh

if [ $# != 1 ];
then
  echo "missing jar version"
  exit -1
fi

dev/install/retrieveJar.sh $1
mvn clean install -Dmaven.test.skip=true
dev/install/deploy.sh

