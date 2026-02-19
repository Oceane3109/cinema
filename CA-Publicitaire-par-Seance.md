# 📊 CA Publicitaire par Séance - Spécification Technique

## 🎯 Objectif
Ajouter dans `AdminChiffreAffaire.vue` un onglet "Par Film - Par Séance" affichant :
- Le revenu des réservations par séance
- Le CA généré par les diffusions publicitaires par séance  
- Le revenu total (réservations + publicité)

---

## 🗂️ MCD (Modèle Conceptuel de Données)

```
FILM
├── id_film (PK)
├── titre
├── duree
└── ...

SEANCE
├── id_seance (PK)
├── id_film (FK → FILM.id_film)
├── id_salle (FK → SALLE.id_salle)
├── date_heure
└── ...

DIFFUSION_PUB
├── id_diffusion (PK)
├── id_societe (FK → SOCIETE.id_societe)
├── id_seance (FK → SEANCE.id_seance)
├── date_heure_diffusion
├── prix_unitaire
└── statut

RESERVATION
├── id_reservation (PK)
├── id_seance (FK → SEANCE.id_seance)
├── id_client (FK → CLIENT.id_client)
├── montant_total
└── ...

SOCIETE
├── id_societe (PK)
├── nom
└── ...
```

**✅ Tables existantes utilisées - Aucune nouvelle table nécessaire**

---

## 🎨 Maquette UI

### Onglet "Par Film - Par Séance"

```
┌─ ADMIN CHIFFRE D'AFFAIRES ───────────────────────────┐
│ [Par Film] [Par Séance] [Par Film - Par Séance]       │
├──────────────────────────────────────────────────────┤
│ 📊 Revenus par Séance avec Publicité                  │
│                                                      │
│ ┌─ FILTRES ───────────────────────────────────────┐ │
│ │ 🎬 Film: [Avatar ▼] 📅 Du: [25/12/2025] Au: [31/12/2025] │ │
│ │ [🔍 Filtrer] [🔄 Réinitialiser] [📥 Exporter Excel]   │ │
│ └────────────────────────────────────────────────────┘ │
│                                                      │
│ ┌─ FILM ──┬─ SÉANCE ──┬─ REVENU ┬─ CA PUB ┬─ TOTAL ──┐ │
│ │ Avatar  │ 25/12 14H │ 2.5M Ar │ 400K Ar│ 2.9M Ar │ │
│ │ Avatar  │ 25/12 18H │ 3.1M Ar │ 400K Ar│ 3.5M Ar │ │
│ │ Avatar  │ 26/12 14H │ 2.8M Ar │ 600K Ar│ 3.4M Ar │ │
│ │ Dune    │ 26/12 16H │ 4.2M Ar │ 800K Ar│ 5.0M Ar │ │
│ │ Dune    │ 27/12 20H │ 3.9M Ar │ 800K Ar│ 4.7M Ar │ │
│ │ ...     │ ...       │ ...     │ ...    │ ...     │ │
│ └─────────┴───────────┴─────────┴────────┴─────────┘ │
│                                                      │
│ 📈 Légende:                                         │
│ • REVENU = Σ(montant_total) des réservations         │
│ • CA PUB = Σ(prix_unitaire) des pubs cette séance   │
│ • TOTAL = REVENU + CA PUB                           │
└──────────────────────────────────────────────────────┘
```

---

## 🔧 Spécifications Fonctionnelles

### Backend - ControlleurChiffreAffaires.java

```java
/**
 * GET /api/chiffre-affaires/revenus-par-seance-avec-pub
 * Retourne les revenus par séance avec CA publicitaire inclus
 */
@GetMapping("/revenus-par-seance-avec-pub")
public ResponseEntity<List<Map<String, Object>>> getRevenusParSeanceAvecPub(
    @RequestParam(required = false) Long filmId,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin
) {
    // Retourne: List<{
    //   id_seance: Long,
    //   titre_film: String,
    //   date_heure: LocalDateTime,
    //   revenu_reservations: Double,
    //   ca_publicite: Double,
    //   revenu_total: Double
    // }>
}

/**
 * GET /api/chiffre-affaires/exporter/seances-avec-pub
 * Export Excel des revenus par séance avec CA publicitaire
 */
@GetMapping("/exporter/seances-avec-pub")
public ResponseEntity<byte[]> exporterSeancesAvecPub(
    @RequestParam(required = false) Long filmId,
    @RequestParam(required = false) LocalDate dateDebut,
    @RequestParam(required = false) LocalDate dateFin
) {
    // Retourne: ResponseEntity<byte[]> (fichier Excel)
}
```

### Frontend - AdminChiffreAffairesPage.vue

```javascript
// ===== FONCTIONS PRINCIPALES =====

/**
 * Charge les revenus par séance avec CA publicitaire
 * @param {number|null} filmId - ID du film (optionnel)
 * @param {Date|null} dateDebut - Date de début (optionnelle)
 * @param {Date|null} dateFin - Date de fin (optionnelle)
 * @returns {Promise<void>}
 */
async function loadRevenusParSeanceAvecPub(filmId = null, dateDebut = null, dateFin = null) {
    // Appelle: GET /api/chiffre-affaires/revenus-par-seance-avec-pub
    // Met à jour: revenusParSeance.value
}

/**
 * Filtre par film
 * @param {number} filmId - ID du film sélectionné
 * @returns {void}
 */
function filterByFilm(filmId) {
    selectedFilmId.value = filmId
    loadRevenusParSeanceAvecPub(filmId, dateDebut.value, dateFin.value)
}

/**
 * Filtre par dates
 * @param {Date} dateDebut - Date de début
 * @param {Date} dateFin - Date de fin
 * @returns {void}
 */
function filterByDates(dateDebut, dateFin) {
    dateDebut.value = dateDebut
    dateFin.value = dateFin
    loadRevenusParSeanceAvecPub(selectedFilmId.value, dateDebut, dateFin)
}

// ===== FONCTIONS D'AFFICHAGE =====

/**
 * Formate un montant en Ariary
 * @param {number} montant - Montant à formater
 * @returns {string} - Ex: "2.5M Ar"
 */
function formatMontant(montant) {
    if (montant >= 1000000) {
        return (montant / 1000000).toFixed(1) + 'M Ar'
    } else if (montant >= 1000) {
        return (montant / 1000).toFixed(0) + 'K Ar'
    }
    return montant.toFixed(0) + ' Ar'
}

/**
 * Formate une date/heure
 * @param {Date} dateHeure - Date/heure à formater
 * @returns {string} - Ex: "25/12 14H30"
 */
function formatDateTime(dateHeure) {
    return new Date(dateHeure).toLocaleString('fr-FR', {
        day: '2-digit',
        month: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    }).replace(':', 'H')
}

/**
 * Exporte les données en Excel
 * @returns {Promise<void>}
 */
async function exportExcel() {
    // Appelle: GET /api/chiffre-affaires/exporter/seances-avec-pub
    // Télécharge le fichier Excel
}
```

---

## 🎯 Actions des Boutons

### Bouton "🔍 Filtrer"
```javascript
// onClick: handleFiltrer
function handleFiltrer() {
    // Appelle: filterByFilm(selectedFilmId.value)
    // Appelle: filterByDates(dateDebut.value, dateFin.value)
    // Classe CSS: .btn-primary
    // Icone: <i class="bi bi-search"></i>
}
```

### Bouton "🔄 Réinitialiser"
```javascript
// onClick: handleReset
function handleReset() {
    // Réinitialise: selectedFilmId.value = null
    // Réinitialise: dateDebut.value = null
    // Réinitialise: dateFin.value = null
    // Appelle: loadRevenusParSeanceAvecPub()
    // Classe CSS: .btn-secondary
    // Icone: <i class="bi bi-arrow-clockwise"></i>
}
```

### Bouton "📥 Exporter Excel"
```javascript
// onClick: handleExportExcel
function handleExportExcel() {
    // Appelle: exportExcel()
    // Args: filmId, dateDebut, dateFin actuels
    // Classe CSS: .btn-success
    // Icone: <i class="bi bi-file-earmark-excel"></i>
}
```

### Bouton "👁️ Détails" (par ligne)
```javascript
// onClick: showDetailsSeance(seance.id_seance)
function showDetailsSeance(seanceId) {
    // Args: seanceId (number)
    // Affiche: Modal avec détails réservations + pubs
    // Classe CSS: .btn-sm.btn-outline-primary
    // Icone: <i class="bi bi-eye"></i>
}
```

---

## 📊 Logique de Calcul (SQL)

### Revenu Total par Séance avec Publicité

```sql
SELECT 
    s.id_seance,
    s.date_heure,
    f.titre as titre_film,
    -- Revenu des réservations
    COALESCE(SUM(r.montant_total), 0) as revenu_reservations,
    -- CA publicitaire
    COALESCE(SUM(dp.prix_unitaire), 0) as ca_publicite,
    -- Revenu total
    (COALESCE(SUM(r.montant_total), 0) + COALESCE(SUM(dp.prix_unitaire), 0)) as revenu_total,
    -- Nombre de réservations
    COUNT(DISTINCT r.id_reservation) as nb_reservations,
    -- Nombre de diffusions pub
    COUNT(DISTINCT dp.id_diffusion) as nb_diffusions_pub
FROM seances s
LEFT JOIN films f ON s.id_film = f.id_film
LEFT JOIN reservations r ON s.id_seance = r.id_seance
LEFT JOIN diffusion_pub dp ON s.id_seance = dp.id_seance
WHERE 1=1
    AND (:filmId IS NULL OR s.id_film = :filmId)
    AND (:dateDebut IS NULL OR DATE(s.date_heure) >= :dateDebut)
    AND (:dateFin IS NULL OR DATE(s.date_heure) <= :dateFin)
GROUP BY s.id_seance, s.date_heure, f.titre
ORDER BY s.date_heure DESC
```

### CA Publicitaire par Séance (détail)

```sql
SELECT 
    s.id_seance,
    s.date_heure,
    f.titre,
    dp.id_diffusion,
    so.nom as societe,
    dp.prix_unitaire,
    dp.date_heure_diffusion,
    dp.statut
FROM seances s
JOIN films f ON s.id_film = f.id_film
JOIN diffusion_pub dp ON s.id_seance = dp.id_seance
JOIN societe so ON dp.id_societe = so.id_societe
WHERE s.id_seance = :seanceId
ORDER BY dp.date_heure_diffusion
```

---

## 🎨 Styles CSS

```css
/* Styles pour l'onglet "Par Film - Par Séance" */
.tab-seances-pub {
    animation: fadeIn 0.3s ease;
}

.table-seances-pub th {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border: none;
    font-weight: 600;
    text-transform: uppercase;
    font-size: 0.85rem;
}

.table-seances-pub td {
    vertical-align: middle;
    padding: 1rem;
}

.revenu-cell {
    font-weight: 600;
    color: #2d3748;
}

.ca-pub-cell {
    font-weight: 600;
    color: #d69e2e;
    background-color: #fffbeb;
}

.total-cell {
    font-weight: 700;
    color: #22543d;
    background-color: #f0fff4;
}

.filtres-seances {
    background: #f8fafc;
    border-radius: 10px;
    padding: 1.5rem;
    margin-bottom: 2rem;
}

.btn-export {
    background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
    border: none;
    color: white;
}

.btn-export:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(72, 187, 120, 0.4);
}
```

---

## 🔄 État Réactif (Vue 3)

```javascript
// État réactif dans AdminChiffreAffairesPage.vue
const state = reactive({
    // Onglets
    activeTab: ref('par-film-seance'),
    
    // Filtres
    selectedFilmId: ref(null),
    dateDebut: ref(null),
    dateFin: ref(null),
    
    // Données
    revenusParSeance: ref([]),
    films: ref([]),
    loading: ref(false),
    
    // UI
    showDetailsModal: ref(false),
    selectedSeance: ref(null)
})
```

---

## 📋 Checklist d'Implémentation

### Backend ☑️
- [ ] Ajouter endpoint `/revenus-par-seance-avec-pub` dans `ControlleurChiffreAffaires.java`
- [ ] Implémenter la requête SQL de calcul
- [ ] Ajouter endpoint d'export Excel
- [ ] Tester les endpoints

### Frontend ☑️
- [ ] Ajouter onglet "Par Film - Par Séance" dans `AdminChiffreAffairesPage.vue`
- [ ] Implémenter les fonctions de chargement
- [ ] Ajouter les filtres (film, dates)
- [ ] Créer le tableau avec 3 colonnes (REVENU, CA PUB, TOTAL)
- [ ] Ajouter les boutons d'action
- [ ] Implémenter l'export Excel
- [ ] Ajouter les styles CSS

### Tests ☑️
- [ ] Tester avec données réelles
- [ ] Vérifier les calculs
- [ ] Tester les filtres
- [ ] Tester l'export
- [ ] Vérifier la responsivité

---

## 🚀 Avantages de cette Solution

1. **✅ Pas de nouvelle table** - Utilise les données existantes
2. **⚡ Calcul en temps réel** - Toujours à jour
3. **🎯 Performance optimisée** - Requêtes SQL efficaces
4. **🔍 Filtrage flexible** - Par film et par période
5. **📊 Export complet** - Excel avec toutes les données
6. **🎨 UI moderne** - Design cohérent avec le reste

---

*Prêt à implémenter cette solution complète pour le CA publicitaire par séance !* 🎯✨
