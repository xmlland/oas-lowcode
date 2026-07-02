<template>
  <SingleTableView :class="useModernListSkin ? 'modern-list-page gen-table-ext-list-view' : 'tree-table-view'"
                   ref="tableView" :url="url" deleteParamName="id" :singleTable="singleTable" :modalComponent="modalComponent" :modalTitle="modalTitle"
                   :autoHeight="useModernListSkin" :queryArea="queryArea"
                   :buttons="buttons" formNo="gen_table" @clickRow="clickRow" @clickButton="clickButton" :modalFull="true" :u-modal="{showTempSave:true,extendButtons:extendButtons}"
                   @clickOperate="clickOperate">
    <template v-if="useModernListSkin" #queryHeader>
      <div class="modern-list-query-heading">
        <h2 class="modern-list-query-title">表单配置</h2>
        <p class="modern-list-query-desc">维护动态表单、设计入口、同步状态和发布操作。</p>
      </div>
    </template>
    <template #queryFields>
      <QueryField  name="name" label="表名" :type="type"   :props="{placeholder:'表名' }"></QueryField>
      <QueryField  name="module" label="模块" type="input"  :props="{placeholder:'模块' }"></QueryField>
      <QueryField  name="comments" label="说明" type="input"  :props="{placeholder:'说明' }"></QueryField>
      <QueryField  name="parentTable" label="父表表名" type="input"  :props="{placeholder:'父表表名' }"></QueryField>
    </template>
    <template #extendHiddenArea>
      <u-modal ref="menuModal" :component="menuForm" style="margin-top: 100px;">
      </u-modal>
      <u-modal ref="sqlModal" :component="sqlForm" :full="true"/>
      <u-modal ref="importAndExportModal" :component="importAndExportForm" :width="1200"/>
    </template>
  </SingleTableView>

  <u-modal @afterClose="handleAfterCloseImportModal" :customOK="true" @clickOk="saveImportTable"
           @saveSuccess="saveSuccess" title="导入表" ref="importTableTable" :component="importTable" :width="1200"/>
  <ai-create-module-drawer v-model:visible="aiCreateModuleDrawerVisible"/>

</template>

<script lang="jsx">
export default {
  name: "genTableExt_list"
}
</script>
<script setup lang="jsx">
import {computed, getCurrentInstance, onUnmounted, ref} from "vue";
import form from "@/views/gen/genTableExt/form";
import importAndExportForm from "@/views/gen/genTableExt/importAndExportForm";
import importTable from "@/views/gen/genTableExt/importTable";
import sqlForm from "@/views/gen/genTableExt/sqlForm";
import menuForm from "@/views/admin/sys_menu/form";
import AiCreateModuleDrawer from "@/views/gen/genTableExt/ai/AiCreateModuleDrawer";
import {confirmAction, postAction} from "@/api/action";
import {message, Modal} from "ant-design-vue";
import SingleTableView from "@/components/view/SingleTableView";
import QueryField from "@/components/query/QueryField";
import {confirm, getTextFormArray, UUID} from "@/lib/tools";
import {tableTypeOptionData} from "@/views/gen/genOptionData";
import UModal from "@/components/modal/UModal";
import emitter from "@/lib/event";
import config from "@/config";

const importTableTable = ref(null);
const aiCreateModuleDrawerVisible = ref(false)
let modalComponent = form
let instance = getCurrentInstance();
const useModernListSkin = computed(() => {
  return (config.theme?.adminLayout === 'modern' || config.theme?.layoutStyle === 'modern') && config.theme?.modernListSkin !== false
})
let queryArea = computed(() => {
  return useModernListSkin.value ? { resetButton: true, queryAreaStyle: 'ltr' } : {}
})
let url = ref({
  list: 'gen/genTable/findDynamicList',
  delete: 'gen/genTable/removeDynamic?t=t'
})
let type=ref('input');
let buttons = ref([{
  value: 'importTable',
  text: '导入',
},{
  value: 'add',
  text: '添加',
}, {
  value: 'aiCreateModule',
  text: 'AI创建模块',
}, {
  value: 'batchRemoveCustom',
  text: '批量移除',
  danger: true,
  disabledFilter: (rows) => rows.length === 0
}, {
  value: 'syncAll',
  text: '同步表',
}, {
  value: 'syncAllVersionTable',
  text: '同步留痕表',
}, {
  value: 'changeDb',
  text: '换库执行',
}, {
  value: 'publish',
  text: '发布',
  disabledFilter: (rows) => {
    return rows.filter(item => item.tableType !== '2').length === 0
  }
}])
let extendButtons = ref([{
  value: 'createMenu',
  text: '添加菜单',
  visibleFilter: ({row}) => {
    return row.id
  },
}])
let singleTable = ref({
  rowExpandable: (row) => {
    if (!row) {
      return true
    }
    return row.hasChildren === true || row.hasChildren === 1 || row.hasChildren === '1'
  },
  loadChildrenAction: (row) => {
    return postAction(url.value.list, {parentTable: row.name})
  },
  optionWidth: 240,
  rowButtons: [
    {
      value: 'edit',
      text: '设计',
    },
    {
      value: 'editSql',
      text: '编辑SQL',
    },
    {
      value: 'addChildren',
      text: '添加子表',
    },
    {
      value: 'importAndExport',
      text: '导入导出设计',
    },
    {
      value: 'copy',
      text: '复制',
    },
    {
      value: 'app',
      text: '设计App',
    },
    {
      value: 'deleteCustom',
      text: '移除',
      color: 'error',
    },
    {
      value: 'unlock',
      text: '取消lock',
      color: 'error',
    }
  ],
  columns: [
    {title: '表名', dataIndex: 'name', minWidth: 180, align: 'left', sorter: 'false'},
    {title: '说明', dataIndex: 'comments', minWidth: 120, align: 'left', sorter: 'false'},
    {title: '模块', dataIndex: 'module', minWidth: 60, align: 'center', sorter: 'false'},
    {
      title: '表类型', dataIndex: 'tableType', minWidth: 60, align: 'center', sorter: 'false', customRender: ({column, text, index, record}) => {
        return getTextFormArray(text, tableTypeOptionData)
      }
    },
    {title: '流程分类', dataIndex: 'processDefinitionCategory', minWidth: 100, sorter: 'false', align: 'center'},
    {
      title: '同步数据库', dataIndex: 'formType', minWidth: 300, align: 'center', sorter: 'false',
      customRenderSlot: ({column, text, index, record}) => {
        if (record.name.indexOf("@") > -1) {
          return (<span>-</span>)
        } else if (text === "dynamic") {
          if (record.isVersion === '1') {
            //创建留痕表
            return (<span>

                      <a style="padding-left: 4px" onClick={() => {
                        syncTable({record})
                      }}><i className="fa fa-database"></i>同步表</a>
                      <a style="color: #ff4d4f;padding-left: 4px" onClick={() => {
                        syncDB({record})
                      }}><i className="fa fa-database"></i>重建表</a>
                  <a style="padding-left: 4px" onClick={() => {
                    syncVersionTable({record})
                  }}><i className="fa fa-database"></i>同步留痕表</a>
                  <a style="color: #ff4d4f;padding-left: 4px" onClick={() => {
                    syncDBVersion({record})
                  }}><i className="fa fa-database"></i>创建留痕表</a>
                    </span>
            )
          } else {
            return (<span>
                      <a style="padding-left: 4px" onClick={() => {
                        syncTable({record})
                      }}><i className="fa fa-database"></i>同步表</a>
                      <a style="color: #ff4d4f;padding-left: 4px" onClick={() => {
                        syncDB({record})
                      }}><i className="fa fa-database"></i>重建表</a>
                    </span>)
          }
        }
      },
    },
    {
      title: '是否同步', dataIndex: 'isSync', minWidth: 100, align: 'center', sorter: 'false',
      customRenderSlot:({column, text, index, record})=>{
        if(record.name.indexOf('@')>0){return (<span>-</span>)}
        if (text === '1') {
          return (<a-tag color="green">已同步</a-tag>)
        } else {
          return (<a-tag color="red">未同步</a-tag>)
        }
      }
    },
    {
      title: '发布状态', dataIndex: 'isRelease', minWidth: 100, align: 'center', sorter: 'false', status: {
        enableText: '是',
        disableText: '否'
      }
    },
    {title: 'lockSql', dataIndex: 'exportRuleName', minWidth: 60, sorter: 'false', align: 'left'},
  ]
})

const modalTitle = (row, typeName) => {
  if (typeName === '添加' || typeName === '编辑') {
    return '表单设计'
  }
  return typeName
}

const syncDBVersion = (row) => {
  confirmAction('操作确认？', '确定要创建留痕表吗？', 'gen/genTable/syncDynamicVersion', {id: row.record.id}, (res) => {
    message.success(res.msg);
    saveSuccess()
  })
}

const syncVersionTable = (row) => {
  confirmAction('操作确认？', '确定要同步留痕表吗？', 'gen/genTable/syncVersionTable', {id: row.record.id}, (res) => {
    message.success(res.msg);
    saveSuccess()
  })
}

const syncTable = (row) => {
  confirmAction('操作确认？', '确定要同步表吗？', 'gen/genTable/syncDynamicTable', {id: row.record.id}, (res) => {
    message.success(res.msg);
    saveSuccess()
  })
}

const syncDB = (row) => {
  confirmAction('操作确认？', '确定要重建表吗？', 'gen/genTable/syncDynamic', {id: row.record.id}, (res) => {
    message.success(res.msg);
    saveSuccess()
  })
}
const saveSuccess = () => {
  instance.refs.tableView.saveSuccess()
}
const clickRow = ({value, row, index}) => {
  if ('editSql' === value) {
    instance.refs.sqlModal.open('编辑sql', row)
  }
  if ('addChildren' === value) {
    instance.refs.tableView.getModal().open('添加子表', {
      parentTable: row.name,
      //如果当前为树表 则子表为左树右表 否则为子表
      tableType: row.tableType === '3' ? '4' : '0',
      module: row.module
    })
  } else if ('importAndExport' === value) {
    instance.refs.importAndExportModal.open('导入导出设计', row)
  } else if ('copy' === value) {
    confirmAction('操作确认', '确认要复制该表单吗', 'gen/genTable/copyDynamic', {id: row.id}, () => {
      saveSuccess()
    })
  } else if ('deleteCustom' === value) {
    confirmAction('删除确认', '确认要删除该条记录吗', url.value.delete, {id: row.id}, () => {
      saveSuccess()
    })
  } else if ('unlock' === value) {
    confirmAction('操作确认', '确认要取消所有lockSql吗', `gen/genTable/unlockSql?id=${row.id}`, {}, () => {
      saveSuccess()
    })
  } else if ('app' === value) {
    let theUrl = import.meta.env.BASE_URL + 'editor-mobile/index.html?formId=' + row.id;
    window.open(theUrl, '_blank');
  }
}

const importTableModalValue = ref(null)

function handleAfterCloseImportModal() {
  importTableModalValue.value = null
}
emitter.on('importTableName', (value) => {
  importTableModalValue.value = value
});
onUnmounted(()=>{
  emitter.off('importTableName')
})

const clickButton = ({value, data}) => {
  if (value === 'syncAll') {
    confirmAction('操作确认', '确定要批量同步表吗', 'gen/genTable/syncTables', data, () => {
      saveSuccess()
    })
  } else if (value === 'syncAllVersionTable') {
    confirmAction('操作确认', '确定要同步所有留痕表吗', 'gen/genTable/syncAllVersionTable', {}, () => {
      saveSuccess()
    })
  } else if (value === 'changeDb') {
    confirmAction('操作确认', '确定要换库执行吗', 'gen/genTable/changeDb', data, () => {
      saveSuccess()
    })
  } else if (value === 'importTable') {

    //instance.refs.importTable.open('导入表')
    importTableTable.value?.open('导入表')

  } else if (value === 'aiCreateModule') {
    aiCreateModuleDrawerVisible.value = true
  } else if (value === 'batchRemoveCustom') {
    batchRemoveCustom()
  } else if (value === 'publish') {
    let rows = instance.refs.tableView.getSelectedRows().filter((item) => {
      return item.tableType !== '2'
    })
    if (rows.length === 0) {
      message.error('请选择表单')
      return
    }
    confirm({
      title: '操作确认',
      content: '确定要发布选中的表单吗',
      onOk() {
        const msg = message.loading({
          content: '处理中...',
          duration: 0
        });
        let promiseArr = []
        rows.forEach((item) => {
          promiseArr.push(new Promise((resolve, reject) => {
            postAction('gen/genTable/buildViewDynamic', {genTable: {id: item.id}}).then((buildRes) => {
              if (buildRes.data && buildRes.data.genScheme) {
                let scheme = buildRes.data.genScheme
                postAction('gen/genTable/buildDynamic', {
                  category: scheme.category,
                  functionName: scheme.functionName,
                  functionNameSimple: scheme.functionNameSimple,
                  genTable: {id: item.id},
                  id: scheme.id,
                  moduleName: scheme.moduleName,
                  packageName: scheme.packageName,
                }).then((res) => {
                  resolve(res)
                }).catch(() => {
                  reject()
                })
              } else {
                reject()
              }
            })
          }))
        })

        Promise.all(promiseArr).then(() => {
          Modal.info({
            title: '提示',
            content: '发布成功'
          });
          saveSuccess()
        }).catch(() => {
          Modal.info({
            title: '提示',
            content: '发布失败'
          });
        }).finally(()=> {
          msg()
        })

      },
      onCancel() {
      },
    })
  }
}

const formatRemoveRowName = (row = {}) => {
  const name = row.name || row.id || ''
  const comments = row.comments ? `（${row.comments}）` : ''
  return `${name}${comments}`
}

const removeRowsByQueue = async (rows = []) => {
  const successRows = []
  const failedRows = []
  for (const row of rows) {
    try {
      const res = await postAction(url.value.delete, {id: row.id})
      if (res.code === 0) {
        successRows.push(row)
      } else {
        failedRows.push(row)
      }
    } catch (e) {
      console.error(e)
      failedRows.push(row)
    }
  }
  return {successRows, failedRows}
}

const batchRemoveCustom = () => {
  const rows = instance.refs.tableView.getSelectedRows().filter(row => row && row.id)
  if (rows.length === 0) {
    message.warning('请选择要移除的表单配置')
    return
  }
  const previewRows = rows.slice(0, 5).map(formatRemoveRowName).join('、')
  const moreText = rows.length > 5 ? ` 等 ${rows.length} 条` : ''
  Modal.confirm({
    title: '批量移除确认',
    content: `确定要移除选中的 ${rows.length} 条表单配置吗？将移除：${previewRows}${moreText}。此操作只移除表单配置和生成方案，不会删除已同步的业务表。`,
    okText: '确认移除',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      const closeLoading = message.loading({
        content: '正在批量移除...',
        duration: 0
      })
      try {
        const {successRows, failedRows} = await removeRowsByQueue(rows)
        if (failedRows.length === 0) {
          message.success(`已移除 ${successRows.length} 条表单配置`)
        } else {
          Modal.warning({
            title: '批量移除完成，部分失败',
            content: `成功 ${successRows.length} 条，失败 ${failedRows.length} 条。失败项：${failedRows.slice(0, 5).map(formatRemoveRowName).join('、')}${failedRows.length > 5 ? ' 等' : ''}`
          })
        }
        instance.refs.tableView.getTableRef().updateAllPageSelectedRows({deleteRowKeys: successRows.map(row => row.id)})
        saveSuccess()
      } finally {
        closeLoading()
      }
    }
  })
}

const clickOperate = (item, callback, data) => {
  if ('createMenu' === item.value) {
    let table = data[0]
    let extendJs = {}
    if (table.extendJs) {
      extendJs = JSON.parse(table.extendJs)
    }
    let permissionArr = []
    let index = 10
    if (extendJs.list__buttons) {
      extendJs.list__buttons.forEach((item) => {
        if (item.permission) {
          permissionArr.push({formNo: 'sys_menu', type_: '3', is_show: '1', name: item.text, name_en: item.text, permission: item.permission, sort: index})
          index += 10
          if (item.permission.indexOf(':del')>=0){
            permissionArr.push({formNo: 'sys_menu', type_: '4', is_show: '1', name: '批量删除', name_en: '批量删除', permission: item.permission.replace(':del',':remove'), sort: index})
            index += 10
          }
        }
      })
    }
    if (extendJs.singleTable__rowButtons) {
      extendJs.singleTable__rowButtons.forEach((item) => {
        if (item.permission) {
          permissionArr.push({formNo: 'sys_menu', type_: '4', is_show: '1', name: item.text, name_en: item.text, permission: item.permission, sort: index})
          index += 10
        }
      })
    }
    //permissionArr 根据permission去重
    let permissionArr2 = []
    permissionArr.forEach((item) => {
      let flag = true
      permissionArr2.forEach((item2) => {
        if (item.permission === item2.permission) {
          flag = false
        }
      })
      if (flag) {
        item.id = UUID()
        permissionArr2.push(item)
      }
    })

    instance.refs.menuModal.open('添加菜单', {
      name: table.comments,
      name_en: table.comments_EN,
      is_show: '1',
      type_: '1',
      sort: 100,
      href: '/ht/dynamic/' + table.id,
      target: 'DynamicList',
      quickPermission: '1',
      processDefinitionCategory: table.processDefinitionCategory,
      permissionArr: permissionArr2
    })
  }
}

function saveImportTable(callback) {
  if (!importTableModalValue.value){
    Modal.info({
      title: '提示',
      content: '请选择表名',
    })
  }else {
    callback(postAction, 'gen/genTable/importDynamic', {"name": importTableModalValue.value.tableName})
  }
}

</script>
<style lang="less" scoped>
:deep(.ant-table-cell-with-append) {
  display: flex;
  align-items: center;
}
.dp{
  cursor: pointer;
  padding-left: 10px;
}
.dp:hover{
  background-color: rgba(75, 87, 213,0.15);
}
</style>
