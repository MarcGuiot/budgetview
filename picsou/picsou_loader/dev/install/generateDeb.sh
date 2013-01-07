#!/bin/sh 

VERSION=$1
SOFT_VERSION=$2
echo generate deb for ${VERSION} "  " ${SOFT_VERSION}
sudo rm -rf deb
mkdir deb
cd deb
mkdir budgetview
cd budgetview
mkdir -p ./usr/local/bin
mkdir -p ./usr/share/budgetview
mkdir -p ./usr/share/applications
mkdir -p ./usr/share/pixmaps
mkdir -p ./DEBIAN
cp ../../dev/install/budgetview.sh.deb usr/local/bin/budgetview.sh
cat ../../dev/install/budgetview.desktop  | sed -e "s/SOFT_VERSION/${SOFT_VERSION}/" > usr/share/applications/budgetview.desktop
cp ../../budgetview/budgetviewloader-1.0.jar usr/share/budgetview/
cp ../../budgetview/budgetview${VERSION}.jar usr/share/budgetview/
cp ../../dev/images/budgetview_icon_{16,32,48,128}.png usr/share/pixmaps/
cp ../../budgetview/copyright DEBIAN/copyright
echo "7" > DEBIAN/compat
cat ../../dev/install/control.deb | sed -e "s/SOFT_VERSION/${SOFT_VERSION}/" > DEBIAN/control
find usr -type f  | xargs md5sum > DEBIAN/md5sums
sudo chown -R root:root usr DEBIAN
cd ..
dpkg -b budgetview
cp budgetview.deb ../
mv budgetview.deb budgetview-${SOFT_VERSION}.deb
cd ..
sudo rm -rf deb


