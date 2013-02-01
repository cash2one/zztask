package com.zz91.mission.front;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;

/**
 * 修正数据 同步 同步crm_cs_log昨天小计的时间与crm_cs最后联系时间同步
 * 
 * @author kongsj
 * @date 2013-2-1
 */
public class CrmCsInfoUpdateTask implements ZZTask {

	final static String DATE_FORMAT = "yyyy-MM-dd";
	final static String DB = "ast";

	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		// 搜索有新小计的客户crm_cs_log
		Map<Integer, String> map = queryCrmCsLog(DateUtil.toString(baseDate,
				DATE_FORMAT), DateUtil.toString(DateUtil.getDateAfterDays(
				baseDate, 1), DATE_FORMAT));

		// 更新这批客户的crm_cs的最后联系时间
		for (Integer companyId : map.keySet()) {
			updateCrmCs(companyId, map.get(companyId));
		}
		return true;
	}

	@Override
	public boolean init() throws Exception {
		return false;
	}

	private Map<Integer, String> queryCrmCsLog(String from, String to) {
		final Map<Integer, String> map = new HashMap<Integer, String>();

		String sql = "SELECT company_id,gmt_created FROM ast.crm_cs_log where gmt_created >='"
				+ from + "' and gmt_created <'" + to + "' order by id asc";
		DBUtils.select(DB, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					map.put(rs.getInt(1), rs.getString(2));
				}
			}
		});
		return map;
	}
	
	private void updateCrmCs(Integer companyId,String date){
		String sql = "UPDATE crm_cs SET gmt_visit = '"+date+"',gmt_modified = now() WHERE company_id = "+companyId +" and gmt_visit < '"+date+"'";
		DBUtils.insertUpdate(DB, sql);
	}

	public static void main(String[] args) throws Exception {

		DBPoolFactory.getInstance().init(
				"file:/usr/tools/config/db/db-zztask-jdbc.properties");

		Date baseDate = DateUtil.getDate("2013-01-31", "yyyy-MM-dd");

		CrmCsInfoUpdateTask obj = new CrmCsInfoUpdateTask();
		obj.exec(baseDate);

	}
}
