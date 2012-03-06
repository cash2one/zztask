package com.zz91.mission.huzhu;

import java.util.Date;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;

/** 任务描述：清除一周以前的日志信息（按照一周前时间截）
 * @author qizj 
 * @email  qizj@zz91.net
 * @version 创建时间：2011-8-17 
 */
public class ClearBbsViewLogTask implements ZZTask {

	@Override
	public boolean init() throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		long target=DateUtil.getTheDayZero(baseDate, -7);
		String sql="delete from bbs_view_log where gmt_target="+target;
		DBUtils.insertUpdate("ast", sql);
		return true;
	}

	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}
	public static void main(String[] args) {
		ClearBbsViewLogTask task=new ClearBbsViewLogTask();
		try {
			task.exec(new Date());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
