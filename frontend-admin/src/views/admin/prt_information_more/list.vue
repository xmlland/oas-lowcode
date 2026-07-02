<template>
  <left-tree-right-table-view :rightTableView="rightTableView" modalTitle="文章" treeFormNo="prt_channel"
                              :treeWidth="useModernListSkin ? 260 : 300"
                              :class="useModernListSkin ? 'modern-left-tree-right-page prt-information-more-ltr-view' : ''">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">文章浏览</h2>
        <p class="modern-list-query-desc">按栏目浏览门户文章，快速定位已发布内容和状态信息。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="title" label="文章标题" type="input" :props="{placeholder:'文章标题' }"></QueryField>
      <QueryField name="release_date" label="发布时间" type="date-range" :props="{formatPatter:'yyyy-MM-dd'}"></QueryField>
      <QueryField name="status" label="状态" type="select" :props="{placeholder:'状态',dictType:'prt_info_status'}"></QueryField>
    </template>
  </left-tree-right-table-view>
</template>

<script lang="jsx">
export default {
  name: "prt_information_list",
}
</script>
<script setup lang="jsx">
import {computed, ref} from "vue";
import form from "@/views/admin/prt_information/form";
import config from "@/config";
const useModernListSkin = computed(() => {
  return (config.theme?.adminLayout === 'modern' || config.theme?.layoutStyle === 'modern') && config.theme?.modernListSkin !== false
})
let rightTableView=ref({
  modalComponent:form,
  formNo:'prt_information',
  autoHeight: useModernListSkin.value,
  queryArea: useModernListSkin.value ? { resetButton: true } : {},
  buttons: [],
  singleTable:{
    rowButtons: [

    ],
    columns: [
      {title: '栏目名称', dataIndex: ['parent_id','name'], minWidth: 120, align: 'left'},
      {title: '文章标题', dataIndex: 'title', minWidth: 120, align: 'left'
        ,customRenderSlot:({text, record, index})=>{
          return <a-button type="link" onclick={()=> {
            window.open('/#/contentData' + '?parentid='+ '&value=' + index + '&id=' + record.id)
          }
          }>
            <span style={{ width: '250px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'normal', textAlign: 'left'}}>{text}</span>
          </a-button>
        }
        },
      {title: '拟稿人', dataIndex: 'draft_name', minWidth: 30, align: 'center'},
      {title: '发布人', dataIndex: 'release_name', minWidth: 30, align: 'center'},
      {title: '发布时间', dataIndex: 'release_date', minWidth: 120, align: 'center'},
      {title: '状态', dataIndex: 'status', minWidth: 30, align: 'center', dict: 'prt_info_status'},
    ]
  }
})
</script>
<style scoped>
</style>
