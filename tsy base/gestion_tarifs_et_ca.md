# Gestion des Tarifs et Chiffre d'Affaires - Spécifications Fonctionnelles

## Récapitulatif de la demande

### 1. Gestion des tarifs différenciés
- **Catégories de personnes** : Étudiants, Professionnels, Enfants, Adultes, Seniors, etc.
- **Genre** : Homme, Femme, ou les deux (ex: Adulte Femme)
- **Combinaisons possibles** : Adulte Femme, Étudiant Homme, Enfant, etc.

### 2. Chiffre d'affaires par film
- **Périodes** : Journalier, Mensuel, Annuel
- **Calcul** : Basé sur le nombre d'entrées/spectateurs
- **Analyse** : Par film et par période

---

## MCD (Modèle Conceptuel de Données)

### Tables existantes à conserver/modifier
- `films` (id, titre, description, duree_minutes, etc.)
- `seances` (id, film_id, salle_id, date_heure, prix, etc.)
- `reservations` (id, seance_id, nom_client, etc.)
- `factures` (id, reservation_id, montant_ttc, etc.)
- `tarifs` (table existante à étendre)

### Nouvelles tables à ajouter

#### 1. Extension de la table tarifs existante
```sql
-- Ajouter des colonnes à la table tarifs existante
ALTER TABLE tarifs ADD COLUMN genre VARCHAR(20) DEFAULT 'TOUS';
ALTER TABLE tarifs ADD COLUMN age_min INTEGER;
ALTER TABLE tarifs ADD COLUMN age_max INTEGER;
ALTER TABLE tarifs ADD COLUMN description_complementaire TEXT;

-- Mettre à jour le check constraint pour inclure les genres
ALTER TABLE tarifs DROP CONSTRAINT tarifs_type_check;
ALTER TABLE tarifs ADD CONSTRAINT tarifs_type_check 
CHECK (type::text = ANY (ARRAY['STANDARD'::character varying, 'ENFANT'::character varying, 'ETUDIANT'::character varying, 'SENIOR'::character varying, 'VIP'::character varying, 'GROUPE'::character varying, 'MATINEE'::character varying, 'SOIREE'::character varying, 'PROFESSIONNEL'::character varying]::text[]));

ALTER TABLE tarifs ADD CONSTRAINT tarifs_genre_check 
CHECK (genre::text = ANY (ARRAY['HOMME'::character varying, 'FEMME'::character varying, 'TOUS'::character varying]::text[]));

-- Index pour le nouveau champ genre
CREATE INDEX idx_tarifs_genre ON tarifs(genre);
```

#### 2. genres (table de référence)
```sql
CREATE TABLE genres (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(20) NOT NULL UNIQUE, -- 'HOMME', 'FEMME', 'TOUS'
    description TEXT,
    actif BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 3. client_tarifs (liaison réservations-tarifs)
```sql
CREATE TABLE client_tarifs (
    id SERIAL PRIMARY KEY,
    reservation_id INTEGER NOT NULL REFERENCES reservations(id),
    tarif_id INTEGER NOT NULL REFERENCES tarifs(id),
    genre_applique VARCHAR(20) NOT NULL, -- 'HOMME', 'FEMME', 'TOUS'
    tarif_applique DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 4. chiffre_affaire_film
```sql
CREATE TABLE chiffre_affaire_film (
    id SERIAL PRIMARY KEY,
    film_id INTEGER NOT NULL REFERENCES films(id),
    date_calcul DATE NOT NULL,
    type_periode VARCHAR(10) NOT NULL CHECK (type_periode IN ('JOUR', 'MOIS', 'ANNEE')),
    nombre_entrées INTEGER NOT NULL DEFAULT 0,
    chiffre_affaire DECIMAL(12,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (film_id, date_calcul, type_periode)
);
```

---

## Dessins d'Écrans

### 1. Écran de Gestion des Tarifs

```
┌─────────────────────────────────────────────────────────────────┐
│                    GESTION DES TARIFS                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  [AJOUTER TARIF] [MODIFIER] [SUPPRIMER] [ACTIVER/DESACTIVER]   │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                     LISTE DES TARIFS                       │ │
│  │                                                             │ │
│  │ Catégorie   │ Genre    │ Prix    │ Statut    │ Actions     │ │
│  │-------------│----------│---------│-----------│-------------│ │
│  │ Étudiant    │ Homme    │ 9.00€   │ Actif     │ [✏️][🗑️]    │ │
│  │ Étudiant    │ Femme    │ 8.50€   │ Actif     │ [✏️][🗑️]    │ │
│  │ Enfant      │ Tous     │ 6.00€   │ Actif     │ [✏️][🗑️]    │ │
│  │ Adulte      │ Homme    │ 12.00€  │ Actif     │ [✏️][🗑️]    │ │
│  │ Adulte      │ Femme    │ 11.00€  │ Actif     │ [✏️][🗑️]    │ │
│  │ Senior      │ Tous     │ 8.00€   │ Inactif   │ [✏️][🗑️]    │ │
│  │ ...         │ ...      │ ...     │ ...       │ ...         │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│  [FILTRER PAR CATÉGORIE] [FILTRER PAR GENRE] [EXPORTER CSV]     │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 2. Écran de Chiffre d'Affaires

```
┌─────────────────────────────────────────────────────────────────┐
│                 CHIFFRE D'AFFAIRES PAR FILM                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Période: [JOURNAIER ▼] [MOIS ▼] [ANNÉE ▼]                     │
│  Date: [01/01/2026 ▼] au [31/01/2026 ▼]                        │
│  Film: [TOUS ▼]                                                 │
│                                                                 │
│  [ACTUALISER] [EXPORTER PDF] [EXPORTER EXCEL]                   │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                    RÉSULTATS                               │ │
│  │                                                             │ │
│  │ Film                │ Entrées │ CA Total │ CA Moyen/Entrée  │ │
│  │---------------------│---------│----------│-----------------│ │
│  │ Avatar 2            │ 1,234   │ 14,808€  │ 12.00€          │ │
│  │ Top Gun: Maverick   │ 987     │ 11,844€  │ 12.00€          │ │
│  │ Dune: Part Two     │ 756     │ 8,316€   │ 11.00€          │ │
│  │ ...                 │ ...     │ ...      │ ...             │ │
│  │                     │         │          │                 │ │
│  │ TOTAL GÉNÉRAL       │ 2,977   │ 34,968€  │ 11.75€          │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                    GRAPHIQUE                                │ │
│  │                                                             │ │
│  │     📊 Évolution du CA par film                            │ │
│  │                                                             │ │
│  │  15k€ ┤                                                   █ │ │
│  │  12k€ ┤                                                 █ █ │ │
│  │   9k€ ┤                                               █ █   │ │
│  │   6k€ ┤                                             █ █     │ │
│  │   3k€ ┤                                           █ █       │ │
│  │    0€ ┤─────────────────────────────────────────█ █─────────│ │
│  │        Avatar 2    Top Gun    Dune    Autres    Total       │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 3. Écran d'Ajout/Modification de Tarif

```
┌─────────────────────────────────────────────────────────────────┐
│                  AJOUTER/MODIFIER UN TARIF                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Catégorie de personne: [ÉTUDIANT ▼] *                         │
│  Genre: [HOMME ▼] *                                            │
│  Prix: [12.50€] *                                              │
│  Description: [Tarif étudiant pour séances en semaine]         │
│  Statut: [✓] Actif                                             │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                 TARIFS SIMILAIRES                           │ │
│  │                                                             │ │
│  │ • Étudiant - Femme: 8.50€                                  │ │
│  • Étudiant - Tous: 9.00€                                   │ │
│  • Adulte - Homme: 12.00€                                    │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│  [SAUVEGARDER] [ANNULER] [RÉINITIALISER]                       │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Spécifications des Boutons et Fonctions

### Écran de Gestion des Tarifs

| Bouton | Fonction appelée | Type de retour | Arguments | Vue associée |
|--------|------------------|----------------|-----------|--------------|
| [AJOUTER TARIF] | `ajouterTarif()` | `void` | `TarifDTO tarif` | `tarifs_detailles` |
| [MODIFIER] | `modifierTarif()` | `TarifDTO` | `Long id, TarifDTO tarif` | `tarifs_detailles` |
| [SUPPRIMER] | `supprimerTarif()` | `void` | `Long id` | `tarifs_detailles` |
| [ACTIVER/DESACTIVER] | `changerStatutTarif()` | `void` | `Long id, boolean actif` | `tarifs_detailles` |
| [FILTRER PAR CATÉGORIE] | `filtrerParCategorie()` | `List<TarifDTO>` | `String categorie` | `tarifs_detailles` |
| [FILTRER PAR GENRE] | `filtrerParGenre()` | `List<TarifDTO>` | `String genre` | `tarifs_detailles` |
| [EXPORTER CSV] | `exporterTarifsCSV()` | `byte[]` | `List<TarifDTO> tarifs` | `tarifs_detailles` |

### Écran de Chiffre d'Affaires

| Bouton | Fonction appelée | Type de retour | Arguments | Vue associée |
|--------|------------------|----------------|-----------|--------------|
| [ACTUALISER] | `calculerChiffreAffaire()` | `List<CADTO>` | `String periode, Date debut, Date fin, Long filmId` | `chiffre_affaire_film` |
| [EXPORTER PDF] | `exporterCAPDF()` | `byte[]` | `List<CADTO> ca, String periode` | `chiffre_affaire_film` |
| [EXPORTER EXCEL] | `exporterCAExcel()` | `byte[]` | `List<CADTO> ca, String periode` | `chiffre_affaire_film` |

### Écran d'Ajout/Modification de Tarif

| Bouton | Fonction appelée | Type de retour | Arguments | Vue associée |
|--------|------------------|----------------|-----------|--------------|
| [SAUVEGARDER] | `sauvegarderTarif()` | `TarifDTO` | `TarifDTO tarif` | `tarifs_detailles` |
| [ANNULER] | `retourListeTarifs()` | `void` | - | `tarifs_detailles` |
| [RÉINITIALISER] | `reinitialiserFormulaire()` | `void` | - | - |

---

## Vues SQL Principales

### 1. Vue des tarifs actifs
```sql
CREATE VIEW v_tarifs_actifs AS
SELECT 
    t.id,
    t.nom,
    t.prix,
    t.description,
    t.type,
    t.genre,
    t.age_min,
    t.age_max,
    t.description_complementaire,
    t.actif
FROM tarifs t
WHERE t.actif = true;
```

### 2. Vue du chiffre d'affaires par film
```sql
CREATE VIEW v_ca_film_mensuel AS
SELECT 
    f.id as film_id,
    f.titre,
    EXTRACT(MONTH FROM r.date_reservation) as mois,
    EXTRACT(YEAR FROM r.date_reservation) as annee,
    COUNT(r.id) as nombre_reservations,
    SUM(r.montant_total) as chiffre_affaire
FROM films f
JOIN seances s ON f.id = s.film_id
JOIN reservations r ON s.id = r.seance_id
WHERE r.statut = 'CONFIRMEE'
GROUP BY f.id, f.titre, 
         EXTRACT(MONTH FROM r.date_reservation), 
         EXTRACT(YEAR FROM r.date_reservation);
```

### 3. Vue des statistiques par catégorie et genre
```sql
CREATE VIEW v_stats_ca_categorie_genre AS
SELECT 
    t.type as categorie,
    ct.genre_applique as genre,
    COUNT(ct.id) as nombre_clients,
    SUM(ct.tarif_applique) as total_ca,
    AVG(ct.tarif_applique) as tarif_moyen
FROM client_tarifs ct
JOIN tarifs t ON ct.tarif_id = t.id
GROUP BY t.type, ct.genre_applique;
```

---

## Intégration avec le système existant

### Modifications nécessaires sur les tables existantes

#### Table `reservations`
Ajouter une colonne pour stocker les informations de tarif appliqué:
```sql
ALTER TABLE reservations ADD COLUMN tarif_info JSONB;
```

#### Table `seances`
Conserver le prix existant comme prix de référence:
```sql
ALTER TABLE seances ADD COLUMN prix_base DECIMAL(10,2);
UPDATE seances SET prix_base = prix;
```

### Trigger pour calcul automatique du CA
```sql
CREATE OR REPLACE FUNCTION calculer_ca_film()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO chiffre_affaire_film (film_id, date_calcul, type_periode, nombre_entrées, chiffre_affaire)
    VALUES (
        (SELECT s.film_id FROM seances s WHERE s.id = NEW.seance_id),
        CURRENT_DATE,
        'JOUR',
        1,
        NEW.montant_total
    )
    ON CONFLICT (film_id, date_calcul, type_periode) 
    DO UPDATE SET 
        nombre_entrées = chiffre_affaire_film.nombre_entrées + 1,
        chiffre_affaire = chiffre_affaire_film.chiffre_affaire + NEW.montant_total;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_calcul_ca_film 
    AFTER INSERT ON reservations 
    FOR EACH ROW 
    WHEN (NEW.statut = 'CONFIRMEE')
    EXECUTE FUNCTION calculer_ca_film();
```

### Exemples de données pour la table tarifs étendue
```sql
-- Mise à jour des tarifs existants pour inclure le genre
UPDATE tarifs SET genre = 'TOUS' WHERE genre IS NULL;

-- Ajout de tarifs différenciés par genre
INSERT INTO tarifs (nom, prix, description, type, genre, age_min, age_max) VALUES
('Adulte Homme', 12.00, 'Tarif adulte pour hommes', 'STANDARD', 'HOMME', 18, 64),
('Adulte Femme', 11.00, 'Tarif adulte pour femmes', 'STANDARD', 'FEMME', 18, 64),
('Étudiant Homme', 9.00, 'Tarif étudiant pour hommes', 'ETUDIANT', 'HOMME', 16, 25),
('Étudiant Femme', 8.50, 'Tarif étudiant pour femmes', 'ETUDIANT', 'FEMME', 16, 25),
('Professionnel Homme', 10.00, 'Tarif professionnel pour hommes', 'PROFESSIONNEL', 'HOMME', 25, 65),
('Professionnel Femme', 9.50, 'Tarif professionnel pour femmes', 'PROFESSIONNEL', 'FEMME', 25, 65);
```

---

## Style Visuel Cohérent

### Palette de couleurs
- **Primaire** : #2C3E50 (bleu marine)
- **Secondaire** : #3498DB (bleu clair)
- **Succès** : #27AE60 (vert)
- **Danger** : #E74C3C (rouge)
- **Warning** : #F39C12 (orange)
- **Fond** : #ECF0F1 (gris clair)

### Typographie
- **Titres** : Montserrat, 18-24px, gras
- **Texte** : Open Sans, 14-16px
- **Boutons** : 14px, medium

### Icônes
- Utiliser Font Awesome ou Material Icons
- Taille cohérente : 16px pour les actions
- Couleurs : primaire pour les actions principales, secondaire pour les actions secondaires

### Espacement
- **Marges** : 20px entre les sections
- **Padding** : 15px dans les conteneurs
- **Hauteur des lignes** : 1.5 pour le texte

### Bordures et ombres
- **Bordures** : 1px solid #BDC3C7
- **Ombres** : 0 2px 4px rgba(0,0,0,0.1) pour les cartes
- **Coins arrondis** : 8px pour les boutons et conteneurs
