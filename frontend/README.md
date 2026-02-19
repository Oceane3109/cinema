# 🎬 Cinéma Management - Frontend

Interface utilisateur moderne et élégante pour le système de gestion de cinéma.

## ✨ Fonctionnalités Principales

### 🧭 Navigation Professionnelle

#### Navbar Client
- **Design moderne** avec effets de verre (glassmorphism)
- **Logo animé** avec effets de glow
- **Navigation fluide** avec indicateurs actifs
- **Barre de progression** du scroll
- **Actions utilisateur** (notifications, compte)
- **Responsive** complet mobile/desktop

#### Navbar Admin
- **Interface administrative** dédiée
- **Statistiques en temps réel** dans le dropdown
- **Notifications** pour réservations en attente
- **Menu déroulant** avec options utilisateur
- **Effets visuels** premium
- **Navigation rapide** vers tous les modules

### 🎨 Design System

#### Animations & Effets
- **Transitions fluides** CSS avec cubic-bezier
- **Hover effects** avec transformations 3D
- **Scroll progress** animé
- **Pulse animations** pour les badges
- **Glassmorphism** moderne

#### Palette de Couleurs
```css
Client: Dégradé violet-bleu (#667eea → #764ba2)
Admin: Dégradé bleu-violet (#1e3a8a → #581c87)
Accents: Dégradé coloré (#ff6b6b → #4ecdc4)
```

### 📱 Responsive Design
- **Breakpoints** optimisés (576px, 768px, 992px, 1200px)
- **Mobile-first** approach
- **Collapse menu** élégant
- **Touch-friendly** interactions

### 🔔 Système de Notifications
- **Toasts élégants** avec glassmorphism
- **Types**: Succès, Erreur, Avertissement, Info
- **Auto-dismiss** configurable
- **Animations** d'entrée/sortie
- **API globale** accessible partout

## 🚀 Utilisation

### Toasts Notifications
```javascript
import { toast } from '@/services/api'

// Messages de succès
toast.success('Réservation confirmée !', 'Succès')

// Messages d'erreur
toast.error('Erreur lors de la réservation', 'Erreur')

// Messages d'information
toast.info('Votre session expire bientôt', 'Info')

// Messages d'avertissement
toast.warning('Places limitées disponibles', 'Attention')
```

### Navigation
- **Routes automatiques** avec indicateurs actifs
- **Scroll tracking** en temps réel
- **Animations** fluides entre les pages
- **États visuels** pour la navigation

## 🛠️ Technologies

- **Vue.js 3** - Framework réactif
- **Bootstrap 5** - Framework CSS
- **CSS3** - Animations et effets modernes
- **JavaScript ES6+** - Logique interactive
- **Axios** - Requêtes HTTP

## 📁 Structure

```
frontend/src/
├── components/
│   ├── AdminHeader.vue      # Navbar admin professionnelle
│   └── ...
├── views/
│   ├── admin/               # Pages admin
│   └── ...
├── services/
│   └── api.js              # API + utilitaires toasts
├── assets/
│   └── app.css             # Styles globaux
├── App.vue                 # Navbar client + toasts
└── router/
    └── index.js            # Configuration routes
```

## 🎯 Fonctionnalités Avancées

### 🔍 Filtres & Recherche
- **Films**: Par genre, note, durée
- **Salles**: Par type, capacité
- **Séances**: Par film, salle, prix, date
- **Réservations**: Par statut, période, montant

### 📊 Dashboard Admin
- **Statistiques temps réel**
- **Notifications push**
- **Actions rapides**
- **Navigation intelligente**

### 🎨 UI/UX
- **Micro-interactions**
- **Feedback visuel**
- **Animations cohérentes**
- **Accessibilité** (ARIA, contrastes)

---

**🎭 Interface élégante pour une expérience cinéma premium !**

## Installation et démarrage

### Prérequis
- Node.js (version 16 ou supérieure)
- Backend Spring Boot en cours d'exécution sur `http://localhost:8080`

### Installation
```bash
# Aller dans le dossier frontend
cd frontend

# Installer les dépendances
npm install

# Démarrer le serveur de développement
npm run dev
```

L'application sera accessible sur `http://localhost:5173`

### Build pour la production
```bash
# Build pour la production
npm run build

# Prévisualiser la build
npm run preview
```


