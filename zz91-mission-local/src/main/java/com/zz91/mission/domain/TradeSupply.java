/*
 * 文件名称：TradeSupply.java
 * 创建者　：涂灵峰
 * 创建时间：2012-4-18 下午3:46:56
 * 版本号　：1.0.0
 */
package com.zz91.mission.domain;

import java.util.Date;

/**
 * 项目名称：中国环保网
 * 模块编号：数据持久层
 * 模块描述：供求信息实体类。
 * 变更履历：修改日期　　　　　修改者　　　　　　　版本号　　　　　修改内容
 *　　　　　 2012-04-18　　　涂灵峰　　　　　　　1.0.0　　　　　创建类文件
 */
public class TradeSupply implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private Integer id;
	private Integer uid;
	private Integer cid;
	private String title;
	private String details;
	private String categoryCode;
	private Integer groupId;
	private String photoCover;
	private String provinceCode;
	private String areaCode;
	private Integer totalNum;
	private String totalUnits;
	private Integer priceNum;
	private String priceUnits;
	private Integer priceFrom;
	private Integer priceTo;
	private String useTo;
	private Short usedProduct;
	private String tags;
	private String tagsSys;
	private String detailsQuery;
	private String propertyQuery;
	private Integer messageCount;
	private Integer viewCount;
	private Integer favoriteCount;
	private Integer plusCount;
	private String htmlPath;
	private Short integrity;
	private Date gmtPublish;
	private Date gmtRefresh;
	private Short validDays;
	private Short delStatus;
	private Short pauseStatus;
	private Short checkStatus;
	private String checkAdmin;
	private String checkRefuse;
	private Date gmtCheck;
	private Date gmtCreated;
	private Date gmtModified;
	private Date gmtExpired;
	private Integer infoComeFrom;
	
	public TradeSupply(){
		
	}
	
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getUid() {
		return this.uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public Integer getCid() {
		return this.cid;
	}

	public void setCid(Integer cid) {
		this.cid = cid;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDetails() {
		return this.details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String getCategoryCode() {
		return this.categoryCode;
	}

	public void setCategoryCode(String categoryCode) {
		this.categoryCode = categoryCode;
	}

	public Integer getGroupId() {
		return this.groupId;
	}

	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}

	public String getPhotoCover() {
		return this.photoCover;
	}

	public void setPhotoCover(String photoCover) {
		this.photoCover = photoCover;
	}

	public String getProvinceCode() {
		return this.provinceCode;
	}

	public void setProvinceCode(String provinceCode) {
		this.provinceCode = provinceCode;
	}

	public String getAreaCode() {
		return this.areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	public Integer getTotalNum() {
		return this.totalNum;
	}

	public void setTotalNum(Integer totalNum) {
		this.totalNum = totalNum;
	}

	public String getTotalUnits() {
		return this.totalUnits;
	}

	public void setTotalUnits(String totalUnits) {
		this.totalUnits = totalUnits;
	}

	public Integer getPriceNum() {
		return this.priceNum;
	}

	public void setPriceNum(Integer priceNum) {
		this.priceNum = priceNum;
	}

	public String getPriceUnits() {
		return this.priceUnits;
	}

	public void setPriceUnits(String priceUnits) {
		this.priceUnits = priceUnits;
	}

	public Integer getPriceFrom() {
		return this.priceFrom;
	}

	public void setPriceFrom(Integer priceFrom) {
		this.priceFrom = priceFrom;
	}

	public Integer getPriceTo() {
		return this.priceTo;
	}

	public void setPriceTo(Integer priceTo) {
		this.priceTo = priceTo;
	}

	public String getUseTo() {
		return this.useTo;
	}

	public void setUseTo(String useTo) {
		this.useTo = useTo;
	}

	public Short getUsedProduct() {
		return this.usedProduct;
	}

	public void setUsedProduct(Short usedProduct) {
		this.usedProduct = usedProduct;
	}

	public String getTags() {
		return this.tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getTagsSys() {
		return this.tagsSys;
	}

	public void setTagsSys(String tagsSys) {
		this.tagsSys = tagsSys;
	}

	public String getDetailsQuery() {
		return this.detailsQuery;
	}

	public void setDetailsQuery(String detailsQuery) {
		this.detailsQuery = detailsQuery;
	}

	public String getPropertyQuery() {
		return this.propertyQuery;
	}

	public void setPropertyQuery(String propertyQuery) {
		this.propertyQuery = propertyQuery;
	}

	public Integer getMessageCount() {
		return this.messageCount;
	}

	public void setMessageCount(Integer messageCount) {
		this.messageCount = messageCount;
	}

	public Integer getViewCount() {
		return this.viewCount;
	}

	public void setViewCount(Integer viewCount) {
		this.viewCount = viewCount;
	}

	public Integer getFavoriteCount() {
		return this.favoriteCount;
	}

	public void setFavoriteCount(Integer favoriteCount) {
		this.favoriteCount = favoriteCount;
	}

	public Integer getPlusCount() {
		return this.plusCount;
	}

	public void setPlusCount(Integer plusCount) {
		this.plusCount = plusCount;
	}

	public String getHtmlPath() {
		return this.htmlPath;
	}

	public void setHtmlPath(String htmlPath) {
		this.htmlPath = htmlPath;
	}

	public Short getIntegrity() {
		return this.integrity;
	}

	public void setIntegrity(Short integrity) {
		this.integrity = integrity;
	}

	public Date getGmtPublish() {
		return this.gmtPublish;
	}

	public void setGmtPublish(Date gmtPublish) {
		this.gmtPublish = gmtPublish;
	}

	public Date getGmtRefresh() {
		return this.gmtRefresh;
	}

	public void setGmtRefresh(Date gmtRefresh) {
		this.gmtRefresh = gmtRefresh;
	}

	public Short getValidDays() {
		return this.validDays;
	}

	public void setValidDays(Short validDays) {
		this.validDays = validDays;
	}

	public Short getDelStatus() {
		return this.delStatus;
	}

	public void setDelStatus(Short delStatus) {
		this.delStatus = delStatus;
	}

	public Short getPauseStatus() {
		return this.pauseStatus;
	}

	public void setPauseStatus(Short pauseStatus) {
		this.pauseStatus = pauseStatus;
	}

	public Short getCheckStatus() {
		return this.checkStatus;
	}

	public void setCheckStatus(Short checkStatus) {
		this.checkStatus = checkStatus;
	}

	public String getCheckAdmin() {
		return this.checkAdmin;
	}

	public void setCheckAdmin(String checkAdmin) {
		this.checkAdmin = checkAdmin;
	}

	public String getCheckRefuse() {
		return this.checkRefuse;
	}

	public void setCheckRefuse(String checkRefuse) {
		this.checkRefuse = checkRefuse;
	}

	public Date getGmtCheck() {
		return this.gmtCheck;
	}

	public void setGmtCheck(Date gmtCheck) {
		this.gmtCheck = gmtCheck;
	}

	public Date getGmtCreated() {
		return this.gmtCreated;
	}

	public void setGmtCreated(Date gmtCreated) {
		this.gmtCreated = gmtCreated;
	}

	public Date getGmtModified() {
		return this.gmtModified;
	}

	public void setGmtModified(Date gmtModified) {
		this.gmtModified = gmtModified;
	}

	public void setGmtExpired(Date gmtExpired) {
		this.gmtExpired = gmtExpired;
	}

	public Date getGmtExpired() {
		return gmtExpired;
	}

	public Integer getInfoComeFrom() {
		return infoComeFrom;
	}

	public void setInfoComeFrom(Integer infoComeFrom) {
		this.infoComeFrom = infoComeFrom;
	}

}