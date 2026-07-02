<template>
  <SingleTableView :modalFull="false"
    :class="useModernListSkin ? 'modern-list-page demo-mtable-sub1-list-view' : ''"
    :autoHeight="useModernListSkin"
    :queryArea="queryArea"
    :subTable="true" :queryButton="true" :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="测试子表1" :buttons="buttons" formNo="demo_mtable_sub1" :modalWidth="1200" >
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">测试子表1</h2>
        <p class="modern-list-query-desc">验证子表选择、导出和批量删除操作。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="name" label="姓名" type="input" :props="{placeholder:'姓名' }"></QueryField>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "demo_mtable_sub1_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "@/views/demo/demo_mtable_sub1/form";
import config from "@/config";
let modalComponent = form
const useModernListSkin = computed(() => {
  return (config.theme?.adminLayout === 'modern' || config.theme?.layoutStyle === 'modern') && config.theme?.modernListSkin !== false
})
let queryArea = computed(() => {
  return useModernListSkin.value ? { resetButton: true, queryAreaStyle: 'ltr' } : {}
})

let buttons = ref([
  {
    "value": "add",
    "text": "选择",
    "permission": "",
    "batchSelect": {
      "selectDataModalProps": {
        "singleTable": {
          "showSorter": false,
          "columns": [
            {
              "dataIndex": "login_name",
              "title": "登录名",
              "isQuery": "0",
              "isShow": "1",
              "align": "left",
              "dict": "",
              "__temp_id__": "8f96203e-3c5d-496b-b14c-4c6b3dafca95"
            },
            {
              "dataIndex": "name",
              "title": "姓名",
              "isQuery": "0",
              "isShow": "1",
              "align": "left",
              "dict": "",
              "__temp_id__": "96842188-434b-42d7-8070-ca171b124cd3"
            }
          ]
        },
        "modalWidth": 1200,
        "targetFilterData": [
          {
            "key": "targetTable.parent_id",
            "value": "${parentId}",
            "type": "eq"
          }
        ],
        "filterData": [],
        "fromFormNo": "sys_user",
        "targetFormNo": "demo_mtable_sub1",
        "targetField": "user_id"
      },
      "dataMapping": {},
      "queryFieldArr": []
    }
  },
  {
    "value": "export",
    "text": "导出Excel",
    "permission": ""
  },
  {
    "value": "batch-delete",
    "text": "删除",
    "permission": "",
    "disabledFilterStr": "return rows.length===0"
  }
])

let singleTable = ref({
  rowButtons: [
    {
      value: 'view',
      text: '查看',
    }, {
      value: 'edit',
      text: '编辑',
    }
  ],
  columns: [
    {title:'姓名',dataIndex:'name',minWidth:150,align:'center',sorter:'true',ellipsis:false},
    {title:'性别',dataIndex:'gender',minWidth:150,align:'center',sorter:'true',dictMultiple:'0',ellipsis:false,dict:'sex'},
    {title:'身份证号',dataIndex:'id_number',minWidth:150,align:'center',sorter:'false',ellipsis:false},
    {title:'生日',dataIndex:'birthday',minWidth:150,align:'center',sorter:'false',ellipsis:false},
    {title:'工作年限',dataIndex:'years_of_work',minWidth:150,align:'center',sorter:'false',ellipsis:false},
  ]
})

</script>
<style scoped>
</style>
