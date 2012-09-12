/*
 * 文件名称：CompProfile.java
 * 创建者　：涂灵峰
 * 创建时间：2012-4-18 下午3:46:56
 * 版本号　：1.0.0
 */
package com.zz91.mission.domain.ep;

import java.util.Date;

/**
 * 项目名称：中国环保网
 * 模块编号：数据持久层
 * 模块描述：公司详细信息实体类。
 * 变更履历：修改日期　　　　　修改者　　　　　　　版本号　　　　　修改内容
 *　　　　　 2012-04-18　　　涂灵峰　　　　　　　1.0.0　　　　　创建类文件
 */
public class CompProfile implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private Integer id;// 编号
	private String name;// 公司名称
	private String details;// 公司介绍
	private String industryCode;// 行业类别
	private Short mainBuy;// 是否是求购商
	private String mainProductBuy;//  求购产品
	private Short mainSupply;// 是否是供应商
	private String mainProductSupply;// 供应产品
	private String memberCode;// 会员类型
	private String memberCodeBlock;// 非正常用户的member_code
	private String registerCode;// 注册来源
	private String businessCode;// 业务类型
	private String areaCode;// 地区
	private String provinceCode;// 省份
	private String legal;// 法人
	private String funds;// 注册资金
	private String mainBrand;// 主要品牌
	private String address;// 地址
	private String addressZip;// 邮编
	private String domain;// 公司网址
	private String domainTwo;// 二级域名
	private Integer messageCount;// 消息数
	private Integer viewCount;// 查看次数
	private String tags;// 标签
	private String detailsQuery;// 详细信息（纯文本）
	private Date gmtCreated;// 创建日期
	private Date gmtModified;// 修改日期
	private Integer delStatus;// 删除标记
	private String processMethod;// 加工方式
	private String process;// 工艺
	private String employeeNum;// 员工人数
	private String developerNum;// 研发部门人数
	private String plantArea;// 厂房面积
	private String mainMarket;// 主要市场
	private String mainCustomer;// 主要客户
	private String monthOutput;// 月产量
	private String yearTurnover;// 年营业额
	private String yearExports;// 年出口额
	private String qualityControl;// 质量控制
	private String registerArea;// 企业注册地区
	private String enterpriseType;// 企业类型
	private Date sendTime;//发送询盘时间
	private Date receiveTime;//接收询盘时间 
	private String operName;//最后操作人

	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	public Date getReceiveTime() {
		return receiveTime;
	}

	public void setReceiveTime(Date receiveTime) {
		this.receiveTime = receiveTime;
	}

	public String getOperName() {
		return operName;
	}

	public void setOperName(String operName) {
		this.operName = operName;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDetails() {
		return this.details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String getIndustryCode() {
		return this.industryCode;
	}

	public void setIndustryCode(String industryCode) {
		this.industryCode = industryCode;
	}

	public Short getMainBuy() {
		return this.mainBuy;
	}

	public void setMainBuy(Short mainBuy) {
		this.mainBuy = mainBuy;
	}

	public String getMainProductBuy() {
		return this.mainProductBuy;
	}

	public void setMainProductBuy(String mainProductBuy) {
		this.mainProductBuy = mainProductBuy;
	}

	public Short getMainSupply() {
		return this.mainSupply;
	}

	public void setMainSupply(Short mainSupply) {
		this.mainSupply = mainSupply;
	}

	public String getMainProductSupply() {
		return this.mainProductSupply;
	}

	public void setMainProductSupply(String mainProductSupply) {
		this.mainProductSupply = mainProductSupply;
	}

	public String getMemberCode() {
		return this.memberCode;
	}

	public void setMemberCode(String memberCode) {
		this.memberCode = memberCode;
	}

	public String getMemberCodeBlock() {
		return this.memberCodeBlock;
	}

	public void setMemberCodeBlock(String memberCodeBlock) {
		this.memberCodeBlock = memberCodeBlock;
	}

	public String getRegisterCode() {
		return this.registerCode;
	}

	public void setRegisterCode(String registerCode) {
		this.registerCode = registerCode;
	}

	public String getBusinessCode() {
		return this.businessCode;
	}

	public void setBusinessCode(String businessCode) {
		this.businessCode = businessCode;
	}

	public String getAreaCode() {
		return this.areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	public String getProvinceCode() {
		return this.provinceCode;
	}

	public void setProvinceCode(String provinceCode) {
		this.provinceCode = provinceCode;
	}

	public String getLegal() {
		return this.legal;
	}

	public void setLegal(String legal) {
		this.legal = legal;
	}

	public String getFunds() {
		return this.funds;
	}

	public void setFunds(String funds) {
		this.funds = funds;
	}

	public String getMainBrand() {
		return this.mainBrand;
	}

	public void setMainBrand(String mainBrand) {
		this.mainBrand = mainBrand;
	}

	public String getAddress() {
		return this.address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddressZip() {
		return this.addressZip;
	}

	public void setAddressZip(String addressZip) {
		this.addressZip = addressZip;
	}

	public String getDomain() {
		return this.domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getDomainTwo() {
		return this.domainTwo;
	}

	public void setDomainTwo(String domainTwo) {
		this.domainTwo = domainTwo;
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

	public String getTags() {
		return this.tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getDetailsQuery() {
		return this.detailsQuery;
	}

	public void setDetailsQuery(String detailsQuery) {
		this.detailsQuery = detailsQuery;
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

	public Integer getDelStatus() {
		return delStatus;
	}

	public void setDelStatus(Integer delStatus) {
		this.delStatus = delStatus;
	}

	public String getProcessMethod() {
		return processMethod;
	}

	public void setProcessMethod(String processMethod) {
		this.processMethod = processMethod;
	}

	public String getProcess() {
		return process;
	}

	public void setProcess(String process) {
		this.process = process;
	}

	public String getEmployeeNum() {
		return employeeNum;
	}

	public void setEmployeeNum(String employeeNum) {
		this.employeeNum = employeeNum;
	}

	public String getDeveloperNum() {
		return developerNum;
	}

	public void setDeveloperNum(String developerNum) {
		this.developerNum = developerNum;
	}

	public String getPlantArea() {
		return plantArea;
	}

	public void setPlantArea(String plantArea) {
		this.plantArea = plantArea;
	}

	public String getMainMarket() {
		return mainMarket;
	}

	public void setMainMarket(String mainMarket) {
		this.mainMarket = mainMarket;
	}

	public String getMainCustomer() {
		return mainCustomer;
	}

	public void setMainCustomer(String mainCustomer) {
		this.mainCustomer = mainCustomer;
	}

	public String getMonthOutput() {
		return monthOutput;
	}

	public void setMonthOutput(String monthOutput) {
		this.monthOutput = monthOutput;
	}

	public String getYearTurnover() {
		return yearTurnover;
	}

	public void setYearTurnover(String yearTurnover) {
		this.yearTurnover = yearTurnover;
	}

	public String getYearExports() {
		return yearExports;
	}

	public void setYearExports(String yearExports) {
		this.yearExports = yearExports;
	}

	public String getQualityControl() {
		return qualityControl;
	}

	public void setQualityControl(String qualityControl) {
		this.qualityControl = qualityControl;
	}

	public String getRegisterArea() {
		return registerArea;
	}

	public void setRegisterArea(String registerArea) {
		this.registerArea = registerArea;
	}

	public String getEnterpriseType() {
		return enterpriseType;
	}

	public void setEnterpriseType(String enterpriseType) {
		this.enterpriseType = enterpriseType;
	}

}