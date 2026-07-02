<template>
  <u-form extend-class="sql-form" v-model:value="formModel" v-model:disabled="disabled" :labelWidth="120" formNo="gen_table">
    <a-tabs>
      <a-tab-pane :key="0" forceRender tab="字段SQL">
        <a-row :gutter="16">
          <a-col :span="24">
            <a-form-item name="form_no_ext" label="表名替代SQL">
              <u-code-mirror :height="100" lang="sql" v-model:value="formModel.form_no_ext"/>
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item name="sql_columns" label="SQL" :rules="[{ required: true, message: '请输入字段SQL' }]">
              <u-code-mirror height="calc(100vh - 180px - 200px)" lang="sql" v-model:value="formModel.sql_columns"/>
            </a-form-item>
          </a-col>
        </a-row>
      </a-tab-pane>
      <a-tab-pane :key="1" forceRender tab="字段SQL（友好接口）">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item name="sql_columns_friendly" label="SQL" :rules="[{ required: true, message: '请输入字段SQL（友好接口）' }]">
              <template #extra>
                <span style="color:var(--ant-error-color)">强烈不建议修改此处的SQL，会导致有字段变化时不会在此处生成对应的SQL</span>
              </template>
              <u-code-mirror height="calc(100vh - 180px - 20px)" lang="sql" v-model:value="formModel.sql_columns_friendly"/>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item name="sql_columns_friendly_ext" label="扩展字段SQL">
              <u-code-mirror height="calc(100vh - 180px - 20px)" lang="sql" v-model:value="formModel.sql_columns_friendly_ext"/>
            </a-form-item>
          </a-col>
        </a-row>
      </a-tab-pane>
      <a-tab-pane :key="2" forceRender tab="关联SQL">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item name="sql_joins" label="SQL" :rules="[{ required: false, message: '请输入关联SQL' }]">
              <template #extra>
                <span style="color:var(--ant-error-color)">强烈不建议修改此处的SQL，会导致有字段变化时不会在此处生成对应的SQL</span>
              </template>
              <u-code-mirror height="calc(100vh - 180px - 20px)" lang="sql" v-model:value="formModel.sql_joins"/>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item name="sql_joins_ext" label="扩展关联SQL">
              <u-code-mirror height="calc(100vh - 180px - 20px)" lang="sql" v-model:value="formModel.sql_joins_ext"/>
            </a-form-item>
          </a-col>
        </a-row>
      </a-tab-pane>
      <a-tab-pane :key="3" forceRender tab="插入SQL">
        <a-row :gutter="16">
          <a-col :span="24">
            <a-form-item name="sql_insert" label="SQL" :rules="[{ required: true, message: '请输入插入SQL' }]">
              <u-code-mirror height="calc(100vh - 180px)" lang="sql" v-model:value="formModel.sql_insert"/>
            </a-form-item>
          </a-col>
        </a-row>
      </a-tab-pane>
      <a-tab-pane :key="4" forceRender tab="更新SQL">
        <a-row :gutter="16">
          <a-col :span="24">
            <a-form-item name="sql_update" label="SQL" :rules="[{ required: true, message: '请输入更新SQL' }]">
              <u-code-mirror height="calc(100vh - 180px)" lang="sql" v-model:value="formModel.sql_update"/>
            </a-form-item>
          </a-col>
        </a-row>
      </a-tab-pane>
      <a-tab-pane :key="5" forceRender tab="排序SQL">
        <a-row :gutter="16">
          <a-col :span="24">
            <a-form-item name="sql_sort" label="SQL">
              <u-code-mirror height="calc(100vh - 180px)" lang="sql" v-model:value="formModel.sql_sort"/>
            </a-form-item>
          </a-col>
        </a-row>
      </a-tab-pane>
      <a-tab-pane :key="6" forceRender tab="对外接口SQL">
        <a-row :gutter="16">
          <a-col :span="24">
            <a-form-item name="ext_sql02" label="SQL">
              <u-code-mirror height="calc(100vh - 180px)" lang="sql" v-model:value="formModel.ext_sql02"/>
            </a-form-item>
          </a-col>
        </a-row>
      </a-tab-pane>
    </a-tabs>
  </u-form>
</template>

<script>
export default {
  name: "sqlForm"
}
</script>
<script setup>
import UForm from "@/components/form/UForm";
import {ref} from "vue";
import UCodeMirror from "@/components/form/code/UCodeMirror";

let formModel = ref({})
let disabled = ref(false)
</script>
<style lang="less" scoped>
.sql-form{
  .ant-tabs-tabpane{
    height: calc(100vh - 42px - 53px - 46px - 20px);
    padding: 0 20px;
    overflow-y: auto;
  }
}
</style>
