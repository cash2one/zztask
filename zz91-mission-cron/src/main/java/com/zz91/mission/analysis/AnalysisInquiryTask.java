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
public class AnalysisInquiryTask implements ZZTask {

	private String DB = "ast";
	private String LOG_DATE_FORMAT = "yyyy-MM-dd";

	@Override
	public boolean init() throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		String startDate = DateUtil.toString(
				DateUtil.getDateAfterDays(baseDate, -1), LOG_DATE_FORMAT);
		String endDate = DateUtil.toString(baseDate, LOG_DATE_FORMAT);
		String sql = "select be_inquired_type,be_inquired_id from inquiry where '"
				+ startDate + "'<= send_time and send_time < '" + endDate + "'";
		final Map<String,Integer> resultMap = new HashMap<String,Integer>();
		
		DBUtils.select(DB, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					Integer num = resultMap.get(rs.getString(1)+","+rs.getInt(2));
					if(num==null){
						resultMap.put(rs.getString(1)+","+rs.getInt(2), 1);
					}else{
						resultMap.put(rs.getString(1)+","+rs.getInt(2), ++num);
					}
				}
			}
		});
		
		for(String code:resultMap.keySet()){
			String codes[] = code.split(",");
			saveToDB(codes[0],codes[1],startDate,resultMap.get(code));
		}
		
		return true;
	}
	
	private void saveToDB(String inquiryType,String inquiryTarget,String gmtTarget,Integer num){
		String sql = "insert into analysis_inquiry (inquiry_type,inquiry_target,num,gmt_target,gmt_created,gmt_modified) values('" 
			+ inquiryType + "'," + inquiryTarget + ","+ num +",'" + gmtTarget + "',now(),now())";
		DBUtils.insertUpdate(DB, sql);
	}

	@Override
	public boolean clear(Date baseDate) throws Exception {
		String targetDate=DateUtil.toString(DateUtil.getDateAfterDays(baseDate, -1), LOG_DATE_FORMAT);
		return DBUtils.insertUpdate(DB, "delete from analysis_inquiry where gmt_target='"+targetDate+"'");
	}

	public static void main(String[] args) throws Exception{
		
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		
		long start=System.currentTimeMillis();
		AnalysisInquiryTask analysis=new AnalysisInquiryTask();
		analysis.clear(DateUtil.getDate("2011-12-06", "yyyy-MM-dd"));
		analysis.exec(DateUtil.getDate("2011-12-06", "yyyy-MM-dd"));
		long end=System.currentTimeMillis();
		System.out.println("共耗时："+(end-start));
	}
}
