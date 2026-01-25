<template lang="pug">
v-card(
  style="width: 100%; height: 100%;"
  :class="settings.hidden.isAndroid15OrHigher ? 'top-android-15-or-higher' : ''"
  )
  v-card-actions
    p.ml-2(style="font-size: 1.3em") タイムライン
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
        @click="timelineDeleteDialog = true"
        )
        .icon
          v-icon mdi-delete-outline
        .text
          p.title タイムラインデータの削除
          p.description 保存されているタイムラインデータをすべて削除します
v-dialog(
  v-model="timelineDeleteDialog"
  max-width="500"
  )
  v-card
    v-card-title タイムラインデータの削除
    v-card-text 本当に保存されているタイムラインデータをすべて削除しますか？ この操作は元に戻せません。
    v-card-actions
      v-spacer
      v-btn(
        text
        @click="timelineDeleteDialog = false"
        ) キャンセル
      v-btn(
        text
        @click="deleteTimelineData"
        style="background-color: rgb(var(--v-theme-primary)); color: white;"
        ) 削除
v-dialog(
  v-model="timelineDeletedDialog"
  max-width="500"
  )
  v-card
    v-card-title タイムラインデータの削除完了
    v-card-text タイムラインデータの削除が完了しました。
    v-card-actions
      v-spacer
      v-btn(
        text
        @click="timelineDeletedDialog = false"
        ) 閉じる
</template>

<script lang="ts">
  import { Directory, Filesystem } from '@capacitor/filesystem'
  import { useMyProfileStore } from '@/stores/myProfile'
  import { useSettingsStore } from '@/stores/settings'

  export default {
    name: 'SettingsPage',
    data () {
      return {
        settings: useSettingsStore(),
        myProfile: useMyProfileStore(),
        timelineDeleteDialog: false,
        timelineDeletedDialog: false,
      }
    },
    async mounted () {},
    methods: {
      /** タイムラインデータの削除 */
      deleteTimelineData () {
        this.timelineDeleteDialog = false
        Filesystem.deleteFile({
          path: 'timeline.json',
          directory: Directory.Data,
        })
        this.timelineDeletedDialog = true
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
