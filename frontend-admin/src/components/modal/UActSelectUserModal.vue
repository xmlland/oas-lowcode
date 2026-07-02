<template>
  <u-modal @afterClose="afterClose" @onCancel="afterClose" ref="modal" :width="800" :formDisabled="true" :customOK="true" @clickOk="clickOk">
    <a-input v-if="showUserList.length > 10"
        v-model:value="search"
        :allowClear="true"
        placeholder="输入关键词进行搜索"
        style="width: 100%"
    />
    <div style="margin-top: 20px">
      <a-form ref="form" :model="localFormModel" :labelCol="{ style: { width: labelWidth+'px' } }">
        <a-form-item name="tempLoginName" v-show="showUserList.length>1&&userListVisible"  label="办理人" :rules="[{ required: userList.length>0, message: '请选择'}]">
          <template v-if="multiUser">
            <a-checkbox-group v-model:value="localFormModel.tempLoginName" style="width: 100%">
              <a-row :gutter="[16,16]">
                <a-col v-bind:key="index" v-for="(user,index) in showUserList" :span="12">
                  <a-checkbox :value="user.loginName">{{ user.name.replace(/&emsp\;/g, ' ') }}</a-checkbox>
                </a-col>
              </a-row>
            </a-checkbox-group>
          </template>
          <template v-else>
            <a-radio-group v-model:value="localFormModel.tempLoginName" style="width: 100%" name="radioGroup">
              <a-row :gutter="[16,16]">
                <a-col v-bind:key="index" v-for="(user,index) in showUserList" :span="12">
                  <a-radio :value="user.loginName">{{ user.name.replace(/&emsp\;/g, ' ') }}</a-radio>
                </a-col>
              </a-row>
            </a-radio-group>
          </template>
        </a-form-item>
        <a-form-item v-if="isFormItemShow(actRuleArgs,'act.comment')" name="actComment" :label="commentLabel"
                     :rules="[{ required: isFormItemRequire(actRuleArgs,'act.comment'), message: '请输入'+commentLabel }]">
          <u-input :disabled="isFormItemDisabled(actRuleArgs,'act.comment')" :maxlength="500" :textarea="true" v-model:value="localFormModel.actComment" :placeholder="'请输入'+commentLabel"/>
        </a-form-item>
        <component v-if="customFormComponent" ref="extendForm" :is="customFormComponent" :actRuleArgs="actRuleArgs" :rowData="rowData"/>
      </a-form>
    </div>

  </u-modal>
</template>

<script>
export default {
  name: "UActSelectUserModal"
}
</script>
<script setup>
import {isFormItemShow, isFormItemDisabled, isFormItemRequire} from "@/lib/act/actForm";
import {getCurrentInstance, ref, computed} from "vue";
let emits = defineEmits(['afterCloseOrOnCancel', ])

const form = ref();
let currentResolve = null
let instance = getCurrentInstance();

let localFormModel = ref({})

let search = ref('')
let userList = ref([])
let showUserList = computed(() => {
  return userList.value.filter(item => item.name.indexOf(search.value) >= 0)
})
let multiUser = ref(false)

let hasCommentSlot = ref(true)
let commentLabel = ref('')
let actRuleArgs = ref({})
let rowData = ref({})
let customFormComponent = ref(null)
let labelWidth  = ref(100)
let userListVisible = ref(true)
/**
 * 打开选择用户弹窗
 * @param data 用户列表返回数据
 * @param multi  是否多选
 * @param hasSlot 是否有评论插槽
 * @param actCommentLabel 评论文本框标签
 * @param _actRuleArgs 工作流规则变量
 * @param _rowData 行数据
 * @returns {Promise<unknown>}
 */
const open = (data, multi, {hasSlot, actCommentLabel}, _actRuleArgs, _rowData) => {
  if (data.nextNode){
    instance.refs.modal.open(`提交  →  【${data.nextNode.name}】`)
  }else{
    instance.refs.modal.open('提交')
  }
  localFormModel.value = {}
  userList.value = data.userList || []
  multiUser.value = multi
  hasCommentSlot.value = hasSlot
  commentLabel.value = actCommentLabel
  actRuleArgs.value = _actRuleArgs
  rowData.value = _rowData
  customFormComponent.value = false
  labelWidth.value = 100
  if (_actRuleArgs.formExtend){
    if (_actRuleArgs.formExtend['act.submit.form']){
      let value = _actRuleArgs.formExtend['act.submit.form']
      if (value){
        import(`@/views/${value}.vue`).then(module => {
          customFormComponent.value = module.default
        })
      }
    }
    if (_actRuleArgs.formExtend['act.userList']){
      userListVisible.value = isFormItemShow(_actRuleArgs,'act.userList')
    }
    labelWidth.value = _actRuleArgs.formExtend['act.form.labelWidth'] || 100
    commentLabel.value = _actRuleArgs.formExtend['act.comment']

  }

  if (multi) {
    localFormModel.value.tempLoginName = userList.value.map(item => item.loginName)
  } else if (userList.value.length === 1) {
    localFormModel.value.tempLoginName = userList.value[0].loginName
  }
  return new Promise(resolve => {
    currentResolve = resolve;
  })
}
const clickOk = () => {
  let promiseArr = [form.value.validateFields()]
  let extendForm = instance.refs.extendForm;
  if (extendForm){
    promiseArr.push(extendForm.validateFields())
  }
  Promise.all(promiseArr).then(resArr => {
    let res = resArr[0]
    if (!multiUser.value) {
      res.tempLoginName = [res.tempLoginName]
    }
    if (resArr.length>1){
      for (let i = 1; i < resArr.length; i++) {
        Object.assign(res,resArr[i])
      }
    }
    currentResolve(res)
    instance.refs.modal.close()
  })

}

function afterClose() {
  emits('afterCloseOrOnCancel')
}
defineExpose({
  open
})
</script>
<style scoped>

</style>
