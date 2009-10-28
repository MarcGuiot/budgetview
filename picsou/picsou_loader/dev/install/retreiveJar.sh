#!/bin/sh

if [ $# != 1 ];
then
  echo "missing jar version"
  exit -1
fi

if [ -a retreiveJar.sh ];
then
  echo back to ../..
  cd ../../
fi

if [ -d ../picsou/obfuscated ]; # je n'ai pas trouve comment faire un not (!) 
then
  echo -n ""
else
  echo directory ../picsou/obfuscated is missing from `pwd`
  exit -1
fi

JAR_VERSION=$1

scp build@91.121.123.100:versions/cashpilot${JAR_VERSION}.jar ../picsou/obfuscated/cashpilot.jar
scp build@91.121.123.100:versions/ChangeLogOutput-${JAR_VERSION}.txt.bz2 ../picsou/


