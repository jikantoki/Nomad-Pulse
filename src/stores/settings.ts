import { defineStore } from 'pinia'

export const useSettingsStore = defineStore('settings', {
  state: () => ({
    display: {
      theme: 'system' as 'system' | 'light' | 'dark',
      language: '日本語' as const,
    },
    notification: {},
    location: {
      pause: false,
      shareTime: {
        /** Trueなら、StartからEndまでの時間しか共有しない */
        enabled: false,
        start: {
          hour: 0,
          min: 0,
        },
        end: {
          hour: 0,
          min: 0,
        },
      },
      shareLocation: {
        /** Trueなら、centerからdistanceの距離にいる時しか共有しない */
        enabled: false,
        centerLatlng: [0, 0],
        /** centerから何m先までシェアを有効にするか */
        distance: 0,
      },
    },
    developerOptions: {
      enabled: false,
      statusBarNotch: 'default' as 'default' | 'true' | 'false',
    },
    hidden: {
      isAndroid15OrHigher: false,
    },
  }),
  persist: true,
})
