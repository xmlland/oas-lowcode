<template>
  <left-tree-right-table-view ref="rightTableViewRef" :rightTableView="rightTableView" modalTitle="模型管理" treeFormNo="sys_office"
                              :treeWidth="useModernListSkin ? 260 : 300"
                              :class="useModernListSkin ? 'modern-left-tree-right-page process-center-ltr-view' : ''"
                              @clickRow="clickRow">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">流程模型</h2>
        <p class="modern-list-query-desc">按机构和流程分类维护模型，支持设计、部署、设置和回退权限配置。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="category" label="流程分类" type="select" :props="{placeholder:'流程分类',dictType:'act_category'}"></QueryField>
    </template>
  </left-tree-right-table-view>
  <u-modal ref="fallBackPermissionsModal">
    <oa_task_fallback_permissions_setting_list :procDefKey="procDefKey"/>
  </u-modal>
</template>

<script>
export default {
  name: "oa_process_center_list"
}

</script>
<script setup>
import {computed, getCurrentInstance, ref} from "vue";
import form from "@/views/oa/processCenter/form";
import {getAction} from "@/api/action";
import {message} from "ant-design-vue";
import oa_task_fallback_permissions_setting_list from "@/views/oa/oa_task_fallback_permissions_setting/list";
import config from "@/config";

let instance = getCurrentInstance();
const useModernListSkin = computed(() => {
  return (config.theme?.adminLayout === 'modern' || config.theme?.layoutStyle === 'modern') && config.theme?.modernListSkin !== false
})

let rightTableView=ref({
  modalComponent:form,
  formNo:'act_re_model',
  autoHeight: useModernListSkin.value,
  queryArea: useModernListSkin.value ? { resetButton: true } : {},
  url: {
    list: 'service/model/listmap',
  },
  buttons: [{
    value: 'add',
    text: '添加',
  }],
  singleTable:{
    optionWidth: 180,
    rowButtons: [
      {
        value: 'disignModel',
        text: '设计',
      }, {
        value: 'deployModel',
        text: '部署',
      }, {
        value: 'edit',
        text: '设置',
      }, {
        value: 'setFallBackPermissions',
        text: '设置回退权限',
      }, {
        value: 'deleteModel',
        text: '删除',
        color: 'error',
      }
    ],
    columns: [
    {title: '流程分类', dataIndex: 'category_name', width: 160, minWidth: 120, align: 'left'},
    {title: '模型名称', dataIndex: 'name', width: 220, minWidth: 120, align: 'left'},
    {title: '归属机构', dataIndex: 'officeName', width: 160, minWidth: 120, align: 'center'},
    /*{title: '表单路径', dataIndex: ['metaInfo', 'scope'], width: 220, minWidth: 120, align: 'left'},*/
    {title: '最后更新时间', dataIndex: 'lastUpdateTime', width: 180, minWidth: 120, align: 'center'},
    ]
  }
})

let procDefKey = ref(null);

const clickRow = ({value, row, index}) => {
  if ('disignModel' === value) {
    let href = import.meta.env.BASE_URL + 'modeler.html?modelId='+row.id+'&ctx=/gtoa/a';
    window.open(href);
  } else if ('deployModel' === value) {
    let loading = message.loading("部署流程...")
    getAction('service/model/deploy', {id:row.id}).then((res) => {
      message.success(res.msg);
      loading()
    })
  } else if ('deleteModel' === value) {
    let loading = message.loading("删除流程...")
    getAction('service/model/delete', {id:row.id}).then((res) => {
      message.success(res.msg);
      loading()
      instance.refs.rightTableViewRef.saveSuccess()
    })
  } else if('setFallBackPermissions' === value){
    procDefKey.value = row.metaInfo.procDefKey;
    instance.refs.fallBackPermissionsModal.open('回退权限设置');
  }
}

</script>
<style scoped>
</style>
