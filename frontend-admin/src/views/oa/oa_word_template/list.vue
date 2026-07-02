<template>
  <SingleTableView
    @clickRow="clickRow"
    :singleTable="singleTable"
    :modalWidth="500"
    :modalComponent="modalComponent"
    modalTitle="Word模板"
    :class="useModernListSkin ? 'modern-list-page oa-word-template-list-view' : ''"
    :autoHeight="useModernListSkin"
    :queryArea="queryArea"
    :buttons="buttons"
    formNo="oa_word_template"
    @clickButton="clickButton"
  >
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">Word模板</h2>
        <p class="modern-list-query-desc">维护公文模板文件，支持模板导出和模板文件完整性检查。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="word_name" label="模板名称" type="input" :props="{placeholder:'模板名称' }"></QueryField>
    </template>
    <template #extendHiddenArea>
      <u-modal ref="checkTemplateModal">
        <u-title>未找到文件</u-title>
        <a-table v-bind="fileSingleTable" :dataSource="checkTemplateData.notExistFile"></a-table>
        <u-title>未知文件</u-title>
        <a-table :columns="[
          {title: '文件名称', dataIndex: 'id', minWidth: 200, align: 'left'}
        ]" :dataSource="checkTemplateData.unknownFileMap"></a-table>
        <u-title>已找到文件</u-title>
        <a-table v-bind="fileSingleTable" :dataSource="checkTemplateData.existFile"></a-table>
      </u-modal>
    </template>
  </SingleTableView>
</template>

<script>
import UModal from "@/components/modal/UModal";
import UTitle from "@/components/text/UTitle";
export default {
  name: "oa_word_template_list",
  components: {UTitle, UModal},
}
</script>
<script setup>
import {computed, getCurrentInstance, ref} from "vue";
import form from "@/views/oa/oa_word_template/form";
import {getAction, postAction} from "@/api/action";
import {downLoadFileAction} from "@/api/api";
import config from "@/config";
let modalComponent = form
let instance = getCurrentInstance();
const useModernListSkin = computed(() => {
  return (config.theme?.adminLayout === 'modern' || config.theme?.layoutStyle === 'modern') && config.theme?.modernListSkin !== false
})
let queryArea = computed(() => {
  return useModernListSkin.value ? { resetButton: true, queryAreaStyle: 'ltr' } : {}
})

let buttons = ref([{
  value: 'add',
  text: '添加',
  permission: 'app:oa_word_template:add',
}, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:oa_word_template:remove',
}, {
  value: 'check-template',
  text: '检查模板文件',
  permission: 'app:oa_word_template:add',
}])

let singleTable = ref({
  rowButtons: [
    /*{
      value: 'view',
      text: '查看',
      permission: 'app:oa_word_template:view',
    }, */{
      value: 'edit',
      text: '编辑',
      permission: 'app:oa_word_template:edit',
    }, {
      value: 'exportWord',
      text: '导出',
      permission: 'app:oa_word_template:edit',
    }/*, {
      value: 'delete',
      text: '删除',
      permission: 'app:oa_word_template:del',
      color: 'error',
    }*/
  ],
  columns: [
    {title: 'ID/编码', dataIndex: 'id', minWidth: 200, align: 'center'},
    {title: '创建者', dataIndex: ['createBy','name'], minWidth: 120, align: 'left'},
    {title: '创建时间', dataIndex: 'create_date', minWidth: 120, align: 'center'},
    {title: '更新者', dataIndex: ['updateBy','name'], minWidth: 120, align: 'left'},
    {title: '更新时间', dataIndex: 'update_date', minWidth: 120, align: 'center'},
    {title: '模板名称', dataIndex: 'word_name', minWidth: 120, align: 'left', sorter: 'false'},
  ]
})

const clickRow = ({value, row}) => {
  if (value === 'exportWord') {
    getAction("/oa/oaWordTemplate/exportWord", {fileGroupId: row.template_file}).then((res) => {
      if (res.data.data) {
        downLoadFileAction(res.data.data)
      }
    })
  }
}

let checkTemplateData = ref({
  notExistFile: [],
  existFile: [],
  unknownFile: [],
  unknownFileMap: [],
});

let fileSingleTable = ref({
  columns: [
    {title: '编码', dataIndex: 'word_code', minWidth: 200, align: 'center'},
    {title: 'ID', dataIndex: 'id', minWidth: 200, align: 'center'},
    {title: '文件名称', dataIndex: 'name_', minWidth: 120, align: 'left'},
    {title: '模板名称', dataIndex: 'word_name', minWidth: 120, align: 'left'},
  ]
})

const clickButton = ({value}) => {
  if ('check-template' === value) {
    postAction('oa/oaWordTemplate/checkTemplate', {}).then((res) => {
      checkTemplateData.value = res.data;
      checkTemplateData.value.unknownFileMap = res.data.unknownFile.map(i => {
        return {id: i}
      });
      instance.refs.checkTemplateModal.open('检查模板文件');
    })
  }
}

</script>
<style scoped>
</style>
