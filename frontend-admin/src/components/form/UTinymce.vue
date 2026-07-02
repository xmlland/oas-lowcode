<template>
  <div class="u-tinymce">
    <editor
      v-model="currentValue"
      :disabled="disabled"
      :license-key="licenseKey"
      :init="initConfig"
    />
  </div>
</template>

<script>
import 'tinymce/tinymce'
import Editor from '@tinymce/tinymce-vue'

import 'tinymce/models/dom'
import 'tinymce/themes/silver'
import 'tinymce/icons/default'

import 'tinymce/plugins/image'
import 'tinymce/plugins/media'
import 'tinymce/plugins/table'
import 'tinymce/plugins/lists'
import 'tinymce/plugins/wordcount'
import 'tinymce/plugins/link'
import 'tinymce/plugins/code'
import 'tinymce/plugins/preview'
import 'tinymce/plugins/fullscreen'
import 'tinymce/plugins/help'

export default {
  name: 'UTinymce',
  components: {
    Editor
  }
}
</script>

<script setup>
import {ref, watch} from 'vue'
import {message} from 'ant-design-vue'
import chunkUpload from '@/api/chunkUpload'
import {UUID} from '@/lib/tools'
import {fileDownloadUrl, getFileListAction} from '@/api/api'
import {baseUrl} from '@/api/action'

const props = defineProps({
  value: {
    type: String,
    default: null
  },
  disabled: {
    type: Boolean,
    default: false
  },
  licenseKey: {
    type: String,
    default: 'gpl'
  },
  height: {
    type: Number,
    default: 420
  },
  oss: {
    type: String,
    default: 'minio:main:userfiles'
  },
  init: {
    type: Object,
    default() {
      return {
        base_url: 'tinymce',
        language: 'zh_CN',
        skin_url: 'tinymce/skins/ui/oxide',
        convert_urls: false,
        plugins: 'link lists image code table wordcount media preview fullscreen help',
        toolbar: 'bold italic underline strikethrough | fontfamily fontsize | forecolor backcolor | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent blockquote | undo redo | link unlink code lists table image media | removeformat | fullscreen preview',
        statusbar: true,
        font_size_formats: '12px 14px 16px 18px 20px 22px 24px 28px 32px 36px 48px 56px 72px',
        font_family_formats: '微软雅黑=Microsoft YaHei,Helvetica Neue,PingFang SC,sans-serif;苹果苹方=PingFang SC,Microsoft YaHei,sans-serif;宋体=simsun,serif;仿宋=FangSong,serif;黑体=SimHei,sans-serif;Arial=arial,helvetica,sans-serif;Arial Black=arial black,avant garde;Book Antiqua=book antiqua,palatino;',
        lineheight_formats: '0.5 0.8 1 1.2 1.5 1.75 2 2.5 3 4 5',
        branding: false,
        paste_data_images: true,
        automatic_uploads: true
      }
    }
  }
})

const emits = defineEmits(['update:value'])

const appendOssToFileUrl = (url) => {
  if (!url || !props.oss || url.includes('oss=') || !url.includes(fileDownloadUrl)) {
    return url
  }
  return `${url}${url.includes('?') ? '&' : '?'}oss=${encodeURIComponent(props.oss)}`
}

const formatTinyMceFileUrl = (fileId) => {
  return appendOssToFileUrl(baseUrl + fileDownloadUrl + fileId)
}

const normalizeTinyMceContent = (html) => {
  if (!html || !props.oss || !html.includes(fileDownloadUrl)) {
    return html
  }

  try {
    const doc = new DOMParser().parseFromString(`<div>${html}</div>`, 'text/html')
    const wrapper = doc.body.firstElementChild
    if (!wrapper) {
      return html
    }

    wrapper.querySelectorAll('img[src]').forEach(img => {
      img.setAttribute('src', appendOssToFileUrl(img.getAttribute('src')))
    })
    return wrapper.innerHTML
  } catch (e) {
    return html
  }
}

const createImagesUploadHandler = () => {
  return (blobInfo, progress) => {
    return new Promise((resolve, reject) => {
      if (blobInfo.blob().size / 1024 / 1024 > 10) {
        message.warning({
          title: '选择文件错误',
          zIndex: 10000,
          content: '上传失败，图片大小请控制在 10M 以内'
        })
        reject({remove: true})
        return
      }

      const uuid = 'tinymce' + UUID()
      chunkUpload({
        file: new File([blobInfo.blob()], blobInfo.filename(), {
          type: blobInfo.blob().type || 'image/png',
        })
      }, (processNumber) => {
        progress(processNumber)
      }, uuid, props.oss).then(() => {
        getFileListAction(uuid).then(fileRes => {
          const files = fileRes.data.fileListMap.files
          if (files.length > 0) {
            resolve(formatTinyMceFileUrl(files[0].id))
          } else {
            reject({remove: true})
          }
        })
      }).catch(err => {
        console.log('err', err)
        reject({remove: true})
      })
    })
  }
}

const initConfig = ref({
  ...props.init,
  images_upload_handler: createImagesUploadHandler(),
  height: props.height
})

if (props.disabled) {
  initConfig.value.statusbar = false
  initConfig.value.menubar = false
  initConfig.value.plugins = ''
  initConfig.value.toolbar = ''
}

const currentValue = ref(null)

watch(() => props.value, () => {
  currentValue.value = props.value ? normalizeTinyMceContent(props.value) : ''
}, {immediate: true})

watch(() => currentValue.value, () => {
  emits('update:value', currentValue.value)
}, {immediate: true})
</script>

<style lang="less">
</style>
