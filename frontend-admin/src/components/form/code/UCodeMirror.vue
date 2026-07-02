<template>
  <div :id="id" ref="editorContainer" :style="monaco?style:''"></div>
</template>

<script>
export default {
  name: "UCodeMirror"
}
</script>
<script setup>
import * as monaco from 'monaco-editor';
import editorWorker from 'monaco-editor/esm/vs/editor/editor.worker?worker'
import jsonWorker from 'monaco-editor/esm/vs/language/json/json.worker?worker'
import tsWorker from 'monaco-editor/esm/vs/language/typescript/ts.worker?worker'

// 配置 Monaco Editor Worker，解决 Vite 环境下 toUrl 报错
if (!self.MonacoEnvironment) {
  self.MonacoEnvironment = {
    getWorker(_, label) {
      if (label === 'json') {
        return new jsonWorker()
      }
      if (label === 'typescript' || label === 'javascript') {
        return new tsWorker()
      }
      return new editorWorker()
    }
  }
}

import {format as sqlFormat} from 'sql-formatter';
import {computed, onMounted, onUnmounted, ref, watch} from "vue";
import {basicSetup} from "codemirror"
import {keymap, EditorView} from "@codemirror/view";
import {espresso} from 'thememirror';
import {indentWithTab} from "@codemirror/commands";
import {javascript} from "@codemirror/lang-javascript"
import {sql} from "@codemirror/lang-sql"
import {EditorState} from '@codemirror/state';
import {isEmpty, isNotEmpty, UUID} from "@/lib/tools";
import {SQL_COMPLETION_CACHE_PREFIX, sqlCompletionItemProvider} from "@/components/form/code/SqlCompletion";

let id = ref('code-editor' + UUID())
let props = defineProps({
  value: {
    type: String,
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false
  },
  lang: {
    type: String,
    default: 'javascript'
  },
  /**
   * 是否使用 monaco 编辑器
   */
  monaco: {
    type: Boolean,
    default: true
  },
  height: {
    type: [Number, String],
    default: 300
  },
  /**
   * sql 元数据 提供代码提示
   */
  sqlDataBaseMetaData: {
    type: Object,
    default: () => {
      return {tables: []}
    }
  }

})

let style = computed(() => {
  return {
    width: '100%',
    height: typeof props.height === 'number' ? (props.height + 'px') : props.height
  }
})

let emits = defineEmits(['update:value', 'change'])
let editorValue = ref(props.value)
let state, view
let triggerUpdate = 0

const editorContainer = ref();
let editor = null;
watch(() => props.value, () => {

  if (props.monaco) {
    editorValue.value = props.value
    if (editor) {
      if (editor.getValue() !== props.value && isNotEmpty(props.value)) {
        editor.setValue(props.value)
      } else if (isEmpty(props.value)) {
        editor.setValue('')
      }
    }
    return;
  }

  if (!view || !view.state) {
    return
  }
  if (triggerUpdate > 0) {
    triggerUpdate--
    return
  }
  editorValue.value = props.value
  const tr = view.state.update({
    changes: {
      from: 0,
      to: view.state.doc.length,
      insert: props.value
    }
  });
  view.dispatch(tr);

}, {immediate: true})

let completionItemProvider = null

watch(() => props.sqlDataBaseMetaData, (val) => {
  if ('sql' === props.lang) {
    if (props.monaco && editor && val.tables) {
      window[SQL_COMPLETION_CACHE_PREFIX + editor._modelData.model.id] = val
    }
  }
}, {immediate: true, deep: true})

const initMonacoEditor = () => {
  if (editor) {
    editor.dispose();
  }
  if (completionItemProvider) {
    completionItemProvider.dispose()
  }
  if (editorContainer.value) {
    editor = monaco.editor.create(editorContainer.value, {
      value: editorValue.value,
      language: props.lang,
      theme: 'vs-dark',
      codeLens: true,
      folding: true,
      snippetSuggestions: 'inline',
      tabCompletion: 'on',
      foldingStrategy: 'auto',
      smoothScrolling: true,
      readOnly: props.disabled,
    });

    if ('sql' === props.lang) {
      window[SQL_COMPLETION_CACHE_PREFIX + editor._modelData.model.id] = props.sqlDataBaseMetaData
      if (!window['sqlCompletionItemProvider']) {
        window.sqlCompletionItemProvider = true
        completionItemProvider = monaco.languages.registerCompletionItemProvider('sql', sqlCompletionItemProvider)
      }
      // 创建自定义菜单项
      editor.addAction({
        id: 'format.sql',
        label: 'Format SQL',
        precondition: null,
        contextMenuGroupId: 'navigation',
        contextMenuOrder: 1,
        run: function () {
          // sql代码格式化
          editor.setValue(sqlFormat(editor.getValue()));
        }
      });
    }

    editor.onDidChangeModelContent(() => {
      if (editorValue.value !== editor.getValue()) {
        editorValue.value = editor.getValue()
        emits('update:value', editor.getValue())
        emits('change', editor.getValue())
      }
    })
  }
}

onMounted(() => {
  const myDiv = document.getElementById(id.value);
  const resizeObserver = new ResizeObserver(entries => {
    for (const entry of entries) {
      const { width, height } = entry.contentRect;
      if (width === 0 || height === 0) {
        return;
      }
      initMonacoEditor()
    }
  });
  resizeObserver.observe(myDiv);

  if (props.monaco) {
    initMonacoEditor()
    return;
  }
  // 创建 EditorState 对象
  let language = props.lang
  let lang = null
  if (language === 'js' || language === 'javascript') {
    lang = javascript()
  }
  if (language === 'sql') {
    lang = sql()
  }
  state = EditorState.create({
    doc: editorValue.value,
    extensions: [basicSetup, lang, espresso, keymap.of(["Mod-z", 'undo'], ["Mod-Shift-Z", 'redo']), keymap.of([indentWithTab])],
  });
  view = new EditorView({
    state,
    parent: document.getElementById(id.value)
  });
  view.dom.addEventListener('keyup', (event) => {
    if (editorValue.value !== view.state.doc.toString()) {
      editorValue.value = view.state.doc.toString()
      triggerUpdate++
      emits('update:value', view.state.doc.toString())
      emits('change', view.state.doc.toString())
    }
  });
});

onUnmounted(() => {
  if (editor) {
    editor.dispose();
  }
  if (completionItemProvider) {
    completionItemProvider.dispose()
  }
});
</script>
<style scoped>

</style>
