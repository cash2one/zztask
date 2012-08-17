/**
 * @author qizj
 * @email  qizhenj@gmail.com
 * @create_time  2012-7-9 下午02:34:45
 */
package com.zz91.mission.ep;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import org.apache.log4j.Logger;
import com.zz91.task.common.ZZTask;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.lang.StringUtils;

/**
 * @author qizj
 * 数据统计(crm转四星,转五星统计)
 */

public class CrmFourOrFiveTask implements ZZTask {
	
	Logger LOG=Logger.getLogger(CrmFourOrFiveTask.class);

	final static String DB="crm";
	
	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		boolean result=false;
		do {
			tongjiStar();
			result=true;
		}while(false);
		return result;
	}

	/**
	 * 统计昨天转四星,转五星的
	 */
	private void tongjiStar() {
		//查找昨天有联系客户的员工
		String sql = "select sale_account from crm_log"
		+ " where date_format(gmt_created,'%Y-%m-%d') = date_format(date_add(now(), interval -1 day),'%Y-%m-%d') group by sale_account";
		DBUtils.select(DB, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()) {
					tongji(rs.getString("sale_account"));
				}
			}
		});
	}
	
	private void tongji(String account) {
		String sql = "select * from (select id,cid,star_old,star,situation,sale_account,sale_name,sale_dept from crm_log "
					+" where date_format(gmt_created,'%Y-%m-%d') = date_format(date_add(now(), interval -1 day),'%Y-%m-%d')"
					+" and sale_account = '"+account+"' "
					+" order by gmt_created desc) tmp group by cid";
		DBUtils.select(DB, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				String account = "";
				String saleName = "";
				String saleDept = "";
				int star4 = 0;
				int star5 = 0;
				while(rs.next()) {
					if (StringUtils.isEmpty(account)) {
						account = rs.getString("sale_account");
					}
					if (StringUtils.isEmpty(saleName)) {
						saleName = rs.getString("sale_name");
					}
					if (StringUtils.isEmpty(saleDept)) {
						saleDept = rs.getString("sale_dept");
					}
					if(!"4".equals(rs.getString("star_old")) && !"5".equals(rs.getString("star_old")) && "4".equals(rs.getString("star"))) {
						star4++;
					}
					if(!"5".equals(rs.getString("star_old")) && "5".equals(rs.getString("star"))) {
						star5++;
					}
				}
				insertTongji(account, saleName, saleDept,star4,star5);
			}
		});
	}
	
	private void insertTongji(String saleAccount,String saleName,String saleDept,Integer star4,Integer star5){
			String sql="insert into crm_turn_star_statistics(sale_dept,sale_name,sale_account,star4,star5,gmt_target,gmt_created,gmt_modified) VALUES ('"
			+saleDept+"','"+saleName+"','"+saleAccount+"',"+star4+","+star5+",date_format(date_add(now(), interval -1 day),'%Y-%m-%d'),now(),now()"
			+")";
			boolean result=DBUtils.insertUpdate(DB, sql);
			if (!result) {
			LOG.info(">>>>>>>>>>>>>>>>>统计转四星,转五星失败:"+sql);
			}
	}
	
	@Override
	public boolean init() throws Exception {
		return false;
	}
	
	public static void main(String[] args) throws Exception {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		CrmFourOrFiveTask task = new CrmFourOrFiveTask();
		task.exec(new Date());
	}

}
