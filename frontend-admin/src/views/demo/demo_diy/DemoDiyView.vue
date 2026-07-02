<template>
  <div>我是demo自定义页面 访问地址是
    {{baseUrl}}#/demoDiyView
  </div>
</template>

<script>
export default {
  name: "DemoDiyView"
}
</script>
<script setup>
import {ref} from "vue";

const baseUrl = ref(window.document.location.protocol + "//" + window.document.location.host + "/")
</script>
<style scoped>

</style>
