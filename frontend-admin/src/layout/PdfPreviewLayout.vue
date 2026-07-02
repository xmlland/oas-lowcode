<template>
  <div style="width: 100%;height: 100%">
    <a-tabs v-model:activeKey="activeKey">
      <a-tab-pane :key="index" :tab="formatName(file,index)" v-for="(file,index) in fileList">
        <iframe style="border: 0;width: 100%;height: calc(100vh - 46px - 22px)" :src="format(file)"/>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script>
export default {
  name: "PdfPreviewLayout"
}
</script>
<script setup>
import {useRoute} from "vue-router";
import {ref, watch} from "vue";
import {getFileListAction, listDataAction} from "@/api/api";
import {getWebUrl} from "@/lib/tools";

let route = useRoute()
let gId = ref('')
let fIds = ref('')
let fNames = ref([])
let activeKey = ref(0)
let fileList = ref([])
const loadFileByGid = () => {
  if (gId.value) {
    getFileListAction(gId.value).then(res => {
      let files = res.data.fileListMap.files;
      fileList.value = files
    })
  }
}
const loadFileByFIds = () => {
  if (fIds.value) {
    listDataAction('sys_file_', {
      'a.id': fIds.value,
      queryParamType: {
        'a.id': 'in',
      }
    }).then(res => {
      fileList.value = res.rows
    })
  }
}
watch(() => route, () => {
  gId.value = route.query.gId
  fIds.value = route.query.fIds
  fNames.value = (route.query.fNames || '').split(',').filter(item=>item)
  loadFileByGid()
  loadFileByFIds()
}, {immediate: true})
const format = (file) => {
  let fileId = file.id + '.' + (file.ext || file.ext_)
  const dotIndex = fileId.lastIndexOf('.')
  const ext = dotIndex > -1 ? fileId.substring(dotIndex + 1).toLowerCase() : ''
  const id = fileId.substring(0, dotIndex)
  return getWebUrl() + '#/onlineEditView?fileId=' + id + '&file_name=' + encodeURIComponent(fileId) + '&mode=view&oss='
}
const formatName = (file, index) => {
  if (fNames.value.length > 0) {
    return fNames.value[index]
  }
  if (file.name_) {
    return file.name_.replace('.' + file.ext_, '')
  }
  if (file.name) {
    return file.name.replace('.' + file.ext, '')
  }
}
</script>
<style scoped>
:deep(.ant-tabs-tab-btn) {
  padding: 0 10px;
}
</style>
