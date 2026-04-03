-- ============================================
-- INITIALISATION COMPLETE DE LA BASE CINEMA
-- ============================================
-- Structure, vidage, réinitialisation, et données de base
-- PostgreSQL

-- 1. STRUCTURE DES TABLES

CREATE TABLE societe (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(100),
    deja_payer DECIMAL(10,2) DEFAULT 0.00
);

CREATE TABLE DIFFUSION_PUB (
    id SERIAL PRIMARY KEY,
    id_societe INT,
    id_sceance INT,
    date_heure_diffusion TIMESTAMP,
    prix_unitaire DECIMAL(10,2),
    statut VARCHAR(100)
);

CREATE TABLE categories_personnes (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(50) NOT NULL,
    description TEXT,
    actif BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE genres (
    id SERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL,
    libelle VARCHAR(20) NOT NULL,
    actif BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE produits_extra (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL UNIQUE,
    prix_unitaire DECIMAL(10,2) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ventes_produits_extra (
    id SERIAL PRIMARY KEY,
    id_seance INTEGER NOT NULL REFERENCES seances(id),
    id_produit INTEGER NOT NULL REFERENCES produits_extra(id),
    quantite INTEGER NOT NULL CHECK (quantite > 0),
    prix_unitaire_vente DECIMAL(10,2) NOT NULL,
    date_vente TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE paiement (
    id SERIAL PRIMARY KEY,
    id_societe INT REFERENCES societe(id) ON DELETE CASCADE,
    montant DECIMAL(10,2) NOT NULL,
    date_paiement TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_diffusion_pub INT,
    id_paiement_groupe INT
);

-- Ajustements de compatibilite si les tables existent deja
ALTER TABLE paiement ADD COLUMN IF NOT EXISTS id_diffusion_pub INT;
ALTER TABLE paiement ADD COLUMN IF NOT EXISTS id_paiement_groupe INT;

-- 2. VIDAGE COMPLET DES TABLES

ALTER TABLE reservations DISABLE TRIGGER ALL;
ALTER TABLE seances DISABLE TRIGGER ALL;

DELETE FROM billets;
DELETE FROM factures;
DELETE FROM reservation_places;
DELETE FROM reservations;
DELETE FROM seances;
DELETE FROM places;
DELETE FROM salles;
DELETE FROM ventes_quotidiennes;
DELETE FROM films;
DELETE FROM client_tarifs;
DELETE FROM chiffre_affaire_film;
DELETE FROM tarifs;
DELETE FROM genres;

ALTER TABLE reservations ENABLE TRIGGER ALL;
ALTER TABLE seances ENABLE TRIGGER ALL;

-- 3. REINITIALISATION DES SEQUENCES

ALTER SEQUENCE films_id_seq RESTART WITH 1;
ALTER SEQUENCE salles_id_seq RESTART WITH 1;
ALTER SEQUENCE seances_id_seq RESTART WITH 1;
ALTER SEQUENCE places_id_seq RESTART WITH 1;
ALTER SEQUENCE reservations_id_seq RESTART WITH 1;
ALTER SEQUENCE reservation_places_id_seq RESTART WITH 1;
ALTER SEQUENCE billets_id_seq RESTART WITH 1;
ALTER SEQUENCE factures_id_seq RESTART WITH 1;
ALTER SEQUENCE tarifs_id_seq RESTART WITH 1;
ALTER SEQUENCE genres_id_seq RESTART WITH 1;
ALTER SEQUENCE client_tarifs_id_seq RESTART WITH 1;
ALTER SEQUENCE chiffre_affaire_film_id_seq RESTART WITH 1;

-- 4. INSERTION DES DONNÉES DE BASE

INSERT INTO genres (code, libelle) VALUES 
('HOMME', 'Homme'),
('FEMME', 'Femme'),
('BOTH', 'Les deux');

INSERT INTO produits_extra (nom, prix_unitaire, description) VALUES
('Popcorn', 10000.00, 'Popcorn sucré ou salé'),
('Boisson', 5000.00, 'Soda, jus ou eau'),
('Bonbon', 2000.00, 'Assortiment de bonbons'),
('Chocolat', 3000.00, 'Barres chocolatées'),
('Hot Dog', 8000.00, 'Hot dog avec sauce');

INSERT INTO tarifs (nom, prix, type, description, actif, created_at, updated_at, genre, age_min, age_max, type_place_id) 
VALUES 
    ('Standard Ado', 20000.00, 'Ado', 'Tarif standard pour adolescents', true, NOW(), NOW(), 'TOUS', 13, 17, NULL),
    ('VIP Ado', 45000.00, 'Ado', 'Tarif VIP pour adolescents', true, NOW(), NOW(), 'TOUS', 13, 17, NULL),
    ('Premium Ado', 30000.00, 'Ado', 'Tarif Premium pour adolescents', true, NOW(), NOW(), 'TOUS', 13, 17, NULL),
    ('Standard Senior', 30000.00, 'SENIOR', 'Tarif standard pour seniors', true, NOW(), NOW(), 'TOUS', 60, 120, NULL),
    ('VIP Senior', 50000.00, 'SENIOR', 'Tarif VIP pour seniors', true, NOW(), NOW(), 'TOUS', 60, 120, NULL),
    ('Premium Senior', 40000.00, 'SENIOR', 'Tarif Premium pour seniors', true, NOW(), NOW(), 'TOUS', 60, 120, NULL);

-- Fin du script
