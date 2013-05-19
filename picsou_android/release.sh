#!/bin/sh

./update_libs.sh
ant release

SIGNED_TARGET=picsou_android-release-signed.apk
cp ./bin/picsou_android-release-unsigned.apk $SIGNED_TARGET
jarsigner -verbose -sigalg MD5withRSA -digestalg SHA1 -keystore bv-android.keystore $SIGNED_TARGET BudgetView

FINAL_TARGET=./bin/BudgetView.apk
$ANDROID_HOME/tools/zipalign -f -v 4 $SIGNED_TARGET $FINAL_TARGET

echo "Release package available: " $FINAL_TARGET

