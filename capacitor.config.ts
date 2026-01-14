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
      /** OSの制約で15分間隔 */
      interval: 15,
      autoStart: true,
    },
    PushNotifications: {
      presentationOptions: ['badge', 'sound', 'alert'],
    },
    CapacitorHttp: {
      enabled: true,
    },
  },
  server: {
    hostname: 'nomadpulse.enoki.xyz',
    androidScheme: 'https',
  },
  deepLinks: {
    enabled: true,
    prefixes: ['nomadpulse://', 'https://nomadpulse.enoki.xyz'],
  },
  android: {
    useLegacyBridge: true,
    notificationIcon: 'ic_location_status',
  },
}

export default config
