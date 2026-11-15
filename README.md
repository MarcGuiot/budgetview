# budgetview
It is a app to manage personnal finance.

The code behind https://budgetview.fr

It use a a private old version of globsFramework (https://github.com/MarcGuiot/globsframework)

mvn -Pgen-version clean install -DskipTests=true
Le livrable est dans budgetview/bv_desktop/target/dist (budgetview.jar + lib/ + config.jar).

generate an exe ; under windows do 
jpackage --input budgetview/bv_desktop/target/dist --main-jar budgetview.jar --name budgetview --app-version 6.0 --win-menu  --win-dir-chooser

Pour linux :
jpackage --input budgetview/bv_desktop/target/dist --dest version --main-jar budgetview.jar --name budgetview --app-version 6.0 --type rpm

## Développement

Il faut un **JDK 8** : uispec4j, dont dépendent les tests d'IHM, sous-classe `sun.awt.SunToolkit`.

```sh
# Une fois : installer uispec4j dans ~/.m2. Il est hors du réacteur racine, mais
# même -DskipTests résout les dépendances de test de globs et bv_desktop.
# Les modules d'exemple addressbook/calculator échouent : sans importance.
mvn -f uispec4j/pom.xml install -DskipTests

# Build complet
mvn clean install -DskipTests

# Une fois, et après tout changement du modèle : comptes de démo
# (src/main/resources/demo/demo-{fr,en}.snapshot, non versionnés).
# Sans eux, le bouton « Compte de démo » et les tests du mode démo échouent.
mvn -Pgen-demo install -pl budgetview/bv_desktop -DskipTests

# Tests
mvn test -pl globs
mvn test -pl budgetview/bv_desktop                    # suite complète, une JVM par classe
mvn test -pl budgetview/bv_desktop -Dtest=LoginTest   # une classe
```

`./run-dev.sh` lance l'application de dev (`--build` pour reconstruire le jar avant) avec ses propres
données (`~/.budgetview-dev`, ou `BV_DEV_DATA`) et `-DSINGLE_INSTANCE_DISABLED=true`, pour qu'elle
cohabite avec une version installée sans toucher à ses données.

Tests désactivés : JUnit 3, donc la méthode est renommée `disabled_test…`, avec un commentaire
qui donne la raison.

## Licence

BudgetView est distribué sous la [licence Apache 2.0](LICENSE).
Copyright 2008-2019 Marc Guiot et Régis Medina (BudgetView SARL) — voir [NOTICE](NOTICE).
