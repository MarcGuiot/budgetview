cp ~/.m2/repository/org/saxstack/2.0/saxstack-2.0.jar ./app/libs
echo "==> saxstack jar copied to libs"

cd ../globs/
mvn clean install -Dmaven.test.skip=true
cd -
cp ../globs/target/globs-core-1.0.jar ./app/libs 
echo "==> globs jar copied to libs"

cd ../picsou/picsou_shared/
mvn install
cd ../../budgetview-android/
cp ~/.m2/repository/com/budgetview/picsouShared/1.0/picsouShared-1.0.jar ./app/libs/
echo "==> picsouShared jar copied to libs"

