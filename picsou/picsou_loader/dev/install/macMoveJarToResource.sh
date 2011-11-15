#/bin/sh

rm -f ../picsou/budgetview.jar
VERSION=`java -jar ../picsou/obfuscated/budgetview.jar -v -jar`
JAR_VERSION=`echo $VERSION | grep "Jar version:" | sed -e 's/Jar version://g' | sed -e 's/  *//g'`
cp ../picsou/obfuscated/budgetview.jar BudgetView-fr/BudgetView.app/Contents/Resources/budgetview${JAR_VERSION}.jar
cp ../picsou/obfuscated/budgetview.jar BudgetView-en/BudgetView.app/Contents/Resources/budgetview${JAR_VERSION}.jar
