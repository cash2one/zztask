package com.zz91.task.board.domain;

import java.util.Date;

public class JobStatus implements java.io.Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2324915422637310967L;
	
	public static final String SUCCESS="success";
	public static final String FAILED="failed";
	
	private Integer id;
	private String JobName;
	private Date gmtBasetime;
	private String result;
	private Long runtime;
	private Date gmtTrigger;
	private String errorMsg;
	private String category;
	private Integer numRetry;
	private Date gmtCreated;
	private Date gmtModified;
	
	/**
	 * 
	 */
	public JobStatus() {
	}
	
	/**
	 * @param jobName
	 * @param gmtBasetime
	 * @param result
	 * @param runtime
	 * @param gmtTrigger
	 * @param errorMsg
	 * @param category
	 * @param numRetry
	 * @param gmtCreated
	 * @param gmtModified
	 */
	public JobStatus(String jobName, Date gmtBasetime, String result,
			Long runtime, Date gmtTrigger, String errorMsg, String category,
			Integer numRetry, Date gmtCreated, Date gmtModified) {
		super();
		JobName = jobName;
		this.gmtBasetime = gmtBasetime;
		this.result = result;
		this.runtime = runtime;
		this.gmtTrigger = gmtTrigger;
		this.errorMsg = errorMsg;
		this.category = category;
		this.numRetry = numRetry;
		this.gmtCreated = gmtCreated;
		this.gmtModified = gmtModified;
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
	 * @return the jobName
	 */
	public String getJobName() {
		return JobName;
	}
	/**
	 * @param jobName the jobName to set
	 */
	public void setJobName(String jobName) {
		JobName = jobName;
	}
	/**
	 * @return the gmtBasetime
	 */
	public Date getGmtBasetime() {
		return gmtBasetime;
	}
	/**
	 * @param gmtBasetime the gmtBasetime to set
	 */
	public void setGmtBasetime(Date gmtBasetime) {
		this.gmtBasetime = gmtBasetime;
	}
	/**
	 * @return the result
	 */
	public String getResult() {
		return result;
	}
	/**
	 * @param result the result to set
	 */
	public void setResult(String result) {
		this.result = result;
	}
	/**
	 * @return the runtime
	 */
	public Long getRuntime() {
		return runtime;
	}
	/**
	 * @param runtime the runtime to set
	 */
	public void setRuntime(Long runtime) {
		this.runtime = runtime;
	}
	/**
	 * @return the gmtTrigger
	 */
	public Date getGmtTrigger() {
		return gmtTrigger;
	}
	/**
	 * @param gmtTrigger the gmtTrigger to set
	 */
	public void setGmtTrigger(Date gmtTrigger) {
		this.gmtTrigger = gmtTrigger;
	}
	/**
	 * @return the errorMsg
	 */
	public String getErrorMsg() {
		return errorMsg;
	}
	/**
	 * @param errorMsg the errorMsg to set
	 */
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}
	/**
	 * @param category the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}
	/**
	 * @return the numRetry
	 */
	public Integer getNumRetry() {
		return numRetry;
	}
	/**
	 * @param numRetry the numRetry to set
	 */
	public void setNumRetry(Integer numRetry) {
		this.numRetry = numRetry;
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
	
	
	
}
