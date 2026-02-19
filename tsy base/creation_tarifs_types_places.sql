-- Création des tarifs par type de place pour le calcul du revenu maximum
-- Ces tarifs sont utilisés pour calculer le revenu potentiel des salles

-- Vérifier si les tarifs par type de place existent déjà
DO $$
DECLARE
    vip_exists BOOLEAN;
    premium_exists BOOLEAN;
    standard_exists BOOLEAN;
BEGIN
    -- Vérifier l'existence des tarifs VIP
    SELECT EXISTS(SELECT 1 FROM tarifs WHERE type = 'VIP' AND actif = true) INTO vip_exists;
    
    -- Vérifier l'existence des tarifs PREMIUM
    SELECT EXISTS(SELECT 1 FROM tarifs WHERE type = 'PREMIUM' AND actif = true) INTO premium_exists;
    
    -- Vérifier l'existence des tarifs STANDARD
    SELECT EXISTS(SELECT 1 FROM tarifs WHERE type = 'STANDARD' AND actif = true) INTO standard_exists;
    
    -- Créer les tarifs manquants
    IF NOT vip_exists THEN
        INSERT INTO tarifs (nom, prix, description, type, actif, created_at, updated_at)
        VALUES ('Tarif VIP', 18.00, 'Tarif pour les places VIP', 'VIP', true, NOW(), NOW());
        RAISE NOTICE 'Tarif VIP créé';
    END IF;
    
    IF NOT premium_exists THEN
        INSERT INTO tarifs (nom, prix, description, type, actif, created_at, updated_at)
        VALUES ('Tarif Premium', 15.00, 'Tarif pour les places Premium', 'PREMIUM', true, NOW(), NOW());
        RAISE NOTICE 'Tarif Premium créé';
    END IF;
    
    IF NOT standard_exists THEN
        INSERT INTO tarifs (nom, prix, description, type, actif, created_at, updated_at)
        VALUES ('Tarif Standard', 12.00, 'Tarif pour les places Standard', 'STANDARD', true, NOW(), NOW());
        RAISE NOTICE 'Tarif Standard créé';
    END IF;
    
    RAISE NOTICE 'Création des tarifs par type de place terminée';
END $$;

-- Afficher les tarifs par type de place actifs
SELECT 
    id,
    nom,
    prix,
    type,
    description,
    actif,
    created_at
FROM tarifs 
WHERE type IN ('VIP', 'PREMIUM', 'STANDARD') AND actif = true
ORDER BY type;

-- Afficher un résumé
SELECT 
    type,
    COUNT(*) as nombre_tarifs,
    ROUND(AVG(prix), 2) as prix_moyen,
    MIN(prix) as prix_min,
    MAX(prix) as prix_max
FROM tarifs 
WHERE type IN ('VIP', 'PREMIUM', 'STANDARD') AND actif = true
GROUP BY type
ORDER BY 
    CASE type 
        WHEN 'VIP' THEN 1 
        WHEN 'PREMIUM' THEN 2 
        WHEN 'STANDARD' THEN 3 
    END;
