<template>
  <div class="seat-selector">
    <!-- Écran de cinéma simple -->
    <div class="cinema-screen">
      <div class="screen-rectangle">
        <span class="screen-text">ÉCRAN</span>
      </div>
    </div>

    <div class="seats-container">
      <div v-for="row in rows" :key="row" class="row">
        <div class="row-label">{{ row }}</div>
        <!-- Places de gauche (1-6) -->
        <div
          v-for="seat in leftSeats"
          :key="seat"
          class="seat"
          :class="getSeatClass(row, seat)"
          @click="toggleSeat(row, seat)"
        >
          {{ seat }}
        </div>
        <!-- Espace central (passage) -->
        <div class="aisle-space"></div>
        <!-- Places de droite (7-15) -->
        <div
          v-for="seat in rightSeats"
          :key="seat"
          class="seat"
          :class="getSeatClass(row, seat)"
          @click="toggleSeat(row, seat)"
        >
          {{ seat }}
        </div>
      </div>
    </div>

    <div class="legend mt-3">
      <div class="legend-section">
        <h6 class="legend-title">Statut</h6>
        <div class="legend-items">
          <div class="legend-item">
            <div class="seat seat-available seat-standard"></div>
            <span>Disponible</span>
          </div>
          <div class="legend-item">
            <div class="seat seat-selected seat-standard"></div>
            <span>Sélectionnée</span>
          </div>
          <div class="legend-item">
            <div class="seat seat-occupied seat-standard"></div>
            <span>Réservée</span>
          </div>
        </div>
      </div>
      
      <div class="legend-section">
        <h6 class="legend-title">Catégories</h6>
        <div class="legend-items">
          <div class="legend-item">
            <div class="seat seat-available seat-vip"></div>
            <span>VIP</span>
          </div>
          <div class="legend-item">
            <div class="seat seat-available seat-premium"></div>
            <span>Premium</span>
          </div>
          <div class="legend-item">
            <div class="seat seat-available seat-standard"></div>
            <span>Standard</span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="selectedSeats.length > 0" class="selected-seats mt-3">
      <h6>Places sélectionnées ({{ selectedSeats.length }}) :</h6>
      <div class="selected-seats-list">
        <span
          v-for="seat in selectedSeats"
          :key="seat.id"
          class="selected-seat-badge"
        >
          Rangée {{ seat.row }}, Place {{ seat.seat }}
        </span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SeatSelector',
  props: {
    salle: {
      type: Object,
      required: true
    },
    occupiedSeats: {
      type: Array,
      default: () => []
    },
    allSeats: {
      type: Array,
      default: () => []
    },
    maxSelection: {
      type: Number,
      default: 10
    }
  },
  data() {
    return {
      selectedSeats: []
    }
  },
  computed: {
    rows() {
      return this.salle.nombreRangees || 10;
    },
    seatsPerRow() {
      return this.salle.placesParRangee || 15;
    },
    leftSeats() {
      // Places 1-6 (côté gauche)
      return Array.from({length: Math.min(6, this.seatsPerRow)}, (_, i) => i + 1);
    },
    rightSeats() {
      // Places 7+ (côté droite)
      const startSeat = 7;
      const endSeat = Math.max(7, this.seatsPerRow);
      const count = Math.max(0, endSeat - startSeat + 1);
      return Array.from({length: count}, (_, i) => startSeat + i);
    },
    generatedSeats() {
      const seats = [];
      for (let row = 1; row <= this.rows; row++) {
        for (let seat = 1; seat <= this.seatsPerRow; seat++) {
          seats.push({
            id: `R${row}P${seat}`,
            row,
            seat,
            isOccupied: this.isSeatOccupied(row, seat)
          });
        }
      }
      return seats;
    }
  },
  methods: {
    getSeatCategory(row, seat) {
      // Trouver la place correspondante dans allSeats pour obtenir sa catégorie
      const place = this.allSeats.find(p => 
        p.numeroRangee === row && p.numeroPlace === seat
      );
      return place ? place.categorie : 'STANDARD'; // Par défaut STANDARD
    },
    
    isSeatOccupied(row, seat) {
      return this.occupiedSeats.some(occupied =>
        occupied.numeroRangee === row && occupied.numeroPlace === seat
      );
    },
    isSeatSelected(row, seat) {
      return this.selectedSeats.some(selected =>
        selected.row === row && selected.seat === seat
      );
    },
    getSeatClass(row, seat) {
      const category = this.getSeatCategory(row, seat);
      
      if (this.isSeatOccupied(row, seat)) {
        return `seat-occupied seat-${category.toLowerCase()}`;
      } else if (this.isSeatSelected(row, seat)) {
        return `seat-selected seat-${category.toLowerCase()}`;
      } else {
        return `seat-available seat-${category.toLowerCase()}`;
      }
    },
    toggleSeat(row, seat) {
      if (this.isSeatOccupied(row, seat)) {
        return; // Ne rien faire si la place est occupée
      }

      const seatIndex = this.selectedSeats.findIndex(selected =>
        selected.row === row && selected.seat === seat
      );

      if (seatIndex > -1) {
        // Désélectionner la place
        this.selectedSeats.splice(seatIndex, 1);
      } else {
        // Vérifier la limite de sélection
        if (this.selectedSeats.length >= this.maxSelection) {
          this.$emit('max-selection-reached');
          return;
        }
        // Sélectionner la place
        this.selectedSeats.push({ row, seat });
      }

      this.$emit('seats-changed', this.selectedSeats);
    }
  },
  watch: {
    occupiedSeats: {
      handler() {
        // Désélectionner automatiquement les places qui deviennent occupées
        this.selectedSeats = this.selectedSeats.filter(selected =>
          !this.isSeatOccupied(selected.row, selected.seat)
        );
        this.$emit('seats-changed', this.selectedSeats);
      },
      deep: true
    }
  }
}
</script>

<style scoped>
.seat-selector {
  max-width: 800px;
  margin: 0 auto;
}

.cinema-screen {
  text-align: center;
  margin-bottom: 30px;
}

.screen-rectangle {
  width: 80%;
  height: 40px;
  background: #2c3e50;
  border: 2px solid #4a5568;
  border-radius: 4px;
  margin: 0 auto;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
}

.screen-text {
  color: #fff;
  font-weight: bold;
  font-size: 14px;
  letter-spacing: 2px;
}

.seats-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 20px;
}

.row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.row-label {
  width: 30px;
  text-align: center;
  color: #fff;
  font-size: 12px;
  font-weight: bold;
}

.aisle-space {
  width: 80px; /* Espace plus large pour le passage */
  height: 30px;
  background: linear-gradient(135deg, rgba(255,255,255,0.08) 0%, rgba(255,255,255,0.03) 100%);
  border-radius: 6px;
  margin: 0 15px;
  border: 1px solid rgba(255,255,255,0.15);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.aisle-space::before {
  content: "PASSAGE";
  color: rgba(255,255,255,0.7);
  font-size: 9px;
  font-weight: bold;
  letter-spacing: 1px;
  text-shadow: 0 1px 2px rgba(0,0,0,0.7);
}

.seat {
  width: 30px;
  height: 30px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
}

.seat-available {
  background-color: #28a745;
  color: white;
  border: 1px solid #218838;
}

.seat-available:hover {
  background-color: #218838;
  transform: scale(1.05);
}

.seat-selected {
  background-color: #007bff;
  color: white;
  border: 1px solid #0056b3;
  animation: pulse 1s infinite;
}

.seat-occupied {
  background-color: #6c757d;
  color: #fff;
  border: 1px solid #545b62;
  cursor: not-allowed;
}

/* Couleurs par catégorie */
/* VIP - Rouge */
.seat-vip.seat-available {
  background: linear-gradient(135deg, #e74c3c, #c0392b);
  color: white;
  border: 1px solid #a93226;
  box-shadow: 0 2px 4px rgba(231, 76, 60, 0.3);
}

.seat-vip.seat-available:hover {
  background: linear-gradient(135deg, #c0392b, #a93226);
  transform: scale(1.05);
  box-shadow: 0 4px 8px rgba(231, 76, 60, 0.5);
}

.seat-vip.seat-selected {
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
  border: 1px solid #2471a3;
  box-shadow: 0 0 10px rgba(52, 152, 219, 0.5);
}

.seat-vip.seat-occupied {
  background: linear-gradient(135deg, #7f8c8d, #566573);
  color: #fff;
  border: 1px solid #5d6d7e;
}

/* Premium - Violet */
.seat-premium.seat-available {
  background: linear-gradient(135deg, #9b59b6, #8e44ad);
  color: white;
  border: 1px solid #7d3c98;
  box-shadow: 0 2px 4px rgba(155, 89, 182, 0.3);
}

.seat-premium.seat-available:hover {
  background: linear-gradient(135deg, #8e44ad, #7d3c98);
  transform: scale(1.05);
  box-shadow: 0 4px 8px rgba(155, 89, 182, 0.5);
}

.seat-premium.seat-selected {
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
  border: 1px solid #2471a3;
  box-shadow: 0 0 10px rgba(52, 152, 219, 0.5);
}

.seat-premium.seat-occupied {
  background: linear-gradient(135deg, #566573, #34495e);
  color: #fff;
  border: 1px solid #2c3e50;
}

/* Standard - Vert */
.seat-standard.seat-available {
  background: linear-gradient(135deg, #27ae60, #229954);
  color: white;
  border: 1px solid #1e8449;
  box-shadow: 0 2px 4px rgba(39, 174, 96, 0.3);
}

.seat-standard.seat-available:hover {
  background: linear-gradient(135deg, #229954, #1e8449);
  transform: scale(1.05);
  box-shadow: 0 4px 8px rgba(39, 174, 96, 0.5);
}

.seat-standard.seat-selected {
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
  border: 1px solid #2471a3;
  animation: pulse 1s infinite;
}

.seat-standard.seat-occupied {
  background-color: #6c757d;
  color: #fff;
  border: 1px solid #545b62;
  cursor: not-allowed;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.legend {
  display: flex;
  justify-content: center;
  gap: 30px;
  flex-wrap: wrap;
}

.legend-section {
  text-align: center;
}

.legend-title {
  font-size: 12px;
  font-weight: bold;
  color: #000;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.legend-items {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
  justify-content: center;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: #000;
}

.legend-item .seat {
  width: 20px;
  height: 20px;
  font-size: 9px;
  cursor: default;
}

.selected-seats {
  background-color: rgba(0, 123, 255, 0.1);
  border: 1px solid #007bff;
  border-radius: 8px;
  padding: 15px;
}

.selected-seats h6 {
  color: #fff;
  margin-bottom: 10px;
}

.selected-seats-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.selected-seat-badge {
  background-color: #007bff;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

/* Responsive */
@media (max-width: 768px) {
  .cinema-screen {
    margin-bottom: 25px;
  }

  .screen-rectangle {
    height: 35px;
  }

  .screen-text {
    font-size: 12px;
    letter-spacing: 1px;
  }

  .seat {
    width: 25px;
    height: 25px;
    font-size: 10px;
  }

  .row-label {
    width: 25px;
    font-size: 11px;
  }

  .aisle-space {
    width: 60px;
    margin: 0 8px;
  }

  .aisle-space::before {
    font-size: 7px;
  }

  .legend {
    gap: 15px;
  }
}
</style>