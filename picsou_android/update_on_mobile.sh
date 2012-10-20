echo "Fabrication du package d'install..."
ant clean debug

echo "Installation sur le mobile..."
adb install -r bin/picsou_android-debug.apk
