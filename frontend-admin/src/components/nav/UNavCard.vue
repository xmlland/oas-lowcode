<template>
  <router-link :to="to">
    <div class="nav-card">
      <div class="nav-card-title">
        {{ title }}
      </div>
      <div class="icon-img">
        <img :src="'imgs/'+image+'-icon.png'">
      </div>
      <div class="bg-img">
        <img :src="'imgs/'+image+'-bg.png'">
      </div>
    </div>
  </router-link>

</template>

<script>
export default {
  name: "UNavCard"
}
</script>
<script setup>

defineProps({
  image: {
    type: String,
    default: ''
  },
  title: {
    type: String,
    default: ''
  },
  subTitle: {
    type: String,
    default: ''
  },
  to: {
    type: String,
    default: ''
  }
})
</script>
<style lang="less" scoped>
@import "@/assets/less/main.less";
.nav-card {
  width: 380px;
  height: 140px;
  background: #FFFFFF;
  box-shadow: 6px 6px 54px rgba(0, 0, 0, 0.05);
  border-radius: 14px;
  position: relative;
  overflow: hidden;
  cursor: pointer;

  .nav-card-title {
    margin: 32px 0 0 44px;
    padding-left: 16px;
    position: relative;
    font-family: @main-font-name;
    font-style: normal;
    font-weight: 700;
    font-size: 18px;
    line-height: 18px;
    /* identical to box height, or 100% */

    display: flex;
    align-items: center;
    letter-spacing: 0.3px;

    color: var(--ant-primary-color);
  }

  .nav-card-title::before {
    position: absolute;
    content: '';
    left: 0;
    top: 2px;
    width: 4px;
    height: 16px;
    background: var(--ant-primary-color);
    border-radius: 12px;
  }

  .icon-img {
    position: absolute;
    right: 24px;
    top: 24px;
  }

  .bg-img {
    position: absolute;
    left: -25px;
    bottom: -44px;
    opacity: 0.1;
  }
}
</style>
