<template>
  <u-form ref="configForm" v-model:value="currentFormItemFormModel" :label-width="100" setFormInjectKey="">
    <h3>
<!--      {{ getTextFormArray(currentFormItemFormModel.key, dynamicFormItemType, 'key') }}-->
      <div style="width: 100%" >
        排序：<u-input style="width: 80px" type="number" :step="5" :value="currentFormItemFormModel.column__formSort" @change="changeSort"/>
        类型：<u-select style="width: calc(100% - 200px)" @change="changeType" v-model:value="currentFormItemFormModel.key" value-field="key" text-field="text" :option-data="dynamicFormItemType" :allow-clear="false"/>
      </div>
    </h3>
    <u-form-title class="sqltitle">数据库配置</u-form-title>
    <a-row :gutter="16">
      <a-col :span="24">
        <a-form-item name="formItemProps__name" label="字段名" :validateFirst="true" :rules="[{ required: true, message: '请输入字段名' }]">
          <u-input :disabled="currentFormItemFormModel.type==='modalMultiSelect'" v-model:value="currentFormItemFormModel.formItemProps__name"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="formItemProps__label" label="说明" :validateFirst="true" :rules="[{ required: true, message: '请输入说明' }]">
          <u-input v-model:value="currentFormItemFormModel.formItemProps__label"/>
        </a-form-item>
      </a-col>
      <template v-if="currentFormItemFormModel.type!=='modalMultiSelect'">
        <a-col :span="24">
          <a-form-item name="column__comments_EN" label="comments" :validateFirst="true" :rules="[{ required: true, message: '请输入comments' }]">
            <u-input v-model:value="currentFormItemFormModel.column__comments_EN"/>
          </a-form-item>
        </a-col>
        <a-col :span="16">
          <a-form-item name="column__jdbcTypeReplace" label="物理类型">
            <u-select v-model:value="currentFormItemFormModel.column__jdbcTypeReplace" value-field="value" style="" text-field="text" :allow-clear="false" :option-data="jdbcTypeOptionData"/>
          </a-form-item>
        </a-col>
        <a-col :span="8" v-if="currentFormItemFormModel.column__jdbcTypeReplace==='varchar'">
          <a-form-item name="column__varcharLength" label="" :rules="[{ required: true, message: '请输入' }]">
            <u-input type="number" style="" :min="0" :step="10" v-model:value="currentFormItemFormModel.column__varcharLength"/>
          </a-form-item>
        </a-col>
        <a-col :span="4" v-if="currentFormItemFormModel.column__jdbcTypeReplace==='decimal'">
          <a-form-item name="column__decimalPrecision" label="" :rules="[{ required: true, message: '总位数' }]">
            <u-input type="number" style="" :min="1" :max="38" :step="1" v-model:value="currentFormItemFormModel.column__decimalPrecision" placeholder="总位数"/>
          </a-form-item>
        </a-col>
        <a-col :span="4" v-if="currentFormItemFormModel.column__jdbcTypeReplace==='decimal'">
          <a-form-item name="column__decimalScale" label="" :rules="[{ required: true, message: '小数位' }]">
            <u-input type="number" style="" :min="0" :max="18" :step="1" v-model:value="currentFormItemFormModel.column__decimalScale" placeholder="小数位"/>
          </a-form-item>
        </a-col>
      </template>
      <template v-else>
        <a-col :span="24">
          <a-form-item name="column__relTable" label="关系表名称" :validateFirst="true" :rules="[{ required: true, message: '请输入关系表名称' }]">
            <template #extra>
              示例：用户角色关系表 sys_user_role
            </template>
            <u-input v-model:value="currentFormItemFormModel.column__relTable"/>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item name="column__relColumn" label="关联字段" :validateFirst="true" :rules="[{ required: true, message: '请输入关联字段' }]">
            <template #extra>
              当前表id存储在关系表的哪个字段
              示例：用户角色关系表 user_id
            </template>
            <u-input v-model:value="currentFormItemFormModel.column__relColumn"/>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item name="column__relManyColumn" label="来源字段" :validateFirst="true" :rules="[{ required: true, message: '请输入来源字段' }]">
            <template #extra>
              数据来源表id存储在关系表的哪个字段
              示例：用户角色关系表 role_id
            </template>
            <u-input v-model:value="currentFormItemFormModel.column__relManyColumn"/>
          </a-form-item>
        </a-col>
      </template>
    </a-row>
    <u-form-title class="sheettitle">表单配置</u-form-title>
    <a-row :gutter="16">
      <a-col :span="24">
        <a-form-item name="colProps__span" label="宽度">
          <a-slider v-model:value="currentFormItemFormModel.colProps__span" :min="0" :max="24" :step="1" :marks="{
                        3:'3',4:'4',6:'6',12:'12',24:'24'
                      }"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="formItemProps__required" label="必填">
          <u-select form-type="radio" v-model:value="currentFormItemFormModel.formItemProps__required" value-field="value" text-field="text" :option-data="yesNoOptionData"/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="formItemProps__extra" label="extra" :validateFirst="true">
          <u-input v-model:value="currentFormItemFormModel.formItemProps__extra" placeholder=""/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="formItemProps__renderPredicate" label="渲染条件">
          <u-input :disabled="true" placeholder="点击配置" :value="currentFormItemFormModel.formItemProps__renderPredicate">
            <template #addonAfter>
              <setting-outlined @click="openRenderModal('formItemProps__renderPredicate',currentFormItemFormModel.formItemProps__renderPredicate)"/>
            </template>
          </u-input>
        </a-form-item>
      </a-col>
      <u-modal ref="renderModal" :width="1200" :custom-body-style="{height:'70vh',overflow:'auto'}" :customOK="true" @clickOk="clickRenderModal">
        <predicate-editor :allColumns="allColumns" v-model:value="predicateValue"/>
      </u-modal>
    </a-row>
    <u-form-title class="controltitle">控件配置</u-form-title>
    <a-row :gutter="16">
      <!--基本属性-->

      <a-col :span="24">
        <a-form-item name="formControlProps__defaultValue" label="默认值" :validateFirst="true">
          <template #extra>
            <span v-if="currentFormItemFormModel.type==='date'">支持变量：${currentTime} ${currentDate} ${currentYear}</span>
            <span v-if="currentFormItemFormModel.type==='userSelect'">支持变量：${currentUser} </span>
            <span v-if="currentFormItemFormModel.type==='textInput'">支持变量：${currentUserName} </span>
            <span v-if="currentFormItemFormModel.type==='officeSelect'">支持变量：${currentCompany} ${currentOffice} </span>
            <span v-if="currentFormItemFormModel.type==='area'">支持变量：${currentCompanyArea} </span>
          </template>
          <u-input v-model:value="currentFormItemFormModel.formControlProps__defaultValue" placeholder=""/>
        </a-form-item>
      </a-col>
      <a-col :span="24">
        <a-form-item name="formControlProps__disabled" label="禁用">
          <u-select form-type="radio" v-model:value="currentFormItemFormModel.formControlProps__disabled" value-field="value" text-field="text" :option-data="yesNoOptionData"/>
        </a-form-item>
      </a-col>
      <!--input-->

      <template v-if="currentFormItemFormModel.key==='textInput'">
        <a-col :span="24">
          <a-form-item name="formControlProps__placeholder" label="placeholder" :validateFirst="true">
            <u-input v-model:value="currentFormItemFormModel.formControlProps__placeholder"/>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item name="formItemProps__unique" label="表内唯一">
            <u-select form-type="radio" v-model:value="currentFormItemFormModel.formItemProps__unique" value-field="value" text-field="text" :option-data="yesNoOptionData"/>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item name="formItemProps__validateType" label="校验规则">
            <template #extra>
              如选项不满足，可自定义正则表达式，填写正确正则即可，如：^[0-9]*$(不需要加/)，最大255字符
            </template>
            <u-select-custom-add v-model:value="currentFormItemFormModel.formItemProps__validateType" value-field="value" text-field="text"
                      :option-data="validateTypeOptionData" :show-search="true"/>
            <div v-if="currentFormItemFormModel.formItemProps__validateType.length > 0 && validateTypeOptionData.filter(item=> item.value == currentFormItemFormModel.formItemProps__validateType).length == 0">
              <span>自定义正则，正则格式{{ validator.isValidRegex(currentFormItemFormModel.formItemProps__validateType) ? '正确' : '错误' }}</span>
            </div>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item name="formItemProps__encryptType" label="加密规则">
            <template #extra>
              需要增加隐藏的字段存储加密数据的字段名，
              如mobile_phone 联系电话字段 需要加密 增加字段encrypt_mobile_phone
            </template>
            <u-select v-model:value="currentFormItemFormModel.formItemProps__encryptType" value-field="value" text-field="text"
                      :option-data="encryptTypeOptionData"/>
          </a-form-item>
        </a-col>

      </template>

      <template v-if="(currentFormItemFormModel.key==='textInput'||currentFormItemFormModel.key==='textareaInput')&&currentFormItemFormModel.column__jdbcTypeReplace==='varchar'">
        <a-col :span="24">
          <a-form-item name="formControlProps__maxlength" label="最大长度" :rules="[{ required: true, message: '请输入最大长度' }]">
            <u-input type="number" :min="0" :step="10" v-model:value="currentFormItemFormModel.formControlProps__maxlength"/>
          </a-form-item>
        </a-col>
      </template>
      <template v-if="currentFormItemFormModel.key==='textareaInput'">
        <!--                        TODO 可以配置高度-->
      </template>

      <template v-if="currentFormItemFormModel.key==='numberInput'||currentFormItemFormModel.key==='digitsInput'">
        <a-col :span="24">
          <a-form-item name="formControlProps__min" label="最小值">
            <u-input :type="currentFormItemFormModel.formItemProps__validateType" :max="currentFormItemFormModel.formControlProps__max" :step="10"
                     v-model:value="currentFormItemFormModel.formControlProps__min"/>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item name="formControlProps__max" label="最大值">
            <u-input :type="currentFormItemFormModel.formItemProps__validateType" :min="currentFormItemFormModel.formControlProps__min" :step="10"
                     v-model:value="currentFormItemFormModel.formControlProps__max"/>
          </a-form-item>
        </a-col>

      </template>

      <template v-if="currentFormItemFormModel.type==='select'">
        <a-col :span="24">
          <a-form-item name="column__selectSimple" label="系统字典">
            <u-select form-type="radio" v-model:value="currentFormItemFormModel.column__selectSimple" value-field="value" text-field="text" :option-data="yesNoOptionData"/>
          </a-form-item>
        </a-col>
        <template v-if="currentFormItemFormModel.key==='select'">
          <a-col :span="24">
            <a-form-item name="formControlProps__multiple" label="是否多选">
              <u-select form-type="radio" v-model:value="currentFormItemFormModel.formControlProps__multiple" value-field="value" text-field="text" :option-data="yesNoOptionData"/>
            </a-form-item>
          </a-col>
        </template>
        <template v-if="currentFormItemFormModel.column__selectSimple==='1'">
          <a-col :span="12">
            <a-button type="link" @click="refreshDictType">刷新字典</a-button>
            <QuickAddDict :code="formNo+'_'+currentFormItemFormModel.formItemProps__name"
                          :name="tableComment+'-'+currentFormItemFormModel.formItemProps__label"
                          @addSuccess="addSuccess"/>
          </a-col>
          <a-col :span="24">
            <a-form-item name="formControlProps__dictType" label="字典类型" :rules="[{ required: true, message: '请输入字典类型' }]">
              <u-select ref="dictSelect" form-type="select" defaultValue="" v-model:value="currentFormItemFormModel.formControlProps__dictType" type="table"
                        dict-type="sys_dictionary" valueField="code" textField="name" :format="(row)=>row.name+'('+row.code+')'"
                        tableOrderBy="a.sort asc" :tableFilterData="[{key:'a.parent_code',type:'eq',value:'data-params'}]" placeholder="请选择字典类型"/>
            </a-form-item>
          </a-col>
        </template>
        <template v-if="currentFormItemFormModel.column__selectSimple==='0'">
          <a-col :span="24">
            <a-form-item name="formControlProps__dataUrl" label="请求数据url" :rules="[{ required: false, message: '请输入url' }]">
              <u-input  v-model:value="currentFormItemFormModel.formControlProps__dataUrl"/>
            </a-form-item>
          </a-col>
          <a-col :span="24" v-if="currentFormItemFormModel.formControlProps__dataUrl">
            <a-form-item name="formControlProps__postData" label="请求数据" :rules="[{ required: false, message: '请输入请求数据' }]">
              <template #extra>
                可以使用${name}占位置
                例如：{"name":"${name}"}
              </template>
              <u-json v-model:value="currentFormItemFormModel.formControlProps__postData"/>
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item name="formControlProps__dictType" label="Table表名" :validateFirst="true" :rules="[{ required: !currentFormItemFormModel.formControlProps__dataUrl, message: '请输入Table表名' }]">
              <u-input v-model:value="currentFormItemFormModel.formControlProps__dictType"/>
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item name="formControlProps__valueField" label="存储值字段" :validateFirst="true" :rules="[{ required: true, message: '请输入存储值字段' }]">
              <u-input v-model:value="currentFormItemFormModel.formControlProps__valueField"/>
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item name="formControlProps__textField" label="显示值字段" :validateFirst="true" :rules="[{ required: true, message: '请输入显示值字段' }]">
              <u-input v-model:value="currentFormItemFormModel.formControlProps__textField"/>
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item name="formControlProps__tableOrderBy" label="排序" :validateFirst="true">
              <u-input v-model:value="currentFormItemFormModel.formControlProps__tableOrderBy"/>
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item name="" label="过滤条件">

            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item  :labelCol="{style: '20px'}" label=" " :colon="false" name="formControlProps__tableFilterData">
              <template #extra>
                支持类型：
                <a-typography-paragraph>eq, ne, lt, le, gt, ge, between, like, notLike, likeLeft, likeRight, isEmpty, isNull, isNotNull, in, notIn</a-typography-paragraph>
                配置项：
                <a-typography-paragraph>key,value,value2,type,or,children</a-typography-paragraph>
                示例：
                <a-typography-paragraph copyable>{"key": "a.enname","value": "CityUser","type": "ne"}</a-typography-paragraph>
              </template>
              <u-json v-model:value="currentFormItemFormModel.formControlProps__tableFilterData"/>
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item name="" label="format函数">

            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item  :labelCol="{style: '20px'}" label=" " :colon="false" name="formControlProps__formatFuncStr">
              <template #extra>
                用于格式化显示值，参数为当前行数据和行索引，返回值为显示值
              </template>
              (option, index)=>{
              <u-code-mirror v-model:value="currentFormItemFormModel.formControlProps__formatFuncStr"/>
              }
            </a-form-item>
          </a-col>
        </template>
      </template>

      <template v-if="currentFormItemFormModel.type==='date'">
        <a-col :span="24">
          <a-form-item name="formControlProps__formatPatter" label="日期格式">
            <u-select :allow-clear="false" v-model:value="currentFormItemFormModel.formControlProps__formatPatter" value-field="value" text-field="text"
                      :option-data="patternTypeOptionData"/>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item name="formControlProps__minValue" label="最小值">
            <template #extra>
              可以是固定值，也可以是表单中的某个字段
              例如：2023-01-01 ${scrap_time}
            </template>
            <u-input v-model:value="currentFormItemFormModel.formControlProps__minValue"/>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item name="formControlProps__maxValue" label="最大值">
            <template #extra>
              可以是固定值，也可以是表单中的某个字段
              例如：2023-12-31 ${build_date}
            </template>
            <u-input v-model:value="currentFormItemFormModel.formControlProps__maxValue"/>
          </a-form-item>
        </a-col>
      </template>

      <template v-if="currentFormItemFormModel.type==='userSelect'||currentFormItemFormModel.type==='usersSelect'">
        <!--                        选择用户-->
        <a-col :span="24">
          <a-form-item name="formControlProps__dataScope" label="数据范围">
            <u-select v-model:value="currentFormItemFormModel.formControlProps__dataScope" value-field="value" text-field="text"
                      :option-data="userSelectDataScopeOptionData"/>
          </a-form-item>
        </a-col>
        <a-col :span="24" v-if="currentFormItemFormModel.formControlProps__dataScope==='target'">
          <a-form-item name="formControlProps__targetOrgId" label="机构" :rules="[{ required: true, message: '请选择机构' }]">
            <u-office-select v-model:value="currentFormItemFormModel.formControlProps__targetOrgId"/>
          </a-form-item>
        </a-col>

        <a-col :span="24">
          <a-form-item name="formControlProps__modalWidth" label="弹窗宽度" :rules="[{ required: true, message: '请输入弹窗宽度' }]">
            <u-input type="number" :min="100" :step="100" v-model:value="currentFormItemFormModel.formControlProps__modalWidth"/>
          </a-form-item>
        </a-col>

        <a-col :span="24" v-if="currentFormItemFormModel.type==='userSelect'">
          <a-form-item name="formControlProps__hideLoginName" label="隐藏登录名">
            <u-select form-type="radio" v-model:value="currentFormItemFormModel.formControlProps__hideLoginName" value-field="value" text-field="text" :option-data="yesNoOptionData"/>
          </a-form-item>
        </a-col>

        <template v-if="currentFormItemFormModel.type==='userSelect'">
          <!--        用户选择-->
          <u-form-title class="controltitle">后端配置</u-form-title>
          <a-col :span="24">
            <a-form-item name="formControlProps__createSysUser" label="创建系统用户">
              <u-select form-type="radio" v-model:value="currentFormItemFormModel.formControlProps__createSysUser" value-field="value" text-field="text" :option-data="yesNoOptionData"/>
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item name="formControlProps__loginNameField" label="登录名字段" :validateFirst="true">
              <u-input v-model:value="currentFormItemFormModel.formControlProps__loginNameField"/>
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item name="formControlProps__userNameField" label="用户名称字段" :validateFirst="true">
              <u-input v-model:value="currentFormItemFormModel.formControlProps__userNameField"/>
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item name="formControlProps__parentOrgField" label="上级机构字段">
              <template #extra>
                为空时默认取当前用户的机构
              </template>
              <u-input v-model:value="currentFormItemFormModel.formControlProps__parentOrgField"/>
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item name="formControlProps__userRoles" label="默认角色" >
              <u-select :multiple="true" form-type="select" defaultValue="" v-model:value="currentFormItemFormModel.formControlProps__userRoles" type="table"
                        dict-type="sys_role" valueField="id" :format="(row)=>row.name+'('+row.enname+')'"
                        tableOrderBy="a.sort asc" placeholder="请选择角色"/>
            </a-form-item>
          </a-col>

        </template>
      </template>

      <template v-if="currentFormItemFormModel.type==='officeSelect'">
        <!--        机构选择-->
        <u-form-title class="controltitle">后端配置</u-form-title>
        <a-col :span="24">
          <a-form-item name="formControlProps__createSysOffice" label="创建系统机构">
            <u-select form-type="radio" v-model:value="currentFormItemFormModel.formControlProps__createSysOffice" value-field="value" text-field="text" :option-data="yesNoOptionData"/>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item name="formControlProps__parentOrgId" label="上级机构">
            <u-office-select v-model:value="currentFormItemFormModel.formControlProps__parentOrgId"/>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item name="formControlProps__orgNameField" label="机构名称字段" :validateFirst="true">
            <u-input v-model:value="currentFormItemFormModel.formControlProps__orgNameField"/>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item name="formControlProps__areaIdField" label="机构行政区字段" :validateFirst="true">
            <u-input v-model:value="currentFormItemFormModel.formControlProps__areaIdField"/>
          </a-form-item>
        </a-col>
      </template>
      <template v-if="currentFormItemFormModel.type==='area'">

        <a-col :span="24">
          <a-form-item name="formControlProps__freeChoice" label="自由选择">
            <u-select form-type="radio" v-model:value="currentFormItemFormModel.formControlProps__freeChoice" value-field="value" text-field="text" :option-data="yesNoOptionData"/>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item name="formControlProps__showRank" label="行政区级别">
            <u-select form-type="radio" v-model:value="currentFormItemFormModel.formControlProps__showRank" value-field="value" text-field="text" :option-data="areaLevelOptionData"/>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item name="formControlProps__rootAreaId" label="根行政区id" :validateFirst="true">
            <u-input v-model:value="currentFormItemFormModel.formControlProps__rootAreaId"/>
          </a-form-item>
        </a-col>
      </template>

      <template v-if="currentFormItemFormModel.type==='upload'">
        <a-col :span="24" v-if="currentFormItemFormModel.key==='upload'">
          <a-form-item name="formControlProps__acceptsStr" label="文件格式" :rules="[{ required: true, message: '请输入文件格式' }]">
            <u-select form-type="checkbox" :multiple="true" :allow-clear="false" v-model:value="currentFormItemFormModel.formControlProps__acceptsStr" value-field="value"
                      text-field="text"
                      :option-data="acceptFilesOptionData"/>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item name="formControlProps__fileCount" label="文件个数" :rules="[{ required: true, message: '请输入文件个数' }]">
            <u-input type="number" :min="1" :step="1" v-model:value="currentFormItemFormModel.formControlProps__fileCount"/>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item name="formControlProps__maxSize" label="大小(M)" :rules="[{ required: true, message: '单个大小(M)' }]">
            <u-input type="number" :min="0" :step="10" :max="config.maxFileSize" v-model:value="currentFormItemFormModel.formControlProps__maxSize"/>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item name="formControlProps__multiple" label="多选">
            <u-select form-type="radio" v-model:value="currentFormItemFormModel.formControlProps__multiple" value-field="value" text-field="text" :option-data="booleanOptionData"/>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item name="formControlProps__directory" label="文件夹上传">
            <u-select form-type="radio" v-model:value="currentFormItemFormModel.formControlProps__directory" value-field="value" text-field="text" :option-data="booleanOptionData"/>
          </a-form-item>
        </a-col>
      </template>
      <template v-if="currentFormItemFormModel.type==='modalSelect'||currentFormItemFormModel.type==='modalMultiSelect'">
        <!--                        弹出单选 弹出多选-->
        <a-col :span="24">
          <a-form-item name="formControlProps__formNo" label="Table表名" :validateFirst="true" :rules="[{ required: true, message: '请输入Table表名' }]">
<!--            <u-input v-model:value="currentFormItemFormModel.formControlProps__formNo"/>-->
<!--            <a-select v-model:value="currentFormItemFormModel.formControlProps__formNo" show-search :options="select1" @focus="listtable">
            </a-select>-->
            <u-select v-model:value="currentFormItemFormModel.formControlProps__formNo" placeholder="" type="table" dict-type="gen_table" value-field="name"
                      :format="row=>`${row.name} ${row.comments}`"/>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item name="formControlProps__targetTable" label="目标表">
            <template #extra>
              一般用于某个主表的子表只允许选择某个内容一次
              例如：监测点位的监测项目，一个点位只能选择一个项目
            </template>
            <u-select form-type="radio" v-model:value="currentFormItemFormModel.formControlProps__targetTable" value-field="value" text-field="text" :option-data="yesNoOptionData"/>
          </a-form-item>
        </a-col>
        <template v-if="currentFormItemFormModel.formControlProps__targetTable==='1'">
          <a-col :span="24">
            <a-form-item name="formControlProps__targetFormNo" label="目标表名">
              <u-input v-model:value="currentFormItemFormModel.formControlProps__targetFormNo"/>
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item name="formControlProps__targetField" label="存储字段">
              <u-input v-model:value="currentFormItemFormModel.formControlProps__targetField"/>
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item name="" label="过滤条件">

            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item  :labelCol="{style: '20px'}" label=" " :colon="false" name="formControlProps__targetFilterData">
              <template #extra>
                目标表数据过滤条件 关键是这个配置
                //TODO 添加示例
                <!--                                //TODO 添加示例-->
              </template>
              <u-json v-model:value="currentFormItemFormModel.formControlProps__targetFilterData"/>
            </a-form-item>
          </a-col>
        </template>

        <a-col :span="24">
          <a-form-item label="数据列">
            <template #extra>
              例如：配置弹出选择的数据列，点击配置按钮，弹出数据列配置弹窗
            </template>
            <u-input :disabled="true" placeholder="点击配置" :value="currentFormItemFormModel.formControlProps__allColumns.map(item=>item.title).join(',')">
              <template #addonAfter>
                <setting-outlined @click="openColumnModal"/>
              </template>
            </u-input>
          </a-form-item>
        </a-col>

        <u-modal ref="columnModal" :width="1600" :custom-body-style="{height:'70vh',overflow:'auto'}" :customOK="true" @clickOk="clickColumnModal">
          <single-table-view ref="columnModalTableView" v-model:value="currentFormItemFormModel.formControlProps__allColumns" :rowEdit="true"
                             :rowEditDefaultRow="{align: 'left',isShow: '1',isQuery: '0'}"
                             :auto-height="true"
                             :query-button="false"
                             :single-table="modalSelectColumnsEditorTable" @cellValueChange="cellValueChange">
            <template #queryFields>
              <div style="height: 100%;margin-top: 5px;">
                数据列
                <a-button type="link" @click="clearModalColumns">清空</a-button>
              </div>
              <a-select @change="changeModalColumn" v-model:value="modalColumn" style="width: 500px;margin-left: 750px;margin-top: 5px" placeholder="选择字段">
                <a-select-option v-for="(item,index) in modalColumnSelect" :key="index" :value="item.name">{{item.comments}}:{{item.name}}</a-select-option>
              </a-select>
            </template>
          </single-table-view>
        </u-modal>
        <a-col :span="24">
          <a-form-item name="formControlProps__searchLabelWidth" label="label宽度" extra="查询区域label的宽度，一般设置为最长title汉字个数*4">
            <u-input type="number" :min="20" :step="20" v-model:value="currentFormItemFormModel.formControlProps__searchLabelWidth"/>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item name="" label="查询配置">

          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item  :labelCol="{style: '20px'}" label=" " :colon="false" name="formControlProps__searchConfig">
            <template #extra>
              默认查询框为input
              示例：
              <a-typography-paragraph copyable>{"name": {"type":"select","queryType":"like","props":{}}}</a-typography-paragraph>
            </template>
            <u-json v-model:value="currentFormItemFormModel.formControlProps__searchConfig"/>
          </a-form-item>
        </a-col>
        <a-col :span="24" v-if="currentFormItemFormModel.type==='modalSelect'">
          <a-form-item name="formControlProps__nameDataIndex" label="名称字段">
            <u-select v-model:value="currentFormItemFormModel.formControlProps__nameDataIndex" value-field="dataIndex" text-field="title"
                      :option-data="currentFormItemFormModel.formControlProps__allColumns"/>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item name="formControlProps__modalWidth" label="弹窗宽度" :rules="[{ required: true, message: '请输入弹窗宽度' }]">
            <u-input type="number" :min="100" :step="100" v-model:value="currentFormItemFormModel.formControlProps__modalWidth"/>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item name="formControlProps__modalTitle" label="弹窗标题" :validateFirst="true" :rules="[{ required: true, message: '请输入弹窗标题' }]">
            <u-input v-model:value="currentFormItemFormModel.formControlProps__modalTitle"/>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item name="" label="format函数">

          </a-form-item>
        </a-col>
        <a-col :span="24" v-if="currentFormItemFormModel.type==='modalMultiSelect'">
          <a-form-item  :labelCol="{style: '20px'}" label=" " :colon="false" name="formControlProps__formatFuncStr">
            <template #extra>
              用于格式化显示值，参数为当前行数据 和 一些公用方法{translateDict}
            </template>
            (item,obj)=>{
            <u-code-mirror v-model:value="currentFormItemFormModel.formControlProps__formatFuncStr"/>
            }
          </a-form-item>
          <a-col :span="24">
            <a-form-item name="formControlProps__genTableSql" label="使用genTable的Sql">
              <template #extra>
                在编辑/查看数据时使用genTable的sql查询数据，可以显示关联查询中的字段
              </template>
              <u-select formType="radio" defaultValue="" v-model:value="currentFormItemFormModel.formControlProps__genTableSql" placeholder="" value-field="value" text-field="text" :option-data="yesNoOptionData"/>
            </a-form-item>
          </a-col>
        </a-col>
        <a-col :span="24">
          <a-form-item  name="" label="过滤条件">

          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item  :labelCol="{style: '20px'}" label=" " :colon="false" name="formControlProps__filterData">
            <template #extra>
              支持类型：
              <a-typography-paragraph>eq, ne, lt, le, gt, ge, between, like, notLike, likeLeft, likeRight, isEmpty, isNull, isNotNull, in, notIn</a-typography-paragraph>
              配置项：
              <a-typography-paragraph>key,value,value2,type,or,children</a-typography-paragraph>
              示例：
              <a-typography-paragraph copyable>{"key": "a.enname","value": "CityUser","type": "ne"}</a-typography-paragraph>
            </template>
            <u-json v-model:value="currentFormItemFormModel.formControlProps__filterData"/>
          </a-form-item>
        </a-col>

        <template v-if="currentFormItemFormModel.type==='modalSelect'">
          <a-col :span="24">
            <a-form-item name="" label="数据带入">

            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item  :labelCol="{style: '20px'}" label=" " :colon="false" name="formControlProps__formUpdateMap">
              <template #extra>
                示例：
                <a-typography-paragraph copyable>{"point_coe": "code"}</a-typography-paragraph>
                将code字段的值带入point_coe字段
              </template>
              <u-json v-model:value="currentFormItemFormModel.formControlProps__formUpdateMap"/>
            </a-form-item>
          </a-col>
        </template>
      </template>

      <template v-if="currentFormItemFormModel.type==='iconSelect'">
        <a-col :span="24">
          <a-form-item name="formControlProps__showInput" label="显示输入框">
            <u-select formType="radio" defaultValue="" v-model:value="currentFormItemFormModel.formControlProps__showInput" placeholder="" value-field="value" text-field="text" :option-data="yesNoOptionData"/>
          </a-form-item>
        </a-col>
      </template>

      <template v-if="currentFormItemFormModel.type==='serialNo'">
        <a-col :span="24">
          <a-form-item name="formControlProps__prefix" label="编号前缀" :rules="[{ required: true, message: '请输入编号前缀' }]">
            <u-input v-model:value="currentFormItemFormModel.formControlProps__prefix" placeholder="如：BK、CG等"/>
          </a-form-item>
        </a-col>
      </template>
    </a-row>

    <template v-if="currentFormItemFormModel.type!=='modalMultiSelect'">
      <u-form-title class="javatitle">java配置</u-form-title>
      <a-row :gutter="16">
        <a-col :span="24">
          <a-form-item name="column__javaType" label="Java类型" :validateFirst="true">
            <u-select v-model:value="currentFormItemFormModel.column__javaType" value-field="value" text-field="text" :allow-clear="false" :option-data="javaTypeOptionData"/>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item name="column__javaField" label="属性名称" :validateFirst="true">
            <u-input v-model:value="currentFormItemFormModel.column__javaField"/>
          </a-form-item>
        </a-col>

      </a-row>
    </template>

  </u-form>
</template>

<script>
export default {
  components: { USelectCustomAdd },
  name: "formDesignFormItemConfigForm"
}
</script>
<script setup>
import {computed, getCurrentInstance, ref, watch, nextTick} from "vue";
import * as validator from "@/lib/validator";

//import {getTextFormArray} from "@/lib/tools";
import UForm from "@/components/form/UForm";
import UFormTitle from "@/components/form/UFormTitle";
import UInput from "@/components/form/UInput";
import USelect from "@/components/form/USelect";
import UOfficeSelect from "@/components/form/sys/UOfficeSelect";
import UJson from "@/components/form/UJson";
import UModal from "@/components/modal/UModal";
import SingleTableView from "@/components/view/SingleTableView";
import config from "@/config"
import {
  yesNoOptionData,
  validateTypeOptionData,
  encryptTypeOptionData,
  javaTypeOptionData,
  jdbcTypeOptionData,
  patternTypeOptionData,
  userSelectDataScopeOptionData,
  acceptFilesOptionData,
  areaLevelOptionData,
  booleanOptionData,
} from "@/views/gen/genOptionData";
import {
  dynamicFormItemType,
} from "@/views/gen/dynamicFormItem";
import {editorTable, modalMultiSelectColumnsArr, modalSelectColumnsArr} from "@/views/gen/genTableExt/formStaticConfig";
import {postAction} from "@/api/action";
import {listDataAction} from "@/api/api";
import PredicateEditor from "@/views/gen/genTableExt/PredicateEditor";
import QuickAddDict from "@/views/gen/genTableExt/QuickAddDict";
import USelectCustomAdd from '@/components/form/USelectCustomAdd.vue';

let props = defineProps({
  allColumns: {
    type: Array,
    default() {
      return []
    }
  },
  value: {
    type: Object,
    default: () => {
      return {}
    }
  },
  formNo: {
    type: String,
    default: ''
  },
  tableComment: {
    type: String,
    default: ''
  },
})
let emits = defineEmits(["update:value","changeType","changeSort"]);
let currentFormItemFormModel = ref(props.value);
watch(() => props.value, (value) => {
  currentFormItemFormModel.value = value
})
watch(() => currentFormItemFormModel.value, (value) => {
  currentFormItemFormModel.value.column__comments_EN = currentFormItemFormModel.value.formItemProps__label
  emits("update:value", value)
},{deep: true})
let instance = getCurrentInstance();
/**
 * 刷新字典
 */
const refreshDictType = () => {
  instance.refs.dictSelect.loadDictData()
}
/**
 * 添加字典成功
 * @param val
 */
const addSuccess = (val) => {
  refreshDictType() //刷新字典
  currentFormItemFormModel.value.formControlProps__dictType = val //设置字典类型
}

let modalColumnSelect = ref(null);
let modalSelectColumnsEditorTable = computed(() => {
  let config = {}
  Object.assign(config, editorTable)
  config.data = currentFormItemFormModel.value.formControlProps__allColumns
  config.columns = currentFormItemFormModel.value.type === 'modalMultiSelect' ? modalMultiSelectColumnsArr : modalSelectColumnsArr
  return config
})

const openColumnModal = () => {
  instance.refs.columnModal.open('配置列')
    let id=null;
  genTableList.forEach(item=>{if(item.name===currentFormItemFormModel.value.formControlProps__formNo){id=item.id}})
    postAction('gen/genTable/editForm',{formNo:currentFormItemFormModel.value.formControlProps__formNo,id:id}).then(result=>{
      let rows = result.data.data
      rows.sort((a, b) => {
        return Number(a.listSort) - Number(b.listSort)
      })
      modalColumnSelect.value = rows;
    })
}

let genTableList = []
/**
 * 查出所有的gen_table
 */
const listTable = () => {
  listDataAction('gen_table').then(res => {
    genTableList = res.rows;
  })
}
listTable()

//当前选择的列
let modalColumn = ref(null);

//选择某一个列
const changeModalColumn = () => {

  let selectItem = modalColumnSelect.value.filter(item => item.name === modalColumn.value)[0]
  currentFormItemFormModel.value.formControlProps__allColumns.push({
    dataIndex: selectItem.name,
    title: selectItem.comments,
    isQuery: '0',
    isShow: '1',
    align: selectItem.align,
    dict: selectItem.dictType,
  })
}
//清空所有列
const clearModalColumns = () => {
  currentFormItemFormModel.value.formControlProps__allColumns = []
}

//列值改变
const cellValueChange = (data) => {
  let isQuery = data.record.isQuery
  let key = data.record.queryDataIndex||data.record.dataIndex
  //修改是否查询时，自动添加查询配置
  if (isQuery === '1' && !currentFormItemFormModel.value.formControlProps__searchConfig[key]) {
    let _searchConfig = {
      type: 'input',
      queryType: 'like',
      props: {}
    }
    if (data.record.dict) {
      //字典类型
      _searchConfig.type = 'select'
      _searchConfig.queryType = 'eq'
      _searchConfig.props.dictType = data.record.dict
      currentFormItemFormModel.value.formControlProps__searchConfig[key] = _searchConfig
    }
  }else{
    delete currentFormItemFormModel.value.formControlProps__searchConfig[key]
  }
}

const clickColumnModal = () => {
  instance.refs.columnModalTableView.getDataSource(true).then(() => {
    instance.refs.columnModal.close()
  })
}

const changeType = (val, options) => {
  emits("changeType", currentFormItemFormModel.value, options[0])
}
const getRef = () => {
  return instance.refs.configForm
}

let predicateField = null//当前配置条件的字段
let predicateValue = ref('')//当前配置条件的值
const openRenderModal = (field, value) => {
  predicateField = field
  predicateValue.value = value
  nextTick(() => {
    instance.refs.renderModal.open('渲染条件')
  })
}

const clickRenderModal = () => {
  currentFormItemFormModel.value[predicateField] = predicateValue.value
  instance.refs.renderModal.close()
}
/**
 * 改变当前表单项的排序值
 * @param e
 */
const changeSort = (e) => {
  emits("changeSort", {id: currentFormItemFormModel.value.__id, sort: Number(e)})
}
defineExpose({
  getRef
})
</script>
<style lang="less"  scoped>
.sqltitle{
  :deep(.business-form-title.default-icon::before){
    content: '';
    display: block;
    height: 1px;
    background: linear-gradient(90deg, rgba(16, 32, 57, 0.14), rgba(16, 32, 57, 0.5), rgba(16, 32, 57, 0.14));
    margin-right: 30px;
    flex: 1;
    opacity: .3;
  }
}
.sheettitle{
  :deep(.business-form-title.default-icon::before){
    content: '';
    display: block;
    height: 1px;
    background: linear-gradient(90deg, rgba(16, 32, 57, 0.14), rgba(16, 32, 57, 0.5), rgba(16, 32, 57, 0.14));
    margin-right: 30px;
    flex: 1;
    opacity: .3;
  }
}
.controltitle{
  :deep(.business-form-title.default-icon::before){
    content: '';
    display: block;
    height: 1px;
    background: linear-gradient(90deg, rgba(16, 32, 57, 0.14), rgba(16, 32, 57, 0.5), rgba(16, 32, 57, 0.14));
    margin-right: 30px;
    flex: 1;
    opacity: .3;
  }
}
.javatitle{

  :deep(.business-form-title.default-icon::before){
    content: '';
    display: block;
    height: 1px;
    background: linear-gradient(90deg, rgba(16, 32, 57, 0.14), rgba(16, 32, 57, 0.5), rgba(16, 32, 57, 0.14));
    margin-right: 30px;
    flex: 1;
    opacity: .3;
  }
}

</style>
