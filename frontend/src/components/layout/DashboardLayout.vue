<template>
  <v-card>
    <v-layout>
      <v-app-bar color="primary">
        <v-app-bar-nav-icon variant="text" @click.stop="drawer = !drawer"></v-app-bar-nav-icon>
        <v-toolbar-title>Cake Suite</v-toolbar-title>
        <v-menu :offset="[-15, -25]" location="bottom" transition="slide-y-transition">
          <template v-slot:activator="{ props }">
            <v-btn icon="$dotsVertical" variant="text" v-bind="props"></v-btn>
          </template>

          <v-list>
            <v-list-item @click="handleLogout">
              <v-list-item-title>Logout</v-list-item-title>
            </v-list-item>
          </v-list>
        </v-menu>
      </v-app-bar>

      <v-navigation-drawer v-model="drawer" temporary>
        <v-list nav>
          <v-list-item
            v-for="item in items"
            :key="item.value"
            :title="item.title"
            :to="item.to"
            @click="drawer = false"
          />
        </v-list>
      </v-navigation-drawer>

      <v-main>
        <router-view />
      </v-main>
    </v-layout>
  </v-card>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { auth } from '../../firebase'
import { signOut } from 'firebase/auth'

const drawer = ref<boolean>(false)

const router = useRouter()

const items = computed(() =>
  router
    .getRoutes()
    .filter(r => r.meta?.nav && r.name)
    .sort((a, b) => Number(a.meta?.order ?? 0) - Number(b.meta?.order ?? 0))
    .map(r => ({
      title: (r.meta?.title as string) ?? String(r.name),
      value: String(r.name),
      to: { name: r.name as string },
    }))
)

const handleLogout = async () => {
  try {
    await signOut(auth)
  } catch (error) {
    console.error('Logout error:', error)
  }
}

// auto-close drawer on route change
watch(() => router.currentRoute.value, () => {
  drawer.value = false
})
</script>

<style scoped>
.v-layout {
  min-height: 100vh;
}
</style>
