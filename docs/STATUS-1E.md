# Incrément 1E — Simulation et décisions

- Simulation d’une étape AGENT `READY` en dev/test uniquement.
- Validation de 2 à 3 propositions UX (identifiants distincts).
- Création automatique des demandes d’approbation pour les étapes APPROVAL.
- Décision humaine transactionnelle, reprise du workflow et invalidation transitive après refus.
- Artefacts versionnés et conservés ; les décisions historiques restent consultables.

## Limitations / vérifications requises

- Le simulateur accepte du JSON métier simple ; les schémas JSON détaillés de chaque type d’artefact restent à renforcer.
- Le moteur fonctionne sur des dépendances simples `requires` (une seule dépendance par étape).
- Aucun runtime IA réel ni intégration GitHub.
- Compiler et exécuter les tests sous Java 25 / Maven sur la machine du développeur.
- Ne pas utiliser le profil `dev` en production.
