# Incrément 1H — Premier agent IA réel (Analyste)

## Inclus
- POST `/api/v1/workflows/{workflowId}/steps/{stepId}/run-analyst` (ADMIN uniquement via Spring Security)
- Connexion serveur à l'API OpenAI Responses, `OPENAI_API_KEY` jamais transmise au frontend
- Génération JSON de `PROJECT_BRIEF` (`summary`, `requirements`, `assumptions`, `questions`)
- Sauvegarde en artefact PostgreSQL, événement et progression BMAD après réponse valide
- Bouton « Lancer l’analyste IA » dans le cockpit et la timeline
- Aucune migration SQL : réutilisation des tables existantes

## Exécution locale
Configurer `OPENAI_API_KEY` dans l'environnement backend ou dans `infra/.env` (fichier à ne jamais committer). `OPENAI_MODEL` optionnel, défaut `gpt-4.1-mini`. L'API OpenAI est facturée séparément de ChatGPT.

Depuis `infra`, exécuter `docker compose --profile local up --build -d`, puis ouvrir `http://localhost:8088/agent-workspace/`. Se connecter en ADMIN, créer un projet puis un workflow BMAD, démarrer et cliquer sur « Lancer l’analyste IA » à l'étape « Analyse du besoin ». Consulter ensuite l'artefact PROJECT_BRIEF.

## Limites
- Exécution HTTP synchrone (90 s max) : ne convient pas encore aux travaux longs.
- Aucune garantie de non-duplication des coûts si deux requêtes simultanées appellent OpenAI ; le verrou en base empêche seulement la double validation.
- Pas encore de conversation itérative, streaming, registre d'agents ni outils GitHub.
- Le premier agent produit un PROJECT_BRIEF, pas le PRD ; le PRD est l'étape Product Manager suivante.
- Les données de la demande sont envoyées à OpenAI : ne pas utiliser de secrets ou de données confidentielles sans autorisation.
- Les erreurs OpenAI sont volontairement génériques pour éviter d'exposer des données sensibles.
