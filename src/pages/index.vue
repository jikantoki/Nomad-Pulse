<template lang="pug">
div(style="height: 100%; width: 100%")
  LMap(
    :zoom="leaflet.zoom"
    :center="leaflet.center"
    style="height: calc(100% - 5em); width: 100%"
    :useGlobalLeaflet="false"
    @update:zoom="leaflet.zoom = $event"
    @update:center="leaflet.center = $event"
    :options="{ zoomControl: false }"
    ref="map"
    )
    LTileLayer(
      url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      attribution='&copy; <a href="https://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> contributors'
    )
    //- 自分の現在地マーカー
    MarkerCluster
      LMarker(
        :lat-lng="myLocation"
        @click="detailCardTarget = myProfile"
        )
        LIcon(
          :icon-size="[0,0]"
          style="border: none;"
          :icon-anchor="[16, 16]"
          )
          div(style="display: flex; align-items: center; width: auto;")
            img(
              loading="lazy"
              :src="myProfile?.icon ?? '/account_default.jpg'"
              style="height: 32px; width: 32px; border-radius: 9999px; border: solid 1px #000;"
              onerror="this.src='/account_default.jpg'"
              )
            p.ml-2.name-space(:style="leaflet.zoom >= 15 ? 'opacity: 1;' : 'opacity: 0;'")
              span(v-if="myProfile") {{ myProfile.name ?? myProfile.userId }}
      LMarker(
        v-for="(friend, cnt) of friendList"
        :lat-lng="[friend.location.lat, friend.location.lng]"
        @click="detailCardTarget = friend.friendProfile"
        )
        LIcon(
          :icon-size="[0,0]"
          style="border: none;"
          :icon-anchor="[16, 16]"
          )
          div(
            style="display: flex; align-items: center; width: auto;"
            :class="(diffSeconds(friend.friendProfile.lastGetLocationTime)) > 60 * 60 ? 'opacity05' : ''"
            )
            img(
              loading="lazy"
              :src="friend.friendProfile.icon && friend.friendProfile.icon.length ? friend.friendProfile.icon : '/account_default.jpg'"
              style="height: 32px; width: 32px; border-radius: 9999px; border: solid 1px #000;"
              onerror="this.src='/account_default.jpg'"
              )
            p.ml-2.name-space(:style="leaflet.zoom >= 15 ? 'opacity: 1;' : 'opacity: 0;'")
              span {{ friend.friendProfile.name && friend.friendProfile.name.length && friend.friendProfile.name != 'null' ? friend.friendProfile.name : friend.friendProfile.userId }}
  //-- 下部のアクションバー --
  .action-bar
    .buttons
      .button(
        v-ripple
        @click="setCurrentPosition"
        )
        v-icon mdi-map-marker
        p マップ
      .button(
        v-ripple
        @click="timelineMode = true"
        style="opacity: 0.8;"
        )
        v-icon mdi-chart-timeline-variant
        p タイムライン
      .button(
        v-ripple
        @click="optionsDialog = true"
        style="opacity: 0.8;"
        )
        v-icon mdi-dots-vertical
        p その他
    .bottom-android-15-or-higher(v-if="settings.hidden.isAndroid15OrHigher")
  //-- 右下の現在地ボタン --
  .right-bottom-buttons
    .current-button
      v-btn(
        size="x-large"
        icon
        @click="setCurrentPosition"
        style="background-color: rgb(var(--v-theme-primary)); color: white"
        )
        v-icon mdi-crosshairs-gps
    .bottom-android-15-or-higher(v-if="settings.hidden.isAndroid15OrHigher")
  //-- 左上の友達リストボタン --
  .left-top-buttons
    .top-android-15-or-higher(v-if="settings.hidden.isAndroid15OrHigher")
    .current-button
      v-btn(
        size="x-large"
        icon
        @click="$router.push('/friendlist')"
        style="background-color: rgb(var(--v-theme-primary)); color: white"
        )
        v-icon mdi-account-multiple
  //-- 右上のアカウントボタン --
  .right-top-buttons
    .top-android-15-or-higher(v-if="settings.hidden.isAndroid15OrHigher")
    .account-button
      .button(
        v-ripple
        @click="optionsDialog = true"
        style="cursor: pointer; border-radius: 9999px; height: 4em; width: 4em;"
        )
        img(
          loading="lazy"
          :src="myProfile && myProfile.icon ? myProfile.icon : '/account_default.jpg'"
          style="height: 4em; width: 4em; border-radius: 9999px; border: solid 2px #000;"
          onerror="this.src='/account_default.jpg'"
          )
    .account-button.my-2
      v-btn(
        v-ripple
        @click="$router.push('/qrcode')"
        icon="mdi-qrcode-scan"
        color="rgb(var(--v-theme-primary)"
        size="x-large"
      )
  //- 地図で押したアカウントの詳細カード
  .detail-card-target
    v-card(
      v-if="detailCardTarget"
      style="position: fixed; bottom: 0; left: 0; z-index: 1000; width: 100%; border-radius: 16px 16px 0 0;"
    )
      v-card-actions
        p.ml-2(
          style="display: flex; align-items: center;"
        )
          img.mr-2(
            :src="detailCardTarget.icon && detailCardTarget.icon.length ? detailCardTarget.icon : '/account_default.jpg'"
            style="height: 1.5em; width: 1.5em; border-radius: 9999px;"
            onerror="this.src='/account_default.jpg'"
            )
          span {{ detailCardTarget.name ? detailCardTarget.name : detailCardTarget.userId }}
        v-spacer
        v-btn(
          text
          @click="detailCardTarget = null"
          icon="mdi-close"
          )
      v-card-text
        .info
          v-icon mdi-card-account-details-outline
          p @{{ detailCardTarget.userId }}
        .info
          v-icon(
            v-if="detailCardTarget && detailCardTarget.battery"
            ) {{ chooseBatteryIcon(detailCardTarget.battery.parsent, detailCardTarget.battery.chargingNow) }}
          v-icon(
            v-else
          ) mdi-battery
          p {{ detailCardTarget.battery && detailCardTarget.battery.parsent ? detailCardTarget.battery.parsent.toFixed(0) + '%' : '取得できませんでした' }}
        .info
          v-icon mdi-map-marker-account
          p {{ detailCardTargetAddress ?? '住所取得中...' }}
        .info
          v-icon mdi-clock-outline
          p {{ diffLastGetTime(detailCardTarget.lastGetLocationTime) }}
        .info.pa-4(
          style="background-color: rgba(255, 255, 0, 0.3); border-radius: 16px;"
          v-show="diffSeconds(detailCardTarget.lastGetLocationTime) > 60 * 60"
        )
          v-icon mdi-information-outline
          p この友達は圏外、電源オフ、または位置情報の共有を停止している可能性があります。
        v-btn.my-2(
          text
          v-if="!detailCardTarget.guest"
          @click="$router.push(`/user/${detailCardTarget.userId}`)"
          prepend-icon="mdi-account-circle"
          style="background-color: rgb(var(--v-theme-primary)); width: 100%;"
        ) プロフィールを表示
        v-btn.my-2(
          text
          v-else
          @click="$router.push(`/login`)"
          prepend-icon="mdi-login"
          style="background-color: rgb(var(--v-theme-primary)); width: 100%;"
        ) ログイン
        v-btn.my-2(
          text
          @click="openGoogleMaps(detailCardTarget.location)"
          prepend-icon="mdi-map-marker"
          style="background-color: rgb(var(--v-theme-primary)); width: 100%;"
        ) Google Mapsで開く
  //-- 友達検索ダイアログ --
  v-dialog(
    v-model="searchFriendDialog"
  )
    v-card
      v-card-actions(
        style="width: 90vw;"
      )
        p.ml-2 友達を探す
        v-spacer
        v-btn(
          text
          @click="searchFriendDialog = false"
          icon="mdi-close"
          )
      v-card-text
        v-text-field(
          label="友達のID"
          prepend-icon="mdi-account"
          v-model="searchFriendId"
          @keydown="searchFriendErrorMessage = ''"
          @keydown.enter="searchFriend(searchFriendId)"
        )
        p(
          style="height: 1em;"
        ) {{ searchFriendErrorMessage }}
      v-card-actions
        v-btn(
          @click="searchFriend(searchFriendId)"
          prepend-icon="mdi-magnify"
          style="background-color: rgb(var(--v-theme-primary));"
          :loading="searchFriendLoading"
        ) 検索
  //-- オプションダイアログ --
  v-dialog(
    v-model="optionsDialog"
    transition="dialog-bottom-transition"
    fullscreen
  )
    v-card(
      style="width: 100%; height: 100%;"
    )
      .top-android-15-or-higher(v-if="settings.hidden.isAndroid15OrHigher")
      v-card-actions
        p.ml-2(class="headline" style="font-size: 1.3em") ようこそ
        v-spacer
        v-btn(
          text
          @click="optionsDialog = false"
          icon="mdi-close"
          )
      v-card-text
        .account-details(
          style="display: flex; flex-direction: column; align-items: center; gap: 1em; margin-bottom: 1em;"
        )
          .account-img
            img(
              :src="myProfile && myProfile.icon ? myProfile.icon : '/account_default.jpg'"
              style="height: 8em; width: 8em; border-radius: 9999px;"
              onerror="this.src='/account_default.jpg'"
              )
          .account-info(
            style="text-align: center;"
          )
            p(
              v-if="myProfile.userId && !myProfile.guest"
              style="font-size: 1.2em; margin: 0; padding: 0;"
              ) {{ myProfile.name ? myProfile.name : myProfile.userId }}
            p(
              v-else
              style="font-size: 1.2em; margin: 0; padding: 0;"
              ) ログインしていません
            p(style="margin: 0; padding: 0;")
              | {{ myProfile.userId && !myProfile.guest ? `@${myProfile.userId}` : 'データは同期されていません' }}
            v-btn.my-2(
              v-if="myProfile.userId && !myProfile.guest"
              text
              @click="$router.push(`/user/${myProfile.userId}`)"
              append-icon="mdi-account-outline"
              style="background-color: rgb(var(--v-theme-primary));"
            ) プロフィールを表示
            v-btn.my-2(
              v-else
              text
              @click="$router.push('/login')"
              append-icon="mdi-login"
              style="background-color: rgb(var(--v-theme-primary)); color: white;"
            ) ログイン
        v-list.options-list
          v-list-item.item( @click="timelineMode = true" )
            .icon-and-text
              v-icon mdi-chart-timeline-variant
              v-list-item-title タイムライン
          v-list-item.item( @click="searchFriendDialog = true" )
            .icon-and-text
              v-icon mdi-magnify
              v-list-item-title 友達を探す
          v-list-item.item( @click="$router.push('qrcode')" )
            .icon-and-text
              v-icon mdi-qrcode-scan
              v-list-item-title QRコードで友達を探す
          v-list-item.item(
            @click="$router.push('/friendlist')"
            v-show="myProfile && myProfile.userId"
          )
            .icon-and-text
              v-icon mdi-account-multiple
              v-list-item-title 友達リスト
          v-list-item.item( @click="$router.push('/settings')" )
            .icon-and-text
              v-icon mdi-cog
              v-list-item-title 設定
          v-list-item.item( @click="$router.push('/terms')" )
            .icon-and-text
              v-icon mdi-file-document-outline
              v-list-item-title 利用規約
          v-list-item.item( @click="openURL('https://enoki.xyz/privacy')" )
            .icon-and-text
              v-icon mdi-shield-lock-outline
              v-list-item-title プライバシーポリシー
          v-list-item.item( @click="$router.push('/about')" )
            .icon-and-text
              v-icon mdi-information
              v-list-item-title このアプリについて
          v-list-item.item( @click="share('https://play.google.com/store/apps/details?id=xyz.enoki.nomadpulse', 'Nomad Pulse')" )
            .icon-and-text
              v-icon mdi-share-variant
              v-list-item-title このアプリを共有する
  //-- 位置情報利用許可ダイアログ --
  v-dialog(
    v-model="requestGeoPermissionDialog"
    persistent
    max-width="400"
  )
    v-card
      v-card-title(class="headline") 位置情報利用のお願い
      v-card-text
        .text-content
          p このアプリは現在地の表示、及びお互いに承認した友達と共有するために位置情報を利用します。
          .my-2
          p アプリを閉じている間や使用していない間も、正確な移動履歴を記録するためにバックグラウンドで位置情報を取得します。
          .my-2
          p また、バックグラウンドでもアプリを動かすために、通知の許可も必要です。
          .my-2
          p 続行するには、以下の「ええで！」を押してください。
        .privacy-policy-link.mt-4
          p
            span 詳細は
            a.pa-2.ma-2(
              @click="openURL('https://enoki.xyz/privacy')"
              style="background-color: rgb(var(--v-theme-primary)); color: white; cursor: pointer; border-radius: 8px;"
            ) プライバシーポリシー
            span をご覧ください。
      v-card-actions
        v-spacer
        v-btn(
          text
          @click="requestGeoPermissionDialog = false; requestBackgroundDialog = true"
          prepend-icon="mdi-close"
          ) 嫌だ！
        v-btn(
          style="background-color: rgb(var(--v-theme-primary)); color: white"
          text
          @click="requestGeoPermission"
          prepend-icon="mdi-check"
          ) ええで！
  //-- バックグラウンド許可ダイアログ
  v-dialog(
    v-model="requestBackgroundDialog"
    persistent
    max-width="600"
  )
    v-card
      v-card-title(class="headline") バックグラウンドでの通知の許可
      v-card-text
        p このアプリはバックグラウンドでも動き続けます。端末側の設定画面より、以下の操作を実行してください。
        ol.ma-6
          li 「ええで！」を押して「バッテリー使用量設定」を開く
          li リストから「Nomad Pulse」を選択する
          li 「バックグラウンドでの使用を許可」を開き、「制限なし」を選択
          li その後、「戻る」操作でこのアプリまで戻ってきてください！
        br
        p ちなみに、タスクキルをすると位置情報が更新できなくなるため、タスクキルはしないようにお願いします。
      v-card-actions
        v-spacer
        v-btn(
          text
          @click="requestBackgroundDialog = false"
          prepend-icon="mdi-close"
          ) 嫌だ！
        v-btn(
          style="background-color: rgb(var(--v-theme-primary)); color: white"
          text
          @click="requestBackground"
          prepend-icon="mdi-check"
          ) ええで！
  //-- タイムラインモード --
  v-dialog(
    v-model="timelineMode"
    fullscreen
    transition="dialog-bottom-transition"
  )
    v-card(
      style="width: 100%; height: 100%;"
    )
      .top-android-15-or-higher(v-if="settings.hidden.isAndroid15OrHigher")
      v-card-actions
        p.ml-2(class="headline" style="font-size: 1.3em") タイムライン
        v-spacer
        v-btn(
          text
          @click="timelineMode = false"
          icon="mdi-close"
          )
      v-card-text(style="height: inherit; overflow-y: auto;")
        p ここにタイムラインコンテンツを表示します。
        .timeline(
          v-for="(timeline, index) of sortedTimeline"
          :key="index"
        )
          .timeline-entry
            .timeline-entry-header
            .timeline-entry-body
              p(
                v-if="timeline.address"
              ) 住所: {{ timeline.address }}
              p(
                v-else
              ) 位置: 緯度 {{ timeline.lat.toFixed(5) }}, 経度 {{ timeline.lng.toFixed(5) }}
              p 時間: {{ new Date(timeline.startTimestamp).toLocaleString() }}～{{ new Date(timeline.endTimestamp).toLocaleString() }}
  v-dialog(
    v-model="acceptDialog"
    persistent
  )
    v-card
      v-card-title 友達リクエストが来ています！
      v-card-text
        p {{ acceptList.length }}人の友達があなたを待っています。承認してつながろう！
      v-card-actions
        v-btn(
          @click="acceptDialog = false"
        ) やめとく
        v-btn(
          @click="$router.push('/friendlist')"
          style="background-color: rgb(var(--v-theme-primary)); color: white"
        ) リクエストを見る
</template>

<script lang="ts">
  import type { BackgroundGeolocationPlugin } from '@capacitor-community/background-geolocation'
  import { App } from '@capacitor/app'
  import { BackgroundRunner } from '@capacitor/background-runner'
  import { Browser } from '@capacitor/browser'
  import { Capacitor, CapacitorHttp, registerPlugin } from '@capacitor/core'
  import { Device } from '@capacitor/device'
  import { Directory, Encoding, Filesystem, type ReadFileResult } from '@capacitor/filesystem'
  import { Geolocation, type Position } from '@capacitor/geolocation'
  import { Share } from '@capacitor/share'
  import { Toast } from '@capacitor/toast'
  import { LIcon, LMap, LMarker, LTileLayer } from '@vue-leaflet/vue-leaflet'
  import { AndroidSettings, IOSSettings, NativeSettings } from 'capacitor-native-settings'
  import MarkerCluster from '@/components/MarkerCluster.vue'

  import muniArray from '@/js/muni'
  // @ts-ignore
  import mixins from '@/mixins/mixins'
  import { useMyProfileStore } from '@/stores/myProfile'
  import { useSettingsStore } from '@/stores/settings'
  import 'leaflet/dist/leaflet.css'
  const BackgroundGeolocation = registerPlugin<BackgroundGeolocationPlugin>('BackgroundGeolocation')

  interface TimelineEntry {
    lat: number
    lng: number
    timestamp: string
  }

  export default {
    components: {
      LMap,
      LMarker,
      LTileLayer,
      LIcon,
      MarkerCluster,
    },
    mixins: [mixins],
    data () {
      return {
        /** Leafletの設定 */
        leaflet: {
          zoom: 13,
          center: [35.690_430_765_555_42, 139.700_211_526_229_54],
        },
        /** 位置情報利用許可ダイアログの表示フラグ */
        requestGeoPermissionDialog: false,
        /** 自分の現在地 */
        myLocation: [0, 0],
        /** 最後に取得した位置情報 */
        lastGetLocation: [0, 0],
        /** 自分のバッテリー残量 */
        myBatteryPersent: 0 as number | undefined,
        /** 充電中かどうか */
        chargeingNow: false as boolean | undefined,
        /** 詳細カードのターゲット */
        detailCardTarget: null as {} | null,
        /** 詳細カードに現在の住所を表示 */
        detailCardTargetAddress: null as string | null,
        /** オプションダイアログの表示フラグ */
        optionsDialog: false,
        /** タイムラインモードかどうか */
        timelineMode: false,
        /** 自分のプロフィール */
        myProfile: useMyProfileStore(),
        /** 自分の位置情報を最後にいつ取得したか？ */
        lastGetMyLocationTime: null as Date | null,
        /** 最後にサーバーに位置情報を送信した時間 */
        lastUpdateMyLocationTime: new Date(0),
        /** バックグラウンド許可が欲しいダイアログフラグ */
        requestBackgroundDialog: false,
        /** 友達検索ダイアログ */
        searchFriendDialog: false,
        /** 検索する友達のID */
        searchFriendId: '',
        /** 友達検索中のローディング画面 */
        searchFriendLoading: false,
        /** 友達検索画面のエラー表示 */
        searchFriendErrorMessage: '',
        /** 環境変数 */
        env: null as any,
        /** 承認待ち友達リスト */
        acceptList: [] as any,
        /** 承認してほしい友達がいるダイアログ */
        acceptDialog: false,
        /** setIntervalしたものをクリアする用 */
        updateLocationInterval: null as any,
        /** 友達リスト */
        friendList: [] as any[],
        /** 設定ストア */
        settings: useSettingsStore(),
        /** タイムラインデータ */
        timelineData: [] as {
          lat: number
          lng: number
          /** Date.toLocaleString() */
          startTimestamp: string
          /** Date.toLocaleString() */
          endTimestamp: string
          address: string | null
        }[],
        lastUpdatedTimeline: {
          lat: 0,
          lng: 0,
          timestamp: new Date(0).toLocaleDateString(),
        } as TimelineEntry,
      }
    },
    computed: {
      /** 新しい順タイムライン */
      sortedTimeline () {
        // eslint-disable-next-line vue/no-side-effects-in-computed-properties, unicorn/no-array-sort
        return this.timelineData.sort((a, b) => {
          return new Date(b.endTimestamp).getTime() - new Date(a.endTimestamp).getTime()
        })
      },
    },
    watch: {
      /** プロフィール詳細を押された時に、現在の住所を表示する */
      detailCardTarget: {
        handler: async function (newProfile) {
          if (!newProfile || !newProfile.location) {
            this.detailCardTargetAddress = null
            return
          } else {
            const latlng = newProfile.location
            this.detailCardTargetAddress = await this.getAddress(latlng[0], latlng[1])
            return
          }
        },
        deep: true,
        immediate: true,
      },
      /** ようこそ画面の表示状態を保存 */
      optionsDialog: {
        handler: async function (dialog: boolean) {
          localStorage.setItem('welcomeDialog', String(dialog))
        },
      },
      timelineMode: {
        handler: async function (mode: boolean) {
          if (!mode) {
            return
          }
          // タイムラインを開いたタイミングで、住所を取得する
          let cnt = 0
          for (const entry of this.timelineData) {
            if (entry.address) {
              cnt++
              continue
            }
            this.timelineData[cnt]!.address = await this.getAddress(entry.lat, entry.lng)
            cnt++
          }
          await Filesystem.writeFile({
            path: 'timeline.json',
            directory: Directory.Data,
            encoding: Encoding.UTF8,
            data: JSON.stringify(this.timelineData),
          })
        },
      },
    },
    async mounted () {
      // @ts-ignore
      this.env = import.meta.env as any

      /** ようこその復活 */
      const welcomeDialog = localStorage.getItem('welcomeDialog')
      if (welcomeDialog && welcomeDialog.toLowerCase() == 'true') {
        this.optionsDialog = true
      }

      /** ローカルストレージから最後に取得した位置情報を読み込み */
      const localStorageLatlng = localStorage.getItem('latlng')
      if (localStorageLatlng) {
        this.myLocation = JSON.parse(localStorageLatlng)
        this.lastGetLocation = JSON.parse(localStorageLatlng)
        /** バグるので0.1秒待ってから地図の中心を設定 */
        setTimeout(() => {
          this.leaflet.center = this.myLocation
          this.leaflet.zoom = 15
        }, 100)
      }

      /** ログイン情報 */
      if (this.myProfile.$state.guest == false) {
        if (this.myProfile.lastGetLocationTime) {
          this.myProfile.lastGetLocationTime = new Date(this.myProfile.lastGetLocationTime)
        }

        setTimeout(async () => {
          const token = this.myProfile.userToken
          const profile: any = await this.getProfile(this.myProfile.userId ?? '')
          if (profile) {
            profile.userToken = token
            profile.battery = this.myProfile.battery
            profile.guest = false
            this.myProfile = {
              ...this.myProfile,
              ...profile,
            }
          }
        }, 100)
      } else {
        this.myProfile.reset()
      }

      const locationList = localStorage.getItem('locationList')
      if (locationList) {
        this.friendList = JSON.parse(locationList)
        let cnt = 0
        for (const friend of this.friendList) {
          this.friendList[cnt].friendProfile.lastGetLocationTime = new Date(friend.location.unixtime * 1000)
          cnt++
        }
      }

      /** 位置情報の許可を確認 */
      if (Capacitor.getPlatform() === 'web') {
        /** 位置情報の許可を確認 */
        Geolocation.checkPermissions().then(result => {
          if (result.location === 'granted') {
            this.setCurrentPosition()
          } else {
            Geolocation.requestPermissions().then(result => {
              if (result.location === 'granted') {
                this.setCurrentPosition()
              }
            }).catch(() => {
              if (Capacitor.getPlatform() === 'web') {
                this.requestGeoPermissionDialog = true
              }
            })
          }
        })
      } else {
        /** スマホの場合、この方法で位置情報と通知を許可してもらう */
        const perm = await Geolocation.checkPermissions()
        if (perm.coarseLocation != 'granted' && perm.location != 'granted') {
          const permission = await BackgroundRunner.checkPermissions()
          if (permission.geolocation !== 'granted') {
            this.requestGeoPermissionDialog = true
          }
        }
      }

      /** バックボタンのリスナーを追加 */
      App.addListener('backButton', () => {
        if (this.timelineMode) {
          /** タイムラインモードを閉じる */
          this.timelineMode = false
        } else if (this.searchFriendDialog) {
          // 友達検索ダイアログを閉じる
          this.searchFriendDialog = false
        } else if (this.acceptDialog) {
          // 友達承認しろダイアログを閉じる
          this.acceptDialog = false
        } else if (this.optionsDialog) {
          /** オプションダイアログを閉じる */
          this.optionsDialog = false
        } else if (this.requestGeoPermissionDialog) {
          /** 位置情報利用許可ダイアログを閉じる */
          this.requestGeoPermissionDialog = false
        } else if (this.detailCardTarget) {
          /** 詳細カードを閉じる */
          this.detailCardTarget = null
        } else if (this.requestBackgroundDialog) {
          // ここはあえて何もしない
        } else if (this.requestGeoPermissionDialog) {
          // ここはあえて何もしない
        } else if (this.$route.path === '/') {
          /** ルートページならアプリを最小化 */
          App.minimizeApp()
          Toast.show({ text: 'アプリはバックグラウンドで実行されます' })
        } else {
          /** ルート以外のページなら1つ戻る */
          this.$router.back()
        }
      })

      // ローカルにタイムラインデータを保存
      /** タイムラインデータ */
      let file: ReadFileResult
      try {
        file = await Filesystem.readFile({
          path: 'timeline.json',
          directory: Directory.Data,
          encoding: Encoding.UTF8,
        })
      } catch {
        await Filesystem.writeFile({
          path: 'timeline.json',
          directory: Directory.Data,
          encoding: Encoding.UTF8,
          data: JSON.stringify([]),
        })

        file = await Filesystem.readFile({
          path: 'timeline.json',
          directory: Directory.Data,
          encoding: Encoding.UTF8,
        })
      }

      // @ts-ignore
      this.timelineData = JSON.parse(file.data)

      // Webの場合は従来のAPIで位置情報追跡
      if (Capacitor.getPlatform() === 'web') {
        /** 現在地を監視 */
        Geolocation.watchPosition({
          enableHighAccuracy: true,
          timeout: 20_000,
          interval: 20_000,
        }, position =>
          this.watchPosition(position),
        )
      }

      // ネイティブアプリでは位置情報のバックグラウンド追跡
      if (Capacitor.getPlatform() !== 'web') {
        BackgroundGeolocation.addWatcher({
          backgroundMessage: 'バックグラウンドで位置情報を取得しています。タスクキルしないでください。',
          backgroundTitle: '位置情報取得中',
          distanceFilter: 5,
          requestPermissions: false,
        }, (location, error) => {
          // あいまいな位置情報でも誤検知するので一旦消している
          // if (error) {
          //   if (error.code === 'NOT_AUTHORIZED') {
          //     this.requestBackgroundDialog = true
          //   }
          //   return console.error(error)
          // }

          const position = {
            coords: {
              latitude: location?.latitude,
              longitude: location?.longitude,
            },
          }
          if (position.coords.latitude
            && position.coords.longitude) {
            this.lastGetLocation = [
              position.coords.latitude,
              position.coords.longitude,
            ]
          }
          // this.watchPosition(position)
          return location
        }).then(watcherId => {
          localStorage.setItem('watcherId', watcherId)
        })
      }

      // 5秒に一回、位置情報に関わらずサーバーにリクエストを送る
      setInterval(() => {
        if (this.lastGetLocation[0] && this.lastGetLocation[1]) {
          this.watchPosition({
            coords: {
              latitude: this.lastGetLocation[0],
              longitude: this.lastGetLocation[1],
            },
          })
        }
      }, 1000 * 5)

      // Getパラメータから友達IDがあればその人の位置情報場所に飛ぶ
      const urlParams = new URLSearchParams(window.location.search)
      const friendId = urlParams.get('viewUser')
      if (friendId) {
        const friend = this.friendList.find(f => f.friendProfile.userId === friendId)
        if (friend) {
          this.leaflet.center = [
            friend.location.lat,
            friend.location.lng,
          ]
        }
        this.optionsDialog = false
      } else {
        /** 現在地を取得し、地図の中心も移動 */
        await this.setCurrentPosition()
      }

      /** 遅延も考慮して0.5秒後に再度中心移動 */
      setTimeout(() => {
        // Getパラメータから友達IDがあればその人の位置情報場所に飛ぶ
        const urlParams = new URLSearchParams(window.location.search)
        const friendId = urlParams.get('viewUser')
        if (friendId) {
          const friend = this.friendList.find(f => f.friendProfile.userId === friendId)
          if (friend) {
            this.leaflet.center = [
              friend.location.lat,
              friend.location.lng,
            ]
          }
          urlParams.delete('viewUser')
          history.replaceState(null, '', window.location.pathname + '?' + urlParams.toString())
          history.pushState(null, '', window.location.pathname + '?' + urlParams.toString())
        } else {
          /** 現在地を取得し、地図の中心も移動 */
          this.setCurrentPosition()
        }
      }, 500)

      // 承認していない友達リクエストがあったらポップアップを表示
      const res: any = await this.sendAjaxWithAuth('/getMyFriendList.php', {
        id: this.myProfile.userId,
        token: this.myProfile.userToken,
        withLocation: true,
      })
      if (res && res.body) {
        const allFriendList: any[] = res.body.friendList
        this.acceptList = []
        if (allFriendList && allFriendList[0]) {
          for (const friend of allFriendList) {
            friend.friendProfile.userId = friend.friendRealId
            if (friend.status == 'request' && friend.fromUserId != res.body.mySecretId) {
              this.acceptList.push(friend.friendProfile)
            }
          }
        }
      }
      if (this.acceptList.length > 0 && history.length <= 2) {
        this.acceptDialog = true
      }

      /** 友達の現在地を更新 */
      this.updateLocation()
      /** 10秒に1回、サーバーと通信して位置情報を更新 */
      this.updateLocationInterval = setInterval(
        async () => this.updateLocation(),
        10_000)
    },
    unmounted () {
      App.removeAllListeners()
      clearInterval(this.updateLocationInterval)
    },
    methods: {
      /** 位置情報監視のコールバック */
      async watchPosition (position: {
        coords: {
          [key: string]: any
        }
      } | null) {
        if (this.diffSeconds(this.lastUpdateMyLocationTime) < 5) {
          // 5秒以内に更新があったら処理をキャンセル
          return false
        }
        // console.log('最終更新:', new Date(this.lastUpdateMyLocationTime))
        if (position) {
          const lat: number = position.coords.latitude
          const lng: number = position.coords.longitude
          this.lastGetLocation = [lat, lng]
          this.myLocation = [lat, lng]
          const lastGetMyLocationTime = new Date()
          localStorage.setItem('latlng', JSON.stringify(this.myLocation))
          this.myProfile.lastGetLocationTime = lastGetMyLocationTime
          this.myProfile.location = [lat, lng]

          const now = new Date()
          // if (this.isWithin10Seconds(this.lastUpdateMyLocationTime, now)) {
          //   console.log('10秒以内の位置情報更新はサーバーに送信しません')
          //   return
          // }
          this.lastUpdateMyLocationTime = now

          /** バッテリー情報を取得 */
          const info = await Device.getBatteryInfo()
          let batteryLevel = null
          let batteryCharging = null
          if (info.batteryLevel) {
            batteryLevel = info.batteryLevel * 100
            batteryCharging = info.isCharging
            this.myBatteryPersent = batteryLevel
            if (this.myProfile) {
              this.myProfile.battery = {
                parsent: info.batteryLevel * 100,
                chargingNow: batteryCharging,
              }
            }
          }
          this.chargeingNow = info.isCharging

          if (this.myProfile && !this.myProfile.guest) {
            /** 取得した情報をサーバーに送信 */
            try {
              await CapacitorHttp.post({
                url: 'https://api.nomadpulse.enoki.xyz/updateGeoLocation.php',
                headers: {
                  'Content-Type': 'application/json',
                  'id': this.myProfile.userId,
                  'token': this.myProfile.userToken ?? '',
                  'lat': String(lat),
                  'lng': String(lng),
                  'batteryLevel': String(batteryLevel),
                  'batteryCharging': String(batteryCharging),
                  'apiid': this.env.VUE_APP_API_ID,
                  'apitoken': this.env.VUE_APP_API_TOKEN,
                  'apipassword': this.env.VUE_APP_API_ACCESSKEY,
                },
              })
            } catch (error) {
              console.error(error)
              const res = await this.sendAjaxWithAuth(
                '/updateGeoLocation.php', {
                  id: this.myProfile.userId,
                  token: this.myProfile.userToken,
                  lat,
                  lng,
                  batteryLevel,
                  batteryCharging,
                },
              )
              if (!res || !res.body) {
                console.error('サーバー送信エラー', res)
              }
            }
          }

          /** タイムラインデータをストレージと同期 */
          let file: ReadFileResult
          try {
            file = await Filesystem.readFile({
              path: 'timeline.json',
              directory: Directory.Data,
              encoding: Encoding.UTF8,
            })
          } catch {
            await Filesystem.writeFile({
              path: 'timeline.json',
              directory: Directory.Data,
              encoding: Encoding.UTF8,
              data: JSON.stringify([]),
            })

            file = await Filesystem.readFile({
              path: 'timeline.json',
              directory: Directory.Data,
              encoding: Encoding.UTF8,
            })
          }
          // @ts-ignore
          this.timelineData = JSON.parse(file.data)

          // @ts-ignore
          const distance = this.calcDistance(
            [lat, lng],
            [this.lastUpdatedTimeline.lat, this.lastUpdatedTimeline.lng],
          )
          if (distance < 20 && this.timelineData.length > 0) {
            // 20メートル以内の移動なら、最後のデータの終了時間を更新するだけにする
            this.timelineData.at(-1)!.endTimestamp = new Date().toLocaleString()
          } else {
            this.timelineData.push({
              lat,
              lng,
              startTimestamp: new Date().toLocaleString(),
              endTimestamp: new Date().toLocaleString(),
              address: null,
            })
            this.lastUpdatedTimeline = {
              lat,
              lng,
              timestamp: new Date().toLocaleString(),
            }
          }
          // ローカルにタイムラインデータを保存
          await Filesystem.writeFile({
            path: 'timeline.json',
            directory: Directory.Data,
            encoding: Encoding.UTF8,
            data: JSON.stringify(this.timelineData),
          })
        }

        return
      },
      /** 現在地を取得し、地図の中心も移動 */
      async setCurrentPosition () {
        /** 仮で最後に取得した位置情報を地図の中心に設定 */
        this.leaflet.center = this.lastGetLocation
        /** バグるので0.5秒待つ */
        await setTimeout(() => {}, 500)
        this.leaflet.zoom = 15
      },
      /** 位置情報の許可を求める */
      async requestGeoPermission () {
        if (Capacitor.getPlatform() === 'web') {
          await Geolocation.getCurrentPosition()
          this.requestGeoPermissionDialog = false
          return
        }

        const permissions = await BackgroundRunner.requestPermissions({
          apis: ['geolocation', 'notifications'],
        })
        if (permissions.geolocation === 'granted') {
          this.setCurrentPosition()
        }

        this.requestGeoPermissionDialog = false

        /** バックグラウンドでの使用許可設定メソッドへの準備 */
        this.requestBackgroundDialog = true
      },
      /** 2地点間の距離（メートル）を計算 */
      calcDistance (latlng1: [number, number], latlng2: [number, number]) {
        const R = Math.PI / 180
        const lat1 = latlng1[0] * R
        const lat2 = latlng2[0] * R
        const lng1 = latlng1[1] * R
        const lng2 = latlng2[1] * R
        return 6371e3 * Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lng2 - lng1)) as number
      },
      /** バッテリーアイコンを選択 */
      chooseBatteryIcon (batteryPersent: number | undefined, chargingNow: boolean | undefined) {
        if (batteryPersent === undefined) {
          return 'mdi-battery-unknown'
        }
        let returnText = 'mdi-battery-'
        if (chargingNow) {
          returnText += 'charging-'
        }
        if (batteryPersent >= 95) {
          // 100%表示の時だけこれ
          return 'mdi-battery'
        } else if (batteryPersent >= 90) {
          return returnText + '90'
        } else if (batteryPersent >= 80) {
          return returnText + '80'
        } else if (batteryPersent >= 60) {
          return returnText + '60'
        } else if (batteryPersent >= 40) {
          return returnText + '40'
        } else if (batteryPersent >= 20) {
          return returnText + '20'
        } else {
          return returnText + '10'
        }
      },
      /** 秒比較 */
      diffSeconds (date: Date | null | undefined) {
        if (!date) {
          return 999_999
        }

        const now = new Date()
        /** 差分秒 */
        const diff = (now.getTime() - date.getTime()) / 1000
        return diff
      },
      /** 現在時刻と位置情報を最後に取得した時間を比較 */
      diffLastGetTime (date: Date | null | undefined) {
        if (!date) {
          return ''
        }
        /** 差分秒 */
        const diff = this.diffSeconds(date)
        if (diff < 30) {
          return 'たった今'
        } else if (diff < 60) {
          return `${Math.floor(diff)}秒前`
        } else if (diff < 60 * 60) {
          return `${Math.floor(diff / 60)}分前`
        } else if (diff < 60 * 60 * 24) {
          return `${Math.floor(diff / 60 / 60)}時間前`
        } else {
          return `${Math.floor(diff / 60 / 60 / 24)}日前`
        }
      },
      openGoogleMaps (latlng: [
        number, number,
      ]) {
        this.openURL(
          `https://www.google.com/maps/search/?api=1&query=${latlng[0]},${latlng[1]}`,
        )
      },
      /** URLをブラウザで開く */
      async openURL (url: string) {
        await Browser.open({ url: url })
      },
      /** 位置情報のバックグラウンド追跡設定を開く */
      async requestBackground () {
        await NativeSettings.open({
          optionAndroid: AndroidSettings.BatteryOptimization,
          optionIOS: IOSSettings.App,
        })
        this.requestBackgroundDialog = false
        this.setCurrentPosition()
      },
      /** 友達を検索 */
      async searchFriend (searchId: string) {
        searchId = searchId.replace('@', '')
        this.searchFriendLoading = true
        if (!searchId) {
          this.searchFriendErrorMessage = '検索するIDを入力してください'
          this.searchFriendLoading = false
          return
        }
        const userData = await this.getProfile(
          searchId,
          this.myProfile.userId,
          this.myProfile.userToken,
        )
        if (userData) {
          this.$router.push(`/user/${userData.userId}`)
        } else {
          this.searchFriendErrorMessage = '友達が見つかりませんでした'
          this.searchFriendLoading = false
          return
        }
        this.searchFriendLoading = false
      },
      /** 友達の位置情報を取得して更新 */
      async updateLocation () {
        const res = await this.sendAjaxWithAuth('/getMyFriendList.php', {
          id: this.myProfile?.userId,
          token: this.myProfile?.userToken,
          withLocation: true,
        })
        if (res && res.body && res.body.friendList) {
          const friendList = []
          // friend.friendProfileがdetailCardTargetに反映される
          for (const friend of res.body.friendList) {
            if (friend.status == 'friend' && friend.location) {
              friend.friendProfile.userId = friend.friendRealId
              friend.friendProfile.battery = {
                parsent: friend.location.batteryLevel,
                chargingNow: friend.location.batteryCharging ? true : false,
              }
              friend.friendProfile.location = [
                friend.location.lat,
                friend.location.lng,
              ]
              friend.friendProfile.lastGetLocationTime = new Date(friend.location.unixtime * 1000)
              friendList.push(friend)
            }
          }
          this.friendList = friendList
          localStorage.setItem('locationList', JSON.stringify(friendList))
        }
      },
      /**
       * 2つのDateオブジェクトの差が10秒以内か判定する
       * @param {Date} date1 - 比較する1つ目の日付
       * @param {Date} date2 - 比較する2つ目の日付
       * @returns {boolean} 10秒以内ならtrue
       */
      isWithin10Seconds (date1: Date, date2: Date) {
        // 10秒 = 10000ミリ秒
        const diffInMilliseconds = Math.abs(date1.getTime() - date2.getTime())
        return diffInMilliseconds <= 10_000
      },
      /**
       * 緯度経度から住所を取得
       * @param lat 緯度
       * @param lng 経度
       */
      async getAddress (lat: number, lng: number) {
        const geoCodingUrl = new URL(
          'https://mreversegeocoder.gsi.go.jp/reverse-geocoder/LonLatToAddress',
        )
        geoCodingUrl.searchParams.set('lat', String(lat))
        geoCodingUrl.searchParams.set('lon', String(lng))
        return fetch(geoCodingUrl.toString())
          .then(res => res.json())
          .then(json => {
            const data = json.results
            const muniData = muniArray[data.muniCd]
            if (!muniData) {
              return '住所不明'
            }
            const splitedMuniData = muniData.split(',')
            const pref = splitedMuniData[1]
            const city = splitedMuniData[3]
            const address = data.lv01Nm
            return `${pref}${city}${address}`
          })
          .catch(() => {
            return null
          })
      },
      /** シェアダイアログ */
      async share (content: string, title = '') {
        await Share.share({
          url: content,
          title: title,
        })
      },
    },
  }
</script>

<style lang="scss" scoped>
.right-bottom-buttons {
  position: fixed;
  right: 16px;
  bottom: calc(16px + 4em);
  display: flex;
  flex-direction: column;
  gap: 8px;
  z-index: 1000;

  .current-button {
    background-color: white;
    border-radius: 50%;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  }
}
.right-top-buttons {
  position: fixed;
  right: 16px;
  top: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  z-index: 1000;

  .account-button {
    border-radius: 50%;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  }
}
.left-top-buttons {
  position: fixed;
  left: 16px;
  top: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  z-index: 1000;

  .account-button {
    border-radius: 50%;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  }
}

.name-space {
  font-size: 16px;
  font-weight: 500;
  white-space: nowrap;
  -webkit-text-stroke: 2px black;
  paint-order: stroke;
  color: white;
  transition: all 1s;
}

.action-bar{
  position: fixed;
  bottom: 0;
  left: 0;
  background-color: rgb(var(--v-theme-surface));
  z-index: 500;
  width: 100%;
  align-items: center;
  box-shadow: 0 -2px 4px rgba(0, 0, 0, 0.3);
  .buttons{
    width: 100%;
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: space-evenly;
    height: 4em;
    .button {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      width: 6em;
      border-radius: 1em;
      height: 80%;
      cursor: pointer;
      color: rgb(var(--v-theme-on-surface));

      v-icon {
        font-size: 24px;
      }

      p {
        font-size: 10px;
        margin: 0;
        padding: 0;
      }
    }
  }
  .bottom-android-15-or-higher {
    width: 100%;
  }
}

.bottom-android-15-or-higher {
  height: 16px;
}
.top-android-15-or-higher {
  height: 40px;
}

.options-list {
  .item {
    padding : 12px 16px;
    border-radius: 12px!important;
    margin: 8px 0;
    cursor: pointer;
    &:hover {
      background-color: rgba(var(--v-theme-primary), 0.1);
    }
    .icon-and-text {
      display: flex;
      flex-direction: row;
      align-items: center;
      gap: 16px;
      v-icon {
        font-size: 24px;
      }
    }
  }
}

.detail-card-target {
  .info{
    display: flex;
    flex-direction: row;
    align-items: center;
    gap: 1em;
    margin: 1em 0;
  }
}

.opacity05 {
  opacity: 0.7;
}
</style>
