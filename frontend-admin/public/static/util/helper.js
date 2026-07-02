
;(function(global) {
    "use strict";

    var helper = function(options) {
    	var _this = this;
    };

    //覆写原型链，给继承者提供方法
    helper.prototype = {
    	//获得日期
    	getDate:function(timeStr,timeType){
      		var time = new Date(parseInt(timeStr));
			var y = time.getFullYear();
			var m = time.getMonth()+1;
			var d = time.getDate();
			var h = time.getHours();
			var mm = time.getMinutes();
			var s = time.getSeconds();
			if(!time || !y){
				return "";
			}else if(timeType === "year"){
				return y;
			}else if(timeType === "month"){
				return y+'-'+this.add0(m);
			}else if(timeType === "day"){
				return y+'-'+this.add0(m)+'-'+this.add0(d);
			}else{
				return y+'-'+this.add0(m)+'-'+this.add0(d)+' '+this.add0(h)+':'+this.add0(mm)+':'+this.add0(s);
			}
    	},
    	add0:function(m){
    		return m<10?'0'+m:m
    	},
        getpath: function(w) {
        	w = w ? w : window;
        	var pathName = w.location.pathname.substring(1);
			var webName = pathName == '' ? '' : pathName.substring(0, pathName.indexOf('/'));
			if(webName == "") {
				return w.location.protocol + '//' + w.location.host;
			} else {
				return w.location.protocol + '//' + w.location.host + '/' + webName;
			}
        },
        getWebName: function(w) {
        	w = w ? w : window;
        	var pathName = w.location.pathname.substring(1);
			var webName = pathName == '' ? '' : pathName.substring(0, pathName.indexOf('/'));
			return "/"+webName;
        },
        toLowerCase: function() {
        	return str.replace(/([A-Z])/g,"-$1").toLowerCase();
        },
        toCamel: function() {
        	var re=/-(\w)/g;
		    return str.replace(re,function ($0,$1){
		        return $1.toUpperCase();
		    });
        },
        getAjaxUrl: function(urlKey){
        	var url = this.getWebName() + "/" + this.AjaxUrlConfig.baseUrl + this.AjaxUrlConfig[urlKey].url;
        	return url;
        },
        getAjaxUrlType: function(urlKey){
        	var type = this.AjaxUrlConfig[urlKey].type;
        	return type;
        },
        /**获取特定的get请求参数信息*/
		getFixUrlParams: function(name,w) {
			w = w ? w : window;
		    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
		    var search = decodeURI(w.location.search);
		    var r = search.substr(1).match(reg);
		    if (r != null) {
		    	return unescape(r[2]);
		    }
		    return null;
		},
		/**获取所有的get请求参数信息*/
		getAllUrlParam: function(w) {
			w = w ? w : window;
		   var url = decodeURI(w.location.search); //获取url中"?"符后的字串
		   var theRequest = new Object();
		   if (url.indexOf("?") != -1) {
		      var str = url.substr(1);
		      var strs = str.split("&");
		      for(var i = 0; i < strs.length; i ++) {
		         theRequest[strs[i].split("=")[0]]=unescape(strs[i].split("=")[1]);
		      }
		   }
		   return theRequest;
		},
		composeData: function($form) {
			var $disabled = []
			$.each($form.find("*[disabled]"), function (i,o) {
				$disabled.push(o)
				$(this).removeAttr("disabled");
			})
			var serialObj = {};
			var formSerializeArray = $form.serializeArray();

			var $checkbox = $form.find('input[type=checkbox]');
	        var temp = {};
	        $.each($checkbox, function () {
	            if (!temp.hasOwnProperty(this.name)) {
	                if ($form.find("input[name='" + this.name + "']:checked").length == 0) {
	                    temp[this.name] = "";
	                    formSerializeArray.push({name: this.name, value: ""});
	                }
	            }
	        });
			$(formSerializeArray).each(function() {
				var splitName = this.name.split(".");
				if(splitName.length == 2){
					if(serialObj[splitName[0]]){
						if(serialObj[splitName[0]][splitName[1]]){
							if(!(serialObj[splitName[0]][splitName[1]] instanceof Array)){
								var arr = [];
								arr.push(serialObj[splitName[0]][splitName[1]]);//放入原来的值
								serialObj[splitName[0]][splitName[1]] = arr;
							}
							serialObj[splitName[0]][splitName[1]].push(this.value);//放入新值
						}else{
							if(/^c((0[1-9]{1})|(10))List$/.test(splitName[1])){
								serialObj[splitName[0]][splitName[1]] = [this.value];
							}else{
								serialObj[splitName[0]][splitName[1]] = this.value;
							}
						}
					}else{
						serialObj[splitName[0]] = {};
						if(/^c((0[1-9]{1})|(10))List$/.test(splitName[1])){
							serialObj[splitName[0]][splitName[1]] = [this.value];
						}else{
							serialObj[splitName[0]][splitName[1]] = this.value;
						}
					}
				}else{
					if(serialObj[this.name]){
						if(!(serialObj[this.name] instanceof Array)){
							var arr = [];
							arr.push(serialObj[this.name]);//放入原来的值
							serialObj[this.name] = arr;
						}
						serialObj[this.name].push(this.value);//放入新值
					}else{
						if(/^c((0[1-9]{1})|(10))List$/.test(this.name)){
							serialObj[this.name] = [this.value];
						}else{
							serialObj[this.name] = this.value;
						}

					}
				}
			});
			$.each($disabled, function (i,o) {
				$(this).attr("disabled","disabled");
			})
			return serialObj;
		},
		initSelect2:function(dom,w){
			var setWidth = "195px";
			if(dom.closest('form').attr("id") == "inputForm"){
				setWidth = "100%";
			}
			if(w){
				setWidth = w
			}
			if(dom.hasClass("select2-search")){
				dom.select2({
					width: setWidth,
					language: {
						noResults: function (params) {
							return "暂无数据";
						}
					}
				});
			}
			else{
				dom.select2({
					// placeholder:'请选择',
					width: setWidth,
					minimumResultsForSearch: -1
				})
			}
		},
		initIChecks:function(dom){
			dom.find(".i-checks").iCheck({
				checkboxClass: "icheckbox_square-green",
				radioClass: "iradio_square-green"
			});
		},
		disabledIchecks:function(dom){
			dom.find(".i-checks").iCheck("disable");
		},
		getSecOptionHtml:function(value,selectedValue){
			var secOptionHtml = "";
			layui.admin.getDictionary('data-params', 'sys_sec_level', function (res) {
				var loginSecLevel = layui.admin.getSessionList("secLevel");
				if(loginSecLevel){
					loginSecLevel = parseInt(loginSecLevel);
				}
    			$.each(res, function(index, term) {
    				var selected = "";
    				if(loginSecLevel && parseInt(term.member) > loginSecLevel){
    					return;
    				}
    				if(typeof selectedValue != "undefined" && selectedValue == term.member){
    					selected = " selected"
    				}
    				if(typeof value == "undefined"){
    					secOptionHtml += "<option value='"+term.member+"' "+selected+">"+term["memberName" + layui.admin.lang()]+"</option>";
    				}
    				else{
    					if(parseInt(term.member) < parseInt(value) || parseInt(term.member) == parseInt(value)){
    						secOptionHtml += "<option value='"+term.member+"' "+selected+">"+term["memberName" + layui.admin.lang()]+"</option>";
    					}
    				}
    			});
    		});
    		return secOptionHtml;
		},
		initWebUploader:function(id,componentAccept,componentUploadpath,componentFileType,successFuc){
			var _this = this;
			//附件上传下载方法
			/*$("#"+id).closest("div[component-type='upload']").on("click",".webUploadUpload",function(){
				var fileId = $(this).attr("data-fileId");
				$("#submit-form").ajaxSubmit({
					url: layui.admin.basePath + '/system/sysFile/fileDownload',
					data:{
						fileId:fileId
					},
					type:"get",
					headers: {"token" : sessionStorage.getItem('$tokenBPM')},
					success: function(data){
				  	}
				});

			});*/

			var template = $("#"+id).closest("div[component-type='upload']").attr("component-template") || $("#"+id).closest("div[component-type='uploadsec']").attr("component-template") || "";
			var callbackFuc = $("#"+id).closest("div[component-type='upload']").attr("callback");

			if($("#"+id).closest(".input-group").find(".fileListDiv").find("div").length == 0){
				$("#"+id).closest(".input-group").find("#groupIdForZipA").addClass("hidden")
			}else{
				$("#"+id).closest(".input-group").find("#groupIdForZipA").removeClass("hidden")
			}
			//附件上传删除方法
			$("#"+id).closest("div[component-type='upload']").on("click",".webUploadRemove",function(){
				var _this = this;
				var fileId = $(_this).attr("data-fileId");
				LayerUtil.ajax({
					url: layui.admin.basePath + '/system/sysFile/deleteFile?fileId=' + fileId,
					async:false,
					type: 'get',
					success: function(data){
						if(data.code == 0){
							$(_this).closest("div").remove();
							//${input}Count--;
						}else{
							LayerUtil.error(data[LayerUtil.getMsgLang()]);
						}
						if($("#"+id).closest(".input-group").find(".fileListDiv").find("div").length == 0){
							$("#"+id).closest(".input-group").find("#groupIdForZipA").addClass("hidden")
						}
					}
				});
			});
			$("#"+id).closest("div[component-type='uploadsec']").on("click",".webUploadRemove",function(){
				var _this = this;
				var fileId = $(_this).attr("data-fileId");
				LayerUtil.ajax({
					url: layui.admin.basePath + '/system/sysFile/deleteFile?fileId=' + fileId,
					async:false,
					type: 'get',
					success: function(data){
						if(data.code == 0){
							$(_this).closest("div").remove();
							//${input}Count--;
						}else{
							LayerUtil.error(data[LayerUtil.getMsgLang()]);
						}
					}
				});
			});
			if(componentAccept == "pic"){
				componentAccept =  {
	                 title: 'Images',
	                 extensions: 'gif,jpg,jpeg,png,bmp,tiff,tif,GIF,JPG,JPEG,PNG,BMP,TIFF,TIF',
	                 mimeTypes: 'image/*'
	             }
			}
			else if($.isPlainObject(componentAccept)){
				componentAccept = componentAccept;
			}
			else{
				componentAccept = {};
			}

			var groupId = "";
			var flie_count = 0;
			var cSize = 100*1024*1024;

			var text1 = "文件大小不能超过";
			var text2 = "请上传";
			var text3 = "等待上传...";
			var text4 = "上传中...";
			var text5 = "下载";
			var text6 = "删除";
			var text7 = "格式文件";
			var text8 = "所选文件类型无法上传";
			var text9 = "上传成功";
			var text10 = "上传完成，但存在上传失败的文件";
			var text11 = "上传失败";
			var text12 = "未知错误";
			if(layui.admin.lang()){
				text1 = "File size cannot exceed";
				text2 = "Please upload";
				text3 = "Waiting for uploading...";
				text4 = "Uploading...";
				text5 = "Download";
				text6 = "Delete";
				text7 = "Format file";
				text8 = "The selected file type cannot be uploaded";
				text9 = "Upload success";
				text10 = "Upload completed, but there are files failed to upload";
				text11 = "Upload failure";
				text12 = "Unknown error";
			}

			var createObj = {
				//是否允许重复的图片
				duplicate: true,
				auto: true, // 选完文件后，是否自动上传
				swf: 'static/plugin/webuploader-0.1.5/Uploader.swf', // swf文件路径
				/* server: '${ctx}/sys/user/imageUpload',// 文件接收服务端 */
				server: layui.admin.basePath + componentUploadpath,
				pick: {
					id: "#"+id
					//innerHTML: "添加APP图标 118*118"
				}, // 选择文件的按钮。可选
				formData:{
					groupId: groupId,
					chunk: flie_count
				},
				fileSingleSizeLimit: 104857600,//限制上传单个文件大小
				threads: 1,//并发数为1
				chunked: true,//分片上传-大文件的时候分片上传，默认false
	            chunkSize: cSize,
	            resize: false,
				// 只允许选择图片文件。
				accept: componentAccept,
			}
			if(componentAccept.fileNumLimit){
				createObj.fileNumLimit = componentAccept.fileNumLimit;
			}

			var uploader = WebUploader.create(createObj);

			$("#"+id).closest("div[name='importExcel']").on("click",".webUploadRemove",function(){
				var _this = this;
				var fileId = $(_this).attr("data-fileId");
				LayerUtil.ajax({
					url: layui.admin.basePath + '/system/sysFile/deleteFile?fileId=' + fileId,
					async:false,
					type: 'get',
					success: function(data){
						if(data.code == 0){
							uploader.reset()
							$(_this).closest("div").remove();
							$("#"+id).closest("div[name='importExcel']").find("input").val("")
						}else{
							LayerUtil.error(data[LayerUtil.getMsgLang()]);
						}
					}
				});
			});

			uploader.onError = function( code ) {
				var max =  104857600;//
				if(code == "F_EXCEED_SIZE"){
					code = text1 + (max/1024/1024) + "M！";
				}else if(code == "Q_EXCEED_SIZE_LIMIT"){
					code = text1 + (max/1024/1024) + "M！";
				}else if(code == "Q_TYPE_DENIED"){
					if(componentAccept == "pic"){
						code = text2+"gif,jpg,jpeg,png,bmp,tiff,tif,GIF,JPG,JPEG,PNG,BMP,TIFF,TIF！"+text7;
					}else if(componentAccept.errMsg){
						code = componentAccept.errMsg;
					}else{
						code = "格式错误"
					}
				}else if(code == "Q_EXCEED_NUM_LIMIT"){
					if(layui.admin.lang()){
						code = "The quantity exceeded the limit！";
					}else{
						code = "超出上传数量限制！";
					}

				}
				LayerUtil.error(code);
			};
			uploader.on('uploadBeforeSend', function (obj, data, headers) {
				headers.token = sessionStorage.getItem('$tokenBPM');
				groupId = $("#"+id).closest(".input-group").children("input[type=hidden]").val();
				if(groupId == ""){
					groupId = WebUploader.Base.guid();
                    $("#"+id).closest(".input-group").find("#groupIdForZipA").attr("href",
                        layui.admin.basePath+"/system/sysFile/fileDownloadZip?groupId="+groupId)
				}
				data.groupId = groupId;
				/*data.fileName = data.name;
				data.fileSize = data.size;
				data.cSize = cSize;
				data.isChunk = true;*/
				if(componentFileType=="sec"){
					data.secret = 1;
				}
			});
			uploader.on('fileQueued', function (file) {
				$("#"+id).siblings(".fileListDiv").append('<div style="color: #676A6C;" id="'+file.id+'" class="item">' +
						'<span class="text">'+text3+'</span> ' +file.name +
						'</div>');
			});
			uploader.on( 'uploadProgress', function( file, percentage ) {
				$("#"+id).siblings(".fileListDiv").find("#" + file.id).find('.text').text(text4+(percentage * 100).toFixed(2) + '%...');
				var $percent = $("#"+id).siblings(".fileListDiv").find("#" + file.id).find('.progress span');
				// 避免重复创建
				if ( !$percent.length ) {
					$percent = $('<p class="progress">' +
						'<span style="display:-moz-inline-box;display:inline-block;' +
						'background-color:#1ab394;height:20px;width: 0%;"></span></p>')
							.appendTo($("#"+id).siblings(".fileListDiv").find("#" + file.id))
							.find('span');
				}
				$percent.css( 'width', percentage * 100 + '%' );
			});
			uploader.on( 'uploadSuccess', function( file ,result) {
				if(result.token){
                	sessionStorage.setItem("$tokenBPM",result.token);
                }

				var ajaxUrl = layui.admin.basePath + componentUploadpath +"?groupId="+groupId+"&fileName="+file.name+"&fileSize="+file.size+"&chunk="+Math.ceil(file.size / (cSize))+"&isChunk=false&isEncoding=false&cSize="+cSize;
				if(componentFileType=="sec"){
					ajaxUrl += "&secret=1";
				}
				if(template){
					ajaxUrl += "&template="+template;
				}
				LayerUtil.ajax({
					url: ajaxUrl,
					/*data: JSON.stringify({
						"groupId":$("#"+id).siblings("input[type=hidden]").val(),
						"fileName":file.name,
						"fileSize":file.size,
						"chunk":Math.ceil(file.size / (cSize)),
						"isChunk":"false",
						"cSize":cSize,
						"isEncoding":false
					}),*/
					type: 'post',
					success: function(result){
						if(typeof successFuc=="function"){
							successFuc(result.data.map.groupId)
						}
	        			$("#"+id).siblings("input[type=hidden]").val(result.data.map.groupId);
						//这里手动修改webuploader的参数
						uploader.options.formData.groupId = result.data.map.groupId;

						$("#"+id).siblings(".fileListDiv").find("#" + file.id).remove();

						$.each(result.data.map.successList,function(index, data){
							var html="<div>";
							//html+="<span id='"+data.id+"'name='${input}Allow'>";
							//html+="<span><a href='javascript:;' i='${ctx}${uploadPath}/fileDownloadThumbNew?fileId=" + data.id + "' class='Slide ${input}Slide'><img id='img" + data.id + "' align='middle' style='margin-left:1%;margin-bottom:0px;margin-top:1%'	  src='${ctx}${uploadPath}/fileDownloadThumbNew?fileId=" + data.id + "' width='${width == null ? '100' : width}px' height='${height == null ? '60' : height}px'  data="+data.id+"   /></a></span>";
							if(componentFileType=="sec"){
								var secValue = $("#"+id).closest("form").find("select[name='secLevel']").val() || undefined;
								var secHtml = '<select name="secLevel-single-file" class="form-control-sec">';
								secHtml += _this.getSecOptionHtml(secValue);
			            		secHtml += '</select>';
								html = "<div style='margin-top:10px;'>"+secHtml;
							}
							html+=data.name;
							html+="&emsp;";
							if(componentFileType=="sec"){
								html+="<a class='webUploadUpload' data-fileId='" + data.id + "' href='"+layui.admin.basePath+"/system/sysFile/fileDownload?secret=1&fileId="+data.id+"' download='"+data.name+"' target='_blank'>"+text5+"</a>";
							}else{
								html+="<a class='webUploadUpload' data-fileId='" + data.id + "' href='"+layui.admin.basePath+"/system/sysFile/fileDownload?fileId="+data.id+"' download='"+data.name+"' target='_blank'>"+text5+"</a>";
							}
							html+="&nbsp;";
							html+="<a class='webUploadRemove' data-fileId='" + data.id + "'>"+text6+"</a>";
							html+="</div>";
							$("#"+id).siblings(".fileListDiv").append(html);
							//${input}Count++;
						});
						//$(".${input}Slide").simpleSlide();
						$.each(result.data.map.failList,function(index,data){
							$("#"+id).siblings(".fileListDiv").html(
									$("#"+id).siblings(".fileListDiv").html()
									+ "<span>"
									+ "<span style='color:red;'>" + data.name + "&emsp;"+text8+"</span>"
									+ "<br/>"
									+ "</span>"
							);
						});

						if(result.data.map.successList.length > 0 && result.data.map.failList.length == 0) {
							$("#"+id).closest(".input-group").find("#groupIdForZipA").removeClass("hidden")
							LayerUtil.success(text9);
						} else if(result.data.map.successList.length > 0 && result.data.map.failList.length > 0) {
							LayerUtil.warning(text10);
						} else if(result.data.map.successList.length == 0 && result.data.map.failList.length > 0) {
							LayerUtil.error(text11);
						} else {
							LayerUtil.error(text12);
						}
	        		}
				});
			});
			uploader.on( 'uploadComplete', function( file ) {
				flie_count = 0;
				if(typeof window[callbackFuc] == "function"){
					window[callbackFuc](file);
				}
			});
		},
		initSummernote: function(dom) {
			dom.summernote({
		        height: 200,
		        tabsize: 2,
		        lang: 'zh-CN',
		        disableDragAndDrop:true,
		        callbacks:{
		        	onChange:function(contents,$editable){
		        		dom.text(contents);
		        	}
		        },
		        maximumImageFileSize:307200
		    });
		},
		editTableRow: function(dictionaryId, columnObj ,$element, oldValue, oldKey) {
//			$element.attr('editStatus', true);
			layui.admin.getDictionary('page-form-field', dictionaryId, function(d) {
				$.each(d, function(idx, item) {
					//var $obj = formObj.find('[name="' + item.member + '"]');
					var $obj = $element;
					var componentType = columnObj.componentType;
					var required = columnObj.componentRequired == true ? "required" : "";
					var disabled = columnObj.componentDisabled;
					var fieldName = columnObj.fieldName;
					var componentName = columnObj.componentName;//控件别名 如user01.id
					var changedFun = columnObj.changedFun;
					var inputMinLen = columnObj.inputMinLen ? ` minlength="`+columnObj.inputMinLen+`" ` : ``;
					var	inputMaxLen = columnObj.inputMaxLen ? ` maxlength="`+columnObj.inputMaxLen+`" ` : ``;
					var	limitNum = columnObj.limitNum;
					var	validateClass = " " +(columnObj.validateClass instanceof Array? columnObj.validateClass.join(" "): "");
					if(item.member == fieldName || item.member == componentName ){
						switch(componentType) {
							case "input":
								var checkboxHidden = columnObj.checkboxHidden;
								if(checkboxHidden && $obj.closest("tr").find("input[name='"+checkboxHidden+"List']").prop("checked")){
									validateClass = validateClass.replaceAll("hidden"," ")
								}
								if(disabled != true && disabled != "true"){
									if(changedFun){
										$obj.html(`<fieldset class="set">` +
										`<input autocomplete="off" name="`+fieldName+`" onkeyup="`+fieldName+`Fun(this)" `+inputMinLen+inputMaxLen+` component-type="input" type="text" class="form-control ` + required + validateClass + `" value="`+oldValue+`"/>` +
										`<script>var `+fieldName+`Fun = `+changedFun+`</script>` +
										`</fieldset>`)
									}else{
										$obj.html(`<fieldset class="set">` +
										`<input autocomplete="off" name="`+fieldName+`" component-type="input" type="text" `+inputMinLen+inputMaxLen+` class="form-control ` + required + validateClass + `" value="`+oldValue+`"/>` +
										`</fieldset>`)
									}
								}else{
									$obj.html(`<fieldset class="set">` +
										`<input autocomplete="off" name="`+fieldName+`" readonly="readonly" component-type="input" type="text" `+inputMinLen+inputMaxLen+` class="form-control ` + required + validateClass + `" value="`+oldValue+`"/>` +
										`</fieldset>`)
								}
								break;
							case "textarea":
								if(disabled != true && disabled != "true"){
									$obj.html(`<fieldset class="set">` +
									`<textarea name="`+fieldName+'" component-type="textarea" rows="1" class="form-control ' + required + '">'+oldValue+'</textarea>' +
									`</fieldset>`);
								}
								break;
							case "select":
								if(disabled != true && disabled != "true"){
									var componentId = columnObj.componentId;
									if(componentId) {
										layui.admin.getDictionary('data-params', componentId, function(res) {
											var html = '<fieldset class="set"><select ';
											if(changedFun){
												html += ' onchange="selectFunc(this)" ';
											}
											html += ' name="'+fieldName+'" component-type="select" component-id="'+componentId+'" class="form-control ' + required + validateClass + '">';
											$.each(res, function(index, term) {
												if(term.member == oldValue){
													html += "<option value='" + term.member + "' selected>" + term["memberName" + layui.admin.lang()] + "</option>"
												}else{
													html += "<option value='" + term.member + "'>" + term["memberName" + layui.admin.lang()] + "</option>"
												}
											});
											html += '</select></fieldset>';
											if(changedFun){
												html += `<script>var selectFunc = `+changedFun+`</script>`;
											}
											$obj.html(html);
											myHelper.initSelect2($obj.find("select"))
										})
									}
								}else{
									var componentId = columnObj.componentId;
									if(componentId) {
										layui.admin.getDictionary('data-params', componentId, function(res) {
											var html = '<fieldset class="set"><select disabled="disabled" name="'+fieldName+'" component-type="select" component-id="'+componentId+'" class="form-control ' + required + '">';
											$.each(res, function(index, term) {
												if(term.member == oldValue){
													html += "<option value='" + term.member + "' selected>" + term["memberName" + layui.admin.lang()] + "</option>"
												}else{
													html += "<option value='" + term.member + "'>" + term["memberName" + layui.admin.lang()] + "</option>"
												}
											});
											html += '</select></fieldset>';
											$obj.html(html);
											myHelper.initSelect2($obj.find("select"))
										})
									}
								}
								break;
							case "checkbox":
								if(disabled != true && disabled != "true"){
									oldValue = oldValue.split(",");
									var componentId = columnObj.componentId;
									var needDict = columnObj.needDict;
									var clickFun = columnObj.clickFun || "";
									if(componentId) {
										layui.admin.getDictionary('data-params', componentId, function(res) {
											var html = '<fieldset class="set"><div name="'+fieldName+'" component-type="checkbox" component-id="yes_no" class="i-checks-div ">';
											if(needDict){
												$.each(res, function(index, term) {
													if (oldValue.indexOf(term.member) > -1) {
														html += "<input type='checkbox' name='" + item.member + "List' value='" + term.member + "' class='i-checks " + required + "' checked='checked'>"
													}else if(term.member == needDict){
														html += "<input type='checkbox' name='" + item.member + "List' value='" + term.member + "' class='i-checks " + required + "'>"
													}
												});
											}else {
												$.each(res, function (index, term) {
													if (oldValue.indexOf(term.member) > -1) {
														html += "<input type='checkbox' name='" + item.member + "List' value='" + term.member + "' class='i-checks " + required + "' checked='checked'>" + term["memberName" + layui.admin.lang()]
													} else {
														html += "<input type='checkbox' name='" + item.member + "List' value='" + term.member + "' class='i-checks " + required + "'>" + term["memberName" + layui.admin.lang()]
													}
												});
											}
											html += '</div></fieldset>';
											$obj.html(html);
											myHelper.initIChecks($obj)
											if(clickFun){
												$obj.on("ifClicked","input[type='checkbox']",clickFun)
											}
										})
									} else {
										myHelper.initIChecks($obj)
									}
								}
								break;
							case "radio":
								if(disabled != true && disabled != "true"){
									var componentId = columnObj.componentId;
									var needDict = columnObj.needDict;
									if(componentId) {
										layui.admin.getDictionary('data-params', componentId, function(res) {
											var html = '<fieldset class="set"><div name="'+fieldName+'" component-type="radio" component-id="'+componentId+'" class="i-checks-div ">';
											if(needDict){
												$.each(res, function(index, term) {
													if(required && index == 0 && !oldValue) {
														html += "<input type='radio' name='" + item.member + "' value='" + term.member + "' class='i-checks' checked='checked'>" + term["memberName" + layui.admin.lang()]
													} else {
														if(term.member == oldValue){
															html += "<input type='radio' name='" + item.member + "' value='" + term.member + "' class='i-checks' checked='checked'>" + term["memberName" + layui.admin.lang()]
														}else if(term.member == needDict){
															html += "<input type='radio' name='" + item.member + "' value='" + term.member + "' class='i-checks'>" + term["memberName" + layui.admin.lang()]
														}
													}
												});
											}else{
												$.each(res, function(index, term) {
													if(required && index == 0 && !oldValue) {
														html += "<input type='radio' name='" + item.member + "' value='" + term.member + "' class='i-checks' checked='checked'>" + term["memberName" + layui.admin.lang()]
													} else {
														if(term.member == oldValue){
															html += "<input type='radio' name='" + item.member + "' value='" + term.member + "' class='i-checks' checked='checked'>" + term["memberName" + layui.admin.lang()]
														}else{
															html += "<input type='radio' name='" + item.member + "' value='" + term.member + "' class='i-checks'>" + term["memberName" + layui.admin.lang()]
														}
													}
												});
											}
											html += '</div></fieldset>';
											$obj.html(html);
											myHelper.initIChecks($obj)
										})
									} else {
										myHelper.initIChecks($obj)
									}
								}
								break;
							case "datePicker":
								if(disabled != true && disabled != "true"){
									$obj.html('<fieldset class="set"><input name="'+fieldName+'" component-type="datePicker" readonly="readonly" onclick="WdatePicker({dateFmt:\''+columnObj.componentDateFmt+'\'})" type="text" class="Wdate form-control ' + required + '" value="'+oldValue+'"/></fieldset>')
								}else{
									$obj.html('<fieldset class="set"><input disabled="disabled" name="'+fieldName+'" component-type="datePicker" readonly="readonly" onclick="WdatePicker({dateFmt:\''+columnObj.componentDateFmt+'\'})" type="text" class="Wdate form-control ' + required + '" value="'+oldValue+'"/></fieldset>')
								}
								break;
							case "treeSelect":
								if(disabled != true && disabled != "true"){
									var textName = item.member.replace(".id", ".name");
									var componentActionUrl = columnObj.componentActionUrl;
									var componentSync = columnObj.componentSync;
									var componentCheck = columnObj.componentCheck;
									var componentChooseParent = columnObj.componentChooseParent;
									var html = '<div class="adapt" style="margin:0px"><fieldset class="set">'
										+'<div name="'+componentName+'" component-type="treeSelect" actionUrl="'+componentActionUrl+'" sync="'+componentSync+'" check="'+componentCheck+'" chooseParent="'+componentChooseParent+'" class="tag-div ">'
											+'<input name="' + item.member + '" value="'+oldKey+'" type="hidden">'
											+'<input name="' + textName + '" readonly="readonly" class="form-control ' + required + '" type="text" value="'+oldValue+'"><a class="tag-div-button"><i class="fa fa-search"></i></a>'
										+'</div>'
									+'</fieldset></div>';
									$obj.html(html);
								}
								break;
							/*case "iconSelect":
								var html = '<input name="' + item.member + '" readonly="readonly" class="form-control ' + required + '" type="text" value=""><a class="tag-div-button"><i class="fa fa-search"></i></a>';
								$obj.append(html);
								break;*/
							case "grid":
								if(disabled != true && disabled != "true"){
									var tableName = columnObj.tableName;
									var searchKey = columnObj.searchKey;
									var fieldKeys = columnObj.fieldKeys;
									var fieldLabels = columnObj.fieldLabels;
									var searchLabel = columnObj.searchLabel;
									var javaField = columnObj.javaField;
									var textName = item.member.replace(".id", "") + "." + javaField.replace(item.member + "|", "");

									var selectSimple = columnObj.selectSimple;
									var selectValuefield = columnObj.selectValuefield;
									var selectDsf = columnObj.selectDsf;
									var selectOrder = columnObj.selectOrder;

									var html = '<div class="adapt" style="margin:0px"><fieldset class="set">'
										+'<div name="'+componentName+'" component-type="grid" class="tag-div " tableName="'+tableName+'" searchKey="'+searchKey+'" fieldKeys="'+fieldKeys+'" fieldLabels="'+fieldLabels+'" searchLabel="'+searchLabel+'" javaField="'+javaField+'" selectSimple="'+selectSimple+'" selectValuefield="'+selectValuefield+'" selectDsf="'+selectDsf+'" selectOrder="'+selectOrder+'">'
									 		+'<input name="' + item.member + '" value="'+oldKey+'" type="hidden">'
									 		+'<input name="' + textName + '" readonly="readonly" class="form-control ' + required + '" type="text" value="'+oldValue+'"><a class="tag-div-button"><i class="fa fa-search"></i></a>'
										+'</div>'
									+'</fieldset></div>';
									$obj.html(html);
								}
								break;
							/*case "upload":
								var id = "uploaderBtn" + new Date().getTime();
								html = '<div class="input-group" style="width:100%"> <input name="' + item.member + '" type="hidden" class="' + required + '">';
								if(layui.admin.lang()) {
									html += '<a id="' + id + '" class="btn btn-white btn-sm" ' + disabled + '><i class="fa fa-plus"></i> Add file</a> '
								} else {
									html += '<a id="' + id + '" class="btn btn-white btn-sm" ' + disabled + '><i class="fa fa-plus"></i> 添加文件</a> '
								}
								html += '<div class="fileListDiv"></div></div>';
								$obj.append(html);
								var componentAccept = $obj.attr("component-accept");
								var componentUploadpath = $obj.attr("component-uploadpath");
								myHelper.initWebUploader(id, componentAccept, componentUploadpath);
								break;*/
							default:
								break;
						}
					}
				})
			})
		},
		setFormLang:function(dictionaryId, formObj){
	    	layui.admin.getDictionary('page-form-field', dictionaryId, function (d) {
	    		console.log(d)
	    		$.each(d, function (idx, item) {
	    			var text = item['memberName' + layui.admin.lang()];
	                var $obj = formObj.find('[name="' + item.member + '"]');
	                $obj.closest(".adapt").children("label").text(text);
	                var componentType = $obj.attr("component-type");

	                var required = $obj.hasClass("required") ? "required" : "";
	                if(required){
						$obj.closest(".adapt").children("label").html("<span style='color: red'>*</span>"+text);
					}else{
						$obj.closest(".adapt").children("label").text(text);
					}
					var disabled = $obj.attr("disabled")?"disabled" : "";
	                switch (componentType){
	                	case "input":
							$obj.attr("autocomplete","off")
	                		break;
	                	case "textarea":
	                		break;
	                	case "select":
	                		var componentId = $obj.attr("component-id");
	                		if(componentId){
	                			var loginSecLevel = layui.admin.getSessionList("secLevel");
								if(loginSecLevel){
									loginSecLevel = parseInt(loginSecLevel);
								}
								var thisName = $obj.attr("name");
	                			layui.admin.getDictionary('data-params', componentId, function (res) {
		                			var html = "";
		                			$.each(res, function(index, term) {
		                				if(thisName == "secLevel" && loginSecLevel && parseInt(term.member) > loginSecLevel){
					    					return;
					    				}
		                				html += "<option value='"+term.member+"'>"+term["memberName" + layui.admin.lang()]+"</option>";
		                			});
		                			$obj.append(html);
		                			myHelper.initSelect2($obj);
		                		});
	                		}else{
	                			myHelper.initSelect2($obj);
	                		}
							break;
	                	case "checkbox":
	                		var componentId = $obj.attr("component-id");
	                		if(componentId){
	                			layui.admin.getDictionary('data-params', componentId, function (res) {
		                			var html = "";
		                			$.each(res, function(index, term) {
		                				html += "<input type='checkbox' name='"+item.member+"List' value='"+term.member+"' class='i-checks "+required+"'>"+term["memberName" + layui.admin.lang()];
		                			});
		                			$obj.append(html);
		                			myHelper.initIChecks($obj);
		                		});
	                		}else{
	                			myHelper.initIChecks($obj);
	                		}
	                		if(disabled){
	                			myHelper.disabledIchecks($obj);
	                		}
	                		break;
	                	case "radio":
	                		var componentId = $obj.attr("component-id");
	                		if(componentId){
	                			layui.admin.getDictionary('data-params', componentId, function (res) {
		                			var html = "";
		                			$.each(res, function(index, term) {
		                				if(required && index == 0){
		                					html += "<input type='radio' name='"+item.member+"' value='"+term.member+"' class='i-checks' checked='checked'>"+term["memberName" + layui.admin.lang()];
		                				}else{
		                					html += "<input type='radio' name='"+item.member+"' value='"+term.member+"' class='i-checks'>"+term["memberName" + layui.admin.lang()];
		                				}
		                			});
		                			$obj.append(html);
		                			myHelper.initIChecks($obj);
		                		});
	                		}else{
	                			myHelper.initIChecks($obj);
	                		}
	                		if(disabled){
	                			myHelper.disabledIchecks($obj);
	                		}
	                		break;
	                	case "datePicker":
	                		break;
	                	case "treeSelect":
	                		var textName = item.member.replace(".id",".name");
	                		var html = '<input name="'+item.member+'" type="hidden">'+
								'<input name="'+textName+'" readonly="readonly" class="form-control '+required+'" type="text" value="">'+
								'<a class="tag-div-button"><i class="fa fa-search"></i></a>';
							$obj.append(html);
	                		break;
	                	case "iconSelect":
	                		var html = '<input name="'+item.member+'" readonly="readonly" class="form-control '+required+'" type="text" value="">'+
								'<a class="tag-div-button"><i class="fa fa-search"></i></a>';
							$obj.append(html);
	                		break;
	                	case "grid":
	                		var javaField = $obj.attr("javaField");
	                		var textName = item.member.replace(".id","") + "."+ javaField.replace(item.member+"|","");
	                		var html = '<input name="'+item.member+'" type="hidden">'+
                				'<input name="'+textName+'" readonly="readonly" class="form-control '+required+'" type="text" value="">'+
								'<a class="tag-div-button"><i class="fa fa-search"></i></a>';
							$obj.append(html);
	                		break;
	                	case "upload":
	                		var id = "uploaderBtn"+new Date().getTime();
		                	html = '<div class="input-group" style="width:100%"> '+
		                		'<input name="'+item.member+'" type="hidden" class="'+required+'">';
		                	if(layui.admin.lang()){
		                		html += '<a id="'+id+'" class="btn btn-white btn-sm" '+disabled+'><i class="fa fa-plus"></i> Add file</a> ';
		                	}else{
		                		html += '<a id="'+id+'" class="btn btn-white btn-sm" '+disabled+'><i class="fa fa-plus"></i> 添加文件</a> ';
		                	}
                            html += '<a id="groupIdForZipA" class="hidden" style="height: 5px;position: absolute;left: 95px;top: 6px;" href="" target="_blank">全部下载</a>';
			                html +=	'<div class="fileListDiv"></div>'+'</div>';
							$obj.append(html);
							var componentAccept = $obj.attr("component-accept");
							var componentUploadpath = $obj.attr("component-uploadpath");
							if(componentAccept && componentAccept != "pic"){
								componentAccept = JSON.parse(componentAccept);
							}
							myHelper.initWebUploader(id,componentAccept,componentUploadpath);
	                		break;
	                	case "uploadsec":
	                		var id = "uploaderBtn"+new Date().getTime();
		                	html = '<div class="input-group" style="width:100%"> '+
		                		'<input name="'+item.member+'" type="hidden" class="'+required+'">';
		                	if(layui.admin.lang()){
		                		html += '<a id="'+id+'" class="btn btn-white btn-sm" '+disabled+'><i class="fa fa-plus"></i> Add file</a> ';
		                	}else{
		                		html += '<a id="'+id+'" class="btn btn-white btn-sm" '+disabled+'><i class="fa fa-plus"></i> 添加文件</a> ';
		                	}
			                html +=	'<div class="fileListDiv"></div>'+'</div>';
							$obj.append(html);
							var componentAccept = $obj.attr("component-accept");
							var componentUploadpath = $obj.attr("component-uploadpath");
							if(componentAccept && componentAccept != "pic"){
								componentAccept = JSON.parse(componentAccept);
							}
							myHelper.initWebUploader(id,componentAccept,componentUploadpath,"sec");
	                		break;
	                	case "summernote":
							myHelper.initSummernote($obj);
							if(disabled) {
								$obj.summernote("disable");
							}
							break;
						case "input-bh":
							break;
						case "textarea-hq":
							var disCSS = "";
							if(disabled){
								disCSS = "hide";
							}
							html = '<div class="'+disCSS+'" style="position: absolute;top: -25px;right: 0px;">'+
								'<a class="hq-btn"><i class="fa fa-commenting"></i> 常用语</a>'+
								'<div class="hq-div">'+
									'<div>同意</div>'+
									'<div>不同意</div>'+
								'</div>'+
							'</div>';
							html += '<textarea rows="3" name="temp'+item.member+'" class="form-control '+disCSS+'" type="text" value=""> </textarea> '
							html += '<div name="show'+item.member+'" style="padding: 6px 12px;" ></div>'
							$obj.append(html);
							break;
						case "textarea-qf":
							var disCSS = "";
							if(disabled){
								disCSS = "hide";
							}
							html = '<div class="qf-div '+disCSS+'">'+
									'<a class="qf-btn-qf">签发</a>'+
									'<a class="qf-btn-clear">清空</a>'+
								'</div>';
							$obj.closest("div.adapt").append(html);
							break;
	                	default:
	                		break;
	                }
	    		});
			});
	    },
	    getValueFromDictionary:function(dictionaryId,value){
	    	var arr = [];
	    	layui.admin.getDictionary('data-params', dictionaryId, function (res) {
    			$.each(res, function(index, term) {
    				if(term.member == value){
    					arr.push(term["memberName" + layui.admin.lang()]);
    				}
    			});
    		});
			return arr.join("，");
	    },
	    formParamsToObject:function(params){
			var paramArr = params.split('&');
			var res = {};
			for(var i = 0;i<paramArr.length;i++){
				var str = paramArr[i].split('=');
				var regBegin = /^beginD.*/;
				var regEnd = /^endD.*/;
				if(regBegin.test(str[0]) || regEnd.test(str[0])){
					if(str[1] !== ''){
						res[str[0]]=str[1];
					}
				}else{
					res[str[0]]=str[1];
				}
			}
			return res;
		},
        toDecimal4:function (params, n) {
            var f = 0.00;
			try{
				f = parseFloat(params)
			}catch(e){
				f = 0.00;
			}
            var nn = 10000;
			if(n && n > 0){
				nn = Math.pow(10,n)
			}else{
				n = 4
			}
            if (isNaN(f)) {
                return "0.0000";
            }
            var f = Math.round(params*nn)/nn;
            var s = f.toString();
            var rs = s.indexOf('.');
            if (rs < 0) {
                rs = s.length;
                s += '.';
            }
            while (s.length <= rs + n) {
                s += '0';
            }
            return s;
        },
		escapeHTML: function escapeHTML(text) {
			if (typeof text === 'string') {
				return text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#039;').replace(/`/g, '&#x60;');
			}
			return text;
		},
    };

    //兼容CommonJs规范
    if (typeof module !== 'undefined' && module.exports) module.exports = helper;

    //兼容AMD/CMD规范
    if (typeof define === 'function') define(function() { return helper; });

    //注册全局变量，兼容直接使用script标签引入该插件
    global.helper = helper;

//this，在浏览器环境指window，在nodejs环境指global
//使用this而不直接用window/global是为了兼容浏览器端和服务端
//将this传进函数体，使全局变量变为局部变量，可缩短函数访问全局变量的时间
})(this);
const encryptUrl = url => {
	if (!url||url.indexOf('?')<0) return url
	let serverPublicKey = sessionStorage.getItem('$serverPublicKey') || "";
	let urlParameters = getURLParameters(url);
	let _url = url.split('?')[0]
	let isFirst = true
	for (let key in urlParameters) {
		_url += (isFirst ? '?' : '&') + key + '=' + Sm2Encrypt(decodeURIComponent(urlParameters[key]), serverPublicKey)
		isFirst = false
	}
	return  _url
}

const getURLParameters = url => {
	return url.match(/([^?=&]+)(=([^&]*))/g)
		.reduce((a, v) => (a[v.slice(0, v.indexOf('='))] = v.slice(v.indexOf('=') + 1), a), {}
		);
}
/**
 * 生成SM2密钥对
 * @return {*}
 * @constructor
 */
 const Sm2GenerateKeyPairHex = () => {
	return sm2.generateKeyPairHex();
}

/**
 * 验证SM2公钥格式
 * SM2公钥应为128位十六进制字符（64字节）或130位带04前缀
 * @param {string} publicKey - 待验证的公钥
 * @return {boolean} 是否有效
 */
const isValidSm2PublicKey = (publicKey) => {
	if (!publicKey || typeof publicKey !== 'string') {
		return false;
	}
	const hexPattern = /^[0-9a-fA-F]+$/;
	if (!hexPattern.test(publicKey)) {
		return false;
	}
	if (publicKey.length === 128) {
		return true;
	}
	if (publicKey.length === 130 && publicKey.startsWith('04')) {
		return true;
	}
	return false;
}

/**
 * Sm2加密数据
 * @param {any} msgString - 需加密的数据
 * @param {string} publicKey - 加密公钥
 * @return {string} 加密后的字符串
 */
 const Sm2Encrypt = (msgString, publicKey) => {
	if (msgString == null || msgString === '') {
		return msgString;
	}

	// 加密前验证公钥格式
	if (!isValidSm2PublicKey(publicKey)) {
		console.error('[SM2] Invalid public key format:', publicKey?.substring(0, 20) + '...');
		throw new Error('Invalid SM2 public key format');
	}

	let msg = msgString;
	if (typeof (msgString) === "object") {
		msg = JSON.stringify(msgString);
	}else if (typeof (msgString) === "boolean") {
		msg = msgString? 'true' : 'false';
	}else if (typeof (msgString) === "number") {
		msg = msgString + '';
	}else if (typeof (msgString) !== "string") {
		msg = msgString.toString();
	}

	// 1 - C1C3C2；	0 - C1C2C3；	默认为1
	let cipherMode = 1; // 特别注意,此处前后端需保持一致
	// 加密结果
	let encryptData = sm2.doEncrypt(msg, publicKey, cipherMode);
	// 加密后的密文前需要添加04，后端才能正常解密
	return '04' + encryptData;
}

/**
 * Sm2解密数据
 * @param {any} enStr - 待解密的数据
 * @param {string} privateKey - 解密私钥
 * @return {string} 解密后的字符串
 */
 const Sm2Decrypt = (enStr, privateKey) => {
	let msg = enStr;
	if (typeof (enStr) !== "string") {
		msg = JSON.stringify(enStr);
	}
	msg = msg.substr(2)
	// 1 - C1C3C2；	0 - C1C2C3；	默认为1
	let cipherMode = 1;
	// 解密结果
	return sm2.doDecrypt(msg, privateKey, cipherMode);
}

const keyPairHex = Sm2GenerateKeyPairHex();
$.ajaxSetup({
	beforeSend: function(xhr, settings) {
		// 在请求发送前拦截处理
		// 可以在这里对请求进行修改、添加请求头等操作
		//传输加密
		let serverPublicKey = sessionStorage.getItem('$serverPublicKey') || "";
		if (!serverPublicKey){
			return
		}
		if (settings.dataType === 'json' && serverPublicKey) {
			xhr.setRequestHeader('serverPublicKey', serverPublicKey)
			settings.url = encryptUrl(settings.url)
		}
		if (settings.contentType === 'application/json'){
			settings.data = JSON.stringify({
				text: Sm2Encrypt(settings.data, serverPublicKey)
			})
		}
		if (settings.contentType.indexOf('application/x-www-form-urlencoded')>=0&&settings.data){
			let urlParameters = getURLParameters(settings.data);
			let _data = ''
			for (let key in urlParameters) {
				_data += '&' + key + '=' + Sm2Encrypt(decodeURIComponent(urlParameters[key]), serverPublicKey)
			}
			settings.data = _data.substr(1)
		}
		xhr.setRequestHeader('clientPublicKey', keyPairHex.publicKey)
	}
});
var myHelper = new helper();
