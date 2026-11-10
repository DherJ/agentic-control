# Agent Workspace — Lot 1, incrément 1A

Socle Java 25 / Spring Boot 4.0.0, PostgreSQL 17 et Liquibase, issu du premier ZIP v1.1.

## Démarrage local (pré-requis)
- JDK 25 et Maven 3.9+ ; PostgreSQL 17 avec une base `agent_workspace`.
- Créer un utilisateur PostgreSQL autorisé à créer le schéma `agent_workspace` lors du premier lancement.
- Définir `DB_URL`, `DB_USER`, `DB_PASSWORD`.
- Pour créer le premier administrateur, définir **une seule fois** `APP_BOOTSTRAP_ADMIN_USERNAME` et `APP_BOOTSTRAP_ADMIN_PASSWORD` (16 caractères minimum), puis retirer le secret de l'environnement.
- `cd backend && mvn test && mvn spring-boot:run`.

L'authentification Spring Security utilise le formulaire Spring par défaut et une session serveur. CSRF reste actif.

## Limitations
Cet incrément n'est pas prêt pour la production : il n'a pas encore de cockpit React, d'API de gestion des workflows, de contrôle d'accès par projet, de workflow BMAD exécutable, ni de Docker Compose. Ne pas déployer sur le VPS.

Consulter `docs/STATUS.md` et `docs/ADR-011-ux-mockup-approval.md`.

## Incrément 1B — Registre de workflows BMAD

- Workflow YAML versionné `bmad-standard:1.0.0`, avec UX Designer (maximum 3 maquettes) et validation UX obligatoire avant validation fonctionnelle.
- Import au démarrage dans `workflow_definition` et `step_definition` (Liquibase 003).
- Immutabilité par checksum SHA-256 du fichier source ; redémarrage idempotent ; changement d'une version existante = erreur au démarrage.
- API authentifiée : `GET /api/v1/workflow-definitions`, `GET /api/v1/workflow-definitions/{id}`, `GET /api/v1/workflow-definitions/{id}/steps`.
- Les définitions sont chargées après les migrations ; les endpoints ne permettent pas leur édition.
- Tests unitaires de validation YAML dans `DefinitionValidatorTest`.

### Exécution locale

Prérequis : Java 25, Maven, PostgreSQL. Configurer `DB_PASSWORD` et, lors de la création initiale, `APP_BOOTSTRAP_ADMIN_USERNAME` / `APP_BOOTSTRAP_ADMIN_PASSWORD`.

```bash
cd backend
mvn clean verify
mvn spring-boot:run
```

Le backend 1B n'exécute encore aucun workflow ni agent. Cela relève des incréments 1C à 1E.

## Lot 1C — Moteur persistant

Voir `docs/STATUS-1C.md` pour le périmètre et les endpoints. Cette livraison n'exécute pas encore d'agent : après démarrage, la première étape `project-brief` est `READY`. L'approbation humaine et les artefacts arrivent en 1D/1E.

## Increment 1D
Artifact and approval entities, REST read endpoints, approval decision service, and Liquibase 005. See docs/STATUS-1D.md for important integration limitations.


## Incrément 1E

Voir `docs/STATUS-1E.md`. Avec `SPRING_PROFILES_ACTIVE=dev`, après démarrage du workflow, simuler la complétion d'une étape `READY` :

`POST /api/v1/dev/workflows/{workflowId}/steps/{stepId}/complete`

Corps : `{"content":{"summary":"Livrable simulé"}}`. Pour `UX_MOCKUP_SET`, fournir `{"content":{"proposals":[{"id":"a"},{"id":"b"}]}}`.

Les étapes APPROVAL génèrent automatiquement une approbation `PENDING`. Décider avec `POST /api/v1/approvals/{id}/decision` et le `artifactId` et `approvalVersion` renvoyés par l'API. Les sessions et CSRF Spring Security restent actifs.

## Incrément 1F — Cockpit React

Le dossier `frontend/` contient le cockpit React/TypeScript/Vite inspiré des directions visuelles « dashboard holographique + timeline immersive ». Voir `frontend/README.md` pour les commandes et les limites. Le backend ajoute `SessionController` pour exposer la session et le jeton CSRF aux formulaires React. Aucune migration n'a été modifiée dans 1F.

## Incrément 1G — Docker et intégration (candidat de recette)

Voir `docs/RUNBOOK-1G.md` et `docs/STATUS-1G.md`. Pour tester localement avec Docker et PostgreSQL 17 :

```powershell
cd infra
Copy-Item .env.example .env
# Modifier .env avec des secrets forts, puis :
docker compose --profile local up --build -d
```

Interface : http://localhost:8088/agent-workspace/ . Le profil `dev` est réservé au poste local pour simuler les agents. Ne jamais l'exposer sur Internet.

Le mode production n'est **pas déployé** : le reverse proxy OVH existant doit être inspecté et relié au réseau Docker `web` avant utilisation. Le fichier `infra/nginx/ovh-existing-proxy.example.conf` est uniquement une référence.

## Incrément 1H.1 — FinOps IA (préversion)

- Migration Liquibase `007-ai-finops.sql` : budgets par projet, historique des appels IA et réservations.
- Nouvelle navigation **Coûts IA** : sélection du projet, jauge de budget, alerte à 80 %, paramètres ADMIN, top 5, recherche et filtres par modèle/statut.
- Le backend réserve **avant** l'appel OpenAI un coût conservateur fondé sur la taille du prompt et le plafond de sortie, et refuse la demande en HTTP 409 si le plafond par demande ou le budget disponible serait dépassé.
- Après réponse, l'historique enregistre les tokens rapportés par l'API Responses (`usage.input_tokens`, `usage.output_tokens`, `usage.input_tokens_details.cached_tokens`), le modèle, les tarifs configurés et le coût estimé.
- Les erreurs API et les réponses invalides sont historisées. Les erreurs réseau sans usage rapporté sont enregistrées avec 0 token : elles peuvent néanmoins avoir été facturées par le fournisseur.
- Tarifs à définir dans `infra/.env` : `OPENAI_PRICE_INPUT_USD_PER_MILLION`, `OPENAI_PRICE_OUTPUT_USD_PER_MILLION` (en USD). **Les appels sont bloqués si les tarifs sont absents.** Ne pas reprendre un prix d'un autre modèle.
- Pour les appels avec cache, la version 1H.1 affiche les tokens cachés mais les facture au prix d'entrée standard (estimation conservatrice). Une future évolution devra ajouter les prix du cache et les outils facturés séparément.
- Le plafond est une **préautorisation estimée**, pas une limite absolue garantie par le fournisseur. Les tokens de raisonnement sont compris dans les tokens de sortie rapportés. Des écarts peuvent survenir (overhead, réseau, tarification, cache).
- Les budgets et dépenses sont cumulatifs sur toute la vie du projet (pas de remise à zéro mensuelle).
- Le top 5 et les compteurs de tokens respectent les filtres ; le budget global reste celui du projet entier. L'historique est limité aux 500 lignes les plus récentes correspondant aux filtres.
- L'accès GET est ouvert aux utilisateurs authentifiés autorisés à consulter le projet ; les modifications PUT sont réservées à ADMIN via Spring Security.

### Validation avant utilisation réelle

1. Renseigner les tarifs exacts du modèle dans `infra/.env` et redémarrer le backend.
2. Exécuter `mvn clean verify` dans `backend` et `npm install && npm run build` dans `frontend`.
3. Démarrer PostgreSQL et vérifier la migration Liquibase `007`.
4. Créer un projet et contrôler le budget par défaut (10 USD, 2 USD par demande, alerte à 80 %).
5. Exécuter l'Analyste, comparer les tokens et le coût avec le tableau de bord OpenAI.
6. Tester le blocage à budget insuffisant, l'accès INVITE en lecture seule et les filtres.
7. Tester un échec d'appel, puis vérifier la libération de la réservation.

**Limite connue :** une interruption brutale du processus pendant un appel peut laisser une réservation `RESERVED` à réconcilier manuellement ; un job de récupération et des tests de concurrence seront nécessaires avant la production.

## Incrément 1H.2 — FinOps global mensuel

Migration `008-global-monthly-finops.sql` : singleton `ai_billing_settings`, plafond mensuel global par défaut de 50 USD, alerte à 80 %, blocage activé.

- `GET /api/v1/finops/monthly-summary` : total du mois en cours (Europe/Paris) et ventilation par projet.
- `GET /api/v1/finops/settings` : paramètres globaux.
- `PUT /api/v1/finops/settings` : ADMIN seulement.
- Écran **Paramètres FinOps** réservé aux ADMIN, dans la navigation.
- La réservation verrouille d'abord le singleton global, puis le budget projet pour sérialiser les appels concurrents.
- Les réservations sont comptées dans la période de leur création. Les appels qui traversent minuit à la fin du mois nécessiteront une stratégie de rattachement comptable plus sophistiquée si ce cas devient fréquent.
- Les budgets sont des garde-fous estimatifs, pas une limite contractuelle du fournisseur. Le mode `enforceLimit=false` est réservé à l'administration et autorise explicitement les dépassements.
- Tarifs du modèle et frais d'outils restent configurés séparément.
