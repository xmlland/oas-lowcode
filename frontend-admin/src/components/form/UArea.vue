<template>
  <a-cascader v-if="!isFreeChoice" :allowClear="allowClear"
              :showSearch="showSearch"
              :disabled="disabled"
              :options="options"
              :fieldNames="fieldNames"
              :placeholder="currentPlaceholder"
              :changeOnSelect="changeOnSelect"
              :getPopupContainer="getPopupContainer"
              @change="changeInput"
              v-model:value="currentValue"/>
  <u-tree-select v-else
                 :data="options"
                 :allowClear="allowClear"
                 :showSearch="showSearch"
                 :disabled="disabled"
                 :placeholder="currentPlaceholder"
                 :defaultExpandAll="defaultExpandAll"
                 :container="props.container"
                 @change="changeSelect"
                 v-model:value="currentValue"></u-tree-select>
</template>

<script>
export default {
  name: "UArea"
}
</script>
<script setup>
import {computed, onMounted, ref, watch} from "vue";
import {getAreaDataAction} from "@/api/api";
import config from "@/config"
import {replaceDefaultValue} from "@/lib/tools";
import UTreeSelect from "@/components/form/UTreeSelect";

let props = defineProps({
  value: {
    type: Object,
    default() {
      return {
        id: ''
      }
    }
  },
  disabled: {
    type: Boolean,
    default: false
  },
  allowClear: {
    type: Boolean,
    default: true
  },
  placeholder: {
    type: String,
    default: ''
  },
  showSearch: {
    type: Boolean,
    default: true
  },
  /**
   * 是否自由选择 true:可以自由选择任意区域  false:只能选择到最后一级
   */
  freeChoice: {
    type: [String, Boolean],
    default: false
  },
  //显示到那个等级:1:显示到省,2:显示到市,3:显示到区/县
  showRank: {
    type: [String, Number],
    default: 3
  },
  rootAreaId: {
    type: String,
    default: config.areaId
  },
  changeOnSelect: {
    type: Boolean,
    default: false
  },
  defaultValue: {
    type: String,
    default: null
  },
  defaultExpandAll: {
    type: Boolean,
    default: true
  },
  container:{
    type: String,
    default: ''
  },
  dataFilter: {
    type: Function,
    default: null
  },
})
let currentPlaceholder = ref(props.disabled ? '' : props.placeholder)
let currentValue = ref(null)
let options = ref([])
let fieldNames = ref({label: 'label', value: 'value', children: 'children'})

let areaData = ref({})

let isFreeChoice = computed(() => {
  if (typeof props.freeChoice === 'string') {
    return props.freeChoice === '1' || props.freeChoice === 'true'
  }
  return props.freeChoice
})

const setValue = () => {
  if (props.value !== null && props.value !== undefined && props.value.id) {
    if (isFreeChoice.value) {
      currentValue.value = {id: props.value.id}
    } else {
      currentValue.value = areaData.value[props.value.id];
    }
  } else {
    let defaultValue = replaceDefaultValue(props.defaultValue);
    if (defaultValue) {
      if (isFreeChoice.value) {
        currentValue.value = {id: defaultValue}
      } else {
        currentValue.value = areaData.value[defaultValue];
      }
      if (currentValue.value) {
        let data = {id: defaultValue, name: currentValue.value.name}
        emits('update:value', data)
        emits('change', data)
      }
    } else {
      if (isFreeChoice.value) {
        currentValue.value = null;
      } else {
        currentValue.value = null;
      }

    }
  }
}
watch(() => props.value, () => {
  setValue()
}, {immediate: true})

let emits = defineEmits(['update:value', 'change'])

const changeInput = (value, selectedOptions) => {
  if (!value) {
    emits('update:value', null)
    emits('change', null)
    return
  }
  let data = {id: value[value.length - 1], name: selectedOptions[selectedOptions.length - 1].label}
  emits('update:value', data)
  emits('change', data)
}

const changeSelect = (data) => {
  const value = data && data.id ? data : null
  emits('update:value', value)
  emits('change', value)
}

const listToTree = (data, id, arrData = []) => {
  let result = []
  data.forEach((item) => {
    if (item.parentId === id) {
      let arr = areaData.value[item.id] || JSON.parse(JSON.stringify(arrData))
      arr.push(item.id)
      let res = {
        label: item.name,
        value: item.id
      }
      if (item.hasChildren) {
        res.children = listToTree(data, item.id, JSON.parse(JSON.stringify(arr)))
      }
      result.push(res)
      areaData.value[item.id] = arr
    }

  })
  return result
}

const loadData = () => {
  getAreaDataAction(props.rootAreaId||config.areaId).then(res => {
    let data = res.data.data
    let showData = []
    if(props.dataFilter){
      showData = props.dataFilter(data);
    }else{
      let showRank = parseInt(props.showRank)
      //判断城市等级是否为3
      if (showRank !== 3) {
        //城市等级不为3
        if (showRank === 2) {
          //城市等级为2，帅选出所有市级地区
          data.forEach(item => {
            let id = item.id;
            let name = item.name;
            //根据地区代码筛选出所有的市级以上单位（包括市级），不包括4个直辖市的所有区
            if ((id.endsWith("00") && name !== '县' && name !== '市辖区') || id === "1" ) {
              showData.push(item);
            }
          })
        } else if (showRank === 1) {
          //城市等级为1，帅选出所有省级地区
          data.forEach(item => {
            let id = item.id
            //根据地区代码筛选出所有的省级以上单位（包括省级），包括4个直辖市
            if (id.endsWith("0000") || id === "1") {
              showData.push(item);
            }
          })
        }
      } else {
        //城市等级为3，直接将所有数据显示
        showData = data
      }
    }

    areaData.value = {}
    options.value = listToTree(showData, props.rootAreaId)
    setValue()
  })
}
watch(() => props.rootAreaId, () => {
  loadData()
})

watch(() => props.showRank, () => {
  loadData()
})
const getPopupContainer = () => {
  return props.container === ''?document.body: document.getElementById(props.container)
}

onMounted(() => {
  loadData()
})

</script>
<style scoped>

</style>
