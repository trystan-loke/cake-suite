import { createApp } from 'vue'

// Vuetify
import 'vuetify/styles'
import { createVuetify } from 'vuetify'
import { aliases, mdi } from 'vuetify/iconsets/mdi-svg'
import { 
  mdiDotsVertical, 
  mdiPlus, 
  mdiPencil, 
  mdiDelete,
  mdiAccount,
  mdiPhone,
  mdiEmail,
  mdiCalendar,
  mdiTruck,
  mdiStore,
  mdiUpload,
  mdiFilter,
  mdiFileDownload
} from '@mdi/js'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'

import App from './App.vue'
import { router } from './router.ts'
import './styles.css'
import './registerSW'

const vuetify = createVuetify({
  components,
  directives,
  icons: {
    defaultSet: 'mdi',
    aliases: {
      ...aliases,
      dotsVertical: mdiDotsVertical,
      plus: mdiPlus,
      pencil: mdiPencil,
      delete: mdiDelete,
      edit: mdiPencil,
      account: mdiAccount,
      phone: mdiPhone,
      email: mdiEmail,
      calendar: mdiCalendar,
      truck: mdiTruck,
      store: mdiStore,
      upload: mdiUpload,
      filter: mdiFilter,
      fileDownload: mdiFileDownload
    },
    sets: {
      mdi,
    },
  },
})

const app = createApp(App)
app.use(vuetify)
app.use(router)
app.mount('#app')
