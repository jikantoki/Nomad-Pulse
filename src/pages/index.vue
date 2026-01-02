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
    )
    LTileLayer(
      url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      attribution='&copy; <a href="https://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> contributors'
    )
    //- 自分の現在地マーカー
    LMarker(
      :lat-lng="myLocation"
      @click="detailCardTarget = 'myUser'"
      )
      LIcon(
        :icon-size="[0,0]"
        style="border: none;"
        :icon-anchor="[16, 16]"
        )
        div(style="display: flex; align-items: center; width: auto;")
          img(src="/account_default.jpg" style="height: 32px; width: 32px; border-radius: 9999px;")
          p.ml-2.name-space(:style="leaflet.zoom >= 15 ? 'opacity: 1;' : 'opacity: 0;'")
            span あなたの現在地
  //-- 下部のアクションバー --
  .action-bar
    .buttons
      .button(v-ripple @click="timelineMode = false")
        v-icon mdi-map-marker
        p マップ
      .button(v-ripple @click="timelineMode = true")
        v-icon mdi-chart-timeline-variant
        p タイムライン
      .button(v-ripple @click="optionsDialog = true")
        v-icon mdi-dots-vertical
        p その他
    .bottom-android-15-or-higher(v-if="isAndroid15OrHigher")
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
    .bottom-android-15-or-higher(v-if="isAndroid15OrHigher")
  //-- 右上のアカウントボタン --
  .right-top-buttons
    .top-android-15-or-higher(v-if="isAndroid15OrHigher")
    .account-button
      .button(
        v-ripple
        @click="optionsDialog = true"
        style="cursor: pointer; border-radius: 9999px; height: 3em; width: 3em;"
        )
        img(src="/account_default.jpg" style="height: 3em; width: 3em; border-radius: 9999px;")
  //- 地図で押したアカウントの詳細カード
  .detail-card-target
    v-card(
      v-if="detailCardTarget"
      style="position: fixed; bottom: 0; left: 0; z-index: 1000; width: 100%; height: 16em; border-radius: 16px 16px 0 0;"
    )
      v-card-actions
        p.ml-2 {{ detailCardTarget }}
        v-spacer
        v-btn(
          text
          @click="detailCardTarget = null"
          icon="mdi-close"
          )
      v-card-text
        .info
          v-icon {{ chooseBatteryIcon(myBatteryPersent, chargeingNow) }}
          p {{ myBatteryPersent !== undefined ? myBatteryPersent.toFixed(0) + '%' : '取得できませんでした' }}
        .info
          v-icon mdi-map-marker-account
          p {{ myLocation }}
  //-- オプションダイアログ --
  v-dialog(
    v-model="optionsDialog"
    transition="dialog-bottom-transition"
    fullscreen
  )
    v-card
      .top-android-15-or-higher(v-if="isAndroid15OrHigher")
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
            img(src="/account_default.jpg" style="height: 8em; width: 8em; border-radius: 9999px;")
          .account-info(
            style="text-align: center;"
          )
            p(style="font-size: 1.2em; margin: 0; padding: 0;") {{ myUserId ? myUserId : 'ログインしていません' }}
            p(style="margin: 0; padding: 0;") {{ myUserId ? `@${myUserId}` : 'データは同期されていません' }}
            v-btn.my-2(
              v-if="myUserId"
              text
              append-icon="mdi-account-edit"
            ) アカウント情報を編集
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
          v-list-item.item( @click="$router.push('/settings')" )
            .icon-and-text
              v-icon mdi-cog
              v-list-item-title 設定
          v-list-item.item( @click="$router.push('/terms')" )
            .icon-and-text
              v-icon mdi-file-document-outline
              v-list-item-title 利用規約
          v-list-item.item( @click="$router.push('/about')" )
            .icon-and-text
              v-icon mdi-information
              v-list-item-title このアプリについて
  //-- 位置情報利用許可ダイアログ --
  v-dialog(
    v-model="requestGeoPermissionDialog"
    persistent
    max-width="400"
  )
    v-card
      v-card-title(class="headline") 位置情報の利用許可
      v-card-text
        | このアプリは位置情報を利用します。 位置情報の利用を許可してください。
        br
        | また、バックグラウンドでの位置情報取得と通知の許可も必要です。
      v-card-actions
        v-spacer
        v-btn(
          text
          @click="requestGeoPermissionDialog = false"
          prepend-icon="mdi-close"
          ) 嫌だ！
        v-btn(
          style="background-color: rgb(var(--v-theme-primary)); color: white"
          text
          @click="requestGeoPermission"
          prepend-icon="mdi-check"
          ) ええで！
  //-- タイムラインモード --
  v-dialog(
    v-model="timelineMode"
    fullscreen
    transition="dialog-bottom-transition"
  )
    v-card
      .top-android-15-or-higher(v-if="isAndroid15OrHigher")
      v-card-actions
        p.ml-2(class="headline" style="font-size: 1.3em") タイムライン
        v-spacer
        v-btn(
          text
          @click="timelineMode = false"
          icon="mdi-close"
          )
      v-card-text
        p ここにタイムラインコンテンツを表示します。
</template>

<script lang="ts">
  import { App } from '@capacitor/app'
  import { BackgroundRunner } from '@capacitor/background-runner'
  import { Capacitor } from '@capacitor/core'
  import { Device } from '@capacitor/device'
  import { Geolocation } from '@capacitor/geolocation'
  import { LIcon, LMap, LMarker, LTileLayer } from '@vue-leaflet/vue-leaflet'
  import 'leaflet/dist/leaflet.css'

  export default {
    components: {
      LMap,
      LMarker,
      LTileLayer,
      LIcon,
    },
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
        detailCardTarget: null as string | null,
        /** オプションダイアログの表示フラグ */
        optionsDialog: false,
        /** Android 15以上かどうか */
        isAndroid15OrHigher: true,
        /** タイムラインモードかどうか */
        timelineMode: false,
        /** 自分のユーザーID */
        myUserId: null as string | null,
      }
    },
    async mounted () {
      console.log(import.meta.env)
      /** ログイン情報確認 */
      if (localStorage.getItem('userId')) {
        this.myUserId = localStorage.getItem('userId')
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
        const permission = await BackgroundRunner.checkPermissions()
        if (permission.geolocation !== 'granted') {
          this.requestGeoPermissionDialog = true
        }
      }

      /** バックボタンのリスナーを追加 */
      App.addListener('backButton', () => {
        if (this.timelineMode) {
          /** タイムラインモードを閉じる */
          this.timelineMode = false
        } else if (this.optionsDialog) {
          /** オプションダイアログを閉じる */
          this.optionsDialog = false
        } else if (this.requestGeoPermissionDialog) {
          /** 位置情報利用許可ダイアログを閉じる */
          this.requestGeoPermissionDialog = false
        } else if (this.detailCardTarget) {
          /** 詳細カードを閉じる */
          this.detailCardTarget = null
        } else if (this.$route.path === '/') {
          /** ルートページならアプリを終了 */
          App.exitApp()
        } else {
          /** ルート以外のページなら1つ戻る */
          this.$router.back()
        }
      })

      /** 現在地を監視 */
      Geolocation.watchPosition({
        enableHighAccuracy: true,
        timeout: 10_000,
        interval: 5000,
      }, position =>
        this.watchPosition(position),
      )

      /** 現在地を取得し、地図の中心も移動 */
      await this.setCurrentPosition()
    },
    methods: {
      /** 位置情報監視のコールバック */
      watchPosition (position: any) {
        if (position) {
          const lat: number = position.coords.latitude
          const lng: number = position.coords.longitude
          this.lastGetLocation = [lat, lng]
          this.myLocation = [lat, lng]
          localStorage.setItem('latlng', JSON.stringify(this.myLocation))
        }

        /** バッテリー情報を取得 */
        Device.getBatteryInfo().then(info => {
          if (info.batteryLevel) {
            this.myBatteryPersent = info.batteryLevel * 100
          }
          this.chargeingNow = info.isCharging
        })
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
      },
      /** 2地点間の距離を計算 */
      calcDistance (latlng1: [number, number], latlng2: [number, number]) {
        const R = Math.PI / 180
        const lat1 = latlng1[0] * R
        const lat2 = latlng2[0] * R
        const lng1 = latlng1[1] * R
        const lng2 = latlng2[1] * R
        return 6371e3 * Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lng2 - lng1))
      },
      /** バッテリーアイコンを選択 */
      chooseBatteryIcon (batteryPersent: number | undefined, chargingNow: boolean | undefined) {
        if (batteryPersent === undefined || chargingNow === undefined) {
          return 'mdi-battery-unknown'
        }
        let returnText = 'mdi-battery-'
        if (chargingNow) {
          returnText += 'charging-'
        }
        if (batteryPersent >= 95) {
          return returnText + '100'
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
</style>
