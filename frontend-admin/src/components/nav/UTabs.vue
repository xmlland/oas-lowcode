<template>
  <a-tabs
      class="u-tabs"
      v-model:activeKey="activeKey"
      @change="change"
  >
    <slot/>
  </a-tabs>
</template>

<script>
export default {
  name: "UTabs"
}
</script>
<script setup>
import {ref} from "vue";

const activeKey = ref(0)

let emits = defineEmits(['change'])

const change = (activeKey) => {
  emits('change', activeKey)
}
</script>
<style lang="less" scoped>
@import "@/assets/less/main.less";

.u-tabs {
  height: 100%;

  :deep(.ant-tabs-nav::before) {
    border: 0;
  }

  :deep( .ant-tabs-content) {
    height: 100%;
  }

  :deep( .ant-tabs-nav-list) {
    background: #FAFBFD;
    border: 0.6px solid #D5D5D5;
    border-radius: 12px;

    .ant-tabs-tab {
      padding: 6px 16px;
      font-family: @main-font-name;
      font-style: normal;
      font-weight: 700;
      font-size: @font-size-base;
      line-height: @font-size-base;
      margin: 0;
      border-bottom: 0;
    }

    .ant-tabs-tab:first-child.ant-tabs-tab-active {
      background: var(--ant-primary-color);
      border-radius: 12px 0 0 12px;

      .ant-tabs-tab-btn {
        color: #fff;
      }
    }

    .ant-tabs-tab.ant-tabs-tab-active {
      background: var(--ant-primary-color);
      border-radius: 0;

      .ant-tabs-tab-btn {
        color: #fff;
      }
    }

    .ant-tabs-tab:nth-last-child(2).ant-tabs-tab-active {
      background: var(--ant-primary-color);
      border-radius: 0 12px 12px 0;

      .ant-tabs-tab-btn {
        color: #fff;
      }
    }

    .ant-tabs-ink-bar {
      height: 0;
    }
  }
}
</style>
