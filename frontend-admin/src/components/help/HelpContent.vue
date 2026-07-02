<template>
  <div class="help-content">
    <div class="help-title" @click="closeHelp">
      <a-button type="text">
        <close-outlined/>
      </a-button>
      <div class="text">
        填报说明
      </div>
      <span style="width: 46px">&nbsp;</span>
    </div>
    <div class="help-data">
      <a-spin size="large" :spinning="loading">
        <div  v-html="helpContent"></div>
      </a-spin>
    </div>

  </div>
</template>

<script>
export default {
  name: "HelpContent"
}
</script>
<script setup>
import {useStore} from "vuex";
import {computed, ref, watch} from "vue";
import {getAction} from "@/api/action";

let store = useStore();
const closeHelp = () => {
  store.commit('setHelpVisible', false)
}
let loading = ref(true)
let helpContentId = computed(() => store.getters.getHelpContentId)
let helpContent = ref('')
watch(() => helpContentId.value, () => {
  if (helpContentId.value) {
    loading.value = true
    getAction('dynamic/zform/getZformMap', {
      formNo: 'dsc_ext_help',
      id: helpContentId.value
    }).then(res => {
      helpContent.value = res.data.data.help_content
    }).finally(() => {
      loading.value = false
    })
  }

}, {immediate: true})

</script>
<style lang="less" scoped>
@import "@/assets/less/main.less";
.help-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  border-left: 1px solid #f0f0f0;

  .help-title {
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

  .help-data {
    flex: 1;
    padding: 20px 10px;
    overflow: auto;
  }
}
</style>
