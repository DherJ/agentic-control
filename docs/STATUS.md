# Lot 1 — état de livraison

## Incrément 1A (source livré, vérification non exécutée)
- Base conservée depuis le ZIP v1.1 : parent Spring Boot 4.0.0, Java 25, entité Workflow, migration initiale.
- Adaptation de l'enum global WorkflowStatus au moteur configurable v1.2.
- Authentification locale branchée sur app_user via Spring Security UserDetailsService.
- Bootstrap facultatif d'un administrateur via variables d'environnement, mot de passe haché.
- Liquibase : changeset 001 **inchangé**, nouveau changeset 002 additif.
- Ports du runtime d'agents (interfaces Java) et rôle UX_DESIGNER.
- Tests unitaires de politique de transition actualisés.
- ADR-011 : validation humaine UX avant validation fonctionnelle, 2 à 3 maquettes.

## Non livré en 1A
- Moteur de définitions YAML, tables v1.2 (workflow_definition, step_execution...), validations transactionnelles.
- Contrats API REST, gestion fine des autorisations par projet, simulation, frontend et Docker Compose.
- Maquettes produites automatiquement, intégration IA, GitHub et déploiement VPS.

## Vérifications non réalisées
Java 25, Maven et dépendances Maven Central indisponibles dans cet environnement : aucune compilation ni exécution de tests n'a été effectuée. Le projet doit être vérifié avec JDK 25 et Maven 3.9+.

## Attention migration
Le schéma 001 reste celui de v1.1 ; la normalisation vers workflow_instance/step_execution interviendra via des migrations additives ultérieures. Ne pas supposer que le modèle v1.2 est déjà opérationnel.
