<template>
  <div>
    <u-upload ref="upload" :visible="false" :value="value" @update:value="change" @fileListChange="fileListChange" :fileCount="fileCount"/>
    <div v-if="!disabled" v-show="fileList.length<fileCount" style="text-align: right">
      <a-button type="primary" @click="uploadFile">上传附件</a-button>
    </div>
    <single-table v-bind="singleTableRef" @clickRow="clickRow"></single-table>
  </div>
</template>

<script>
import UUpload from "@/components/form/UUpload";
import SingleTable from "@/components/table/SingleTable";

export default {
  name: "UFileTableList",
  components: {SingleTable, UUpload}
}
</script>
<script setup>

import {computed, ref, getCurrentInstance} from "vue";
import {downLoadFileAction, previewFile} from "@/api/api";

let props = defineProps({
  value: {
    type: String,
    default: null
  },
  disabled: {
    type: Boolean,
    default: false
  },
  /**
   * 禁用状态是否允许删除
   */
  disabledAllowDelete: {
    type: Boolean,
    default: true
  },
  columns: {
    type: Array,
    default: () => {
      return [{
        title: '文件名称',
        dataIndex: 'name',
        minWidth: 300,
        align: 'left',
      }, {
        title: '上传时间',
        dataIndex: 'uploadTime',
      }, {
        title: '上传人',
        dataIndex: 'uploadName',
      }]
    }
  },
  fileCount: {
    type: Number,
    default: 10
  },
})
let fileList = ref([])
let singleTableRef = computed(() => {
  return {
    autoHeight: true,
    rowSelection: false,
    columns: props.columns,
    data: fileList.value,
    pagination: false,
    showSorter: false,
    rowButtons: [{
      value: 'preview',
      text: '预览',
    }, {
      value: 'download',
      text: '下载',
    }, {
      value: 'removeFile',
      text: '删除',
      visibleFilter: (row) => {
        return row.removeAble && (props.disabled ? props.disabledAllowDelete : true)
      }
    }]
  }
})
let emits = defineEmits(['update:value', 'fileListChange'])
const change = (groupId) => {
  emits('update:value', groupId)
}
const fileListChange = (_fileList) => {
  fileList.value = _fileList
  emits('fileListChange', _fileList)
}
let instance = getCurrentInstance();
const clickRow = ({value, row, index}) => {
  console.log(value, row, index)
  if ('preview' === value) {
    previewFile(row.id + '.' + row.ext)
  } else if ('download' === value) {
    downLoadFileAction(row.id)
  } else if ('removeFile' === value) {
    instance.refs.upload.remove(row)
  }
}
const uploadFile = () => {
  instance.refs.upload.triggerUpload()
}
</script>
<style scoped>

</style>
