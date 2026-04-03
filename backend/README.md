# Backend Gestion Cinéma

Backend simple de l'application de gestion de cinéma développé avec Spring Boot.

## Technologies utilisées

- **Spring Boot 3.2.0** : Framework principal
- **Java 17** : Version du langage
- **PostgreSQL** : Base de données
- **Spring Data JPA** : Accès aux données
- **Lombok** : Réduction du code répétitif

## Structure du projet

```
src/
├── main/
│   ├── java/com/cinema/
│   │   ├── controller/      # Contrôleurs REST
│   │   ├── model/           # Entités JPA
│   │   └── repository/      # Accès aux données
│   └── resources/
│       └── application.properties # Configuration
└── test/                    # Tests
```

## Configuration requise

### Base de données PostgreSQL

Créer une base de données PostgreSQL :
```sql
CREATE DATABASE cinema;
```

## Démarrage de l'application

```bash
mvn spring-boot:run
```

L'application sera disponible sur : http://localhost:8080

## API REST

### 🎬 Gestion des Films

- `GET /api/films` : Liste de tous les films
- `GET /api/films/{id}` : Détails d'un film
- `POST /api/films` : Créer un nouveau film
- `PUT /api/films/{id}` : Modifier un film
- `DELETE /api/films/{id}` : Supprimer un film

### 🏢 Gestion des Salles

- `GET /api/salles` : Liste de toutes les salles
- `GET /api/salles/{id}` : Détails d'une salle
- `GET /api/salles/nom/{nom}` : Salle par nom
- `GET /api/salles/type/{type}` : Salles par type
- `GET /api/salles/capacite/{capacite}` : Salles avec capacité minimale
- `POST /api/salles` : Créer une nouvelle salle
- `PUT /api/salles/{id}` : Modifier une salle
- `DELETE /api/salles/{id}` : Supprimer une salle

### 🎭 Gestion des Séances

- `GET /api/seances` : Liste de toutes les séances
- `GET /api/seances/{id}` : Détails d'une séance
- `GET /api/seances/film/{filmId}` : Séances d'un film
- `GET /api/seances/salle/{salleId}` : Séances d'une salle
- `GET /api/seances/date/{date}` : Séances à une date
- `GET /api/seances/film/{filmId}/date/{date}` : Séances d'un film à une date
- `GET /api/seances/disponibles/film/{filmId}/date/{date}/places/{nbPlaces}` : Séances disponibles
- `GET /api/seances/aujourdhui` : Séances d'aujourd'hui
- `GET /api/seances/avenir` : Séances futures
- `POST /api/seances` : Créer une nouvelle séance
- `PUT /api/seances/{id}` : Modifier une séance
- `DELETE /api/seances/{id}` : Supprimer une séance

### 🎫 Gestion des Réservations

- `GET /api/reservations` : Liste de toutes les réservations
- `GET /api/reservations/{id}` : Détails d'une réservation
- `GET /api/reservations/reference/{reference}` : Réservation par référence
- `GET /api/reservations/client/{email}` : Réservations d'un client
- `GET /api/reservations/seance/{seanceId}` : Réservations d'une séance
- `GET /api/reservations/recente` : Réservations récentes
- `POST /api/reservations` : Créer une nouvelle réservation
- `PUT /api/reservations/{id}/annuler` : Annuler une réservation
- `PUT /api/reservations/{id}/utiliser` : Marquer comme utilisée
- `DELETE /api/reservations/{id}` : Supprimer une réservation
- `GET /api/reservations/stats` : Statistiques des réservations

### 🎭 API Cinéma (Principale)

- `GET /api/cinema/rechercher?filmId=1&date=2024-01-10&nombrePlaces=2` : Rechercher des séances
- `POST /api/cinema/reserver` : Réserver des places
- `GET /api/cinema/seance?filmId=1&date=2024-01-10&heure=10:00&salleId=1` : Trouver une séance spécifique
- `GET /api/cinema/films` : Films disponibles
- `GET /api/cinema/salles` : Salles disponibles
- `GET /api/cinema/info` : Informations générales

## Exemples d'utilisation

### Réserver des places pour Avatar le 10 janvier à 10h en salle 1

```bash
# 1. Trouver la séance
GET /api/cinema/seance?filmId=1&date=2024-01-10&heure=10:00&salleId=1

# 2. Réserver des places
POST /api/cinema/reserver
Content-Type: application/json

{
  "seanceId": 1,
  "nomClient": "Jean Dupont",
  "emailClient": "jean@example.com",
  "telephoneClient": "06 12 34 56 78",
  "nombrePlaces": 2
}
```

### Rechercher des séances disponibles

```bash
# Toutes les séances disponibles pour un film et une date
GET /api/cinema/rechercher?filmId=1&date=2024-01-10&nombrePlaces=2

# Séances disponibles aujourd'hui
GET /api/seances/aujourdhui
```

### Créer une nouvelle séance

```json
POST /api/seances
{
  "film": {"id": 1},
  "salle": {"id": 1},
  "dateHeure": "2024-01-15T14:00:00",
  "prix": 12.50
}
```

## Tests

```bash
mvn test
```
