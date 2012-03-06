/**
 * 
 */
package com.zz91.mission.front;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.pool.DBPoolFactory;

/**
 * @author root
 *
 */
public class AnalysisLogin implements ZZTask{

	public final static String DB="ast";
	
	static String LOG_FILE = "/usr/data/log4z/zz91/run.";
	final static String LOG_DATE_FORMAT = "yyyy-MM-dd";
	
	final static String OPERATION="operation";
	final static String OPERATOR="operator";
	static String OPERATION_VALUE="login";
	
	@Override
	public boolean clear(Date baseDate) throws Exception {
		String targetDate=DateUtil.toString(DateUtil.getDateAfterDays(baseDate, -1), LOG_DATE_FORMAT);
		return DBUtils.insertUpdate(DB, "delete from analysis_login where gmt_target='"+targetDate+"'");
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		String targetDate=DateUtil.toString(DateUtil.getDateAfterDays(baseDate, -1), LOG_DATE_FORMAT);
		
		Map<Integer, Integer> resultMap=new HashMap<Integer, Integer>();
		
		BufferedReader br = new BufferedReader(new FileReader(LOG_FILE+targetDate));
		
		String line;
		Integer num=null;
		while ((line = br.readLine()) != null) {
			JSONObject jobj = JSONObject.fromObject(line);
			
			if(!OPERATION_VALUE.equalsIgnoreCase(jobj.getString(OPERATION))){
				continue ;
			}
			num = resultMap.get(jobj.getInt(OPERATOR));
			if(num==null){
				resultMap.put(jobj.getInt(OPERATOR), 1);
			}else{
				resultMap.put(jobj.getInt(OPERATOR), ++num);
			}
		}
		br.close();
		
		for(Integer companyId:resultMap.keySet()){
			saveToDB(companyId, resultMap.get(companyId), targetDate);
		}
		
		return true;
	}
	
	private void saveToDB(Integer companyId, Integer loginCount, String targetDate){
		String sql="insert into analysis_login(company_id, gmt_target, login_count, gmt_created, gmt_modified) values("+companyId+",'"+targetDate+"', "+loginCount+", now(),now())";
		DBUtils.insertUpdate(DB, sql);
	}

	@Override
	public boolean init() throws Exception {
		return false;
	}
	
	public static void main(String[] args) throws ParseException, Exception {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		
		long start=System.currentTimeMillis();
		AnalysisLogin analysis=new AnalysisLogin();
		
		AnalysisLogin.OPERATION_VALUE="testzhcn";
		AnalysisLogin.LOG_FILE="/usr/data/log4z/log/run.";
		analysis.clear(DateUtil.getDate("2011-12-30", "yyyy-MM-dd"));
		analysis.exec(DateUtil.getDate("2011-12-30", "yyyy-MM-dd"));
		long end=System.currentTimeMillis();
		System.out.println("共耗时："+(end-start));
	}

}
