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
public class AnalysisAdminPubPriceTask implements ZZTask{

	private final static String DB="ast";
	
	private static String LOG_FILE = "/usr/data/log4z/run.";
	private final static String LOG_DATE_FORMAT = "yyyy-MM-dd";
	
	private final static String OPERATION = "operation";
	private final static String OPERATION_VALUE = "post_price";
	private final static String OPERATOR="operator";
	
	
	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
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
			
			if(!OPERATION_VALUE.equalsIgnoreCase(jobj.getString(OPERATION))){
				continue;
			}
			String name = jobj.getString(OPERATOR);
			num = resultMap.get(name);
			if(num==null){
				resultMap.put(name, 1);
			}else{
				resultMap.put(name, ++num);
			}
		}
		br.close();
		
		for(String name:resultMap.keySet()){
			saveToDB(name, resultMap.get(name), targetDate);
		}
		
		return true;
	}
	
	private void saveToDB(String name, Integer num, String targetDate){
		String sql="update analysis_operate set post_price_text = "+num+" where operator='"+name+"' and gmt_created = '"+targetDate +"'";
		DBUtils.insertUpdate(DB, sql);
	}
	
	@Override
	public boolean init() throws Exception {
		return false;
	}
	
	public static void main(String[] args) throws ParseException, Exception {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		
		long start=System.currentTimeMillis();
		AnalysisAdminPubPriceTask analysis=new AnalysisAdminPubPriceTask();
		
		AnalysisAdminPubPriceTask.LOG_FILE="/usr/data/log4z/log/run.";
		analysis.exec(DateUtil.getDate("2013-04-13", "yyyy-MM-dd"));
		long end=System.currentTimeMillis();
		System.out.println("共耗时："+(end-start));
	}

}
