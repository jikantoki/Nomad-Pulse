import type { CapacitorConfig } from '@capacitor/cli'

const config: CapacitorConfig = {
  appId: 'xyz.enoki.nomadpulse',
  appName: 'NomadPulse',
  webDir: 'dist',
  plugins: {
    StatusBar: {
      overlaysWebView: false,
    },
    BackgroundRunner: {
      label: 'xyz.enoki.nomadpulse.background',
      src: 'runners/background-runner.js',
      event: 'NomadPulseBackgroundRunner',
      repeat: true,
      interval: 1,
      autoStart: true,
    },
    PushNotifications: {
      presentationOptions: ['badge', 'sound', 'alert'],
    },
  },
  server: {
    androidScheme: 'https',
  },
  deepLinks: {
    enabled: true,
    prefixes: ['nomadpulse://', 'https://nomadpulse.enoki.xyz'],
  },
}

export default config
