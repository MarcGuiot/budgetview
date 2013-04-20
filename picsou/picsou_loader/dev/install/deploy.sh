#!/bin/sh

#if [ -a deploy.sh ];
#then
#  echo back to ../..
#  cd ../../
#fi;

if ! [ -a ../picsou/obfuscated/budgetview.jar ];
then
  echo ../picsou/obfuscated/budgetview.jar do not exist from `pwd`
  exit 1
fi;

VERSION=`java -jar ../picsou/obfuscated/budgetview.jar -v -jar | grep "Jar version"`
JAR_VERSION=`echo $VERSION | sed -e 's/Jar version://g' | sed -e 's/  *//g'`

SOFT_VERSION=`java -jar ../picsou/obfuscated/budgetview.jar -v -soft | grep "Software version:" |
              sed -e 's/Software version://g' | sed -e 's/  *//g'`

echo jar version : $JAR_VERSION
echo soft version : $SOFT_VERSION

if [ "${JAR_VERSION}" == "" ];
then
  echo can not extract version from ../picsou/obfuscated/budgetview.jar
  exit 2
fi

if [ -a ../../picsou_android/bin/picsou_android-debug.apk ];
then
   scp ../../picsou_android/bin/picsou_android-debug.apk fr_mybudgetview@91.121.123.100:files/app
fi

if [ -a budgetview-${SOFT_VERSION}-en.tar.gz ];
then
   scp budgetview-${SOFT_VERSION}-en.tar.gz en_mybudgetview@91.121.123.100:files/app
fi

if [ -a budgetview-${SOFT_VERSION}-fr.tar.gz ];
then
   scp budgetview-${SOFT_VERSION}-fr.tar.gz fr_mybudgetview@91.121.123.100:files/app
fi

if [ -a budgetview-${SOFT_VERSION}-en.zip ];
then
   scp budgetview-${SOFT_VERSION}-en.zip en_mybudgetview@91.121.123.100:files/app
fi

if [ -a budgetview-${SOFT_VERSION}-fr.zip ];
then
   scp budgetview-${SOFT_VERSION}-fr.zip fr_mybudgetview@91.121.123.100:files/app
fi

if [ -a budgetview-${SOFT_VERSION}.deb ];
then
   scp budgetview-${SOFT_VERSION}.deb fr_mybudgetview@91.121.123.100:files/app
   ssh fr_mybudgetview@91.121.123.100 cp files/app/budgetview-${SOFT_VERSION}.deb /tmp
   ssh en_mybudgetview@91.121.123.100 cp /tmp/budgetview-${SOFT_VERSION}.deb files/app/
   ssh fr_mybudgetview@91.121.123.100 rm /tmp/budgetview-${SOFT_VERSION}.deb
fi

if [ -a dev/install/output/BudgetView-${SOFT_VERSION}-en-Setup.exe ];
then
   scp dev/install/output/BudgetView-${SOFT_VERSION}-en-Setup.exe  en_mybudgetview@91.121.123.100:files/app
fi

if [ -a dev/install/output/BudgetView-${SOFT_VERSION}-fr-Setup.exe ];
then
   scp dev/install/output/BudgetView-${SOFT_VERSION}-fr-Setup.exe  fr_mybudgetview@91.121.123.100:files/app
fi

if [ -a BudgetView-${SOFT_VERSION}-en.dmg ];
then
   scp BudgetView-${SOFT_VERSION}-en.dmg en_mybudgetview@91.121.123.100:files/app
fi

if [ -a BudgetView-${SOFT_VERSION}-fr.dmg ];
then
   scp BudgetView-${SOFT_VERSION}-fr.dmg fr_mybudgetview@91.121.123.100:files/app
fi

scp ../picsou/obfuscated/budgetview${JAR_VERSION}.jar ../picsou/ChangeLogOutput-${JAR_VERSION}.txt.bz2 build@91.121.123.100:versions/

if [ -a ../picsou_licence_server/budgetviewLicenceServer.jar ];
then
  scp ../picsou_licence_server/budgetviewLicenceServer.jar build@91.121.123.100:versions/budgetviewLicenceServer${JAR_VERSION}.jar
fi

