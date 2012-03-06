/**
 * Copyright 2011 ASTO.
 * All right reserved.
 * Created on 2011-7-21
 */
package com.zz91.mission.domain.ads;

import java.io.Serializable;

/**
 * @author mays (mays@zz91.com)
 *
 * created on 2011-7-21
 */
public class AnalysisAdHit implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer adId;
	private String adTitle;
	private Integer adPositionId;
	private Integer numShow;
	private Integer numHit;
	private Integer numHitFirst;
	/**
	 * 
	 */
	public AnalysisAdHit() {
		super();
	}
	/**
	 * @param adId
	 * @param adTitle
	 * @param adPositionId
	 * @param numShow
	 * @param numHit
	 * @param numHitFirst
	 */
	public AnalysisAdHit(Integer adId, String adTitle, Integer adPositionId,
			Integer numShow, Integer numHit, Integer numHitFirst) {
		super();
		this.adId = adId;
		this.adTitle = adTitle;
		this.adPositionId = adPositionId;
		this.numShow = numShow;
		this.numHit = numHit;
		this.numHitFirst = numHitFirst;
	}
	/**
	 * @return the adId
	 */
	public Integer getAdId() {
		return adId;
	}
	/**
	 * @param adId the adId to set
	 */
	public void setAdId(Integer adId) {
		this.adId = adId;
	}
	/**
	 * @return the adTitle
	 */
	public String getAdTitle() {
		return adTitle;
	}
	/**
	 * @param adTitle the adTitle to set
	 */
	public void setAdTitle(String adTitle) {
		this.adTitle = adTitle;
	}
	/**
	 * @return the adPositionId
	 */
	public Integer getAdPositionId() {
		return adPositionId;
	}
	/**
	 * @param adPositionId the adPositionId to set
	 */
	public void setAdPositionId(Integer adPositionId) {
		this.adPositionId = adPositionId;
	}
	/**
	 * @return the numShow
	 */
	public Integer getNumShow() {
		return numShow;
	}
	/**
	 * @param numShow the numShow to set
	 */
	public void setNumShow(Integer numShow) {
		this.numShow = numShow;
	}
	/**
	 * @return the numHit
	 */
	public Integer getNumHit() {
		return numHit;
	}
	/**
	 * @param numHit the numHit to set
	 */
	public void setNumHit(Integer numHit) {
		this.numHit = numHit;
	}
	/**
	 * @return the numHitFirst
	 */
	public Integer getNumHitFirst() {
		return numHitFirst;
	}
	/**
	 * @param numHitFirst the numHitFirst to set
	 */
	public void setNumHitFirst(Integer numHitFirst) {
		this.numHitFirst = numHitFirst;
	}
	
	
}
