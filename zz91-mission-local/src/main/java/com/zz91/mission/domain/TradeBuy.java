package com.zz91.mission.domain;

import java.io.Serializable;
import java.util.Date;

public class TradeBuy implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private Integer id;
	private Integer uid;
	private Integer cid;
	private String title;
	private String details;
	private String categoryCode;
	private String photoCover;
	private String provinceCode;
	private String areaCode;
	private Short buyType;
	private Integer quantity;
	private Integer quantityYear;
	private String quantityUntis;
	private String supplyAreaCode;
	private String useTo;
	private Date gmtConfirm;
	private Date gmtReceive;
	private Date gmtPublish;
	private Date gmtRefresh;
	private Short validDays;
	private String tagsSys;
	private String detailsQuery;
	private Integer messageCount;
	private Integer viewCount;
	private Integer favoriteCount;
	private Integer plusCount;
	private String htmlPath;
	private Short delStatus;
	private Short pauseStatus;
	private Short checkStatus;
	private String checkAdmin;
	private String checkRefuse;
	private Date gmtCheck;
	private Date gmtCreated;
	private Date gmtModified;
	private Date gmtExpired;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getUid() {
		return uid;
	}
	public void setUid(Integer uid) {
		this.uid = uid;
	}
	public Integer getCid() {
		return cid;
	}
	public void setCid(Integer cid) {
		this.cid = cid;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDetails() {
		return details;
	}
	public void setDetails(String details) {
		this.details = details;
	}
	public String getCategoryCode() {
		return categoryCode;
	}
	public void setCategoryCode(String categoryCode) {
		this.categoryCode = categoryCode;
	}
	public String getPhotoCover() {
		return photoCover;
	}
	public void setPhotoCover(String photoCover) {
		this.photoCover = photoCover;
	}
	public String getProvinceCode() {
		return provinceCode;
	}
	public void setProvinceCode(String provinceCode) {
		this.provinceCode = provinceCode;
	}
	public String getAreaCode() {
		return areaCode;
	}
	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}
	public Short getBuyType() {
		return buyType;
	}
	public void setBuyType(Short buyType) {
		this.buyType = buyType;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	public Integer getQuantityYear() {
		return quantityYear;
	}
	public void setQuantityYear(Integer quantityYear) {
		this.quantityYear = quantityYear;
	}
	public String getQuantityUntis() {
		return quantityUntis;
	}
	public void setQuantityUntis(String quantityUntis) {
		this.quantityUntis = quantityUntis;
	}
	public String getSupplyAreaCode() {
		return supplyAreaCode;
	}
	public void setSupplyAreaCode(String supplyAreaCode) {
		this.supplyAreaCode = supplyAreaCode;
	}
	public String getUseTo() {
		return useTo;
	}
	public void setUseTo(String useTo) {
		this.useTo = useTo;
	}
	public Date getGmtConfirm() {
		return gmtConfirm;
	}
	public void setGmtConfirm(Date gmtConfirm) {
		this.gmtConfirm = gmtConfirm;
	}
	public Date getGmtReceive() {
		return gmtReceive;
	}
	public void setGmtReceive(Date gmtReceive) {
		this.gmtReceive = gmtReceive;
	}
	public Date getGmtPublish() {
		return gmtPublish;
	}
	public void setGmtPublish(Date gmtPublish) {
		this.gmtPublish = gmtPublish;
	}
	public Date getGmtRefresh() {
		return gmtRefresh;
	}
	public void setGmtRefresh(Date gmtRefresh) {
		this.gmtRefresh = gmtRefresh;
	}
	public Short getValidDays() {
		return validDays;
	}
	public void setValidDays(Short validDays) {
		this.validDays = validDays;
	}
	public String getTagsSys() {
		return tagsSys;
	}
	public void setTagsSys(String tagsSys) {
		this.tagsSys = tagsSys;
	}
	public String getDetailsQuery() {
		return detailsQuery;
	}
	public void setDetailsQuery(String detailsQuery) {
		this.detailsQuery = detailsQuery;
	}
	public Integer getMessageCount() {
		return messageCount;
	}
	public void setMessageCount(Integer messageCount) {
		this.messageCount = messageCount;
	}
	public Integer getViewCount() {
		return viewCount;
	}
	public void setViewCount(Integer viewCount) {
		this.viewCount = viewCount;
	}
	public Integer getFavoriteCount() {
		return favoriteCount;
	}
	public void setFavoriteCount(Integer favoriteCount) {
		this.favoriteCount = favoriteCount;
	}
	public Integer getPlusCount() {
		return plusCount;
	}
	public void setPlusCount(Integer plusCount) {
		this.plusCount = plusCount;
	}
	public String getHtmlPath() {
		return htmlPath;
	}
	public void setHtmlPath(String htmlPath) {
		this.htmlPath = htmlPath;
	}
	public Short getDelStatus() {
		return delStatus;
	}
	public void setDelStatus(Short delStatus) {
		this.delStatus = delStatus;
	}
	public Short getPauseStatus() {
		return pauseStatus;
	}
	public void setPauseStatus(Short pauseStatus) {
		this.pauseStatus = pauseStatus;
	}
	public Short getCheckStatus() {
		return checkStatus;
	}
	public void setCheckStatus(Short checkStatus) {
		this.checkStatus = checkStatus;
	}
	public String getCheckAdmin() {
		return checkAdmin;
	}
	public void setCheckAdmin(String checkAdmin) {
		this.checkAdmin = checkAdmin;
	}
	public String getCheckRefuse() {
		return checkRefuse;
	}
	public void setCheckRefuse(String checkRefuse) {
		this.checkRefuse = checkRefuse;
	}
	public Date getGmtCheck() {
		return gmtCheck;
	}
	public void setGmtCheck(Date gmtCheck) {
		this.gmtCheck = gmtCheck;
	}
	public Date getGmtCreated() {
		return gmtCreated;
	}
	public void setGmtCreated(Date gmtCreated) {
		this.gmtCreated = gmtCreated;
	}
	public Date getGmtModified() {
		return gmtModified;
	}
	public void setGmtModified(Date gmtModified) {
		this.gmtModified = gmtModified;
	}
	public Date getGmtExpired() {
		return gmtExpired;
	}
	public void setGmtExpired(Date gmtExpired) {
		this.gmtExpired = gmtExpired;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
