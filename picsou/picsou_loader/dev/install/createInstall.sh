#!/bin/sh

cd dev/install
dev/install/cmd.sh
cd ../..
rm ../picsou/fourmics.jar
VERSION=`java -jar ../picsou/obfuscated/fourmics.jar -v -jar`
echo $VERSION
JAR_VERSION=`echo $VERSION | grep "Jar version:" | sed -e 's/Jar version://g' | sed -e 's/  *//g'`
echo $JAR_VERSION
mv ../picsou/obfuscated/fourmics.jar ../picsou/obfuscated/fourmics${JAR_VERSION}.jar
cat dev/install/picsou.template.mpi | sed -e "s/JAR_FOURMICS/fourmics${JAR_VERSION}/" > dev/install/picsou.mpi
~/dev/installer/installjammer/installjammer --build-for-release dev/install/picsou.mpi


rm -rf fourmics fourmics.zip
mkdir -p fourmics
cp dev/images/fourmics_icon_16.png fourmics/
cp dev/images/fourmics_icon_32.png fourmics/
cp dev/images/fourmics_icon_48.png fourmics/
cp dev/images/fourmics_icon_128.png fourmics/
cp dev/install/fourmics.sh fourmics/
cp dev/install/license.txt fourmics/
cp target/fourmicsloader-1.0.jar fourmics/fourmicsloader.jar
cp ../picsou/obfuscated/fourmics${JAR_VERSION}.jar fourmics/
zip fourmics.zip fourmics/fourmics.sh fourmics/license.txt fourmics/fourmics${JAR_VERSION}.jar fourmics/fourmicsloader.jar fourmics/fourmics_icon_16.png fourmics/fourmics_icon_32.png fourmics/fourmics_icon_48.png fourmics/fourmics_icon_128.png
