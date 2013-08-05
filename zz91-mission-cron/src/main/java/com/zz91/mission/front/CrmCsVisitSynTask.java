package com.zz91.mission.front;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.log.LogUtil;

/**
 *	author:kongsj
 *	date:2013-8-5
 */
/**
 * 最后一次联系时间crm_cs表数据同步
 */
public class CrmCsVisitSynTask implements ZZTask{

	private static final String DB = "ast";
	
	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		String sql = "SELECT company_id FROM crm_cs where gmt_visit is null";
		
		final List<Integer> list = new ArrayList<Integer>();
		DBUtils.select(DB, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					list.add(rs.getInt(1));
				}
			}
		});
		
		final Map<Integer, String> map =new HashMap<Integer, String>();
		for(final Integer i:list){
			String crmCsSql = "SELECT gmt_created FROM crm_cs_log where company_id ="+i+" ORDER BY gmt_created desc limit 1";
			DBUtils.select(DB, crmCsSql, new IReadDataHandler() {
				
				@Override
				public void handleRead(ResultSet rs) throws SQLException {
					while (rs.next()) {
						map.put(i, DateUtil.toString(rs.getDate(1), "yyyy-MM-dd"));
					}
				}
			});
		}
		for (Integer companyId:map.keySet()) {
			String updateSql = "update crm_cs set gmt_visit = '"+map.get(companyId)+"' where company_id = "+companyId; 
			DBUtils.insertUpdate(DB, updateSql);
		}
		
		return true;
	}

	@Override
	public boolean init() throws Exception {
		return false;
	}
	
	public static void main(String[] args) throws ParseException, Exception {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		LogUtil.getInstance().init("web.properties");

		CrmCsVisitSynTask task = new CrmCsVisitSynTask();
		task.exec(DateUtil.getDate("2012-01-02", "yyyy-MM-dd"));
	}

}
