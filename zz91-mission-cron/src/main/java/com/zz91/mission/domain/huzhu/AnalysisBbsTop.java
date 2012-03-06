package com.zz91.mission.domain.huzhu;

import java.io.Serializable;

/** 
 * @author qizj 
 * @email  qizj@zz91.net
 * @version 创建时间：2011-8-16 
 */
public class AnalysisBbsTop implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String title;
	private Integer targerId;
	private Integer num;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Integer getTargerId() {
		return targerId;
	}
	public void setTargerId(Integer targerId) {
		this.targerId = targerId;
	}
	public Integer getNum() {
		return num;
	}
	public void setNum(Integer num) {
		this.num = num;
	}
	
}
