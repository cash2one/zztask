/**
 * 
 */
package com.zz91.mission.analysis;

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
public class AnalysisKeywordTask implements ZZTask{

	private final static String DB="ast";
	
	private static String LOG_FILE = "/usr/data/log4z/zz91/run.";
	private final static String LOG_DATE_FORMAT = "yyyy-MM-dd";
	
	private final static String OPERATION = "operation";
	private final static String OPERATION_VALUE = "search";
	private final static String OPERATOR="operator";
	private final static String OPERATOR_VALUE="zz91_trade";
	private final static String DATA="data";
	
	
	@Override
	public boolean clear(Date baseDate) throws Exception {
		String targetDate=DateUtil.toString(DateUtil.getDateAfterDays(baseDate, -1), LOG_DATE_FORMAT);
		return DBUtils.insertUpdate(DB, "delete from analysis_trade_keywords where gmt_target='"+targetDate+"'");
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		String targetDate=DateUtil.toString(DateUtil.getDateAfterDays(baseDate, -1), LOG_DATE_FORMAT);
		
		Map<String, Integer> resultMap=new HashMap<String, Integer>();
		
		BufferedReader br = new BufferedReader(new FileReader(LOG_FILE+targetDate));
		
		String line;
		Integer num=null;
		while ((line = br.readLine()) != null) {
			JSONObject jobj = JSONObject.fromObject(line);
			
			if(!OPERATOR_VALUE.equalsIgnoreCase(jobj.getString(OPERATOR))){
				continue;
			}
			if(!OPERATION_VALUE.equalsIgnoreCase(jobj.getString(OPERATION))){
				continue;
			}
			String keyword = jobj.getString(DATA);
			if(keyword!=null){
				keyword = keyword.toLowerCase();
			}
			num = resultMap.get(keyword);
			if(num==null){
				resultMap.put(keyword, 1);
			}else{
				resultMap.put(keyword, ++num);
			}
		}
		br.close();
		
		for(String keyword:resultMap.keySet()){
			saveToDB(keyword, resultMap.get(keyword), targetDate);
		}
		
		return true;
	}
	
	private void saveToDB(String keyword, Integer num, String targetDate){
		String sql="insert into analysis_trade_keywords (kw, num, gmt_target, gmt_created, gmt_modified) values('"
			+keyword+"',"+num+",'"+targetDate+"',  now(),now())";
		DBUtils.insertUpdate(DB, sql);
	}
	
	@Override
	public boolean init() throws Exception {
		return false;
	}
	
	public static void main(String[] args) throws ParseException, Exception {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		
		long start=System.currentTimeMillis();
		AnalysisKeywordTask analysis=new AnalysisKeywordTask();
		
		AnalysisKeywordTask.LOG_FILE="/usr/data/log4z/log/run.";
		analysis.clear(DateUtil.getDate("2011-12-30", "yyyy-MM-dd"));
		analysis.exec(DateUtil.getDate("2011-12-30", "yyyy-MM-dd"));
		long end=System.currentTimeMillis();
		System.out.println("共耗时："+(end-start));
	}

}
