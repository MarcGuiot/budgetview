#!/bin/sh

if [ "$1" == "demo" ]; then

export JAVA_HOME=/opt/sun-jdk-1.6.0.45
mvn clean install -Dmaven.test.skip.exec=true -am -pl picsou/picsou -Pgen-demo
fi;

if [ "$1" == "version" ]; then
export JAVA_HOME=/opt/jdk1.5.0_22
mvn clean install -Dmaven.test.skip.exec=true -Pgen-version
fi;
