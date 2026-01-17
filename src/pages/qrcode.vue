<template lang="pug">
v-card(
  style="width: 100%; height: 100%;"
  :class="isAndroid15OrHigher ? 'top-android-15-or-higher' : ''"
  )
  v-card-actions
    p.ml-2(style="font-size: 1.3em") QRで友達を探す
    v-spacer
    v-btn(
      text
      @click="$router.back()"
      icon="mdi-close"
      )
  v-card-text(style="height: inherit; overflow-y: auto;")
    .qrcode-stream
      QrcodeStream(@detect="readQrcode")
    .my-16
v-dialog(
  v-model="searchFriendLoading"
  persistent
)
  v-card(width="400")
    v-card-title 処理中…
    v-card-text
      v-progress-linear.my-4(indeterminate)
v-dialog(
  v-model="searchResultDialog"
)
  v-card(width="400")
    v-card-title 検索結果
    v-card-text お探しのユーザーが見つかりませんでした
    v-card-actions
      v-btn(
        @click="searchResultDialog = false"
      ) 閉じる
v-dialog(
  v-model="otherResultDialog"
)
  v-card(width="400")
    v-card-title 検索結果
    v-card-text {{ resultValue }}
    v-card-actions
      v-btn(
        @click="open(resultValue)"
        prepend-icon="mdi-earth"
      ) ブラウザで開く
      v-btn(
        @click="copy(resultValue)"
        prepend-icon="mdi-content-copy"
      ) コピー
      v-btn(
        @click="otherResultDialog = false"
        prepend-icon="mdi-close"
      ) 閉じる
</template>

<script lang="ts">
  import { Browser } from '@capacitor/browser'
  import { Clipboard } from '@capacitor/clipboard'
  import { Device } from '@capacitor/device'
  import { QrcodeStream } from 'vue-qrcode-reader'

  // @ts-ignore
  import mixins from '@/mixins/mixins'

  export default {
    components: {
      QrcodeStream,
    },
    mixins: [mixins],
    data () {
      return {
        isAndroid15OrHigher: true,
        searchFriendLoading: false,
        searchResultDialog: false,
        otherResultDialog: false,
        resultValue: '',
      }
    },
    async mounted () {
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
    methods: {
      readQrcode (content: any) {
        const val = content[0].rawValue as string
        try {
          const url = new URL(val)
          const pathname = url.pathname
          const userId = pathname.match(/\/user\/([^\/]+)\/?$/)
          if (userId) {
            this.searchFriend(userId[1])
            console.log(userId[1])
          } else {
            throw new Error('謎のURL')
          }
        } catch {
          // Invalid URL
          this.resultValue = val
          this.otherResultDialog = true
        }
      },
      /** 友達を検索 */
      async searchFriend (searchId: string) {
        this.searchFriendLoading = true
        if (!searchId) {
          this.searchResultDialog = true
          return
        }
        const userData = await this.getProfile(
          searchId,
        )
        if (userData) {
          this.$router.push(`/user/${userData.userId}`)
        } else {
          this.searchResultDialog = true
          this.searchFriendLoading = false
          return
        }
        this.searchFriendLoading = false
      },
      async copy (content: string) {
        await Clipboard.write({
          string: content,
        })
        this.searchResultDialog = false
      },
      async open (content: string) {
        await Browser.open({
          url: content,
        })
      },
    },
  }
</script>

<style lang="scss" scoped>
.top-android-15-or-higher {
  height: calc(100vh - 40px - 16px)!important;
}
.qrcode-stream {
  width: 100%;
  height: calc(80vh - 40px - 16px);
}
</style>
