# Agent Workspace — Cockpit React (Lot 1F)

Frontend React 19 / TypeScript / Vite, avec tableau de bord holographique, timeline BMAD, projets, workflows, artefacts et validation humaine.

## Démarrage local

1. Démarrer PostgreSQL et le backend Spring Boot 4 / Java 25 (depuis `../backend`).
2. Configurer `DB_PASSWORD` et les identifiants `APP_BOOTSTRAP_ADMIN_USERNAME`, `APP_BOOTSTRAP_ADMIN_PASSWORD` si nécessaire.
3. Démarrer le backend avec `dev` **uniquement en développement** pour utiliser la simulation.
4. Dans ce dossier : `npm install` puis `npm run dev`.
5. Ouvrir `http://localhost:5173/agent-workspace/`.

Le serveur Vite proxy les routes `/agent-workspace/api`, `/agent-workspace/login` et `/agent-workspace/logout` vers `http://localhost:8080`. Le backend conserve son API `/api/v1` et son authentification par session Spring Security. Pour un déploiement Nginx, servir le `dist/` sous `/agent-workspace/` et router `/agent-workspace/api/` vers `/api/` sur le backend. Prévoir également le routage de `/agent-workspace/login` et du traitement de connexion Spring Security, ou une page de connexion intégrée dédiée.

## Fonctionnalités

- Cockpit holographique avec métriques réelles issues des endpoints 1E.
- Timeline des étapes BMAD et progression calculée sur les étapes persistées.
- Création de projets et workflows, démarrage des workflows.
- Artefacts JSON versionnés et validations UX/PRD/architecture/Readiness.
- Sélection d'une maquette UX et demande de révision.
- Simulation JSON des étapes agent en profil `dev`/`test` uniquement.
- Mise en page responsive et respect de `prefers-reduced-motion`.

## Limites connues

- L'écran de connexion s'appuie sur le formulaire Spring Security par défaut, qui nécessite un routage adapté derrière Nginx.
- La simulation reste volontairement désactivée en production.
- Les artefacts UX de ce lot sont des objets JSON descriptifs, pas encore des maquettes interactives rendues.
- Le backend 1E n'expose pas de pagination; le cockpit liste les éléments renvoyés par l'API.
- Aucun test navigateur E2E n'est inclus dans ce livrable; il est prévu pour le lot 1G.
