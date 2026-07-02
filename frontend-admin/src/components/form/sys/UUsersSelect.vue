<template>
  <div>
    <a-input-group class="users-select" compact @click="onSearch">
      <div class="name-container">
        <a-tag :closable="!disabled" v-bind:key="item.id" v-for="(item) in  currentValueMap"
               @close="remove(item.id)">
          {{ item.name }}
        </a-tag>
      </div>
      <a-button v-if="!disabled">
        <template #icon>
          <SearchOutlined/>
        </template>
      </a-button>
    </a-input-group>
    <a-form-item style="width: 0;height: 0" v-if="componentType==='default'&&dataScope !== 'target'">
      <u-modal ref="modal" :width="parseInt(modalWidth)" :formDisabled="true" :customOK="true" @clickOk="clickOk">
        <div class="users-select-modal-content">

          <a-transfer
              v-model:target-keys="targetKeys"
              class="tree-transfer"
              :data-source="dataSource"
              :render="item => `${item.title} ${item.description}`"
              :show-select-all="false"
              style="height: 60vh"
          >
            <template #children="{ direction,selectedKeys, onItemSelect}">
              <div style="height: 100%" v-if="direction === 'left'">
                <u-tree :data="treeData" :showSearch="true" :showIcon="true" :checkable="true" :selectable="false"
                        @check="(checkedKeys, {checked, checkedNodes, node, event})=>{onTreeCheck(checkedKeys, {checked, checkedNodes, node, event},selectedKeys, onItemSelect)}">
                  <template #icon="{ key }">
                    <u-icon :type="isUser(key)?'fa-user':'fa-bank'"></u-icon>
                  </template>
                </u-tree>
              </div>
              <div style="height: 100%;width: 100%;" v-if="direction === 'right'">
                <single-table-view :queryAreaShow="false" :singleTable="singleTable"
                                   @onSelectAll="(selected, selectedRows, changeRows)=>{onSelectAll(selected, selectedRows, changeRows,selectedKeys, onItemSelect)}"
                                   @onSelect="(record, selected, selectedRows, nativeEvent)=>{onSelect(record, selected, selectedRows, nativeEvent,selectedKeys, onItemSelect)}"></single-table-view>
              </div>
            </template>
          </a-transfer>
        </div>
      </u-modal>
    </a-form-item>
    <a-form-item style="display: none" v-if="componentType==='table'||dataScope === 'target'">
      <u-modal-multi-select ref="userModal" formNo="sys_user" modal-title="请选择用户"
                            :searchKey="searchKey" :searchLabel="searchLabel"
                            :filterData="filterData"
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
                            :columns="[
                                  {title: '归属机构', dataIndex: ['parent','name'],  align: 'left'},
                                  {title: '登录名', dataIndex:'login_name',align: 'left'},
                                  {title: '姓名', dataIndex:'name',align: 'left'},
                                ]"
                            codeFiled="login_name"
                            nameFiled="name"
                            :format="item=>item.name"
                            :disabled="disabled" :value="selectUserList" @change="change"/>
    </a-form-item>
  </div>
</template>

<script>
export default {
  name: "UUsersSelect",
}
</script>
<script setup>

import {computed, getCurrentInstance, ref, watch} from "vue";
import {getUserDataAction, listDataAction} from "@/api/api";
import {listToTree, oneOf} from "@/lib/tools";
import config from "@/config";
import UModalMultiSelect from "@/components/form/UModalMultiSelect";

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
  disabledOrg: {
    type: Boolean,
    default: true
  },
  placeholder: {
    type: String,
    default: ''
  },
  modalWidth: {
    type: [String, Number],
    default: 1200
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
    default: config.component && config.component.usersSelect && config.component.usersSelect.componentType ? config.component.usersSelect.componentType : 'default'
  }
})
let currentValue = ref(null)
let currentValueMap = ref([]);
let emits = defineEmits(['update:value', 'change'])
let targetKeys = ref([])
let dataSource = ref([])
let orgUserList = ref([])
let userIdArr = ref([])
let userMap = ref([])
let selectUserList = ref([])
let filterData = ref([])
let init = true;
watch(() => props.value, () => {
  if (props.value && props.value.id) {
    currentValue.value = props.value.name
    if(init && currentValueMap.value.length === 0){
      //根据id查找用户信息
      let filterData = [{key:'a.id',type:'in',value:props.value.id}]
      listDataAction('sys_user',{filterDataArr:filterData}).then(res=>{
        let rows = res.rows;
        // 初始化一个空对象，用于存储ID到名称的映射
        let nameMap = {}

        // 遍历rows数组，建立ID到名称的映射
        for(let i = 0;i<rows.length;i++){
          nameMap[rows[i].id] = rows[i].name
        }
        // 分割props.value.id字符串，获取ID列表
        let idList = props.value.id.split(',')
        // 遍历ID列表，构建当前值的映射对象数组
        for (let i = 0; i < idList.length; i++) {
          currentValueMap.value.push({
            id: idList[i], name: nameMap[idList[i]]
          })
        }
      })
    }
    // 初始化 如果存在值，那么设置为已初始化
    init = false;
  } else {
    currentValue.value = null
    currentValueMap.value = []
    targetKeys.value = []
    selectUserList.value = []
    init = true
  }
}, {deep: true,immediate: true})
let treeData = computed(() => {
  let arr = orgUserList.value
  let rootId = '0';

  let rootArr = arr.filter(item=>item.parentId==='0')
  if (rootArr.length===0){
    //所有机构的id数据
    let orgIds = arr.filter(item=>item.hasChildren).map(item=>item.parentId);
    rootId = orgIds[0]
  }

  if (targetKeys.value.length > 0) {
    arr = arr.filter(item => {
      return !oneOf(item.id, targetKeys.value)
    })
  }
  return listToTree(arr, rootId, {
    title: 'name',
    key: 'id',
    parent: 'parentId',
    showCount: (item, node) => {
      if (node.children) {
        return count(node.children)
      }
      if (node.attributes.loginName) {
        return node.attributes.loginName
      }
      return ''
    }
  })
})

let singleTable = computed(() => {
  let arr = orgUserList.value
  let data = arr.filter(item => {
    return oneOf(item.id, targetKeys.value) && item.loginName
  })
  return {
    rowButtons: [],
    disableInitLoad: true,
    columns: [
      {title: '登录名', dataIndex: 'loginName', align: 'left',minWidth: 100},
      {title: '姓名', dataIndex: 'name', align: 'left',minWidth: 100},
    ],
    pagination: false,
    data: data,
  }
})

const onSelectAll = (selected, selectedRows, changeRows, selectedKeys, onItemSelect) => {
  selectedKeys.forEach(item => {
    onItemSelect(item, false)
  })
  selectedRows.forEach(item => {
    onItemSelect(item.id, true)
  })
}
const onSelect = (record, selected, selectedRows, nativeEvent, selectedKeys, onItemSelect) => {
  selectedKeys.forEach(item => {
    onItemSelect(item, false)
  })
  selectedRows.forEach(item => {
    onItemSelect(item.id, true)
  })
}

const isUser = (key) => {
  return oneOf(key, userIdArr.value)
}
const onTreeCheck = (checkedKeys, {checked, checkedNodes, node, event}, selectedKeys, onItemSelect) => {
  selectedKeys.forEach(item => {
    onItemSelect(item, false)
  })
  checkedKeys.forEach(item => {
    if (isUser(item)) {
      onItemSelect(item, true)
    }
  })
}
const count = (data) => {
  let total = 0;
  data.forEach(item => {
    if (item.children) {
      total += count(item.children)
    } else if (item.attributes.loginName) {
      total += 1
    }
  })
  return total
}
let searchKey = computed(() => {
  let arr = []
  if (props.dataScope !== 'target') {
    arr.push('b.parent_ids')
  }
  arr.push('a.name')
  return arr
})
let searchLabel= computed(() => {
  let arr = []
  if (props.dataScope !== 'target') {
    arr.push('归属机构')
  }
  arr.push('姓名')
  return arr
})
const onSearch = () => {
  if (!props.disabled) {
    dataSource.value = []
    userIdArr.value = []
    userMap.value = {}
    targetKeys.value = []
    if (props.value && props.value.id) {
      targetKeys.value = props.value.id.split(',')
      selectUserList.value = []
      for (let i = 0; i < targetKeys.value.length; i++) {
        selectUserList.value.push({
          dictId: targetKeys.value[i],
          name: currentValueMap.value[i].name
        })
      }

    }

    let filterList = []
    if (props.dataScope === 'org') {
      // 机构及下属机构
      filterList = [{children: [{key: 'code', value: '{company}', type: 'likeRight'}, {or: true, key: 'code', value: '{company}', type: 'eq'}]}]
    } else if (props.dataScope === 'target') {
      filterList = [{key: 'a.id', value: (props.targetOrgId && typeof props.targetOrgId === 'object') ? props.targetOrgId.id : props.targetOrgId, type: 'eq'}]
    }

    if (props.componentType === 'table' || props.dataScope === 'target') {
      filterList = []
      if (props.dataScope === 'org') {
        // 机构及下属机构
        filterList = [{children: [{key: 'b.code', value: '{company}', type: 'likeRight'}, {or: true, key: 'b.code', value: '{company}', type: 'eq'}]}]
      } else if (props.dataScope === 'target') {
        filterList = [{key: 'b.id', value: (props.targetOrgId && typeof props.targetOrgId === 'object') ? props.targetOrgId.id : props.targetOrgId, type: 'eq'}]
      }
      filterData.value = filterList
      instance.refs.userModal.onSearch()
      return
    }

    getUserDataAction(filterList).then(userRes => {
      let arr = userRes.data.data
      let rootArr = arr.filter(item=>item.parentId==='0')
      if (rootArr.length===0){
        //所有机构的id数据
        let orgIds = arr.filter(item=>item.hasChildren).map(item=>item.id);
        arr = arr.filter(item=>oneOf(item.parentId,orgIds)||oneOf(item.id,orgIds))
      }
      arr.forEach(item => {
        if (item.loginName) {
          userIdArr.value.push(item.id)
          userMap.value[item.id] = item
          dataSource.value.push({
            key: item.id, title: item.loginName, description: item.name, disabled: false
          })
        }else if(!props.disabledOrg){
          item.disabled = false;
        }
      })
      orgUserList.value = arr
      instance.refs.modal.open('请选择用户')
    })
  }
}

const clickOk = () => {

  if (targetKeys.value.length > 0) {

    let idArr = []
    let nameArr = []
    let dataArr = []
    let loginNameArr = []
    targetKeys.value.forEach(item => {
      idArr.push(item)
      nameArr.push(userMap.value[item].name)
      dataArr.push(userMap.value[item]);
      loginNameArr.push(userMap.value[item].loginName)
    })
    currentValueMap.value = dataArr;
    currentValue.value = nameArr.join()
    const value = {
      id: idArr.join(), name: nameArr.join(), loginName: loginNameArr.join()
    }
    emits('update:value', value)
    emits('change', value)
  } else {
    emits('update:value', null)
    emits('change', null)
    currentValue.value = null
    currentValueMap.value = [];
  }
  instance.refs.modal.close()
}

const remove = (id) => {
  let idArr = []
  let nameArr = []
  if (currentValueMap.value) {
    currentValueMap.value = currentValueMap.value.filter(item => item.id !== id);
    idArr = currentValueMap.value.map(item => item.id);
    nameArr = currentValueMap.value.map(item => item.name);
    targetKeys.value = idArr
    selectUserList.value = currentValueMap.value.map(item => ({
      dictId: item.id,
      name: item.name
    }))
    if (idArr.length > 0) {
      currentValue.value = nameArr.join()
      const value = {
        id: idArr.join(), name: nameArr.join()
      }
      emits('update:value', value)
      emits('change', value)
    } else {
      currentValue.value = null
      emits('update:value', null)
      emits('change', null)
    }
  }
}

const open = () =>{
  onSearch();
}

const change = (rows) => {
  if (rows.length > 0) {
    currentValueMap.value = []
    let idArr = []
    let nameArr = []
    rows.forEach(item => {
      idArr.push(item.dictId)
      nameArr.push(item.name)
      currentValueMap.value.push({
        id: item.dictId,
        name: item.name
      })
    })
    currentValue.value = nameArr.join()
    selectUserList.value = rows
    const value = {
      id: idArr.join(), name: nameArr.join()
    }
    emits('update:value', value)
    emits('change', value)
  } else {
    emits('update:value', null)
    emits('change', null)
    currentValue.value = null
    currentValueMap.value = []
    selectUserList.value = []
  }
}
defineExpose({
  open
})
</script>
<style lang="less" scoped>
.users-select {
  display: flex;

  .name-container {
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: flex-start;
    overflow: hidden;
    flex: 1;
    padding: 0 9px;
    flex-wrap: wrap;
    border: 1px solid #d9d9d9;

    .ant-tag {
      margin: 2px;
    }
  }
}
</style>
<style lang="less">
.users-select-modal-content {

  .ant-transfer-list {
    width: 40%;

    .ant-transfer-list-body {
      height: calc(100% - 40px);

      .ant-transfer-list-body-customize-wrapper {
        height: 100%;

        .u-tree {
          padding: 0;
          border-radius: 0
        }

        .single-table-view {
          padding: 0;

          .ant-table-striped {
            margin: 0;
          }
        }

      }

    }
  }

}
</style>
