package com.zz91.mission.myrc;

import java.util.Date;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.pool.DBPoolFactory;

public class SendSmsPriceForMyrcTask implements ZZTask{
	/**
	 * 1,搜索订阅过的类别得到公司id
	 *   1.1  
	 * 2,通过公司id获取到邮箱地址
	 * 3,组装内容
	 * 4,通过邮件接口进行发送
	 * 
	 */

	@Override
	public boolean clear(Date baseDate) throws Exception {
			
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
			
		return false;
	}

	@Override
	public boolean init() throws Exception {
			
		return false;
	}
	
	public static void main(String[] args) throws Exception {
		DBPoolFactory.getInstance().init(
				"file:/usr/tools/config/db/db-zztask-jdbc.properties");
		SendSmsPriceForMyrcTask obj = new SendSmsPriceForMyrcTask();
		Date date = DateUtil.getDate("2012-11-20 ", "yyyy-MM-dd");
		obj.exec(date);
	}

}
