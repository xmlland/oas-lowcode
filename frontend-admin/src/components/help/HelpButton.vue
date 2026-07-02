<template>
  <div v-if="visible" class="help-button">
    <a-button title="填报说明" type="link" size="large" @click="showHelp">
      <template #icon>
        <question-circle-outlined/>
      </template>
      填报说明
    </a-button>
  </div>

</template>

<script>
export default {
  name: "HelpButton"
}
</script>
<script setup>

import {useStore} from "vuex";
import {ref} from "vue";
import {postAction} from "@/api/action";

let props = defineProps({
  tableName: {
    type: String,
    default: ''
  },
})

let visible = ref(false)
let id = ref('')
if (props.tableName && props.tableName !== '') {
  postAction('dynamic/zform/datamapView?path=path&traceFlag=&formNo=dsc_ext_help&parentId=', {
    s01: props.tableName,
    pageParam: {
      pageNo: 1,
      pageSize: 1
    },
  }).then(res => {
    if (res.rows.length > 0) {
      if (res.rows[0].help_status === '1') {
        visible.value = true
        id.value = res.rows[0].id
      }
    }
  })
}

let store = useStore();
const showHelp = () => {
  store.commit('setHelpVisible', true)
  store.commit('setHelpContentId', id.value)
}
</script>
<style scoped>
.help-button {
  text-align: right;
}
</style>
