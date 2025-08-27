<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { auth } from './firebase'
import { onIdTokenChanged, User } from 'firebase/auth'
import Login from './components/Login.vue'
import DashboardLayout from './components/layout/DashboardLayout.vue'

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
  </div>
  
</template>

<style>
/* global styles can go here if needed */
</style>
