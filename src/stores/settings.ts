import { defineStore } from 'pinia'

export const useSettingsStore = defineStore('settings', {
  state: () => ({
    /** 表示と言語設定 */
    display: {
      /** テーマ */
      theme: 'system' as 'system' | 'light' | 'dark',
      /** 言語 */
      language: '日本語' as const,
    },
    /** 通知設定 */
    notification: {},
    /** 位置情報とプライバシー */
    location: {
      /** 共有の一時停止 */
      pause: false,
      /** 共有する時間 */
      shareTime: {
        /** Trueなら、StartからEndまでの時間しか共有しない */
        enabled: false,
        /** 共有開始時間 */
        start: {
          hour: 0,
          min: 0,
        },
        /** 共有終了時間 */
        end: {
          hour: 0,
          min: 0,
        },
      },
      /** 共有する場所と距離 */
      shareLocation: {
        /** Trueなら、centerからdistanceの距離にいる時しか共有しない */
        enabled: false,
        centerLatlng: [0, 0],
        /** centerから何m先までシェアを有効にするか */
        distance: 0,
      },
    },
    /** タイムライン設定 */
    timeline: {},
    /** 開発者オプション */
    developerOptions: {
      /** 開発者オプションが有効 */
      enabled: false,
      /** ステータスバーのノッチの切り欠き */
      statusBarNotch: 'default' as 'default' | 'true' | 'false',
    },
    /** 画面からは見えない設定 */
    hidden: {
      /** AndroidかつVer.15以上で自動true */
      isAndroid15OrHigher: false,
    },
  }),
  persist: true,
})
