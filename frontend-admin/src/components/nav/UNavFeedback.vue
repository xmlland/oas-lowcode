<template>
  <div class="nav-card">

    <div class="ndpl-data">
      <div class="nav-card-title">
        {{ title }}
      </div>
      <span class="ndpl-span-data">您在使用、填报过程中遇到任何问题都可以咨询，还可以发送建议来帮助我们改进您的使用体验，发送建议或报告问题非常简单，您可以点击【我要在线反馈】在线填写问题描述并附上一些屏幕截图，也可以通过电话热线或QQ群联系我们，帮助我们了解您所遇到的问题。</span>
      <div class="ndpl-button">
        <a-button @click="clickButton" class="ant-btn-primary">我要在线反馈</a-button>
      </div>
      <div class="ndpl-num">
        <span class="ndpl-span">技术服务QQ群：10630000XX<br>技术服务热线：010-82XXX77<br>技术支持单位：北京中联普瑞科技有限公司</span>
      </div>
    </div>

  </div>
</template>

<script>
export default {
  name: "UNavFeedback"
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
const clickButton = () => {
  emits('clickButton')
}
</script>
<style lang="less" scoped>
@import "@/assets/less/main.less";
.nav-card {
  width: 710px;
  background: #FFFFFF;
  box-shadow: 6px 6px 54px rgba(0, 0, 0, 0.05);
  border-radius: 14px;
  position: relative;
  overflow: hidden;

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
      padding: 60px 10px;
    }

    .ndpl-button {

      .ant-btn-primary {
        color: #fff;
        width: 128px;
        border-color: var(--ant-primary-color);
        background: var(--ant-primary-color);
        text-shadow: 0 -1px 0 rgba(0, 0, 0, 0.12);
        box-shadow: 0 2px 0 rgba(0, 0, 0, 0.045);
      }

    }

    .ndpl-num {

      width: 100%;
      margin-top: 10px;
      .ndpl-span {
        text-align: left;
        font-size: 12px;
      }
    }
  }

}
</style>
