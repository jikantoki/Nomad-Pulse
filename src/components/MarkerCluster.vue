<template lang="pug">
.marker-cluster(ref="markers")
  //- ここにLeafletのLmarkerが大量格納される
  slot
</template>

<script lang="ts">
  export default {
    mounted () {},
  }
</script>
