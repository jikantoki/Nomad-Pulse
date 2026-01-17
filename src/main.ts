/**
 * main.ts
 *
 * Bootstraps Vuetify and other plugins then mounts the App`
 */

import { defineCustomElements } from '@ionic/pwa-elements/loader'

// Composables
import { createApp } from 'vue'

// Plugins
import { registerPlugins } from '@/plugins'

// Components
import App from './App.vue'
// Styles
import 'unfonts.css'
defineCustomElements(window)

const app = createApp(App)

registerPlugins(app)

app.mount('#app')
