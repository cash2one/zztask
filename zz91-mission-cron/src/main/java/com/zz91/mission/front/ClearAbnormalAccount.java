package com.zz91.mission.front;

import java.util.Date;

import com.zz91.task.common.ZZTask;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.pool.DBPoolFactory;

public class ClearAbnormalAccount implements ZZTask {

	/**
	 * 将auth_user表中steping值不为0的数据清空
	 */
	
	@Override
	public boolean init() throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		DBUtils.insertUpdate("ast","delete from auth_user where steping<>0");
		return true;
	}

	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}
	
	public static void main(String[] args) {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		ClearAbnormalAccount a=new ClearAbnormalAccount();
		Date baseDate=new Date();
		try {
			a.exec(baseDate);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
