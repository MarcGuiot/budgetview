#!/bin/sh

VERSION=`java -jar ../picsou/obfuscated/cashpilot.jar -v -jar`
JAR_VERSION=`echo $VERSION | grep "Jar version:" | sed -e 's/Jar version://g' | sed -e 's/  *//g'`

SOFT_VERSION=`java -jar ../picsou/obfuscated/cashpilot.jar -v -soft | grep "Software version:" |
              sed -e 's/Software version://g' | sed -e 's/  *//g'`

rm ../picsou/obfuscated/cashpilot${JAR_VERSION}.jar
cp ../picsou/obfuscated/cashpilot.jar ../picsou/obfuscated/cashpilot${JAR_VERSION}.jar
mkdir -p src/test/resources/jars
cp ../picsou/obfuscated/cashpilot.jar src/test/resources/jars/cashpilot.jar

rm -rf cashpilot cashpilot.zip
mkdir -p cashpilot
java -jar ../picsou/obfuscated/cashpilot.jar -v  > cashpilot/version.txt
cp dev/images/cashpilot_icon_16.png cashpilot/
cp dev/images/cashpilot_icon_32.png cashpilot/
cp dev/images/cashpilot_icon_48.png cashpilot/
cp dev/images/cashpilot_icon_128.png cashpilot/
cp dev/install/cashpilot.sh cashpilot/
cp dev/install/license.txt cashpilot/
cp target/cashpilotloader-1.0.jar cashpilot/
cp ../picsou/obfuscated/cashpilot${JAR_VERSION}.jar cashpilot/
tar cvf cashpilot.tar cashpilot/cashpilot.sh cashpilot/license.txt cashpilot/cashpilot${JAR_VERSION}.jar \
    cashpilot/cashpilotloader-1.0.jar cashpilot/cashpilot_icon_16.png cashpilot/cashpilot_icon_32.png \
    cashpilot/cashpilot_icon_48.png cashpilot/cashpilot_icon_128.png cashpilot/version.txt

gzip cashpilot.tar 

mv cashpilot.tar.gz cashpilot-${SOFT_VERSION}.tar.gz
rm -rf cashpilot
