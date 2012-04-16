package com.zz91.mission.front;

import java.util.Date;

import com.zz91.task.common.ZZTask;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.pool.DBPoolFactory;

public class ClearDeletedInquiry implements ZZTask {

	@Override
	public boolean init() throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		DBUtils.insertUpdate("ast", "delete from inquiry where is_sender_del=1 and is_receiver_del=1");
		return true;
	}

	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}
	public static void main(String[] args) {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		ClearDeletedInquiry a=new ClearDeletedInquiry();
		Date baseDate=new Date();
		try {
			a.exec(baseDate);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
