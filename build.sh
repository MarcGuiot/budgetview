#!/bin/sh

export JAVA_HOME=/opt/jdk1.5.0_22
export JAVA_6=/opt/sun-jdk-1.6.0.45/bin/java
mvn clean install -Dmaven.test.skip.exec=true
