/**
 * 
 */
package com.zz91.mission.ep;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import org.apache.log4j.Logger;
import com.zz91.task.common.ZZTask;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;

/**
 * @author root 统计今天各种客户数分布
 */
public class CrmTodayCompanyDataTask implements ZZTask {

	Logger LOG = Logger.getLogger(CrmTodayCompanyDataTask.class);

	final static String DB = "crm";

	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		boolean result = false;
		do {
			String sql = "SELECT count(*) as count,ctype FROM crm_company group by ctype";
			DBUtils.select(DB, sql, new IReadDataHandler() {
				@Override
				public void handleRead(ResultSet rs) throws SQLException {
					int totals = 0;
					int seeCount = 0;
					int noActiveCount = 0;
					int selfCount = 0;
					int wasteCount = 0;
					while (rs.next()) {
						totals = totals + rs.getInt("count");
						if (rs.getInt("ctype") == 1) {
							selfCount = rs.getInt("count");
						} else if (rs.getInt("ctype") == 2) {
							seeCount = rs.getInt("count");
						} else if (rs.getInt("ctype") == 4) {
							wasteCount = rs.getInt("count");
						} else if (rs.getInt("ctype") == 5) {
							noActiveCount = rs.getInt("count");
						}
					}
					insertCrmStatistics(totals,seeCount,noActiveCount,selfCount,wasteCount);
				}
			});
			result = true;
		} while (false);
		return result;
	}

	protected void insertCrmStatistics(int totals, int seeCount,
			int noActiveCount, int selfCount, int wasteCount) {
		int reaptCount = getReaptCount();
		int todayAssignCount = getTodayAssignCount();
		int todayChooseCount = getTodayChooseCount();
		int todayPutCount = getTodayPutCount();
		String sql="INSERT INTO crm_statistics(`totals`,`sea_count`,`no_active_count`," +
				"`self_count`,`waste_count`,`repeat_count`,`today_assign_count`,`today_choose_count`," +
				"`today_put_count`,`gmt_target`,`gmt_created`,`gmt_modified`) VALUES (" +
				+ totals +","+ seeCount +","+ noActiveCount +","+ selfCount +","
				+ wasteCount +","+ reaptCount +","+ todayAssignCount +","+ todayChooseCount +","
				+ todayPutCount +",date_format(date_add(now(), interval -1 day),'%Y-%m-%d'),now(),now())";
		boolean result=DBUtils.insertUpdate(DB, sql);
		if (!result) {
			LOG.info(">>>>>>>>>>>>>>>>>统计销售联系客户数失败:"+sql);
		}
	}

	//查询今天放入公海客户数
	private int getTodayPutCount() {
		String sql = "SELECT count(*) as count FROM crm_sale_comp where status=0 and disable_status=0 and date_format(gmt_modified,'%Y-%m-%d') = date_format(date_add(now(), interval -1 day),'%Y-%m-%d')";
			final Integer[] count=new Integer[1];
			DBUtils.select(DB, sql, new IReadDataHandler() {
				@Override
				public void handleRead(ResultSet rs) throws SQLException {
					while(rs.next()) {
						count[0]=rs.getInt("count");
					}
				}
			});
		return count[0];
	}
	
	//查询今天挑公海客户数
	private int getTodayChooseCount() {
		String sql = "SELECT count(*) as count FROM crm_sale_comp where date_format(gmt_created,'%Y-%m-%d') = date_format(date_add(now(), interval -1 day),'%Y-%m-%d')";
			final Integer[] count=new Integer[1];
			DBUtils.select(DB, sql, new IReadDataHandler() {
				@Override
				public void handleRead(ResultSet rs) throws SQLException {
					while(rs.next()) {
						count[0]=rs.getInt("count");
					}
				}
			});
		return count[0];
	}
	
	//查询今天新分配客户数
	private int getTodayAssignCount() {
		String sql = "SELECT count(*) as count FROM crm_sale_comp csc,crm_company cc where date_format(csc.gmt_created,'%Y-%m-%d') = date_format(date_add(now(), interval -1 day),'%Y-%m-%d') and cc.block_count = 0 and csc.status = 1 and csc.cid = cc.id";
			final Integer[] count=new Integer[1];
			DBUtils.select(DB, sql, new IReadDataHandler() {
				@Override
				public void handleRead(ResultSet rs) throws SQLException {
					while(rs.next()) {
						count[0]=rs.getInt("count");
					}
				}
			});
		return count[0];
	}
	//查询重复客户数
	private int getReaptCount() {
		String sql = "SELECT count(*) as count FROM crm_company where repeat_id != 0";
			final Integer[] count=new Integer[1];
			DBUtils.select(DB, sql, new IReadDataHandler() {
				@Override
				public void handleRead(ResultSet rs) throws SQLException {
					while(rs.next()) {
						count[0]=rs.getInt("count");
					}
				}
			});
		return count[0];
	}

	@Override
	public boolean init() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
}
