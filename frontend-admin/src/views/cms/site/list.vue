<template>
  <SingleTableView :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="站点"
                   :class="useModernListSkin ? 'modern-list-page cms-site-list-view' : ''"
                   :autoHeight="useModernListSkin"
                   :queryArea="queryArea"
                   :buttons="buttons"
                   formNo="prt_site">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">站点管理</h2>
        <p class="modern-list-query-desc">维护 CMS 站点编号、访问地址、状态和排序信息。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="name" label="站点名称" type="input" :props="{placeholder:'站点名称' }"></QueryField>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "cms_site_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "./form";
import config from "@/config";
let modalComponent = form
const useModernListSkin = computed(() => {
  return (config.theme?.adminLayout === 'modern' || config.theme?.layoutStyle === 'modern') && config.theme?.modernListSkin !== false
})
let queryArea = computed(() => {
  return useModernListSkin.value ? { resetButton: true, queryAreaStyle: 'ltr' } : {}
})

let buttons = ref([{
  value: 'add',
  text: '添加',
  permission: 'app:prt_site:add'
}, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:prt_site:remove'
}])

let singleTable = ref({
  rowButtons: [
    {
      value: 'view',
      text: '查看',
      permission: 'app:prt_site:view'
    }, {
      value: 'edit',
      text: '编辑',
      permission: 'app:prt_site:edit'
    }
  ],
  columns: [
    {title: '站点编号', dataIndex: 'code', minWidth: 120, align: 'left'},
    {title: '站点名称', dataIndex: 'name', minWidth: 120, align: 'left'},
    {title: '站点HTTP', dataIndex: 'http_path', minWidth: 180, align: 'left'},
    {title: '状态', dataIndex: 'status', minWidth: 100, align: 'center', dict: 'sys_useable'},
    {title: '排序', dataIndex: 'sort', minWidth: 100, align: 'center'},
  ]
})

</script>
<style scoped>
</style>
