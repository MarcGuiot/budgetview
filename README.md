# budgetview
It is a app to manage personnal finance.

The code behind https://budgetview.fr

It use a a private old version of globsFramework (https://github.com/MarcGuiot/globsframework)

mvn -Pgen-version clean install -DskipTests=true
generate an exe ; under windows do 
jpackage --input bv/ --main-jar budgetview.jar --name budgetview --app-version 6.0 --win-menu  --win-dir-chooser

Pour linux : créé un repertoire gen et version et mettre le jar dans gen 
jpackage --input gen --dest version --main-jar budgetview.jar --name budgetview --app-version 6.0 --type rpm
