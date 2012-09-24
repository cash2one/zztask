package com.zz91.mission.kl91;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;

public class KlCrmNumLoginImport implements ZZTask{

	final static String DB="kl91_crm_test";
	final static String DB_kl91="kl91";
	static final Integer LIMIT = 10;
	
	@Override
	public boolean exec(Date baseDate) throws Exception {
//		String sql="select count(*) from crm_company ";
//		final Integer[] count=new Integer[1];
//		count[0]=0;
//		DBUtils.select(DB, sql, new IReadDataHandler() {
//			@Override
//			public void handleRead(ResultSet rs) throws SQLException {
//				while(rs.next()){
//					count[0]=rs.getInt(1);
//				}
//			}
//		});
//		Integer total=count[0]/100;	
//		for(Integer i=1;i<=total;i++){
//			String sqlId = "select cid from crm_company limit "+100*(i-1) +"," + 100;
//			final List<Integer> companyId = new ArrayList<Integer>();
//			DBUtils.select(DB, sqlId, new IReadDataHandler() {
//				@Override
//				public void handleRead(ResultSet rs) throws SQLException {
//					while (rs.next()) {
//						companyId.add(rs.getInt(1));
//					}
//				}
//			});
//			//导入公司
//			for (Integer cid : companyId) {
//				selectCompany(cid);
//			}
//		}
//搜出昨天登录的客户
		String from=DateUtil.toString(DateUtil.getDateAfterDays(baseDate, -1), "yyyy-MM-dd HH:mm:ss");
		String to=DateUtil.toString(baseDate, "yyyy-MM-dd HH:mm:ss");
		String sqlId = "select id from company where gmt_last_login > '"+from+"' and '"+to+"' > gmt_last_login";
		final List<Integer> companyId = new ArrayList<Integer>();
		DBUtils.select(DB_kl91, sqlId, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					companyId.add(rs.getInt(1));
				}
			}
		});
//根据id搜出公司的id，和numLogin登陆次数
		for (Integer cid : companyId) {
			selectCompany(cid);
		}
		return true;
		
	}
//搜出kl91的公司登陆次数
	private void selectCompany(Integer cid) {
		String sql="select id,num_login,gmt_last_login from company where id="+cid+" ";
		DBUtils.select(DB_kl91, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					Integer loginCount=rs.getInt(2);
					Integer companyId=rs.getInt(1);
					String gmtLastLogin=rs.getString(3);
					if(loginCount!=null){
						updateLoginCount(companyId,loginCount,gmtLastLogin);
					}
					selectCrmCompanyStatus();
				}
			}
		});
	}
	
//搜索crm的未激活客户放到公海
	private void selectCrmCompanyStatus() {
		String sql="update crm_company set ctype = 2 where ctype=5";
		DBUtils.insertUpdate(DB, sql);
	}
	
//更新crm的登录次数
	private void updateLoginCount(Integer companyId, Integer loginCount,String gmtLastLogin) {
		String sql="update crm_company set login_count="+loginCount+",gmt_login='"+gmtLastLogin+"' where cid="+companyId+"";
		DBUtils.insertUpdate(DB, sql);
	}

	@Override
	public boolean init() throws Exception {
		return false;
	}
	
	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	
	public static void main(String[] args) throws Exception {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		KlCrmNumLoginImport obj = new KlCrmNumLoginImport();
//		API_HOST = "http://localhost:8090/front";
		//Date date = DateUtil.getDate("2012-09-21", "yyyy-MM-dd");
		Date date = DateUtil.getDate("2012-09-25", "yyyy-MM-dd");
		obj.exec(date);
	}

}
