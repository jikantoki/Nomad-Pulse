<template lang="pug">
v-card(
  style="width: 100%; height: 100%;"
  :class="settings.hidden.isAndroid15OrHigher ? 'top-android-15-or-higher' : ''"
  )
  v-card-actions
    p.ml-2(style="font-size: 1.3em") 位置情報とプライバシー
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
        @click="togglePause"
        )
        .icon
          v-icon mdi-map-marker-off
        .text
          p.title 位置情報の一時停止
          p.description(v-if="!settings.location.pause") 位置情報を共有しています
          p.description(v-else) 位置情報を共有していません
        v-spacer
        v-switch(
          v-model="settings.location.pause"
          color="primary"
          hide-details
          @click.stop
          )
      .setting-item(
        v-ripple
        @click="openTimeDialog"
        )
        .icon
          v-icon mdi-timer-marker-outline
        .text
          p.title 位置情報の共有時間
          p.description(v-if="!settings.location.shareTime.enabled") 常に共有
          p.description(v-else) {{ formatTime(settings.location.shareTime.start) }} ～ {{ formatTime(settings.location.shareTime.end) }}
        v-spacer
        v-switch(
          v-model="settings.location.shareTime.enabled"
          color="primary"
          hide-details
          @click.stop
          )
      .setting-item(
        v-ripple
        @click="openLocationDialog"
        )
        .icon
          v-icon mdi-map-marker-radius
        .text
          p.title 位置情報の共有場所
          p.description(v-if="!settings.location.shareLocation.enabled") 場所制限なし
          p.description(v-else) 指定地点から半径 {{ settings.location.shareLocation.distance / 1000 }}km以内
        v-spacer
        v-switch(
          v-model="settings.location.shareLocation.enabled"
          color="primary"
          hide-details
          @click.stop
          )

  //- 共有時間設定ダイアログ
  v-dialog(
    v-model="timeDialog"
    max-width="400px"
    )
    v-card
      v-card-title 共有時間の設定
      v-card-text
        v-row
          v-col(cols="12")
            p.text-subtitle-2 開始時刻
            v-row
              v-col(cols="6")
                v-text-field(
                  v-model.number="tempTime.start.hour"
                  label="時"
                  type="number"
                  min="0"
                  max="23"
                  density="compact"
                  hide-details
                  )
              v-col(cols="6")
                v-text-field(
                  v-model.number="tempTime.start.min"
                  label="分"
                  type="number"
                  min="0"
                  max="59"
                  density="compact"
                  hide-details
                  )
          v-col(cols="12")
            p.text-subtitle-2 終了時刻
            v-row
              v-col(cols="6")
                v-text-field(
                  v-model.number="tempTime.end.hour"
                  label="時"
                  type="number"
                  min="0"
                  max="23"
                  density="compact"
                  hide-details
                  )
              v-col(cols="6")
                v-text-field(
                  v-model.number="tempTime.end.min"
                  label="分"
                  type="number"
                  min="0"
                  max="59"
                  density="compact"
                  hide-details
                  )
      v-card-actions
        v-spacer
        v-btn(
          text
          @click="timeDialog = false"
          ) キャンセル
        v-btn(
          text
          color="primary"
          @click="saveTimeSettings"
          ) 保存

  //- 共有場所設定ダイアログ
  v-dialog(
    v-model="locationDialog"
    max-width="500px"
    )
    v-card
      v-card-title 共有場所の設定
      v-card-text
        v-row
          v-col(cols="12")
            p.text-subtitle-2 中心地点の緯度
            v-text-field(
              v-model.number="tempLocation.centerLatlng[0]"
              label="緯度"
              type="number"
              step="0.000001"
              density="compact"
              hide-details
              )
          v-col(cols="12")
            p.text-subtitle-2 中心地点の経度
            v-text-field(
              v-model.number="tempLocation.centerLatlng[1]"
              label="経度"
              type="number"
              step="0.000001"
              density="compact"
              hide-details
              )
          v-col(cols="12")
            p.text-subtitle-2 共有範囲（半径）
            v-text-field(
              v-model.number="tempLocation.distance"
              label="距離（m）"
              type="number"
              min="0"
              step="100"
              density="compact"
              hide-details
              suffix="m"
              )
          v-col(cols="12")
            v-btn(
              block
              color="primary"
              @click="setCurrentLocation"
              prepend-icon="mdi-crosshairs-gps"
              ) 現在地を設定
      v-card-actions
        v-spacer
        v-btn(
          text
          @click="locationDialog = false"
          ) キャンセル
        v-btn(
          text
          color="primary"
          @click="saveLocationSettings"
          ) 保存
</template>

<script lang="ts">
  import { Geolocation } from '@capacitor/geolocation'
  import { useMyProfileStore } from '@/stores/myProfile'
  import { useSettingsStore } from '@/stores/settings'

  export default {
    name: 'SettingsPage',
    data () {
      return {
        settings: useSettingsStore(),
        myProfile: useMyProfileStore(),
        timeDialog: false,
        locationDialog: false,
        tempTime: {
          start: { hour: 0, min: 0 },
          end: { hour: 0, min: 0 },
        },
        tempLocation: {
          centerLatlng: [0, 0],
          distance: 0,
        },
      }
    },
    async mounted () {},
    methods: {
      togglePause () {
        this.settings.location.pause = !this.settings.location.pause
      },
      openTimeDialog () {
        this.tempTime = {
          start: { ...this.settings.location.shareTime.start },
          end: { ...this.settings.location.shareTime.end },
        }
        this.timeDialog = true
      },
      openLocationDialog () {
        this.tempLocation = {
          centerLatlng: [...this.settings.location.shareLocation.centerLatlng],
          distance: this.settings.location.shareLocation.distance,
        }
        this.locationDialog = true
      },
      formatTime (time: { hour: number, min: number }) {
        const h = String(time.hour).padStart(2, '0')
        const m = String(time.min).padStart(2, '0')
        return `${h}:${m}`
      },
      saveTimeSettings () {
        this.settings.location.shareTime.start = { ...this.tempTime.start }
        this.settings.location.shareTime.end = { ...this.tempTime.end }
        this.timeDialog = false
      },
      saveLocationSettings () {
        this.settings.location.shareLocation.centerLatlng = [...this.tempLocation.centerLatlng]
        this.settings.location.shareLocation.distance = this.tempLocation.distance
        this.locationDialog = false
      },
      async setCurrentLocation () {
        try {
          const position = await Geolocation.getCurrentPosition()
          this.tempLocation.centerLatlng = [
            position.coords.latitude,
            position.coords.longitude,
          ]
        } catch (error) {
          console.error('位置情報の取得に失敗しました', error)
        }
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
