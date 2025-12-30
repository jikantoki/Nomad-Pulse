<template lang="pug">
div(style="height: 100%; width: 100%")
  LMap(
    :zoom="leaflet.zoom"
    :center="leaflet.center"
    style="height: 100%; width: 100%"
    :useGlobalLeaflet="false"
    @update:zoom="leaflet.zoom = $event"
    @update:center="leaflet.center = $event"
    )
    LTileLayer(
      url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      attribution='&copy; <a href="https://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> contributors'
    )
    LMarker(:lat-lng="myLocation")
      LIcon(
        :icon-size="[0,0]"
        style="border: none;"
        :icon-anchor="[16, 16]"
        )
        div(style="display: flex; align-items: center; width: 1000em;")
          img(src="/account_default.jpg" style="height: 32px; width: 32px; border-radius: 9999px;")
          p.ml-2.name-space(:style="leaflet.zoom >= 15 ? 'opacity: 1;' : 'opacity: 0;'")
            | あなたの現在地
  .right-bottom-buttons
    .current-button
      v-btn(
        size="x-large"
        icon
        @click="setCurrentPosition"
        style="background-color: rgb(var(--v-theme-primary)); color: white"
        )
        v-icon mdi-crosshairs-gps
  v-dialog(
    v-model="requestGeoPermissionDialog"
    persistent
    max-width="400"
  )
    v-card
      v-card-title(class="headline") 位置情報の利用許可
      v-card-text
        | このアプリは位置情報を利用します。 位置情報の利用を許可してください。
      v-card-actions
        v-spacer
        v-btn(
          text
          @click="requestGeoPermissionDialog = false"
          ) 嫌だ！
        v-btn(
          style="background-color: rgb(var(--v-theme-primary)); color: white"
          text
          @click="requestGeoPermission"
          ) ええで！
</template>

<script lang="ts">
  import { Capacitor } from '@capacitor/core'
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
      }
    },
    mounted () {
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
    },
    methods: {
      /** 現在地を取得し、地図の中心も移動 */
      async setCurrentPosition () {
        /** 仮で最後に取得した位置情報を地図の中心に設定 */
        this.leaflet.center = this.lastGetLocation
        this.leaflet.zoom = 15

        const position = await Geolocation.getCurrentPosition()

        this.leaflet.center = [position.coords.latitude, position.coords.longitude]
        this.myLocation = [position.coords.latitude, position.coords.longitude]
        this.lastGetLocation = [position.coords.latitude, position.coords.longitude]
      },
      /** 位置情報の許可を求める */
      async requestGeoPermission () {
        if (Capacitor.getPlatform() === 'web') {
          await Geolocation.getCurrentPosition()
          this.requestGeoPermissionDialog = false
          return
        }
        const result = await Geolocation.requestPermissions()
        if (result.location === 'granted') {
          this.setCurrentPosition()
        }
        this.requestGeoPermissionDialog = false
      },
    },
  }
</script>

<style lang="scss" scoped>
.right-bottom-buttons {
  position: fixed;
  right: 16px;
  bottom: 16px;
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

.name-space {
  font-size: 16px;
  font-weight: 900;
  white-space: nowrap;
  -webkit-text-stroke: 1px black;
  color: white;
  transition: all 1s;
}
</style>
