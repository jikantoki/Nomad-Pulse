<template lang="pug">
v-card(
  style="position: fixed; top: 0; left: 0; width: 100%; height: 100%;"
  )
  v-card-actions
    p.ml-2(style="font-size: 1.3em") 設定
    v-spacer
    v-btn(
      text
      @click="$router.back()"
      icon="mdi-close"
      )
  v-card-text
    .settings-list
      .setting-item(v-ripple)
        .icon
          v-icon mdi-palette-outline
        .text
          p.title 外観
          p.description テーマ、色
      .setting-item(v-ripple)
        .icon
          v-icon mdi-bell-outline
        .text
          p.title 通知
          p.description メール、プッシュ通知
      .setting-item(
        v-ripple
        @click="$router.push('/terms')"
        )
        .icon
          v-icon mdi-file-document-outline
        .text
          p.title 位置情報とプライバシー
          p.description タイムラン、マップ履歴
      .setting-item(
        v-ripple
        @click="$router.push('/terms')"
        )
        .icon
          v-icon mdi-file-document-outline
        .text
          p.title 利用規約
      .setting-item(
        v-ripple
        @click="$router.push('/about')"
        )
        .icon
          v-icon mdi-information-outline
        .text
          p.title このアプリについて
          p.description バージョン情報
</template>

<script lang="ts">
  export default {}
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
