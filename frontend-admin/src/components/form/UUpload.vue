<template>
  <div class="a-upload-div"  :id="contentId" v-show="visible">
    <a-upload
        :file-list="fileList"
        :listType="picture?'picture-card':'text'"
        :accept="realAccepts.join()"
        :beforeUpload="beforeUpload"
        :show-upload-list="{ showDownloadIcon: true, showRemoveIcon: !disabled }"
        :custom-request="customRequest"
        @download="download"
        @remove="remove"
        @preview="preview"
        :directory="directory"
        :multiple="multiple"
    >
      <template v-if="!disabled&&fileList.length < fileCount">
        <template v-if="!picture && !online">
          <a-spin v-if="uploadPercent"/>
          <a-button ref="upBtn" v-else>
            <upload-outlined></upload-outlined>
            {{ buttonText }}
          </a-button>
          <span style="color: red;margin-left: 10px">{{ promptText }}</span>
        </template>
        <div v-else-if="picture">
          <a-spin v-if="uploadPercent"/>
          <plus-outlined v-else/>
        </div>
      </template>
      <!-- online模式单独处理：始终显示上传按钮（支持替换文件） -->
      <template v-if="online && !disabled">
        <a-spin v-if="uploadPercent"/>
        <a-button ref="upBtn" v-else>
          <upload-outlined></upload-outlined>
          {{ buttonText }}
        </a-button>
      </template>
      <template v-if="!picture && !online" #itemRender="{ file, actions }">
        <a-space>

          <file-image-outlined v-if="['jpg','png','jpeg'].indexOf(getFileType(file.name))>=0"></file-image-outlined>
          <file-word-outlined v-else-if="['doc','docx'].indexOf(getFileType(file.name))>=0"></file-word-outlined>
          <file-excel-outlined v-else-if="['xls','xlsx'].indexOf(getFileType(file.name))>=0"></file-excel-outlined>
          <file-pdf-outlined v-else-if="['pdf'].indexOf(getFileType(file.name))>=0"></file-pdf-outlined>
          <file-outlined v-else></file-outlined>
          <a href="javascript:;" @click="actions.download">
            <span :style="file.status === 'error' ? 'color: red' : ''">{{ file.name }}</span>
          </a>
          <delete-outlined v-if="!disabled&&deleteAble" @click="actions.remove"/>

          <slot v-if="$slots.itemRender" name="itemRender" :file="file"></slot>
        </a-space>
      </template>
      <template v-else-if="picture&&isEdit&&!disabled" #itemRender="{file}">
        <div class="img-box">
          <img :src="file.url+'&uuid='+imgRandomKey" class="upload-img" :alt="file.name"/>
          <div class="icon-container">
            <a-tooltip placement="bottom" title="编辑图片">
              <a-button type="text" class="icon-style" size="middle"
                        @click="editImg(file)">
                <form-outlined/>
              </a-button>
            </a-tooltip>
            <a-tooltip placement="bottom" title="删除图片">
              <a-button type="text" class="icon-style" size="middle" @click="remove(file)">
                <delete-outlined/>
              </a-button>
            </a-tooltip>
            <a-tooltip placement="bottom" title="预览图片">
              <a-button type="text" class="icon-style" size="middle"
                        @click="preview(file)">
                <eye-outlined/>
              </a-button>
            </a-tooltip>
            <div class="sort-icon">
              <a-tooltip placement="bottom" title="左移图片">
                <a-button type="text" class="icon-style" size="middle" @click="handleClickSort(1,file)"
                          :disabled="file.index===0" v-show="isSort">
                  <caret-left-outlined/>
                </a-button>
              </a-tooltip>
              <a-tooltip placement="bottom" title="右移图片">
                <a-button type="text" class="icon-style" size="middle"
                          :disabled="file.index===fileList.length-1" v-show="isSort"
                          @click="handleClickSort(2,file)">
                  <caret-right-outlined/>
                </a-button>
              </a-tooltip>
            </div>
          </div>
        </div>
      </template>
      <template v-else-if="online" #itemRender="{file}">
        <div class="online-file-item">
          <div class="online-file-thumb img-office"></div>
          <div class="online-file-name" :title="file.name">{{file.name}}</div>
          <div class="online-file-actions">
            <a-tooltip v-if="!disabled" placement="bottom" title="编辑文件">
              <a-button type="text" class="online-action-btn" size="small"
                        @click="editOnline(file)">
                <form-outlined/>
              </a-button>
            </a-tooltip>
            <a-tooltip v-if="!disabled" placement="bottom" title="删除文件">
              <a-button type="text" class="online-action-btn" size="small" @click="remove(file)">
                <delete-outlined/>
              </a-button>
            </a-tooltip>
            <a-tooltip placement="bottom" title="预览文件">
              <a-button type="text" class="online-action-btn" size="small"
                        @click="previewOnline(file)">
                <eye-outlined/>
              </a-button>
            </a-tooltip>
          </div>
        </div>
      </template>
    </a-upload>
    <template v-if="picture&&disabled&&(fileList.length===0)">
      <slot name="emptyPicture"></slot>
    </template>
    <template v-if="online&&disabled&&(fileList.length===0)">
      <slot name="emptyOnline"></slot>
    </template>

    <a-image-preview-group
        :preview="{
          visible:previewVisible,
          onVisibleChange:setPreviewVisible,
          getContainer:container,
          current:previewCurrent
        }"  :mask-z-index="3000" >
      <a-image v-for="(data,index) in imageFileList" :key="index" :style="{ display: 'none' }" :width="0" :src="data.url"/>
    </a-image-preview-group>

    <a-modal :destroy-on-close="true" :body-style="editBodyType" :wrapClassName="'full-modal'" :visible="editVisiable"
             :title="editTitle" :footer="null"
             @cancel="handleEditCancel" :width="editModalWidth">
      <UImageEditor :editFile="editFile" @imgSaveSuccess="handleImageSaveSuccess" :key="imgRandomKey"/>
    </a-modal>
  </div>
</template>

<script>
export default {
  name: "UUpload"
}
</script>
<script setup>
import {message, Form, Modal} from 'ant-design-vue';
import {computed, getCurrentInstance, ref, watch} from "vue";
import config from '@/config'
import {getIntersection, getFileExtend, oneOf, UUID, getWebUrl} from "@/lib/tools";
import chunkUpload from "@/api/chunkUpload";
import {
  downLoadFileAction,
  getFileListAction,
  deleteFileUrl,
  fileDownloadUrl,
  previewFileByDialog, saveDataAction, batchSaveSelectAction,
  batchGetPresignedFileUrl
} from "@/api/api";
import {confirmAction, baseUrl} from "@/api/action";
import {useStore} from "vuex";
import UImageEditor from "@/components/form/UImageEditor.vue";

const store = useStore();
const formItemContext = Form.useInjectFormItemContext();
let instance = getCurrentInstance();
let userView = computed(() => store.getters.getUserView);
let props = defineProps({
  visible: {
    type: Boolean,
    default: true
  },
  value: {
    type: String,
    default: null
  },
  picture: {
    type: Boolean,
    default: null
  },
  online: {
    type: Boolean,
    default: null
  },
  //对象存储标志，三种格式：对象存储类型:桶:目录；桶:目录；目录，空表示不用对象存储，示例：minio:main:test
  oss: {
    type: String,
    default: "minio:main:userfiles"
  },
  //文件最大 M
  maxSize: {
    type: Number,
    default: config.maxFileSize
  },
  maximumDuration: {
    type: Number,
    default: 10//默认10分钟
  },
  fileCount: {
    type: Number,
    default: 5
  },
  accepts: {
    type: Array,
    default() {
      return config.acceptFiles
    }
  },
  disabled: {
    type: Boolean,
    default: false
  },
  deleteAble: {
    type: Boolean,
    default: true
  },
  bodyType: {
    type: Object,
    default() {
      return {maxHeight: '60vh'}
    }
  },
  //图片编辑Modal Style
  editBodyType: {
    type: Object,
    default() {
      return {"height": "100%"}
    }
  },
  modalWidth: {
    type: String,
    default: "520px"
  },
  editModalWidth: {
    type: String,
    default: "100%"
  },
  pictureStype: {
    type: String,
    default: 'width:100%;'
  },
  buttonText: {
    type: String,
    default: '上传'
  },
  promptText: {
    type: String,
    default: ''
  },
  runUpdateValue: {
    type: Boolean,
    default: true
  },
  previewFile: {
    type: Boolean,
    default: false
  },
  /*自定义预览*/
  customPreview: {
    type: Boolean,
    default: false
  },
  directory: {
    type: Boolean,
    default: false
  },
  multiple: {
    type: Boolean,
    default: false
  },
  //文件ID前缀
  idPrefix: {
    type: String,
    default: ''
  },
  //是否编辑图片
  isEdit: {
    type: Boolean,
    default: false
  },
  //是否更换图片顺序
  isSort: {
    type: Boolean,
    default: false
  },
  /**
  * 图片上传时验证
  * @param img
  * @return Promise
  * */
  imgValidate: {
    type: Function,
    default: () =>{
      return Promise.resolve(true);
    }
  },
  setDbData: {
    type: Boolean,
    default: false
  },
  formNo: {
    type: String,
    default: ''
  },
  data: {
    type: Object,
    default: () => {
    }
  },
  isDuration: {
    type: Boolean,
    default: false
  },
  isHorizontal: {
    type: Boolean,
    default: false
  },
  is1080P: {
    type: Boolean,
    default: false
  },
  customDownloadFile: {
    type: Function,
    default: null
  },
  /**
   * 是否忽略使用groupId加载数据  在列表中使用时，可以先批量查询数据通过initFileList传入，避免大量请求
   */
  ignoreLoadByGroupId: {
    type: Boolean,
    default: false
  },
  /**
   * 初始化文件列表
   */
  initFileList: {
    type: Array,
    default: () => {
      return []
    }
  }
})
const previewVisible = ref(false);
const editVisiable = ref(false);
const editTitle = ref('');
const editFile = ref({});
let uuid = UUID()
let previewCurrent = ref(0);
let contentId = ref('u-upload'+UUID());
let container = ref(document.getElementById(contentId.value));
let fileList = ref([]);
let id = ref(props.value)
let oss = ref(props.oss)
let emits = defineEmits(['update:value', 'fileChange', 'fileListChange', 'preview', 'editSaveSuccess', 'uploadFinish'])
let firstLoad = true
let imgRandomKey = ref(Math.random())

//online&&isEdit&&!disabled
console.log('online',props.online, 'isEdit', props.isEdit, 'disabled', props.disabled)

const imageExtsForUpload = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg']
const isImageFile = (fileName) => {
  if (!fileName) return false
  const ext = fileName.split('.').pop().toLowerCase()
  return imageExtsForUpload.includes(ext)
}

// 仅包含图片文件的列表，用于 a-image-preview-group，避免非图片文件触发无用的 fileDownload 请求
const imageFileList = computed(() => {
  return fileList.value.filter(f => isImageFile(f.name || f.ext || ''))
})

const getFileId = (file) => {
  if (!file.response) {
    return file.id
  } else if (file.response.data && file.response.data.map && file.response.data.map.successList.length > 0) {
    return file.response.data.map.successList[0].id
  }

}
const getFiles = () => {
  return fileList.value
}
const formatFileObj = (file,index) => {
  return {
    name: file.name || file.name_,
    id: file.id,
    uid: file.id,
    url: oss.value ? getOSSFileUrl(file) : (baseUrl + fileDownloadUrl + getFileId(file)),
    thumbUrl: oss.value ? getOSSFileUrl(file) : '',
    status: 'done',
    uploadTime: file.uploadTime,
    uploadName: file.uploadUser?.name || file.upload_user_name_ || '-',
    ext: file.ext || file.ext_,
    path: file.path || file.path_,
    sort: file.sort,
    editObjects: file.editObjects || file.edit_objects_,
    index: index,
    key: Math.random(),
    removeAble: userView.value.id === (file.upload_user_id_ || file.uploadUser?.id)
  }
}

// 异步批量更新图片文件的预签名URL
const updatePresignedUrls = async (files) => {
  const imageFiles = files.filter(f => isImageFile(f.name || f.ext))
  if (imageFiles.length === 0) return

  const fileIds = imageFiles.map(f => f.id)
  const urls = await batchGetPresignedFileUrl(fileIds)

  fileList.value.forEach(f => {
    if (urls[f.id]) {
      f.url = urls[f.id]
      f.thumbUrl = urls[f.id]
    }
  })
}

const getFileList = () => {
  getFileListAction(props.value).then(res => {
    let files = res.data.fileListMap.files;
    let _files = []
    files.forEach((file, index) => {
      _files.push(formatFileObj(file, index))
    })
    fileList.value = _files
    if (files.length === 0) {
      emits('update:value', '')
      emits('fileListChange', [])
      formItemContext.onFieldChange()
    } else {
      emits('update:value', id.value)
      emits('fileListChange', fileList.value)
      formItemContext.onFieldChange()
      // 文件加载完成后异步更新图片预签名URL
      updatePresignedUrls(_files)
    }

  })
}

const refreshInitFileList = () => {
  let arr = JSON.parse(JSON.stringify(fileList.value))
  arr = arr.concat((props.initFileList||[]).map((file, index) => {
    return formatFileObj(file, index)
  }))
  //根据id将arr去重
  let obj = {}
  arr.forEach(item => {
    if (!obj[item.id]) {
      obj[item.id] = item
    }
  })
  fileList.value = Object.values(obj)
  if (fileList.value.length === 0) {
    emits('update:value', '')
    emits('fileListChange', [])
    formItemContext.onFieldChange()
  } else {
    emits('update:value', id.value)
    emits('fileListChange', fileList.value)
    formItemContext.onFieldChange()
    // 文件加载完成后异步更新图片预签名URL
    updatePresignedUrls(fileList.value)
  }
}

watch(() => props.value, () => {
  if (!props.value) {
    id.value = uuid
    //赋值null后清空fileList #1457  zry 2022-11-26 14:06
    if (firstLoad && fileList.value.length > 0) {
      fileList.value = []
      firstLoad = false
    }
  } else {
    id.value = props.value
    if (!props.ignoreLoadByGroupId) {
      //忽略加载
      getFileList()
    } else {
      refreshInitFileList()
    }
  }

}, {immediate: true})

const updateId = (_id) => {
  id.value = _id
}

let realAccepts = computed(() => {
  if (props.picture) {
    return getIntersection(['.jpg', '.png', '.jpeg'], props.accepts)
  }
  return getIntersection(config.acceptFiles, props.accepts)
})

const getFileType = (name) => {
  return getFileExtend(name)
}

const beforeUpload = async (file) => {
  // online模式不限制文件数量，但会替换已有文件
  if (!props.online && fileList.value.length >= props.fileCount) {
    return false;
  }

  for (let f of fileList.value) {
    if(f.name === file.name){
      message.warning({
        title: '文件名重复',
        zIndex: 10000,
        content: '上传失败，已有重复文件名为：' + f.name
      });
      return false;
    }
  }

  let extend = '.' + getFileType(file.name);
  let accept = realAccepts.value;

  if (!oneOf(extend, accept, true)) {
    message.warning({
      title: '选择文件错误',
      zIndex: 10000,
      content: '文件格式错误，请选择' + accept.join('、')
    });
    return false;
  }

  if (file.size / 1024 / 1024 > props.maxSize) {
    message.warning({
      title: '选择文件错误',
      zIndex: 10000,
      content: '文件大小超出限制，请上传' + props.maxSize + 'M以内文件！'
    });
    return false;
  }

  if (['video/mp4', 'video', 'mp4'].includes(file.type)) {
    try {
      // 创建一个 Promise 用于异步获取音频时长
      const {duration, isHorizontal, is1080P} = await getMediaProperties(file);
      // const duration = await getAudioDuration(file);

      if ((props.isDuration && duration > props.maximumDuration * 60)) {
        message.warning({
          title: '视频时长错误',
          zIndex: 10000,
          content: '视频时长超出限制，请上传' + props.maximumDuration + '分钟以内文件！'
        });
        return false; // 时长超出限制，阻止文件上传
      } else if (props.isHorizontal && !isHorizontal) {
        message.warning({
          title: '视频方向错误',
          zIndex: 10000,
          content: '视频方向错误，请上传横屏拍摄的视频文件！'
        });
        return false; // 非横屏拍摄，阻止文件上传
      } else if (props.is1080P && !is1080P) {
        message.warning({
          title: '视频分辨率错误',
          zIndex: 10000,
          content: '视频分辨率错误，请上传分辨率为1080P的视频文件！'
        });
        return false; // 非1080P分辨率，阻止文件上传
      } else {
        return true; // 视频时长、方向和分辨率均符合要求，允许文件上传
      }
    } catch (error) {
      console.error('获取视频时长出错：', error);
      return false; // 获取视频信息失败，阻止文件上传
    }
  }
  if(['image/jpg','jpg', 'image/png', 'png', 'jpeg', 'image/jpeg', 'image'].includes(file.type)){
    try {
      let img = await getImageProperties(file);
      if(props.imgValidate){
        return await props.imgValidate(img);
      }
    } catch (error) {
      console.error('获取图片信息出错：', error);
      return false; // 获取图片信息失败，阻止文件上传
    }
  }
};

const getMediaProperties = (file) => {
  return new Promise((resolve, reject) => {
    let url = URL.createObjectURL(file);
    let mediaElement = document.createElement('video');
    mediaElement.src = url;
    mediaElement.addEventListener("loadedmetadata", () => {
      let duration = parseInt(mediaElement.duration);
      let isHorizontal = mediaElement.videoWidth > mediaElement.videoHeight;
      let is1080P = mediaElement.videoWidth === 1920 && mediaElement.videoHeight === 1080;
      resolve({duration, isHorizontal, is1080P});
    });

    // 在一定时间后如果仍未触发 loadedmetadata 事件，则认为无法获取视频信息
    setTimeout(() => {
      reject(new Error('无法获取视频信息'));
    }, 10000);
  });
};

const getImageProperties = (file) => {
  return new Promise((resolve, reject) => {
    // 使用FileReader读取文件
    const reader = new FileReader();
    reader.onload = function(e) {
      const img = new Image();
      img.src = e.target.result;

      // 图片加载完成后获取宽度和高度
      img.onload = function() {
        resolve(img);
      };
    };
    const blob = file.slice(0, file.size);
    reader.readAsDataURL(blob);
    setTimeout(() => {
      reject(new Error('无法获取图片信息'));
    }, 10000);
  });
};

const download = (file) => {
  if(props.customDownloadFile){
    props.customDownloadFile(file);
    return;
  }
  if (props.previewFile) {
    previewFileByDialog(file);
  } else {
    downLoadFileAction(getFileId(file) + '&oss=' + encodeURIComponent(oss.value))
  }
}
const editImg = (file) => {
  if (!file.url) {
    return
  }
  editFile.value = file;
  editVisiable.value = true;
  editTitle.value = file.name || file.url.substring(file.url.lastIndexOf('/') + 1);
}
const editOnline = (file) => {
  //打开onlyoffice编辑文件
  if (!file.url) {
    return
  }
  const theUrl = getWebUrl()+`#/onlineEditView?fileId=${file.id}&file_name=${file.name}&mode=edit&oss=` + encodeURIComponent(oss.value)
  const windowWidth = window.screen.width;
  const windowHeight = window.screen.height;
  const windowFeatures = `width=${windowWidth},height=${windowHeight},top=0,left=0,location=no,menubar=no,toolbar=no,fullscreen=yes`;
  let childWindow = window.open(theUrl, '_blank', windowFeatures);
  //当 childWindow 页面关闭时，请求后台接口保存文件
  let modal = null;
  let timer = setInterval(() => {
    if (childWindow.closed) {
      clearInterval(timer);
      if(modal){
        modal.destroy()
      }
      modal = Modal.info({
        title: '提示',
        okButtonProps: {style: {display: 'none'}},
        content: '数据处理中...',
      });
      setTimeout(()=>{
        modal.destroy()
      },15000)
      // instance.refs.tableView.saveSuccess()
    }
  }, 1000);
}
const setPreviewVisible = (val) =>{
  previewVisible.value = val
}
const preview = (file) => {
  if (props.customPreview) {
    emits('preview', file)
  } else {
    if (!file.url) {
      return
    }
    //获取previewCurrent（基于过滤后的图片列表索引）
    let fileId = file.id;
    previewCurrent.value = imageFileList.value.findIndex(item => item.id === fileId);
    setPreviewVisible(true);
  }
}
const previewOnline = (file) => {
  //打开onlyoffice查看文件，只读
  if (!file.url) {
    return
  }
  const theUrl = getWebUrl()+`#/onlineEditView?fileId=${file.id}&file_name=${file.name}&mode=view&oss=` + encodeURIComponent(oss.value)
  const windowWidth = window.screen.width;
  const windowHeight = window.screen.height;
  const windowFeatures = `width=${windowWidth},height=${windowHeight},top=0,left=0,location=no,menubar=no,toolbar=no,fullscreen=yes`;
  let childWindow = window.open(theUrl, '_blank', windowFeatures);
}

const setDbData = ref(props.setDbData)

const handleEditCancel = () => {
  editVisiable.value = false;
}
const remove = (file) => {
  return new Promise((resolve, reject) => {
    confirmAction('删除确认', '是否确认删除该附件？', deleteFileUrl, {fileId: getFileId(file)}, () => {

      fileList.value = fileList.value.filter(item => {
        return item.id !== file.id
      })
      emits('fileListChange', fileList.value)

      if (fileList.value.length > 0) {
        emits('update:value', id.value)
        emits('fileChange', id.value, fileList.value)
        formItemContext.onFieldChange()
      } else {
        emits('update:value', '')
        emits('fileChange', id.value, fileList.value)
        formItemContext.onFieldChange()
      }

      if (setDbData.value) {
        if (fileList.value.length === 0) {
          saveDataAction(`${props.formNo}`, props.data)
        }
      }

      resolve()
    }, () => {
      reject()
    }, 'get')
  })
}

let uploadPercent = ref(null)
const customRequest = (upload) => {
  console.log('upload', upload);
  uploadPercent.value = 0
  chunkUpload(upload, (processNumber) => {
    //console.log(processNumber)
    //upload.onProgress({percent: processNumber})
    uploadPercent.value = processNumber
  }, id.value, oss.value, props.idPrefix).then(res => {
    console.log('上传onSuccess', res, upload)
    uploadPercent.value = 100
    
    // online模式：替换已有文件，只保留新上传的文件
    if (props.online && fileList.value.length > 0) {
      // 先删除旧文件（调用后台删除接口）
      const oldFiles = [...fileList.value]
      oldFiles.forEach(oldFile => {
        deleteFileUrl && fetch(baseUrl + deleteFileUrl + '?fileId=' + oldFile.id, { method: 'GET' })
          .catch(err => console.log('删除旧文件失败', err))
      })
      fileList.value = []
    }
    
    let new_sort = fileList.value.length === 0 ? 0 : fileList.value[fileList.value.length - 1].sort + 1
    let new_index = fileList.value.length
    let _file =  formatFileObj(res.data.map.successList[0])
    _file.sort = new_sort
    _file.index = new_index
    _file.name = upload.file.name
    _file.uid = upload.file.id
    fileList.value.push(_file)
    //upload.onSuccess(res, upload)
    if (props.runUpdateValue) {
      emits('update:value', id.value)
    }
    emits('fileListChange', fileList.value)
    formItemContext.onFieldChange()
    emits('fileChange', id.value, fileList.value)
    emits('uploadFinish', id.value, fileList.value)
    uploadPercent.value = null
    firstLoad = true
  }).catch(err => {
    message.warning({
      title: '系统提示',
      zIndex: 10000,
      content: '上传失败'
    })
    //console.log('err', err)
  })
}

const clearFileList = () => {
  firstLoad = true;
  fileList.value = [];
}

const triggerUpload = () => {
  instance.refs.upBtn.$el.click()
}
const handleClickSort = (value, file) => {
  let fileIndex = file.index
  if (value === 1) {
    let midSort = fileList.value[fileIndex - 1].sort
    fileList.value[fileIndex - 1].sort = fileList.value[fileIndex].sort
    fileList.value[fileIndex].sort = midSort
    let midIndex = fileList.value[fileIndex - 1].index
    fileList.value[fileIndex - 1].index = fileList.value[fileIndex].index
    fileList.value[fileIndex].index = midIndex
    let temp = fileList.value[fileIndex - 1]
    fileList.value[fileIndex - 1] = fileList.value[fileIndex]
    fileList.value[fileIndex] = temp
    updateFileSort(fileList.value[fileIndex - 1], fileList.value[fileIndex])
  } else if (value === 2) {
    let midSort = fileList.value[fileIndex + 1].sort
    fileList.value[fileIndex + 1].sort = fileList.value[fileIndex].sort
    fileList.value[fileIndex].sort = midSort
    let midIndex = fileList.value[fileIndex + 1].index
    fileList.value[fileIndex + 1].index = fileList.value[fileIndex].index
    fileList.value[fileIndex].index = midIndex
    let temp = fileList.value[fileIndex + 1]
    fileList.value[fileIndex + 1] = fileList.value[fileIndex]
    fileList.value[fileIndex] = temp
    updateFileSort(fileList.value[fileIndex], fileList.value[fileIndex + 1])
  }
}
const updateFileSort = (file1, file2) => {
  //需要在当前菜单配置app:sys_file_:edit权限才能正常调用此接口
  batchSaveSelectAction('sys_file_', [file1, file2]).then(res => {
    if (res.code === 0) {
      message.success("操作成功")
    } else {
      message.error("操作失败")
    }
  })
}

const handleImageSaveSuccess = (flag) => {
  if (flag) {
    editVisiable.value = false
    fileList.value = []
    emits('editSaveSuccess', true)
    getFileList()
    imgRandomKey.value = Math.random()

  }
}

/**
 * 当使用 minio 后 文件的 地址处理
 * @param file
 * @returns {string}
 */
function getOSSFileUrl(file) {
  return (baseUrl + fileDownloadUrl + getFileId(file)) + '&oss=' + encodeURIComponent(props.oss)
}

defineExpose({
  triggerUpload, updateId, remove, clearFileList, getFiles, formatFileObj, removeById: (id) => {
    fileList.value = fileList.value.filter(item => {
      return item.id !== id
    })
  }
})
</script>
<style scoped>
.img-office {
  background: url(@/assets/img/word.png) no-repeat center center;
  background-size: cover;
}

/* online模式文件列表项样式 */
.online-file-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.online-file-thumb {
  width: 48px;
  height: 48px;
  flex-shrink: 0;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
}

.online-file-name {
  flex-shrink: 0;
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: rgba(0, 0, 0, 0.85);
  font-size: 14px;
}

.online-file-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.online-action-btn {
  color: rgba(0, 0, 0, 0.45);
  padding: 4px 8px;
}

.online-action-btn:hover {
  color: #1890ff;
}

.img-box {
  height: 100px;
  width: 100px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #fff;
  border: 1px solid #d9d9d9;
  box-sizing: border-box;
  position: relative;
}

.upload-img {
  max-width: 80%;
  max-height: 80%;
  height: auto;
  width: auto;
  display: block;
  margin: 0 auto
}

.img-box:before {
  content: '';
  width: 100%;
  display: none;
  position: absolute;
  top: 0;
  height: 100%;
  background-color: #30313390;
}

.img-box:hover:before,
.img-box:hover .icon-container {
  display: flex;
}

.icon-container {
  display: none;
  flex-direction: row;
  position: absolute;
  flex-wrap: wrap;
  width: 80%;
  top: 35%;
  z-index: 999;
  align-items: center;
  justify-content: center;
}

.sort-icon {
  width: 100%;
  display: flex;
  justify-content: space-between;
}

.icon-style {
  width: 33%;
  padding: 0 3px;
  box-sizing: border-box;
  color: #fff;
}
</style>
<style lang="less" scoped>
.full-modal {
  .ant-modal {
    max-width: 100%;
    top: 0;
    padding-bottom: 0;
    margin: 0;
  }

  .ant-modal-content {
    display: flex;
    flex-direction: column;
    height: calc(100vh);
  }

  .ant-modal-body {
    flex: 1;
    overflow: auto;

    .ant-spin-nested-loading, .ant-spin-container, .u-form-container {
      height: 100%;
    }
  }
}

:global(.ant-image-preview-mask){
  z-index: 99999999;
}
:global(.ant-image-preview-wrap) {
  z-index:  100000000 !important;
}
</style>
