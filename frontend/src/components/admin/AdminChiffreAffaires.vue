<template>
  <div class="admin-chiffre-affaires">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div class="d-flex align-items-center gap-3">
        <h3 class="fw-bold">
          <i class="bi bi-graph-up me-2 text-primary"></i>
          Chiffre d'Affaires
        </h3>
        <div class="btn-group" role="group">
          <button type="button" class="btn" 
                  :class="vueAffichage === 'salle' ? 'btn-primary' : 'btn-outline-primary'"
                  @click="vueAffichage = 'salle'">
            <i class="bi bi-building me-1"></i>
            Par salle
          </button>
          <button type="button" class="btn" 
                  :class="vueAffichage === 'film' ? 'btn-primary' : 'btn-outline-primary'"
                  @click="vueAffichage = 'film'">
            <i class="bi bi-film me-1"></i>
            Par film
          </button>
          <button type="button" class="btn" 
                  :class="vueAffichage === 'film-seance' ? 'btn-primary' : 'btn-outline-primary'"
                  @click="vueAffichage = 'film-seance'">
            <i class="bi bi-film me-1"></i>
            Par film par séance
          </button>
        </div>
      </div>
      <div class="d-flex gap-2">
        <button @click="toggleFilters" class="btn btn-outline-secondary">
          <i class="bi bi-funnel me-1"></i>
          Filtres
          <span v-if="hasActiveFilters" class="badge bg-primary ms-1">{{ activeFiltersCount }}</span>
        </button>
        <button @click="resetFilters" class="btn btn-outline-warning" v-if="hasActiveFilters">
          <i class="bi bi-x-circle me-1"></i>
          Réinitialiser
        </button>
        <button @click="recalculerCA" class="btn btn-success">
          <i class="bi bi-arrow-clockwise me-2"></i>
          Recalculer
        </button>
        <div class="btn-group">
          <button class="btn btn-outline-secondary dropdown-toggle" type="button" data-bs-toggle="dropdown">
            <i class="bi bi-download me-1"></i>
            Exporter
          </button>
          <ul class="dropdown-menu">
            <li><a class="dropdown-item" href="#" @click="exporterPDF">
              <i class="bi bi-file-pdf me-2"></i>PDF
            </a></li>
            <li><a class="dropdown-item" href="#" @click="exporterExcel">
              <i class="bi bi-file-excel me-2"></i>Excel
            </a></li>
          </ul>
        </div>
      </div>
    </div>

    <!-- Filtres -->
    <div v-show="showFilters" class="card mb-4 border-0 shadow-sm">
      <div class="card-header bg-light border-bottom">
        <h6 class="mb-0 fw-semibold">
          <i class="bi bi-funnel me-2 text-primary"></i>
          Filtres et période
        </h6>
      </div>
      <div class="card-body">
        <div class="row g-3">
          <!-- Filtres pour vue par salle -->
          <template v-if="vueAffichage === 'salle'">
            <div class="col-md-3">
              <label class="form-label fw-medium">
                <i class="bi bi-building me-1"></i>
                Salle
              </label>
              <select v-model="filterSalle" @change="applyFilters" class="form-select">
                <option value="">Toutes les salles</option>
                <option v-for="salle in sallesDisponibles" :key="salle.id" :value="salle.id">
                  {{ salle.nom }} ({{ salle.type }})
                </option>
              </select>
            </div>
            <div class="col-md-3">
              <label class="form-label fw-medium">
                <i class="bi bi-tag me-1"></i>
                Type de salle
              </label>
              <select v-model="filterType" @change="applyFilters" class="form-select">
                <option value="">Tous les types</option>
                <option value="STANDARD">Standard</option>
                <option value="VIP">VIP</option>
                <option value="PREMIUM">Premium</option>
                <option value="3D">3D</option>
                <option value="FAMILLE">Famille</option>
              </select>
            </div>
          </template>

          <!-- Filtres pour vue par film par séance -->
          <template v-if="vueAffichage === 'film-seance'">
            <div class="col-md-4">
              <label class="form-label fw-medium">
                <i class="bi bi-film me-1"></i>
                Film
              </label>
              <select v-model="filterFilm" @change="applyFilters" class="form-select">
                <option value="">Tous les films</option>
                <option v-for="film in films" :key="film.id" :value="film.titre">
                  {{ film.titre }}
                </option>
              </select>
            </div>
            <div class="col-md-4">
              <label class="form-label fw-medium">
                <i class="bi bi-person me-1"></i>
                Type Client
              </label>
              <select v-model="filterTypeClient" @change="applyFilters" class="form-select">
                <option value="adulte">Adulte</option>
                <option value="enfant">Enfant</option>
              </select>
            </div>
          </template>

          <!-- Filtres pour vue par film -->
          <template v-if="vueAffichage === 'film'">
            <div class="col-md-4">
              <label class="form-label fw-medium">
                <i class="bi bi-film me-1"></i>
                Film
              </label>
              <select v-model="filterFilm" @change="applyFilters" class="form-select">
                <option value="">Tous les films</option>
                <option v-for="film in filmsDisponibles" :key="film.id" :value="film.id">
                  {{ film.titre }}
                </option>
              </select>
            </div>
            <div class="col-md-4">
              <label class="form-label fw-medium">
                <i class="bi bi-calendar3 me-1"></i>
                Période
              </label>
              <select v-model="filterPeriode" @change="applyFilters" class="form-select">
                <option value="">Toutes les périodes</option>
                <option value="JOURNALIER">Journalier</option>
                <option value="HEBDOMADAIRE">Hebdomadaire</option>
                <option value="MENSUEL">Mensuel</option>
                <option value="ANNUEL">Annuel</option>
              </select>
            </div>
            <div class="col-md-4">
              <label class="form-label fw-medium">
                <i class="bi bi-calendar-range me-1"></i>
                Période personnalisée
              </label>
              <div class="d-flex gap-2">
                <input v-model="dateDebut" type="date" @change="applyFilters" 
                       class="form-control" placeholder="Date début">
                <input v-model="dateFin" type="date" @change="applyFilters" 
                       class="form-control" placeholder="Date fin">
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>

    <!-- Statistiques principales -->
    <div class="row mb-4">
      <div class="col-md-3 mb-3">
        <div class="card border-0 shadow-sm text-center" style="border: 2px solid #28a745 !important;">
          <div class="card-body">
            <div class="icon-box bg-success bg-opacity-10 text-success rounded-3 p-3 mb-3">
              <i class="bi bi-calculator fs-4"></i>
            </div>
            <h6 class="text-muted small mb-2">Vrai Total Chiffre d'Affaires</h6>
            <h4 class="mb-0 fw-bold text-success">{{ formatPrix(totalChiffreAffairesComplet) }}€</h4>
            <small class="text-muted">Tickets + Publicité + Extras</small>
          </div>
        </div>
      </div>
      <div class="col-md-3 mb-3">
        <div class="card border-0 shadow-sm text-center" style="border: 2px solid #007bff !important;">
          <div class="card-body">
            <div class="icon-box bg-primary bg-opacity-10 text-primary rounded-3 p-3 mb-3">
              <i class="bi bi-bank fs-4"></i>
            </div>
            <h6 class="text-muted small mb-2">CA Réel (en banque)</h6>
            <h4 class="mb-0 fw-bold text-primary">{{ formatPrix(totalChiffreAffairesReel) }}€</h4>
            <small class="text-muted">Tickets + Publicité payée + Extras</small>
          </div>
        </div>
      </div>
      <div class="col-md-3 mb-3">
        <div class="card border-0 shadow-sm text-center">
          <div class="card-body">
            <div class="icon-box bg-primary bg-opacity-10 text-primary rounded-3 p-3 mb-3">
              <i class="bi bi-cash-stack fs-4"></i>
            </div>
            <h6 class="text-muted small mb-2">Chiffre d'affaires total ticket</h6>
            <h4 class="mb-0 fw-bold text-primary">{{ formatPrix(statsGenerales?.totalChiffreAffaire || 0) }}€</h4>
          </div>
        </div>
      </div>
      <div class="col-md-3 mb-3">
        <div class="card border-0 shadow-sm text-center">
          <div class="card-body">
            <div class="icon-box bg-warning bg-opacity-10 text-warning rounded-3 p-3 mb-3">
              <i class="bi bi-megaphone fs-4"></i>
            </div>
            <h6 class="text-muted small mb-2">CA Total Publicité</h6>
            <h4 class="mb-0 fw-bold text-warning">{{ formatPrix(totalCAPublicite) }}€</h4>
          </div>
        </div>
      </div>
      <div class="col-md-3 mb-3">
        <div class="card border-0 shadow-sm text-center" style="border: 2px solid #ffc107 !important;">
          <div class="card-body">
            <div class="icon-box bg-warning bg-opacity-10 text-warning rounded-3 p-3 mb-3">
              <i class="bi bi-cash-coin fs-4"></i>
            </div>
            <h6 class="text-muted small mb-2">CA Réel Publicité (payée)</h6>
            <h4 class="mb-0 fw-bold text-warning">{{ formatPrix(totalCAPublicitePayee) }}€</h4>
            <small class="text-muted">Seulement ce qui est encaissé</small>
          </div>
        </div>
      </div>
      <div class="col-md-3 mb-3">
        <div class="card border-0 shadow-sm text-center">
          <div class="card-body">
            <div class="icon-box bg-info bg-opacity-10 text-info rounded-3 p-3 mb-3">
              <i class="bi bi-basket fs-4"></i>
            </div>
            <h6 class="text-muted small mb-2">CA Total Produits Extras</h6>
            <h4 class="mb-0 fw-bold text-info">{{ formatPrix(totalCAProduitsExtras) }}€</h4>
          </div>
        </div>
      </div>
      <div class="col-md-3 mb-3">
        <div class="card border-0 shadow-sm text-center">
          <div class="card-body">
            <div class="icon-box bg-success bg-opacity-10 text-success rounded-3 p-3 mb-3">
              <i class="bi bi-people fs-4"></i>
            </div>
            <h6 class="text-muted small mb-2">Total entrées</h6>
            <h4 class="mb-0 fw-bold text-success">{{ (statsGenerales?.totalEntrees || 0).toLocaleString() }}</h4>
          </div>
        </div>
      </div>
      <div class="col-md-3 mb-3">
        <div class="card border-0 shadow-sm text-center">
          <div class="card-body">
            <div class="icon-box bg-info bg-opacity-10 text-info rounded-3 p-3 mb-3">
              <i class="bi bi-graph-up-arrow fs-4"></i>
            </div>
            <h6 class="text-muted small mb-2">Tarif moyen</h6>
            <h4 class="mb-0 fw-bold text-info">{{ formatPrix(statsGenerales?.tarifMoyen || 0) }}€</h4>
          </div>
        </div>
      </div>
      <div class="col-md-3 mb-3">
        <div class="card border-0 shadow-sm text-center">
          <div class="card-body">
            <div class="icon-box bg-warning bg-opacity-10 text-warning rounded-3 p-3 mb-3">
              <i class="bi bi-film fs-4"></i>
            </div>
            <h6 class="text-muted small mb-2">Films concernés</h6>
            <h4 class="mb-0 fw-bold text-warning">{{ statsGenerales?.nombreFilms || 0 }}</h4>
          </div>
        </div>
      </div>
    </div>

    <!-- Tableau du chiffre d'affaires -->
    <div class="card border-0 shadow-sm">
      <div class="card-header bg-light border-bottom">
        <h6 class="mb-0 fw-semibold">
          <i class="bi bi-list-ul me-2 text-primary"></i>
          Détail du chiffre d'affaires
        </h6>
      </div>
      <div class="card-body">
        <div v-if="loading" class="text-center py-4">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Chargement...</span>
          </div>
        </div>
        
        <div v-else-if="filteredCA.length === 0" class="text-center py-4 text-muted">
          <i class="bi bi-inbox fs-1 d-block mb-3"></i>
          <p class="mb-0">Aucune donnée de chiffre d'affaires trouvée</p>
          <p class="small mb-3">Cliquez sur "Recalculer" pour générer les données à partir des réservations existantes</p>
          <button @click="recalculerCA" class="btn btn-success">
            <i class="bi bi-arrow-clockwise me-2"></i>
            Recalculer le chiffre d'affaires
          </button>
        </div>

        <div v-else class="table-responsive">
          <table class="table table-hover align-middle">
            <thead class="table-light">
              <tr>
                <!-- En-têtes pour vue par salle -->
                <template v-if="vueAffichage === 'salle'">
                  <th @click="sortBy('salleNom')" style="cursor: pointer;" class="fw-medium">
                    Salle
                    <i v-if="sortField === 'salleNom'" :class="sortOrder === 'asc' ? 'bi bi-arrow-up' : 'bi bi-arrow-down'" class="ms-1"></i>
                  </th>
                  <th @click="sortBy('type')" style="cursor: pointer;" class="fw-medium">
                    Type
                    <i v-if="sortField === 'type'" :class="sortOrder === 'asc' ? 'bi bi-arrow-up' : 'bi bi-arrow-down'" class="ms-1"></i>
                  </th>
                  <th @click="sortBy('capacite')" style="cursor: pointer;" class="fw-medium">
                    Capacité
                    <i v-if="sortField === 'capacite'" :class="sortOrder === 'asc' ? 'bi bi-arrow-up' : 'bi bi-arrow-down'" class="ms-1"></i>
                  </th>
                  <th class="fw-medium">Répartition places</th>
                </template>
                
                <!-- En-têtes pour vue par film -->
                <template v-if="vueAffichage === 'film'">
                  <th @click="sortBy('filmTitre')" style="cursor: pointer;" class="fw-medium">
                    Film
                    <i v-if="sortField === 'filmTitre'" :class="sortOrder === 'asc' ? 'bi bi-arrow-up' : 'bi bi-arrow-down'" class="ms-1"></i>
                  </th>
                  <th @click="sortBy('typePeriode')" style="cursor: pointer;" class="fw-medium">
                    Période
                    <i v-if="sortField === 'typePeriode'" :class="sortOrder === 'asc' ? 'bi bi-arrow-up' : 'bi bi-arrow-down'" class="ms-1"></i>
                  </th>
                  <th @click="sortBy('dateCalcul')" style="cursor: pointer;" class="fw-medium">
                    Date calcul
                    <i v-if="sortField === 'dateCalcul'" :class="sortOrder === 'asc' ? 'bi bi-arrow-up' : 'bi bi-arrow-down'" class="ms-1"></i>
                  </th>
                </template>
                
                <!-- En-têtes pour vue par film par séance -->
                <template v-if="vueAffichage === 'film-seance'">
                  <th @click="sortBy('salleNom')" style="cursor: pointer;" class="fw-medium">
                    Salle
                    <i v-if="sortField === 'salleNom'" :class="sortOrder === 'asc' ? 'bi bi-arrow-up' : 'bi bi-arrow-down'" class="ms-1"></i>
                  </th>
                  <th @click="sortBy('filmTitre')" style="cursor: pointer;" class="fw-medium">
                    Film
                    <i v-if="sortField === 'filmTitre'" :class="sortOrder === 'asc' ? 'bi bi-arrow-up' : 'bi bi-arrow-down'" class="ms-1"></i>
                  </th>
                  <th @click="sortBy('dateHeure')" style="cursor: pointer;" class="fw-medium">
                    Date/Heure
                    <i v-if="sortField === 'dateHeure'" :class="sortOrder === 'asc' ? 'bi bi-arrow-up' : 'bi bi-arrow-down'" class="ms-1"></i>
                  </th>
                  <th @click="sortBy('placesReservees')" style="cursor: pointer;" class="fw-medium">
                    Places Réservées
                    <i v-if="sortField === 'placesReservees'" :class="sortOrder === 'asc' ? 'bi bi-arrow-up' : 'bi bi-arrow-down'" class="ms-1"></i>
                  </th>
                  <th @click="sortBy('revenuReel')" style="cursor: pointer;" class="fw-medium">
                    montant genere par ticket vendu
                    <i v-if="sortField === 'revenuReel'" :class="sortOrder === 'asc' ? 'bi bi-arrow-up' : 'bi bi-arrow-down'" class="ms-1"></i>
                  </th>
                  <th @click="sortBy('caPublicite')" style="cursor: pointer;" class="fw-medium">
                    montant genere par diffusion de publicite
                    <i v-if="sortField === 'caPublicite'" :class="sortOrder === 'asc' ? 'bi bi-arrow-up' : 'bi bi-arrow-down'" class="ms-1"></i>
                  </th>
                  <th @click="sortBy('montantPublicitePaye')" style="cursor: pointer;" class="fw-medium">
                    montant déjà payé publicité
                    <i v-if="sortField === 'montantPublicitePaye'" :class="sortOrder === 'asc' ? 'bi bi-arrow-up' : 'bi bi-arrow-down'" class="ms-1"></i>
                  </th>
                  <th @click="sortBy('montantPubliciteReste')" style="cursor: pointer;" class="fw-medium">
                    reste à payer publicité
                    <i v-if="sortField === 'montantPubliciteReste'" :class="sortOrder === 'asc' ? 'bi bi-arrow-up' : 'bi bi-arrow-down'" class="ms-1"></i>
                  </th>
                  <th @click="sortBy('montantProduitsExtras')" style="cursor: pointer;" class="fw-medium">
                    montant genere par produits extras
                    <i v-if="sortField === 'montantProduitsExtras'" :class="sortOrder === 'asc' ? 'bi bi-arrow-up' : 'bi bi-arrow-down'" class="ms-1"></i>
                  </th>
                  <th @click="sortBy('revenuTotal')" style="cursor: pointer;" class="fw-medium">
                    montant total CA
                    <i v-if="sortField === 'revenuTotal'" :class="sortOrder === 'asc' ? 'bi bi-arrow-up' : 'bi bi-arrow-down'" class="ms-1"></i>
                  </th>
                </template>
                
                <!-- En-têtes communs (masqués pour film-seance) -->
                <template v-if="vueAffichage !== 'film-seance'">
                  <th @click="sortBy('nombreEntrees')" style="cursor: pointer;" class="fw-medium">
                    Total Places
                    <i v-if="sortField === 'nombreEntrees'" :class="sortOrder === 'asc' ? 'bi bi-arrow-up' : 'bi bi-arrow-down'" class="ms-1"></i>
                  </th>
                  <th @click="sortBy('chiffreAffaire')" style="cursor: pointer;" class="fw-medium">
                    Revenu Max
                    <i v-if="sortField === 'chiffreAffaire'" :class="sortOrder === 'asc' ? 'bi bi-arrow-up' : 'bi bi-arrow-down'" class="ms-1"></i>
                  </th>
                  <th @click="sortBy('tarifMoyen')" style="cursor: pointer;" class="fw-medium">
                    Tarif Moyen
                    <i v-if="sortField === 'tarifMoyen'" :class="sortOrder === 'asc' ? 'bi bi-arrow-up' : 'bi bi-arrow-down'" class="ms-1"></i>
                  </th>
                </template>
                <th class="fw-medium">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="ca in paginatedCA" :key="ca.id" class="border-bottom">
                <!-- Colonnes pour vue par salle -->
                <template v-if="vueAffichage === 'salle'">
                  <td>
                    <div class="fw-semibold">{{ ca.salleNom }}</div>
                  </td>
                  <td>
                    <span class="badge" :class="getTypeBadgeClass(ca.type)">
                      {{ ca.type }}
                    </span>
                  </td>
                  <td>
                    <span class="badge bg-secondary">{{ ca.capacite }}</span>
                  </td>
                  <td>
                    <div class="d-flex flex-wrap gap-1">
                      <span v-for="(count, categorie) in ca.placesParCategorie" :key="categorie" 
                            class="badge" :class="getCategorieBadgeClass(categorie)">
                        {{ categorie }}: {{ count }}
                      </span>
                    </div>
                  </td>
                </template>
                
                <!-- Colonnes pour vue par film -->
                <template v-if="vueAffichage === 'film'">
                  <td>
                    <div class="fw-semibold">{{ ca.filmTitre }}</div>
                  </td>
                  <td>
                    <span class="badge bg-info">{{ ca.typePeriode }}</span>
                  </td>
                  <td>
                    <span class="badge bg-secondary">{{ formatDate(ca.dateCalcul) }}</span>
                  </td>
                </template>
                
                <!-- Colonnes pour vue par film par séance -->
                <template v-if="vueAffichage === 'film-seance'">
                  <td>
                    <div class="fw-semibold">{{ ca.salleNom }}</div>
                  </td>
                  <td>
                    <div class="fw-semibold">{{ ca.filmTitre }}</div>
                  </td>
                  <td>
                    <span class="badge bg-secondary">{{ formatDateTime(ca.dateHeure) }}</span>
                  </td>
                  <td>
                    <span class="badge bg-info">{{ ca.placesReservees.toLocaleString() }}</span>
                  </td>
                  <td>
                    <span class="badge bg-success">{{ formatPrix(ca.revenuReel) }}€</span>
                  </td>
                  <td>
                    <span class="badge bg-warning">{{ formatPrix(ca.caPublicite || 0) }}€</span>
                  </td>
                  <td>
                    <span class="badge bg-info">{{ formatPrix(ca.montantPublicitePaye || 0) }}€</span>
                  </td>
                  <td>
                    <span class="badge bg-warning">{{ formatPrix(ca.montantPubliciteReste || 0) }}€</span>
                  </td>
                  <td>
                    <span class="badge bg-info">{{ formatPrix(ca.montantProduitsExtras || 0) }}€</span>
                  </td>
                  <td>
                    <span class="badge bg-primary">{{ formatPrix((ca.revenuReel || 0) + (ca.caPublicite || 0) + (ca.montantProduitsExtras || 0)) }}€</span>
                  </td>
                </template>
                
                <!-- Colonnes communes (masquées pour film-seance) -->
                <template v-if="vueAffichage !== 'film-seance'">
                  <td>
                    <span class="badge bg-info">{{ ca.nombreEntrees.toLocaleString() }}</span>
                  </td>
                  <td>
                    <span class="badge bg-success fs-6">{{ formatPrix(ca.chiffreAffaire) }}€</span>
                  </td>
                  <td>
                    <span class="badge bg-warning">{{ formatPrix(ca.tarifMoyen) }}€</span>
                  </td>
                </template>
                <td>
                  <div class="btn-group" role="group">
                    <button @click="voirDetails(ca)" class="btn btn-sm btn-outline-primary" title="Voir détails">
                      <i class="bi bi-eye"></i>
                    </button>
                    <button @click="exporterLigne(ca)" class="btn btn-sm btn-outline-secondary" title="Exporter">
                      <i class="bi bi-download"></i>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        <nav v-if="totalPages > 1" class="mt-4">
          <ul class="pagination justify-content-center mb-0">
            <li class="page-item" :class="{ disabled: currentPage === 1 }">
              <button class="page-link" @click="currentPage = 1" :disabled="currentPage === 1">
                <i class="bi bi-chevron-double-left"></i>
              </button>
            </li>
            <li class="page-item" :class="{ disabled: currentPage === 1 }">
              <button class="page-link" @click="currentPage--" :disabled="currentPage === 1">
                <i class="bi bi-chevron-left"></i>
              </button>
            </li>
            <li v-for="page in visiblePages" :key="page" class="page-item" :class="{ active: page === currentPage }">
              <button class="page-link" @click="currentPage = page">{{ page }}</button>
            </li>
            <li class="page-item" :class="{ disabled: currentPage === totalPages }">
              <button class="page-link" @click="currentPage++" :disabled="currentPage === totalPages">
                <i class="bi bi-chevron-right"></i>
              </button>
            </li>
            <li class="page-item" :class="{ disabled: currentPage === totalPages }">
              <button class="page-link" @click="currentPage = totalPages" :disabled="currentPage === totalPages">
                <i class="bi bi-chevron-double-right"></i>
              </button>
            </li>
          </ul>
        </nav>
      </div>
    </div>

    <!-- Modal Détails -->
    <div class="modal fade" id="detailsModal" tabindex="-1">
      <div class="modal-dialog modal-lg">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header bg-light border-bottom">
            <h5 class="modal-title fw-semibold">
              <i class="bi bi-graph-up me-2 text-primary"></i>
              Revenu maximum par salle 
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <div v-if="selectedCA">
              <div class="row mb-3">
                <div class="col-md-6">
                  <h6 class="fw-semibold">Film</h6>
                  <p class="mb-0">{{ selectedCA.filmTitre }}</p>
                </div>
                <div class="col-md-6">
                  <h6 class="fw-semibold">Date de calcul</h6>
                  <p class="mb-0">{{ formatDate(selectedCA.dateCalcul) }}</p>
                </div>
              </div>
              <div class="row mb-3">
                <div class="col-md-3">
                  <h6 class="fw-semibold">Période</h6>
                  <p class="mb-0">{{ selectedCA.typePeriode }}</p>
                </div>
                <div class="col-md-3">
                  <h6 class="fw-semibold">Entrées</h6>
                  <p class="mb-0">{{ selectedCA.nombreEntrees.toLocaleString() }}</p>
                </div>
                <div class="col-md-3">
                  <h6 class="fw-semibold">Chiffre d'affaires</h6>
                  <p class="mb-0 fw-bold text-success">{{ formatPrix(selectedCA.chiffreAffaire) }}€</p>
                </div>
                <div class="col-md-3">
                  <h6 class="fw-semibold">Tarif moyen</h6>
                  <p class="mb-0">{{ formatPrix(selectedCA.tarifMoyen) }}€</p>
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer bg-light border-top">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
              <i class="bi bi-x-circle me-2"></i>
              Fermer
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { Modal } from 'bootstrap'

export default {
  name: 'AdminChiffreAffaires',
  props: {
    statsGenerales: {
      type: Object,
      default: () => ({
        totalChiffreAffaire: 0,
        totalEntrees: 0,
        tarifMoyen: 0,
        nombreFilms: 0
      })
    }
  },
  setup(props) {
    const chiffreAffaires = ref([])
    const loading = ref(false)
    const showFilters = ref(false)
    const films = ref([])
    
    // Filtres
    const vueAffichage = ref('salle') // 'salle', 'film' ou 'film-seance'
    const filterSalle = ref('')
    const filterType = ref('')
    const filterPeriode = ref('ANNUEL') // Valeur par défaut pour charger des données
    const filterTypeClient = ref('adulte') // Par défaut adulte pour film-seance
    const dateDebut = ref('')
    const dateFin = ref('')
    const filterFilm = ref('')
    
    // Données pour les sélecteurs
    const sallesDisponibles = ref([])
    const filmsDisponibles = ref([])
    
    // Tri
    const sortField = ref('dateCalcul')
    const sortOrder = ref('desc')
    
    // Pagination
    const currentPage = ref(1)
    const itemsPerPage = ref(10)
    
    // Modal
    let detailsModal = null
    const selectedCA = ref(null)
    
    // Computed
    const hasActiveFilters = computed(() => {
      if (vueAffichage.value === 'salle') {
        return filterSalle.value || filterType.value || filterPeriode.value
      } else {
        return filterFilm.value || dateDebut.value || dateFin.value || filterPeriode.value
      }
    })

    const activeFiltersCount = computed(() => {
      let count = 0
      if (vueAffichage.value === 'salle') {
        if (filterSalle.value) count++
        if (filterType.value) count++
        if (filterPeriode.value) count++
      } else {
        if (filterFilm.value) count++
        if (dateDebut.value) count++
        if (dateFin.value) count++
        if (filterPeriode.value) count++
      }
      return count
    })
    
    // Calcul des totaux pour les nouvelles cartes
    const totalCAPublicite = computed(() => {
      if (!chiffreAffaires.value || chiffreAffaires.value.length === 0) return 0
      return chiffreAffaires.value.reduce((total, item) => {
        return total + (item.caPublicite || 0)
      }, 0)
    })
    
    const totalCAPublicitePayee = computed(() => {
      if (!chiffreAffaires.value || chiffreAffaires.value.length === 0) return 0
      return chiffreAffaires.value.reduce((total, item) => {
        return total + (item.montantPublicitePaye || 0)
      }, 0)
    })
    
    const totalCAProduitsExtras = computed(() => {
      if (!chiffreAffaires.value || chiffreAffaires.value.length === 0) return 0
      return chiffreAffaires.value.reduce((total, item) => {
        return total + (item.montantProduitsExtras || 0)
      }, 0)
    })
    
    const totalChiffreAffairesComplet = computed(() => {
      const tickets = statsGenerales.value?.totalChiffreAffaire || 0
      const publicite = totalCAPublicite.value
      const extras = totalCAProduitsExtras.value
      return tickets + publicite + extras
    })
    
    const totalChiffreAffairesReel = computed(() => {
      const tickets = statsGenerales.value?.totalChiffreAffaire || 0
      const publicitePayee = totalCAPublicitePayee.value
      const extras = totalCAProduitsExtras.value
      return tickets + publicitePayee + extras
    })

    const statsGenerales = computed(() => props.statsGenerales)

    const filteredCA = computed(() => {
      if (!chiffreAffaires.value || !Array.isArray(chiffreAffaires.value)) {
        return []
      }
      
      let filtered = [...chiffreAffaires.value]
      
      // Filtre par période
      if (filterPeriode.value) {
        filtered = filtered.filter(ca => ca.typePeriode === filterPeriode.value)
      }
      
      // Filtre par dates
      if (dateDebut.value) {
        filtered = filtered.filter(ca => ca.dateCalcul >= dateDebut.value)
      }
      
      if (dateFin.value) {
        filtered = filtered.filter(ca => ca.dateCalcul <= dateFin.value)
      }
      
      // Filtre par film
      if (filterFilm.value) {
        filtered = filtered.filter(ca => ca.filmId == filterFilm.value)
      }
      
      // Tri
      filtered.sort((a, b) => {
        let aVal = a[sortField.value]
        let bVal = b[sortField.value]
        
        if (typeof aVal === 'string') {
          aVal = aVal.toLowerCase()
          bVal = bVal.toLowerCase()
        }
        
        if (sortOrder.value === 'asc') {
          return aVal > bVal ? 1 : -1
        } else {
          return aVal < bVal ? 1 : -1
        }
      })
      
      return filtered
    })

    const totalPages = computed(() => {
      return Math.ceil(filteredCA.value.length / itemsPerPage.value)
    })

    const paginatedCA = computed(() => {
      const start = (currentPage.value - 1) * itemsPerPage.value
      const end = start + itemsPerPage.value
      return filteredCA.value.slice(start, end)
    })

    const visiblePages = computed(() => {
      const pages = []
      const maxVisible = 5
      let start = Math.max(1, currentPage.value - Math.floor(maxVisible / 2))
      let end = Math.min(totalPages.value, start + maxVisible - 1)
      
      if (end - start + 1 < maxVisible) {
        start = Math.max(1, end - maxVisible + 1)
      }
      
      for (let i = start; i <= end; i++) {
        pages.push(i)
      }
      
      return pages
    })

    const statsGeneralesCalculees = computed(() => {
      if (filteredCA.value.length === 0) {
        return {
          totalChiffreAffaire: 0,
          totalEntrees: 0,
          tarifMoyen: 0,
          nombreFilms: 0
        }
      }
      
      // Utiliser les champs retournés par l'API: revenuMaximum et totalPlaces
      const totalCA = filteredCA.value.reduce((sum, ca) => sum + (ca.revenuMaximum || 0), 0)
      const totalEntrees = filteredCA.value.reduce((sum, ca) => sum + (ca.totalPlaces || 0), 0)
      const filmsUniques = new Set(filteredCA.value.map(ca => ca.salleId))
      
      return {
        totalChiffreAffaire: totalCA,
        totalEntrees,
        tarifMoyen: totalEntrees > 0 ? totalCA / totalEntrees : 0,
        nombreFilms: filmsUniques.size
      }
    })

    // Watch pour recalculer les stats quand les données changent
    watch(filteredCA, () => {
      statsGeneralesCalculees.value
    }, { deep: true })

    // Watch pour recharger les données quand la vue change
    watch(vueAffichage, () => {
      currentPage.value = 1 // Réinitialiser la pagination
      loadChiffreAffaires()
    })

    // Méthodes
    const loadChiffreAffaires = async () => {
      loading.value = true
      try {
        let endpoint
        
        if (vueAffichage.value === 'salle') {
          // Vue par salle : revenu maximum par salle
          endpoint = '/api/chiffre-affaires/revenu-max-par-salle'
        } else if (vueAffichage.value === 'film-seance') {
          // Vue par film par séance : utiliser le revenu réel basé sur les réservations
          console.log('Vue par film par séance - utilisation de /api/chiffre-affaires/revenu-reel-par-seance')
          endpoint = '/api/chiffre-affaires/revenu-reel-par-seance'
          
          // Ajouter le filtre par film si spécifié
          if (filterFilm.value) {
            const params = new URLSearchParams()
            params.append('filmTitre', filterFilm.value)
            endpoint += '?' + params.toString()
          }
        } else {
          // Vue par film : chiffre d'affaires réel par film depuis les réservations
          console.log('Vue par film - utilisation de /test-data')
          endpoint = '/api/chiffre-affaires/test-data'
        }
        
        const response = await fetch(endpoint)
        if (!response.ok) {
          console.error('Erreur HTTP:', response.status, response.statusText)
          chiffreAffaires.value = []
          return
        }
        
        const data = await response.json()
        console.log('Données reçues (' + vueAffichage.value + '):', data)
        
        // Transformer les données selon la vue
        if (vueAffichage.value === 'salle') {
          chiffreAffaires.value = Array.isArray(data) ? data.map(salle => ({
            id: salle.salleId,
            filmId: null,
            filmTitre: 'Tous films',
            dateCalcul: new Date().toISOString().split('T')[0],
            typePeriode: 'SALLE',
            chiffreAffaire: salle.revenuMaximum || 0,
            nombreEntrees: salle.totalPlaces || 0,
            tarifMoyen: salle.totalPlaces > 0 ? (salle.revenuMaximum || 0) / salle.totalPlaces : 0,
            salleNom: salle.salleNom,
            capacite: salle.capacite,
            type: salle.type,
            placesParCategorie: salle.placesParCategorie || {}
          })) : []
        } else if (vueAffichage.value === 'film-seance') {
          // Vue par film par séance : utiliser les données de revenu réel
          chiffreAffaires.value = Array.isArray(data) ? data.map(seance => ({
            id: seance.seanceId,
            dateHeure: seance.dateHeure,
            filmTitre: seance.filmTitre,
            filmId: seance.filmId,
            salleNom: seance.salleNom,
            salleId: seance.salleId,
            revenuReel: seance.revenuReel || 0,
            caPublicite: seance.caPublicite || 0,
            montantPublicitePaye: seance.montantPublicitePaye || 0,
            montantPubliciteReste: seance.montantPubliciteReste || 0,
            montantProduitsExtras: seance.montantProduitsExtras || 0,
            totalPlacesReservees: seance.totalPlacesReservees || 0,
            placesReservees: seance.totalPlacesReservees || 0, // Ajout pour la compatibilité avec le template
            placesParCategorie: seance.placesParCategorie || {},
            nombreEntrees: seance.totalPlacesReservees || 0
          })) : []
          
          console.log('🔍 DEBUG film-seance données:', chiffreAffaires.value)
          console.log('🔍 DEBUG montantProduitsExtras:', chiffreAffaires.value.map(ca => ({film: ca.filmTitre, montant: ca.montantProduitsExtras})))
        } else {
          // Vue par film : utiliser les données directement
          chiffreAffaires.value = Array.isArray(data) ? data : []
        }
        
        console.log('chiffreAffaires.value après transformation:', chiffreAffaires.value)
      } catch (error) {
        console.error('Erreur lors du chargement du chiffre d\'affaires:', error)
        chiffreAffaires.value = []
      } finally {
        loading.value = false
      }
    }

    const loadFilms = async () => {
      try {
        const response = await fetch('/api/films')
        const data = await response.json()
        films.value = data
      } catch (error) {
        console.error('Erreur lors du chargement des films:', error)
      }
    }

    const applyFilters = async () => {
      currentPage.value = 1
      // Recharger les données avec les nouveaux filtres
      await loadChiffreAffaires()
    }

    const resetFilters = () => {
      filterSalle.value = ''
      filterType.value = ''
      filterPeriode.value = ''
      dateDebut.value = ''
      dateFin.value = ''
      filterFilm.value = ''
      currentPage.value = 1
    }

    const sortBy = (field) => {
      if (sortField.value === field) {
        sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
      } else {
        sortField.value = field
        sortOrder.value = 'desc'
      }
    }

    const toggleFilters = () => {
      showFilters.value = !showFilters.value
    }

    const recalculerCA = async () => {
      try {
        // Utiliser une période très large pour inclure toutes les réservations
        const params = new URLSearchParams({
          dateDebut: '2020-01-01',  // Très large pour inclure tout
          dateFin: '2030-12-31'     // Très large pour inclure tout
        })
        
        console.log('Envoi du recalcul avec params:', params.toString())
        
        const response = await fetch(`/api/chiffre-affaires/recalculer?${params}`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          }
        })
        
        if (!response.ok) {
          console.error('Erreur HTTP lors du recalcul:', response.status, response.statusText)
          const errorText = await response.text()
          console.error('Détail erreur:', errorText)
          return
        }
        
        const result = await response.json()
        console.log('Résultat du recalcul:', result)
        
        // Recharger les données après recalcul
        await loadChiffreAffaires()
        
        console.log('Chiffre d\'affaires recalculé avec succès')
      } catch (error) {
        console.error('Erreur lors du recalcul du CA:', error)
      }
    }

    const voirDetails = (ca) => {
      selectedCA.value = ca
      detailsModal.show()
    }

    const exporterPDF = async () => {
      try {
        const params = new URLSearchParams({
          periode: filterPeriode.value,
          dateDebut: dateDebut.value,
          dateFin: dateFin.value,
          filmId: filterFilm.value
        })
        
        const response = await fetch(`/api/chiffre-affaires/exporter/pdf?${params}`)
        const blob = await response.blob()
        
        const url = window.URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = `chiffre-affaires-${new Date().toISOString().split('T')[0]}.pdf`
        document.body.appendChild(a)
        a.click()
        window.URL.revokeObjectURL(url)
        document.body.removeChild(a)
      } catch (error) {
        console.error('Erreur lors de l\'export PDF:', error)
      }
    }

    const exporterExcel = async () => {
      try {
        const params = new URLSearchParams({
          periode: filterPeriode.value,
          dateDebut: dateDebut.value,
          dateFin: dateFin.value,
          filmId: filterFilm.value
        })
        
        const response = await fetch(`/api/chiffre-affaires/exporter/excel?${params}`)
        const blob = await response.blob()
        
        const url = window.URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = `chiffre-affaires-${new Date().toISOString().split('T')[0]}.xlsx`
        document.body.appendChild(a)
        a.click()
        window.URL.revokeObjectURL(url)
        document.body.removeChild(a)
      } catch (error) {
        console.error('Erreur lors de l\'export Excel:', error)
      }
    }

    const exporterLigne = async (ca) => {
      try {
        const response = await fetch(`/api/chiffre-affaires/exporter/ligne/${ca.id}`, {
          headers: {
            'Content-Type': 'application/json'
          }
        })
        const blob = await response.blob()
        
        const url = window.URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = `ca-${ca.filmTitre}-${ca.dateCalcul}.pdf`
        document.body.appendChild(a)
        a.click()
        window.URL.revokeObjectURL(url)
        document.body.removeChild(a)
      } catch (error) {
        console.error('Erreur lors de l\'export de la ligne:', error)
      }
    }

    // Utilitaires
    const formatPrix = (prix) => {
      return new Intl.NumberFormat('fr-FR', { minimumFractionDigits: 2 }).format(prix)
    }

    const formatDate = (date) => {
      return new Date(date).toLocaleDateString('fr-FR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    }

    const formatDateTime = (dateTime) => {
      if (!dateTime) return 'Date inconnue'
      try {
        return new Date(dateTime).toLocaleString('fr-FR', {
          year: 'numeric',
          month: 'short',
          day: 'numeric',
          hour: '2-digit',
          minute: '2-digit'
        })
      } catch (error) {
        console.warn('Date invalide:', dateTime)
        return 'Date invalide'
      }
    }

    const getPeriodeBadgeClass = (periode) => {
      const classes = {
        'JOURNALIER': 'bg-primary',
        'HEBDOMADAIRE': 'bg-info',
        'MENSUEL': 'bg-success',
        'ANNUEL': 'bg-warning'
      }
      return classes[periode] || 'bg-secondary'
    }

    const getCategorieBadgeClass = (categorie) => {
      const classes = {
        'VIP': 'bg-warning',
        'PREMIUM': 'bg-info',
        'STANDARD': 'bg-secondary'
      }
      return classes[categorie] || 'bg-secondary'
    }

    const getTypeBadgeClass = (type) => {
      const classes = {
        'STANDARD': 'bg-secondary',
        'VIP': 'bg-warning',
        'PREMIUM': 'bg-info',
        'IMAX': 'bg-danger',
        '3D': 'bg-primary'
      }
      return classes[type] || 'bg-secondary'
    }

    // Lifecycle
    onMounted(() => {
      loadChiffreAffaires()
      loadFilms()
      detailsModal = new Modal(document.getElementById('detailsModal'))
    })

    return {
      chiffreAffaires,
      loading,
      showFilters,
      films,
      vueAffichage,
      filterSalle,
      filterType,
      filterTypeClient,
      filterPeriode,
      dateDebut,
      dateFin,
      filterFilm,
      sallesDisponibles,
      filmsDisponibles,
      sortField,
      sortOrder,
      currentPage,
      itemsPerPage,
      selectedCA,
      hasActiveFilters,
      activeFiltersCount,
      filteredCA,
      totalPages,
      paginatedCA,
      visiblePages,
      statsGenerales,
      totalCAPublicite,
      totalCAPublicitePayee,
      totalCAProduitsExtras,
      totalChiffreAffairesComplet,
      totalChiffreAffairesReel,
      loadChiffreAffaires,
      loadFilms,
      applyFilters,
      resetFilters,
      sortBy,
      toggleFilters,
      recalculerCA,
      voirDetails,
      exporterPDF,
      exporterExcel,
      exporterLigne,
      formatPrix,
      formatDate,
      formatDateTime,
      getPeriodeBadgeClass,
      getCategorieBadgeClass,
      getTypeBadgeClass
    }
  }
}
</script>

<style scoped>
.admin-chiffre-affaires {
  padding: 20px;
  background-color: var(--bg-secondary);
}

/* Cartes de statistiques */
.icon-box {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  border-radius: var(--radius-lg);
  font-size: 1.5rem;
  transition: all 0.15s ease-in-out;
}

.icon-box:hover {
  transform: scale(1.05);
}

/* Tableau */
.table {
  background-color: var(--bg-primary);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.table th {
  border-top: none;
  font-weight: 600;
  color: var(--text-primary);
  background-color: var(--bg-tertiary);
  padding: 1rem;
}

.table td {
  padding: 0.875rem 1rem;
  vertical-align: middle;
  border-color: var(--border-color);
}

.table tbody tr:hover {
  background-color: var(--bg-secondary);
}

/* Cards */
.card {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  background-color: var(--bg-primary);
}

.card-header {
  background-color: var(--bg-tertiary);
  border-bottom: 1px solid var(--border-color);
  padding: 1rem 1.25rem;
}

.card-body {
  padding: 1.25rem;
}

/* Formulaire */
.form-control {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 0.75rem;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  transition: all 0.15s ease-in-out;
}

.form-control:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 0.2rem rgba(37, 99, 235, 0.25);
  outline: 0;
}

.form-select {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 0.75rem;
  background-color: var(--bg-primary);
  color: var(--text-primary);
}

.form-select:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 0.2rem rgba(37, 99, 235, 0.25);
  outline: 0;
}

.form-label {
  color: var(--text-primary);
  font-weight: 500;
  margin-bottom: 0.5rem;
}

/* Modal */
.modal-content {
  border: none;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-xl);
  background-color: var(--bg-primary);
}

.modal-header {
  background-color: var(--bg-tertiary);
  border-bottom: 1px solid var(--border-color);
  padding: 1.25rem;
  border-radius: var(--radius-lg) var(--radius-lg) 0 0;
}

.modal-footer {
  background-color: var(--bg-tertiary);
  border-top: 1px solid var(--border-color);
  padding: 1.25rem;
  border-radius: 0 0 var(--radius-lg) var(--radius-lg);
}

/* Pagination */
.pagination .page-link {
  color: var(--primary-color);
  border: 1px solid var(--border-color);
  padding: 0.5rem 0.75rem;
  background-color: var(--bg-primary);
}

.pagination .page-link:hover {
  background-color: var(--bg-secondary);
  border-color: var(--primary-color);
}

.pagination .page-item.active .page-link {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
}

.pagination .page-item.disabled .page-link {
  color: var(--text-muted);
  background-color: var(--bg-secondary);
  border-color: var(--border-light);
}

/* Boutons */
.btn-primary {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
  color: white;
  border-radius: var(--radius-md);
  padding: 0.75rem 1.5rem;
  font-weight: 500;
  transition: all 0.15s ease-in-out;
}

.btn-primary:hover {
  background-color: var(--primary-dark);
  border-color: var(--primary-dark);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.btn-success {
  background-color: var(--success-color);
  border-color: var(--success-color);
  color: white;
  border-radius: var(--radius-md);
  padding: 0.75rem 1.5rem;
  font-weight: 500;
  transition: all 0.15s ease-in-out;
}

.btn-success:hover {
  background-color: var(--success-dark);
  border-color: var(--success-dark);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.btn-outline-secondary {
  color: var(--text-secondary);
  border-color: var(--border-color);
  background-color: transparent;
}

.btn-outline-secondary:hover {
  background-color: var(--secondary-color);
  border-color: var(--secondary-color);
  color: white;
}

.btn-outline-primary {
  color: var(--primary-color);
  border-color: var(--primary-color);
  background-color: transparent;
}

.btn-outline-primary:hover {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
  color: white;
}

.btn-outline-warning {
  color: var(--warning-color);
  border-color: var(--warning-color);
  background-color: transparent;
}

.btn-outline-warning:hover {
  background-color: var(--warning-color);
  border-color: var(--warning-color);
  color: white;
}

.btn-group .btn {
  padding: 0.375rem 0.5rem;
  border-radius: var(--radius-md);
  border-color: var(--border-color);
}

.btn-group .btn:hover {
  background-color: var(--bg-secondary);
}

/* Badge */
.badge {
  font-size: 0.75em;
  padding: 0.375rem 0.75rem;
  border-radius: var(--radius-full);
  font-weight: 500;
}

/* Spinner */
.spinner-border {
  border-color: var(--primary-color) transparent var(--primary-color) transparent;
}

/* Dropdown */
.dropdown-menu {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  background-color: var(--bg-primary);
}

.dropdown-item {
  color: var(--text-primary);
  padding: 0.5rem 1rem;
  transition: all 0.15s ease-in-out;
}

.dropdown-item:hover {
  background-color: var(--bg-secondary);
  color: var(--text-primary);
}

/* Responsive */
@media (max-width: 768px) {
  .admin-chiffre-affaires {
    padding: 1rem;
  }
  
  .table-responsive {
    font-size: 0.875rem;
  }
  
  .btn-group .btn {
    padding: 0.25rem 0.375rem;
  }
}
</style>
