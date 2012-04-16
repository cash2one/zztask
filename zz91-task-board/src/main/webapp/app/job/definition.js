Ext.namespace("com.zz91.task.board.job.definition");

com.zz91.task.board.job.definition.Field = ["id","jobName","cron","jobGroup","jobClassName","description","isInUse","jobClasspath"];

com.zz91.task.board.job.definition.SimpleGrid=Ext.extend(Ext.grid.GridPanel,{
	targetStatusGridId:null,
	constructor:function(config){
	    config = config||{};
	    Ext.apply(this,config);
	    
	    var _fields = this.definitionRecord;
        var _url = this.listUrl; 
        var _store = new Ext.data.JsonStore({
            root:"records",
            totalProperty:'totals',
            remoteSort:true,
            fields:_fields,
            url:_url,
            autoLoad:true
        });
	    
        var grid=this;
        var _sm=new Ext.grid.CheckboxSelectionModel({
			listeners: {
				"selectionchange": function(sm) {
        			if(grid.targetStatusGridId==null){
        				return ;
        			}
        			var selectedRecord = _sm.getSelected();
	                if(typeof(selectedRecord)!="undefined"){
	                	var jobName=selectedRecord.get("jobName");
						if(jobName!=null) {
							var statusGrid=Ext.getCmp(grid.targetStatusGridId);
							var B=statusGrid.getStore().baseParams||{};
							B["jobName"] = jobName;
							statusGrid.getStore().baseParams = B;
							statusGrid.getStore().reload({params:{"start":0, "limit":Context.PAGE_SIZE}});
						}
	                }
				} 
			}
		});
        
        var _cm=new Ext.grid.ColumnModel([_sm,{
    		header : "id",
    		sortable : true,
    		dataIndex : 'id',
    		hidden:true
    	},{
    		header : "",
    		sortable : true,
    		width:27,
    		dataIndex : 'isInUse',
    		renderer:function(value, metadata, record, rowIndex,colIndex, store) {
				if(value==1){
					return "<div class='accept16'>&nbsp;</div>";
				}
				return "<div class='stop16'>&nbsp;</div>";
			}
    	},{
    		header : "jobId",
    		sortable : true,
    		dataIndex : 'jobName'
    	},{
    		header : "cron",
    		sortable : false,
    		width:120,
    		dataIndex : 'cron'
    	},{
    		header : "组",
    		sortable : false,
    		dataIndex : 'jobGroup',
    		hidden:true
    	},{
    		header : "jar包",
    		sortable : false,
    		dataIndex : 'jobClasspath'
    	},{
    		header : "任务Class",
    		sortable : false,
    		dataIndex : 'jobClassName'
    	},{
    		header : "任务描述",
    		sortable : false,
    		dataIndex : 'description'
    	}]);
        var grid= this;
	    var c={
	    	loadMask:MESSAGE.loading,
	    	id:"definitiongrid",
	    	iconCls:"app16",
	    	store:_store,
            sm:_sm,
            cm:_cm,
            tbar:[{
            	iconCls:"add16",
            	text:"增加",
            	handler:function(btn){
            		com.zz91.task.board.job.definition.CreateDefinitionWin();
            	}
            },{
            	iconCls:"edit16",
            	text:"修改",
            	handler:function(btn){
	            	var selectedRecord = grid.getSelectionModel().getSelected();
	                if(typeof(selectedRecord)=="undefined"){
                        Ext.MessageBox.show({
                            title:MESSAGE.title,
                            msg : MESSAGE.needOneRecord,
                            buttons:Ext.MessageBox.OK,
                            icon:Ext.MessageBox.WARNING
                        });
                        return false;
	                }
            	com.zz91.task.board.job.definition.UpdateDefinitionWin(selectedRecord.get("id"));
            	}
            },{
            	iconCls:"delete16",
            	text:"删除",
            	handler:function(btn){
	            	var sm=grid.getSelectionModel();
	                var submitIds=sm.getCount();
	                if ( submitIds== 0){
                        Ext.MessageBox.show({
                            title:MESSAGE.title,
                            msg : MESSAGE.needOneRecord,
                            buttons:Ext.MessageBox.OK,
                            icon:Ext.MessageBox.WARNING
                        });
	                } else{
                        Ext.MessageBox.confirm(MESSAGE.title, MESSAGE.confirmDelete, function(btn){
                            if(btn != "yes"){
                                    return false;
                            }
                            
                            var row = sm.getSelections();
                            var _ids = new Array();
                            for (var i=0,len = row.length;i<len;i++){
                                var _id=row[i].get("id");
                                //TODO 一条条删除
                                com.zz91.task.board.job.definition.deleteDefinition(_id);
                            }
                        });
	                }
            	}
            },"-",{
            	iconCls:"accept16",
            	text:"启用",
            	handler:function(btn){
	            	var sm=grid.getSelectionModel();
	            	var row = sm.getSelections();
	                var _ids = new Array();
	                for (var i=0,len = row.length;i<len;i++){
	                	if(row[i].get("isInUse")==0 || row[i].get("isInUse")==""){
		                    com.zz91.task.board.job.definition.resumeTask(row[i].get("id"),row[i].get("jobName"));
	                	}
	                }
            	}
            },{
            	iconCls:"stop16",
            	text:"停用",
            	handler:function(btn){
	            	var sm=grid.getSelectionModel();
	            	var row = sm.getSelections();
	                var _ids = new Array();
	                for (var i=0,len = row.length;i<len;i++){
	                	if(row[i].get("isInUse")==1){
		                    com.zz91.task.board.job.definition.pauseTask(row[i].get("id"),row[i].get("jobName"));
	                	}
	                }
            	}
            },"-",{
            	iconCls:"accept16",
            	text:"简",
            	handler:function(){
	            	var sm=grid.getSelectionModel();
	            	var row = sm.getSelections();
	                var _ids = new Array();
	                for (var i=0,len = row.length;i<len;i++){
		                com.zz91.task.board.job.definition.resumeSimpleTask(row[i].get("id"),row[i].get("cron"),row[i].get("jobName"));
	                }
            	}
            },{
            	iconCls:"stop16",
            	text:"简",
            	handler:function(){
	            	var sm=grid.getSelectionModel();
	            	var row = sm.getSelections();
	                var _ids = new Array();
	                for (var i=0,len = row.length;i<len;i++){
		                  com.zz91.task.board.job.definition.pauseSimpleTask(row[i].get("id"),row[i].get("jobName"));
	                }
            	}
            },"->","指定时间：",{
            	id:"basetime",
            	xtype:"datefield",
            	format:"Y-m-d"
            },{
            	iconCls:"edit16",
            	text:"手动执行",
            	handler:function(btn){
            		
	            	var sm=grid.getSelectionModel();
	                var submitIds=sm.getCount();
	                if ( submitIds== 0){
	                    Ext.MessageBox.show({
	                        title:MESSAGE.title,
	                        msg : MESSAGE.needOneRecord,
	                        buttons:Ext.MessageBox.OK,
	                        icon:Ext.MessageBox.WARNING
	                    });
	                } else{
                        var row = sm.getSelections();
                        var _ids = new Array();
                        for (var i=0,len = row.length;i<len;i++){
                            var _id=row[i].get("id");
                            var _startDate=Ext.util.Format.date(Ext.getCmp("basetime").getValue(), 'Y-m-d H:i:s');
                            Ext.Ajax.request({
        	                    url:Context.ROOT+"/job/definition/doTask.htm",
        	                    params:{"id":row[i].get("id"), "startDate":_startDate},
        	                    success:function(response,opt){
        	                    	var obj = Ext.decode(response.responseText);
        	                        if(obj.success){
        	                        	com.zz91.task.board.utils.Msg("","Task scheduled！")
        	                        }else{
        	                        	com.zz91.task.board.utils.Msg("","Sorry task unscheduled！")
        	                        }
        	                    },
        	                    failure:function(response,opt){
        	                    }
        	            	});
                        }
	                }
	                
            	}
            }],
            bbar: new Ext.PagingToolbar({
                pageSize : Context.PAGE_SIZE,
                store : _store,
                displayInfo: true,
                displayMsg: MESSAGE.paging.displayMsg,
                emptyMsg : MESSAGE.paging.emptyMsg,
                beforePageText : MESSAGE.paging.beforePageText,
                afterPageText : MESSAGE.paging.afterPageText,
                paramNames : MESSAGE.paging.paramNames
	        })
	    };
	    
	    com.zz91.task.board.job.definition.SimpleGrid.superclass.constructor.call(this,c);
	},
	listUrl:Context.ROOT+"/job/definition/queryDefinition.htm",
	definitionRecord:com.zz91.task.board.job.definition.Field,
	loadDefinition:function(){
		//load system definition
		this.getStore().reload();
	}
});

com.zz91.task.board.job.definition.deleteDefinition = function(id){
	Ext.Ajax.request({
        url:Context.ROOT+"/job/definition/deleteDefinition.htm",
        params:{"id":id},
        success:function(response,opt){
            var obj = Ext.decode(response.responseText);
            if(obj.success){
            	Ext.getCmp("definitiongrid").getStore().reload();
            }
        },
        failure:function(response,opt){
        }
	});
}

com.zz91.task.board.job.definition.pauseTask = function(id,jobName){
	Ext.Ajax.request({
        url:Context.ROOT+"/job/definition/pauseTask.htm",
        params:{"id":id,"jobName":jobName},
        success:function(response,opt){
            var obj = Ext.decode(response.responseText);
            if(obj.success){
            	Ext.getCmp("definitiongrid").getStore().reload();
            }
        },
        failure:function(response,opt){
        }
	});
}

com.zz91.task.board.job.definition.resumeTask = function(id,jobName){
	Ext.Ajax.request({
        url:Context.ROOT+"/job/definition/resumeTask.htm",
        params:{"id":id,"jobName":jobName},
        success:function(response,opt){
            var obj = Ext.decode(response.responseText);
            if(obj.success){
            	Ext.getCmp("definitiongrid").getStore().reload();
            }
        },
        failure:function(response,opt){
        }
	});
}

com.zz91.task.board.job.definition.resumeSimpleTask=function(id,cron,jobName){
	Ext.Ajax.request({
        url:Context.ROOT+"/job/definition/resumeSimpleTask.htm",
        params:{"id":id,"cron":cron,"jobName":jobName},
        success:function(response,opt){
            var obj = Ext.decode(response.responseText);
            if(obj.success){
            	Ext.getCmp("definitiongrid").getStore().reload();
            }
        },
        failure:function(response,opt){
        }
	});
}

com.zz91.task.board.job.definition.pauseSimpleTask=function(id,jobName){
	Ext.Ajax.request({
        url:Context.ROOT+"/job/definition/pauseSimpleTask.htm",
        params:{"id":id,"jobName":jobName},
        success:function(response,opt){
            var obj = Ext.decode(response.responseText);
            if(obj.success){
            	Ext.getCmp("definitiongrid").getStore().reload();
            }
        },
        failure:function(response,opt){
        }
	});
}


com.zz91.task.board.job.definition.FormWithParam = Ext.extend(Ext.form.FormPanel,{
	isEdit:false,
	constructor:function(config){
	    config = config||{};
	    Ext.apply(this,config);
	    
	    var _isEdit=this.isEdit;
	    
	    var c={
    		labelAlign : "right",
            labelWidth : 60,
            layout:"column",
            frame:true,
            items:[{
            	columnWidth:0.5,
                layout:"form",
                defaults:{
                    anchor:"100%",
                    xtype:"textfield",
                    labelSeparator:""
                },
                items:[{
                	xtype:"hidden",
                	id:"id",
                	name:"id"
                },{
                	xtype:"hidden",
                	id:"isInUse",
                	name:"isInUse"
                },{
                	id:"jobName",
                	name:"jobName",
                	allowBlank:false,
                    itemCls:"required",
                    vtype:"alphanum",
                    fieldLabel:"任务ID",
                    readOnly:_isEdit
                },{
                	id:"cron",
                	name:"cron",
                	allowBlank:false,
                	itemCls:"required",
                	fieldLabel:"cron"
                }]
            },{
            	columnWidth:0.5,
                layout:"form",
                defaults:{
                    anchor:"100%",
                    xtype:"textfield",
                    labelSeparator:""
                },
                items:[{
                	id:"jobClassName",
                	name:"jobClassName",
                	allowBlank:false,
                	itemCls:"required",
                	fieldLabel:"class"
                },{
                	xtype:"textfield",
                	id:"jobClasspath",
                	fieldLabel: 'jar包',
                	itemCls:"required",
                	name:"jobClasspath",
                	listeners:{
                		"focus":function(c){
                			com.zz91.task.board.UploadConfig.uploadURL=Context.ROOT+"/job/definition/uploadjar.htm";
                			var win = new com.zz91.task.board.UploadWin({
                				title:"上传jar"
                			});
                			com.zz91.task.board.UploadConfig.uploadSuccess=function(f,o){
                				if(o.result.success){
                					win.close();
                					Ext.getCmp("jobClasspath").setValue(o.result.data);
                				}
                			}
	                        win.show();
                		}
                	}
                }]
            },{
            	columnWidth:1,
                layout:"form",
                items:[{
                	xtype:"textarea",
                	id:"description",
                	name:"description",
                	fieldLabel:"任务描述",
                	anchor:"100%"
                }]
            }
//            ,{
//            	columnWidth:1,com.zz91.task.board.job.definition.Field
//            	layout:"fit",
//            	height:280,
//            	items:[new com.zz91.task.board.job.definition.ParamOfDefinitionGrid()]
//            }
            ],
            buttons:[{
            	text:"保存",
            	scope:this,
            	handler:function(btn){
            		var _url=this.saveUrl;
            		this.fileUpload = false;
	            	if(this.getForm().isValid()){
	                    this.getForm().submit({
                            url:_url,
                            method:"post",
                            type:"json",
                            success:function(_form,_action){
	                    		Ext.getCmp("createdefinitionwin").close();
	                    		Ext.getCmp("definitiongrid").getStore().reload();
                    		},
                            failure:function(_form,_action){
                    			Ext.MessageBox.show({
                                    title:MESSAGE.title,
                                    msg : MESSAGE.saveFailure,
                                    buttons:Ext.MessageBox.OK,
                                    icon:Ext.MessageBox.ERROR
                    			});
                    		}
	                    });
		            }else{
	                    Ext.MessageBox.show({
                            title:MESSAGE.title,
                            msg : MESSAGE.unValidate,
                            buttons:Ext.MessageBox.OK,
                            icon:Ext.MessageBox.ERROR
	                    });
		            }
            	}
            },{
            	text:"取消",
            	handler:function(btn){
            		Ext.getCmp("createdefinitionwin").close();
            	}
            }]
	    };
	    
	    com.zz91.task.board.job.definition.FormWithParam.superclass.constructor.call(this,c);
	},
	loadDefinition:function(id){
		
		var form = this;
        var store=new Ext.data.JsonStore({
                root : "records",
                fields : com.zz91.task.board.job.definition.Field,
                url : Context.ROOT + "/job/definition/queryDefinitionById.htm",
                baseParams:{"id":id},
                autoLoad : true,
                listeners : {
//                      "exception":function(misc){
//                              alert(misc+"   "+store.getCount())
//                      },
                        "datachanged" : function(s) {
                                var record = s.getAt(0);
                                if (record == null) {
                                        Ext.MessageBox.alert(MESSAGE.title,MESSAGE.loadError);
                                } else {
                                        form.getForm().loadRecord(record);
                                }
                        }
                }
        });
        
	},
	saveUrl:Context.ROOT+"/job/definition/createDefinition.htm"
});

/**
com.zz91.task.board.job.definition.ParamOfDefinitionGrid = Ext.extend(Ext.grid.EditorGridPanel,{
	listUrl:Context.ROOT+"/job/definition/queryParamByDefinition.htm",
	constructor:function(config){
		config=config||{};
		Ext.apply(this,config);
		
		var _fields = this.paramRecord;
        var _url = this.listUrl; 
        var _store = new Ext.data.JsonStore({
            root:"records",
            totalProperty:'totals',
            remoteSort:true,
            fields:_fields,
            url:_url,
            autoLoad:false
        });
	    
        var _sm=new Ext.grid.CheckboxSelectionModel();
        var _cm=new Ext.grid.ColumnModel([_sm,{
    		header : "id",
    		sortable : true,
    		dataIndex : 'id',
    		hidden:true
    	},{
    		header : "key",
    		sortable : true,
    		dataIndex : 'name',
    		editor:new Ext.form.TextField({})
    	},{
    		header : "value",
    		sortable : true,
    		dataIndex : 'value',
    		editor:new Ext.form.TextField({})
    	}]);
        
		var c={
	    	store:_store,
            sm:_sm,
            cm:_cm,
            frame:true,
            tbar:[{
            	iconCls:"add16",
            	text:"增加参数",
            	handler:function(btn){
            		
            	}
            },{
            	iconCls:"delete16",
            	text:"增加参数",
            	handler:function(btn){
            		
            	}
            }]
		};
		
		com.zz91.task.board.job.definition.ParamOfDefinitionGrid.superclass.constructor.call(this,c);
	},
	paramRecord:Ext.data.Record.create(["id","jobId","name","value"])
});
*/

com.zz91.task.board.job.definition.CreateDefinitionWin = function(){
	var form = new com.zz91.task.board.job.definition.FormWithParam({
        id:"formwithparam",
        region:"center"
	});
	
	var win = new Ext.Window({
        id:"createdefinitionwin" ,
        title:"添加任务",
        width:"80%",
        autoHeight:true,
        modal:true,
        maximizable:true,
        items:[form]
	});
	
	win.show();
}

com.zz91.task.board.job.definition.UpdateDefinitionWin = function(id){
	var form = new com.zz91.task.board.job.definition.FormWithParam({
		isEdit:true,
        id:"formwithparam",
        region:"center",
        saveUrl:Context.ROOT+"/job/definition/updateDefinition.htm"
	});
	
	form.loadDefinition(id);
	
	var win = new Ext.Window({
        id:"createdefinitionwin" ,
        title:"添加任务",
        width:"80%",
        autoHeight:true,
        modal:true,
        maximizable:true,
        items:[form]
	});
	
	win.show();
}

/*************old code**************
//定义变量
var _C = new function() {
	this.RESULT_GRID = "resultGrid";
	this.EDIT_FORM = "editForm";
	this.EDIT_WIN = "editWin";
	this.ADD_FORM = "addForm";
}

com.zz91.task.board.listRightWin=function(_cfg){

	if(_cfg==null){
		_cfg = {};
	}

	var _jobId = _cfg["jobId"] || 0;
	var _roleRightList = _cfg["roleRightList"] || null;

	var rights=com.zz91.task.board.listRightNotInRole({jobId:_jobId,roleRightList:_roleRightList});

	var win=new Ext.Window({
		id:"listRightWin",
		title:"参数信息",
		closeable:true,
		width:700,
		height:350,
		modal:true,
		border:false,
		plain:true,
		maximizable:true,
		layout:"fit",
		items:[rights]
	});
	win.show();
}


com.zz91.task.board.listRightNotInRole=function(_cfg){
	if(_cfg==null){
		_cfg={};
	}
	
	var _jobId = _cfg["jobId"] || 0;
	var _roleRightList = _cfg["roleRightList"] || null;

	var sm = new Ext.grid.CheckboxSelectionModel();
	var cm = new Ext.grid.ColumnModel([sm,{
		header : "id",
		sortable : true,
		dataIndex : 'id',
		hidden:true
	},{
		header : "jobId",
		sortable : true,
		dataIndex : 'jobId',
		hidden:true
	},{
		header : "参数名称",
		sortable : true,
		dataIndex : "name"
	},{
		header : "参数类型",
		sortable : false,
		dataIndex : "type"
	},{
		header : "参数值",
		sortable : false,
		dataIndex : "value"
	},{
		header : "是否必须",
		sortable : false,
		dataIndex : "required",
		renderer : function(value, metadata, record, rowIndex,
				colIndex, store) {
			if (value == 0) {
				return "否";
			} else {
				return "是";
			}
		}
	}]);

	var reader = ["id","name","jobId","type","required","value","description"];

	var storeUrl = Context.ROOT + "/job/definition/listParameter.htm?jobId="+_jobId;

	var grid = new com.zz91.task.board.StandardGridPanelNoPage({
		id:"jobGrid",
		sm: sm,
		cm: cm,
		reader : reader,
		storeUrl : storeUrl,
		tbar : new Ext.Toolbar({
			items:[{
				text:"添加",
				iconCls : "add16",
				handler :function(btn){
					com.zz91.task.board.jobdefinition.addParameterFormWin(_jobId);
				}
			},{
				text:"修改",
				iconCls:"edit16",
				handler:function(){
					var row =grid.getSelectionModel().getSelections();
					if(row.length==0){
						Ext.Msg.alert(Context.MSG_TITLE, "请选定一条记录");
					}else if (row.length > 1) {
						Ext.MessageBox.show({
								title : Context.MSG_TITLE,
								msg : "最多只能选择一条记录！",
								buttons : Ext.MessageBox.OK,
								icon : Ext.MessageBox.ERROR
							});
					} else {
						var _id = row[0].get("id");
						 com.zz91.task.board.jobdefinition.editParameterFormWin(_id);
					}
				}
			},{
				text:"删除",
				iconCls:"delete16",
				handler:function(){
				
					var row =grid.getSelectionModel().getSelections();
					if(row.length==0)
						Ext.Msg.alert(Context.MSG_TITLE, "请至少选定一条记录");
					else
						Ext.MessageBox.confirm(Context.MSG_TITLE, '你真的要删除所选记录?',function(btn){
							if(btn!="yes"){
								return ;
							}
						var id=row[0].get("id");
						var conn = new Ext.data.Connection();
						conn.request({
									url : Context.ROOT +"/job/definition/deleteParameter.htm?id="+id,
									method : "get",
									scope : this,
									callback : function(options, success, response) {
										var a = Ext.decode(response.responseText);
										if (success) {
											Ext.MessageBox.alert(Context.MSG_TITLE, "选定的记录已被删除!");
											grid.getStore().reload();
										} else {
											Ext.MessageBox.alert(Context.MSG_TITLE, "所选记录删除失败!");
										}
									}
						});
					});
				}
			}]
		})
	});
	return grid;
}

com.zz91.task.board.jobdefinition.addParameterFormWin = function(_jobId) {
	
	var form = new com.zz91.task.board.jobdefinition.editForm1({
				id : _C.ADD_FORM,
				region : "center"
			});

	var win = new Ext.Window({
				id : _C.EDIT_WIN,
				title : "添加任务",
				width : "85%",
				modal : true,
				items :[form]
	});
	win.show();
	Ext.get("jobId").dom.value=_jobId;
};

//修改窗口
com.zz91.task.board.jobdefinition.editParameterFormWin = function(id) {
	
	var form = new com.zz91.task.board.jobdefinition.editForm1({
				id : _C.EDIT_FORM,
				region : "center",
				saveUrl : Context.ROOT + "/job/definition/updateParameter.htm"
	});

	var win = new Ext.Window({
			id : _C.EDIT_WIN,
			title : "修改t任务",
			width : "75%",
			modal : true,
			items : [form]
		});
	form.loadRecords(id);
	win.show();
 };

//form表单
com.zz91.task.board.jobdefinition.editForm1 = Ext.extend(Ext.form.FormPanel, {
	constructor : function(config) {
		config = config || {};
		Ext.apply(this, config);
	
			var c={
					labelAlign : "right",
					labelWidth : 80,
					layout : "column",
					bodyStyle : "padding:5px 0 0",
					frame : true,
					items : [{
						columnWidth : 1,
						layout : "form",
						items:[{
								xtype : "hidden",
								name : "id",
								dataIndex : "id"
							},{
								xtype : "hidden",
								id:"jobId",
								name : "jobId"
							},{
								xtype : "textfield",
								fieldLabel : "名称",
								name : "name",
								allowBlank : false,
								anchor : "75%",
								blankText : "名称不能为空"
							}, {
								xtype:"combo",
								id:"type-combo",
								hiddenId : "type",
								hiddenName : "type",
								triggerAction : "all",
								anchor : "75%",
								forceSelection : true,
								fieldLabel:"类型",
								displayField : "v",
								valueField : "k",
								mode : 'local',
								store : new Ext.data.JsonStore({
									root : "type",
									fields : ['k', 'v'],
									data :{
										type:[
											{k:"string",v:"string"},
											{k:"Integer",v:"Integer"},
											{k:"boolean",v:"boolean"}]
									}
								})
							}, {
								xtype : "checkbox",
								fieldLabel :"必须",
								name : "required",
								tabIndex : 1,
								inputValue :1
							},{
								xtype:"textfield",
								fieldLabel:"参数值",
								name:"value",
								anchor : "75%"
							}, {
								xtype : "textarea",
								fieldLabel : "描述",
								name : "description",
								height:140,
								anchor : "95%"
							}]		
					}],
				buttons:[{
					text:"确定",
					handler:this.save,
					scope:this
				},{
					text:"关闭",
					handler:function(){
						Ext.getCmp(_C.EDIT_WIN).close();
					},
					scope:this
				}]
			};
			com.zz91.task.board.jobdefinition.editForm1.superclass.constructor.call(this,c);
		},
		mystore:null,
		loadRecords:function(id){

		var _fields=[
				{name:"id",mapping:"id"},
				{name:"name",mapping:"name"},
				{name:"jobId",mapping:"jobId"},
				{name:"type",mapping:"type"},
				{name:"value",mapping:"value"},
				{name:"required",mapping:"required"},
				{name:"description",mapping:"description"},
				{name:"gmtCreated",mapping:"gmtCreated"},
				{name:"gmtModified",mapping:"gmtModified"}
			];
			var form = this;
			var store=new Ext.data.JsonStore({
			//	root : "records",
				fields : _fields,
				url : Context.ROOT + "/job/definition/initParameter.htm",
				baseParams:{"id":id},
				autoLoad : true,
				listeners : {
					"datachanged" : function(s) {
						var record = s.getAt(0);
						if (record == null) {
							Ext.MessageBox.alert(Context.MSG_TITLE,"数据加载失败...");
						} else {
							form.getForm().loadRecord(record);
						}
					}
				}
			})
		},
		saveUrl:Context.ROOT + "/job/definition/addParameter.htm",
		save:function(){
			var _url = this.saveUrl;
			if(this.getForm().isValid()){
				this.getForm().submit({
					url:_url,
					method:"post",
					type:"json",
					success:this.onSaveSuccess,
					failure:this.onSaveFailure,
					scope:this
				});
			}else{
				Ext.MessageBox.show({
					title:Context.MSG_TITLE,
					msg : "验证未通过",
					buttons:Ext.MessageBox.OK,
					icon:Ext.MessageBox.ERROR
				});
			}
		},
		onSaveSuccess:function (){
			Ext.MessageBox.show({
				title:Context.MSG_TITLE,
				msg : "操作成功！",
				buttons:Ext.MessageBox.OK,
				icon:Ext.MessageBox.INFO
			});
			Ext.getCmp(_C.EDIT_WIN).close();
			Ext.getCmp("jobGrid").getStore().reload();
		},
		onSaveFailure:function (){
			Ext.MessageBox.show({
				title:Context.MSG_TITLE,
				msg : "操作失败！",
				buttons:Ext.MessageBox.OK,
				icon:Ext.MessageBox.ERROR
			});
		}

	});
*/