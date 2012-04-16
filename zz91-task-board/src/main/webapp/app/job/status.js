Ext.namespace("com.zz91.task.board.job.status");

com.zz91.task.board.job.status.StatusRecord=Ext.data.Record.create(["id","jobName","gmtBasetime","result","runtime","gmtTrigger","errorMsg","numRetry"]);

com.zz91.task.board.job.status.StatusOfDefinitionGrid=Ext.extend(Ext.grid.GridPanel,{
	hideTaskName:true,
	constructor:function(config){
	    config = config||{};
	    Ext.apply(this,config);
	    
	    var _fields = this.statusRecord;
        var _url = this.listUrl; 
        var _store = new Ext.data.JsonStore({
            root:"records",
            totalProperty:'totals',
            remoteSort:true,
            fields:_fields,
            url:_url,
            autoLoad:false
        });
        
        var grid=this;
	    
        var _sm=new Ext.grid.CheckboxSelectionModel();
        var _cm=new Ext.grid.ColumnModel([_sm,{
    		header : "id",
    		sortable : true,
    		dataIndex : 'id',
    		hidden:true
    	},{
    		header : "任务",
    		sortable : true,
    		dataIndex : 'jobName',
    		hidden:grid.hideTaskName
    	},{
    		header:"基准时间",
    		sortable : true,
    		dataIndex : 'gmtBasetime',
    		width:150,
    		renderer:function(value, metadata, record, rowIndex,colIndex, store) {
				if(value!=null){
					return Ext.util.Format.date(new Date(value.time), 'Y-m-d H:i:s');
				}
				else{
					return "";
				}
			}
    	},{
    		header : "执行结果",
    		sortable : false,
    		dataIndex : 'result'
    	},{
    		header : "执行时间",
    		sortable : false,
    		width:80,
    		dataIndex : 'runtime',
    		renderer:function(value, metadata, record, rowIndex,colIndex, store) {
				return com.zz91.task.board.job.status.buildRuntime(value);
			}
    	},{
    		header : "触发时间",
    		sortable : false,
    		dataIndex : 'gmtTrigger',
    		width:150,
    		renderer:function(value, metadata, record, rowIndex,colIndex, store) {
				if(value!=null){
					return Ext.util.Format.date(new Date(value.time), 'Y-m-d H:i:s');
				}
				else{
					return "";
				}
			}
    	},{
    		header : "错误信息",
    		sortable : false,
    		dataIndex : 'errorMsg'
    	},{
    		header : "分类",
    		sortable : false,
    		dataIndex : 'category'
    	},{
    		header : "重试情况",
    		sortable : false,
    		dataIndex : 'numRetry'
    	}]);
        
	    var c={
	    	iconCls:"app16",
	    	store:_store,
            sm:_sm,
            cm:_cm,
            tbar:[{
            	iconCls:"redo16",
            	text:"重试",
            	handler:function(btn){
	            	var sm=grid.getSelectionModel();
	            	var row = sm.getSelections();
	                var _ids = new Array();
	                for (var i=0,len = row.length;i<len;i++){
	                	var startDate=Ext.util.Format.date(new Date(row[i].get("gmtBasetime").time), 'Y-m-d H:i:s');
	                	com.zz91.task.board.job.status.retryTask(row[i].get("jobName"),startDate);
	                }
            	}
            },{
            	iconCls:"delete16",
            	text:"清空日志",
            	handler:function(btn){
            		var row=_store.getAt(0);
            		if(typeof row!="undefined"){
            			com.zz91.task.board.job.status.clearStatus(row.get("jobName"),_store);
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
	        }),
	        listeners:{
	    		"rowdblclick":function(g,rowIndex,e){
	    			var row=g.getStore().getAt(rowIndex);
	    			var _html="";
	    			_html=_html+"基准时间时间："+Ext.util.Format.date(new Date(row.get("gmtBasetime").time), 'Y-m-d H:i:s')+"<br />";
	    			_html=_html+"触发时间："+Ext.util.Format.date(new Date(row.get("gmtTrigger").time), 'Y-m-d H:i:s')+"<br />";
	    			_html=_html+"执行时间："+com.zz91.task.board.job.status.buildRuntime(row.get("runtime"))+"<br />";
	    			_html=_html+"执行结果："+row.get("result")+"<br />";
	    			_html=_html+"错误信息："+row.get("errorMsg")+"<br />";
	    			
			    	var win = new Ext.Window({
						iconCls:"infoabout16",
						title:MESSAGE.title,
						width:500,
						autoHeight:true,
						modal:true,
						items:[{
							xtype:"panel",
							region:"center",
							layout:"fit",
							html:_html,
							height:400,
							autoScroll:true
						}]
					});
					win.show();
	    		}
	    	}
	    };
	    
	    com.zz91.task.board.job.status.StatusOfDefinitionGrid.superclass.constructor.call(this,c);
	},
	listUrl:Context.ROOT+"/job/status/queryStatus.htm",
	statusRecord:com.zz91.task.board.job.status.StatusRecord,
	loadStatusOfDefinition:function(jobname){
		
	}
});

com.zz91.task.board.job.status.retryTask=function(jobName,startDate){
	Ext.Ajax.request({
        url:Context.ROOT+"/job/definition/doTask.htm",
        params:{"jobName":jobName,"startDate":startDate},
        success:function(response,opt){
            var obj = Ext.decode(response.responseText);
            if(obj.success){
            	com.zz91.task.board.utils.Msg("","Task scheduled！")
            }
        },
        failure:function(response,opt){
        }
	});	
}

com.zz91.task.board.job.status.clearStatus=function(jobName, store){
	Ext.Ajax.request({
        url:Context.ROOT+"/job/status/clearStatus.htm",
        params:{"jobName":jobName},
        success:function(response,opt){
            var obj = Ext.decode(response.responseText);
            if(obj.success){
            	com.zz91.task.board.utils.Msg("","Status has been cleaned！")
            	store.reload();
            }
        },
        failure:function(response,opt){
        }
	});
}

com.zz91.task.board.job.status.buildRuntime=function(value){
	if(value!=null){
		if(value<=1000){
			return value+"ms";
		}else{
			return (value/1000)+"s";
		}
	}
	else{
		return "";
	}
}
