# Authentification - extension 1G.1

Les comptes sont persistés dans `agent_workspace.app_user`. Mots de passe encodés par Spring Security (bcrypt via DelegatingPasswordEncoder).

## Développement uniquement

Avec `SPRING_PROFILES_ACTIVE=dev`, deux comptes sont créés **uniquement s’ils n’existent pas** :
- `jerome` / `test` : `ADMIN`
- `invite` / `invite` : `INVITE` (lecture seule)

Ces mots de passe sont volontairement faibles pour la démonstration. **Ne jamais exposer le profil `dev` sur Internet**. En production, choisir le profil `prod` et un mot de passe administrateur fort via `APP_BOOTSTRAP_ADMIN_*`; aucun compte de démonstration n'est créé.

Les rôles sont contrôlés dans Spring Security : les requêtes GET/HEAD authentifiées sont autorisées, toutes les autres mutations `/api/v1/**` nécessitent `ROLE_ADMIN` (hors login/logout). La consultation par `INVITE` couvre tous les projets et workflows; l'interface masque les actions d'écriture.

Le schéma est modifié via la migration Liquibase additive `006-invite-role.sql`. Aucun compte en clair dans les migrations.

Après extraction : `mvn clean verify` et `npm run build`. Testez l'accès lecture seule en tentant un POST avec `invite` : réponse HTTP 403 attendue (avec CSRF valide).
