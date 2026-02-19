<template>
  <div class="container mt-4">
    <PubChiffreAffaire />
  </div>
</template>

<script>
import PubChiffreAffaire from '../../components/admin/PubChiffreAffaire.vue'

export default {
  name: 'PubChiffreAffairePage',
  components: {
    PubChiffreAffaire
  }
}
</script>

<style scoped>
.container {
  max-width: 1200px;
}
</style>
