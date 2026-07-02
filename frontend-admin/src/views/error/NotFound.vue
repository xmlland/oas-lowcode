<template>
  <div class="error-container">
    <img class="ico" src="~@/assets/svg/error/404-ico-svg.svg">
    <div style=" margin: 20px;">
      非常抱歉，您访问的地址错误...
    </div>
    <div style=" margin: 20px;">
      <a-button type="primary" @click="router.push({path: homePath})">返回首页</a-button>
    </div>
  </div>
</template>

<script>
export default {
  name: "NotFound"
}
</script>
<script setup>
import {useRouter} from "vue-router";
import {homePath} from "@/router";

const router = useRouter();
</script>
<style lang="less" scoped>
.error-container {
  width: 100vw;
  height: 100vh;
  background: url("@/assets/svg/error/404-bg-svg.svg") no-repeat;
  text-align: center;

  .ico {
    margin-top: 120px;

  }
}
</style>
