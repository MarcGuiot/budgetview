#!/bin/sh

if [ $# != 1 ];
then
  echo "missing jar version"
  exit -1
fi

if [ -a retrieveJar.sh ];
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

curl -o ../picsou/obfuscated/budgetview.jar http://www.mybudgetview.fr/files/app/budgetview${JAR_VERSION}.jar

# scp build@91.121.123.100:versions/budgetview${JAR_VERSION}.jar ../picsou/obfuscated/budgetview.jar
scp build@91.121.123.100:versions/ChangeLogOutput-${JAR_VERSION}.txt.bz2 ../picsou/
rm ../picsou/ChangeLogOutput.txt
bunzip2 ../picsou/ChangeLogOutput-${JAR_VERSION}.txt.bz2
mv ../picsou/ChangeLogOutput-${JAR_VERSION}.txt ../picsou/ChangeLogOutput.txt

