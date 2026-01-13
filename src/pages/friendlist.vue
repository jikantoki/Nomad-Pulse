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
    //-- 承認待ちリスト
    .friend-accept-list
      p 承認待ち
      .friend-cover
        .friend(
          v-ripple
          @click=""
          )
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
    //-- 申請中リスト
    .friend-accept-list
      p 申請中（友達の承認待ち）
      .friend-cover
        .friend(
          v-ripple
          @click=""
          )
          .icon
            img(src="/account_default.jpg")
          .name-and-id-and-description
            .name-and-id
              span.name 名前
              span.id @id-hogehoge
            .description いい感じのアカウントです
    //-- フレンドリスト
    .friend-list
      p {{ friendList.length }}人の友達
      .friend-cover
        .friend(
          v-ripple
          @click=""
          )
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
              @click.stop="deleteDialog = true; deleteTarget = {userId: 'jikantoki', name: 'えのき'}"
            )
v-dialog(
  v-model="deleteDialog"
)
  v-card
    v-card-title 友達の削除
    v-card-text {{ deleteTarget.name }}@{{ deleteTarget.userId }}を友達から削除しますか？
    v-card-actions
      v-btn(
        @click="deleteDialog = false"
      ) キャンセル
      v-btn 削除
</template>

<script lang="ts">
  // @ts-ignore
  import mixins from '@/mixins/mixins'

  export default {
    mixins: [mixins],
    data () {
      return {
        /**
         * 友達削除ダイアログフラグ
         * */
        deleteDialog: false as boolean,
        /**
         * ダイアログのターゲット（userInfoObject）
         */
        deleteTarget: null as null | any,
        myProfile: null as null | any,
        /** 友達側の承認待ちリスト */
        requestList: [] as any[],
        /** 自分側の承認待ちリスト */
        acceptList: [] as any[],
        /** 友達リスト */
        friendList: [] as any[],
      }
    },
    async mounted () {
      const myProfile = localStorage.getItem('profile')
      if (myProfile) {
        this.myProfile = JSON.parse(myProfile)
      }

      // 友達リストの取得（申請・承認リストも同時に取得）
      const res = await this.sendAjaxWithAuth('/getMyFriendList.php', {
        id: this.myProfile.userId,
        token: this.myProfile.userToken,
      })
      if (res && res.body) {
        const allFriendList = res.body.friendList
        for (const friend of allFriendList) {
          if (friend.status == 'request') {
            if (friend.fromUserId == res.body.mySecretId) {
              this.requestList.push(friend.friendProfile)
            } else {
              this.acceptList.push(friend.friendProfile)
            }
          } else if (friend.status == 'friend') {
            this.friendList.push(friend.friendProfile)
          }
        }
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
