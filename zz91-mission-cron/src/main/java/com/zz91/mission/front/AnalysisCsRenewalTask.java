package com.zz91.mission.front;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zz91.mission.domain.CompanyService;
import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;

/**cs相关数据统计
 * 
 * 只有续签
 * 1.查找crm_cs表中cs_account,统计数据都以单个cs准。
 * 
 * 2.F(E=F-C-D)统计当月到期客户总数(到期时间：当月)
 * 3.C统计当月到期客户月前续签数(续签时间：月前，到期时间：当月)
 * 4.D统计当月到期客户当月续签数(续签时间：当月，到期时间：当月)
 * 
 * 5.A统计过期180天内当月续签客户数(续签时间：当月，到期时间：已过期，过期半年内)
 * 6.B统计过期180天外当月续签客户数(续签时间：当月，到期时间：已过期，过期半年前)
 * 7.H未到期客户当月续签数： 提前180天外续签(续签时间：当月，到期时间：未过期，还有半年以上过期)
 * 8.G未到期客户当月续签数 提前180天内(续签时间：当月，到期时间：未过期，还有半年不到过期)
 * 9.I当月续签总数(续签时间：当月)
 * 
 * 10.J统计有效客户数=A+C+D
 * 11.K续签率=J/F
 * 
 * 12.将统计结果写入analysis_cs_log表
 * 
 * 获取数据：cs账户、上次服务到期时间、续签时间
 * 固定条件：已开通的，续签的，再生通服务(1000)
 * 
 * @author root
 *
 */

public class AnalysisCsRenewalTask implements ZZTask {
	
	public static String DB="ast";
	
	@Override
	public boolean clear(Date baseDate) throws Exception {
		Date nowMonth = DateUtil.getDate(baseDate, "yyyy-MM-01");
		Date lastMonth=DateUtil.getDateAfterMonths(nowMonth, -1);
		
		return DBUtils.insertUpdate(DB, "delete from analysis_cs_renewal where gmt_target='"+DateUtil.toString(lastMonth, "yyyy-MM-dd")+"' ");
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		
		Date nowMonth = DateUtil.getDate(baseDate, "yyyy-MM-01");
		Date lastMonth=DateUtil.getDateAfterMonths(nowMonth, -1);
		
		Map<String, Map<String, Integer>> analysisResult=new HashMap<String, Map<String,Integer>>();
		
		analysisCD(lastMonth, analysisResult);
		analysisF(lastMonth, analysisResult);
		analysisABGHI(lastMonth, analysisResult);
		analysisEJK(lastMonth, analysisResult);
		writeResult(lastMonth, analysisResult);
		
		return true;
	}
	
	/**
	 * C统计当月到期客户月前续签数(续签时间：月前，到期时间：当月)
	 * D统计当月到期客户当月续签数(续签时间：当月，到期时间：当月)
	 * @param lastMonth
	 * @param resultMap
	 * @throws ParseException 
	 */
	private void analysisCD(Date lastMonth, Map<String, Map<String, Integer>> resultMap) throws ParseException{
		String sql = "select (select cs.cs_account from crm_cs cs where cs.company_id=ccs.company_id) as cs_account, " +
				"gmt_pre_end,gmt_signed from crm_company_service ccs where crm_service_code='1000' and '"+
				DateUtil.toString(lastMonth, "yyyy-MM-dd")+"' <= gmt_pre_end and gmt_pre_end < '"+
				DateUtil.toString(DateUtil.getDateAfterMonths(lastMonth, 1), "yyyy-MM-dd")+"' and apply_status='1' ";
		List<CompanyService> list=queryCompanyServiceData(sql);
		for(CompanyService obj:list){
			if(obj.getCsAccount()==null){
				continue;
			}
			Map<String, Integer> m = resultMap.get(obj.getCsAccount());
			if(m==null){
				m=new HashMap<String, Integer>();
				resultMap.put(obj.getCsAccount(), m);
			}
			if(DateUtil.getIntervalDays(obj.getGmtSigned(),lastMonth)<0){
				calculate(m, "c", 1);
			}else {
				calculate(m, "d", 1);
			}
		}
	}
	
	/**
	 * F(E=F-C-D)统计当月到期客户总数(到期时间：当月)
	 * @param lastMonth
	 * @param resultMap
	 */
	private void analysisF(Date lastMonth, Map<String, Map<String, Integer>> resultMap){
		String sql = "select (select cs.cs_account from crm_cs cs where cs.company_id=ccs.company_id) as cs_account, " +
		"gmt_pre_end, gmt_signed from crm_company_service ccs where crm_service_code='1000' and '"+
		DateUtil.toString(lastMonth, "yyyy-MM-dd")+"' <= gmt_end and gmt_end < '"+
		DateUtil.toString(DateUtil.getDateAfterMonths(lastMonth, 1), "yyyy-MM-dd")+"' and apply_status='1'";
		List<CompanyService> list=queryCompanyServiceData(sql);
		for(CompanyService obj:list){
			if(obj.getCsAccount()==null){
				continue;
			}
			
			Map<String, Integer> m = resultMap.get(obj.getCsAccount());
			if(m==null){
				m=new HashMap<String, Integer>();
				resultMap.put(obj.getCsAccount(), m);
			}
			calculate(m, "f", 1);
		}
	}
	
	/**
	 * A统计过期180天内当月续签客户数(续签时间：当月，到期时间：已过期，过期半年内)
	 * B统计过期180天外当月续签客户数(续签时间：当月，到期时间：已过期，过期半年前)
	 * H未到期客户当月续签数： 提前180天外续签(续签时间：当月，到期时间：未过期，还有半年以上过期)
	 * G未到期客户当月续签数 提前180天内(续签时间：当月，到期时间：未过期，还有半年不到过期)
	 * I当月续签总数(续签时间：当月)
	 * @param lastMonth
	 * @param resultMap
	 * @throws ParseException
	 */
	private void analysisABGHI(Date lastMonth, Map<String, Map<String, Integer>> resultMap) throws ParseException{
		String sql = "select (select cs.cs_account from crm_cs cs where cs.company_id=ccs.company_id) as cs_account, " +
				"gmt_pre_end, gmt_signed from crm_company_service ccs where crm_service_code='1000' and gmt_pre_end is not null and '"+ //gmt_pre_end!=0 and 
				DateUtil.toString(lastMonth, "yyyy-MM-dd")+"' <= gmt_signed and gmt_signed < '"+
				DateUtil.toString(DateUtil.getDateAfterMonths(lastMonth, 1), "yyyy-MM-dd")+"' and apply_status='1' ";
		List<CompanyService> list=queryCompanyServiceData(sql);
		for(CompanyService obj:list){
			if(obj.getCsAccount()==null){
				continue;
			}
			Map<String, Integer> m = resultMap.get(obj.getCsAccount());
			if(m==null){
				m=new HashMap<String, Integer>();
				resultMap.put(obj.getCsAccount(), m);
			}
			calculate(m, "i", 1);
			if(obj.getPreGmtEnd()==null){
				continue ;
			}
			int intervalAB=DateUtil.getIntervalDays( obj.getPreGmtEnd(), lastMonth);
			
			
			if(intervalAB<-365){
				calculate(m, "b", 1);
			}else if(intervalAB<0){
				calculate(m, "a", 1);
			}
			
			Date now=DateUtil.getDateAfterMonths(lastMonth, 1);
			int intervalGH=DateUtil.getIntervalDays( obj.getPreGmtEnd(), now);
			if(intervalGH>0 && intervalGH<180){
				calculate(m, "g", 1);
			}else if(intervalGH>180){
				calculate(m, "h", 1);
			}
		}
	}
	
	/**
	 * E=F-C-D
	 * J统计有效客户数=A+C+D
	 * K续签率=J/F
	 * @param lastMonth
	 * @param resultMap
	 */
	private void analysisEJK(Date lastMonth, Map<String, Map<String, Integer>> resultMap){
		for(String account:resultMap.keySet()){
			Map<String, Integer> result=resultMap.get(account);
			if(result==null){
				continue;
			}
			result.put("e", getInteger(result.get("f"),0)-getInteger(result.get("c"),0)-getInteger(result.get("d"),0));
			result.put("j", getInteger(result.get("a"), 0)+getInteger(result.get("c"), 0)+getInteger(result.get("d"), 0));
			if(getInteger(result.get("f"), 0)==0){
				result.put("k", 0);
			}else{
				result.put("k", (getInteger(result.get("j"), 0)*10000)/getInteger(result.get("f"), 0));
			}
		}
	}
	
	private void writeResult(Date lastMonth, Map<String, Map<String, Integer>> resultMap){
		for(String account:resultMap.keySet()){
			Map<String, Integer> result=resultMap.get(account);
			if(result==null){
				continue;
			}
			writeToDb(account, result, lastMonth);
		}
		
		String dept="zzzzzzzz";
		Map<String, Integer> m = new HashMap<String, Integer>();
		
		for(String account:resultMap.keySet()){
			Map<String, Integer> result=resultMap.get(account);
			if(result==null){
				continue;
			}
			System.out.println(account+" a:"+getInteger(result.get("a"), 0));
			calculate(m, "a", getInteger(result.get("a"), 0));
			calculate(m, "b", getInteger(result.get("b"), 0));
			calculate(m, "c", getInteger(result.get("c"), 0));
			calculate(m, "d", getInteger(result.get("d"), 0));
			calculate(m, "e", getInteger(result.get("e"), 0));
			calculate(m, "f", getInteger(result.get("f"), 0));
			calculate(m, "g", getInteger(result.get("g"), 0));
			calculate(m, "h", getInteger(result.get("h"), 0));
			calculate(m, "i", getInteger(result.get("i"), 0));
			calculate(m, "j", getInteger(result.get("j"), 0));
		}
		
		if(getInteger(m.get("f"), 0)==0){
			m.put("k", 0);
		}else{
			m.put("k", (getInteger(m.get("j"), 0)*10000)/getInteger(m.get("f"), 0));
		}
		
		writeToDb(dept, m, lastMonth);
		
	}
	
	private boolean writeToDb(String account, Map<String, Integer> result, Date targetDate){
		StringBuffer sb=new StringBuffer();
		sb.append("insert into analysis_cs_renewal (cs_account, gmt_target, a, b, c, d, e, f, g, h, i, j, k, gmt_created, gmt_modified)");
		sb.append(" values('").append(account).append("','").append(DateUtil.toString(targetDate, "yyyy-MM-dd")).append("',"+
				getInteger(result.get("a"), 0)+","+
				getInteger(result.get("b"), 0)+","+
				getInteger(result.get("c"), 0)+","+
				getInteger(result.get("d"), 0)+","+
				getInteger(result.get("e"), 0)+","+
				getInteger(result.get("f"), 0)+","+
				getInteger(result.get("g"), 0)+","+
				getInteger(result.get("h"), 0)+","+
				getInteger(result.get("i"), 0)+","+
				getInteger(result.get("j"), 0)+","+
				getInteger(result.get("k"), 0)/100f+",now(),now())");
		return DBUtils.insertUpdate(DB, sb.toString());
	}
	
	private List<CompanyService> queryCompanyServiceData(String sql){
		
		final List<CompanyService> list=new ArrayList<CompanyService>();
		
		DBUtils.select(DB, sql, new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					CompanyService cs=new CompanyService(rs.getString("cs_account"), rs.getDate("gmt_pre_end"), rs.getDate("gmt_signed"));
					list.add(cs);
				}
			}
		});
		return list;
	}
	
	private void calculate(Map<String, Integer> map, String k, Integer num){
		if(map.get(k)!=null){
			num = num.intValue()+map.get(k).intValue();
		}
		map.put(k, num);
	}
	
	private int getInteger(Integer v, int defaultValue){
		if(v==null){
			return defaultValue;
		}
		return v.intValue();
	}
	
	@Override
	public boolean init() throws Exception {
		
		return false;
	}
	
	public static void main(String[] args) throws Exception {
		
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		
		Date baseDate=DateUtil.getDate("2012-01-01", "yyyy-MM-dd");

		AnalysisCsRenewalTask.DB="ast";
		AnalysisCsRenewalTask task=new AnalysisCsRenewalTask();
		task.clear(baseDate);
		task.exec(baseDate);
		
		AnalysisCsRenewalTask.DB="ast_test";
		AnalysisCsRenewalTask task_test=new AnalysisCsRenewalTask();
		task_test.clear(baseDate);
		task_test.exec(baseDate);
		
//		System.out.println(DateUtil.getIntervalDays(DateUtil.getDate("2011-12-01", "yyyy-MM-dd"), DateUtil.getDate("2011-12-21", "yyyy-MM-dd")));
//		System.out.println(DateUtil.getIntervalDays(DateUtil.getDate("2011-12-01", "yyyy-MM-dd"), DateUtil.getDate("2011-11-21", "yyyy-MM-dd")));
		
//		Date lastmonth=DateUtil.getDateAfterMonths(DateUtil.getDate("2012-01-01", "yyyy-MM-dd"), -1);
//		Date thisMonth=DateUtil.getDateAfterMonths(lastmonth, +1);
//		
//		System.out.println(DateUtil.toString(DateUtil.getDateAfterDays(lastmonth, -365),"yyyy-MM-dd"));
//		System.out.println(DateUtil.toString(DateUtil.getDateAfterDays(thisMonth, 180),"yyyy-MM-dd"));
		
	}
}
