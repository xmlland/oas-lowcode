<template>
  <div>
    <a-input-group class="user-select" compact @click="onSearch">
      <a-input
          v-model:value="currentValue"
          :placeholder="placeholder"
          :disabled="true"
      />
      <a-button v-if="allowClear && !disabled && currentValue" class="user-select-clear" @click.stop="clearSelection">
        <span>&times;</span>
      </a-button>
      <a-button v-if="!disabled">
        <template #icon>
          <SearchOutlined/>
        </template>
      </a-button>
    </a-input-group>
    <a-form-item style="width: 0;height: 0" v-if="componentType==='default'&&dataScope !== 'target'">
      <u-modal ref="modal" :width="parseInt(modalWidth)" :formDisabled="true" :customOK="true" @clickOk="clickOk">
        <div class="user-select-modal-content">
          <u-tree class="left-tree" :responseData="['rows']" :selectedKeys="selectedKeys" :url="treeDataUrl"
                  :postData="treePostData"
                  @select="onTreeSelect"
                  :transform="{ title: 'name', key: 'id',  parent: ['parent','id'] }"/>
          <u-card class="right-content" :title="title" :flex="false">

            <single-table-view ref="tableView" :url="{list:tableDataUrl}" :singleTable="singleTable" :buttons="[]"
                               :height="530"
                               @clickRow="clickRow"
                               :disabled="false">
              <template #queryFields>
                <QueryField width="100" name="login_name" type="input" :props="{placeholder:'登录名' }" v-show="!isHideLoginName"></QueryField>
                <QueryField width="100" name="name" type="input" :props="{placeholder:'姓名' }"></QueryField>
              </template>
            </single-table-view>
          </u-card>
        </div>

      </u-modal>
    </a-form-item>
    <a-form-item style="display: none" v-if="componentType==='table'||dataScope === 'target'">
      <u-modal-select ref="userModal" formNo="sys_user" modal-title="请选择用户" name-data-index="name"
                      :filter-data="userFilterData"
                      :searchKey="searchKey" :searchLabel="searchLabel"
                      :modalWidth="parseInt(modalWidth)"
                      :searchConfig="{
                                  'b.parent_ids':{
                                    type:'office-select',
                                    queryType:'like',
                                    formatValue:(val)=>{
                                      if(val){
                                        return val.id
                                      }
                                      return ''
                                    }
                                  }
                                }"
                      :columns="isHideLoginName?[
                           {title: '归属机构', dataIndex: ['parent','name'], align: 'left'},
                           {title: '姓名', dataIndex:'name',align: 'left'},
                          ]:[
                           {title: '归属机构', dataIndex: ['parent','name'], align: 'left',sortColumn:'b.name'},
                           {title: '登录名', dataIndex:'login_name',align: 'left'},
                           {title: '姓名', dataIndex:'name',align: 'left'},
                          ]"
                      :disabled="disabled" :value="selectValue" @change="change"/>

    </a-form-item>
  </div>
</template>

<script>
import UCard from "@/components/card/UCard";

export default {
  name: "UUserSelect",
  components: {UCard},
}
</script>
<script setup>

import {computed, getCurrentInstance, ref, watch} from "vue";
import {message} from 'ant-design-vue';
import {replaceDefaultValue} from "@/lib/tools";
import config from "@/config"
import UModalSelect from "@/components/form/UModalSelect";

let instance = getCurrentInstance();
let props = defineProps({
  value: {
    type: Object,
    default() {
      return {}
    }
  },
  disabled: {
    type: Boolean,
    default: false
  },
  placeholder: {
    type: String,
    default: ''
  },
  modalWidth: {
    type: [String, Number],
    default: 1000
  },
  defaultValue: {
    type: String,
    default: null
  },
  hideLoginName: {
    type: [String, Boolean],
    default: false
  },
  /**
   * 数据范围 all:全部 org:本部门及下属部门 target:指定部门
   */
  dataScope: {
    type: String,
    default: 'all'
  },
  /**
   * 指定部门id
   */
  targetOrgId: {
    type: [String, Object],
    default: ''
  },
  // 组件类型 default  默认组件  table  表格组件
  componentType: {
    type: String,
    default: config.component && config.component.userSelect && config.component.userSelect.componentType ? config.component.userSelect.componentType : 'default'
  },
  // 排除系统用户
  excludeSysUsers: {
    type: Boolean,
    default: true
  },
  allowClear: {
    type: Boolean,
    default: true
  }
})
let currentValue = ref(null)
let selectedKeys = ref([])
let title = ref('请先选择机构')
let singleTable = ref({
  rowButtons: [{
    value: 'check',
    text: '选择',
    disableShow: true
  }],
  columns: [
    {title: '登录名', dataIndex: 'login_name', align: 'left'},
    {title: '姓名', dataIndex: 'name', align: 'left'},
  ],
  initParam: {},
  defaultSelectedRowKeys: [],
  rowSelectionType: 'radio'
})
let isHideLoginName = computed(() => {
  if (typeof props.hideLoginName === 'string') {
    return props.hideLoginName === '1' || props.hideLoginName === 'true'
  }
  return props.hideLoginName
})
if(isHideLoginName.value){
  singleTable.value.columns = [
    {title: '姓名', dataIndex: 'name', align: 'left'},
  ]
}

let selectValue = ref({})
watch(() => props.value, () => {
  if (props.value && props.value.id) {
    selectValue.value = props.value
    currentValue.value = props.value.name
    singleTable.value.defaultSelectedRowKeys = [props.value.id]
    return
  }
  let defaultVal = replaceDefaultValue(props.defaultValue);
  if (defaultVal && defaultVal.id) {
    selectValue.value = defaultVal
    currentValue.value = defaultVal.name
    singleTable.value.defaultSelectedRowKeys = [defaultVal.id]
    emits('update:value', defaultVal)
    emits('change', defaultVal)
    return
  }
  selectValue.value = null
  currentValue.value = null
  singleTable.value.defaultSelectedRowKeys = []
  emits('update:value', null)
  emits('change', null)
}, {immediate: true})
let emits = defineEmits(['update:value', 'change'])

let treeDataUrl = ref(`dynamic/zform/gridselectDataMap`)
let filterData = computed(() => {
  let arr = []
  if (props.dataScope === 'org') {
    // 机构及下属机构
    arr = [{children: [{key: 'a.code', value: '{company}', type: 'likeRight'}, {or: true, key: 'a.code', value: '{company}', type: 'eq'}]}]
  } else if (props.dataScope === 'target') {
    arr = [{key: 'a.id', value: (props.targetOrgId && typeof props.targetOrgId === 'object') ? props.targetOrgId.id : props.targetOrgId, type: 'eq'}]
  }
  return arr
})
let userFilterData = computed(() => {
  const arr = [];

  if (props.dataScope === 'org') {
    arr.push({
      children: [
        { key: 'b.code', value: '{company}', type: 'likeRight' },
        { or: true, key: 'b.code', value: '{company}', type: 'eq' }
      ]
    });
  } else if (props.dataScope === 'target') {
    arr.push({
      key: 'b.id',
      value: (props.targetOrgId && typeof props.targetOrgId === 'object') ? props.targetOrgId.id : props.targetOrgId,
      type: 'eq'
    });
  }

  if (props.excludeSysUsers) {
    arr.push({
      key: 'a.is_sys',
      value: '1',
      type: 'ne'
    });
  }

  return arr;
});

let searchKey = computed(() => {
  let arr = []
  if (props.dataScope !== 'target') {
    arr.push('b.parent_ids')
  }
  if (!isHideLoginName.value) {
    arr.push('a.login_name')
  }
  arr.push('a.name')
  return arr
})
let searchLabel= computed(() => {
  let arr = []
  if (props.dataScope !== 'target') {
    arr.push('归属机构')
  }
  if (!isHideLoginName.value) {
    arr.push('登录名')
  }
  arr.push('姓名')
  return arr
})
let treePostData = computed(() => {
  let postData = {
    pageParam: {
      pageNo: 1,
      //查1000个 如果多于10000个 请使用分页 componentType配置建议使用table
      pageSize: 10000,
    },
    tableName: 'sys_office',
    filterData: filterData.value,
  }

  return postData
})

let tableDataUrl = ref(``)

const onSearch = () => {
  if (!props.disabled) {
    if (props.componentType === 'table' || props.dataScope === 'target') {
      instance.refs.userModal.onSearch()
    } else {
      instance.refs.modal.open('请选择用户')
    }
  }
}

const onTreeSelect = (selectedKeys, info) => {
  title.value = info.node.parentTitles.join(' / ')
  singleTable.value.initParam = {
    parent_id: {id: selectedKeys[0]},
    filterDataArr: userFilterData.value
  }
  tableDataUrl.value = 'dynamic/zform/datamap?path=path&traceFlag=&formNo=sys_user&parentId='
  instance.refs.tableView.loadData()
}

const clickRow = ({value, row, index}) => {
  if (value === 'check') {
    currentValue.value = row.name
    emits('update:value', {
      id: row.id, name: row.name
    })
    emits('change', {
      id: row.id, name: row.name
    })
    instance.refs.modal.close()
  }
}
const clickOk = () => {
  let rows = instance.refs.tableView.getSelectedRows();
  if (rows.length > 0) {
    let row = rows[0]
    if (row.id === selectValue.value.id) {
      emits('update:value', selectValue.value)
      emits('change', selectValue.value)
    } else {
      currentValue.value = row.name
      emits('update:value', {
        id: row.id, name: row.name
      })
      emits('change', {
        id: row.id, name: row.name
      })
    }
    instance.refs.modal.close()
  } else {
    message.warning('请选择用户！')
  }

}

const change = (val) => {
  emits('update:value', val)
  emits('change', val)
  selectValue.value = val
  currentValue.value = val ? val.name : ''
}

const clearSelection = () => {
  selectValue.value = null
  currentValue.value = null
  singleTable.value.defaultSelectedRowKeys = []
  emits('update:value', null)
  emits('change', null)
}
</script>
<style lang="less" scoped>
.user-select {
  display: flex;
}

.user-select-clear {
  padding: 0 8px;
}
</style>
<style lang="less">
.user-select-modal-content {
  box-sizing: border-box;
  display: flex;
  justify-content: space-between;

  .left-tree {
    width: 300px;
    height: 598px;
  }

  .right-content {
    box-sizing: border-box;
    width: calc(100% - 328px);
    background: #FFFFFF;
    box-shadow: 6px 6px 54px rgba(0, 0, 0, 0.05);
    border-radius: 14px;
    padding: 32px;
  }
}
</style>
