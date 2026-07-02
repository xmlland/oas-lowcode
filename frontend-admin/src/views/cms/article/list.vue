<template>
  <left-tree-right-table-view :rightTableView="rightTableView" modalTitle="文章" treeFormNo="prt_channel"
                              :treeWidth="useModernListSkin ? 260 : 300"
                              :class="useModernListSkin ? 'modern-left-tree-right-page cms-article-ltr-view' : ''">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">文章管理</h2>
        <p class="modern-list-query-desc">按栏目筛选 CMS 文章，维护文章标题、作者、发布状态和发布时间。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="title" label="文章标题" type="input" :props="{placeholder:'文章标题' }"></QueryField>
      <QueryField name="release_date" label="发布时间" type="date-range" :props="{formatPatter:'yyyy-MM-dd'}"></QueryField>
      <QueryField name="status" label="状态" type="select" :props="{placeholder:'状态',dictType:'prt_info_status'}"></QueryField>
    </template>
  </left-tree-right-table-view>
</template>

<script>
export default {
  name: "cms_article_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "./form";
import config from "@/config";
const useModernListSkin = computed(() => {
  return (config.theme?.adminLayout === 'modern' || config.theme?.layoutStyle === 'modern') && config.theme?.modernListSkin !== false
})

let rightTableView=ref({
  modalComponent:form,
  formNo:'prt_information',
  autoHeight: useModernListSkin.value,
  queryArea: useModernListSkin.value ? { resetButton: true } : {},
  buttons: [{
    value: 'add',
    text: '添加',
    permission: 'app:prt_information:add'
  }, {
    value: 'batch-delete',
    text: '删除',
    permission: 'app:prt_information:del',
  }],
  singleTable:{
    rowButtons: [
      {
        value: 'view',
        text: '查看',
        permission: 'app:prt_information:view'
      }, {
        value: 'edit',
        text: '编辑',
        permission: 'app:prt_information:edit'
      }, {
        value: 'delete',
        text: '删除',
        permission: 'app:prt_information:del',
        color: 'error',
      }
    ],
    columns: [
    {title: '栏目名称', dataIndex: ['parent_id','name'], minWidth: 120, align: 'left'},
    {title: '文章标题', dataIndex: 'title', minWidth: 200, align: 'left'},
    {title: '作者', dataIndex: 'author', minWidth: 100, align: 'left'},
    {title: '发布人', dataIndex: 'release_name', minWidth: 100, align: 'left'},
    {title: '发布时间', dataIndex: 'release_date', minWidth: 150, align: 'center'},
    {title: '状态', dataIndex: 'status', minWidth: 100, align: 'center', dict: 'prt_info_status'},
    ]
  }
})

</script>
<style scoped>
</style>
