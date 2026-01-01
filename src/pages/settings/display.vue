<template lang="pug">
v-card(
  style="width: 100%; height: 100%;"
  :class="isAndroid15OrHigher ? 'top-android-15-or-higher' : ''"
  )
  v-card-actions
    p.ml-2(style="font-size: 1.3em") 外観
    v-spacer
    v-btn(
      text
      @click="$router.back()"
      icon="mdi-close"
      )
  v-card-text(style="height: inherit; overflow-y: auto;")
    .settings-list
      .setting-item
        .icon
          v-icon mdi-theme-light-dark
        .text
          p.title テーマ
          p.description アプリをライトテーマで表示するか、ダークテーマで表示するかが選べます
      .setting-button-item
        .li(
          v-ripple
          :class="options.theme === undefined ? 'selected' : null"
          @click="options.theme = undefined"
        )
          v-icon(size="x-large") mdi-brightness-auto
          p システム
        .li(
          v-ripple
          :class="options.theme === true ? 'selected' : null"
          @click="options.theme = true"
        )
          v-icon(size="x-large") mdi-brightness-7
          p ライト
        .li(
          v-ripple
          :class="options.theme === false ? 'selected' : null"
          @click="options.theme = false"
        )
          v-icon(size="x-large") mdi-brightness-3
          p ダーク
      .my-16
</template>

<script lang="ts">
  import { Device } from '@capacitor/device'
  import { StatusBar, Style } from '@capacitor/status-bar'

  export default {
    data () {
      return {
        options: {
          theme: undefined as boolean | undefined,
        },
        isAndroid15OrHigher: false,
      }
    },
    watch: {
      options: {
        handler (newOptions) {
          localStorage.setItem('themeOptions', JSON.stringify(newOptions))

          switch (newOptions.theme) {
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
              if (systemTheme) {
                StatusBar.setStyle({ style: Style.Dark })
                this.$vuetify.theme.change('dark')
              } else {
                StatusBar.setStyle({ style: Style.Light })
                this.$vuetify.theme.change('light')
              }
              break
            }
          // No default
          }
        },
        deep: true,
      },
    },
    async mounted () {
      const optionsJson = localStorage.getItem('themeOptions')
      if (optionsJson) {
        const options = JSON.parse(optionsJson)
        this.options = {
          ...options,
        }
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
    },
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

  .setting-button-item {
    display: flex;
    flex-direction: row;
    justify-content: space-between;
    margin: 0 1em;
    border-radius: 8px;
    overflow: hidden;
      border: 1px solid rgba(var(--v-theme-on-surface), 0.3);
    .li {
      width: 33%;
      text-align: center;
      cursor: pointer;
      .v-icon {
        margin: 8px;
      }
      p {
        margin: 0;
        padding: 0.5em 0;
      }
      &.selected {
        background-color: rgba(var(--v-theme-primary), 0.3);
      }
    }
  }

  .top-android-15-or-higher {
    height: calc(100vh - 40px - 16px)!important;
  }
</style>
