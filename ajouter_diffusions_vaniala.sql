-- Ajouter 10 diffusions supplémentaires pour Vaniala (societe_id = 1)
INSERT INTO DIFFUSION_PUB (id_societe, id_sceance, date_heure_diffusion, prix_unitaire, statut) VALUES 
-- Vaniala - Diffusions supplémentaires en décembre 2025
(1, 21, '2025-12-25 14:30:00', 200000.00, 'TERMINE'),
(1, 22, '2025-12-26 17:00:00', 200000.00, 'TERMINE'),
(1, 23, '2025-12-27 20:00:00', 200000.00, 'TERMINE'),
(1, 24, '2025-12-28 14:30:00', 200000.00, 'TERMINE'),
(1, 25, '2025-12-29 17:00:00', 200000.00, 'TERMINE'),
(1, 26, '2025-12-30 20:00:00', 200000.00, 'TERMINE'),
(1, 27, '2025-12-31 14:30:00', 200000.00, 'TERMINE'),
(1, 28, '2025-12-15 20:00:00', 200000.00, 'TERMINE'),
(1, 29, '2025-12-16 14:30:00', 200000.00, 'TERMINE'),
(1, 30, '2025-12-17 17:00:00', 200000.00, 'TERMINE');

-- Vérification des totaux par société
SELECT 
    s.nom,
    COUNT(dp.id) as nb_diffusions,
    SUM(dp.prix_unitaire) as montant_total
FROM societe s
JOIN DIFFUSION_PUB dp ON s.id = dp.id_societe
GROUP BY s.id, s.nom
ORDER BY nb_diffusions DESC;

-- Total général
SELECT 
    COUNT(*) as total_diffusions,
    SUM(prix_unitaire) as montant_total
FROM DIFFUSION_PUB;
