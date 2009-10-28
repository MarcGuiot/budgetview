#!/bin/sh

cd dev/install
./cmd.sh
cd ../..
rm -f ../picsou/cashpilot.jar
VERSION=`java -jar ../picsou/obfuscated/cashpilot.jar -v -jar | grep "Jar version"`
JAR_VERSION=`echo $VERSION | sed -e 's/Jar version://g' | sed -e 's/  *//g'`
SOFT_VERSION=`java -jar ../picsou/obfuscated/cashpilot.jar -v -soft | grep "Software version:" |
              sed -e 's/Software version://g' | sed -e 's/  *//g'`

rm -f ../picsou/obfuscated/cashpilot${JAR_VERSION}.jar
cp ../picsou/obfuscated/cashpilot.jar ../picsou/obfuscated/cashpilot${JAR_VERSION}.jar
bzip2 -c ../picsou/ChangeLogOutput.txt > ../picsou/ChangeLogOutput-${JAR_VERSION}.txt.bz2

cat dev/install/picsou.template.mpi | sed -e "s/JAR_FOURMICS/cashpilot${JAR_VERSION}/" |
    sed -e "s/SOFT_VERSION/${SOFT_VERSION}/" > dev/install/picsou.mpi

~/installjammer/installjammer --build-for-release dev/install/picsou.mpi
