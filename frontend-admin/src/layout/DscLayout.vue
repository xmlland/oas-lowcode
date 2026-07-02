<template>
  <div id="app-container">
    <a-layout :class="helpVisible?'help-container-show':''" class="my-layout" id="modal-container">
      <a-layout-header v-if="headerVisible">
        <layout-header @loadSuccess="loadSuccess"></layout-header>
      </a-layout-header>
      <a-layout-content>
        <layout-main v-if="success"></layout-main>
      </a-layout-content>
      <a-layout-footer v-if="footerVisible">
        <layout-footer></layout-footer>
      </a-layout-footer>
    </a-layout>
    <div class="help-container" :class="helpVisible?'active':''">
      <help-content v-if="helpVisible"/>
    </div>
  </div>
</template>

<script>
import LayoutHeader from "@/layout/LayoutHeader";
import LayoutFooter from "@/layout/LayoutFooter";
import LayoutMain from "@/layout/LayoutMain";

export default {
  name: "DscLayout",
  components: {LayoutMain, LayoutFooter, LayoutHeader},
}
</script>
<script setup>
import {computed, ref, watch} from "vue";
import {useStore} from "vuex";
import HelpContent from "@/components/help/HelpContent";

const store = useStore();
let headerVisible = computed(() => store.getters.getHeaderVisible)
let footerVisible = computed(() => store.getters.getFooterVisible)
let helpVisible = computed(() => store.getters.getHelpVisible)

let footer = {
  copyright: '北京中联普瑞科技有限公司',
  orgAddress: '北京市',
  orgPostal: '102200',
  icpCode: 'xxxxxxxx'
}
store.commit('setFooter', footer)

let success = ref(false);
watch(() => headerVisible, () => {
  if (!headerVisible.value) {
    success.value = true
  }
}, {immediate: true})

const loadSuccess = () => {
  success.value = true
}
</script>
<style lang="less" scoped>
.my-layout.ant-layout {
  background: #ebebeb;
  height: 100%;
  width: 100%;

  .ant-layout-header {
    height: 140px;
    padding: 0;
    color: #fff;
    background: #00675b;
    display: none;
  }

  .ant-layout-content {
    height: cacl(100% - 140px -70px);
    background: #ffffff;
  }

  .ant-layout-footer {
    height: 70px;
    /* line-height: 90px; */
    padding: 0;
    background: #F5F8FA;
    display: flex;
    flex-direction: column;
    justify-content: center;
  }
}
.my-layout.ant-layout.help-container-show{
  width: 70%;
}
#app-container {
  display: flex;
}

</style>
