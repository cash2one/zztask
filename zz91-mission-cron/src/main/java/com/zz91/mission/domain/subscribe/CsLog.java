package com.zz91.mission.domain.subscribe;

import java.io.Serializable;

public class CsLog implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String csAccount;
	private Integer callType;
	private Integer situation;
	private Integer star;
	
	public String getCsAccount() {
		return csAccount;
	}
	public void setCsAccount(String csAccount) {
		this.csAccount = csAccount;
	}
	public Integer getCallType() {
		return callType;
	}
	public void setCallType(Integer callType) {
		this.callType = callType;
	}
	public Integer getSituation() {
		return situation;
	}
	public void setSituation(Integer situation) {
		this.situation = situation;
	}
	public Integer getStar() {
		return star;
	}
	public void setStar(Integer star) {
		this.star = star;
	}
}
