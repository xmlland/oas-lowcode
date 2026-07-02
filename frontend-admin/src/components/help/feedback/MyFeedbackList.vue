<template>
  <div class="content-bg">
    <div class="search-area">
      <query-area @query="queryData">
        <template #queryFields>
          <QueryField width="260" name="key_word" type="input" :props="{placeholder:'关键词' }"></QueryField>
        </template>
      </query-area>
    </div>
    <div class="card-list">
      <a-spin v-if="loading" size="large"/>
      <template v-else>
        <a-empty v-if="pagination.total===0" description="暂无数据"/>
        <template v-else>
          <div class="card-list-item" v-bind:key="index" v-for="(item,index) in dataList">
            <div class="card-list-header">
              <a-badge dot v-if="item.user_read==='0'">
                <a-avatar size="small" shape="square" class="index-no">{{ formatIndex(index) }}</a-avatar>
              </a-badge>
              <a-avatar v-else size="small" shape="square" class="index-no">{{ formatIndex(index) }}</a-avatar>
              <div class="card-list-title">
                {{ item.title_ }}
              </div>
              <div class="card-list-status">
                <a-tag v-if="item.is_reply === '1'" color="#87d068">已回复</a-tag>
                <a-tag v-else color="#f50">未回复</a-tag>
              </div>
            </div>
            <div class="card-list-body">
              时间：{{ item.submit_time }}
              模块：{{ item.module_ }}
              <a-button type="link" @click="showDetail(item)">详情</a-button>
            </div>
          </div>
        </template>
      </template>
    </div>
    <div style="text-align: right">
      <a-pagination v-model:current="pagination.current" :hideOnSinglePage="true" :total="pagination.total" @change="pageChange"/>
    </div>
  </div>
</template>

<script>
export default {
  name: "MyFeedbackList"
}
</script>
<script setup>
import {onMounted, ref} from "vue";
import {listDataAction} from "@/api/api";
import QueryArea from "@/components/query/QueryArea";
import QueryField from "@/components/query/QueryField";
import {postActionByParams} from "@/api/action";
import {useStore} from "vuex";

let props = defineProps({
  funcObj: {
    type: Object,
    default: () => {
      return {}
    }
  }
})

let loading = ref(true)
let pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})
let dataList = ref([])

const formatIndex = (index) => {
  return pagination.value.pageSize * (pagination.value.current - 1) + index + 1
}

const loadData = (page = 1, queryData = {}) => {
  loading.value = true
  pagination.value.current = page
  queryData.pageParam = {
    pageNo: page,
    pageSize: pagination.value.pageSize
  }
  listDataAction('sys_feedback', queryData).then(res => {
    dataList.value = res.rows
    pagination.value.total = res.total
  }).finally(() => {
    loading.value = false
  })
}

onMounted(() => {
  loadData()
})

const pageChange = (page) => {
  loadData(page)
}

const queryData = (obj) => {
  loadData(pagination.value.current, obj)
}
let store = useStore();
const showDetail = (item) => {
  if (item.user_read === '0') {
    postActionByParams(`admin/sysFeedback/updateRead`, {id: item.id}).then(res => {
      item.user_read = '1'
      store.commit('setFeedbackNews', res.data.needRead)
    })
  }
  props.funcObj?.navTo('FeedBackDetailForm', '问题详情', {item})
}
</script>
<style lang="less" scoped>
@import "./feedback";
</style>
