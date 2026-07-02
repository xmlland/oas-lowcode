<template>
  <SingleTableView :modalFull="false"
    :class="useModernListSkin ? 'modern-list-page oas-meeting-reservation-list-view' : ''"
    :autoHeight="useModernListSkin"
    :queryArea="queryArea"
    :queryButton="true" :singleTable="singleTable" :modalComponent="modalComponent" modalTitle="会议室预定" :buttons="buttons" formNo="oas_meeting_reservation" :modalWidth="1200" >
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">会议室预定</h2>
        <p class="modern-list-query-desc">维护会议室预约、时间安排和预订人信息。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField name="meeting_room" label="会议室" type="modal-select"
                  :props="{formNo: 'oas_meeting_room',searchKey: 'name', searchLabel: '名称',
                           columns: [
                             {title: '名称', dataIndex:'name',align: 'left'},
                             {title: '位置', dataIndex:'position',align: 'left'},
                             {title: '人数', dataIndex:'people_number',align: 'left'},
                             {title: '设备', dataIndex:'equipment',align: 'left'},
                             {title: '状态', dataIndex:'status',align: 'left'},
                           ],
                           filterData:[],
                           placeholder: '请选择会议室'}">
      </QueryField>
      <QueryField name="name" label="会议名称" type="input" :props="{placeholder:'会议名称' }"></QueryField>
      <QueryField name="booker" label="预订人" type="userSelect" :props="{placeholder: '预订人'}"></QueryField>
      <QueryField name="start_time" label="开始时间" type="date" :props="{formatPatter:'yyyy-MM-dd HH:mm'}"></QueryField>
      <QueryField name="end_time" label="结束时间" type="date" :props="{formatPatter:'yyyy-MM-dd HH:mm'}"></QueryField>
    </template>
  </SingleTableView>
</template>

<script>
export default {
  name: "oas_meeting_reservation_list",
}
</script>
<script setup>
import {computed, ref} from "vue";
import form from "@/views/oas/oas_meeting_reservation/form";
import config from "@/config";
let modalComponent = form
const useModernListSkin = computed(() => {
  return (config.theme?.adminLayout === 'modern' || config.theme?.layoutStyle === 'modern') && config.theme?.modernListSkin !== false
})
let queryArea = computed(() => {
  return useModernListSkin.value ? { resetButton: true, queryAreaStyle: 'ltr' } : {}
})

let buttons = ref([{
  value: 'add',
  text: '添加',
  permission: 'app:oas_meeting_reservation:add',
}, {
  value: 'batch-delete',
  text: '删除',
  permission: 'app:oas_meeting_reservation:remove',
}])

let singleTable = ref({
  rowButtons: [
    {
      value: 'view',
      text: '查看',
      permission: 'app:oas_meeting_reservation:view',
    }, {
      value: 'edit',
      text: '编辑',
      permission: 'app:oas_meeting_reservation:edit',
    }
  ],
  columns: [
    {title:'会议室',dataIndex:'meeting_room__name',minWidth:150,align:'left',sorter:'true',ellipsis:false},
    {title:'会议名称',dataIndex:'name',minWidth:150,align:'left',sorter:'false',ellipsis:false},
    {title:'预订人',dataIndex:'booker__name',minWidth:150,align:'center',queryColumn:'user02.name',sorter:'true',ellipsis:false},
    {title:'手机',dataIndex:'mobile_phone',minWidth:150,align:'center',sorter:'false',ellipsis:false},
    {title:'开始时间',dataIndex:'start_time',minWidth:150,align:'center',sorter:'true',ellipsis:false},
    {title:'结束时间',dataIndex:'end_time',minWidth:150,align:'center',sorter:'false',ellipsis:false},
  ]
})

</script>
<style scoped>
</style>
