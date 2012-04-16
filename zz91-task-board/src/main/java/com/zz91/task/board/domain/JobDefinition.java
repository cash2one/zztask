package com.zz91.task.board.domain;

import java.io.Serializable;
import java.util.Date;

public class JobDefinition implements Serializable {
	
	
	private static final long serialVersionUID = 1643190952324185481L;

	private Integer id;
	private Date gmtCreated;
	private Date gmtModified;
	private String jobName;
	private String jobGroup;
	private String jobClasspath;
	private String jobClassName;
	private String description;
	private String cron;
	private String isInUse; //停用0启用1
	private Date startTime;
	private Date endTime;
	private Long nextFireTime;
	
	public JobDefinition() {
	}

	/**
	 * @param id
	 * @param gmtCreated
	 * @param gmtModified
	 * @param jobName
	 * @param jobGroup
	 * @param jobClasspath
	 * @param jobClassName
	 * @param description
	 * @param cron
	 * @param isInUse
	 * @param startTime
	 * @param endTime
	 */
	public JobDefinition(Integer id, Date gmtCreated, Date gmtModified,
			String jobName, String jobGroup, String jobClasspath,
			String jobClassName, String description, String cron,
			String isInUse, Date startTime, Date endTime) {
		super();
		this.id = id;
		this.gmtCreated = gmtCreated;
		this.gmtModified = gmtModified;
		this.jobName = jobName;
		this.jobGroup = jobGroup;
		this.jobClasspath = jobClasspath;
		this.jobClassName = jobClassName;
		this.description = description;
		this.cron = cron;
		this.isInUse = isInUse;
		this.startTime = startTime;
		this.endTime = endTime;
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
	 * @return the jobName
	 */
	public String getJobName() {
		return jobName;
	}

	/**
	 * @param jobName the jobName to set
	 */
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	/**
	 * @return the jobGroup
	 */
	public String getJobGroup() {
		return jobGroup;
	}

	/**
	 * @param jobGroup the jobGroup to set
	 */
	public void setJobGroup(String jobGroup) {
		this.jobGroup = jobGroup;
	}

	/**
	 * @return the jobClasspath
	 */
	public String getJobClasspath() {
		return jobClasspath;
	}

	/**
	 * @param jobClasspath the jobClasspath to set
	 */
	public void setJobClasspath(String jobClasspath) {
		this.jobClasspath = jobClasspath;
	}

	/**
	 * @return the jobClassName
	 */
	public String getJobClassName() {
		return jobClassName;
	}

	/**
	 * @param jobClassName the jobClassName to set
	 */
	public void setJobClassName(String jobClassName) {
		this.jobClassName = jobClassName;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the cron
	 */
	public String getCron() {
		return cron;
	}

	/**
	 * @param cron the cron to set
	 */
	public void setCron(String cron) {
		this.cron = cron;
	}


	/**
	 * @return the startTime
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the endTime
	 */
	public Date getEndTime() {
		return endTime;
	}

	/**
	 * @param endTime the endTime to set
	 */
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	/**
	 * @return the isInUse
	 */
	public String getIsInUse() {
		return isInUse;
	}

	/**
	 * @param isInUse the isInUse to set
	 */
	public void setIsInUse(String isInUse) {
		this.isInUse = isInUse;
	}

	/**
	 * @return the nextFireTime
	 */
	public Long getNextFireTime() {
		return nextFireTime;
	}

	/**
	 * @param nextFireTime the nextFireTime to set
	 */
	public void setNextFireTime(Long nextFireTime) {
		this.nextFireTime = nextFireTime;
	}
	
	
}

