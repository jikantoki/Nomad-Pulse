<template lang="pug">
v-card(
  style="width: 100%; height: 100%;"
  :class="isAndroid15OrHigher ? 'top-android-15-or-higher' : ''"
  )
  v-card-actions
    p.ml-2(style="font-size: 1.3em") 友達リスト
    v-spacer
    v-btn(
      text
      @click="$router.back()"
      icon="mdi-close"
      )
  v-card-text(style="height: inherit; overflow-y: auto;")
    //-- フレンドリスト
    .friend-list
      p 0人の友達
      .friend-cover
        .friend(v-ripple)
          .icon
            img(src="/account_default.jpg")
          .name-and-id-and-description
            .name-and-id
              span.name 名前
              span.id @id-hogehoge
            .description いい感じのアカウントです
          .action-buttons
            v-btn.trash(
              icon="mdi-trash-can-outline"
              @click.stop="deleteDialog = {userId: 'jikantoki', name: 'えのき'}"
            )
    //-- 承認待ちリスト
    .friend-accept-list
      p 承認待ち
      .friend-cover
        .friend(v-ripple)
          .icon
            img(src="/account_default.jpg")
          .name-and-id-and-description
            .name-and-id
              span.name 名前
              span.id @id-hogehoge
            .description いい感じのアカウントです～～～～～～～～～～～～～～～～～～～ふえええええ～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～～
          .action-buttons
            v-btn(
              color="var(--color-error)"
              icon="mdi-close"
              @click.stop=""
            )
            v-btn(
              color="var(--color-success)"
              icon="mdi-check"
              @click.stop=""
            )
v-dialog(
  v-model="deleteDialog"
)
  v-card
    v-card-title 友達の削除
    v-card-text {{ deleteDialog.name }}@{{ deleteDialog.userId }}を友達から削除しますか？
    v-card-actions
      v-btn(
        @click="deleteDialog = false"
      ) キャンセル
      v-btn 削除
</template>

<script lang="ts">
  export default {
    data () {
      return {
        /**
         * 友達削除ダイアログフラグ（userInfoObject）
         * nullだとエラー出るのでfalse or Object型
         * */
        deleteDialog: false as false | any,
      }
    },
  }
</script>

<style lang="scss" scoped>
.friend-cover {
  .friend {
    display: flex;
    align-items: center;
    border-radius: 16px;
    padding: 12px;
    margin: 8px 0;
    overflow: hidden;
    transition: background-color .2s;
    &:hover {
      background-color: rgba(var(--v-theme-surface-light), 0.5);
    }
    cursor: pointer;
    .icon {
      img{
      height: 4em;
      width: 4em;
      border-radius: 9999px;
      }
    }
      .name-and-id-and-description {
        margin: 0 16px;
        //max-width: calc(100vw - 18em);
        white-space: nowrap;
        overflow: hidden;
        margin-right: auto;
        .name-and-id {
          .id {
            opacity: 0.5;
          }
        }
      }
      .action-buttons {
        margin-left: 16px;
        display: flex;
        button {
          margin: 4px;
        }
        .trash:hover {
          background-color: var(--color-error);
        }
      }
  }
}
</style>
