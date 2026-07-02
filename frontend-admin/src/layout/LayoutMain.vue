<template>
  <div class="layout-main">
    <router-view/>
  </div>
</template>

<script>
export default {
  name: "LayoutMain"
}
</script>

<style scoped>
.layout-main {
  width: 100%;
  height: 100%;
  overflow: auto;
  background: url('../assets/img/background.svg');
  background-repeat: no-repeat;
  background-position: center 110px;
  background-size: 100%;
}
</style>
