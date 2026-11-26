# Build et application de dev

Les commandes de build, de génération des comptes de démo et de test sont dans le `README.md`. Cette note dit
ce qu'il faut savoir en plus. Index des notes : [AGENTS.md](../AGENTS.md).

## JDK et uispec4j

- **JDK 21** : le réacteur est compilé en `--release 21`, et `jpackage` est fourni par le JDK.
- uispec4j, dont dépendent les tests d'IHM, sous-classe `sun.awt.SunToolkit` : son jar installé dans `~/.m2` ne
  marche qu'avec le JDK qui l'a construit. **Changer de JDK = réinstaller uispec4j avec ce JDK**, puis
  `clean install` du réacteur. Il reste compilé en `-source 8 -target 8`, car `--release` interdirait ses accès
  à `sun.awt`.
- Même `-DskipTests` résout les dépendances de test de `globs` et `bv_desktop` : uispec4j doit être installé
  avant le premier build. Ses modules d'exemple (addressbook, calculator) échouent : sans importance.
- Le profil Maven `jdk17` (options `--add-opens` des tests) s'active d'office à partir de Java 9.

## Build

- `mvn clean` efface `target/dist/budgetview.jar`, que `run-dev.sh` lance : fermer l'application de dev avant.
- Les comptes de démo (`src/main/resources/demo/demo-{fr,en}.snapshot`) ne sont pas versionnés : les générer
  une fois, et après tout changement du modèle. Sans eux, le bouton « Compte de démo » et les tests du mode démo
  échouent. Une étape « une fois » comme celle-ci se revérifie à chaque changement de JDK.
- Les balises `Utils.beginRemove()` / `endRemove()` entouraient le code réservé au développement, que le
  shrinker retirait du jar livré. Le shrinker a été supprimé lors du passage à Java 21 : ce code est désormais
  livré. Ne pas en ajouter de nouveau en comptant sur ces balises.
- `bv_server` (hors build) : après `mvn install` du réacteur, `mvn -f budgetview/bv_server/pom.xml compile`
  vérifie qu'il compile.

## Application de dev

- Toujours la lancer par `./run-dev.sh` (`--build` pour reconstruire le jar avant). Il passe
  `-DSINGLE_INSTANCE_DISABLED=true` et son propre dossier de données (`~/.budgetview-dev`, ou `BV_DEV_DATA`), et
  refuse les dossiers de données de l'application installée.
- **Instance unique** : chaque instance de BudgetView écoute sur 127.0.0.1, ports 5454, 3474, 14457 et 9381. Sans
  `-DSINGLE_INSTANCE_DISABLED=true`, si une autre instance tourne, la version de dev s'y connecte et sort
  aussitôt avec le code 0, sans rien afficher.
- **Données** : ne jamais la faire pointer sur de vraies données. Ses triggers d'upgrade migreraient leur
  format, peut-être de façon illisible pour une version plus ancienne. Pour essayer un premier démarrage :
  `BV_DEV_DATA=<dossier vide> ./run-dev.sh`.
