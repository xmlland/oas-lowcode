# AntVue3 组件库使用手册

## 目录结构

```
frontend-admin/src/components/
├── form/                   # 表单组件（核心）
│   ├── UInput.vue         # 输入框
│   ├── USelect.vue        # 下拉选择
│   ├── UDate.vue          # 日期/时间选择
│   ├── UDateRange.vue     # 日期范围选择
│   ├── USwitch.vue        # 开关
│   ├── UUpload.vue        # 文件上传
│   ├── UArea.vue          # 地区选择
│   ├── UTreeSelect.vue    # 树形选择
│   ├── UIconSelect.vue    # 图标选择
│   ├── UTinymce.vue       # 富文本编辑器
│   ├── UJson.vue          # JSON编辑器
│   ├── UYesNo.vue         # 是否选择
│   ├── UForm.vue          # 表单容器
│   ├── UFormTitle.vue     # 表单标题
│   ├── UDynamicFormItem.vue # 动态表单项
│   ├── UTimelinePicker.vue  # 时间轴选择器
│   ├── ULngLatSelect.vue    # 经纬度/地图选点
│   └── sys/               # 系统组件
│       ├── UUserSelect.vue    # 用户选择
│       ├── UUsersSelect.vue   # 多用户选择
│       └── UOfficeSelect.vue  # 机构选择
├── modal/                  # 弹窗组件
├── query/                  # 查询组件
├── echarts/                # 图表组件
├── map/                    # 地图组件
└── act/                    # 工作流组件
```

---

## 一、表单输入组件

### 1. UInput - 输入框

多类型输入框，支持文本、数字、整数、文本域。

**Props**

| 属性 | 类型 | 默认值 | 说明 |
|-----|------|-------|------|
| value | String/Number | null | 当前值 |
| type | String | 'text' | 类型: text/digits/number |
| disabled | Boolean | false | 禁用 |
| placeholder | String | '' | 占位符 |
| maxlength | Number | 255 | 最大长度 |
| precision | Number | 0 | 数字精度（小数位数） |
| max | Number | Infinity | 最大值（数字类型） |
| min | Number | -Infinity | 最小值（数字类型） |
| allowClear | Boolean | true | 显示清除按钮 |
| textarea | Boolean | false | 是否文本域 |
| showCount | Boolean | false | 显示字数统计 |
| defaultValue | String | null | 默认值 |

**Events**

| 事件 | 参数 | 说明 |
|-----|-----|------|
| update:value | value | 值变化 |
| change | value | 改变事件 |
| blur | value | 失焦事件 |

**示例**

```vue
<!-- 文本输入 -->
<u-input v-model:value="formData.name" placeholder="请输入姓名" />

<!-- 数字输入 -->
<u-input v-model:value="formData.age" type="number" :min="0" :max="150" />

<!-- 带精度的数字 -->
<u-input v-model:value="formData.price" type="number" :precision="2" />

<!-- 文本域 -->
<u-input v-model:value="formData.remark" textarea :showCount="true" :maxlength="500" />
```

---

### 2. USelect - 下拉选择

支持字典、URL、表格等多种数据源的下拉选择框。

**Props**

| 属性 | 类型 | 默认值 | 说明 |
|-----|------|-------|------|
| value | String/Number/Boolean | null | 当前值 |
| disabled | Boolean | false | 禁用 |
| multiple | Boolean | false | 多选 |
| multipleSeparator | String | ',' | 多选分隔符 |
| type | String | 'dict' | 数据源类型: dict/table/url |
| dictType | String | null | 字典类型 |
| dataUrl | String | '' | 数据URL |
| postData | Object | {} | URL请求参数 |
| optionData | Array | [] | 自定义选项数据 |
| valueField | String | 'member' | 值字段 |
| textField | String | 'memberName' | 显示字段 |
| placeholder | String | '' | 占位符 |
| allowClear | Boolean | true | 显示清除按钮 |
| showSearch | Boolean | true | 显示搜索框 |
| defaultIndex | Number | null | 默认选中索引 |

**Events**

| 事件 | 参数 | 说明 |
|-----|-----|------|
| update:value | value | 值变化 |
| change | value, options | 选择改变 |
| changeText | text | 文本变化 |
| loadSuccess | options | 数据加载成功 |

**示例**

```vue
<!-- 字典数据源 -->
<u-select v-model:value="formData.status" type="dict" dictType="sys_status" />

<!-- URL数据源 -->
<u-select 
  v-model:value="formData.deptId" 
  type="url" 
  dataUrl="/api/dept/list" 
  valueField="id" 
  textField="name" 
/>

<!-- 自定义数据 -->
<u-select 
  v-model:value="formData.type" 
  :optionData="[{id: 1, name: '类型A'}, {id: 2, name: '类型B'}]" 
  valueField="id" 
  textField="name" 
/>

<!-- 多选 -->
<u-select v-model:value="formData.tags" :multiple="true" dictType="sys_tags" />
```

---

### 3. UDate - 日期/时间选择

灵活的日期时间选择器，支持多种格式。

**Props**

| 属性 | 类型 | 默认值 | 说明 |
|-----|------|-------|------|
| value | String | null | 当前值 |
| disabled | Boolean | false | 禁用 |
| formatPatter | String | 'yyyy-MM-dd' | 格式模式 |
| placeholder | String | null | 占位符 |
| allowClear | Boolean | true | 显示清除按钮 |
| minValue | String | null | 最小日期 |
| maxValue | String | null | 最大日期 |
| showQuickSelect | Boolean | false | 显示快速选择 |
| defaultValue | String | null | 默认值 |

**支持格式**

| 格式 | 选择器类型 | 示例 |
|-----|----------|------|
| yyyy | 年份 | 2026 |
| yyyy-MM | 年月 | 2026-02 |
| yyyy-MM-dd | 日期 | 2026-02-26 |
| yyyy-MM-dd HH:mm | 日期时间 | 2026-02-26 14:30 |
| yyyy-MM-dd HH:mm:ss | 完整时间 | 2026-02-26 14:30:00 |
| HH:mm | 时间 | 14:30 |
| HH:mm:ss | 时间秒 | 14:30:00 |

**示例**

```vue
<!-- 日期选择 -->
<u-date v-model:value="formData.birthDate" formatPatter="yyyy-MM-dd" />

<!-- 日期时间选择 -->
<u-date v-model:value="formData.startTime" formatPatter="yyyy-MM-dd HH:mm" />

<!-- 时间选择 -->
<u-date v-model:value="formData.workStart" formatPatter="HH:mm" />

<!-- 带快速选择 -->
<u-date v-model:value="formData.date" :showQuickSelect="true" />
```

---

### 4. UDateRange - 日期范围选择

**Props**

| 属性 | 类型 | 默认值 | 说明 |
|-----|------|-------|------|
| value | Array | null | [开始日期, 结束日期] |
| disabled | Boolean | false | 禁用 |
| formatPatter | String | 'yyyy-MM-dd' | 格式 |
| showQuickSelect | Boolean | false | 快速选择 |

**示例**

```vue
<u-date-range v-model:value="formData.dateRange" :showQuickSelect="true" />
```

---

### 5. USwitch - 开关

布尔值开关选择器。

**Props**

| 属性 | 类型 | 默认值 | 说明 |
|-----|------|-------|------|
| value | Boolean | null | 当前值 |
| defaultValue | String/Boolean | null | 默认值 |
| disabled | Boolean | false | 禁用 |

**示例**

```vue
<u-switch v-model:value="formData.isActive" />
<u-switch v-model:value="formData.enabled" defaultValue="true" />
```

---

### 6. UUpload - 文件上传

支持图片、文件上传，支持预览和编辑。

**Props**

| 属性 | 类型 | 默认值 | 说明 |
|-----|------|-------|------|
| value | String | null | 文件组ID |
| picture | Boolean | null | 图片模式 |
| online | Boolean | null | 在线编辑 |
| disabled | Boolean | false | 禁用 |
| fileCount | Number | 5 | 最大文件数 |
| maxSize | Number | 配置 | 最大大小(MB) |
| accepts | Array | 配置 | 接受类型 |
| isEdit | Boolean | false | 图片编辑 |
| isSort | Boolean | false | 排序 |
| multiple | Boolean | false | 多选 |

**Events**

| 事件 | 参数 | 说明 |
|-----|-----|------|
| update:value | groupId | 值变化 |
| uploadFinish | groupId, fileList | 上传完成 |
| fileListChange | fileList | 文件列表变化 |

**示例**

```vue
<!-- 文件上传 -->
<u-upload v-model:value="formData.attachments" :fileCount="10" />

<!-- 图片上传 -->
<u-upload v-model:value="formData.avatar" :picture="true" :isEdit="true" />
```

---

### 7. UArea - 地区选择

省市区级联选择。

**Props**

| 属性 | 类型 | 默认值 | 说明 |
|-----|------|-------|------|
| value | Object | {id: ''} | 选中地区 |
| disabled | Boolean | false | 禁用 |
| showRank | Number | 3 | 级别: 1省 2市 3区 |
| freeChoice | Boolean | false | 自由选择 |

**示例**

```vue
<u-area v-model:value="formData.area" :showRank="2" />
```

---

### 8. UTreeSelect - 树形选择

树形菜单选择，支持从表单或API加载数据。

**Props**

| 属性 | 类型 | 默认值 | 说明 |
|-----|------|-------|------|
| value | Object | {id: ''} | 选中值 |
| disabled | Boolean | false | 禁用 |
| formNo | String | '' | 表单编号 |
| data | Array | [] | 树形数据 |
| parentId | String | '0' | 根节点ID |
| onlyLeafSelect | Boolean | false | 仅叶子可选 |

**示例**

```vue
<u-tree-select v-model:value="formData.dept" formNo="sys_org" />
```

---

### 9. ULngLatSelect - 经纬度/地图选点

基于天地图的坐标选择组件。

**Props**

| 属性 | 类型 | 默认值 | 说明 |
|-----|------|-------|------|
| format | String | 'DMS' | 格式: DD(小数)/DMS(度分秒) |
| longitude | String/Number | '' | 经度 |
| latitude | String/Number | '' | 纬度 |
| disabled | Boolean | false | 禁用 |
| buttonText | String | '地图选择' | 按钮文字 |
| DDRetainDecimals | Number | 6 | DD格式小数位 |

**Events**

| 事件 | 参数 | 说明 |
|-----|-----|------|
| update:longitude | value | 经度变化 |
| update:latitude | value | 纬度变化 |
| textAddressShow | address | 逆地理编码地址 |

**示例**

```vue
<u-lng-lat-select 
  format="DD"
  v-model:longitude="formData.longitude"
  v-model:latitude="formData.latitude"
  @textAddressShow="(addr) => formData.address = addr"
/>
```

---

### 10. UIconSelect - 图标选择

Font Awesome 图标选择器。

**Props**

| 属性 | 类型 | 默认值 | 说明 |
|-----|------|-------|------|
| value | String | '' | 选中图标 |
| disabled | Boolean | false | 禁用 |
| placeholder | String | '' | 占位符 |

**示例**

```vue
<u-icon-select v-model:value="formData.icon" placeholder="请选择图标" />
```

---

### 11. UYesNo - 是否选择

简单的是/否二值选择。

**Props**

| 属性 | 类型 | 默认值 | 说明 |
|-----|------|-------|------|
| value | String | null | 当前值 ('1'是/'0'否) |
| disabled | Boolean | false | 禁用 |
| formType | String | 'select' | 表单类型 |

**示例**

```vue
<u-yes-no v-model:value="formData.isMarried" />
```

---

## 二、系统选择组件

### UUserSelect - 用户选择

单用户选择器。

```vue
<u-user-select v-model:value="formData.userId" />
```

### UUsersSelect - 多用户选择

多用户选择器。

```vue
<u-users-select v-model:value="formData.userIds" />
```

### UOfficeSelect - 机构选择

部门/机构选择器。

```vue
<u-office-select v-model:value="formData.deptId" />
```

---

## 三、富文本与编辑器

### UTinymce - 富文本编辑器

**Props**

| 属性 | 类型 | 默认值 | 说明 |
|-----|------|-------|------|
| value | String | null | 编辑器内容 |
| disabled | Boolean | false | 禁用 |
| height | Number | 420 | 高度(px) |

**示例**

```vue
<u-tinymce v-model:value="formData.content" :height="500" />
```

### UJson - JSON编辑器

**Props**

| 属性 | 类型 | 默认值 | 说明 |
|-----|------|-------|------|
| value | Object/Array/String | null | JSON数据 |
| disabled | Boolean | false | 只读模式 |

**示例**

```vue
<u-json v-model:value="formData.metadata" />
```

---

## 四、容器组件

### UForm - 表单容器

核心表单容器，支持主子表、选项卡、锚点布局。

**Props**

| 属性 | 类型 | 默认值 | 说明 |
|-----|------|-------|------|
| value | Object | {} | 表单数据 |
| disabled | Boolean | false | 禁用表单 |
| labelWidth | Number | 150 | 标签宽度 |
| formNo | String | '' | 表单编号 |
| subTables | Array | [] | 子表配置 |
| subTableType | String | 'top' | 子表位置: top/bottom/anchor |

**Methods**

| 方法 | 返回 | 说明 |
|-----|-----|------|
| validateFields() | Promise | 验证表单 |
| getFormData() | Promise | 获取数据 |
| setData(data) | void | 设置数据 |

**示例**

```vue
<template>
  <u-form ref="formRef" v-model:value="formData" :disabled="isView">
    <a-row :gutter="24">
      <a-col :span="12">
        <a-form-item label="名称" name="name" :rules="[{required: true}]">
          <u-input v-model:value="formData.name" />
        </a-form-item>
      </a-col>
    </a-row>
  </u-form>
</template>

<script setup>
const formRef = ref(null)
const formData = ref({})

const submit = async () => {
  const data = await formRef.value.validateFields()
  console.log('表单数据:', data)
}
</script>
```

### UFormTitle - 表单标题

表单区域分隔标题。

```vue
<u-form-title>基本信息</u-form-title>
```

---

## 五、动态表单

### UDynamicFormItem - 动态表单项

根据配置动态渲染对应组件。

**支持的 formType**

| formType | 组件 |
|----------|------|
| input | UInput |
| select | USelect |
| date | UDate |
| area | UArea |
| upload | UUpload |
| tree-select | UTreeSelect |
| user-select | UUserSelect |
| users-select | UUsersSelect |
| office-select | UOfficeSelect |
| icon-select | UIconSelect |
| richText | UTinymce |
| switch | USwitch |

**示例**

```vue
<u-dynamic-form-item 
  :formType="field.showType"
  v-model:value="formData[field.name]"
  :props="field.props"
  :disabled="disabled"
/>
```

---

## 六、特殊组件

### UTimelinePicker - 时间轴选择器

用于会议室预约等场景的时间段可视化选择。

**Props**

| 属性 | 类型 | 默认值 | 说明 |
|-----|------|-------|------|
| startTime | String | '' | 开始时间 |
| endTime | String | '' | 结束时间 |
| meetingRoomId | String | '' | 会议室ID |
| disabled | Boolean | false | 禁用 |
| startHour | Number | 8 | 工作开始小时 |
| endHour | Number | 21 | 工作结束小时 |
| slotMinutes | Number | 30 | 时间槽分钟数 |

**示例**

```vue
<u-timeline-picker 
  v-model:startTime="booking.startTime"
  v-model:endTime="booking.endTime"
  :meetingRoomId="roomId"
  :startHour="8"
  :endHour="21"
/>
```

---

## 七、组件全局注册

所有 `U` 前缀组件已在 `main.js` 中全局注册，可直接在模板中使用，无需单独引入。

---

## 八、最佳实践

### 1. 表单数据流

```vue
<template>
  <u-form ref="formRef" v-model:value="formData">
    <a-form-item label="状态">
      <u-select v-model:value="formData.status" dictType="sys_status" />
    </a-form-item>
    <a-form-item label="日期">
      <u-date v-model:value="formData.date" formatPatter="yyyy-MM-dd" />
    </a-form-item>
  </u-form>
</template>

<script setup>
const formRef = ref()
const formData = ref({
  status: '',
  date: ''
})

// 验证并获取数据
const getData = () => formRef.value.validateFields()

// 重置表单
const reset = () => {
  formData.value = { status: '', date: '' }
}
</script>
```

### 2. 多数据源选择

```vue
<!-- 字典 -->
<u-select type="dict" dictType="sys_status" />

<!-- URL -->
<u-select type="url" dataUrl="/api/list" valueField="id" textField="name" />

<!-- 本地数据 -->
<u-select :optionData="options" valueField="value" textField="label" />
```

### 3. 地图选点与地址联动

```vue
<a-form-item label="地址">
  <u-input v-model:value="formData.address" />
</a-form-item>
<a-form-item label="坐标">
  <u-lng-lat-select 
    format="DD"
    v-model:longitude="formData.longitude"
    v-model:latitude="formData.latitude"
    @textAddressShow="(addr) => formData.address = addr"
  />
</a-form-item>
```

### 4. 文件上传处理

```vue
<u-upload 
  ref="uploadRef" 
  v-model:value="formData.fileId" 
  :picture="false" 
  :fileCount="10"
  @uploadFinish="onUploadFinish"
/>

<script setup>
const onUploadFinish = (groupId, fileList) => {
  console.log('上传完成，文件组ID:', groupId)
  console.log('文件列表:', fileList)
}
</script>
```

---

## 九、组件依赖关系

```
UForm (主表单容器)
├── UDynamicFormItem (动态渲染)
│   ├── UInput
│   ├── USelect
│   ├── UDate
│   ├── UDateRange
│   ├── UArea
│   ├── UUpload
│   ├── UTreeSelect
│   ├── UUserSelect
│   ├── UIconSelect
│   ├── UTinymce
│   ├── USwitch
│   └── UJson
├── UFormTitle (区域标题)
└── 子表组件

USelect (选择框)
└── 支持 dict/table/url 多数据源

UDate (日期)
└── 支持日期/时间多格式
    └── HH:mm / HH:mm:ss (纯时间选择)
```

---

## 十、更新日志

- **2026-02-26**: 新增 `UDate` 时间选择支持 (`HH:mm`, `HH:mm:ss`)
- **2026-02-26**: 新增 `USwitch` 开关组件
- **2026-02-26**: 新增 `ULngLatSelect` 地图选点组件使用说明
