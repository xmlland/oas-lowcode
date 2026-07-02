<template>
  <div class="simple-table" :id="id">
    <table class="simple-table-head">
      <tr>
        <th v-if="showIndex" class="table-header-column table-column-index">序号</th>
        <template v-bind:key="column.dataIndex" v-for="column in columns">
          <th class="table-header-column" :class="'table-header-column-'+column.dataIndex" :style="getStyle(column,true)">{{ column.title }}</th>
        </template>
      </tr>
    </table>
    <div class="simple-table-body-container" :style="{height:tableHeight+'px'}">
      <a-spin v-if="loading" size="large"/>
      <table v-else class="simple-table-body">
        <tbody>
          <tr v-for="(item, index) in data" :key="index">
          <td v-if="showIndex" class="table-content-column table-column-index">{{ index + 1 }}</td>
          <template v-bind:key="column.dataIndex" v-for="column in columns">
            <template v-if="!column.customRenderSlot">
              <td :title="format(item, column, index)" class="table-content-column" :class="'table-content-column-'+column.dataIndex" :style="getStyle(column)">{{ format(item, column, index) }}</td>
            </template>
            <template v-else>
              <td class="table-content-column" :class="'table-content-column-'+column.dataIndex" :style="getStyle(column)">
                <component :is="column.customRenderSlot({item,text:format(item, column, index), column, index,style:column.style})"/>
              </td>
            </template>
          </template>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
export default {
  name: "SimpleTable"
}
</script>
<script setup>
import {onMounted, ref} from "vue";
import {UUID} from "@/lib/tools";

let id = ref('u-simple-table-' + UUID())
defineProps({
  headerHeight: {
    type: Number,
    default: 42
  },
  showIndex: {
    type: Boolean,
    default: true
  },
  columns: {
    type: Array,
    default: () => {
      return [{
        title: '行政区',
        dataIndex: 'area',
        style: {
          width: '33%'
        }
      }, {
        title: '名称',
        dataIndex: 'name',
        style: {
          width: '33%'
        }
      }, {
        title: '数量',
        dataIndex: 'count',
        style: {
          width: '33%'
        }
      }]
    }
  },
  data: {
    type: Array,
    default: () => {
      return []
    }
  },
  loading: {
    type: Boolean,
    default: false
  }
})
const format = (item, column, index) => {
  if (column.formatter) {
    return column.formatter(item[column.dataIndex], item, index)
  }
  return item[column.dataIndex]
}
const getStyle = (column, header = false) => {
  let style = column.style || {}
  if (!style.width) {
    style.width = column.minWidth + 'px'
  }
  if (!header) {
    style['text-align'] = column.align || 'left'
  }
  return style
}

let tableHeight = ref(0)
onMounted(() => {
  let tableHead = document.getElementById(id.value).querySelector('.simple-table-head')
  tableHeight.value = document.getElementById(id.value).clientHeight - tableHead.clientHeight
})
</script>
<style lang="less" scoped>
.simple-table {
  height: 100%;

  table {
    width: 100%;
  }

  .simple-table-head {

    tr {
      //border-radius: 50%;
    }

    th {
      line-height: 20px;
      padding: 10px 5px !important;
    }
  }

  .simple-table-body-container {

    overflow-y: auto;

    .ant-spin {
      display: flex;
      justify-content: center;
      align-items: center;
      height: 100%;
      width: 100%;
    }

    &::-webkit-scrollbar {
      width: 0;
    }

    .simple-table-body {
      tr {
        &:last-child {
          border-bottom: 0;
        }
      }

      td {
        padding: 4px 5px !important;
      }
    }
  }

  .table-header-column {
    text-align: center;
  }

  .table-column-index {
    width: 50px;
    text-align: center;
  }
}
</style>
