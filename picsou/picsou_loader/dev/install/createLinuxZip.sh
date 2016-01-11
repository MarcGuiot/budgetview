#!/bin/sh

createTar() {
  tar cvf budgetview.tar budgetview/budgetview.sh budgetview/license.txt budgetview/budgetview${JAR_VERSION}.jar \
      budgetview/budgetviewloader-1.0.jar budgetview/budgetview_icon_16.png budgetview/budgetview_icon_32.png \
      budgetview/budgetview_icon_48.png budgetview/budgetview_icon_128.png budgetview/version.txt \
      budgetview/budgetview.desktop
  
  gzip budgetview.tar
}

VERSION=`java -jar ../picsou/target/obfuscated/budgetview.jar -v -jar | grep "Jar version"`
JAR_VERSION=`echo $VERSION | sed -e 's/Jar version://g' | sed -e 's/  *//g'`

SOFT_VERSION=`java -jar ../picsou/target/obfuscated/budgetview.jar -v -soft | grep "Software version:" |
              sed -e 's/Software version://g' | sed -e 's/  *//g'`

rm -f ../picsou/target/obfuscated/budgetview${JAR_VERSION}.jar
cp ../picsou/target/obfuscated/budgetview.jar ../picsou/target/obfuscated/budgetview${JAR_VERSION}.jar
bzip2 -c ../picsou/ChangeLogOutput.txt > ../picsou/ChangeLogOutput-${JAR_VERSION}.txt.bz2
mkdir -p src/test/resources/jars
cp ../picsou/target/obfuscated/budgetview.jar src/test/resources/jars/budgetview.jar

rm -rf budgetview budgetview.zip
mkdir -p budgetview
java -jar ../picsou/target/obfuscated/budgetview.jar -v  | grep version > budgetview/version.txt
cp dev/images/budgetview_icon_16.png budgetview/
cp dev/images/budgetview_icon_32.png budgetview/
cp dev/images/budgetview_icon_48.png budgetview/
cp dev/images/budgetview_icon_128.png budgetview/
cp dev/install/license.txt budgetview/
cp dev/install/copyright budgetview/
cp dev/install/budgetview.desktop budgetview/
cp target/budgetviewloader-1.0.jar budgetview/
cp ../picsou/target/obfuscated/budgetview${JAR_VERSION}.jar budgetview/

cp dev/install/budgetview.en.sh budgetview/budgetview.sh
createTar
mv budgetview.tar.gz budgetview-${SOFT_VERSION}-en.tar.gz

cp dev/install/budgetview.fr.sh budgetview/budgetview.sh
createTar
mv budgetview.tar.gz budgetview-${SOFT_VERSION}-fr.tar.gz

if [ -a /usr/bin/dpkg ] ; then
  dev/install/generateDeb.sh ${JAR_VERSION} ${SOFT_VERSION}
fi

rm -rf budgetview
mkdir budgetview

cp ../picsou/target/obfuscated/budgetview.jar budgetview
cp dev/install/budgetviewInMemory.sh budgetview/budgetviewInMemory.sh
tar cvf budgetviewInMemory.tar budgetview/budgetviewInMemory.sh budgetview/budgetview.jar
gzip budgetviewInMemory.tar
mv budgetviewInMemory.tar.gz budgetviewInMemory-${SOFT_VERSION}.tar.gz
rm -rf budgetview

