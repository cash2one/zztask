/**
 * Copyright 2011 ASTO.
 * All right reserved.
 * Created on 2011-3-16
 */
package com.zz91.mission.demo;

import java.sql.Connection;
import java.util.Date;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;

/**
 * @author mays (mays@zz91.com)
 * 
 *         created on 2011-3-16
 */
public class DemoTask implements ZZTask {

	@Override
	public boolean clear(Date baseDate) throws Exception {
		System.out.println("任务：DEMO正在清理");
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		return DBUtils.insertUpdate("task",
				"insert into test(col1,col2) values('"
						+ DateUtil.getDate(baseDate, "yyyy-MM-dd hh:mm:ss")
						+ "','task is run')");
	}

	@Override
	public boolean init() throws Exception {
		System.out.println("任务：DEMO正在被初始化");
		return false;
	}
}
