#!/bin/sh

export JAR_VERSION=2.2

if [ $# != 1 ];
then
  echo "No args: generating default version" $JAR_VERSION
else
  JAR_VERSION = $1
fi

export BV_SERVER_ROOT=$PWD
export RELEASE_LOCAL_DIR=$BV_SERVER_ROOT/release_local
export RELEASE_PROD_DIR=$BV_SERVER_ROOT/release_prod

cd $BV_SERVER_ROOT/../../globs
mvn clean install -Dmaven.test.skip.exec=true

cd $BV_SERVER_ROOT/../bv_shared
rm target/*.jar
mvn clean install -Dmaven.test.skip.exec=true

cd $BV_SERVER_ROOT/../bv_desktop
rm target/*.jar
mvn clean install -Dmaven.test.skip.exec=true

cd $BV_SERVER_ROOT
mvn clean install -Dmaven.test.skip.exec=true

export GENERATED_JAR=$BV_SERVER_ROOT/target/bv_server-$JAR_VERSION.jar
if [ ! -e  $GENERATED_JAR ];
then
  echo "Error: file" $GENERATED_JAR "not found"
  exit -1
fi

cd $BV_SERVER_ROOT

echo "Preparing local release..."
rm -r $RELEASE_LOCAL_DIR
mkdir -p $RELEASE_LOCAL_DIR
  cp $BV_SERVER_ROOT/ssl/keystore $RELEASE_LOCAL_DIR
  cp $BV_SERVER_ROOT/server_admin/scripts/local/*.sh $RELEASE_LOCAL_DIR
  chmod +x $RELEASE_LOCAL_DIR/*.sh
mkdir -p $RELEASE_LOCAL_DIR/jars
  cp $GENERATED_JAR $RELEASE_LOCAL_DIR/jars
mkdir -p $RELEASE_LOCAL_DIR/config
  cp $BV_SERVER_ROOT/server_admin/config/*_local.properties $RELEASE_LOCAL_DIR/config
  cp $BV_SERVER_ROOT/server_admin/config/log4j_local.properties $RELEASE_LOCAL_DIR/config

echo "Preparing prod release..."
rm -r $RELEASE_PROD_DIR
mkdir -p $RELEASE_PROD_DIR
  cp $BV_SERVER_ROOT/server_admin/scripts/*.sh $RELEASE_PROD_DIR
  chmod +x $RELEASE_PROD_DIR/*.sh
mkdir -p $RELEASE_PROD_DIR/jars
  cp $GENERATED_JAR $RELEASE_PROD_DIR/jars
mkdir -p $RELEASE_PROD_DIR/config
  cp $BV_SERVER_ROOT/server_admin/config/bv_*_prod.properties $RELEASE_PROD_DIR/config
  cp $BV_SERVER_ROOT/server_admin/config/log4j_*_prod.properties $RELEASE_PROD_DIR/config
  cp $BV_SERVER_ROOT/server_admin/config/logging.properties $RELEASE_PROD_DIR/config

echo "Done"
