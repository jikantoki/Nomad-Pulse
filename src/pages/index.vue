<template lang="pug">
div(style="height: 100%; width: 100%")
  LMap(:zoom="leaflet.zoom" :center="leaflet.center" style="height: 100%; width: 100%" :useGlobalLeaflet="false")
    LTileLayer(
      url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    )
    LMarker(:lat-lng="leaflet.center")
</template>

<script lang="ts">
  import { LMap, LMarker, LTileLayer } from '@vue-leaflet/vue-leaflet'
  import 'leaflet/dist/leaflet.css'

  export default {
    components: {
      LMap,
      LMarker,
      LTileLayer,
    },
    data () {
      return {
        leaflet: {
          zoom: 13,
          center: [35.690_430_765_555_42, 139.700_211_526_229_54],
        },
      }
    },
  }
</script>
