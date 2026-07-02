<template>
  <div>
    <template v-bind:key="fileIndex" v-for="(file,fileIndex) in allFileList">
      <div>
        <UFileItem :file="file" :download="true" :preview="false" :deleteButton="true" :actions="{'delete':deleteFileAction}"/>
      </div>
    </template>
    <a class="table-row-btn" v-if="isShowUpload" @click="uploadFile" type="link">上传</a>
    <UUpload :ignoreLoadByGroupId="true" :initFileList="fileList" ref="file" :visible="false" v-model:value="currentVal" @fileChange="fileChange" v-bind="uploadPropsLocal"/>
  </div>
</template>

<script>
export default {
  name: "UTableCellUpload"
}
</script>
<script setup>
import {computed, getCurrentInstance, ref, nextTick, onMounted} from "vue";
import UUpload from "@/components/form/UUpload";
import UFileItem from "@/components/ext/UFileItem";
import {confirmAction} from "@/api/action";
import {deleteFileUrl} from "@/api/api";
import {oneOf} from "@/lib/tools";

let instance = getCurrentInstance();
let props = defineProps({
  disabled: {
    type: Boolean,
    default: false
  },
  uploadProps: {
    type: Object,
    default() {
      return {}
    }
  },
  fileList: {
    type: Array,
    default() {
      return []
    }
  },
  value: {
    type: String,
    default: ''
  }
})
let emits = defineEmits(['update:value'])
let currentVal = ref(props.value)

let propsFileList = ref([])
let tempFileList = ref([])

let allFileList = computed(() => {
  let arr = propsFileList.value.map((file, index) => {
    return instance.refs.file.formatFileObj(file, index)
  }).concat(tempFileList.value).filter(file => {
    return !oneOf(file.id, deleteList.value)
  })
  //根据id将arr去重
  let obj = {}
  arr.forEach(item => {
    if (!obj[item.id]) {
      obj[item.id] = item
    }
  })
  return Object.values(obj)

})
onMounted(() => {
  propsFileList.value = props.fileList
})

let uploadPropsLocal = computed(() => {
  let _props = JSON.parse(JSON.stringify(props.uploadProps))
  _props.fileCount = (_props.fileCount || 5)
  return _props
})

let isShowUpload = computed(() => {
  return !props.disabled && (uploadPropsLocal.value.fileCount - allFileList.value.length) > 0
})

let oldId = ''
const fileChange = (id) => {
  propsFileList.value = []
  tempFileList.value = instance.refs.file.getFiles()
  nextTick(() => {
    if (id === oldId) {
      //确保只触发一次
      return
    }
    emits('update:value', id)
    oldId = id

  })
}

const uploadFile = () => {
  instance.refs.file.triggerUpload()
}

let deleteList = ref([])
const deleteFileAction = (file) => {
  confirmAction('删除确认', '是否确认删除该附件？', deleteFileUrl, {fileId: file.id}, () => {
    deleteList.value.push(file.id)
    instance.refs.file.removeById(file.id)
    if(allFileList.value.length === 0){
      emits('update:value', '')
    }
  }, () => {
  }, 'get')
}
</script>
<style scoped>

</style>
