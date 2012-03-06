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
public class Product implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer id;
	private String title;
	private String productType;
	private String price;
	private String priceUnit;
	private String quantityUnit;
	private String pic;
	private String gmtDate;
	private Integer companyId;
	
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
	 * @return the price
	 */
	public String getPrice() {
		return price;
	}
	/**
	 * @param price the price to set
	 */
	public void setPrice(String price) {
		this.price = price;
	}
	/**
	 * @return the priceUnit
	 */
	public String getPriceUnit() {
		return priceUnit;
	}
	/**
	 * @param priceUnit the priceUnit to set
	 */
	public void setPriceUnit(String priceUnit) {
		this.priceUnit = priceUnit;
	}
	/**
	 * @return the quantityUnit
	 */
	public String getQuantityUnit() {
		return quantityUnit;
	}
	/**
	 * @param quantityUnit the quantityUnit to set
	 */
	public void setQuantityUnit(String quantityUnit) {
		this.quantityUnit = quantityUnit;
	}
	/**
	 * @return the pic
	 */
	public String getPic() {
		return pic;
	}
	/**
	 * @param pic the pic to set
	 */
	public void setPic(String pic) {
		this.pic = pic;
	}
	/**
	 * @return the gmtDate
	 */
	public String getGmtDate() {
		return gmtDate;
	}
	/**
	 * @param gmtDate the gmtDate to set
	 */
	public void setGmtDate(String gmtDate) {
		this.gmtDate = gmtDate;
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
	 * @return the productType
	 */
	public String getProductType() {
		return productType;
	}
	/**
	 * @param productType the productType to set
	 */
	public void setProductType(String productType) {
		this.productType = productType;
	}
	
}
