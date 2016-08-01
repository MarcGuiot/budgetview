SOFT_VERSION=`java -jar ../picsou/target/obfuscated/budgetview.jar -v -soft | grep "Software version:" |
              sed -e 's/Software version://g' | sed -e 's/  *//g'`

echo "budgetviewVersion="${SOFT_VERSION} > budgetviewVersion.properties

