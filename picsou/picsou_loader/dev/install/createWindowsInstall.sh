#!/bin/sh

cd dev/install
./cmd.fr.sh
mv budgetview.exe budgetview.fr.exe
./cmd.en.sh
mv budgetview.exe budgetview.en.exe
cd ../..
rm -f ../picsou/budgetview.jar
VERSION=`java -jar ../picsou/obfuscated/budgetview.jar -v -jar | grep "Jar version"`
JAR_VERSION=`echo $VERSION | sed -e 's/Jar version://g' | sed -e 's/  *//g'`
SOFT_VERSION=`java -jar ../picsou/obfuscated/budgetview.jar -v -soft | grep "Software version:" |
              sed -e 's/Software version://g' | sed -e 's/  *//g'`

rm -f ../picsou/obfuscated/budgetview${JAR_VERSION}.jar
cp ../picsou/obfuscated/budgetview.jar ../picsou/obfuscated/budgetview${JAR_VERSION}.jar
bzip2 -c ../picsou/ChangeLogOutput.txt > ../picsou/ChangeLogOutput-${JAR_VERSION}.txt.bz2

mv dev/install/budgetview.fr.exe dev/install/budgetview.exe
cat dev/install/picsou.template.fr.mpi | sed -e "s/JAR_FOURMICS/budgetview${JAR_VERSION}/" |
    sed -e "s/SOFT_VERSION/${SOFT_VERSION}/" > dev/install/picsou.fr.mpi

~/dev/installer/installjammer/installjammer --build-for-release dev/install/picsou.fr.mpi

mv dev/install/budgetview.en.exe dev/install/budgetview.exe
cat dev/install/picsou.template.en.mpi | sed -e "s/JAR_FOURMICS/budgetview${JAR_VERSION}/" |
    sed -e "s/SOFT_VERSION/${SOFT_VERSION}/" > dev/install/picsou.en.mpi

~/dev/installer/installjammer/installjammer --build-for-release dev/install/picsou.en.mpi

dev/install/createWindowsZip.sh
