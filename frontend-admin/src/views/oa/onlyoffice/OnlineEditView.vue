<template>
  <div style="width: 100%;height: 100%;position:relative;">
    <DocumentEditor
        v-if="apiLoaded"
        id="docEditor"
        :documentServerUrl="resolvedDocServerUrl"
        :config="config"
        :events_onDocumentReady="onDocumentReady"
        :onLoadComponentError="onLoadComponentError"
    />
    <div v-if="!apiLoaded" style="display:flex;align-items:center;justify-content:center;height:100%;font-size:16px;color:#999;">
      正在加载文档编辑器...
    </div>
  </div>
</template>
<script>
export default {
  name: 'online_edit_view'
}
</script>
<script setup>
import {DocumentEditor} from "@onlyoffice/document-editor-vue";
import {onMounted, ref} from "vue";
import {useRoute} from 'vue-router'

const route = useRoute()
const apiLoaded = ref(false);
const resolvedDocServerUrl = ref('');
const config = ref({});

const fileTypeMap = {
  'doc': { documentType: 'word', fileType: 'doc' },
  'docx': { documentType: 'word', fileType: 'docx' },
  'rtf': { documentType: 'word', fileType: 'rtf' },
  'txt': { documentType: 'word', fileType: 'txt' },
  'xls': { documentType: 'cell', fileType: 'xls' },
  'xlsx': { documentType: 'cell', fileType: 'xlsx' },
  'ppt': { documentType: 'slide', fileType: 'ppt' },
  'pptx': { documentType: 'slide', fileType: 'pptx' },
  'pdf': { documentType: 'pdf', fileType: 'pdf' },
}

const onDocumentReady = () => {}

const onLoadComponentError = (errorCode, errorDescription) => {
  switch (errorCode) {
    case -1:
      console.warn(errorDescription);
      break;
    case -2:
      console.warn(errorDescription);
      break;
    case -3:
      console.warn(errorDescription);
      break;
  }
}

onMounted(() => {
  // 在 onMounted 中读取 window 全局变量，确保 main.js 的 __RUNTIME_CONFIG__ 已执行
  const documentServerUrl = window['documentServerUrl'];
  const serverUrl = window['serverUrl'];
  const onlyofficeInnerUrl = window['onlyofficeInnerUrl'];

  // 1. 解析 OnlyOffice 编辑器 URL（浏览器直连 OnlyOffice 服务器）
  let docServerUrl = documentServerUrl;
  if (!docServerUrl) {
    const loc = window.location;
    docServerUrl = loc.protocol + '//' + loc.hostname + ':6105/';
  } else if (!docServerUrl.startsWith('http')) {
    const loc = window.location;
    docServerUrl = loc.protocol + '//' + loc.host + docServerUrl;
  }
  if (!docServerUrl.endsWith('/')) docServerUrl += '/';
  resolvedDocServerUrl.value = docServerUrl;

  // 2. 解析 OnlyOffice 服务端请求 URL（文件下载、保存回调）
  //    优先使用 Docker 内部地址（避免私有 IP 被 OnlyOffice 拒绝），回退到外部地址
  let srvUrl;
  if (onlyofficeInnerUrl) {
    srvUrl = onlyofficeInnerUrl;
  } else {
    srvUrl = serverUrl || '/jeeStudio/gtoa/a/';
    if (srvUrl && !srvUrl.startsWith('http')) {
      const loc = window.location;
      srvUrl = loc.protocol + '//' + loc.host + srvUrl;
    }
  }

  // 3. 构造编辑器配置
  const userId = route.query.userId;
  const userName = route.query.userName;
  const fileId = route.query.fileId;
  const fileName = route.query.file_name;
  const oss = encodeURIComponent(route.query.oss);
  const mode = route.query.mode;
  const t = new Date().getTime();

  const ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
  const typeInfo = fileTypeMap[ext] || { documentType: 'word', fileType: ext };
  const isPdf = ext === 'pdf';
  const effectiveMode = isPdf ? 'view' : mode;

  config.value = {
    "document": {
      "fileType": typeInfo.fileType,
      "key": fileId + "_" + t,
      "title": fileName,
      "url": `${srvUrl}system/sysFile/fileDownload?fileId=${fileId}&oss=${oss}&t=${t}`,
      "permissions": {
        "edit": effectiveMode !== 'view',
        "download": true,
        "print": true,
        "copy": true,
        "comment": effectiveMode !== 'view'
      }
    },
    "documentType": typeInfo.documentType,
    "editorConfig": {
      "callbackUrl": srvUrl + "onlyoffice/save",
      "lang": "zh-CN",
      "mode": effectiveMode,
      "user": {
        "id": userId,
        "name": userName
      },
      "customization": {
        "forcesave": true,
        "help": false,
        "plugins": false,
        "review": {
          "showReviewChanges": false,
          "trackChanges": false
        }
      },
      "collaboration": {
        "mode": "fast",
        "change": false
      }
    },
  };

  // 4. 预加载 OnlyOffice api.js，绕过 DocumentEditor 组件内部加载机制的兼容性问题
  if (window.DocsAPI) {
    apiLoaded.value = true;
    return;
  }
  const script = document.createElement('script');
  script.src = docServerUrl + 'web-apps/apps/api/documents/api.js';
  script.onload = () => { apiLoaded.value = true; };
  script.onerror = (e) => { console.error('Failed to load OnlyOffice api.js:', e); };
  document.head.appendChild(script);
})
</script>
<style scoped>
</style>
