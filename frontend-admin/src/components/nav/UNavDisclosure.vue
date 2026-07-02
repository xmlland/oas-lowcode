<template>
  <div class="nav-card">
<!--    <div class="icon-img">
      <img :src="'imgs/'+image+'-icon.png'" style="width: 50px;height: 50px;">
    </div>
    <div class="bg-img">
      <img :src="'imgs/'+image+'-bg.png'">
    </div>-->
    <div class="ndpl-data">
      <div class="nav-card-title">
        {{ title }}
      </div>
      <span class="ndpl-span-data" v-html="resultContent"></span>
      <div class="ndpl-button">
        <a-button @click="clickButton(buttonName)" class="ant-btn-primary">{{ buttonName }}</a-button>
        <a-button v-if="buttonName === '重新上传'" @click="clickButton('文件下载')" class="ant-btn-primary wjxz-button">文件下载</a-button>
        <div class="ndpl-num">
          <span class="ndpl-span" v-html="resultNum"></span>
        </div>
      </div>

    </div>

  </div>
</template>

<script>
export default {
  name: "UNavDisclosure"
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
  },
  buttonName: {
    type: String,
    default: ''
  },
  resultContent: {
    type: String,
    default: ''
  },
  resultNum: {
    type: String,
    default: ''
  }
})
let emits = defineEmits(['clickButton'])
const clickButton = (value) => {
  emits('clickButton',value)
}
</script>
<style lang="less" scoped>
@import "@/assets/less/main.less";
.nav-card {
  background: #FFFFFF;
  box-shadow: 6px 6px 54px rgba(0, 0, 0, 0.05);
  border-radius: 14px;
  position: relative;
  overflow: hidden;
  width: calc((100% - 40px) / 3);

  .nav-card-title {
    width: 100%;
    padding-left: 16px;
    position: relative;
    font-family: @main-font-name;
    font-style: normal;
    font-weight: 700;
    font-size: 16px;
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
    right: 10px;
    top: 45px;
    pointer-events: none;
  }

  .bg-img {
    position: absolute;
    left: -25px;
    bottom: -44px;
    opacity: 0.1;
    pointer-events: none;
  }

  .ndpl-data {
    padding: 20px 20px 40px 20px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    align-items: center;
    flex-wrap: nowrap;
    height: 100%;

    .ndpl-span-data {
      text-align: left;
      flex: 1;
      padding: 30px 10px;
      min-height: 250px;
    }

    .ndpl-button {

      .ant-btn-primary {
        color: #fff;
        width: 88px;
        border-color: var(--ant-primary-color);
        background: var(--ant-primary-color);
        text-shadow: 0 -1px 0 rgba(0, 0, 0, 0.12);
        box-shadow: 0 2px 0 rgba(0, 0, 0, 0.045);
      }

      .ndpl-num {
        margin-top: 20px;
        .ndpl-span {
          display: block;
          text-align: center;
          font-weight: bold;
          font-size: 16px;
        }
      }
    }

  }

  .wjxz-button{
    border-color: #fa8c16 !important;
    background: #fa8c16 !important;;
    margin-left: 20px;
  }
}
</style>
