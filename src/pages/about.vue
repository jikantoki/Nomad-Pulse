<template lang="pug">
v-card(
  style="width: 100%; height: 100%;"
  )
  v-card-actions
    p.ml-2(style="font-size: 1.3em") このアプリについて
    v-spacer
    v-btn(
      text
      @click="$router.back()"
      icon="mdi-close"
      )
  v-card-text(style="height: calc(100% - 64px); overflow-y: auto;")
    .account-details(
      style="display: flex; flex-direction: column; align-items: center; gap: 1em; margin-bottom: 1em;"
    )
      .account-img
        img(src="/icon.png" style="height: 8em; width: 8em;")
      .account-info(
        style="text-align: center;"
      )
        v-btn(
          text
          append-icon="mdi-github"
          @click="openURL('https://github.com/jikantoki/Nomad-Pulse')"
        ) Github
        v-btn(
          text
          append-icon="mdi-web"
          @click="openURL('https://nomadpulse.enoki.xyz')"
        ) ホームページ
    .settings-list
      .setting-item(v-ripple)
        .icon
          v-icon mdi-application-outline
        .text
          p.title Nomad Pulse
          p.description &copy; 2025 エノキ電気
      .setting-item(
        v-ripple
        @click="developerOptionToggle()"
        )
        .icon
          v-icon mdi-information-outline
        .text
          p.title バージョン情報
          p.description v{{ packageJson.version }}
      .setting-item(
        v-ripple
        @click="$router.push('/settings/developer-options')"
        )
        .icon
          v-icon mdi-cog-outline
        .text
          p.title 開発者オプション
          p.description 特定のモバイル向けの機能を強制有効
    hr
    p.ma-4(style="font-size: 1.3em;") 製作者情報
    .settings-list
      .setting-item(v-ripple)
        .icon
          img(src="/jikantoki.jpg" width="36" height="36" style="border-radius: 50%;")
        .text
          p.title ときえのき
          p.description jikantoki
      .setting-item(
        v-ripple
        @click="openURL('https://enoki.xyz')"
        )
        .icon
          img(src="/jikantoki-homepage.jpg" width="36" height="36" style="border-radius: 50%;")
        .text
          p.title ホームページ
          p.description https://enoki.xyz
      .setting-item(
        v-ripple
        @click="openURL('https://twitter.com/jikantoki')"
        )
        .icon(
          style="background: #1DA1F2;"
        )
          v-icon mdi-twitter
        .text
          p.title Twitter
          p.description https://twitter.com/jikantoki
      .setting-item(
        v-ripple
        @click="openURL('https://github.com/jikantoki')"
        )
        .icon(
          style="background: #111111;"
        )
          v-icon mdi-github
        .text
          p.title GitHub
          p.description https://github.com/jikantoki
</template>

<script lang="ts">
  import { Browser } from '@capacitor/browser'
  import { Toast } from '@capacitor/toast'
  import PackageJson from '../../package.json'

  export default {
    data () {
      return {
        packageJson: PackageJson,
        developerOptionClickCount: 0,
        developerOptionEnabled: false,
      }
    },
    mounted () {
      const developerOptionEnabled = localStorage.getItem('developerOptionEnabled')
      if (developerOptionEnabled === 'true') {
        this.developerOptionEnabled = true
      }
    },
    methods: {
      /** 開発者オプションを有効にする */
      async developerOptionToggle () {
        if (this.developerOptionEnabled) {
          await Toast.show({
            text: '開発者オプションは既に有効です',
            duration: 'long',
          })
          return
        }
        await Toast.show({
          text: `開発者オプションを有効にするまであと ${8 - this.developerOptionClickCount} 回クリックしてください`,
        })
        this.developerOptionClickCount += 1
        if (this.developerOptionClickCount >= 8) {
          this.developerOptionEnabled = true
          localStorage.setItem('developerOptionEnabled', 'true')
          await Toast.show({
            text: '開発者オプションが有効になりました',
            duration: 'long',
          })
        }
      },
      /** URLをブラウザで開く */
      async openURL (url: string) {
        await Browser.open({ url: url })
      },
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
</style>
