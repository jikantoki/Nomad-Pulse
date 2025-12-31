<template lang="pug">
v-app
  v-main
    .status-bar-padding(:class="isAndroid15OrHigher ? 'android-15-or-higher' : ''")
    router-view
</template>

<script lang="ts">
  import { App } from '@capacitor/app'
  import { Device } from '@capacitor/device'
  import { StatusBar } from '@capacitor/status-bar'

  export default {
    data () {
      return {
        isAndroid15OrHigher: false,
      }
    },
    async mounted () {
      StatusBar.setOverlaysWebView({
        overlay: false,
      })
      const info = await Device.getInfo()
      if (info.platform === 'android' && Number(info.osVersion) >= 15) {
        this.isAndroid15OrHigher = true
      }

      /** バックボタンのリスナーを追加 */
      App.addListener('backButton', () => {
        if (this.$route.path === '/') {
          /** ルートページならアプリを終了 */
          App.exitApp()
        } else {
          /** ルート以外のページなら1つ戻る */
          this.$router.back()
        }
      })
    },
  }
</script>

<style lang="scss" scoped>
.android-15-or-higher {
  height: env(titlebar-area-height, 40px);
}

body {
  user-select: none;
}
</style>
