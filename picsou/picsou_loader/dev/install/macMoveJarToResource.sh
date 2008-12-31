#/bin/sh

rm -f ../picsou/cashpilot.jar
VERSION=`java -jar ../picsou/obfuscated/cashpilot.jar -v -jar`
JAR_VERSION=`echo $VERSION | grep "Jar version:" | sed -e 's/Jar version://g' | sed -e 's/  *//g'`
cp ../picsou/obfuscated/cashpilot.jar CashPilot/CashPilot.app/Contents/Resources/cashpilot${JAR_VERSION}.jar
