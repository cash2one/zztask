/**
 * 
 */
package com.zz91.mission.front;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.lang.StringUtils;

/**
 * @author root
 *
 */
public class AnalysisEsiteVisit implements ZZTask{

	public final static String DB="ast";
	
	static String LOG_FILE = "/usr/data/log4z/zz91/run.";
	final static String LOG_DATE_FORMAT = "yyyy-MM-dd";
	
	final static String OPERATION="operation";
	final static String OPERATOR="operator";
	static String OPERATION_VALUE="esite_visit";
	
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
		
		countLog(targetDate);
		
		return true;
	}
	
	private void saveToDB(Integer companyId, Integer loginCount, String targetDate){
		String sql="insert into analysis_log(operator, operation, log_total, gmt_target, gmt_created, gmt_modified) values('"+companyId+"','"+OPERATION_VALUE+"',"+loginCount+", '"+targetDate+"',  now(),now())";
		DBUtils.insertUpdate(DB, sql);
	}
	
	private void countLog(String targetDate){
		String sql="select distinct operator,log_total from analysis_log where operation='"+OPERATION_VALUE+"' and gmt_target='"+targetDate+"'";
		
		final Map<String, Integer> result=new HashMap<String, Integer>();
		
		DBUtils.select(DB, sql, new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					result.put(rs.getString(1), rs.getInt(2));
				}
			}
		});
		
		for(String k:result.keySet()){
			if(!StringUtils.isNumber(k)){
				continue ;
			}
			addtoesiteCount(Integer.valueOf(k), result.get(k));
		}
	}
	
	private void addtoesiteCount(Integer cid, Integer count){
		final Integer[] isExisit=new Integer[1];
		DBUtils.select(DB, "select id from analysis_esite_visit where company_id="+cid, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					isExisit[0]=1;
				}
			}
		});
		
		if(isExisit[0]!=null && isExisit[0]==1){
			DBUtils.insertUpdate(DB, "update analysis_esite_visit set visit_count=visit_count+"+count+", real_visit_count=real_visit_count+"+count+",gmt_modified=now() where company_id="+cid);
		}else{
			DBUtils.insertUpdate(DB, "insert into analysis_esite_visit (company_id,visit_count,real_visit_count,gmt_created,gmt_modified) values("+cid+","+(count+(int)(Math.random()*50000))+","+count+",now(),now())");
		}
	}

	@Override
	public boolean init() throws Exception {
		return false;
	}
	
	public static void main(String[] args) throws ParseException, Exception {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		
		long start=System.currentTimeMillis();
		AnalysisEsiteVisit analysis=new AnalysisEsiteVisit();
		
		AnalysisEsiteVisit.OPERATION_VALUE="testzhcn";
		AnalysisEsiteVisit.LOG_FILE="/usr/data/log4z/log/run.";
		analysis.clear(DateUtil.getDate("2011-12-30", "yyyy-MM-dd"));
		analysis.exec(DateUtil.getDate("2011-12-30", "yyyy-MM-dd"));
		long end=System.currentTimeMillis();
		System.out.println("共耗时："+(end-start));
	}

}
