// src/registerSW.ts
import { registerSW } from 'virtual:pwa-register'

const updateSW = registerSW({
  onNeedRefresh() {
    // Show a notification to the user that a new version is available
    if (confirm('New content available. Reload?')) {
      updateSW()
    }
  },
  onOfflineReady() {
    console.log('App ready to work offline')
  }
})
