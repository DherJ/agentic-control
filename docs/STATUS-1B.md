# Lot 1B - Registre des definitions BMAD

## Livré dans ce paquet

- Definition YAML BMAD Standard v1.0.0 (4 phases, 11 etapes).
- UX Designer avant approbation fonctionnelle, 3 propositions maximum.
- Validation des identifiants, types, dependances, ordre, approbations et cible de refus.
- Import idempotent et controle de checksum des versions publiees.
- Migration Liquibase 003 additive, sans modification de 001 et 002.
- Trois endpoints GET securises par la configuration Spring Security existante.
- Tests unitaires du validateur.
- Correction de la dependance Testcontainers PostgreSQL 2.x.

## Non livre dans 1B

- Execution d'instances de workflows (1C).
- Approbations et artefacts persistants (1D).
- Agents reels ou simules (1E).
- Interface React (1F).

## Verification requise localement

L'environnement de generation ne dispose que de Java 21 et ne dispose pas de Maven. Executer `mvn clean verify` avec Java 25 sur le poste de developpement et transmettre les eventuelles erreurs. Ne pas appliquer les migrations en production sans sauvegarde.
