<template>
  <div class="feedback-content">
    <div class="feedback-title" @click="closeFeedback">
      <a-button type="text">
        <close-outlined/>
      </a-button>
      <div class="text">
        问题反馈
      </div>
      <span style="width: 46px">&nbsp;</span>
    </div>
    <div class="feedback-data">
      <a-tabs v-show="!formShow" v-model:activeKey="activeKey">
        <template #rightExtra>
          <a-button type="ghost" @click="addFeedback">
            <div class="icon">
              <img src="/imgs/help/help.apng" alt="feedback">
            </div>
            提交新问题
          </a-button>
        </template>
        <a-tab-pane key="1" tab="常见问题">
          <div class="tab-content">
            <CommonQuestionList :funcObj="funcObj"/>
          </div>
        </a-tab-pane>
        <a-tab-pane key="2" tab="我提交的">
          <div class="tab-content">
            <MyFeedbackList :funcObj="funcObj"/>
          </div>
        </a-tab-pane>
      </a-tabs>
      <div v-if="formShow" class="tab-content">

        <div class="form-title">
          <span class="back-btn" @click="back">
            <left-outlined/>
          </span>
          <span class="title-text">{{ formTitle }}</span>

        </div>
        <component v-bind="currentComponentProps" :is="currentComponent" @close="close"/>
      </div>
    </div>

  </div>
</template>

<script>
export default {
  name: "FeedbackContent"
}
</script>
<script setup>
import {computed, ref} from "vue";
import {useStore} from "vuex";
import MyFeedbackList from "@/components/help/feedback/MyFeedbackList";
import CommonQuestionList from "@/components/help/feedback/CommonQuestionList";

let store = useStore();
let activeKey = ref('1')

let getFeedbackNews = computed(() => store.getters.getFeedbackNews)
if (getFeedbackNews.value > 0) {
  activeKey.value = '2'
}

const closeFeedback = () => {
  store.commit('setFeedbackVisible', false)
}
const addFeedback = () => {
  navTo('FeedBackAddForm', '提交新问题')
}

let currentComponent = ref(null)
let currentComponentProps = ref({})
let formTitle = ref('')
let formShow = ref(false)
const helpModules = import.meta.glob('./*.vue')
const navTo = (comp, title = '', props = {}) => {
  formTitle.value = title
  currentComponentProps.value = props
  const loader = helpModules[`./${comp}.vue`]
  if (loader) {
    loader().then(res => {
      currentComponent.value = res.default
      formShow.value = true
    })
  }
}
const back = () => {
  formShow.value = false
}
const setCurrentKey = (key) => {
  activeKey.value = key
}

let funcObj = {
  setCurrentKey, navTo
}

const close = (callback) => {
  formShow.value = false
  callback({setCurrentKey})
}
</script>
<style lang="less" scoped>

@import "@/assets/less/main.less";

.feedback-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  border-left: 1px solid #f0f0f0;

  .feedback-title {
    height: 42px;
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    border-bottom: 1px solid #f0f0f0;

    .text {
      font-family: @main-font-name;
      font-style: normal;
      font-weight: 700;
      font-size: 18px;
      line-height: 38px;
      letter-spacing: 0.3px;
      color: var(--ant-primary-color);
      text-align: center;
      flex: 1;
    }
  }

  .feedback-data {
    flex: 1;
    padding: 20px 10px;
    overflow: auto;

    .icon {
      display: inline-block;
      width: 22px;
      height: 22px;
      border-radius: 50%;
      background: url(@/assets/img/help/bg.png) center center / 100% no-repeat;

      img {
        width: 22px;
        height: 22px;
      }
    }

    .tab-content {
      height: calc(100vh - 136px);

      .form-title {
        display: flex;
        align-items: center;

        font-weight: 700;
        margin-bottom: 20px;
        background: var(--ant-primary-color);
        color: #fff;
        height: 36px;

        .back-btn {
          cursor: pointer;
          width: 32px;
          text-align: center;
        }

        .title-text {
          font-size: 16px;
          flex: 1;
          text-align: center;
          padding-right: 32px;
        }
      }
    }
  }
}
</style>
