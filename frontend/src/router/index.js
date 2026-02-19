import { createRouter, createWebHistory } from 'vue-router'
import Accueil from '../views/Accueil.vue'
import Seances from '../views/Seances.vue'
import Reservation from '../views/Reservation.vue'

const routes = [
  {
    path: '/',
    name: 'Accueil',
    component: Accueil
  },
  {
    path: '/film/:filmId/seances',
    name: 'FilmSeances',
    component: () => import('../views/FilmSeances.vue'),
    props: true
  },
  {
    path: '/seances',
    name: 'Seances',
    component: Seances
  },
  {
    path: '/reservation/:seanceId?',
    name: 'Reservation',
    component: Reservation,
    props: true
  },
  {
    path: '/mes-reservations',
    name: 'MesReservations',
    component: () => import('../views/MesReservations.vue')
  },
  {
    path: '/admin',
    redirect: '/admin/films'
  },
  {
    path: '/admin/films',
    name: 'AdminFilms',
    component: () => import('../views/admin/AdminFilmsPage.vue')
  },
  {
    path: '/admin/salles',
    name: 'AdminSalles',
    component: () => import('../views/admin/AdminSallesPage.vue')
  },
  {
    path: '/admin/seances',
    name: 'AdminSeances',
    component: () => import('../views/admin/AdminSeancesPage.vue')
  },
  {
    path: '/admin/reservations',
    name: 'AdminReservations',
    component: () => import('../views/admin/AdminReservationsPage.vue')
  },
  {
    path: '/admin/tarifs',
    name: 'AdminTarifs',
    component: () => import('../views/admin/AdminTarifsPage.vue')
  },
  {
    path: '/admin/chiffre-affaires',
    name: 'AdminChiffreAffaires',
    component: () => import('../views/admin/AdminChiffreAffairesPage.vue')
  },
  {
    path: '/admin/pub-chiffre-affaire',
    name: 'AdminPubChiffreAffaireOld',
    component: () => import('../views/admin/PubChiffreAffairePage.vue')
  },
  {
    path: '/admin/PubChiffreAffaire',
    name: 'PubChiffreAffaire',
    component: () => import('../views/admin/PubChiffreAffairePage.vue')
  },
  {
    path: '/admin/pub-gestion',
    name: 'PubGestionComplete',
    component: () => import('../views/admin/PubGestionComplete.vue')
  },
  {
    path: '/admin/statistiques',
    name: 'AdminStatistiques',
    component: () => import('../views/admin/AdminStatistiquesPage.vue')
  },
  {
    path: '/admin/parametres',
    name: 'AdminParametres',
    component: () => import('../views/admin/AdminParametresPage.vue')
  },
  {
    path: '/admin/produits-extras',
    name: 'AdminProduitsExtras',
    component: () => import('../views/admin/ProduitExtraPage.vue')
  },
  {
    path: '/admin/vente-produits-extras',
    name: 'AdminVenteProduitsExtras',
    component: () => import('../views/admin/VenteProduitPage.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
