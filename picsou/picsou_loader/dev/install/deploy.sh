#!/bin/sh

#if [ -a deploy.sh ];
#then
#  echo back to ../..
#  cd ../../
#fi;

if [ -a ../picsou/obfuscated/cashpilot.jar ];  # je n'ai pas trouve comment faire un not (!) 
then
  echo -n ""
else
  echo ../picsou/obfuscated/cashpilot.jar do not exist from `pwd`
  exit 1
fi;

VERSION=`java -jar ../picsou/obfuscated/cashpilot.jar -v -jar | grep "Jar version"`
JAR_VERSION=`echo $VERSION | sed -e 's/Jar version://g' | sed -e 's/  *//g'`

SOFT_VERSION=`java -jar ../picsou/obfuscated/cashpilot.jar -v -soft | grep "Software version:" |
              sed -e 's/Software version://g' | sed -e 's/  *//g'`

echo jar version : $JAR_VERSION
echo soft version : $SOFT_VERSION

if [ "${JAR_VERSION}" == "" ];
then
  echo can not extract version from ../picsou/obfuscated/cashpilot.jar
  exit 2
fi

if [ -a cashpilot-${SOFT_VERSION}.tar.gz ];
then
   scp cashpilot-${SOFT_VERSION}.tar.gz cashpilot@91.121.123.100:files/app
fi

if [ -a dev/install/output/CashPilot-${SOFT_VERSION}-Setup.exe ];
then
   scp dev/install/output/CashPilot-${SOFT_VERSION}-Setup.exe  cashpilot@91.121.123.100:files/app
fi

if [ -a CashPilot-${SOFT_VERSION}.dmg ];
then
   scp CashPilot-${SOFT_VERSION}.dmg cashpilot@91.121.123.100:files/app
fi

scp ../picsou/obfuscated/cashpilot${JAR_VERSION}.jar ../picsou/ChangeLogOutput-${JAR_VERSION}.txt.bz2 build@91.121.123.100:versions/

if [ -a ../picsou_licence_server/picsouLicenceServer.jar ];
then
  scp ../picsou_licence_server/picsouLicenceServer.jar build@91.121.123.100:versions/picsouLicenceServer${JAR_VERSION}.jar
fi

