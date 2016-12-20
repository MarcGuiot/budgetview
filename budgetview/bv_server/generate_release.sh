#!/bin/sh

export JAR_VERSION=2.0

if [ $# != 1 ];
then
  echo "No argument : generating default version" $JAR_VERSION
else
  JAR_VERSION = $1
fi

export BV_SERVER_ROOT=$PWD
export RELEASE_LOCAL_DIR=$BV_SERVER_ROOT/release_local
export RELEASE_PROD_DIR=$BV_SERVER_ROOT/release_prod

# cd $BV_SERVER_ROOT/../../globs
# mvn install -Dmaven.test.skip.exec=true
#
# cd $BV_SERVER_ROOT/../bv_shared
# mvn install -Dmaven.test.skip.exec=true
#
# cd $BV_SERVER_ROOT/../bv_desktop
# mvn install -Dmaven.test.skip.exec=true
#

cd $BV_SERVER_ROOT
mvn install -Dmaven.test.skip.exec=true

export GENERATED_JAR=$BV_SERVER_ROOT/target/bv_server-$JAR_VERSION.jar
if [ ! -e  $GENERATED_JAR ];
then
  echo "Error: file" $GENERATED_JAR "not found"
  exit -1
fi

cd $BV_SERVER_ROOT


echo "Preparing local release..."
mkdir -p $RELEASE_LOCAL_DIR
cp $GENERATED_JAR $RELEASE_LOCAL_DIR/
cp $BV_SERVER_ROOT/dev/config/*_local.properties $RELEASE_LOCAL_DIR
cp $BV_SERVER_ROOT/dev/config/log4j_local.properties $RELEASE_LOCAL_DIR
cp $BV_SERVER_ROOT/dev/scripts/start_*_local_server.sh $RELEASE_LOCAL_DIR
cp $BV_SERVER_ROOT/ssl/keystore $RELEASE_LOCAL_DIR
chmod +x $RELEASE_LOCAL_DIR/*.sh

echo "Preparing prod release..."
mkdir -p $RELEASE_PROD_DIR
cp $GENERATED_JAR $RELEASE_PROD_DIR
cp $BV_SERVER_ROOT/dev/config/bv_*_prod.properties $RELEASE_PROD_DIR
cp $BV_SERVER_ROOT/dev/config/log4j_prod.properties $RELEASE_PROD_DIR
cp $BV_SERVER_ROOT/dev/scripts/start_*_server_prod.sh $RELEASE_PROD_DIR

echo "Done"
