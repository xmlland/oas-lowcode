;
(function(global) {
	"use strict";
	var BootstrapTableUtil = function(getTime, isActivity, getTime2, trigger) {
		var _this = this;
		var tableGetTime = getTime2 ? getTime2 : getTime;
		_this.dom = $("#table-list" + getTime);
		_this.body = $("#body-div" + tableGetTime);
		_this.config = {
			classes: 'table table-hover',
			dataType: "json",
			method: 'GET',
			cache: false,
			pagination: true,
			pageNumber: 1,
			pageSize: 10,
			pageList: [10, 25, 50, 100],
			sidePagination: "server",
			uniqueId: "uniqueId",
			showRefresh: false,
			showToggle: false,
			showColumns: false,
			showExport: false,
			showPaginationSwitch: false,
			ajaxOptions: {
				headers: {
					token: sessionStorage.getItem('$tokenBPM')
				}
			},
			showSearch: false,
			sortOrder: "asc",
			escape: true,
			queryParams: function(param) {
				var options = _this.dom.bootstrapTable('getOptions');
				if(options.columns) {
					var columns = options.columns[0]
				}
				var data = myHelper.composeData(_this.body.find("#searchForm"));
				if(isActivity) {
					data.pageNo = (param.offset / param.limit) + 1;
					data.pageSize = param.limit
				} else {
					if(!data.pageParam){
						data.pageParam = {};
						data.pageParam.orderBy = param.sort === undefined ? "" : param.sort + " " + param.order
					}else{
						if(param.sort){
							data.pageParam.orderBy = param.sort === undefined ? "" : param.sort + " " + param.order
						}else{
							if(!data.pageParam.orderBy){
								data.pageParam.orderBy = param.sort === undefined ? "" : param.sort + " " + param.order
							}
						}
					}
					data.pageParam.pageNo = (param.offset / param.limit) + 1;
					data.pageParam.pageSize = param.limit;
				}
				if(_this.body.find(".layout-tab>li>a.active").length > 0) {
					data.status = data.status ? data.status : _this.body.find(".layout-tab>li>a.active").attr("data-status")
				}
				return data
			},
			onLoadSuccess: function(data) {
				if(data.token) {
					sessionStorage.setItem("$tokenBPM", data.token)
				}
				$.each(data.rows, function(index, obj) {
					obj.uniqueId = index
				});
				_this.dom.bootstrapTable("load", data);
				if(_this.body.find(".layui-tab-content").length > 0 && trigger) {
					_this.body.find(".layui-tab-content .v-hidden").removeClass("v-hidden")
				}
			},
			onPostBody: function() {
				$.each(_this.body.find("#toolbar").find(".btn"),function(i,o){
					$(o).removeAttr("disabled")
				})
				$.each(_this.body.find("#toolbarChild").find(".btn"),function(i,o){
					$(o).removeAttr("disabled")
				})
				_this.body.find(".hide-btn-group").each(function(i, item) {
					var width = 0;
					_this.body.find(this).find(".btn").each(function(index, obj) {
						var count = $(obj).text().length;
						if(layui.admin.lang()) {
							width += count * 8 + 10
						} else {
							width += count * 15 + 10
						}
					});
					$(this).width(width)
				});
				var data = _this.dom.bootstrapTable('getData');
				$.each(data, function(i, item) {
					if(item.hasChildren === false) {
						_this.dom.find('tbody tr').eq(i).find(".detail-icon").hide()
					}
				})
				//增加table的title的提示信息
				var hlen = parseInt(_this.dom.find("thead tr .must").attr("data-field"))+1
				$.each(_this.dom.find("tbody tr td"),function(i,o){
					//console.log((i+1)%hlen)
					if((i+1)%hlen != 0){
						$(o).attr("title",$(o).text());
					}
					//alert($(this).parent().get(0).rowIndex);输出所在行
				});
			},
			onLoadError: function(data) {
				_this.body.find('.fixed-table-body table').bootstrapTable('removeAll')
			},
			onClickRow: function(row) {},
			userIcheck: true,
			defaultBtn: true
		};
		_this.fieldColumns = [];
		_this.listDictionaryId = "";
		_this.saveUrl = "";
	};
	BootstrapTableUtil.prototype = {
		init: function(option, listDictionaryId, saveUrl) {
			this.bindFunction();
			this.appendToolBtns(option);
			this.setBtnLang();
			this.setSearchFormLang(listDictionaryId);
			
			var _this = this;
			_this.listDictionaryId = listDictionaryId;
			_this.saveUrl = saveUrl;
			var complexHeader = _this.initFieldColumns(option, listDictionaryId, saveUrl);

			var totalConfig = $.extend(true, _this.config, option);
			if(_this.fieldColumns.length > 0){
				if(complexHeader){
					totalConfig.columns.unshift(_this.fieldColumns);
				}else{
					totalConfig.columns = [_this.fieldColumns,totalConfig.columns];
				}
			}

			if(saveUrl && !totalConfig.fixedColumns){
				totalConfig.onClickRow = function(row, $element, field) {
					$element.on("ifClicked","input[type='checkbox'].required",function(){
						if($.trim($(this).val()).length > 0){
							$(this).closest("fieldset").siblings("div.tooltip").hide();
						}
					})
					if(_this.dom.attr("editTable") == "true"){
						var grepColumObj = $.grep(_this.fieldColumns, function(obj,idx){
							return obj.fieldName == field;
						})
						if(grepColumObj.length > 0){
							if(!$element.attr('editId') && _this.dom.find("tr[editId]").length == 0){
								var msg = $element.attr("msg")
								if(msg){
									LayerUtil.warning(msg)
									return;
								}
								$element.attr('editId', row.id);
								$.each(_this.fieldColumns, function(idx, columnObj ) {
									if(columnObj.componentName){
										var splitValue = columnObj.fieldName.split(".");
										myHelper.editTableRow(listDictionaryId, columnObj, $($element.find("td")[idx]), row[splitValue[0]] ? row[splitValue[0]].name ? row[splitValue[0]].name : "" : "", row[splitValue[0]] ? row[splitValue[0]].id ? row[splitValue[0]].id : "" : "");
									}else if(columnObj.fieldName == "must"){
										var $obj = $($element.find("td")[idx]);
										$obj.html("")
										var $html = `<div class="show-btn-group"><div class="y-show-btn-group" ><a class="btn save" dealname="保存">保存</a></div></div>`
										$obj.append($html)
										$obj.on("click",'.save',function () {
											_this.editTableRowSave(row);
										})
									}else{
										myHelper.editTableRow(listDictionaryId, columnObj, $($element.find("td")[idx]), row[columnObj.fieldName] ? row[columnObj.fieldName] : "");
									}
								});
								$.each(_this.body.find("#toolbar").find(".btn"),function(i,o){
									$(o).attr("disabled","disabled")
								})
								$.each(_this.body.find("#toolbarChild").find(".btn"),function(i,o){
									$(o).attr("disabled","disabled")
								})
							}else if(!$element.attr('editId')){
								if(!_this.dom.attr("settlementTable")){
									_this.editTableRowSave(row);
								}
							}
						}
						else{
						}
					}
		        }
			}
			
			return _this.dom.bootstrapTable(totalConfig);
		},
		initSon: function(option, listDictionaryId, saveUrl) {
			
			var _this = this;
			_this.listDictionaryId = listDictionaryId;
			_this.saveUrl = saveUrl;
			var complexHeader = _this.initFieldColumns(option, listDictionaryId, saveUrl);
			
			var totalConfig = $.extend(true, _this.config, option);
			if(_this.fieldColumns.length > 0){
				if(complexHeader){
					totalConfig.columns.unshift(_this.fieldColumns);
				}else{
					totalConfig.columns = [_this.fieldColumns,totalConfig.columns];
				}
			}
			
			if(saveUrl && !totalConfig.fixedColumns){
				totalConfig.onClickRow = function(row, $element, field) {
					if(_this.dom.attr("editTable") == "true"){
						var grepColumObj = $.grep(_this.fieldColumns, function(obj,idx){
							return obj.fieldName == field;
						})
						if(grepColumObj.length > 0){
							if(!$element.attr('editId') && _this.body.find("tr[editId]").length == 0){
								$element.attr('editId', row.id);
								$.each(_this.fieldColumns, function(idx, columnObj ) {
									if(columnObj.componentName){
										var splitValue = columnObj.fieldName.split(".");
										myHelper.editTableRow(listDictionaryId, columnObj, $($element.find("td")[idx]), row[splitValue[0]] ? row[splitValue[0]].name ? row[splitValue[0]].name : "" : "", row[splitValue[0]] ? row[splitValue[0]].id ? row[splitValue[0]].id : "" : "");
									}else if(columnObj.fieldName == "must"){
										var $obj = $($element.find("td")[idx]);
										$obj.html("")
										var $html = `<div class="show-btn-group"><div class="y-show-btn-group" ><a class="btn save" dealname="保存">保存</a></div></div>`
										$obj.append($html)
										$obj.on("click",'.save',function () {
											_this.editTableRowSave(row);
										})
									}else if(!$element.attr('editId')){
										myHelper.editTableRow(listDictionaryId, columnObj, $($element.find("td")[idx]), row[columnObj.fieldName] ? row[columnObj.fieldName] : "");
									}
								});
								$.each(_this.body.find("#toolbar").find(".btn"),function(i,o){
									$(o).attr("disabled","disabled")
								})
								$.each(_this.body.find("#toolbarChild").find(".btn"),function(i,o){
									$(o).attr("disabled","disabled")
								})
							}else{
								_this.editTableRowSave(row);
							}
						}
						else{
						}
					}
		        }
			}
			
			return _this.dom.bootstrapTable(totalConfig);
		},
		initProc: function(option, listDictionaryId, procTable, saveUrl) {
			if(procTable == "unsent") {
				this.appendProcToolBtns(option, listDictionaryId)
				this.setSearchFormLang(listDictionaryId);
			} else if(procTable == "done") {
//				var html = "";
//				if(layui.admin.lang()) {
//					html += '<button class="btn btn-sm btn-danger cancelBtn"><i class="fa fa-times" aria-hidden="true"></i> cancel</button><button class="btn btn-sm btn-danger backBtn"><i class="fa fa-reply" aria-hidden="true"></i> cancel</button>'
//				} else {
//					html += '<button class="btn btn-sm btn-danger cancelBtn"><i class="fa fa-times" aria-hidden="true"></i> 撤销</button><button class="btn btn-sm btn-danger backBtn"><i class="fa fa-reply" aria-hidden="true"></i> 取回</button>'
//				}
//				this.dom.closest("div").find(".proc-btn-div").append(html)
			} else if(procTable == "todoAndDoing"){
				var html = "";
				var roles = localStorage.getItem('roles');
				if(roles && roles.indexOf("dleader") != -1  ){
					if(layui.admin.lang()) {
						html += '<button class="btn btn-sm btn-primary cancelBtn"> 批量审批</button>'
					} else {
						html += '<button class="btn btn-sm btn-primary cancelBtn"> 批量审批</button>'
					}
				}
				this.dom.closest("div").find(".proc-btn-div").append(html)
			}
			this.bindFunction();
			this.setBtnLang();
			this.body.find("#Unsent").text(layui.admin.lang() ? "Unsent" : "待发");
			this.body.find("#TodoAndDoing").text(layui.admin.lang() ? "Todo" : "待办");
			this.body.find("#Done").text(layui.admin.lang() ? "Done" : "已办");
			
			var _this = this;
			_this.listDictionaryId = listDictionaryId;
			_this.saveUrl = saveUrl;
			var complexHeader = _this.initFieldColumns(option, listDictionaryId, saveUrl);
			
			var totalConfig = $.extend(true, _this.config, option);
			if(_this.fieldColumns.length > 0){
				if(complexHeader){
					totalConfig.columns.unshift(_this.fieldColumns);
				}else{
					totalConfig.columns = [_this.fieldColumns,totalConfig.columns];
				}
			}
				totalConfig.onClickRow = function(row, $element, field) {
				if(_this.dom.attr("editTable") == "true"){
					var grepColumObj = $.grep(_this.fieldColumns, function(obj,idx){
						return obj.fieldName == field;
					})
					if(grepColumObj.length > 0){
						if(!$element.attr('editId') && _this.body.find("tr[editId]").length == 0){
							$element.attr('editId', row.id);
							$.each(_this.fieldColumns, function(idx, columnObj ) {
								if(columnObj.componentName){
									var splitValue = columnObj.fieldName.split(".");
									myHelper.editTableRow(listDictionaryId, columnObj, $($element.find("td")[idx]), row[splitValue[0]] ? row[splitValue[0]].name ? row[splitValue[0]].name : "" : "", row[splitValue[0]] ? row[splitValue[0]].id ? row[splitValue[0]].id : "" : "");
								}else if(columnObj.fieldName == "must"){
									var $obj = $($element.find("td")[idx]);
									$obj.html("")
									var $html = `<div class="show-btn-group"><div class="y-show-btn-group" ><a class="btn save" dealname="保存">保存</a></div></div>`
									$obj.append($html)
									$obj.on("click",'.save',function () {
										_this.editTableRowSaveWithPorc(row,true);
									})
								}else{
									myHelper.editTableRow(listDictionaryId, columnObj, $($element.find("td")[idx]), row[columnObj.fieldName] ? row[columnObj.fieldName] : "");
								}
							});
							$.each(_this.body.find("#toolbar").find(".btn"),function(i,o){
								$(o).attr("disabled","disabled")
							})
							$.each(_this.body.find("#toolbarChild").find(".btn"),function(i,o){
								$(o).attr("disabled","disabled")
							})
						}else if(!$element.attr('editId')){
							_this.editTableRowSaveWithPorc(row,true);
							// actionUrl : admin.basePath + '/dynamic/zform/getZformWithAct?formNo=xj_kuler_registration_warehousing&id=' + id + '&procDefKey=' + initData.procDefKey,
						}
					}
					else{
					}
				}
			}
			return _this.dom.bootstrapTable(totalConfig);
		},
		appendProcToolBtns: function(option, listDictionaryId) {
			var _this = this;
			LayerUtil.ajax({
				type: "get",
				url: layui.admin.basePath + '/dynamic/zform/getProcDefList?formNo=' + listDictionaryId,
				async: false,
				success: function(res) {
					let html = "";
					let $btnHtml = $(LayerUtil.getBtnsStrFromMenuData(option.hashUrl || "").toolBtnsStr)
					$.each(res.data.procDefList, function(i, obj) {
						if(!option.hiddenDefaultBtn){
							html += '<button id="addBtn" class="btn btn-sm btn-success addBtn" data-procDefKey="' + obj.procDefKey + '"><i class="fa fa-plus"></i> ' + obj.procDefName + '</button>'
						}
						// html += '<button id="addBtn" class="btn btn-sm btn-success addBtn" data-procDefKey="' + obj.procDefKey + '"><i class="fa fa-plus"></i> ' + obj.procDefName + '</button>'
						$btnHtml.each(function(){
							$(this).attr("data-procDefKey",obj.procDefKey)
							html += $(this).prop("outerHTML")
						})
					});
					_this.dom.closest("div").find(".proc-btn-div").append(html)
				}
			})

		},
		setBtnLang: function() {
			var _this = this;
			if(layui.admin.lang()) {
				_this.body.find("#toolbar").find("#searchBtn font").text("Search");
				_this.body.find("#toolbar").find("#resetBtn font").text("Reset");
				_this.body.find("#toolbar").find("#easySearch font").text("Back");
				_this.body.find("#toolbar").find("#hardSearch font").text("Advanced Query");
				_this.body.find(".searchDiv.positionSpc").css("right", "300px")
			}
		},
		setSearchFormLang: function(dictionaryId) {
			var _this = this;
			myHelper.setFormLang(dictionaryId, _this.body.find("#searchForm"))
		},
		appendToolBtns: function(option) {
			var _this = this;
			_this.body.find("#toolbar").prepend(LayerUtil.getBtnsStrFromMenuData(option.hashUrl || "").toolBtnsStr)
		},
		bindFunction: function() {
			var _this = this;
			_this.body.find("#searchBtn").on("click", function() {
				_this.refresh()
			});
			_this.body.find("#resetBtn").on("click", function() {
				_this.body.find("#searchForm input[type='text']").val("");
				_this.body.find("#searchForm input[type='hidden']").val("");
				_this.body.find("#searchForm select[component-type='select']").val("").trigger("change")
				// _this.refresh()
			})
		},
		refresh: function() {
			this.dom.bootstrapTable('refresh')
		},
		destroy: function() {
			this.dom.bootstrapTable('destroy');
		},
		concatColumns: function(columns, columnsAjax) {
			$.each(columns, function(index, item) {
				var arr = $.grep(columnsAjax, function(n, i) {
					return n.field == item.field
				});
				if(arr.length > 0) {
					columnsAjax = $.grep(columnsAjax, function(n, i) {
						return n.field != item.field
					})
				}
			});
			return columnsAjax.concat(columns)
		},
		getSingleId: function() {
			var _this = this;
			var ids = _this.getIdSelections();
			if(ids.length != 1) {
				LayerUtil.alert(layui.admin.lang() ? 'only one' : '请选择一条数据');
				return false
			}
			return ids[0]
		},
		getMultiId: function() {
			var _this = this;
			var ids = _this.getIdSelections();
			if(ids.length == 0) {
				LayerUtil.alert(layui.admin.lang() ? 'at least one' : '请至少选择一条数据');
				return false
			}
			return ids
		},
		getIdSelections: function() {
			var _this = this;
			var ids = [];
			_this.body.find("table.table").each(function(i, obj) {
				if(!obj.closest(".fixed-columns") && !obj.closest(".fixed-columns-right")) {
					var checked = $.map($(obj).bootstrapTable('getSelections'), function(row) {
						return row.id
					});
					if(checked.length > 0) {
						ids = ids.concat(checked)
					}
				}
			});
			return ids;
		},
		getSingleRow: function() {
			var _this = this;
			var ids = _this.getRowSelections();
			if(ids.length != 1) {
				LayerUtil.alert(layui.admin.lang() ? 'only one' : '请选择一条数据');
				return false
			}
			return ids[0]
		},
		getMultiRow: function() {
			var _this = this;
			var ids = _this.getRowSelections();
			if(ids.length == 0) {
				LayerUtil.alert(layui.admin.lang() ? 'at least one' : '请至少选择一条数据');
				return false
			}
			return ids
		},
		getRowSelections: function() {
			var _this = this;
			var ids = [];
			_this.body.find("table.table").each(function(i, obj) {
				if(!obj.closest(".fixed-columns") && !obj.closest(".fixed-columns-right")) {
					var checked = $.map($(obj).bootstrapTable('getSelections'), function(row) {
						return row
					});
					if(checked.length > 0) {
						ids = ids.concat(checked)
					}
				}
			});
			return ids;
		},
		initFieldColumns:function(option, listDictionaryId, saveUrl){
			var _this = this;
			var complexHeader = false;
			layui.admin.getDictionary('page-table-cell', listDictionaryId, function(data) {
				if(data && data.length > 0) {
					$.each(option.columns, function(idx, item) {
						if($.isPlainObject(item)) {
							var fieldColumnObj = {
								"class": 'thHide',
								fieldName: item.field,
								componentType: item.componentType,
								componentId: item.componentId,
								componentDateFmt: item.componentDateFmt,
								componentName: item.componentName,
								componentActionUrl: item.componentActionUrl,
								componentSync: item.componentSync,
								componentCheck: item.componentCheck,
								componentChooseParent: item.componentChooseParent,
								tableName: item.tableName,
								searchKey: item.searchKey,
								fieldKeys: item.fieldKeys,
								fieldLabels: item.fieldLabels,
								searchLabel: item.searchLabel,
								javaField: item.javaField,
								formatter: item.formatter,
								componentRequired: item.componentRequired,
								componentDisabled: item.componentDisabled,
								changedFun: item.changedFun,
								clickFun: item.clickFun,
								inputMinLen: item.inputMinLen,
								inputMaxLen: item.inputMaxLen,
								limitNum: item.limitNum,
								needDict: item.needDict,//单选按钮中选定想要的按钮位置
								checkboxHidden: item.checkboxHidden,
								validateClass: item.validateClass,//number（数字）、mobile（手机号）、email（电子邮件）、digits（整数）、batchNumber（批号唯一性）
							};
							if(item.field) {
								var find = layui.admin.filterList(data, {
									member: item.field
								})[0];
								if(find) {
									item.title = find['memberName' + layui.admin.lang()] || item.title
								}
								if(option.classes && option.classes.indexOf("table-fiexd") > -1){
									if(!item.width) {
										fieldColumnObj.width = "150px";
									}else{
										fieldColumnObj.width = item.width;
									}
								}
								_this.fieldColumns.push(fieldColumnObj);
							}else{
								if(item['class'] && item['class'].indexOf("must") > -1){
									_this.fieldColumns.push({
										"class": 'thHide',
										fieldName: "must",
										width: '45px'
									});
								}else if(item.checkbox){
									_this.fieldColumns.push({
										"class": 'thHide',
										fieldName: "checkbox",
										width: '36px'
									});
								}
							}
						}else if(Array.isArray(item)){
							complexHeader = true;
							$.each(item, function(idx2, item2) {
								if($.isPlainObject(item2)) {
									var fieldColumnObj = {
										"class": 'thHide',
										fieldName: item2.field,
										componentType: item2.componentType,
										componentId: item2.componentId,
										componentDateFmt: item2.componentDateFmt,
										componentName: item2.componentName,
										componentActionUrl: item2.componentActionUrl,
										componentSync: item2.componentSync,
										componentCheck: item2.componentCheck,
										componentChooseParent: item2.componentChooseParent,
										tableName: item2.tableName,
										searchKey: item2.searchKey,
										fieldKeys: item2.fieldKeys,
										fieldLabels: item2.fieldLabels,
										searchLabel: item2.searchLabel,
										javaField: item2.javaField,
										formatter: item2.formatter,
										componentRequired: item2.componentRequired,
										componentDisabled: item2.componentDisabled,
										changedFun: item2.changedFun,
										clickFun: item2.clickFun,
										inputMinLen: item2.inputMinLen,
										inputMaxLen: item2.inputMaxLen,
										limitNum: item2.limitNum,
										needDict: item2.needDict,//单选按钮中选定想要的按钮位置
										checkboxHidden: item2.checkboxHidden,
										validateClass: item2.validateClass,
									};
									if(item2.field) {
										var find = layui.admin.filterList(data, {
											member: item2.field
										})[0];
										if(find) {
											item2.title = find['memberName' + layui.admin.lang()] || item2.title
										}
										if(option.classes && option.classes.indexOf("table-fiexd") > -1){
											if(!item2.width) {
												fieldColumnObj.width = "150px";
											}else{
												fieldColumnObj.width = item2.width;
											}
										}
										_this.fieldColumns.push(fieldColumnObj);
									}else{
										if(item2['class'] && item2['class'].indexOf("must") > -1){
											_this.fieldColumns.push({
												"class": 'thHide',
												width: "45px",
												fieldName: "must"
											});
										}else if(item2.checkbox){
											_this.fieldColumns.push({
												"class": 'thHide',
												width: "36px",
												fieldName: "checkbox"
											});
										}
									}
								}
							})
						}
					})
				}
			});
			return complexHeader;
		},
		editTableRowSaveWithPorc:function (obj, pro) {
			var _this = this;
			var continueStatus = true;
			if(_this.dom.find("tr[editid]").length > 0){
				var $editTableForm = _this.body.find(".layui-tab-item")
				var hasParent = false;
				if($editTableForm.length >= 1){//涉及主表
					$editTableForm = _this.body.find(".layui-show").find("#editTableForm")
					hasParent = true;
				}else{
					$editTableForm = _this.body.find("#editTableForm")
				}
				if($editTableForm.attr("hasParent")){
					hasParent = true;
				}
				var validateForm = $editTableForm.validate({});
				if(validateForm.form()){
					//newObject 即为深拷贝出来的对象
					var data = myHelper.composeData($editTableForm);
					if(typeof window.beforeSaveFuc == "function") {
						if(!window.beforeSaveFuc(data)){
							return;
						}
					}
					if(_this.dom.find("tr[editId]").attr("editId") != "addRowId"){
						data.id = _this.dom.find("tr[editId]").attr("editId");
					}
					if($editTableForm.find("table[editTable]").attr("save") != "false" && hasParent){
						data.parent = {
							id: obj.parent.id
						}
					}
					data.formNo = _this.listDictionaryId;
					if(pro){//带流程
						LayerUtil.ajax({
							url : layui.admin.basePath + '/dynamic/zform/getZformWithAct?formNo='+_this.listDictionaryId+'&id=&procDefKey='+obj.procDefKey,
							type : 'get',
							shade: true,
							async: false,
							success : function(res) {
								data.act = $.extend(true , {} , res.data.data.act)
								data.act.param = "{\"button\":\"暂存\",\"button_EN\":\"Temporary storage\",\"type\":\"save\",\"flag\":\"save\"}"
								data.act.flag = ""
								data.act.comment = ""
								data.procDefKey = obj.procDefKey
								data.procInsId = obj.procInsId
								data.tempLoginName = obj.tempLoginName
								data.tempNodeKey = obj.tempNodeKey
							}
						});
					}
					if($editTableForm.find("table[editTable]").attr("save") == "false"){
						LayerUtil.success("保存成功！");
						$.each(_this.body.find("#toolbar").find(".btn"),function(i,o){
							$(o).removeAttr("disabled")
						})
						$.each(_this.body.find("#toolbarChild").find(".btn"),function(i,o){
							$(o).removeAttr("disabled")
						})
						var rowData = data;
						$.each(rowData, function(itemKey, itemValue) {
							if(/^c((0[1-9]{1})|(10))List$/.test(itemKey)) {
								rowData[itemKey.replace("List","")] = itemValue.join(",");
							}
							else if(/^g(0[1-5]{1})$/.test(itemKey)){
								$.each(itemValue, function(itemValueKey,itemValueValue) {
									if(itemValueKey != "id"){
										rowData[itemKey].name = itemValueValue;
									}
								});
							}
						});
						_this.dom.bootstrapTable('updateRow', {
							index: _this.dom.find("tr[editid]").index(),
							row: rowData
						});
						_this.dom.find("tr[editId]").removeAttr("editId");
						if(typeof window.saveCallBack == "function") {
							window.saveCallBack();
						}
						return false;
					}
					if(data.id == null || data.id == "" || data.id.includes("new_")){
						data.id = null
						if($editTableForm.find("table[editTable]").attr("settlementTable")){
							data = $.extend(obj,data)
						}
					}
					LayerUtil.ajax({
						url : layui.admin.basePath + _this.saveUrl,
						type : 'post',
						data : JSON.stringify(data),
						shade: true,
						async: false,
						success : function(res) {
							LayerUtil.success(res[LayerUtil.getMsgLang()]);
							$.each(_this.body.find("#toolbar").find(".btn"),function(i,o){
								$(o).removeAttr("disabled")
							})
							$.each(_this.body.find("#toolbarChild").find(".btn"),function(i,o){
								$(o).removeAttr("disabled")
							})
							var rowData = data;
							delete rowData.id;
							delete rowData.formNo;
							$.each(rowData, function(itemKey, itemValue) {
								if(/^c((0[1-9]{1})|(10))List$/.test(itemKey)) {
									rowData[itemKey.replace("List","")] = itemValue.join(",");
								}
								else if(/^g(0[1-5]{1})$/.test(itemKey)){
									$.each(itemValue, function(itemValueKey,itemValueValue) {
										if(itemValueKey != "id"){
											rowData[itemKey].name = itemValueValue;
										}
									});
								}
							});
							rowData.id = res.data.entityId
							_this.dom.bootstrapTable('updateRow', {
								index: _this.dom.find("tr[editid]").index(),
								row: rowData
							});
							_this.dom.find("tr[editId]").removeAttr("editId");
							if(typeof window.saveCallBack == "function") {
							   window.saveCallBack();
							}
							/*console.log(rowData)
							var allTableData = _this.dom.bootstrapTable('getData');//获取表格的所有内容行
						   	console.log(allTableData);
							return false;*/

							/*$.each(_this.fieldColumns, function(idx, columnObj ) {
								if(columnObj.fieldName != "checkbox" && columnObj.fieldName != "must"){
									var $td = $(_this.body.find("tr[editId]").find("td")[idx]);
									if(columnObj.componentName){
										var splitValue = columnObj.fieldName.split(".");

										if(columnObj.javaField){
											var splitJavaField = columnObj.javaField.split("|");
											if(typeof columnObj.formatter == "function"){
												if(data[splitValue[0]].id){
													$td.html(columnObj.formatter(data[splitValue[0]].id));
												}else{
													$td.html("");
												}
											}else{
												if(data[splitValue[0]][splitJavaField[1]]){
													$td.html(data[splitValue[0]][splitJavaField[1]]);
												}else{
													$td.html("");
												}
											}
										}else{
											if(typeof columnObj.formatter == "function"){
												$td.html(columnObj.formatter(data[splitValue[0]].id));
											}else{
												$td.html(data[splitValue[0]].name);
											}
										}
									}else{
										if(typeof columnObj.formatter == "function"){
											if(data[columnObj.fieldName]){
												$td.html(columnObj.formatter(data[columnObj.fieldName]));
											}
											else if(Array.isArray(data[columnObj.fieldName+"List"])){
												$td.html(columnObj.formatter(data[columnObj.fieldName+"List"].join(",")));
											}
											else{
												$td.html("");
											}
										}else{
											if(data[columnObj.fieldName]){
												$td.html(data[columnObj.fieldName]);
											}
											else{
												$td.html("");
											}
										}
									}
								}
							});
							_this.body.find("tr[editId]").removeAttr("editId");*/
						}
					});
				}else{
					continueStatus = false;
				}
			}
			return continueStatus;
		},
		editTableRowSave:function(row){
			this.editTableRowSaveWithPorc(row,false)
		},
	};
	if(typeof module !== 'undefined' && module.exports) {
		module.exports = BootstrapTableUtil
	}
	if(typeof define === 'function') {
		define(function() {
			return BootstrapTableUtil
		})
	}
	global.BootstrapTableUtil = BootstrapTableUtil;
})(this);