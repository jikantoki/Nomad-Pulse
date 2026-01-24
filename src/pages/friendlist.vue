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
    .friend-accept-list(
      v-if="acceptList.length"
    )
      p {{ acceptList.length }}人の承認待ち
      .friend-cover
        .friend(
          v-ripple
          v-for="(friend, cnt) of acceptList"
          @click="$router.push(`/user/${friend.userId}`)"
          )
          .icon
            img(
              v-if="friend.icon"
              :src="friend.icon"
            )
            img(
              v-else
              src="/account_default.jpg"
              )
          .name-and-id-and-description
            .name-and-id
              span.name {{ (friend.name && friend.name.length) ? friend.name : friend.userId }}
              span.id @{{ friend.userId }}
            .description {{ friend.message }}
          .action-buttons
            v-btn(
              color="var(--color-error)"
              icon="mdi-close"
              @click.stop="accept(friend.userId, false)"
            )
            v-btn(
              color="var(--color-success)"
              icon="mdi-check"
              @click.stop="accept(friend.userId, true)"
            )
    //-- 申請中リスト
    .friend-accept-list(
      v-if="requestList.length"
    )
      p {{ requestList.length }}人の申請中（友達の承認待ち）
      .friend-cover
        .friend(
          v-ripple
          v-for="(friend, cnt) of requestList"
          @click="$router.push(`/user/${friend.userId}`)"
          )
          .icon
            img(
              v-if="friend.icon"
              :src="friend.icon"
            )
            img(
              v-else
              src="/account_default.jpg"
              )
          .name-and-id-and-description
            .name-and-id
              span.name {{ (friend.name && friend.name.length) ? friend.name : friend.userId }}
              span.id @{{ friend.userId }}
            .description {{ friend.message }}
    //-- フレンドリスト
    .friend-list(v-if="friendList.length")
      p {{ friendList.length }}人の友達
      .friend-cover
        .friend(
          v-ripple
          v-for="(friend, cnt) of friendList"
          @click="$router.push(`/user/${friend.userId}`)"
          )
          .icon
            img(
              v-if="friend.icon"
              :src="friend.icon"
            )
            img(
              v-else
              src="/account_default.jpg"
              )
          .name-and-id-and-description
            .name-and-id
              span.name {{ (friend.name && friend.name.length) ? friend.name : friend.userId }}
              span.id @{{ friend.userId }}
            .description {{ friend.message }}
          .action-buttons
            v-btn.trash(
              icon="mdi-trash-can-outline"
              @click.stop="deleteDialog = true; deleteTarget = friend"
            )
    .no-friend(v-if="!friendList.length")
      .my-16
      p.text-h5(
        style="text-align: center;"
      )
        span 友達がいません。
        br
        span トップページの検索ボタンから探してみよう！
    .my-16.pa-2
v-dialog(
  v-model="deleteDialog"
)
  v-card
    v-card-title 友達の削除
    v-card-text {{ deleteTarget.name?.length ? deleteTarget.name : deleteTarget.userId }}@{{ deleteTarget.userId }}を友達から削除しますか？
    v-card-actions
      v-btn(
        @click="deleteDialog = false"
      ) キャンセル
      v-btn(
        @click="accept(deleteTarget.userId, false)"
      ) 削除
v-dialog(
  v-model="loadingStatusDialog"
  persistent
)
  v-card(width="400")
    v-card-title 処理中…
    v-card-text
      v-progress-linear.my-4(indeterminate)
</template>

<script lang="ts">
  import { Device } from '@capacitor/device'
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
        /** ステータス変更処理中ダイアログ */
        loadingStatusDialog: false,
        developerOptionEnabled: false,
        isAndroid15OrHigher: false,
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
      // ここからページ固有のmounted処理
      const myProfile = localStorage.getItem('profile')
      if (myProfile) {
        this.myProfile = JSON.parse(myProfile)
      }

      const friendList = localStorage.getItem('friendList')
      const acceptList = localStorage.getItem('acceptList')
      const requestList = localStorage.getItem('requestList')
      if (friendList) {
        this.friendList = JSON.parse(friendList)
      }
      if (acceptList) {
        this.acceptList = JSON.parse(acceptList)
      }
      if (requestList) {
        this.requestList = JSON.parse(requestList)
      }

      // 友達リストの取得（申請・承認リストも同時に取得）
      const res = await this.sendAjaxWithAuth('/getMyFriendList.php', {
        id: this.myProfile.userId,
        token: this.myProfile.userToken,
      })
      if (res && res.body) {
        const allFriendList = res.body.friendList
        this.friendList = []
        this.acceptList = []
        this.requestList = []
        if (allFriendList) {
          for (const friend of allFriendList) {
            friend.friendProfile.userId = friend.friendRealId
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
          localStorage.setItem('friendList', JSON.stringify(this.friendList))
          localStorage.setItem('acceptList', JSON.stringify(this.acceptList))
          localStorage.setItem('requestList', JSON.stringify(this.requestList))
        }
      }
    },
    methods: {
      /**
       * 友達として承認、もしくはキャンセルする
       * @param userId ターゲットのユーザーID
       * @param accept True→承認、False→キャンセル
       */
      async accept (userId: string, accept: boolean) {
        this.loadingStatusDialog = true
        await this.sendAjaxWithAuth('/acceptFriend.php', {
          id: this.myProfile.userId,
          token: this.myProfile.userToken,
          targetId: userId,
          friendAccept: accept,
        })
        let cnt = 0
        let friendProfile = {}
        for (const friend of this.acceptList) {
          if (friend.userId == userId) {
            friendProfile = this.acceptList[cnt]
            this.acceptList.splice(cnt, 1)
            break
          }
          cnt++
        }
        cnt = 0
        if (accept) {
          this.friendList.push(friendProfile)
        } else {
          for (const friend of this.friendList) {
            if (friend.userId == userId) {
              this.friendList.splice(cnt, 1)
              break
            }
            cnt++
          }
        }
        // これないとバグる
        this.deleteDialog = false
        this.loadingStatusDialog = false

        localStorage.setItem('friendList', JSON.stringify(this.friendList))
        localStorage.setItem('acceptList', JSON.stringify(this.acceptList))
        localStorage.setItem('requestList', JSON.stringify(this.requestList))
        return
      },
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

.top-android-15-or-higher {
  height: calc(100vh - 40px - 16px)!important;
}
</style>
