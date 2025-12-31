<template lang="pug">
v-app
  v-main
    .status-bar-padding(:class="isAndroid15OrHigher ? 'android-15-or-higher' : ''")
    router-view(:style="isAndroid15OrHigher ? 'height: calc(100vh - 40px - 16px);' : ''")
    .nav-bar-padding(:class="isAndroid15OrHigher ? 'android-15-or-higher' : ''")
</template>

<script lang="ts">
  import { Device } from '@capacitor/device'
  import { StatusBar } from '@capacitor/status-bar'

  export default {
    data () {
      return {
        isAndroid15OrHigher: true,
      }
    },
    async mounted () {
      StatusBar.setOverlaysWebView({
        overlay: false,
      })
      const info = await Device.getInfo()
      this.isAndroid15OrHigher = info.platform === 'android' && Number(info.osVersion) >= 15 ? true : false
    },
  }
</script>

<style lang="scss">
.status-bar-padding.android-15-or-higher {
  height: 40px;
  width: 100vw;
}
.nav-bar-padding.android-15-or-higher {
  height: 16px;
  width: 100vw;
}

body {
  user-select: none;
}

hr {
  border-color: rgba(var(--v-theme-on-surface), 0.3);
  margin: 0.5em 1em;
}
</style>
