# ADR-011 — UX Designer avant validation fonctionnelle
Statut : approuvé par le demandeur, implémentation prévue en incréments 1B–1F.

Ordre : Analyst (PROJECT_BRIEF) -> Product Manager (PRD) -> UX Designer (UX_MOCKUP_SET) -> approbation UX_DESIGN -> approbation FUNCTIONAL -> Architect.

- L'agent UX propose 2 ou 3 maquettes maximum par itération (2 par défaut).
- Les propositions sont distinctes, décrites avec leurs avantages et limites, et rattachées à la version du PRD.
- L'utilisateur sélectionne une maquette ou demande une révision. Aucun accord implicite.
- Une sélection approuvée produit UX_MOCKUP_SELECTION versionné et immuable.
- Toute modification substantielle du PRD invalide les validations UX dépendantes et impose un nouveau contrôle.
- Aucun agent IA ni moteur de rendu de maquettes n'est implémenté en 1A.
- La définition YAML BMAD Standard sera ajoutée en 1B, puis l'approbation sera exécutable en 1D.
