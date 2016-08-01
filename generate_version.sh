#!/bin/sh

if [ $# != 1 ];
then
  echo "Error: missing jar version number"
  exit -1
fi

export BV_ROOT=$PWD
export RELEASE_DIR=$BV_ROOT/releases
export BV_INSTALL_DIR=$BV_ROOT/budgetview/bv_desktop/dev/install
export BV_WIN_INSTALL_DIR=$BV_ROOT/budgetview/bv_desktop/dev/install/windows
export GENERATED_JAR=$BV_ROOT/budgetview/bv_desktop/target/obfuscated/budgetview$1.jar

cd $BV_ROOT/globs
mvn install -Dmaven.test.skip.exec=true

cd $BV_ROOT/uispec4j
mvn install -Dmaven.test.skip.exec=true

cd $BV_ROOT/budgetview
mvn install -Dmaven.test.skip.exec=true -am -pl picsou -Pgen-demo
mvn install -Dmaven.test.skip.exec=true -Pgen-version

if [ ! -e  $GENERATED_JAR ];
then
  echo "Error: file" $GENERATED_JAR "not found"
  exit -1
fi

cd $BV_ROOT/
cp $GENERATED_JAR $RELEASE_DIR
cp $GENERATED_JAR $RELEASE_DIR/budgetview_bundle.jar
mv $BV_ROOT/budgetview/bv_desktop/ChangeLogOutput*.txt.bz2 $RELEASE_DIR

### Prepare Mac & Linux installer
cp $RELEASE_DIR/budgetview_bundle.jar $BV_INSTALL_DIR

### Prepare Windows installer
cp $RELEASE_DIR/budgetview_bundle.jar $BV_WIN_INSTALL_DIR
cd $BV_WIN_INSTALL_DIR
zip bv_windows_install_files$1.zip *
mv $BV_WIN_INSTALL_DIR/bv_windows_install_files$1.zip $RELEASE_DIR

cd $BV_ROOT

echo "Done"
echo "Installer for Mac & Linux ready to be prepared: " $BV_INSTALL_DIR
echo "Installer for Windows ready to be prepared:     " $BV_WIN_INSTALL_DIR
