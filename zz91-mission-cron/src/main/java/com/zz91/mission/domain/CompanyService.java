/**
 * 
 */
package com.zz91.mission.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * @author root
 *
 */
public class CompanyService implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String csAccount;
	private Date preGmtEnd;
	private Date gmtSigned;
	
	public CompanyService(String csAccount, Date preGmtEnd, Date gmtSigned) {
		super();
		this.csAccount = csAccount;
		this.preGmtEnd = preGmtEnd;
		this.gmtSigned = gmtSigned;
	}
	
	public CompanyService() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	
	/**
	 * @return the csAccount
	 */
	public String getCsAccount() {
		return csAccount;
	}
	/**
	 * @param csAccount the csAccount to set
	 */
	public void setCsAccount(String csAccount) {
		this.csAccount = csAccount;
	}
	/**
	 * @return the preGmtEnd
	 */
	public Date getPreGmtEnd() {
		return preGmtEnd;
	}
	/**
	 * @param preGmtEnd the preGmtEnd to set
	 */
	public void setPreGmtEnd(Date preGmtEnd) {
		this.preGmtEnd = preGmtEnd;
	}
	/**
	 * @return the gmtSigned
	 */
	public Date getGmtSigned() {
		return gmtSigned;
	}
	/**
	 * @param gmtSigned the gmtSigned to set
	 */
	public void setGmtSigned(Date gmtSigned) {
		this.gmtSigned = gmtSigned;
	}
	
}
