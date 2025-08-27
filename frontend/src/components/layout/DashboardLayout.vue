<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { auth } from '../../firebase'
import { onAuthStateChanged, signOut } from 'firebase/auth'

const route = useRoute()
const showUserMenu = ref(false)
const userEmail = ref<string | null>(auth.currentUser?.email ?? null)

const currentTitle = computed(() => {
  const name = route.name?.toString() || 'dashboard'
  return name.charAt(0).toUpperCase() + name.slice(1)
})

function logout() {
  signOut(auth)
}

let unsubscribe: (() => void) | undefined
onMounted(() => {
  unsubscribe = onAuthStateChanged(auth, (u) => {
    userEmail.value = u?.email ?? null
  })
  // Close user menu on outside click
  const onDocClick = (e: MouseEvent) => {
    const target = e.target as HTMLElement
    if (!target.closest('.user-menu')) {
      showUserMenu.value = false
    }
  }
  document.addEventListener('click', onDocClick)
  ;(onBeforeUnmount as any)(() => document.removeEventListener('click', onDocClick))
})

onBeforeUnmount(() => {
  if (unsubscribe) unsubscribe()
})
</script>

<template>
  <div class="layout">
  <header class="topbar">
      <div class="brand">
    🍰 Cake Suite
      </div>
      <div class="top-actions">
        <div class="user-menu" @click.stop="showUserMenu = !showUserMenu">
          <button class="avatar" aria-label="User menu" title="Account">
            👤
          </button>
          <div v-if="showUserMenu" class="dropdown" @click.stop>
            <div class="email" v-if="userEmail">{{ userEmail }}</div>
            <RouterLink to="/settings" class="menu-item">Settings</RouterLink>
            <button class="menu-item danger" @click="logout">Logout</button>
          </div>
        </div>
      </div>
    </header>

  <aside :class="['sidebar']">
      <nav>
  <div class="nav-label">Menu</div>
        <RouterLink to="/" class="item" :class="{ active: route.name === 'dashboard' }">Dashboard</RouterLink>
        <RouterLink to="/orders" class="item" :class="{ active: route.name === 'orders' }">Orders</RouterLink>
        <RouterLink to="/inventory" class="item" :class="{ active: route.name === 'inventory' }">Inventory</RouterLink>
        <RouterLink to="/reports" class="item" :class="{ active: route.name === 'reports' }">Reports</RouterLink>
        <RouterLink to="/settings" class="item" :class="{ active: route.name === 'settings' }">Settings</RouterLink>
      </nav>
    </aside>

    <section class="main">
      <div class="page-header">
        <h1>{{ currentTitle }}</h1>
      </div>
      <div class="content">
        <div class="content-card">
          <slot />
        </div>
      </div>
    </section>
  </div>
  
</template>

<style scoped>
.layout {
  display: grid;
  grid-template-rows: 56px 1fr;
  grid-template-columns: 240px 1fr;
  grid-template-areas:
    "header header"
    "sidebar main";
  min-height: 100vh;
  background: var(--bg-app);
}
.topbar {
  grid-area: header;
  position: relative;
  display:flex; justify-content: space-between; align-items:center; padding: 0 1rem;
  background: linear-gradient(180deg, var(--primary) 0%, #f5c4cc 100%);
  color: var(--primary-ink);
  border-bottom: 1px solid #e9b9c3;
  box-shadow: 0 2px 12px rgba(90, 58, 58, 0.08);
}
.topbar::after {
  content: "";
  position: absolute;
  left: 0; right: 0; bottom: -10px; height: 10px;
  background: radial-gradient(10px 10px at 10px 0, var(--primary) 98%, rgba(0,0,0,0) 100%) repeat-x;
  background-size: 20px 10px;
  pointer-events: none;
  opacity: 0.9;
}
.brand { font-weight: 700; letter-spacing: 0.2px; font-family: "Playfair Display", serif; font-size: 1.05rem; }
.top-actions { display:flex; align-items:center; gap: 0.5rem; }
.user-menu { position: relative; }
.avatar { width: 36px; height: 36px; border-radius: 999px; border: 1px solid #e3bbc2; background: #fff4f6; color: var(--primary-ink); display: grid; place-items: center; cursor: pointer; box-shadow: 0 1px 4px rgba(90,58,58,0.12); }
.avatar:hover { background: #ffe9ee; }
.dropdown { position: absolute; right: 0; top: calc(100% + 8px); background: #fff; color: var(--ink); border: 1px solid var(--border); border-radius: 10px; min-width: 220px; box-shadow: 0 10px 25px rgba(42,42,42,0.08); padding: 0.5rem; z-index: 20; }
.dropdown .email { padding: 0.5rem; font-size: 0.85rem; color: var(--muted); border-bottom: 1px solid #f4ebe2; margin-bottom: 0.25rem; }
.menu-item { display: block; width: 100%; text-align: left; background: transparent; border: none; padding: 0.5rem 0.5rem; border-radius: 6px; color: inherit; text-decoration: none; cursor: pointer; }
.menu-item:hover { background: #fff4f6; }
.menu-item.danger { color: #8b2e2e; }
.sidebar {
  grid-area: sidebar;
  background: linear-gradient(180deg, #f3efe8 0%, #efe6db 100%);
  color: var(--ink);
  padding: 1rem;
  border-right: 1px solid var(--border);
  border-radius: 0 14px 14px 0;
}
.logo { font-weight: 700; margin-bottom: 1rem; cursor: pointer; }
nav { display: flex; flex-direction: column; gap: 0.5rem; }
.nav-label { font-size: 0.75rem; letter-spacing: 0.08em; text-transform: uppercase; color: #8a7d7a; margin: 0 0 0.25rem 0.5rem; }
.item { color: #6b5a57; text-decoration: none; padding: 0.6rem 0.75rem; border-radius: 10px; border: 1px solid transparent; transition: transform 120ms ease, box-shadow 160ms ease, background 160ms ease, color 160ms ease, border-color 160ms ease; }
.item:hover { background: #fff; border-color: var(--border); color: var(--espresso); box-shadow: 0 2px 10px rgba(42,42,42,0.05); transform: translateX(2px); }
.item.active { background: var(--mint); color: var(--espresso); border-color: #cfe7df; box-shadow: inset 3px 0 0 var(--sage); }
.main { grid-area: main; display: flex; flex-direction: column; }
.page-header { padding: 1rem 1.25rem; border-bottom: 1px dashed var(--border); background: #fff; }
.page-header h1 { font-size: 1.25rem; margin: 0; }
.content { padding: 1.25rem; }
.content-card {
  background: var(--bg-panel);
  border: 1px solid var(--border);
  border-radius: 14px;
  box-shadow: 0 8px 30px rgba(42,42,42,0.06);
  padding: 1rem 1.25rem;
  max-width: 1200px;
  margin: 0 auto;
}

@media (max-width: 900px) {
  .sidebar { position: fixed; inset: 56px auto 0 0; width: 240px; z-index: 10; transform: translateX(0); }
}
</style>
