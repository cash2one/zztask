package com.zz91.task.board.init;

import org.apache.log4j.Logger;

import com.zz91.util.db.pool.DBPoolFactory;

/**
 * 系统启动时加载数据库中的任务信息
 * 
 * @author mays (mays@zz91.com)
 * 
 *         created on 2011-3-16
 */
public class InitSystem {

	final static Logger LOG = Logger.getLogger(InitSystem.class);
	
	private String propFile="classpath:db-zztask.properties";

	public void init() {
		DBPoolFactory.getInstance().init(propFile);
	}

	/**
	 * @return the propFile
	 */
	public String getPropFile() {
		return propFile;
	}

	/**
	 * @param propFile the propFile to set
	 */
	public void setPropFile(String propFile) {
		this.propFile = propFile;
	}
	
	
}
