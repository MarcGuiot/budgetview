#/bin/sh

rm -f ../picsou/budgetview.jar
VERSION=`java -jar ../picsou/target/obfuscated/budgetview.jar -v -jar`
JAR_VERSION=`echo $VERSION | grep "Jar version:" | sed -e 's/Jar version://g' | sed -e 's/  *//g'`
cp ../picsou/target/obfuscated/budgetview.jar BudgetView-fr/BudgetView.app/Contents/Resources/budgetview${JAR_VERSION}.jar
cp ../picsou/target/obfuscated/budgetview.jar BudgetView-en/BudgetView.app/Contents/Resources/budgetview${JAR_VERSION}.jar
cp ../picsou/target/obfuscated/budgetview.jar BudgetView-InMemory-fr/BudgetView.app/Contents/Resources/budgetview${JAR_VERSION}.jar
