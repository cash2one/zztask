package com.zz91.mission.zz91;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;

/**
 * author:kongsj date:2013-7-13
 */
public class SystemInquiryTask implements ZZTask {

	public final static String DB = "ast";

	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		Map<String, String> accountMap = new HashMap<String, String>();
		do {

			String from = DateUtil.toString(baseDate, "yyyy-MM-dd");
			String to = DateUtil.toString(DateUtil.getDateAfterDays(baseDate,
					1), "yyyy-MM-dd");
			String sql = "SELECT id,company_id,target_id,title,content "
					+ "FROM inquiry_task "
					+ "where  post_status='0' and post_time >= '" + from
					+ "' and post_time < '" + to + "' limit 100";
			final Map<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();
			// 组装发布的信息
			DBUtils.select(DB, sql, new IReadDataHandler() {
				@Override
				public void handleRead(ResultSet rs) throws SQLException {
					while (rs.next()) {
						Map<String, Object> resultMap = new HashMap<String, Object>();
						resultMap.put("id", rs.getString(1));
						resultMap.put("companyId", rs.getString(2));
						resultMap.put("targetId", rs.getString(3));
						resultMap.put("title", rs.getString(4));
						resultMap.put("content", rs.getString(5));
						map.put(rs.getString(1), resultMap);
					}
				}
			});
			

			// 跳出循环
			if (map.size() < 1) {
				break;
			}
			
			for (String id : map.keySet()) {
				putAccountMap(map.get(id).get("companyId").toString(), accountMap);
				putAccountMap(map.get(id).get("targetId").toString(), accountMap);
			}

			for (String id : map.keySet()) {
				String senderAccount = accountMap.get(map.get(id).get("companyId"));
				String receiverAccount = accountMap.get(map.get(id).get("targetId"));
				String updateSql = "update inquiry_task SET post_status='1' where id=" + id;
				DBUtils.insertUpdate(DB, updateSql);
				String insertSql = "INSERT INTO inquiry (" + "title,"
						+ "content," + "be_inquired_type," + "be_inquired_id,"
						+ "inquired_type," + "sender_account,"
						+ "receiver_account," + "batch_send_type,"
						+ "is_rubbish," + "send_time," + "gmt_created,"
						+ "gmt_modified" + ") " + "VALUES" + "('"
						+ map.get(id).get("title") + "','"
						+ map.get(id).get("content") + "',1,"
						+ map.get(id).get("targetId") + ",3,'" + senderAccount
						+ "','" + receiverAccount + "',0,0,now(),now(),now())";
				DBUtils.insertUpdate(DB, insertSql);
			}
		} while (true);
		return true;
	}

	@Override
	public boolean init() throws Exception {
		return false;
	}
	
	private void putAccountMap(final String companyId,final Map<String, String>accountMap){
		String sql = "select account from company_account where company_id="+companyId;
		if(accountMap.get(companyId)==null){
			DBUtils.select(DB, sql, new IReadDataHandler() {
				@Override
				public void handleRead(ResultSet rs) throws SQLException {
					while (rs.next()) {
						accountMap.put(companyId, rs.getString(1));
					}
				}
			});
		}
	}
	
	public static void main(String[] args) throws ParseException, Exception {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		SystemInquiryTask sit = new SystemInquiryTask();
		sit.exec(DateUtil.getDate("2013-7-15", "yyyy-MM-dd"));
	}

}
