<template>
  <div class="u-tree" :class="showSearch?'u-tree-search':''">
    <template v-if="$slots.customTreeHeader">
      <div>
        <slot name="customTreeHeader"></slot>
      </div>
    </template>
    <a-input-search v-if="showSearch" v-model:value="searchValue" placeholder="输入关键词搜索"/>
    <a-tree v-if="treeShowData.length"
            :show-line="false"
            :show-icon="showIcon"
            :checkable="checkable"
            :selectable="selectable"
            :defaultExpandAll="defaultExpandAll"
            :tree-data="treeShowData"
            @select="onSelect"
            @check="onCheck"
            v-model:expandedKeys="currentExpandedKeys"
            v-model:selectedKeys="key"
            v-model:checkedKeys="currentCheckedKeys"
            :checkStrictly="checkStrictly"
            :max="5"
    >
      <template v-if="$slots.title" #title="item">
        <slot name="title" :item="item"></slot>
      </template>

      <template v-else #title="{ title , data}">
        <span v-if="title.indexOf(searchValue) > -1">
          {{ title.substr(0, title.indexOf(searchValue)) }}
          <span style="color: #f50">{{ searchValue }}</span>
          {{ title.substr(title.indexOf(searchValue) + searchValue.length) }}
        </span>
        <span v-else>{{ title }}</span>

        <slot name="titleCustom" :title="title" :data="data">

        </slot>

      </template>

      <template #icon="{ key, selected,active, checked, expanded, loading,  halfChecked, title, children, dataRef, data, defaultIcon, switcherCls, disabled }">
        <slot name="icon" :key="key" :selected="selected" :active="active" :checked="checked" :expanded="expanded" :loading="loading" :halfChecked="halfChecked" :title="title" :children="children" :dataRef="dataRef" :data="data" :defaultIcon="defaultIcon" :switcherCls="switcherCls" :disabled="disabled" />
      </template>

    </a-tree>
  </div>

</template>

<script>
export default {
  name: "UTree"
}
</script>
<script setup>
import {ref, watch, computed} from "vue";
import {postAction} from "@/api/action";
import {getData, listToTree} from "@/lib/tools";

let props = defineProps({
  data: {
    type: Array,
    default() {
      return []
    }
  },
  showIcon: {
    type: Boolean,
    default: false,
  },
  defaultExpandAll: {
    type: Boolean,
    default: true,
  },
  checkable: {
    type: Boolean,
    default: false,
  },
  selectable: {
    type: Boolean,
    default: true,
  },
  defaultActiveIndex: {
    type: Number,
    default: 0
  },
  formDisabled: {
    type: Boolean,
    default: false
  },
  selectedKeys: {
    type: Array,
    default() {
      return []
    }
  },
  checkedKeys: {
    type: [Array, Object],
    default() {
      return []
    }
  },
  expandedKeys: {
    type: Array,
    default() {
      return []
    }
  },
  checkStrictly: {
    type: Boolean,
    default: false
  },
  url: {
    type: String,
    default: null
  },
  postData: {
    type: Object,
    default() {
      return {}
    }
  },
  responseData: {
    type: Array,
    default() {
      return ['rows']
    }
  },
  action: {
    type: Promise,
    default: null
  },
  transform: {
    type: Object,
    default() {
      return {
        title: 'name',
        key: 'id',
        done: 'id',
        parent: ['parent', 'id']
      };
    }
  },
  dataFilter: {
    type: Function,
    default: null
  },
  showSearch: {
    type: Boolean,
    default: false
  }
})
let searchValue = ref('');
let key = ref(props.selectedKeys)
let currentCheckedKeys = ref(props.checkedKeys)
let currentExpandedKeys = ref(props.expandedKeys)
watch(() => props.expandedKeys, () => {
  currentExpandedKeys.value = props.expandedKeys
})
let treeData = ref(props.data)
const filter = (data) => {
  let arr = []
  data.forEach(row => {
    let newRow = {}
    Object.assign(newRow, row)
    if (newRow.children && newRow.children.length > 0) {
      let children = filter(newRow.children)
      if (children.length > 0) {
        newRow.children = children
        arr.push(newRow);
      } else if (newRow.title.indexOf(searchValue.value) >= 0) {
        delete newRow.children
        arr.push(newRow);
      }
    } else {
      if (newRow.title.indexOf(searchValue.value) >= 0) {
        arr.push(newRow);
      }
    }
  })
  return arr
}
let treeShowData = computed(() => {
  if (searchValue.value) {
    return filter(treeData.value)
  }
  return treeData.value;
})

watch(() => props.selectedKeys, () => {
  key.value = props.selectedKeys
})
watch(() => props.checkedKeys, () => {
  currentCheckedKeys.value = props.checkedKeys
})
const expendKeys = (data) => {
  let arr = []
  data.forEach(item => {
    if (item.children) {
      arr = arr.concat(expendKeys(item.children))
    } else {
      arr.push(getData(item, 'key'))
    }
  })
  return arr
}
watch(() => props.data, () => {
  if (props.defaultExpandAll) {
    currentExpandedKeys.value = expendKeys(props.data)
  }
  treeData.value = props.data
}, {immediate: true})
watch(() => props.url, () => {
  loadDataByUrl()
})
watch(() => props.postData, (o, n) => {
  if (JSON.stringify(o) !== JSON.stringify(n)) {
    loadDataByUrl()
  }
},{deep: true})
watch(() => props.action, () => {
  loadDataByAction()
})
let first = true
const loadDataByUrl = () => {
  if (props.url && props.url !== '') {
    postAction(props.url, props.postData).then(async res => {
      emits('loadSuccess', res)
      let resut = res;
      let rrows = res.rows;
      //如果是查看状态 不显示第一项:填报注意事项
      if (props.formDisabled) {
        resut.rows = [];
        for (let i = 0; i < rrows.length; i++) {
          let row = rrows[i];
          let name = row.name;
          if (name !== '填报注意事项') {
            resut.rows.push(row);
          }
        }
      }
      let responseData = getData(resut, props.responseData);
      if (props.defaultExpandAll) {

        currentExpandedKeys.value = responseData.map(item => {
          return getData(item, props.transform.key)
        })
      }
      let arr = getData(resut, props.responseData)
      let rootId = '0';

      let rootArr = arr.filter(item=>getData(item, props.transform.parent)=='0')
      if (rootArr.length === 0) {
        //所有机构的id数据
        let orgIds = arr.filter(item=>item.hasChildren).map(item=>getData(item, props.transform.parent));
        if (orgIds.length === 0 && arr.length > 0) {
          rootId = getData(arr[0], props.transform.parent)
        }else{
          rootId = orgIds[0]
        }
      }

      let data = listToTree(getData(resut, props.responseData), rootId, props.transform)
      if (props.dataFilter) {
        data = await props.dataFilter(data)
      }
      treeData.value = data || []; //修复 当 列表为空的 时候 a-tree 的 tree-data 不更新的 情况
      if (data.length > 0) {
        treeData.value = data;
        if (props.defaultActiveIndex != null && first) {
          first = false
          key.value = [treeData.value[props.defaultActiveIndex].key]
          emits('select', [treeData.value[props.defaultActiveIndex].key], {node: treeData.value[props.defaultActiveIndex]})
        }
      }
    })
  }
}
loadDataByUrl()

const loadDataByAction = () => {
  if (props.action) {
    props.action().then(res => {
      treeData.value = res
    })
  }
}
loadDataByAction()

let emits = defineEmits(['select', 'check', 'update:selectedKeys', 'update:checkedKeys','loadSuccess'])
const onSelect = (selectedKeys, info) => {
  emits('select', selectedKeys, info)
};
const onCheck = (checkedKeys, {checked, checkedNodes, node, event}) => {
  emits('check', checkedKeys, {checked, checkedNodes, node, event})
  emits('update:checkedKeys', currentCheckedKeys.value)
}
const selectAllKey = () => {
  let checkedKeys = getAllKeys( treeShowData.value );
  emits('update:checkedKeys', checkedKeys)
}
const getAllKeys = (data) => {
  let keys = [];
  data.forEach(item => {
    keys.push( item.key );
    if (item.children) {
      keys = keys.concat( getAllKeys(item.children));
    }
  });
  return keys;
}
defineExpose({
  loadDataByUrl,selectAllKey,getAllKeys
})
</script>
<style lang="less" scoped>
@import "@/assets/less/main.less";
.u-tree {
  width: 100%;
  height: 100%;
  overflow: auto;
  box-sizing: border-box;
  background: #FFFFFF;
  box-shadow: 6px 6px 54px rgba(0, 0, 0, 0.05);
  border-radius: 14px;
  padding: 32px 24px;

}

.u-tree-search {
  display: flex;
  flex-direction: column;

  :deep(.ant-tree) {
    overflow: auto;
  }
}

:deep(.ant-input-search) {
  margin-bottom: 10px;
}

:deep(.ant-tree) {

  .ant-tree-treenode {
    align-items: center;
  }

  .ant-tree-node-content-wrapper {
    flex: 1;
  }

  .ant-tree-node-content-wrapper.ant-tree-node-selected {
    background: var(--ant-primary-color-outline);
    border-radius: 10px;
  }

  .ant-tree-title {
    font-family: @main-font-name;
    font-style: normal;
    font-weight: 700;
    font-size: @font-size-base;
    line-height: 22px;
    /* identical to box height, or 157% */
    color: #444444;
  }

  .data-status {
    width: 100%;
  }

  .data-status::after {
    right: -18px;
    top: 0;
    width: 56px;
    height: 16px;
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: center;
    padding: 0 10px;
    border-radius: 10px;
    font-family: 'Microsoft YaHei';
    font-style: normal;
    font-weight: 700;
    font-size: 12px;
    text-align: center;
    color: #FFFFFF;
    content: '未填报';
    /* background: url(../../assets/img/done.png); */
    background: #ff8400;
  }

  .data-done::after {
    content: '已填报';
    /* background: url(../../assets/img/done.png); */
    background: #54C09A;

  }
}
</style>
