# Lot 1F — Cockpit React

Ajout du dossier `frontend` au projet 1E sans modification des migrations Liquibase existantes.

- Dashboard holographique, timeline, progression par phase.
- Pages projets, workflows, artefacts et validations.
- Flux de création de projet/workflow, démarrage, simulation dev, validation et refus UX.
- API session/CSRF en lecture ajoutée au backend (`/api/v1/auth/me`, `/api/v1/auth/csrf`) pour le frontend session-based.
- Le frontend utilise les vrais contrats API 1E, pas des données fictives.
- Sécurité: aucune clé API ni secret embarqué dans le navigateur.

Validation: vérification statique des fichiers et du ZIP. Compilation Java 25 et npm install/build non exécutables ici (JDK 21 et npm registry inaccessible). À exécuter localement avant tout déploiement.
