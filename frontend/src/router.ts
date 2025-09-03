import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  { path: '/', redirect: { name: 'order' } },
  {
    path: '/order',
    name: 'order',
    component: () => import('./views/OrderView.vue'),
    meta: { nav: true, title: 'Order', order: 1},
  },
  {
    path: '/expense',
    name: 'expense',
    component: () => import('./views/ExpenseView.vue'),
    meta: { nav: true, title: 'Expense', order: 2},
  },
  {
    path: '/delivery-log',
    name: 'delivery-log',
    component: () => import('./views/DeliveryLogView.vue'),
    meta: { nav: true, title: 'Delivery Log', order: 3},
  },
]

export const router = createRouter({
  history: createWebHistory(),
  routes,
})
