/**
 * 
 */
package com.zz91.task.board.dto;

import java.io.Serializable;

/**
 * @author yuyh
 * 最终被处理成json格式的数据,返回给页面
 * 格式:
 * {"success":true,"data":[]}
 * success:true表示操作成功,false表示操作失败
 * data:用于携带返回的数据信息
 */
public class ExtResult implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean success;
	private Object data;
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	
}
