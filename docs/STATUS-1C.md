# Lot 1C — Exécution persistée (première tranche)

## Livré
- Création de projets appartenant à l'utilisateur connecté (ADMIN peut consulter tous les projets).
- Création d'instances de workflow à partir d'une définition publiée (clé + version), version de définition figée.
- Initialisation persistée des étapes et de leur itération 1.
- Démarrage, activation des étapes dont les dépendances sont satisfaites, annulation, consultation des étapes et du journal.
- Exécution HANDOFF automatique uniquement lorsque ses dépendances sont terminées.
- Migration Liquibase additive 004 ; pas de modification des migrations 001–003.
- Tests unitaires des états d'étapes.

## Frontière intentionnelle 1C
- Aucun agent IA ni simulation de résultat : les étapes AGENT passent à READY et y restent.
- Aucune décision d'approbation : aucune approbation n'est automatiquement accordée.
- Pas encore de rejeu, de boucle de refus, de réconciliation d'artefacts, de retry d'agent ni de mécanisme de dispatch asynchrone.
- Autorisation projet : propriétaire ou ADMIN seulement ; prise en charge des project_member à compléter.
- Les transitions de complétion d'agents et approbations arriveront avec 1D/1E.

## Commandes locales
`cd backend && mvn clean verify`

Configurer PostgreSQL et l'admin local avant `mvn spring-boot:run`.
Les POST nécessitent la session authentifiée et le jeton CSRF Spring Security.

## Contrats REST ajoutés
- POST /api/v1/projects ; GET /api/v1/projects
- POST /api/v1/workflows ; GET /api/v1/workflows ; GET /api/v1/workflows/{id}
- POST /api/v1/workflows/{id}/start ; POST /api/v1/workflows/{id}/cancel
- GET /api/v1/workflows/{id}/steps ; GET /api/v1/workflows/{id}/events

Le POST /workflows prend `projectId`, `workflowDefinitionKey`, `workflowDefinitionVersion`, `title`, `rawRequest`.

## Limites de validation
Aucune compilation Java 25/Maven ni test PostgreSQL n'a été exécuté dans cet environnement ; à vérifier sur le poste du développeur.
