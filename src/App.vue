<template lang="pug">
v-app
  common-splash-vue(v-show="splashScreen")
  v-main
    .status-bar-padding(:class="isAndroid15OrHigher ? 'android-15-or-higher' : ''")
    router-view(:style="isAndroid15OrHigher ? 'height: calc(100vh - 40px - 16px);' : ''")
    .nav-bar-padding(:class="isAndroid15OrHigher ? 'android-15-or-higher' : ''")
</template>

<script lang="ts">
  import { Device } from '@capacitor/device'
  import { StatusBar, Style } from '@capacitor/status-bar'
  import commonSplashVue from './components/common/commonSplash.vue'

  export default {
    name: 'App',
    components: {
      commonSplashVue,
    },
    data () {
      return {
        isAndroid15OrHigher: true,
        splashScreen: true,
      }
    },
    async mounted () {
      StatusBar.setOverlaysWebView({
        overlay: false,
      })
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

      // テーマに関する設定
      const themeOptions = localStorage.getItem('themeOptions')
      if (themeOptions) {
        const options = JSON.parse(themeOptions)
        switch (options.theme) {
          case true: {
            this.$vuetify.theme.change('light')
            StatusBar.setStyle({ style: Style.Light })
            break
          }
          case false: {
            this.$vuetify.theme.change('dark')
            StatusBar.setStyle({ style: Style.Dark })

            break
          }
          case undefined: {
            const systemTheme = window.matchMedia('(prefers-color-scheme: dark)').matches
            systemTheme ? this.$vuetify.theme.change('dark') : this.$vuetify.theme.change('light')

            break
          }
        }
      }

      /**
       * mountedの最後に記述
       */
      window.setTimeout(() => {
        this.splashScreen = false
      }, 2000)
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

main {
  height: 100vh;
  overflow: hidden;
}

//テーマカラーの変更
//青紫がテーマカラー！
.v-theme--dark {
  --v-theme-primary: 145,56,213!important;
}
.v-theme--light {
  --v-theme-primary: 145,56,213!important;
}
:root {
  --v-theme-primary: 145,56,213!important;
}
</style>
