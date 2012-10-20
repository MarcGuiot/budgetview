echo "\nFabrication du package d'install..."
ant debug

echo "\nInstallation sur le mobile..."
adb install -r bin/picsou_android-debug.apk
