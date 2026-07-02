<template>
  <div style="display: flex;flex-direction: row;justify-content: space-between;">
    <a-badge :color="color"/>
    <div style="text-align: left;flex: 1;"><a :href="aHref" @click="clickA">{{title}}</a></div>
    <span style="opacity: 0.65;font-size: 0.9rem;">
      {{time}}&nbsp;
      <a @click="delButtonClicked" v-if="showDelButton"><delete-outlined /></a>
      <span style="visibility:hidden" v-else>
        <delete-outlined />
      </span>
      <template v-if="$slots.extText">
        <slot name="extText"></slot>
      </template>
    </span>
  </div>
  <u-modal ref="modal" :width="800"  @clickOk="readOK" :customOK="true">
    <div style="white-space: pre-line" v-if="!toHtml">
      {{content}}
    </div>
    <div style="white-space: pre-line" v-if="toHtml" v-html="content"></div>
  </u-modal>
</template>

<script>
import UModal from "@/components/modal/UModal";
export default {
  name: "UMessageLink",
  components: {UModal}
}
</script>
<script setup>
import {computed, getCurrentInstance} from "vue";

let emits = defineEmits(['onClickA','onOpenModal','modalClickOk','clickDel'])

const instance = getCurrentInstance();

const props = defineProps({
  title:{
    type: String,
    default: ''
  },
  time:{
    type: String,
    default:''
  },
  color:{
    type: String,
    default: 'default'
  },
  href:{
    type: String,
    default: null
  },
  content: {
    type: String,
    default: ''
  },
  toHtml:{
    type: Boolean,
    default: false
  },
  item:{
    type: Object,
    default: null
  },
  showDelButton:{
    type:Boolean,
    default:false
  },
  preventClickOpenModal:{
    type:Boolean,
    default: false
  }
})

let aHref = computed(()=>{
  if((!props.href) || (props.href === '')){
    return undefined
  }else {
    return props.href;
  }
})

const readOK = () =>{
  emits('modalClickOk',props.item);
  closeModal()
}

const clickA = () =>{
  if(!aHref.value){
    emits('onClickA',props.item);
    if(!props.preventClickOpenModal){
      instance.refs.modal.open(props.title);
      emits('onOpenModal');
    }
  }
}

const delButtonClicked = ()=>{
  emits('clickDel',props.item);
}

const closeModal = () =>{
  instance.refs.modal.close();
}

const openModal = () =>{
  clickA();
}

defineExpose({
  openModal,closeModal
})

</script>

<style scoped>

</style>
