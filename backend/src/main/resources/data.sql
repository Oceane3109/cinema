-- Création de la table parametre_reservation
CREATE TABLE IF NOT EXISTS parametre_reservation (
    id BIGSERIAL PRIMARY KEY,
    nom_parametre VARCHAR(255) NOT NULL UNIQUE,
    valeur VARCHAR(1000) NOT NULL,
    description TEXT,
    type_valeur VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insertion des paramètres par défaut
INSERT INTO parametre_reservation (nom_parametre, valeur, description, type_valeur) VALUES
('REMISE_PLACES_MIN', '5', 'Nombre minimum de places pour obtenir une remise', 'INTEGER'),
('REMISE_POURCENTAGE', '10', 'Pourcentage de remise pour les réservations groupées', 'DECIMAL')
ON CONFLICT (nom_parametre) DO NOTHING;
