// Services pour la gestion des produits extras

const API_BASE_URL = '/api/produits-extras'

export const produitExtraService = {
  // Retour: Promise<ProduitExtra[]>
  getAllProduits() {
    console.log('🔍 Appel API produits-extras:', `${API_BASE_URL}`)
    return fetch(`${API_BASE_URL}`)
      .then(response => {
        console.log('📡 Réponse API produits-extras:', response.status, response.statusText)
        if (!response.ok) {
          throw new Error('Erreur lors de la récupération des produits')
        }
        return response.json()
      })
  },

  // Retour: Promise<ProduitExtra>
  createProduit(produit) {
    return fetch(`${API_BASE_URL}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(produit)
    })
    .then(response => {
      if (!response.ok) {
        return response.text().then(text => {
          throw new Error(text || 'Erreur lors de la création du produit')
        })
      }
      return response.json()
    })
  },

  // Retour: Promise<ProduitExtra>
  updateProduit(id, produit) {
    return fetch(`${API_BASE_URL}/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(produit)
    })
    .then(response => {
      if (!response.ok) {
        return response.text().then(text => {
          throw new Error(text || 'Erreur lors de la mise à jour du produit')
        })
      }
      return response.json()
    })
  },

  // Retour: Promise<void>
  deleteProduit(id) {
    return fetch(`${API_BASE_URL}/${id}`, {
      method: 'DELETE'
    })
    .then(response => {
      if (!response.ok) {
        return response.text().then(text => {
          throw new Error(text || 'Erreur lors de la suppression du produit')
        })
      }
    })
  },

  // Retour: Promise<ProduitExtra>
  getProduitById(id) {
    return fetch(`${API_BASE_URL}/${id}`)
      .then(response => {
        if (!response.ok) {
          throw new Error('Erreur lors de la récupération du produit')
        }
        return response.json()
      })
  }
}
