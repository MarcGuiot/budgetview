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

if [ -d ../picsou/target/obfuscated ]; # je n'ai pas trouve comment faire un not (!)
then
  echo -n ""
else
  echo directory ../picsou/target/obfuscated is missing from `pwd`
  exit -1
fi

JAR_VERSION=$1

curl -o ../picsou/target/obfuscated/budgetview.jar http://www.mybudgetview.fr/files/app/budgetview${JAR_VERSION}.jar

curl -o ../picsou/target/obfuscated/ http://www.mybudgetview.fr/files/app/ChangeLogOutput-${JAR_VERSION}.txt.bz2 ../picsou/

rm ../picsou/ChangeLogOutput.txt
bunzip2 ../picsou/ChangeLogOutput-${JAR_VERSION}.txt.bz2
mv ../picsou/ChangeLogOutput-${JAR_VERSION}.txt ../picsou/ChangeLogOutput.txt

