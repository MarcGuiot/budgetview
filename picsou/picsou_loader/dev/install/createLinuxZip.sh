#!/bin/sh

VERSION=`java -jar ../picsou/obfuscated/fourmics.jar -v -jar`
JAR_VERSION=`echo $VERSION | grep "Jar version:" | sed -e 's/Jar version://g' | sed -e 's/  *//g'`

rm ../picsou/obfuscated/fourmics${JAR_VERSION}.jar
cp ../picsou/obfuscated/fourmics.jar ../picsou/obfuscated/fourmics${JAR_VERSION}.jar

rm -rf Fourmics Fourmics.zip
mkdir -p Fourmics
cp dev/images/fourmics_icon_16.png Fourmics/
cp dev/images/fourmics_icon_32.png Fourmics/
cp dev/images/fourmics_icon_48.png Fourmics/
cp dev/images/fourmics_icon_128.png Fourmics/
cp dev/install/fourmics.sh Fourmics/
cp dev/install/license.txt Fourmics/
cp target/fourmicsloader-1.0.jar Fourmics/
cp ../picsou/obfuscated/fourmics${JAR_VERSION}.jar Fourmics/
zip Fourmics.zip Fourmics/fourmics.sh Fourmics/license.txt Fourmics/fourmics${JAR_VERSION}.jar Fourmics/fourmicsloader-1.0.jar Fourmics/fourmics_icon_16.png Fourmics/fourmics_icon_32.png Fourmics/fourmics_icon_48.png Fourmics/fourmics_icon_128.png
rm -rf Fourmics