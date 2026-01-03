/**
 * main.ts
 *
 * Bootstraps Vuetify and other plugins then mounts the App`
 */

import type { URLOpenListenerEvent } from '@capacitor/app'
import { App as capacitorApp } from '@capacitor/app'

import { defineCustomElements } from '@ionic/pwa-elements/loader'

// Composables
import { createApp } from 'vue'

// Plugins
import { registerPlugins } from '@/plugins'

// Components
import App from './App.vue'

import router from './router'

// Styles
import 'unfonts.css'
defineCustomElements(window)

const app = createApp(App)

registerPlugins(app)

app.mount('#app')

capacitorApp.addListener('appUrlOpen', function (event: URLOpenListenerEvent) {
  const slug = event.url.split('.app').pop()
  if (slug) {
    router.push({
      path: slug,
    })
  }
})
