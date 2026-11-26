# BudgetView — notes pour les agents de code et les développeurs

Ce fichier suit la convention [AGENTS.md](https://agents.md) : il réunit ce qu'un agent de code (ou un nouveau
développeur) doit savoir pour travailler sur ce dépôt et qu'on ne devine pas en lisant le code. Le `README.md`
reste la référence pour construire et lancer l'application.

BudgetView est un logiciel de gestion de finances personnelles, client lourd Java/Swing (le code derrière
budgetview.fr), développé en privé de 2008 à 2019 puis publié sous licence Apache 2.0. Son ancien nom de code
est **Picsou** : on le retrouve dans les noms de classes (`PicsouModel`, `PicsouInit`) et de paquets
(`org.designup.picsou`). L'application est dans `budgetview/bv_desktop` ; elle s'appuie sur `globs/`, version
privée et ancienne du [GlobsFramework](https://github.com/MarcGuiot/globsframework).

## Notes par thème

Ce fichier reste court : le détail est dans `agents/`. Lire la note du thème **avant** de travailler dessus.

| Note | À lire avant de… |
|---|---|
| [agents/architecture.md](agents/architecture.md) | chercher où se trouve quelque chose : modules, paquets, dossiers de données, comportement attendu |
| [agents/build.md](agents/build.md) | construire, lancer l'application de dev, changer de JDK, toucher aux `pom` |
| [agents/tests.md](agents/tests.md) | écrire, lancer ou corriger un test |

Une nouvelle note par thème plutôt qu'un ajout ici : ce fichier ne garde que l'index, les règles qui évitent
de casser quelque chose, et les conventions qui valent partout. Toute note ajoutée entre dans le tableau
ci-dessus.

## Règles à ne jamais oublier

- Lancer l'application de dev par `./run-dev.sh`, jamais sur de vraies données : ses triggers d'upgrade
  migreraient leur format, peut-être de façon illisible pour une version plus ancienne.
- Sorties de l'application : toujours `AppExit.exit(code)`, jamais `System.exit`.
- Jamais deux `mvn test` en même temps : ils écrivent dans le même `target/`.
- Tout texte visible passe par `i18n/lang_fr.properties` **et** `lang_en.properties`, tenus à jour ensemble.

## Conventions

- Modèle globs : chaque type métier est une classe à champs `public static` (`TYPE`, `ID`…) initialisés par
  réflexion ; ne pas l'instancier.
- IHM décrite en `.splits` (`src/main/resources/layout/`) et liée au code par nom de composant.
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
- Le serveur `register.budgetview.fr` ne répondait plus en novembre 2026 : vérification de version, licences,
  cloud et synchronisation bancaire échouent.
- Les `budgetview/howto/` (livraison, serveur) décrivent l'infrastructure d'origine (adresses, comptes) :
  documentation historique, pas à rejouer telle quelle.
