package com.zz91.mission.zz91;

import java.util.Date;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.http.HttpUtils;

/**
 * author:kongsj date:2013-7-15
 */
public class SystemCPPTask implements ZZTask {

	final static String ADMIN_URL = "http://admin1949.zz91.com/web/zz91/phone/getBill.htm";

	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		String to = DateUtil.toString(baseDate, "yyyy-MM-dd");
		String from = DateUtil.toString(DateUtil.getDateAfterDays(baseDate, -1), "yyyy-MM-dd");
		String url = ADMIN_URL + "?from=" + from + "&to=" + to;
		String result = HttpUtils.getInstance().httpGet(url, HttpUtils.CHARSET_UTF8);
		System.out.println(result);
		return true;
	}

	@Override
	public boolean init() throws Exception {
		return false;
	}

	public static void main(String[] args) throws Exception {
		SystemCPPTask systemCPPTask = new SystemCPPTask();
		systemCPPTask.exec(new Date());
	}
}
