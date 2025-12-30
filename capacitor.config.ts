import type { CapacitorConfig } from '@capacitor/cli'

const config: CapacitorConfig = {
  appId: 'xyz.enoki.nomadpulse',
  appName: 'NomadPulse',
  webDir: 'dist',
  plugins: {
    StatusBar: {
      overlaysWebView: false,
    },
  },
  server: {
    androidScheme: 'https',
  },
}

export default config
