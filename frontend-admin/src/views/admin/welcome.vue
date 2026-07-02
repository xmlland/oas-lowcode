<template>
  <div class="welcome">
    <a-spin :spinning="true" size="large"/>
  </div>
</template>

<script>
export default {
  name: "adminWelcome"
}
</script>
<script setup>

</script>

<style scoped>
.welcome{
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
