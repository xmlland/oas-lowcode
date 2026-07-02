<template>
  <div class="bottom-left">
    <a-card :bordered="false">
      <template #title>
        <FileTextOutlined
            style="color: var(--ant-primary-color)"/>
        <span class="lczx">流程中心</span>
      </template>
      <a-row :gutter="16">
        <a-col :span="12">
          <h3 class="custom-element">
            <ClockCircleOutlined style="color: rgb(249 115 22);margin-right: 13px;" />
            待办事项  {{waitAmount}}
          </h3>
          <act-task-list v-model:count="waitAmount" :customDisplay="true"/>
        </a-col>
        <a-col :span="12">
          <h3 class="custom-element">
            <CheckCircleOutlined style="color: rgb(34 197 94);margin-right: 13px;" />
            已办事项
          </h3>
          <act-task-list :done="true" :customDisplay="true"/>
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script>
/*eslint-disable*/
import {OrderedListOutlined, ClockCircleOutlined, CheckCircleOutlined, FileTextOutlined} from '@ant-design/icons-vue';

export default {
  name: "admin_home",
  components: {
    OrderedListOutlined, ClockCircleOutlined, CheckCircleOutlined, FileTextOutlined
  }
}
</script>
<script setup>
import {ref} from 'vue';
import ActTaskList from "@/components/act/ActTaskList";

const activeKey = ref('1');
const waitAmount = ref(0);

</script>
<style lang="less" scoped>
@import "@/assets/less/main.less";

.bottom-left {
  background-color: #fff;

  .lczx {
    font-size: @font-size-base;
    font-weight: 600;
    margin-left: 13px;
  }

  :deep(.ant-card-head) {
    border-bottom: 0;
  }

  :deep(.ant-card-head-title) {
    border-bottom: 1px solid @border-color-split;
  }

  :deep(.ant-card-body) {
    padding: 8px 24px 24px 24px;
  }

  .custom-element {
    font-weight: 500;
    font-size: 14px;
    color: rgb(55 65 81);
    margin-bottom: 0.5rem;
    display: flex;
    align-items: center;
  }

}
</style>
