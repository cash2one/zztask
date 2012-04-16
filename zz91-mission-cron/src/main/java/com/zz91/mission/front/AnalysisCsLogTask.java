package com.zz91.mission.front;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zz91.mission.domain.subscribe.AnalysisCsLog;
import com.zz91.mission.domain.subscribe.CsLog;
import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;

public class AnalysisCsLogTask implements ZZTask{

	final static String DATE_FORMAT = "yyyy-MM-dd";
	@Override
	public boolean init() throws Exception {
		return false;
	}

	final static Integer ANALYSIS_PART=2000;
	
	@Override
	public boolean exec(Date baseDate) throws Exception {
		long targetDate=DateUtil.getTheDayZero(baseDate, -1);
		Date startDate = new Date(targetDate * 1000);
		
		Map<String, AnalysisCsLog> map=new HashMap<String, AnalysisCsLog>();
		StringBuffer sql=new StringBuffer();
		sql.append("select count(*) from crm_cs_log");
		sql.append(" where gmt_created>='").append(DateUtil.toString(startDate, DATE_FORMAT)).append(" 00:00:00'");
		sql.append(" and gmt_created<'").append(DateUtil.toString(startDate, DATE_FORMAT)).append(" 23:59:59'");
		
		final List<Integer> countList=new ArrayList<Integer>();
		DBUtils.select("ast", sql.toString(), new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					countList.add(rs.getInt(1));
				}
			}
		});
		
		
		if(countList.get(0)!=null && countList.get(0)==0){
			throw new Exception("没有可分析的数据，target date:"+DateUtil.toString(startDate, DATE_FORMAT));
		}
		
		for(int i=0;i<countList.get(0).intValue();i=i+ANALYSIS_PART){
			analysis(map, startDate, i, ANALYSIS_PART);
		}
		
		for(String account:map.keySet()) {
			String sqlInsert="insert into `analysis_cs_log` (`cs_account`,`star0_y`,`star0_n`,`star1_y`,`star1_n`,`star2_y`,`star2_n`,`star3_y`," +
					"`star3_n`,`star4_y`,`star4_n`,`star5_y`,`star5_n`,`sale_call`,`service_call`,`analysis_date`,`gmt_created`,`gmt_modified`)" +
					"values('"
					+account+"',"
					+map.get(account).getStar0Y()+","
					+map.get(account).getStar0N()+","
					+map.get(account).getStar1Y()+","
					+map.get(account).getStar1N()+","
					+map.get(account).getStar2Y()+","
					+map.get(account).getStar2N()+","
					+map.get(account).getStar3Y()+","
					+map.get(account).getStar3N()+","
					+map.get(account).getStar4Y()+","
					+map.get(account).getStar4N()+","
					+map.get(account).getStar5Y()+","
					+map.get(account).getStar5N()+","
					+map.get(account).getSaleCall()+","
					+map.get(account).getServiceCall()+","
					+targetDate+",now(),now())";
			
			DBUtils.insertUpdate("ast", sqlInsert);
		}
		
		return true;
	}
	
	private void analysis(Map<String, AnalysisCsLog> map, Date startDate, int start, int limit){
		StringBuilder sql=new StringBuilder();
		sql.append("select cs_account,call_type,situation,star from crm_cs_log");
		sql.append(" where gmt_created>='").append(DateUtil.toString(startDate, DATE_FORMAT)).append(" 00:00:00'");
		sql.append(" and gmt_created<'").append(DateUtil.toString(startDate, DATE_FORMAT)).append(" 23:59:59'");
		sql.append(" order by id desc limit ").append(start).append(",").append(limit);
		final List<CsLog> list = new ArrayList<CsLog>();

		DBUtils.select("ast",sql.toString(), new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					CsLog cl=new CsLog();
					cl.setCsAccount(rs.getString(1));
					cl.setCallType(rs.getInt(2));
					cl.setSituation(rs.getInt(3));
					cl.setStar(rs.getInt(4));
					list.add(cl);
				}
			}
		});
		
		for (CsLog log : list) {
			AnalysisCsLog analysis=map.get(log.getCsAccount());
			if(analysis==null){
				analysis=new AnalysisCsLog(log.getCsAccount(), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, null, null);
			}
			//0
			if((log.getStar()==0 || log.getStar()==-1)&&log.getSituation()==0){
				analysis.setStar0N(analysis.getStar0N()+1);
			}
			if((log.getStar()==0 || log.getStar()==-1)&&log.getSituation()==1){
				analysis.setStar0Y(analysis.getStar0Y()+1);
			}
			//1
			if(log.getStar()==1&&log.getSituation()==0){
				analysis.setStar1N(analysis.getStar1N()+1);
			}
			if(log.getStar()==1&&log.getSituation()==1){
				analysis.setStar1Y(analysis.getStar1Y()+1);
			}
			//2
			if(log.getStar()==2&&log.getSituation()==0){
				analysis.setStar2N(analysis.getStar2N()+1);
			}
			if(log.getStar()==2&&log.getSituation()==1){
				analysis.setStar2Y(analysis.getStar2Y()+1);
			}
			//3
			if(log.getStar()==3&&log.getSituation()==0)
			{
				analysis.setStar3N(analysis.getStar3N()+1);
			}
			if(log.getStar()==3&&log.getSituation()==1){
				analysis.setStar3Y(analysis.getStar3Y()+1);
			}
			//4
			if(log.getStar()==4&&log.getSituation()==0){
				analysis.setStar4N(analysis.getStar4N()+1);
			}
			if(log.getStar()==4&&log.getSituation()==1){
				analysis.setStar4Y(analysis.getStar4Y()+1);
			}
			//5
			if(log.getStar()==5&&log.getSituation()==0){
				analysis.setStar5N(analysis.getStar5N()+1);
			}
			if(log.getStar()==5&&log.getSituation()==1){
				analysis.setStar5Y(analysis.getStar5Y()+1);
			}
			
			if(log.getCallType()==0){
				analysis.setServiceCall(analysis.getServiceCall()+1);
			}
			if(log.getCallType()==1){
				analysis.setSaleCall(analysis.getSaleCall()+1);
			}
			map.put(log.getCsAccount(), analysis);
		}
	}

	@Override
	public boolean clear(Date baseDate) throws Exception {
		long targetDate=DateUtil.getTheDayZero(baseDate, -1);
		DBUtils.insertUpdate("ast", "delete from analysis_cs_log where analysis_date="+targetDate);
		return false;
	}
	
	public static void main(String[] args) {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		AnalysisCsLogTask csLog=new AnalysisCsLogTask();
		try {
			csLog.clear(DateUtil.getDate("2011-09-20", "yyyy-MM-dd"));
			csLog.exec(DateUtil.getDate("2011-09-20", "yyyy-MM-dd"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
