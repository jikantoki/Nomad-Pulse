/**
 * main.ts
 *
 * Bootstraps Vuetify and other plugins then mounts the App`
 */

import type { URLOpenListenerEvent } from '@capacitor/app'
import { App as capacitorApp } from '@capacitor/app'

import { Toast } from '@capacitor/toast'

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

// 特定のURLを開いたらこのアプリが立ち上がる設定
capacitorApp.addListener('appUrlOpen', function (event: URLOpenListenerEvent) {
  const url = new URL(event.url)
  const slug = url.pathname
  if (slug) {
    router.push(slug)
  } else {
    Toast.show({ text: '対応する動作が見つかりませんでした。' })
  }
})
