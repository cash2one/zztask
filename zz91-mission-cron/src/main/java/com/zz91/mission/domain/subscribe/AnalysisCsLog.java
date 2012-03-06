package com.zz91.mission.domain.subscribe;

import java.io.Serializable;
import java.util.Date;

public class AnalysisCsLog implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String csAccount;
	private Integer star0Y;
	private Integer star0N;
	private Integer star1Y;
	private Integer star1N;
	private Integer star2Y;
	private Integer star2N;
	private Integer star3Y;
	private Integer star3N;
	private Integer star4Y;
	private Integer star4N;
	private Integer star5Y;
	private Integer star5N;
	private Integer saleCall;
	private Integer serviceCall;
	private Integer analysisDate;
	private Date gmtCreated;
	private Date gmtModified;
	
	public AnalysisCsLog(String csAccount, Integer star0y, Integer star0n,
			Integer star1y, Integer star1n, Integer star2y, Integer star2n,
			Integer star3y, Integer star3n, Integer star4y, Integer star4n,
			Integer star5y, Integer star5n, Integer saleCall,
			Integer serviceCall, Integer analysisDate, Date gmtCreated,
			Date gmtModified) {
		super();
		this.csAccount = csAccount;
		star0Y = star0y;
		star0N = star0n;
		star1Y = star1y;
		star1N = star1n;
		star2Y = star2y;
		star2N = star2n;
		star3Y = star3y;
		star3N = star3n;
		star4Y = star4y;
		star4N = star4n;
		star5Y = star5y;
		star5N = star5n;
		this.saleCall = saleCall;
		this.serviceCall = serviceCall;
		this.analysisDate = analysisDate;
		this.gmtCreated = gmtCreated;
		this.gmtModified = gmtModified;
	}
	
	public String getCsAccount() {
		return csAccount;
	}
	public void setCsAccount(String csAccount) {
		this.csAccount = csAccount;
	}
	public Integer getStar0Y() {
		return star0Y;
	}
	public void setStar0Y(Integer star0y) {
		star0Y = star0y;
	}
	public Integer getStar0N() {
		return star0N;
	}
	public void setStar0N(Integer star0n) {
		star0N = star0n;
	}
	public Integer getStar1Y() {
		return star1Y;
	}
	public void setStar1Y(Integer star1y) {
		star1Y = star1y;
	}
	public Integer getStar1N() {
		return star1N;
	}
	public void setStar1N(Integer star1n) {
		star1N = star1n;
	}
	public Integer getStar2Y() {
		return star2Y;
	}
	public void setStar2Y(Integer star2y) {
		star2Y = star2y;
	}
	public Integer getStar2N() {
		return star2N;
	}
	public void setStar2N(Integer star2n) {
		star2N = star2n;
	}
	public Integer getStar3Y() {
		return star3Y;
	}
	public void setStar3Y(Integer star3y) {
		star3Y = star3y;
	}
	public Integer getStar3N() {
		return star3N;
	}
	public void setStar3N(Integer star3n) {
		star3N = star3n;
	}
	public Integer getStar4Y() {
		return star4Y;
	}
	public void setStar4Y(Integer star4y) {
		star4Y = star4y;
	}
	public Integer getStar4N() {
		return star4N;
	}
	public void setStar4N(Integer star4n) {
		star4N = star4n;
	}
	public Integer getStar5Y() {
		return star5Y;
	}
	public void setStar5Y(Integer star5y) {
		star5Y = star5y;
	}
	public Integer getStar5N() {
		return star5N;
	}
	public void setStar5N(Integer star5n) {
		star5N = star5n;
	}
	public Integer getSaleCall() {
		return saleCall;
	}
	public void setSaleCall(Integer saleCall) {
		this.saleCall = saleCall;
	}
	public Integer getServiceCall() {
		return serviceCall;
	}
	public void setServiceCall(Integer serviceCall) {
		this.serviceCall = serviceCall;
	}
	public Integer getAnalysisDate() {
		return analysisDate;
	}
	public void setAnalysisDate(Integer analysisDate) {
		this.analysisDate = analysisDate;
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
}
