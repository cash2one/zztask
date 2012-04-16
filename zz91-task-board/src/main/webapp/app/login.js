Ext.namespace("com.zz91.task.board.login");

var LOGIN = new function() {
	this.LOGINWINDOW = "loginwindow";
}

com.zz91.task.board.login.LoginForm = Ext.extend(Ext.form.FormPanel, {
	constructor : function(config) {
		config = config || {};
		Ext.apply(this, config);
		var c = {
			layout : 'form',
			frame : true,
			labelAlign : 'right',
			labelWidth : 60,
			defaults : {
				anchor : "95%",
				xtype : "textfield"
			},
			items : [ {
				fieldLabel : '用户名',
				name : 'username',
				id : 'username',
				allowBlank : false
			}, {
				inputType : 'password',
				fieldLabel : '密码',
				name : 'password',
				id : 'password',
				allowBlank : false
			} ]
		};

		com.zz91.task.board.login.LoginForm.superclass.constructor
				.call(this, c);

	},
	initFocus : function() {
		this.findById("username").focus(true, 100);
	}
});

/**
 * 用户登录动作 form:登录的表单对象 onSuccess:登录成功后要做的事
 */
com.zz91.task.board.login.UserLogin = function(form, onSuccess) {
	form.getForm().submit( {
		url : Context.ROOT + "/authorize.htm",
		method : "post",
		type : "json",
		success : onSuccess,
		failure : function(_form, _action) {
			var _msg="验证没有通过，请检查密码是否正确！";
			if(_action.result.data!=null && _action.result.data!=""){
				_msg=_action.result.data;
			}
			Ext.MessageBox.show( {
				title : MESSAGE.title,
				msg : _msg,
				buttons : Ext.MessageBox.OK,
				icon : Ext.MessageBox.ERROR
			});
		}
	});
}

com.zz91.task.board.login.UserLoginWin = function(doSuccess) {
	var form = new com.zz91.task.board.login.LoginForm( {
		region : "center"
	});

	var win = new Ext.Window( {
		id : LOGIN.LOGINWINDOW,
		layout : 'border',
		iconCls : "key16",
		width : 300,
		height : 150,
		closable : false,
		title : "Asto 任务管理系统 - 登录",
		modal : true,
		items : [ form ],
		keys : [ {
			key : [ 10, 13 ],
			fn : function() {
				com.zz91.task.board.login.UserLogin(form, doSuccess);
			}
		} ],
		buttons : [ {
			text : '登录',
			handler : function(btn) {
				com.zz91.task.board.login.UserLogin(form, doSuccess);
			}
		} ]
	});

	win.show();

	form.initFocus();
}