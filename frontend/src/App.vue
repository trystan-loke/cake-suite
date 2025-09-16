<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { auth } from './firebase'
import { onIdTokenChanged, User } from 'firebase/auth'
import Login from './components/Login.vue'
import DashboardLayout from './components/layout/DashboardLayout.vue'
import { globalNotifications } from './composables/notifications'

const user = ref<User | null>(null)

onMounted(() => {
  onIdTokenChanged(auth, async (u) => {
    user.value = u
  })
})
</script>

<template>
  <div style="min-height: 100vh;">
    <Login v-if="!user" />
    <DashboardLayout v-else>
      <router-view />
    </DashboardLayout>
    
    <!-- Global Notifications -->
    <div class="notification-container">
      <v-snackbar
        v-for="(notification, index) in globalNotifications.notifications"
        :key="index"
        :color="notification.type"
        :timeout="notification.timeout"
        :model-value="true"
        location="top"
        class="mt-4"
        style="margin-top: calc(var(--v-layout-top) + 8px);"
      >
        {{ notification.message }}
      </v-snackbar>
    </div>
  </div>
</template>

<style>
/* global styles can go here if needed */
</style>
