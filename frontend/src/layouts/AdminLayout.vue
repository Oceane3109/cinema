<template>
  <div class="admin-layout">
    <AdminSidebar />
    <div class="main-content">
      <div class="container-fluid">
        <slot />
      </div>
    </div>
  </div>
</template>

<script>
import AdminSidebar from '../components/AdminSidebar.vue'

export default {
  name: 'AdminLayout',
  components: {
    AdminSidebar
  }
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}

.main-content {
  flex: 1;
  margin-left: 250px;
  padding: 2rem;
  background-color: #f8f9fa;
  min-height: 100vh;
}
</style>
