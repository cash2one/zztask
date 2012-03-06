/**
 * 
 */
package com.zz91.mission.domain.subscribe;

import java.io.Serializable;

/**
 * @author root
 *
 */
public class CompanyAccount implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String account;
	private String email;
	private String backEmail;
	private String sex;
	private String contact;
	private String isUseBackEmail;
	/**
	 * @return the account
	 */
	public String getAccount() {
		return account;
	}
	/**
	 * @param account the account to set
	 */
	public void setAccount(String account) {
		this.account = account;
	}
	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	/**
	 * @return the backEmail
	 */
	public String getBackEmail() {
		return backEmail;
	}
	/**
	 * @param backEmail the backEmail to set
	 */
	public void setBackEmail(String backEmail) {
		this.backEmail = backEmail;
	}
	/**
	 * @return the sex
	 */
	public String getSex() {
		return sex;
	}
	/**
	 * @param sex the sex to set
	 */
	public void setSex(String sex) {
		this.sex = sex;
	}
	/**
	 * @return the contact
	 */
	public String getContact() {
		return contact;
	}
	/**
	 * @param contact the contact to set
	 */
	public void setContact(String contact) {
		this.contact = contact;
	}
	/**
	 * @return the isUseBackEmail
	 */
	public String getIsUseBackEmail() {
		return isUseBackEmail;
	}
	/**
	 * @param isUseBackEmail the isUseBackEmail to set
	 */
	public void setIsUseBackEmail(String isUseBackEmail) {
		this.isUseBackEmail = isUseBackEmail;
	}
	
	
}
