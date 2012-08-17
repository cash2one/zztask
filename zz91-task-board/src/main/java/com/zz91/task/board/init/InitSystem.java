package com.zz91.task.board.init;

import org.apache.log4j.Logger;

import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.search.SorlUtil;

/**
 * 系统启动时加载数据库中的任务信息
 * 
 * @author mays (mays@zz91.com)
 * 
 *         created on 2011-3-16
 */
public class InitSystem {

	final static Logger LOG = Logger.getLogger(InitSystem.class);
	
	public void init() {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		SorlUtil.getInstance().init("file:/usr/tools/config/search/search.properties");
	}

}
