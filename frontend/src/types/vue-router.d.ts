import 'vue-router'

declare module 'vue-router' {
  interface RouteMeta {
    nav?: boolean
    title?: string
    order?: number
  }
}