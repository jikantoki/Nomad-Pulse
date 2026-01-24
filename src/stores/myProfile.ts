import { defineStore } from 'pinia'

export const useMyProfileStore = defineStore('myProfile', {
  state: () => ({
    coverImg: null as string | null,
    createdAt: null as number | null,
    icon: null as string | null,
    message: null as string | null,
    name: 'ゲスト' as string | null,
    status: null as string | null,
    // userIdはnullを許可していないため、空文字列などで初期化する
    userId: 'guest',
    userToken: undefined as string | null | undefined,
    lastGetLocationTime: null as Date | null,
    location: null as [
      lat: number,
      lng: number,
    ] | null,
    battery: undefined as {
      parsent: number
      chargingNow: boolean | undefined
    } | null | undefined,
    guest: true,
  }),
  actions: {
    reset () {
      this.coverImg = null
      this.createdAt = null
      this.icon = null
      this.message = null
      this.name = 'ゲスト'
      this.status = null
      this.userId = 'guest'
      this.userToken = null
      this.lastGetLocationTime = null
      this.location = null
      this.battery = null
      this.guest = true
    },
  },
  persist: true,
})
