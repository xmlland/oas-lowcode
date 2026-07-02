<template>
  <div class="nav-tab">

    <div :id="id" class="u-nav-container" v-if="tabData.length>1">
      <div v-if="scroll" class="u-nav-button u-nav-left" @click="scrollLeft"
           :class="scroll&&scrollValue>scrollMin?'active':''">
        <u-icon image="left"></u-icon>
      </div>
      <div class="u-nav-tab">
        <div @click="changeActive(index,item.skip)" class="u-nav-tab-item" :class="index===currentIndex?'active':''"
             :style="showStatus?'':'justify-content:center'"
             v-bind:key="index"
             v-for="(item,index) in tabData">
          <div class="text">
            {{ item.text }}
          </div>
          <template v-if="showStatus">
            <div class="status status-skip" v-if="item.skip"><span class="text">不涉及</span></div>
            <div class="status status-done" v-else-if="item.done"><span class="text">已填报</span></div>
            <div class="status status-todo" v-else><span class="text">未填报</span></div>
          </template>

        </div>
      </div>
      <div v-if="scroll" class="u-nav-button u-nav-right" @click="scrollRight"
           :class="scroll&&scrollValue<scrollMax?'active':''">
        <u-icon image="right"></u-icon>
      </div>
    </div>

    <template v-if="data.length>0">
      <a-empty v-if="tabData[currentIndex].skip&&tabData[currentIndex].props.formDisabled" image="/nodata.png"
               :description="'不涉及说明：'+tabData[currentIndex].zskipRemarks"/>
      <component v-else v-bind="tabData[currentIndex].props" :autoHeight="true"
                 :is="tabData[currentIndex].component"></component>
    </template>

  </div>

</template>

<script>
import UIcon from "@/components/icon/UIcon";

export default {
  name: "UNavTab",
  components: {UIcon}
}
</script>
<script setup>
import {nextTick, onMounted, ref, watch, provide} from "vue";
import {UUID} from "@/lib/tools";

let id = ref('nav-tab' + UUID())
let props = defineProps({
  data: {
    type: Array,
    default() {
      return []
    }
  },
  value: {
    type: Number,
    default: 0
  },
  showStatus: {
    type: Boolean,
    default: true
  },
  currentTab: {
    type: Object,
    default() {
      return {};
    }
  }
})
let currentIndex = ref(props.value)

watch(() => props.value, () => {
  currentIndex.value = props.value
}, {immediate: true})
let tabData = ref(props.data)
let emits = defineEmits(['update:currentTab', 'update:value'])
let formRef = ref({})
let tableRef = ref({})
provide('setFormRef', (_formRef) => {
  formRef.value = _formRef
});
provide('setTableRef', (formNo, tableData) => {
  tableRef.value[formNo] = tableData
});
watch(() => [props.data, formRef.value, tableRef.value], () => {
  tabData.value = props.data
  emits('update:currentTab', {
    index: currentIndex.value,
    tab: tabData.value[currentIndex.value],
    formRef: formRef.value,
    tableRef: tableRef.value
  })
}, {immediate: true, deep: true})

const changeActive = (index, skip) => {
  currentIndex.value = index
  emits('update:value', currentIndex.value)
  emits('update:currentTab', {
    index: currentIndex.value,
    tab: tabData.value[currentIndex.value],
    formRef: formRef.value,
    tableRef: tableRef.value
  })
}

const totalWidth = () => {
  if (document.getElementById(id.value)) {
    return document.getElementById(id.value).querySelector('.u-nav-tab').clientWidth
  }
  return 0
}
const childrenWidth = () => {
  if (!document.getElementById(id.value)) {
    return 0
  }
  let navTab = document.getElementById(id.value).querySelector('.u-nav-tab')
  let tabItems = navTab.querySelectorAll('.u-nav-tab-item')
  let useWidth = 0
  tabItems.forEach(item => {
    useWidth += item.clientWidth
  })
  return useWidth
}
let scroll = ref(false)
let scrollValue = ref(0)
let scrollMin = ref(0)
let scrollMax = ref(0)
onMounted(() => {
  if (childrenWidth() > totalWidth()) {
    scroll.value = true
    scrollMax.value = childrenWidth() - totalWidth() + 48
  }
})

watch(() => props.data, () => {

  nextTick(() => {
    scroll.value = false
    if (childrenWidth() > totalWidth()) {
      scroll.value = true
      scrollMax.value = childrenWidth() - totalWidth() + 48
    }
  })

}, {immediate: true, deep: true})

const scrollLeft = () => {
  if (!scroll.value) {
    return
  }
  if (scrollValue.value > 100) {
    scrollValue.value -= 100
  } else {
    scrollValue.value = 0
  }
  document.getElementById(id.value).querySelector('.u-nav-tab').scrollLeft = scrollValue.value
}
const scrollRight = () => {
  if (!scroll.value) {
    return
  }
  if (scrollValue.value < scrollMax.value - 100) {
    scrollValue.value += 100
  } else {
    scrollValue.value = scrollMax.value
  }

  document.getElementById(id.value).querySelector('.u-nav-tab').scrollLeft = scrollValue.value
}

</script>
<style lang="less" scoped>
@import "@/assets/less/main.less";
.u-nav-container {
  display: flex;
  flex-direction: row;
  align-items: center;
  width: 100%;
  padding: 12px 0;

  .u-nav-button {
    display: flex;
    width: 40px;
    height: 40px;
    background: #FAFBFD;
    border: 0.6px solid #D5D5D5;
    border-radius: 10px;
    /*display: flex;*/
    flex-direction: row;
    align-items: center;
    justify-content: center;

    opacity: .3;
    cursor: not-allowed;

  }
  .u-nav-left{
    margin-right: 10px;
  }
  .u-nav-right{
    margin-left: 10px;
  }
  .u-nav-button.active {
    opacity: 1;
    background: #ffffff;
    cursor: pointer;
  }

  .u-nav-button.active:hover {
    border-color: #D4E9C6;
  }

  .u-nav-tab::-webkit-scrollbar {
    width: 0;
    height: 0;
  }

  .u-nav-tab {
    display: flex;
    /*padding: 0 24px;*/
    flex: 1;
    overflow: auto;

    .u-nav-tab-item {
      cursor: pointer;
      height: 36px;
      position: relative;
      padding: 0 22px;
      display: flex;
      flex-direction: row;
      flex-wrap: nowrap;
      align-items: center;
      justify-content: space-between;
      white-space: nowrap;

      .text {
        font-family: @main-font-name;
        font-style: normal;
        font-weight: 700;
        font-size: @font-size-base;
        line-height: 14px;
        /* identical to box height, or 100% */
        color: #555555;
      }

      .status {
        margin-left: 10px;
        height: 16px;
        background: #ccc;
        display: flex;
        flex-direction: row;
        align-items: center;
        border-radius: 10px;
        justify-content: center;
        padding: 0 10px;

        .text {
          font-family: 'Noto Sans SC Thin';
          font-style: normal;
          font-weight: 700;
          font-size: 12px;
          text-align: center;
          color: #FFFFFF;

        }

      }

      .status-done {
        background: #54C09A;
      }

      .status-skip {
        background: #939393;
      }

      .status-todo {
        background: #ff8400;
      }
    }

    .u-nav-tab-item.active {
      background: #d9e6e2;
      border-radius: 10px;
    }

    .u-nav-tab-item::before {
      position: absolute;
      content: '';
      left: 10px;
      top: 6px;
      width: 4px;
      height: 24px;
      background: var(--ant-primary-color);
      border-radius: 12px;
    }
  }
}
</style>
