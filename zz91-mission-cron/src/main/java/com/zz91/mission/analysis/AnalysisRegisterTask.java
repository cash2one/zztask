package com.zz91.mission.analysis;

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
 * @Author:kongsj
 * @Date:2012-3-19
 */
public class AnalysisRegisterTask implements ZZTask {

	private final static String DB = "ast";
	private final static String LOG_DATE_FORMAT = "yyyy-MM-dd";

	@Override
	public boolean init() throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		String startDate = DateUtil.toString(DateUtil.getDateAfterDays(baseDate, -1), LOG_DATE_FORMAT);
		String endDate = DateUtil.toString(baseDate, LOG_DATE_FORMAT);
		
		final Map<String, Integer> resultMap = new HashMap<String, Integer>();

		String sql="select regfrom_code from company where '"
			+ startDate + "'<= regtime and regtime < '" + endDate + "'";

		DBUtils.select(DB, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					String code =  rs.getString(1);
					Integer num = resultMap.get(code);
					if(resultMap.get(code)!=null){
						resultMap.put(code, ++num);
					}else{
						resultMap.put(code, 1);
					}
				}
			}
		});

		for(String code:resultMap.keySet()){
			saveToDB(code, resultMap.get(code), startDate);
		}
		return true;
	}
	
	private void saveToDB(String regfromCode, Integer num, String gmtTarget){
		String sql="insert into analysis_register(regfrom_code, num, gmt_target, gmt_created, gmt_modified) values('"
			+regfromCode+"',"+num+",'"+gmtTarget+"',now(),now())";
		DBUtils.insertUpdate(DB, sql);
	}

	@Override
	public boolean clear(Date baseDate) throws Exception {
		String targetDate=DateUtil.toString(DateUtil.getDateAfterDays(baseDate, -1), LOG_DATE_FORMAT);
		return DBUtils.insertUpdate(DB, "delete from analysis_register where gmt_target='"+targetDate+"'");
	}

	public static void main(String[] args) throws Exception {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		long start=System.currentTimeMillis();
		AnalysisRegisterTask task = new AnalysisRegisterTask();
		task.clear(DateUtil.getDate("2011-12-4", "yyyy-MM-dd"));
		task.exec(DateUtil.getDate("2011-12-4", "yyyy-MM-dd"));
		long end=System.currentTimeMillis();
		System.out.println("共耗时："+(end-start));
	}
}