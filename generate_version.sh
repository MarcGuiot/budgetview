#!/bin/sh

cd globs
mvn install -Dmaven.test.skip.exec=true

cd ../uispec4j
mvn install -Dmaven.test.skip.exec=true

cd ../picsou
mvn install -Dmaven.test.skip.exec=true -am -pl picsou -Pgen-demo
mvn install -Dmaven.test.skip.exec=true -Pgen-version

./copy_generated_files.sh
