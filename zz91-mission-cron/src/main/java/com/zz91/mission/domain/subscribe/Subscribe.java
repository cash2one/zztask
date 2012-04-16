/**
 * Copyright 2011 ASTO.
 * All right reserved.
 * Created on 2011-3-25
 */
package com.zz91.mission.domain.subscribe;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author mays (mays@zz91.com)
 *
 * created on 2011-3-25
 */
public class Subscribe implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	
	private Integer id;
	private Integer companyId;
	private String account;
	private String keywords;//定制关键字，订制供求使用
	private String isSearchByArea;	//是否按地区筛选，订制供求使用
	private String areaCode;//订制供求使用
	private String productsTypeCode;//供求类型,来自category表,，订制供求使用
	private Integer priceTypeId;//主类别，订制报价使用,来自price_category表
	private Integer priceAssistTypeId;//辅助类别，来自price_category
	private String isSendByEmail;//是否邮件提醒
	private String subscribeType;//0:供求,1:报价
	private Date gmtCreated;
	private Date gmtModified;
    private String isMustSee;
    private String email;
    private List<Price> priceList;
    private List<Product> productList;
    private String keywordsEncode;
    
    private String priceTypeName;
    
	/**
	 * 
	 */
	public Subscribe() {
		super();
	}
	/**
	 * @param id
	 * @param companyId
	 * @param account
	 * @param keywords
	 * @param isSearchByArea
	 * @param areaCode
	 * @param productsTypeCode
	 * @param priceTypeId
	 * @param priceAssistTypeId
	 * @param isSendByEmail
	 * @param subscribeType
	 * @param gmtCreated
	 * @param gmtModified
	 * @param isMustSee
	 * @param email
	 */
	public Subscribe(Integer id, Integer companyId, String account,
			String keywords, String isSearchByArea, String areaCode,
			String productsTypeCode, Integer priceTypeId,
			Integer priceAssistTypeId, String isSendByEmail,
			String subscribeType, Date gmtCreated, Date gmtModified,
			String isMustSee, String email) {
		super();
		this.id = id;
		this.companyId = companyId;
		this.account = account;
		this.keywords = keywords;
		this.isSearchByArea = isSearchByArea;
		this.areaCode = areaCode;
		this.productsTypeCode = productsTypeCode;
		this.priceTypeId = priceTypeId;
		this.priceAssistTypeId = priceAssistTypeId;
		this.isSendByEmail = isSendByEmail;
		this.subscribeType = subscribeType;
		this.gmtCreated = gmtCreated;
		this.gmtModified = gmtModified;
		this.isMustSee = isMustSee;
		this.email = email;
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
	 * @return the companyId
	 */
	public Integer getCompanyId() {
		return companyId;
	}
	/**
	 * @param companyId the companyId to set
	 */
	public void setCompanyId(Integer companyId) {
		this.companyId = companyId;
	}
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
	 * @return the keywords
	 */
	public String getKeywords() {
		return keywords;
	}
	/**
	 * @param keywords the keywords to set
	 */
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}
	/**
	 * @return the areaCode
	 */
	public String getAreaCode() {
		return areaCode;
	}
	/**
	 * @param areaCode the areaCode to set
	 */
	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}
	/**
	 * @return the productsTypeCode
	 */
	public String getProductsTypeCode() {
		return productsTypeCode;
	}
	/**
	 * @param productsTypeCode the productsTypeCode to set
	 */
	public void setProductsTypeCode(String productsTypeCode) {
		this.productsTypeCode = productsTypeCode;
	}
	/**
	 * @return the priceTypeId
	 */
	public Integer getPriceTypeId() {
		return priceTypeId;
	}
	/**
	 * @param priceTypeId the priceTypeId to set
	 */
	public void setPriceTypeId(Integer priceTypeId) {
		this.priceTypeId = priceTypeId;
	}
	/**
	 * @return the priceAssistTypeId
	 */
	public Integer getPriceAssistTypeId() {
		return priceAssistTypeId;
	}
	/**
	 * @param priceAssistTypeId the priceAssistTypeId to set
	 */
	public void setPriceAssistTypeId(Integer priceAssistTypeId) {
		this.priceAssistTypeId = priceAssistTypeId;
	}
	/**
	 * @return the subscribeType
	 */
	public String getSubscribeType() {
		return subscribeType;
	}
	/**
	 * @param subscribeType the subscribeType to set
	 */
	public void setSubscribeType(String subscribeType) {
		this.subscribeType = subscribeType;
	}
	/**
	 * @return the gmtCreated
	 */
	public Date getGmtCreated() {
		return gmtCreated;
	}
	/**
	 * @param gmtCreated the gmtCreated to set
	 */
	public void setGmtCreated(Date gmtCreated) {
		this.gmtCreated = gmtCreated;
	}
	/**
	 * @return the gmtModified
	 */
	public Date getGmtModified() {
		return gmtModified;
	}
	/**
	 * @param gmtModified the gmtModified to set
	 */
	public void setGmtModified(Date gmtModified) {
		this.gmtModified = gmtModified;
	}
	/**
	 * @return the isMustSee
	 */
	public String getIsMustSee() {
		return isMustSee;
	}
	/**
	 * @param isMustSee the isMustSee to set
	 */
	public void setIsMustSee(String isMustSee) {
		this.isMustSee = isMustSee;
	}
	/**
	 * @return the isSearchByArea
	 */
	public String getIsSearchByArea() {
		return isSearchByArea;
	}
	/**
	 * @param isSearchByArea the isSearchByArea to set
	 */
	public void setIsSearchByArea(String isSearchByArea) {
		this.isSearchByArea = isSearchByArea;
	}
	/**
	 * @return the isSendByEmail
	 */
	public String getIsSendByEmail() {
		return isSendByEmail;
	}
	/**
	 * @param isSendByEmail the isSendByEmail to set
	 */
	public void setIsSendByEmail(String isSendByEmail) {
		this.isSendByEmail = isSendByEmail;
	}
	/**
	 * @return the priceTypeName
	 */
	public String getPriceTypeName() {
		return priceTypeName;
	}
	/**
	 * @param priceTypeName the priceTypeName to set
	 */
	public void setPriceTypeName(String priceTypeName) {
		this.priceTypeName = priceTypeName;
	}
	/**
	 * @return the priceList
	 */
	public List<Price> getPriceList() {
		return priceList;
	}
	/**
	 * @param priceList the priceList to set
	 */
	public void setPriceList(List<Price> priceList) {
		this.priceList = priceList;
	}
	/**
	 * @return the productList
	 */
	public List<Product> getProductList() {
		return productList;
	}
	/**
	 * @param productList the productList to set
	 */
	public void setProductList(List<Product> productList) {
		this.productList = productList;
	}
	/**
	 * @return the keywordsEncode
	 */
	public String getKeywordsEncode() {
		return keywordsEncode;
	}
	/**
	 * @param keywordsEncode the keywordsEncode to set
	 */
	public void setKeywordsEncode(String keywordsEncode) {
		this.keywordsEncode = keywordsEncode;
	}
    
    
}
