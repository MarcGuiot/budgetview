echo "Mise a jour des jars..."
./update_libs.sh

echo "\nFabrication du package d'install..."
ant debug

echo "\nInstallation sur le mobile..."
adb install -r bin/picsou_android-debug.apk
