#!/bin/sh

rm ../picsou/fourmics.jar
VERSION=`java -jar ../picsou/obfuscated/fourmics.jar -v -jar`
echo $VERSION
JAR_VERSION=`echo $VERSION | grep "Jar version:" | sed -e 's/Jar version://g' | sed -e 's/  *//g'`
echo $JAR_VERSION
mv ../picsou/obfuscated/fourmics.jar ../picsou/obfuscated/fourmics${JAR_VERSION}.jar
cat dev/install/picsou.template.mpi | sed -e "s/JAR_FOURMICS/fourmics${JAR_VERSION}/" > dev/install/picsou.mpi
~/dev/installer/installjammer/installjammer --build-for-release dev/install/picsou.mpi
