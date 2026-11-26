# BudgetView — notes pour les agents de code et les développeurs

Ce fichier suit la convention [AGENTS.md](https://agents.md) : il réunit ce qu'un agent de code (ou un nouveau
développeur) doit savoir pour travailler sur ce dépôt et qu'on ne devine pas en lisant le code. Le `README.md`
reste la référence pour construire et lancer l'application.

BudgetView est un logiciel de gestion de finances personnelles, client lourd Java/Swing (le code derrière
budgetview.fr), développé en privé de 2008 à 2019 puis publié sous licence Apache 2.0. Son ancien nom de code
est **Picsou** : on le retrouve dans les noms de classes (`PicsouModel`, `PicsouInit`) et de paquets
(`org.designup.picsou`). Le projet a d'abord été versionné sous Mercurial (`.hgignore`, `howto/livraison.txt`).

## Modules

Réacteur Maven racine `pom.xml` :

| Module | Rôle | Dans le build ? |
|---|---|---|
| `globs/` | GlobsFramework, **version privée et ancienne** du [framework](https://github.com/MarcGuiot/globsframework) : modèle de données réactif (`GlobType`, champs statiques annotés `@Key`, `@Target`…), repository, changesets. Contient aussi `org.globsframework.gui.splits`, le moteur de mise en page Swing des fichiers `.splits` | oui |
| `saxstack/` | Parseur SAX à pile et écriture XML (`org.saxstack`) | oui |
| `prevayler/` | Prevayler (persistance par journal de transactions + snapshots), forké | oui |
| `siteweaver/` | Générateur de l'ancien site web | oui |
| `budgetview/bv_shared` | Code commun client/serveur | oui |
| `budgetview/bv_desktop` | **L'application** (~1 100 classes, ~350 classes de test) | oui |
| `budgetview/bv_server` | Serveur (licences, cloud, mobile ; Jetty, MySQL, Stripe) | **commenté** |
| `budgetview/bv_analytics` | Analyse de logs | **commenté** |
| `budgetview/bv_website` | Contenu du site (fr/en), voir « Comportement attendu » | hors Maven |
| `uispec4j/` | Framework de tests d'IHM Swing (fork), à installer à part (voir README) | **commenté** |
| `splits/` | Ancienne copie du moteur `splits`, doublon de celui de `globs` | hors Maven |
| `budgetview-android/` | Application mobile (Gradle, à part) | hors Maven |
| `functests/` | Ancien projet de tests IntelliJ | hors Maven |

## Application desktop (`budgetview/bv_desktop`)

- Point d'entrée : `com.budgetview.desktop.DesktopApp` → `Application.main`. Version :
  `Application.APPLICATION_VERSION` (6.00), `JAR_VERSION` (156, celui de la 5.01, dernière version distribuée).
- Variantes pour le développement : `InMemoryDesktopApp` (données en mémoire),
  `src/test/java/com/budgetview/desktop/DevDesktopApp.java`.
- Données utilisateur : `~/.budgetview` (Linux, Windows), `~/Library/Application Support/BudgetView` (macOS),
  voir `desktop/startup/AppPaths.java`. Un fichier `redirect.txt` dans ce dossier peut désigner un autre
  emplacement (« Préférences → Stockage »). Surcharges par propriétés système préfixées `budgetview.`
  (`budgetview.data.path`, `budgetview.prevayler.path`…).
- Un dossier de données peut contenir **plusieurs utilisateurs**, chacun avec ses données chiffrées par son mot
  de passe ; un compte créé sans mot de passe s'appelle `autologUser`.
- Paquets principaux sous `com.budgetview` :
  - `model/` : les `GlobType` métier (Account, Transaction, Series, SeriesBudget, Project, Month…) ;
  - `desktop/` : l'IHM, un paquet par écran ou fonction (budget, categorization, accounts, projects,
    analysis, dashboard…) ;
  - `desktop/upgrade` : migration des données d'une version à l'autre (`UpgradeTrigger`, `ConfigUpgradeTrigger`) ;
  - `triggers/` : logique réactive recalculée à chaque changement du repository globs ;
  - `io/importer` (OFX, QIF, CSV, JSON) et `io/exporter` ;
  - `persistence/` : Prevayler, et `direct/` (dont `Dump`, outil de lecture d'un dossier de données) ;
  - `bank/`, `budgea/`, `io/budgea` : synchronisation bancaire par l'agrégateur Budgea ;
  - `client/`, `mobile/`, `session/` : dialogue avec le serveur.
- Ressources (`src/main/resources`) : écrans en `layout/**/*.splits`, textes en `i18n/lang_fr.properties` et
  `lang_en.properties`, banques par pays dans `banks/`, thèmes de couleurs dans `colors/`.
- Journal de l'application : `<dossier de données>/logs/log.txt`. L'application y redirige `System.out`.
- Le serveur `register.budgetview.fr` ne répondait plus en novembre 2026 : vérification de version, licences,
  cloud et synchronisation bancaire échouent. Depuis la 6.00, toutes les extensions sont activées au démarrage,
  sans licence.

### Comportement attendu

`budgetview/bv_website` est la meilleure description du comportement voulu : aide utilisateur dans
`fr/content/support/` (`faq/`, `guides/`) et journal des versions dans `support/derniers_changements.html`
(5.0 du 2017-10-29 : synchronisation Budgea ; 5.01 du 2017-11-12 : jar 156). Le consulter avant de trancher
entre le code et un test qui le contredit.

## Build et tests

Commandes de build, de génération des comptes de démo et de test : voir le `README.md`. À savoir en plus :

- **JDK 21**, et uispec4j réinstallé à chaque changement de JDK : il sous-classe `sun.awt.SunToolkit`, son jar
  ne marche qu'avec le JDK qui l'a construit. Il reste compilé en `-source 8 -target 8`, car `--release`
  interdirait ses accès à `sun.awt`. Le profil Maven `jdk17` (options `--add-opens` des tests) s'active d'office
  à partir de Java 9.
- `mvn clean` efface `target/dist/budgetview.jar`, que `run-dev.sh` lance : fermer l'application de dev avant.
- Les comptes de démo (`src/main/resources/demo/demo-{fr,en}.snapshot`) ne sont pas versionnés : les générer
  une fois, et après tout changement du modèle. Sans eux, le bouton « Compte de démo » et les tests du mode démo
  échouent.
- Toujours lancer l'application de dev par `./run-dev.sh` : il passe `-DSINGLE_INSTANCE_DISABLED=true` et son
  propre dossier de données. Sans cela, si une autre instance de BudgetView tourne (elle écoute sur 127.0.0.1,
  ports 5454, 3474, 14457 et 9381), la version de dev s'y connecte et sort aussitôt, sans rien afficher. Et ne
  jamais la faire pointer sur de vraies données : ses triggers d'upgrade migreraient leur format, peut-être de
  façon illisible pour une version plus ancienne.
- Les balises `Utils.beginRemove()` / `endRemove()` entouraient le code réservé au développement, que le
  shrinker retirait du jar livré. Le shrinker a été supprimé lors du passage à Java 21 : ce code est désormais
  livré. Ne pas en ajouter de nouveau en comptant sur ces balises.

### Tests (`bv_desktop`)

- JUnit 3 (`TestCase`). Surefire prend `**/*Test.java` et `**/*Tests.java`. Pour désactiver un test, renommer
  la méthode `disabled_test…` avec un commentaire qui donne la raison (`@Ignore` est sans effet) ; exclure la
  classe dans le `pom` s'il n'y reste plus aucun test.
- Les tests fonctionnels (`src/test/java/com/budgetview/functests`, classes de base `FunctionalTestCase` →
  `LoggedInFunctionalTestCase`) pilotent l'IHM par uispec4j.
- Réglages de surefire et leurs raisons : `-Xmx1024m` (à 256 Mo, `OutOfMemoryError`) ; `reuseForks=false`, une
  JVM par classe, car les tests fonctionnels laissent fuir fenêtres et threads et une JVM partagée finit par
  mourir ; `-Dapple.awt.UIElement=true`, sans quoi chaque JVM de test vole le focus sur macOS.
- Sur un portable, empêcher la mise en veille pendant la suite (`caffeinate -i` sur macOS) : une JVM gelée par
  la veille fait échouer les tests d'IHM par délai dépassé. Rien d'autre de lourd ne doit tourner à côté, pour
  la même raison, et jamais deux `mvn test` en même temps (même `target/`).
- Diagnostic : rapports dans `target/surefire-reports/*.txt`, pile complète avec `-DtrimStackTrace=false`.
  `OperationChecker.checkDataIsOk` affiche le détail des incohérences de données trouvées.

Pièges des tests fonctionnels :

- **Fenêtre partagée** : `LoggedInFunctionalTestCase` garde la fenêtre principale d'un test à l'autre. Un test
  qui passe seul mais échoue dans sa classe vient presque toujours d'un réglage d'IHM laissé modifié par un test
  précédent : tout test qui change un tel réglage le rétablit avant de finir.
- **Langue** : la 6.00 force le français au démarrage (`Application.LANG_PROPERTY`, `budgetview.lang` = `fr`,
  `en` ou `keep`). `FunctionalTestCase` met `keep`, pour que les tests restent dans la langue qu'ils fixent
  (anglais).
- **Sorties** : toujours `AppExit.exit(code)`, jamais `System.exit`. `FunctionalTestCase` remplace la sortie
  par une exception, pour que la JVM de surefire survive.
- **Licence** : toutes les extensions sont actives ; `LicenseChecker.register()` ne fait plus rien et le menu
  « Activer les extensions… » n'existe plus.
- Un test qui se déconnecte puis se reconnecte doit travailler sur disque (`setInMemory(false)` dans son
  `setUp`, voir `PreferencesTest`) : la déconnexion recrée la session, ce qui vide une base en mémoire.
- Un test unitaire sur un repository globs construit à la main doit y charger les constantes du modèle, comme
  `PicsouInit` (voir `DataCheckingServiceTest`), sinon le contrôle des liens signale de faux liens cassés.

### Autres modules

- `globs` : `mvn clean test -pl globs`. Son `pom` fixe une JVM par classe, une limite de temps et les
  `--add-opens`. `clean` après un changement de signature ; `mvn install -pl globs -DskipTests` avant de tester
  `bv_desktop`, qui prend `globs` dans `~/.m2`.
- `bv_server` (hors build) : après `mvn install` du réacteur, `mvn -f budgetview/bv_server/pom.xml compile`.

## Conventions

- Modèle globs : chaque type métier est une classe à champs `public static` (`TYPE`, `ID`…) initialisés par
  réflexion ; ne pas l'instancier.
- IHM décrite en `.splits` et liée au code par nom de composant. Tout texte visible passe par les fichiers
  `lang_*.properties`, à tenir à jour ensemble en français et en anglais.
- Checklist d'une nouvelle fonctionnalité : `budgetview/howto/checklist_nouvelle_feature.txt` (upgrade des
  données utilisateur, couleurs pour tous les thèmes, traduction anglaise, focus…).
- `@NoObfuscation` : vestige de l'obfuscation Zelix (ZKM), qui n'est plus utilisée.
- Fichiers d'IDE (`.idea`, `.iml`, `.project`, `.classpath`, `.vscode`…) : ignorés, le `pom.xml` fait foi.

## Points d'attention

- Des clés privées du serveur ont figuré dans `ssl/` : retirées de l'arbre, elles restent dans l'historique git
  public. Elles sont compromises, ne jamais les réutiliser. Le `.gitignore` bloque `*.key`, `*.jks`,
  `*.keystore`, `*.p12`, `*.pem`.
- La clé privée DSA qui signait les licences est écrite en dur dans
  `bv_server/.../license/generator/LicenseGenerator.java`, public : elle ne protège plus rien.
- Les `budgetview/howto/` (livraison, serveur) décrivent l'infrastructure d'origine (adresses, comptes) :
  documentation historique, pas à rejouer telle quelle.
