<template lang="pug">
v-card(
  style="width: 100%; height: 100%;"
  :class="isAndroid15OrHigher ? 'top-android-15-or-higher' : ''"
  )
  v-card-actions
    p.ml-2(style="font-size: 1.3em") 位置情報とプライバシー
    v-spacer
    v-btn(
      text
      @click="$router.back()"
      icon="mdi-close"
      )
  v-card-text(style="height: inherit; overflow-y: auto;")
    .settings-list
      .setting-item(
        v-ripple
        )
        .icon
          v-icon mdi-map-marker-off
        .text
          p.title 位置情報の一時停止
          p.description 特定の時間になるまで、位置情報を共有しません
      .setting-item(
        v-ripple
        )
        .icon
          v-icon mdi-timer-marker-outline
        .text
          p.title 位置情報の共有時間
          p.description 何時から何時まで位置情報を共有するかを選べます
      .setting-item(
        v-ripple
        )
        .icon
          v-icon mdi-map-marker-radius
        .text
          p.title 位置情報の共有場所
          p.description どの地点を基準に、何km離れたところまで位置情報を共有するかを選べます
</template>

<script lang="ts">
  import { Device } from '@capacitor/device'

  export default {
    name: 'SettingsPage',
    data () {
      return {
        logoutDialog: false,
        developerOptionEnabled: false,
        isAndroid15OrHigher: false,
        myUserId: null as string | null,
        myProfile: {} as {
          [key: string]: any
        } | null,
      }
    },
    async mounted () {
      const developerOptionEnabled = localStorage.getItem('developerOptionEnabled')
      if (developerOptionEnabled === 'true') {
        this.developerOptionEnabled = true
      }

      /** ステータスバーがWebViewをオーバーレイしないように設定 */
      const info = await Device.getInfo()
      this.isAndroid15OrHigher = info.platform === 'android' && Number(info.osVersion) >= 15 ? true : false

      // 開発者オプション
      const developerOptions = localStorage.getItem('developerOptions')
      if (developerOptions) {
        const options = JSON.parse(developerOptions)
        if (options.statusBarNotch !== undefined) {
          this.isAndroid15OrHigher = options.statusBarNotch
        }
      }

      /** ログイン情報 */
      const myProfile = localStorage.getItem('profile')
      if (myProfile) {
        this.myProfile = JSON.parse(myProfile)
        if (this.myProfile?.lastGetLocationTime) {
          this.myProfile.lastGetLocationTime = new Date(this.myProfile.lastGetLocationTime)
        }
        if (this.myProfile?.userId) {
          this.myUserId = this.myProfile.userId
        }
      }
    },
    methods: {},
  }
</script>

<style lang="scss" scoped>
  .settings-list {
    display: flex;
    flex-direction: column;
    gap: 1em;
    .setting-item {
      display: flex;
      flex-direction: row;
      align-items: center;
      gap: 1em;
      padding: 1em;
      border-radius: 8px;
      cursor: pointer;
      .icon {
        background: rgba(var(--v-theme-on-surface), 0.1);
        border-radius: 50%;
        width: 40px;
        min-width: 40px;
        height: 40px;
        display: flex;
        align-items: center;
        justify-content: center;
      }
      .text {
        .title {
          font-weight: bold;
          font-size: 1.1em;
        }
        .description {
          font-size: 0.9em;
          color: #666;
        }
      }
    }
  }

  .top-android-15-or-higher {
    height: calc(100vh - 40px - 16px)!important;
  }
</style>
