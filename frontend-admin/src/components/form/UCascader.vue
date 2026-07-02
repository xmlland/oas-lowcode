<template>
	<a-cascader  :allowClear="allowClear" :showSearch="showSearch" :disabled="disabled" :options="options"
		:placeholder="currentPlaceholder" :field-names="{label:'title',value:'key',children:'children'}"
		:Cflage="Cflage"  @change="changeInput" v-model:value="currentValue"
               :getPopupContainer="getPopupContainer"
               :change-on-select="freeChoose"
		>
		<template #displayRender="{labels}">
			{{getShowCode()}} {{labels.join(' / ')}}
		</template>
	</a-cascader>
</template>

<script>
	export default {
		name: "UCascader"
	}
</script>
<script setup>
	import {onMounted, ref, watch} from "vue";
	import {
		postAction
	} from "@/api/action";
	import {
		getData,
		listToTree
	} from "@/lib/tools";

	let props = defineProps({
		// 是否需要显示行业编码
		Cflage:{
			type: Boolean,
			default: false
		},
		value: {
			type: String,
			default: undefined
		},
		disabled: {
			type: Boolean,
			default: false
		},
		allowClear: {
			type: Boolean,
			default: true
		},
		placeholder: {
			type: String,
			default: ''
		},
		showSearch: {
			type: Boolean,
			default: true
		},
		//指定的访问URL地址
		url: {
			type: String,
			default: 'dynamic/zform/datamapView?path=path&traceFlag=1&formNo=${form_no}&parentId='
		},
		postData: {
			type: Object,
			default: () => {
				return {
					status: 1
				}
			}
		},
		//默认查询的表格
		formNo: {
			type: String,
			default: 'dsc_dic_industry_detail'
		},
		//默认传递的数据
		data: {
			type: Array,
			default () {
				return []
			}
		},
		parentCode: {
			type: String,
			default: '0',
		},
		fieldNames: {
			type: Object,
			default: () => {
				return {
					title: 'name',
					key: 'id',
					parent: ['parent', 'id'],
					parents: 'parent_ids'
				}
			}
		},
    container:{
      type: String,
      default: ''
    },
    // 是否可以自由选择级联选项每一级
    freeChoose: {
      type: Boolean,
      default: false
    }
	})

	let currentPlaceholder = ref(props.disabled ? '' : props.placeholder)
	let options = ref([])

	let emits = defineEmits(['update:value', 'change'])

	const changeInput = (value, selectedOptions) => {
		if (!value || value.length === 0) {
			emits('update:value', {})
			emits('change', {})
			return
		}
		let data = value[value.length - 1],
			dataOptions = selectedOptions[value.length - 1];
		emits('update:value', data, dataOptions)
		emits('change', data, dataOptions)
	}

	let currentValue = ref(null);
	let dataList = ref([]);

	watch(() => props.value, (newVal, oldVal) => {
		if (newVal !== oldVal) {
			setCurrentValue();
		}
	})

	const loadData = () => {
		if (!!props.data && props.data.length > 0) {
			options.value = listToTree(props.data, props.parentCode, props.fieldNames, true)
		} else {
			if (props.url) {
				let currentUrl = props.url.replace("${form_no}", props.formNo);
				postAction(currentUrl, props.postData).then(res => {
					if (res.rows && res.rows.length > 0) {
						dataList.value = res.rows;
						options.value = listToTree(res.rows, props.parentCode, props.fieldNames, true);
						setCurrentValue();
					}
				})
			}
		}
	}

	const setCurrentValue = () => {
		if (props.value) {
			let filter = dataList.value.filter(item => getData(item, props.fieldNames.key) == props.value);
			if (filter.length > 0) {
				let parentIds = getData(filter[0], props.fieldNames.parents);
				if (parentIds) {
					currentValue.value = (parentIds + props.value).split(",").slice(1);
				}
			}
		} else {
			currentValue.value = null;
		}
	}

	onMounted(() => {
		loadData();
	})
	const getShowCode=()=>{
		if(props.Cflage){
			if(currentValue.value&&currentValue.value.length>0){
				return currentValue.value[ currentValue.value.length-1] +' | '
			}
		}
			return ''

	}
  const getPopupContainer = () => {
    return props.container === ''?document.body: document.getElementById(props.container)
  }

</script>
<style scoped>

</style>
