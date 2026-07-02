<template>
  <div>
    <single-table-view ref="tableView" v-show="!customDisplay" :autoHeight="true" v-bind="singleTableViewRef" @requestResult="requestResultChange"/>
    <template v-if="customDisplay&&$slots.render">
      <slot name="render" :rows="tableData.rows"></slot>
    </template>
    <template v-else>
      <div class="task-list-container">
        <div
            v-bind:key="row.map.ENTITY_ID"
            class="task-item-row"
            v-for="(row) in tableData.rows"
            :class="{ 'priority-high': row.map.PRIORITY === 'high', 'completed': props.done }"
        >
          <div class="task-item-content">
            <div class="task-item-header">
              <span class="user">{{ row.map.PROC_CREATE_USER }}</span>
              <span class="time">{{ formatTime(row.map.PROC_CREATE_TIME) }}</span>
              <span class="category" :class="`category-${props.done}`">{{ row.map.PROC_CATEGORY }}</span>
            </div>
            <div class="task-item-title">
              {{ row.map.TASK_NAME }}
            </div>
            <div class="task-item-footer">
              <div class="table-row-btn" @click="more()" style="padding: 0 6px 0 0">
                {{ props.done ? '详情' : '办理' }}
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="task-item-row more" v-if="tableData.total>tableData.rows.length">
        <div class="task-item-icon">
        </div>
        <div class="task-item-btn">
<!--          <a-button type="link" size="small" @click="more()">{{ props.done ? '更多已办' : '更多待办' }}</a-button>-->
          <div class="table-row-btn" @click="more()">
            {{ props.done ? '更多已办' : '更多待办' }}
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script>
import SingleTableView from "@/components/view/SingleTableView";

export default {
  name: "ActTaskList",
  components: {
    SingleTableView
  }
}
</script>
<script setup>
/*eslint-disable*/

import {ref, computed, getCurrentInstance, inject} from "vue";

let instance = getCurrentInstance();

let props = defineProps({
  count: {
    type: [String, Number],
    default: 0
  },
  done: {
    type: Boolean,
    default: false
  },
  customDisplay: {
    type: Boolean,
    default: false
  }
})

let singleTableViewRef = computed(() => {
  return {
    commonActTaskList: true,
    formNo: 'oa_urge_setting',
    url: {
      list: `dynamic/zform/getTaskList?path=${props.done ? 'done' : 'todoanddoing'}`,
    },
    buttons: [],
    queryAreaShow: false,
    singleTable: {
      idField: ['map', 'ENTITY_ID'],
      pagination: {
        current: 1,
        defaultPageSize: 3,
        pageSize: 3,
        showQuickJumper: true,
        showSizeChanger: true,
        showTotal: (total) => {
          return `共 ${total} 条`
        },
        total: 0
      },
      rowButtons: props.done ? [{
        value: 'viewAct',
        text: '详情',
      }] : [{
        value: 'editAct',
        text: '办理',
      }],
      columns: [
        {title: '流程类型', dataIndex: ['map', 'PROC_CATEGORY'], dict: 'act_category', minWidth: 120, align: 'left'},
        {title: '发起人', dataIndex: ['map', 'PROC_CREATE_USER'], minWidth: 120, align: 'left'},
        {title: '发起时间', dataIndex: ['map', 'PROC_CREATE_TIME'], minWidth: 120, align: 'center'},
        {title: '当前节点', dataIndex: ['map', 'TASK_NAME'], minWidth: 120, align: 'left'},
        {title: '接收时间', dataIndex: ['map', 'TASK_CREATE_TIME'], minWidth: 120, align: 'center'},
      ],
      initParam: {
        "parent": {"id": ""},
      }
    }
  }
})
let tableData = ref({
  total: 0,
  rows: []
})
let emits = defineEmits(['update:count'])
const requestResultChange = (res) => {
  tableData.value = res
  emits('update:count', res.total)
}

const handle = (value, row, index) => {
  instance.refs.tableView.clickRow({
    value, row, index
  })
}
const addTabToAdminContent = inject('addTabToAdminContent'); // inject的参数为provide过来的名称
const more = () => {

  if (addTabToAdminContent){
    addTabToAdminContent({
      attributes:{
        pageName: props.done ? '更多已办' : '待办事项'
      },
      key: `${props.done ? '/oa/oa_done/list' : '/oa/oa_todoanddoing/list' }`,
    })
  }
}

const formatTime = (time) => {
  if (!time) {
    return ''
  }
  let date = new Date(time)
  return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()} ${date.getHours()}:${date.getMinutes()}`
}
</script>
<style lang="less" scoped>
@import "@/assets/less/main.less";

.table-row-btn {
  font-family: @main-font-name;
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 14px;

  text-align: center;

  color: var(--ant-primary-color);
  padding: 0 10px;
  cursor: pointer;
}

.task-list-container {
  width: 100%;
}

.task-item-row {
  display: flex;
  align-items: flex-start;
  padding: 8px;
  margin-bottom: 12px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
  background: white;
  transition: all 0.3s ease;
  border-left: 3px solid transparent;
  position: relative;
}

.task-item-row:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-1px);
  border-left-color: var(--ant-primary-color);
  background: var(--ant-primary-1);
}

.task-item-icon {
  flex-shrink: 0;
  margin-right: 12px;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(var(--ant-primary-color-rgb), 0.1);
  border-radius: 8px;
  color: var(--ant-primary-color);
  font-size: 16px;
}

.task-item-content {
  flex: 1;
  min-width: 0;
}

.task-item-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  flex-wrap: wrap;
  gap: 8px;
  position: relative;
  font-size: 14px;
}

.task-item-header .category {
  font-weight: 500;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  position: absolute;
  right: 0;
  top: 0;
}

.task-item-header .category-false {
  color: #ff7d00;
  background: rgba(255, 125, 0, 0.1);
}

.task-item-header .category-true {
  color: #00b42a;
  background: rgba(0, 180, 42, 0.1);
}

.task-item-header .user {
  color: #666;
}

.task-item-header .time {
  color: #999;
}

.task-item-title {
  font-weight: 500;
  font-size: 14px;
  //margin-bottom: 12px;
  color: #333;
  line-height: 1.4;
  word-break: break-word;
  padding-right: 80px; /* 为右上角标签留出空间 */
}

.task-item-footer {
  display: flex;
  justify-content: end;
  align-items: center;
  margin-top: 4px;
}

.task-item-time {
  color: #999;
  font-size: 13px;
}

.task-item-btn {
  flex-shrink: 0;
}

/* 状态变体 */
.task-item-row.priority-high {
  border-left-color: #ff4d4f;
}

.task-item-row.priority-high .task-item-icon {
  color: #ff4d4f;
  background: rgba(255, 77, 79, 0.1);
}

.task-item-row.priority-high .task-item-header .category {
  color: #ff4d4f;
  background: rgba(255, 77, 79, 0.1);
}

.task-item-row.completed {
  opacity: 0.8;
  background: #f9f9f9;
}

.task-item-row.completed:hover {
  background: #f0f0f0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .task-item-row {
    padding: 12px;
    flex-direction: column;
  }

  .task-item-icon {
    margin-right: 0;
    margin-bottom: 10px;
    align-self: center;
  }

  .task-item-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
    margin-bottom: 8px;
  }

  .task-item-header .category {
    position: static;
    margin-top: 4px;
    align-self: flex-end;
  }

  .task-item-title {
    padding-right: 0;
    margin-bottom: 10px;
  }

  .task-item-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .task-item-btn {
    align-self: flex-end;
  }
}
</style>
