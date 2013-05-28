package com.zz91.mission.domain.financial;

import java.io.Serializable;
import java.util.Date;

public class ReportCashFlow implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer id;
	private String codeCoa;
	private String codeItemCash;
	private String codeItemDept;
	private Long cyDrSum;
	private Long cyCrSum;
	private Date gmtReport;
	private Date gmtCreated;
	private Date gmtModified;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getCodeCoa() {
		return codeCoa;
	}
	public void setCodeCoa(String codeCoa) {
		this.codeCoa = codeCoa;
	}
	public String getCodeItemCash() {
		return codeItemCash;
	}
	public void setCodeItemCash(String codeItemCash) {
		this.codeItemCash = codeItemCash;
	}
	public String getCodeItemDept() {
		return codeItemDept;
	}
	public void setCodeItemDept(String codeItemDept) {
		this.codeItemDept = codeItemDept;
	}
	public Long getCyDrSum() {
		return cyDrSum;
	}
	public void setCyDrSum(Long cyDrSum) {
		this.cyDrSum = cyDrSum;
	}
	public Long getCyCrSum() {
		return cyCrSum;
	}
	public void setCyCrSum(Long cyCrSum) {
		this.cyCrSum = cyCrSum;
	}
	public Date getGmtReport() {
		return gmtReport;
	}
	public void setGmtReport(Date gmtReport) {
		this.gmtReport = gmtReport;
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
