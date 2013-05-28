package com.zz91.mission.front;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.log.LogUtil;

/**
 * CS客户自动掉公海处理任务
 * 
 * @author kongsj
 * 
 */
public class AutoOutCRM implements ZZTask {

	private static final String DB = "ast";
	private static final Integer SIZE = 50;
	private static final String ONE_MONTH_NOVISIT = "一个月未联系，自动掉公海";
	private static final String THREE_MONTH_ISEXPIRED = "过期三个月，自动掉公海";
//	final static List<Map<String, Object>> OUT_LIST = new ArrayList<Map<String, Object>>();
	

	@Override
	public boolean init() throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		// 一个月未联系客户自动掉公海
		Date omDate = DateUtil.getDateAfterDays(baseDate, -31);
		oneMonthOut(omDate);
		

		// 过期三个月自动掉公海
		Date tmDate = DateUtil.getDateAfterDays(baseDate, -91);
		threeMonthOut(tmDate);
		
//		if(OUT_LIST.size()>0){
//			Map<String, Object> dataMap=new HashMap<String, Object>();
//			dataMap.put("logList", OUT_LIST);
//			String date =  DateUtil.toString(new Date(), "yyyy年MM月dd日");
//			dataMap.put("date",date);
//			MailUtil.getInstance().sendMail("[CRM]"+date+"掉公海的公司以及客服信息", "zz91.crm.auto.out@asto.mail", null,null, "zz91", "zz91-crm-auto-out",	dataMap, MailUtil.PRIORITY_TASK);
//		}
		return true;
	}

	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	private Integer getTotal(Date baseDate, String sql) {
		final Integer[] total = { 0 };
		DBUtils.select(DB, sql, new IReadDataHandler() {

			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					total[0] = rs.getInt(1);
				}
			}
		});
		return total[0];
	}

	private Integer getEnd(Integer total) {
		Integer end = 0;
		if (total % SIZE == 0) {
			end = total / SIZE;
		} else {
			end = total / SIZE + 1;
		}
		return end;
	}

	private void outPub(List<Integer> list, final Integer i) {
		final List<Map<String, Object>> logList = new ArrayList<Map<String, Object>>();
		for (final Integer companyId : list) {
			// 检索CRM库，详细信息
			String sql = "SELECT cs_account,gmt_visit,gmt_next_visit_phone,gmt_next_visit_email FROM crm_cs where company_id ="
					+ companyId;
			DBUtils.select(DB, sql, new IReadDataHandler() {

				@Override
				public void handleRead(ResultSet rs) throws SQLException {
					while (rs.next()) {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("companyId", companyId);
						map.put("account", rs.getString(1));
						map.put("gmtVisit", rs.getString(2));
						map.put("gmtNextVisitPhone", rs.getString(3));
						map.put("gmtNextVisitEmail", rs.getString(4));
						if (i == 1) {
							map.put("summary", ONE_MONTH_NOVISIT);
						} else if (i == 3) {
							map.put("summary", THREE_MONTH_ISEXPIRED);
						}
						logList.add(map);
					}
				}
			});
			for (Map<String, Object> logMap : logList) {
				JSONObject js = JSONObject.fromObject(logMap);
				LogUtil.getInstance().mongo("system", "auto_out_pub","127.0.0.1", js.toString());
//				OUT_LIST.add(js);
				// 记录掉公海
				logOut(js.getString("companyId"));
			}

			// 该客户符合条件
			DBUtils.insertUpdate(DB, "delete FROM crm_cs where company_id=" + companyId);
		}
	}
	
	private void oneMonthOut(Date date) {
		Integer total = getTotal(
				date,
				"select count(0) from crm_cs_profile c left join crm_cs cs on c.company_id=cs.company_id inner join company co on c.company_id =co.id and co.membership_code='10051000' where '"
						+ DateUtil.toString(date, "yyyy-MM-dd")
						+ "' >= cs.gmt_visit ");
		Integer end = getEnd(total);
		for (int i = 1; i <= end; i++) {
			// 查询一个月未联系客户
			final List<Integer> list = new ArrayList<Integer>();
			String sql = "select c.company_id from crm_cs_profile c left join crm_cs cs on c.company_id=cs.company_id inner join company co on c.company_id =co.id and co.membership_code='10051000' where '"
					+ DateUtil.toString(date, "yyyy-MM-dd")
					+ "' >= cs.gmt_visit limit " + (i - 1) * SIZE + "," + SIZE;
			DBUtils.select(DB, sql, new IReadDataHandler() {
				@Override
				public void handleRead(ResultSet rs) throws SQLException {
					while (rs.next()) {
						list.add(rs.getInt(1));
					}
				}
			});
			// 遍历 companyId搜索cs账户信息，记录日志，并执行该客户掉公害
			outPub(list, 1);
		}
	}

	private void threeMonthOut(Date date) {
		String sql = "select count(0) from crm_company_service cs inner join company c on cs.company_id = c.id and c.membership_code = '10051000' where cs.crm_service_code='1000' and cs.gmt_end='"
				+ DateUtil.toString(date, "yyyy-MM-dd")+"'";
		Integer total = getTotal(date, sql);
		Integer end = getEnd(total);
		for (int i = 1; i <= end; i++) {
			// 查询过期三个月客户
			final List<Integer> list = new ArrayList<Integer>();
			sql = "select cs.company_id from crm_company_service cs inner join company c on cs.company_id = c.id and c.membership_code = '10051000' where cs.crm_service_code='1000' and cs.gmt_end='"
					+ DateUtil.toString(date, "yyyy-MM-dd")
					+ "' and apply_status=1 limit "
					+ (i - 1) * SIZE + "," + SIZE;
			DBUtils.select(DB, sql, new IReadDataHandler() {
				@Override
				public void handleRead(ResultSet rs) throws SQLException {
					while (rs.next()) {
						list.add(rs.getInt(1));
					}
				}
			});
			// 遍历 companyId搜索cs账户信息，记录日志，并执行该客户掉公害
			outPub(list, 3);
		}
	}
	/**
	 * 记录掉公海日志
	 */
	private void logOut(String companyId){
		String sql = "INSERT INTO crm_out_log"
		+"(company_id,operator,status,gmt_created,gmt_modified)"
		+"VALUES ("+companyId+",0,0,now(),now())";
		DBUtils.insertUpdate(DB, sql);
	}

	public static void main(String[] args) throws ParseException, Exception {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		LogUtil.getInstance().init("web.properties");

		AutoOutCRM task = new AutoOutCRM();
		task.exec(DateUtil.getDate("2012-01-02", "yyyy-MM-dd"));
	}
}
