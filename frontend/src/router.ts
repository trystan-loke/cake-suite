import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'dashboard',
    component: () => import('./views/Dashboard.vue'),
  },
  {
    path: '/orders',
    name: 'orders',
    component: () => import('./views/Orders.vue'),
  },
  {
    path: '/inventory',
    name: 'inventory',
    component: () => import('./views/Inventory.vue'),
  },
  {
    path: '/reports',
    name: 'reports',
    component: () => import('./views/Reports.vue'),
  },
  {
    path: '/settings',
    name: 'settings',
    component: () => import('./views/Settings.vue'),
  },
]

export const router = createRouter({
  history: createWebHistory(),
  routes,
})
