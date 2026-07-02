<template>
  <a-space style="height: 32px">
    <file-image-outlined v-if="['jpg','png','jpeg'].indexOf(file.ext.toLowerCase())>=0"></file-image-outlined>
    <file-word-outlined v-else-if="['doc','docx'].indexOf(file.ext.toLowerCase())>=0"></file-word-outlined>
    <file-excel-outlined v-else-if="['xls','xlsx'].indexOf(file.ext.toLowerCase())>=0"></file-excel-outlined>
    <file-pdf-outlined v-else-if="['pdf'].indexOf(file.ext.toLowerCase())>=0"></file-pdf-outlined>
    <file-outlined v-else></file-outlined>
    <span :title="file.name">{{ file.name }}</span>
    <a v-if="download" href="javascript:;" @click="downloadAction(file)">
      下载
    </a>
    <a v-if="preview" href="javascript:;" @click="previewAction(file)">
      预览
    </a>
    <delete-outlined v-if="deleteButton" style="color: var(--ant-error-color)" @click="deleteAction(file)"/>
  </a-space>
</template>

<script>
export default {
  name: "UFileItem",
}
</script>
<script setup>

import {deleteFileUrl, downLoadFileAction, previewFile} from "@/api/api";
import {confirmAction} from "@/api/action";

let props = defineProps({
  file: {
    type: Object,
    default() {
      return {}
    }
  },
  download: {
    type: Boolean,
    default: true
  },
  preview: {
    type: Boolean,
    default: true
  },
  deleteButton: {
    type: Boolean,
    default: true
  },
  actions: {
    type: Object,
    default() {
      return {
        download: (file) => {
          downLoadFileAction(file.id)
        },
        preview: (file) => {
          previewFile(file.id+'.'+file.ext)
        },
        delete: (file) => {
          confirmAction('删除确认', '是否确认删除该附件？', deleteFileUrl, {fileId: file.id}, () => {
          }, () => {
          }, 'get')
        }
      }
    }
  }
})
let downloadAction = (file) => {
  if (props.actions.download) {
    props.actions.download(file)
  } else {
    downLoadFileAction(file.id)
  }
}
let previewAction = (file) => {
  if (props.actions.preview) {
    props.actions.preview(file)
  } else {
    previewFile(file.id+'.'+file.ext)
  }
}
let deleteAction = (file) => {
  if (props.actions.delete) {
    props.actions.delete(file)
  } else {
    confirmAction('删除确认', '是否确认删除该附件？', deleteFileUrl, {fileId: file.id}, () => {
    }, () => {
    }, 'get')
  }
}
</script>
<style lang="less" scoped>
.ant-space {
  :deep(.ant-space-item:nth-child(2)) {
    text-overflow: ellipsis;
    white-space: nowrap;
    flex: 1;
    overflow: hidden;

  }
}
</style>
