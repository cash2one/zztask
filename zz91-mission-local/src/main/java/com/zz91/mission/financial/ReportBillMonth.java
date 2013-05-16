package com.zz91.mission.financial;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;
/**
 * 获取科目A
 * 得到该科目上一期期末(上个月)
 * 获取本期该科目凭证
 * 统计借贷和期末
 * 完成科目A的统计工作
 * 循环完成所有科目统计
 * */
public class ReportBillMonth implements ZZTask {

	final static String DB = "financial";

	final static String DATE_FORMAT = "yyyy-MM-dd 00:00:00";
	
	final static String COA_PREFIX="";
	
	final static int PAGE_SIZE=50;

	@Override
	public boolean init() throws Exception {
		
		return false;
	}
	
	@Override
	public boolean exec(Date baseDate) throws Exception {
		//获取科目A
		//得到该科目上一期期末
		//获取本期该科目凭证
		//统计借贷和期末
		//完成科目A的统计工作
		//循环完成所有科目统计
		
		Date periodFrom=lastMonthFirstDay(DateUtil.getDateAfterMonths(baseDate, -1));
		Date periodTo=DateUtil.getDateAfterDays(DateUtil.getDateAfterMonths(periodFrom, 1),-1);
		
		Integer start=0;
		Set<String> coaSet = new HashSet<String>();
		do {
			coaSet = queryCoa(start);

			analysis(coaSet, periodFrom, periodTo);
			
			start=start+PAGE_SIZE;
		} while (coaSet.size()>0);
		
		return true;
	}
	
	private Set<String> queryCoa(Integer start){
		final Set<String> coaset=new HashSet<String>();
		
		String sql="select code_coa from config_coa where isleaf=1 limit "+start+","+PAGE_SIZE;
		DBUtils.select(DB, sql, new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					coaset.add(rs.getString(1));
				}
			}
		});
		
		return coaset;
	}

	private void analysis(Set<String> coaSet, Date from, Date to){
		
		for(String coa: coaSet){
			if(coa.length()!=7){
				continue ;
			}
			Integer beginingBalance=queryEndBalanaceFromLastPeroid(coa, from);
			if(beginingBalance==null){
				beginingBalance=0;
			}
			//key: dr,cr
			Map<String, Integer> sumMap=querySumCy(coa, from, to);
			if(sumMap==null || sumMap.size()<2){
				continue ;
			}
			if(sumMap.get("cr")==null){
				sumMap.put("cr", 0);
			}
			if(sumMap.get("dr")==null){
				sumMap.put("dr", 0);
			}
			Integer endBalance=beginingBalance+sumMap.get("dr")-sumMap.get("cr");
			saveAnalysis(coa, beginingBalance, endBalance, sumMap.get("dr"), sumMap.get("cr"), from);
		}
	}
	
	private void saveAnalysis(String coa, Integer bb, Integer eb, Integer sumdr, Integer sumcr, Date from){
		String dept=coa.substring(3, 5);
		StringBuffer sb=new StringBuffer();
		sb.append(" insert into report_bill(code_coa, code_item_dept, ")
				.append("cy_begining_balance, cy_end_balance, cy_dr_sum, ")
				.append("cy_cr_sum, report_category, gmt_report, ")
				.append("gmt_created, gmt_modified) ")
				.append(" values('").append(coa).append("','")
				.append(dept).append("',")
				.append(bb).append(",")
				.append(eb).append(",")
				.append(sumdr).append(",")
				.append(sumcr).append(",2,'")
				.append(DateUtil.toString(from, DATE_FORMAT)).append("',now(),now() )");
		
		DBUtils.insertUpdate(DB, sb.toString());
		
	}
	
	private Map<String, Integer> querySumCy(String coa, Date from, Date to){
		StringBuffer sb=new StringBuffer();
		sb.append("select sum(cy_dr_sum) as dr, sum(cy_cr_sum) as cr from report_bill where ")
			.append(" gmt_report >='")
			.append(DateUtil.toString(from, DATE_FORMAT))
			.append("' and gmt_report <='")
			.append(DateUtil.toString(to, DATE_FORMAT))
			.append("' and code_coa='").append(coa).append("' and report_category=0 ");
		
		final Map<String, Integer> map=new HashMap<String, Integer>();
		DBUtils.select(DB, sb.toString(), new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					map.put("dr", rs.getInt(1));
					map.put("cr", rs.getInt(2));
				}
			}
		});
		
		return map;
	}
	
	private Integer queryEndBalanaceFromLastPeroid(String coa, Date peroidFrom){
		
		final Integer[] result={};
		
		String sql="select cy_begining_balance from report_bill where gmt_report='"
				+DateUtil.toString(peroidFrom, DATE_FORMAT)
				+"' and code_coa='"
				+coa
				+"' and report_category=0 limit 1";
		
		DBUtils.select(DB, sql, new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					result[0]=rs.getInt(1);
				}
			}
			
		});
		
		if(result.length>0){
			return result[0];
		}else{
			return 0;
		}
	}
	
	@Override
	public boolean clear(Date baseDate) throws Exception {
		Date from=lastMonthFirstDay(DateUtil.getDateAfterMonths(baseDate, -1));
		DBUtils.insertUpdate(DB, "delete from report_bill where gmt_report='"+DateUtil.toString(from, DATE_FORMAT)+"' and report_catetory=2");
		return true;
	}
	
	public static Date lastMonthFirstDay(Date targetDate){
		
		Date date=targetDate;
		
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		
		int curYear = cal.get(Calendar.YEAR);
		int curMonth = cal.get(Calendar.MONTH);
		Calendar stdCal = new GregorianCalendar(curYear, curMonth, 1);
		return stdCal.getTime();
	}
	 
	public static void main(String[] args) throws ParseException {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		
		ReportBillMonth task=new ReportBillMonth();
		
		try {
			task.clear(DateUtil.getDate("2013-05-26 00:00:00", "yyyy-MM-dd HH:mm:ss"));
			task.exec(DateUtil.getDate("2013-05-26 00:00:00", "yyyy-MM-dd HH:mm:ss"));
			
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		try {
//			Date date=DateUtil.getDate("2012-12-31 00:00:00", "yyyy-MM-dd HH:mm:ss");
//			Calendar cal=Calendar.getInstance();
//			cal.setTime(date);
////			cal.setFirstDayOfWeek(Calendar.MONDAY);
////			cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
//			
//			int day_of_week = cal.get(Calendar.DAY_OF_WEEK);
//			if(day_of_week==1){
//				cal.add(Calendar.DATE, -6);
//			}else{
//				cal.add(Calendar.DATE, 2-day_of_week);
//			}
//			
//			Date target = cal.getTime();
//			System.out.println(cal.get(Calendar.DAY_OF_WEEK)+"    "+DateUtil.toString(target, DATE_FORMAT));
//			
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
		
		

//		Date baseDate=DateUtil.getDate("2013-04-06 00:00:00", "yyyy-MM-dd HH:mm:ss");
//		Date periodFrom=  lastMonthFirstDay(DateUtil.getDateAfterMonths(baseDate, -1));
//		Date periodTo=DateUtil.getDateAfterDays(DateUtil.getDateAfterMonths(periodFrom, 1),-1);
//		
//		System.out.println(DateUtil.toString(periodFrom, DATE_FORMAT));
//		System.out.println(DateUtil.toString(periodTo, DATE_FORMAT));
		
	}

}
