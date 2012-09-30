cp ~/.m2/repository/org/saxstack/2.0/saxstack-2.0.jar ./libs
echo "==> saxstack jar copied to libs"

cd ../globs/
mvn install -Dmaven.test.skip=true
cd ../picsou_android/
cp ~/.m2/repository/org/globsframework/globs/1.0/globs-1.0.jar ./libs 
echo "==> globs jar copied to libs"

cd ../picsou/picsou_shared/
mvn install
cd ../../picsou_android/
cp ~/.m2/repository/com/budgetview/picsouShared/1.0/picsouShared-1.0.jar ./libs/
echo "==> picsouShared jar copied to libs"

