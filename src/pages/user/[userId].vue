<template lang="pug">
.user-page(v-if="param")
  v-card-actions
    p.ml-2(style="font-size: 1.3em") アカウント詳細
    v-spacer
    v-btn(
      text
      @click="$router.push('/')"
      icon="mdi-close"
      )
  .profile-zone
    .cover
      img.cover-img(v-if="userData && userData.coverImg" :src="userData.coverImg")
      img.cover-img(v-else src="/img/default_cover.jpg")
    .icon-and-follow.px-2
      .icon
        img.icon-img(src="/account_default.jpg")
      .button
        a(v-if="myProfile.userId == param.userId" href="/settings/profile")
          v-btn.follow-button プロフィールを編集
        v-btn.follow-button(v-else disabled) フォロー
    .name-and-id.pl-2
      .name.text-h5(v-if="!userData")
        ContentLoader.text-h5.loading-text(width="3em")
      .name.text-h5(v-else-if="userData && userData.status != 'ng'") {{ userData.name ? userData.name : userData.userId }}
      .name.text-h5(v-else) unknown user
      .id.text-h7(v-if="!userData")
        ContentLoader.text-h7.loading-text(width="6em")
      .id.text-h7(v-else-if="userData && userData.status != 'ng'") @{{ userData.userId }}
      .id.text-h7(v-else) @unknown
    .message
      .message-content(v-if="!userData") データ取得中…
      .message-content(v-else-if="userData && userData.status != 'ng'" v-html="userData.message ? userData.message : 'ステータスメッセージが設定されていません'")
      .message-content(v-else) ユーザーが存在しません
      //div(v-if="userData")
        p {{ userData }}
    .createdat.my-2
      p(v-if="!userData")
      p(
        v-else-if="userData && userData.status != 'ng'"
        ) {{ new Date(userData.createdAt * 1000) }}からNomad Pulseを利用しています
      p(v-else) ユーザーが存在しません
    .share-and-sns-links.px-2
      .share-buttons
        v-btn.mx-2(
          small
          icon="mdi-twitter"
          color="#1DA1F2"
          :href="`https://twitter.com/`"
        )
        v-btn.mx-2(
          small
          icon="mdi-facebook"
          color="#3b5998"
          :href="`https://facebook.com/`"
        )
        v-btn.mx-2(
          small
          icon="mdi-instagram"
          color="#fc2d9f"
          :href="`https://instagram.com/`"
        )
        v-btn.mx-2(
          small
          icon="mdi-youtube"
          color="#FF0033"
          :href="`https://youtube.com/`"
        )
        v-btn.mx-2(
          small
          icon="mdi-chat"
          color="#06C755"
          :href="`https://lin.ee/`"
        )
        v-btn.mx-2(
          small
          icon="mdi-gmail"
          color="#DC483C"
          :href="`mailto:info@enoki.xyz`"
        )
        v-btn.mx-2(
          small
          icon="mdi-earth"
          color="#1ef783"
          :href="`https://enoki.xyz`"
        )
        v-btn.mx-2(
          small
          icon="mdi-share-variant"
          color="#f7e91e"
          @click="shareMyLinkDialog = true"
        )
    .push-message.mt-4.px-2(v-if="userData && userData.userId")
      v-text-field(
        v-model="pushMessage"
        placeholder="インターネットからお邪魔します！"
        :label="`${userData.userId}にメッセージを送ってみよう！`"
        prepend-inner-icon="mdi-email-edit-outline"
        @keydown.enter="sendPushForAccount(userData.userId)"
        clearable
      )
      v-btn(@click="sendPushForAccount(userData.userId)") {{ userData.userId }}に通知を送信
  commonMainContent(
    :target="param.userId"
  )
v-dialog(v-model="errorMessage" style="max-width: 500px;")
  v-card
    v-card-title 送信失敗
    v-card-text この機能は、ログインしたユーザーのみ使えます！（荒らし対策）
    v-card-actions
      v-spacer
      v-btn(@click="a('/login')") ログイン
      v-btn(@click="errorMessage = false") 閉じる
v-dialog(v-model="successMessage" v-if="userData && userData.userId" style="max-width: 500px;")
  v-card
    v-card-title 送信完了
    v-card-text {{ userData.userId }}に通知を送信しました！
    v-card-actions
      v-spacer
      v-btn(@click="successMessage = false") 閉じる
v-dialog(v-model="isInvalid" style="max-width: 500px;" persistent)
  v-card
    v-card-title 閲覧不可
    v-card-text このユーザーのプロフィールは、ログインしたユーザーのみ閲覧可能です。
    v-card-actions
      v-spacer
      v-btn(@click="$router.back()" prepend-icon="mdi-arrow-left") 戻る
      v-btn(
        @click="a('/login')"
        prepend-icon="mdi-login"
        style="background-color: var(--accent-color); color: white;"
        ) ログイン
v-dialog(v-model="shareMyLinkDialog" max-width="500px")
  v-card
    v-card-title このプロフィールを共有
    v-card-text
      p.mb-4 以下のURLをコピーして、共有してください
      v-btn.mb-4(
        @click="copyMyLink()"
        append-icon="mdi-content-copy"
        style="color: white;"
        color="var(--accent-color)"
      ) コピー
      pre.pa-4(
        style="border-radius: var(--border-radius);"
      ) {{ myLink }}
    v-card-actions
      v-btn(
        @click="shareDialog = false"
        variant="elevated"
        append-icon="mdi-check"
        style="color: white;"
        color="var(--accent-color)"
      ) 閉じる
</template>

<script>
  import mixins from '~/mixins/mixins'
  export default {
    mixins: [mixins],
    data () {
      return {
        param: null,
        userData: null,
        pushMessage: '',
        errorMessage: false,
        successMessage: false,
        isInvalid: false,
        /** 自分のプロフィールリンク */
        myLink: null,
        shareMyLinkDialog: false,
        myProfile: null,
      }
    },
    async mounted () {
      /** ログイン情報 */
      const myProfile = localStorage.getItem('profile')
      if (myProfile) {
        this.myProfile = JSON.parse(myProfile)
        if (this.myProfile?.lastGetLocationTime) {
          this.myProfile.lastGetLocationTime = new Date(this.myProfile.lastGetLocationTime)
        }
        if (this.myProfile?.userId) {
          this.myUserId = this.myProfile.userId
        }
      }
      console.log(this.$route)

      this.param = this.$route.params
      this.userData = await this.getProfile(this.param.userId)
      if (this.userData) {
        if (this.userData.status == 'invalid') {
          // ログインしていないので閲覧不可
          this.isInvalid = true
        }
      } else {
        this.setTitle('unknown user')
      }
      this.myLink = `https://nomadpulse.enoki.xyz/${this.param.userId}`
    },
    methods: {
      sendPushForAccount (userId) {
        if (!this.userStore.userId) {
          this.errorMessage = true
          return false
        }
        if (!this.pushMessage && this.pushMessage === '') {
          return false
        }
        this.sendAjaxWithAuth(
          '/sendPushForAccount.php',
          {
            id: this.userStore.userId,
            token: this.userStore.userToken,
            for: userId,
          },
          {
            title: `${this.userStore.userId}からのメッセージ`,
            message: this.pushMessage,
          },
        )
          .then(e => {
            this.successMessage = true
            console.log(e)
            return true
          })
          .catch(error => {
            console.log(error)
            return false
          })
      },
      /** URLをコピー */
      copyMyLink () {
        navigator.clipboard.writeText(this.myLink)
      },
    },
  }
</script>

<style lang="scss" scoped>
.user-page {
  .profile-zone {
    .cover {
      .cover-img {
        width: 100%;
        aspect-ratio: 4/1;
        object-fit: cover;
      }
    }
    .icon-and-follow {
      display: flex;
      align-items: flex-end;
      margin-top: -36px;
      .icon {
        height: 72px;
        .icon-img {
          border-radius: 9999px;
          width: 72px;
          height: 72px;
          object-fit: cover;
        }
      }
      .button {
        margin-left: auto;
        .follow-button {
          border-radius: 9999px;
          margin: 0 !important;
        }
      }
    }
    .name-and-id {
      .id {
        opacity: 0.7;
      }
    }
    .createdat {
      opacity: 0.7;
    }
  }
}
</style>
