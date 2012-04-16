/**
 * Copyright 2011 ASTO.
 * All right reserved.
 * Created on 2011-3-25
 */
package com.zz91.mission.domain.subscribe;

import java.io.Serializable;

/**
 * @author mays (mays@zz91.com)
 *
 * created on 2011-3-25
 */
public class Price implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer id;
	private String title;
	private Integer typeId;
	private String typeName;
	private String gmtTime;
	
	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 * @return the typeId
	 */
	public Integer getTypeId() {
		return typeId;
	}
	/**
	 * @param typeId the typeId to set
	 */
	public void setTypeId(Integer typeId) {
		this.typeId = typeId;
	}
	/**
	 * @return the typeName
	 */
	public String getTypeName() {
		return typeName;
	}
	/**
	 * @param typeName the typeName to set
	 */
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	/**
	 * @return the gmtTime
	 */
	public String getGmtTime() {
		return gmtTime;
	}
	/**
	 * @param gmtTime the gmtTime to set
	 */
	public void setGmtTime(String gmtTime) {
		this.gmtTime = gmtTime;
	}
	
	
	
}
