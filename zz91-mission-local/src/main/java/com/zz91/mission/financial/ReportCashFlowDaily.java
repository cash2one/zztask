package com.zz91.mission.financial;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.zz91.mission.domain.financial.ReportCashFlow;
import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;
/**
 * 
 * */
public class ReportCashFlowDaily implements ZZTask {

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

		Date periodTo= DateUtil.getDate(baseDate, DATE_FORMAT);
		Date periodFrom=DateUtil.getDateAfterDays(periodTo, -1);
		
//		Integer start=0;
//		Set<String> coaSet = new HashSet<String>();
//		do {
//			coaSet = queryCoa(start);
//
//			analysis(coaSet, periodFrom, periodTo);
//			
//			start=start+PAGE_SIZE;
//		} while (coaSet.size()>0);
		
		StringBuffer sb=new StringBuffer();
		sb.append("select code_coa,code_item_cash,code_item_dept,sum(cy_dr) as dr_sum, sum(cy_cr) as cr_sum ")
			.append(" from ac_document where ")
			.append(" gmt_build>='").append(DateUtil.toString(periodFrom,DATE_FORMAT)).append("' ")
			.append(" and gmt_build<'").append(DateUtil.toString(periodTo,DATE_FORMAT)).append("' ")
			.append(" and code_item_cash<>'' group by code_item_cash, code_item_dept, code_coa");
		
		final List<ReportCashFlow> list=new ArrayList<ReportCashFlow>();
		DBUtils.select(DB, sb.toString(), new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				
				while (rs.next()) {
					ReportCashFlow report=new ReportCashFlow();
					report.setCodeCoa(rs.getString(1));
					report.setCodeItemCash(rs.getString(2));
					report.setCodeItemDept(rs.getString(3));
					report.setCyDrSum(rs.getLong(4));
					report.setCyCrSum(rs.getLong(5));
					list.add(report);
				}
			}
		});
		
		for(ReportCashFlow report:list){
			saveAnalysis(report, periodFrom);
		}
		
		return true;
	}
	
	
	private void saveAnalysis(ReportCashFlow report, Date from){
		
		StringBuffer sb=new StringBuffer();
		sb.append(" insert into report_cash_flow(code_coa, code_item_cash, ")
				.append("code_item_dept, cy_dr_sum, cy_cr_sum, gmt_report, ")
				.append("gmt_created, gmt_modified) ")
				.append(" values('").append(report.getCodeCoa()).append("','")
				.append(report.getCodeItemCash()).append("','")
				.append(report.getCodeItemDept()).append("',")
				.append(report.getCyDrSum()).append(",")
				.append(report.getCyCrSum()).append(",'")
				.append(DateUtil.toString(from, DATE_FORMAT)).append("',now(),now() )");
		
		DBUtils.insertUpdate(DB, sb.toString());
		
	}
	
	@Override
	public boolean clear(Date baseDate) throws Exception {
		Date from=DateUtil.getDateAfterDays(baseDate, -1);
		DBUtils.insertUpdate(DB, "delete from report_cash_flow where gmt_report='"+DateUtil.toString(from, DATE_FORMAT)+"'");
		return true;
	}
	
	public static void main(String[] args) {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		
		ReportCashFlowDaily task=new ReportCashFlowDaily();
		
		try {
			
			task.clear(DateUtil.getDate("2013-04-30 00:00:00", "yyyy-MM-dd HH:mm:ss"));
			task.exec(DateUtil.getDate("2013-04-30 00:00:00", "yyyy-MM-dd HH:mm:ss"));
			
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
