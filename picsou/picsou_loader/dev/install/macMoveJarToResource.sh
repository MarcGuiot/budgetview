#/bin/sh

rm -f ../picsou/fourmics.jar
VERSION=`java -jar ../picsou/obfuscated/fourmics.jar -v -jar`
JAR_VERSION=`echo $VERSION | grep "Jar version:" | sed -e 's/Jar version://g' | sed -e 's/  *//g'`
cp ../picsou/obfuscated/fourmics.jar Fourmics/Fourmics.app/Contents/Resources/fourmics${JAR_VERSION}.jar
