#!/bin/sh

VERSION=`java -jar ../picsou/obfuscated/fourmics.jar -v -jar`
JAR_VERSION=`echo $VERSION | grep "Jar version:" | sed -e 's/Jar version://g' | sed -e 's/  *//g'`

SOFT_VERSION=`java -jar ../picsou/obfuscated/fourmics.jar -v -soft | grep "Software version:" |
              sed -e 's/Software version://g' | sed -e 's/  *//g'`

rm ../picsou/obfuscated/fourmics${JAR_VERSION}.jar
cp ../picsou/obfuscated/fourmics.jar ../picsou/obfuscated/fourmics${JAR_VERSION}.jar
mkdir -p src/test/resources/jars
cp ../picsou/obfuscated/fourmics.jar src/test/resources/jars/fourmics.jar

rm -rf fourmics fourmics.zip
mkdir -p fourmics
java -jar ../picsou/obfuscated/fourmics.jar -v  > fourmics/version.txt
cp dev/images/fourmics_icon_16.png fourmics/
cp dev/images/fourmics_icon_32.png fourmics/
cp dev/images/fourmics_icon_48.png fourmics/
cp dev/images/fourmics_icon_128.png fourmics/
cp dev/install/fourmics.sh fourmics/
cp dev/install/license.txt fourmics/
cp target/fourmicsloader-1.0.jar fourmics/
cp ../picsou/obfuscated/fourmics${JAR_VERSION}.jar fourmics/
zip fourmics.zip fourmics/fourmics.sh fourmics/license.txt fourmics/fourmics${JAR_VERSION}.jar \
    fourmics/fourmicsloader-1.0.jar fourmics/fourmics_icon_16.png fourmics/fourmics_icon_32.png \
    fourmics/fourmics_icon_48.png fourmics/fourmics_icon_128.png fourmics/version.txt

mv fourmics.zip fourmis-${SOFT_VERSION}.zip
rm -rf fourmics
