#!/bin/sh

createZip() {
  zip budgetview.zip budgetview/budgetview.cmd budgetview/license.txt budgetview/budgetview${JAR_VERSION}.jar \
      budgetview/budgetviewloader-1.0.jar budgetview/budgetview_icon_16.png budgetview/budgetview_icon_32.png \
      budgetview/budgetview_icon_48.png budgetview/budgetview_icon_128.png budgetview/version.txt
  
}

VERSION=`java -jar ../picsou/target/obfuscated/budgetview.jar -v -jar | grep "Jar version"`
JAR_VERSION=`echo $VERSION | sed -e 's/Jar version://g' | sed -e 's/  *//g'`

SOFT_VERSION=`java -jar ../picsou/target/obfuscated/budgetview.jar -v -soft | grep "Software version:" |
              sed -e 's/Software version://g' | sed -e 's/  *//g'`

rm -rf budgetview budgetview.zip
mkdir -p budgetview
java -jar ../picsou/target/obfuscated/budgetview.jar -v  | grep version > budgetview/version.txt
cp dev/images/budgetview_icon_16.png budgetview/
cp dev/images/budgetview_icon_32.png budgetview/
cp dev/images/budgetview_icon_48.png budgetview/
cp dev/images/budgetview_icon_128.png budgetview/
cp dev/install/license.txt budgetview/
cp dev/install/copyright budgetview/
cp target/budgetviewloader-1.0.jar budgetview/
cp ../picsou/target/obfuscated/budgetview${JAR_VERSION}.jar budgetview/

cp dev/install/budgetview.en.cmd budgetview/budgetview.cmd
createZip
mv budgetview.zip budgetview-${SOFT_VERSION}-en.zip

cp dev/install/budgetview.fr.cmd budgetview/budgetview.cmd
createZip
mv budgetview.zip budgetview-${SOFT_VERSION}-fr.zip

rm -rf budgetview

