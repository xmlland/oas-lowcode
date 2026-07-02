<template>
  <div v-if="!feedbackVisible && sysFeedbackAddFlag" class="feedback-btn-container" :style="buttonConfig.style">
    <div class="feedback-btn-content" @click="show">
      <div class="icon">
        <img src="/imgs/help/help.apng" alt="feedback">
      </div>
      <a-badge :count="getFeedbackNews" :overflow-count="9">
        <div class="text">
          问题反馈
        </div>
      </a-badge>
    </div>
  </div>
</template>

<script>
export default {
  name: "FeedbackButton"
}
</script>
<script setup>
import {useStore} from "vuex";
import {computed, onMounted, onUnmounted, ref, watch} from "vue";
import config from "@/config";
import {hasAnyPermission} from "@/lib/tools";
import {getAction} from "@/api/action";

let buttonConfig = ref({
  style: {
    right: '20px',
    bottom: '180px',
  }
})
let sysFeedbackAddFlag = computed(() => hasAnyPermission(['app:sys_feedback:add']));
let intervalId = ref(null);
if (config && config.feedback && config.feedback.button) {
  buttonConfig.value = config.feedback.button
}
let feedbackVisible = computed(() => store.getters.getFeedbackVisible)
let getFeedbackNews = computed(() => store.getters.getFeedbackNews)
let store = useStore();
const show = () => {
  store.commit('setFeedbackVisible', true)
}

const queryNeedRead = () => {
  getAction('admin/sysFeedback/queryNeedRead').then(res => {
    store.commit('setFeedbackNews', res.data.needRead)
  })
}

watch(() => [feedbackVisible.value, sysFeedbackAddFlag.value], (value) => {
  // 判断是否显示问题反馈
  let flag = !feedbackVisible.value && sysFeedbackAddFlag.value;
  // 如果不显示问题反馈，并且存在定时任务，那么去掉定时任务
  if (!flag && intervalId.value) {
    clearInterval(intervalId.value);
    intervalId.value = null;
  } else if (flag) {
    // 如果显示问题反馈，判断定时任务是否存在，如果不存在则创建一个
    if (!intervalId.value) {
      // 每5分钟查询一次
      intervalId.value = setInterval(() => {
        queryNeedRead()
      }, 5 * 60 * 1000)
    }
  }
}, {deep: true, immediate: true});

onMounted(() => {
  // 初始化加载组件时，判断是否显示问题反馈，如果显示，默认查询首次
  if (!feedbackVisible.value && sysFeedbackAddFlag.value) {
    queryNeedRead()
  }
});

onUnmounted(() => {
  // 组件销毁时清除定时器
  if (intervalId.value) {
    clearInterval(intervalId.value);
    intervalId.value = null;
  }
});

</script>
<style lang="less" scoped>
.feedback-btn-container {
  position: fixed;
  width: 36px;
  height: 112px;
  background: #ffffff;
  z-index: 90000;
  border-radius: 20px;
  font-weight: 500;
  color: #181818;
  text-decoration: none;
  box-shadow: 0 2px 15px #0000001a;

  cursor: pointer;

  .feedback-btn-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 5px 0;

    .icon {
      width: 26px;
      height: 26px;
      border-radius: 50%;
      background: url(@/assets/img/help/bg.png) center center / 100% no-repeat;

      img {
        width: 26px;
        height: 26px;
      }

    }

    .text {
      width: 26px;
      text-align: center;
      line-height: 1.3;
    }
  }

}
</style>
