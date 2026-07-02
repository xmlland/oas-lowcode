<template>
  <u-form v-model:value="formModel" v-model:disabled="disabled" :labelWidth="70">
    <a-row>
      <a-col :span="24" v-if="formModel.is_common!=='1'">
        <a-form-item name="" label="状态">
          <a-tag v-if="formModel.is_reply === '1'" color="#87d068">已回复</a-tag>
          <a-tag v-else color="#f50">未回复</a-tag>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="module_" label="模块">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.module_" placeholder="请输入"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="title_" label="标题">
          <u-input :disabled="disabled" defaultValue="" v-model:value="formModel.title_" placeholder="请输入"/>
        </a-form-item>
      </a-col>
      <a-col :span="24" v-if="formModel.is_common!=='1'">
        <a-form-item name="desc_" label="描述">
          <u-input :textarea="true" :disabled="disabled"
                   :autoSize="{minRows: 3, maxRows: 3}"
                   defaultValue=""
                   v-model:value="formModel.desc_" placeholder="请输入" :maxlength="2000"/>
        </a-form-item>
      </a-col>
      <a-col :span="24" v-if="formModel.file_pic">
        <a-form-item name="file_pic" label="图片">
          <u-upload :picture="true" :is-edit="true" :disabled="disabled" v-model:value="formModel.file_pic" :fileCount="5" :maxSize="5"/>
        </a-form-item>
      </a-col>
      <a-col :span="24" v-if="formModel.is_common!=='1'">
        <a-form-item name="submit_time" label="提交时间">
          <u-date :disabled="disabled" defaultValue="" v-model:value="formModel.submit_time" formatPatter="yyyy-MM-dd HH:mm:ss"/>
        </a-form-item>
      </a-col>
      <template v-if="formModel.is_reply==='1' || formModel.is_common==='1'">
        <a-col :span="24">
          <a-form-item name="reply_time" :label="formModel.is_common==='1'?'时间':'回复时间'">
            <u-date :disabled="disabled" defaultValue="" v-model:value="formModel.reply_time" formatPatter="yyyy-MM-dd HH:mm:ss"/>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item name="reply_content" label="">
            <u-tinymce style="width: 400px" :height="height" :disabled="disabled" v-model:value="formModel.reply_content" placeholder="请输入回复内容"/>
          </a-form-item>
        </a-col>
      </template>

    </a-row>
  </u-form>
</template>

<script>
export default {
  name: "FeedBackDetailForm"
}
</script>
<script setup>
import {ref, computed} from "vue";
import UForm from "@/components/form/UForm";
import UInput from "@/components/form/UInput";
import UUpload from "@/components/form/UUpload";
import UDate from "@/components/form/UDate";
import UTinymce from "@/components/form/UTinymce";

let props = defineProps({
  item: {
    type: Object,
    default: () => {
      return {}
    }
  }
})
let formModel = ref(props.item)
let disabled = ref(true)

let height = computed(() => {
  if (formModel.value.is_common === '1') {
    return document.documentElement.clientHeight - 274
  } else {
    return document.documentElement.clientHeight - 438
  }
})

</script>
<style scoped>

</style>
