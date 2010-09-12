#!/bin/sh

VERSION=`java -jar ../picsou/obfuscated/budgetview.jar -v -jar | grep "Jar version"`
JAR_VERSION=`echo $VERSION | sed -e 's/Jar version://g' | sed -e 's/  *//g'`

SOFT_VERSION=`java -jar ../picsou/obfuscated/budgetview.jar -v -soft | grep "Software version:" |
              sed -e 's/Software version://g' | sed -e 's/  *//g'`

rm -f ../picsou/obfuscated/budgetview${JAR_VERSION}.jar
cp ../picsou/obfuscated/budgetview.jar ../picsou/obfuscated/budgetview${JAR_VERSION}.jar
bzip2 -c ../picsou/ChangeLogOutput.txt > ../picsou/ChangeLogOutput-${JAR_VERSION}.txt.bz2
mkdir -p src/test/resources/jars
cp ../picsou/obfuscated/budgetview.jar src/test/resources/jars/budgetview.jar

rm -rf budgetview budgetview.zip
mkdir -p budgetview
java -jar ../picsou/obfuscated/budgetview.jar -v  | grep version > budgetview/version.txt
cp dev/images/budgetview_icon_16.png budgetview/
cp dev/images/budgetview_icon_32.png budgetview/
cp dev/images/budgetview_icon_48.png budgetview/
cp dev/images/budgetview_icon_128.png budgetview/
cp dev/install/budgetview.sh budgetview/
cp dev/install/license.txt budgetview/
cp target/budgetviewloader-1.0.jar budgetview/
cp ../picsou/obfuscated/budgetview${JAR_VERSION}.jar budgetview/
tar cvf budgetview.tar budgetview/budgetview.sh budgetview/license.txt budgetview/budgetview${JAR_VERSION}.jar \
    budgetview/budgetviewloader-1.0.jar budgetview/budgetview_icon_16.png budgetview/budgetview_icon_32.png \
    budgetview/budgetview_icon_48.png budgetview/budgetview_icon_128.png budgetview/version.txt

gzip budgetview.tar

mv budgetview.tar.gz budgetview-${SOFT_VERSION}.tar.gz
rm -rf budgetview
