<template>
  <div :class="useModernListSkin ? 'modern-list-page cache-management-page' : 'cache-management-legacy'">
  <u-form>
    <a-row :gutter="16">
      <a-col :span="24">
        <div class="forum-item active">
          <div class="row">
            <div class="col-md-12">
              <div class="forum-icon">
                <i class="fa fa-book"></i>
              </div>
              <a class="forum-item-title">字典管理</a>
              <div class="forum-sub-title">Dictionary management
                <a-button @click="refreshDict" type="primary" class="cache-action-button">刷新字典缓存</a-button>
              </div>
            </div>
          </div>
        </div>
      </a-col>
      <a-col :span="24">
        <div class="forum-item active">
          <div class="row">
            <div class="col-md-12">
              <div class="forum-icon">
                <i class="fa fa-reorder"></i>
              </div>
              <a class="forum-item-title">菜单管理</a>
              <div class="forum-sub-title">Menu management
                <a-button @click="refreshMenu" type="primary" class="cache-action-button">刷新菜单缓存</a-button>
              </div>
            </div>
          </div>
        </div>
      </a-col>
      <a-col :span="24">
        <div class="forum-item active">
          <div class="row">
            <div class="col-md-12">
              <div class="forum-icon">
                <i class="fa fa-cubes"></i>
              </div>
              <a class="forum-item-title">表单管理</a>
              <div class="forum-sub-title">GenTable management
                <a-button @click="refreshGenTable" type="primary" class="cache-action-button">刷新表单缓存</a-button>
              </div>
            </div>
          </div>
        </div>
      </a-col>
    </a-row>
  </u-form>
  </div>
  <!--
  <div class="layui-fluid">
    <div class="layui-card">
      <div id="body-div" class="layui-card-body">
        <div class="content-box">
          <div class="forum-item active">
            <div class="row">
              <div class="col-md-9">
                <div class="forum-icon">
                  <i class="fa fa-book"></i>
                </div>
                <a lay-href="/admin/sys_dictionary/list" class="forum-item-title">字典管理</a>
                <div class="forum-sub-title">Dictionary management</div>
              </div>
              <div class="col-md-3 forum-info">
                <a id="refreshDict" class="btn btn-success btn-bordered btn-sm"><i class="fa fa-refresh"></i> 刷新字典缓存</a>
              </div>
            </div>
          </div>
          <div class="forum-item active">
            <div class="row">
              <div class="col-md-9">
                <div class="forum-icon">
                  <i class="fa fa-reorder"></i>
                </div>
                <a lay-href="/admin/sys_menu/list" class="forum-item-title">菜单管理</a>
                <div class="forum-sub-title">Menu management</div>
              </div>
              <div class="col-md-3 forum-info">
                <a id="refreshMenu" class="btn btn-success btn-bordered btn-sm"><i class="fa fa-refresh"></i> 刷新菜单缓存</a>
              </div>
            </div>
          </div>
          <div class="forum-item active">
            <div class="row">
              <div class="col-md-9">
                <div class="forum-icon">
                  <i class="fa fa-cubes"></i>
                </div>
                <a lay-href="/admin/genTable/list" class="forum-item-title">表单管理</a>
                <div class="forum-sub-title">GenTable management</div>
              </div>
              <div class="col-md-3 forum-info">
                <a id="refreshGenTable" class="btn btn-success btn-bordered btn-sm"><i class="fa fa-refresh"></i> 刷新表单缓存</a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  -->
</template>

<script>
export default {
  name: "cache_management_list"
}
</script>

<script setup>
import {getAction} from "@/api/action";
import {message} from "ant-design-vue";
import {computed} from "vue";
import config from "@/config";

const useModernListSkin = computed(() => {
  return (config.theme?.adminLayout === 'modern' || config.theme?.layoutStyle === 'modern') && config.theme?.modernListSkin !== false
})

//刷新字典缓存
const refreshDict = () => {
  let loading = message.loading("刷新字典缓存...")
  getAction('sys/dict/refreshDictCache', {}).then(() => {
    message.success('刷新字典缓存成功！');
    loading()
  })
}

const refreshMenu = () => {
  let loading = message.loading("刷新菜单缓存...")
  getAction('sys/menu/refreshMenuCache', {}).then(() => {
    message.success('刷新菜单缓存成功！');
    loading()
  })
}

const refreshGenTable = () => {
  let loading = message.loading("刷新表单缓存...")
  getAction('gen/genTable/refreshGenTableCache', {}).then(() => {
    message.success('刷新表单缓存成功！');
    loading()
  })
}
</script>

<style lang="less" scoped>
.cache-management-legacy {
  background: #ffffff;
  margin: 14px;
  border-radius: 4px;
}

:deep(.ant-form){
  padding: 20px;
}

.cache-management-page {
  min-height: 100%;

  :deep(.ant-form) {
    padding: 12px 16px 16px;
    background: #ffffff;
    border: 1px solid #e6ebf2;
    border-radius: 8px;
    box-shadow: 0 8px 24px rgba(31, 41, 55, 0.04);
  }

  .forum-item {
    margin: 0;
    padding: 16px 0;
    border-bottom: 1px solid #eef2f7;
  }

  .forum-item:last-child {
    border-bottom: 0;
  }

  .forum-icon {
    width: 32px;
    margin-left: 4px;
    margin-right: 14px;
  }

  .forum-icon .fa {
    margin-top: 4px;
    color: var(--modern-primary, #1677ff);
    font-size: 22px;
  }

  .forum-item.active a.forum-item-title,
  a.forum-item-title {
    color: #1f2937;
    font-size: 15px;
    font-weight: 600;
    line-height: 24px;
  }

  .forum-item .forum-sub-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    width: auto;
    min-height: 32px;
    margin-left: 50px;
    color: #667085;
    font-size: 13px;
    line-height: 20px;
  }

  .cache-action-button {
    float: none;
    flex: 0 0 auto;
    height: 32px;
    border-radius: 6px !important;
    box-shadow: none;
  }
}

.forum-item {
  margin: 10px 0;
  padding: 10px 0 20px;
  border-bottom: 1px solid #f1f1f1;
}

.forum-icon {
  float: left;
  width: 30px;
  margin-left: 20px;
  margin-right: 20px;
  text-align: center;
}

.forum-item.active .fa {
  color: #1ab394;
}

.forum-icon .fa {
  font-size: 30px;
  margin-top: 8px;
  color: #9b9b9b;
}

.forum-item.active .fa.fa-refresh {
  color: #fff;
}

.forum-item.active a.forum-item-title {
  color: #1ab394;
}

a.forum-item-title {
  color: inherit;
  display: block;
  font-size: 18px;
  font-weight: 600;
}

.forum-item .forum-sub-title {
  color: #999;
  margin-left: 50px;
  width: 90%;
}
</style>
