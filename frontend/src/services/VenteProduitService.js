// Services pour la gestion des ventes de produits extras

const API_BASE_URL = '/api/ventes-produits'

export const venteProduitService = {
  // Retour: Promise<VenteProduit>
  createVente(vente) {
    return fetch(`${API_BASE_URL}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(vente)
    })
    .then(response => {
      if (!response.ok) {
        return response.text().then(text => {
          throw new Error(text || 'Erreur lors de l\'enregistrement de la vente')
        })
      }
      return response.json()
    })
  },

  // Retour: Promise<VenteProduit[]>
  getVentesBySeance(idSeance) {
    return fetch(`${API_BASE_URL}/seance/${idSeance}`)
      .then(response => {
        if (!response.ok) {
          throw new Error('Erreur lors de la récupération des ventes de la séance')
        }
        return response.json()
      })
  },

  // Retour: Promise<VenteProduit[]>
  getVentesByDate(date) {
    return fetch(`${API_BASE_URL}/jour/${date}`)
      .then(response => {
        if (!response.ok) {
          throw new Error('Erreur lors de la récupération des ventes de la date')
        }
        return response.json()
      })
  },

  // Retour: Promise<BigDecimal>
  getMontantTotalBySeance(idSeance) {
    return fetch(`${API_BASE_URL}/total/seance/${idSeance}`)
      .then(response => {
        if (!response.ok) {
          throw new Error('Erreur lors de la récupération du montant total')
        }
        return response.json()
      })
  },

  // Retour: Promise<VenteProduit[]>
  getVentesDuJour() {
    return fetch(`${API_BASE_URL}/du-jour`)
      .then(response => {
        if (!response.ok) {
          throw new Error('Erreur lors de la récupération des ventes du jour')
        }
        return response.json()
      })
  },

  // Retour: Promise<BigDecimal>
  getChiffreAffairesDuJour() {
    return fetch(`${API_BASE_URL}/chiffre-affaires-du-jour`)
      .then(response => {
        if (!response.ok) {
          throw new Error('Erreur lors de la récupération du chiffre d\'affaires du jour')
        }
        return response.json()
      })
  }
}
