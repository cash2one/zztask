/**
 * 
 */
package com.zz91.task.common.domain;

import java.io.Serializable;

/**
 * @author mays
 *
 */
public class IdxTaskDefine implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String jobName;
	private Long start;
	private Long end;
//	private String solrModel;
	
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public Long getStart() {
		return start;
	}
	public void setStart(Long start) {
		this.start = start;
	}
	public Long getEnd() {
		return end;
	}
	public void setEnd(Long end) {
		this.end = end;
	}
//	public String getSolrModel() {
//		return solrModel;
//	}
//	public void setSolrModel(String solrModel) {
//		this.solrModel = solrModel;
//	}
	
}
