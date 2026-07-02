<template>
  <div class="seamless-table">
    <div class="table-header" :style="{height:headerHeight+'px'}">
      <template v-bind:key="column.dataIndex" v-for="column in tableColumns">
        <span class="table-header-column" :class="'table-header-column-'+column.dataIndex" :style="getStyle(column)">{{ column.title }}</span>
      </template>
    </div>
    <div class="table-content" :style="{height:'calc(100% - '+(headerHeight+10)+'px)'}">
      <vue3-seamless-scroll
          :list="data"
          :hover="true"
          :wheel="true"
          :step="step"
          class="table-content-warp"
      >
        <ul class="table-content-item">
          <li v-for="(item, index) in data" :key="index">
            <template v-bind:key="column.dataIndex" v-for="column in tableColumns">
              <template v-if="!column.customRenderSlot">
                <span :title="format(item, column, index)" class="table-content-column" :class="'table-content-column-'+column.dataIndex" :style="getStyle(column)">{{ format(item, column, index) }}</span>
              </template>
              <template v-else>
                <component :is="column.customRenderSlot({item,text:format(item, column, index), column, index,style:getStyle(column)})"/>
              </template>
            </template>
          </li>
        </ul>
      </vue3-seamless-scroll>
    </div>
  </div>
</template>

<script>

export default {
  name: "SeamlessTable"
}
</script>
<script setup>
import {Vue3SeamlessScroll} from "vue3-seamless-scroll";
import {computed} from "vue";

let props = defineProps({
  headerHeight: {
    type: Number,
    default: 36
  },
  showIndex: {
    type: Boolean,
    default: true
  },
  columns: {
    type: Array,
    default: () => {
      return [
        {title: '行政区', dataIndex: 'area', minWidth: 120, align: 'left'},
        {title: '名称', dataIndex: 'name', minWidth: 120, align: 'left'},
        {title: '数量', dataIndex: 'count', minWidth: 120, align: 'left'}
      ]
    }
  },
  data: {
    type: Array,
    default: () => {
      return []
    }
  },
  step: {
    type: Number,
    default: 0.5
  }
})
let tableColumns = computed(() => {
  let columns = []
  if (props.showIndex) {
    columns.unshift({
      title: '序号',
      dataIndex: 'index',
      minWidth: 50,
      align: 'center',
      formatter: (value, item, index) => {
        return index + 1
      }
    })
  }
  columns = columns.concat(props.columns)
  return columns
})
const format = (item, column, index) => {
  if (column.formatter) {
    return column.formatter(item[column.dataIndex], item, index)
  }
  return item[column.dataIndex]
}

const getStyle = (column) => {
  let style = column.style || {}
  if (!style.width) {
    style.width = column.minWidth + 'px'
  }
  if (!style['text-align']) {
    style['text-align'] = column.align || 'left'
  }
  return style
}
</script>
<style lang="less" scoped>
.seamless-table {
  height: 100%;

  .table-header {
    width: 100%;
    color: #ffffff;
    background-color: rgb(16, 59, 93);
    border-bottom: 1px solid #dddddd8c;
    padding: 0 5px;
    display: flex;
    align-items: center;
  }

  .table-content {

    .table-content-warp {
      height: 100%;
      width: 100%;
      margin: 0 auto;
      overflow: hidden;
      color: white;

      ul {
        list-style: none;
        padding: 0;
        margin: 0 auto;

        li {
          width: 100%;
          min-height: 30px;
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-top: 10px;
        }
      }
    }
  }
}
</style>
