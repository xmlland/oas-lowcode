<template>
  <u-modal ref="modal" :width="800" :full="false"
           :extendFormData="{}" :showOk="true" :showCancel="false"
           @saveSuccess="saveSuccess">
    <div style="min-height: 300px">
      <template v-for="(item,key) in fileList" :key="key">
        <u-message-link :item="item" :preventClickOpenModal="true" @onClickA="onClickA"
                        :title="item.title" :color="item.color" :time="item.update_date"></u-message-link>
      </template>
      <div v-if="fileList.length===0">
        暂无导出文件
      </div>
    </div>
  </u-modal>
</template>

<script>
export default {
  name: "UExportFileQueue"
}
</script>
<script setup>

import {postAction} from "@/api/action";
import {defaultTableViewUrl, downLoadFileAction} from "@/api/api";
import {ref, getCurrentInstance} from "vue";
import {Modal} from "ant-design-vue";

let instance = getCurrentInstance();

let props = defineProps({
  fileFrom:{
    type:String,
    default: null
  },
  modalTitle:{
    type:String,
    default: "导出队列"
  }
})

let fileList = ref([])
let colorMap = {
  SUCCESS:'green',
  EXPORTING: 'gray',
  FAIL:'red'
}

let titleMap = {
  SUCCESS:'导出成功，点击下载',
  EXPORTING: '导出中',
  FAIL:'导出失败'
}

const getFileList = () =>{
  return new Promise((resolve, reject)=>{
    let reqData = {
      pageParam:{
        pageNo: 1,
        pageSize: 20,
      },
    }
    if(props.fileFrom){
      reqData["a.file_from"] = props.fileFrom;
      reqData.queryParamType = {
        "a.file_from":""
      }
    }

    postAction(defaultTableViewUrl.list
        .replace('${parentId}','')
        .replace('${formNo}','sys_file_queue'),reqData).then((res)=>{
      let list = [];
      for(let k in res.rows){
        let i = res.rows[k];
        let f = {}
        Object.assign(f,i);
        f.color = colorMap[i.export_status]
        if(i.export_status==='SUCCESS'){
          f.title = i.file_name + ": " + titleMap[i.export_status]
        }else {
          f.title = titleMap[i.export_status]
        }
        list.push(f);
      }
      fileList.value = list;
      resolve();
    })
  })
}

const open = () =>{
  getFileList().then(()=>{
    instance.refs.modal.open(props.modalTitle);
  })
}

const close = () =>{
  instance.refs.modal.close();
}

const downLoad = (fileId) =>{
  downLoadFileAction(fileId);
}

const onClickA = (item) =>{
  if(item.export_status==="SUCCESS"){
    downLoad(item.file_id)
  }else if(item.export_status==="EXPORTING"){
    Modal.error({
      title:'正在导出中',
      content:'请耐心等待'
    })
  }
}

const rangeGetFileList = () =>{
  if(instance.refs.modal){
    if(instance.refs.modal.getModalVisible()){
      getFileList().then(()=>{
        setTimeout(()=>{
          rangeGetFileList();
        },3000)
      })
    }else {
      setTimeout(()=>{
        rangeGetFileList();
      },5000)
    }
  }else {
    setTimeout(()=>{
      rangeGetFileList();
    },5000)
  }
}

getFileList();
rangeGetFileList();

defineExpose({open,close})

</script>

<style scoped>

</style>
