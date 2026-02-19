import axios from 'axios'

// Toast notifications utility
export const toast = {
  success: (message, title) => {
    if (window.Vue && window.Vue.prototype.$root) {
      window.Vue.prototype.$root.showToast(message, 'success', title)
    }
  },
  error: (message, title) => {
    if (window.Vue && window.Vue.prototype.$root) {
      window.Vue.prototype.$root.showToast(message, 'error', title)
    }
  },
  warning: (message, title) => {
    if (window.Vue && window.Vue.prototype.$root) {
      window.Vue.prototype.$root.showToast(message, 'warning', title)
    }
  },
  info: (message, title) => {
    if (window.Vue && window.Vue.prototype.$root) {
      window.Vue.prototype.$root.showToast(message, 'info', title)
    }
  }
}

const API_BASE_URL = 'http://localhost:8080/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Services pour les films
export const filmService = {
  getAllFilms: () => api.get('/cinema/films'),
  getFilm: (id) => api.get(`/cinema/films/${id}`),
  createFilm: (film) => api.post('/films', film),
  updateFilm: (id, film) => api.put(`/films/${id}`, film),
  deleteFilm: (id) => api.delete(`/films/${id}`)
}

// Services pour les séances
export const seanceService = {
  getAllSeances: () => api.get('/seances'),
  rechercherSeances: (params) => api.get('/cinema/rechercher', { params }),
  getSeance: (filmId, date, heure, salleId) =>
    api.get('/cinema/seance', {
      params: { filmId, date, heure, salleId }
    }),
  createSeance: (seance) => api.post('/seances', seance),
  updateSeance: (id, seance) => api.put(`/seances/${id}`, seance),
  deleteSeance: (id) => api.delete(`/seances/${id}`)
}

// Services pour les réservations
export const reservationService = {
  getAllReservations: () => api.get('/reservations'),
  getReservation: (id) => api.get(`/reservations/${id}`),
  getReservationsByClient: (email) => api.get(`/reservations/client/${email}`),
  getReservationsParSeance: (seanceId) => api.get(`/reservations/seance/${seanceId}`),
  reserverPlaces: (reservationData) => api.post('/reservations', reservationData),
  createReservation: (reservation) => api.post('/reservations', reservationData),
  modifyReservation: (id, modificationData) => api.put(`/reservations/${id}/modifier`, modificationData),
  cancelReservation: (id) => api.put(`/reservations/${id}/annuler`),
  useReservation: (id) => api.put(`/reservations/${id}/utiliser`),
  deleteReservation: (id) => api.delete(`/reservations/${id}`)
}

// Services pour les salles
export const salleService = {
  getAllSalles: () => api.get('/cinema/salles'),
  getSalle: (id) => api.get(`/salles/${id}`),
  createSalle: (salle) => api.post('/salles', salle),
  updateSalle: (id, salle) => api.put(`/salles/${id}`, salle),
  deleteSalle: (id) => api.delete(`/salles/${id}`)
}

// Services pour les places
export const placeService = {
  getPlacesParSalle: (salleId) => api.get(`/places/salle/${salleId}`),
  getPlacesDisponiblesPourSeance: (seanceId) => api.get(`/places/seance/${seanceId}/disponibles`),
  initialiserPlacesSalle: (salleId) => api.post(`/places/salle/${salleId}/initialiser`),
  configurerPlacesSalle: (salleId, configuration) => api.post(`/places/salle/${salleId}/configurer`, configuration)
}

// Services d'information générale
export const cinemaService = {
  getInfo: () => api.get('/cinema/info'),
  getFilms: () => api.get('/cinema/films'),
  getSalles: () => api.get('/cinema/salles')
}

// Service d'upload de fichiers
export const uploadService = {
  uploadImage: (file) => {
    const formData = new FormData()
    formData.append('file', file)

    return axios.post(`${API_BASE_URL}/upload/image`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}

// Services pour les tarifs
export const tarifService = {
  getAllTarifs: () => api.get('/tarifs'),
  getTarif: (id) => api.get(`/tarifs/${id}`),
  createTarif: (tarif) => api.post('/tarifs', tarif),
  updateTarif: (id, tarif) => api.put(`/tarifs/${id}`, tarif),
  deleteTarif: (id) => api.delete(`/tarifs/${id}`)
}

// Services pour les billets
export const billetService = {
  getBilletsByReservation: (reservationId) => api.get(`/billets/reservation/${reservationId}`),
  getBilletById: (id) => api.get(`/billets/${id}`),
  downloadBilletPdf: (id) => api.get(`/billets/${id}/pdf`, { responseType: 'blob' }),
  utiliserBillet: (id) => api.post(`/billets/${id}/utiliser`),
}

// Services pour les factures
export const factureService = {
  getFactureByReservation: (reservationId) => api.get(`/factures/reservation/${reservationId}`),
  getFactureById: (id) => api.get(`/factures/${id}`),
  downloadFacturePdf: (id) => api.get(`/factures/${id}/pdf`, { responseType: 'blob' }),
  getFacturesByClient: (email) => api.get(`/factures/client/${email}`),
}

export default api
