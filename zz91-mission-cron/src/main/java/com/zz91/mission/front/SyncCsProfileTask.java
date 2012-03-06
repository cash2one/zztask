package com.zz91.mission.front;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

/**
 * <br />任务描述：
 * <br />同步crm_cs_profile的数据，将company,company_account的数据同步到crm_cs_profile
 * <br />
 * <br />涉及表：crm_cs_profile(用户服基本信息表),company(用户公司信息表),company_account(账户表)
 * <br />
 * <br />step1:
 * <br />查找需要同步的profile
 * <br />
 * <br />step2:
 * <br />同步每条profile
 * <br />
 * @author mays (mays@zz91.com)
 *
 * created on 2011-9-29
 */
public class SyncCsProfileTask implements ZZTask {
	
	public static void main(String[] args) {
		
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		SyncCsProfileTask task=new SyncCsProfileTask();
		long start=DateUtil.getSecTimeMillis();
		try {
			task.exec(DateUtil.getDate("2011-9-22", "yyyy-MM-dd"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		long end=DateUtil.getSecTimeMillis();
		System.out.println("总耗时："+(end-start)+"秒");
	}

	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		
		int max=queryMaxSize();
		int limit=1000;
		Map<String, String> errorMap=new HashMap<String, String>();
		for(int i=0;i<(max/limit+1);i++){
			batchSync(i, limit, errorMap);
		}
		if(errorMap.size()>0){
			throw new Exception("数据没有全部同步 Exception:"+JSONObject.fromObject(errorMap).toString());
		}
		return true;
	}

	private int queryMaxSize() throws Exception{
		int max=0;
		Connection conn=null;
		ResultSet rs=null;
		try {
			conn=DBUtils.getConnection("ast");
			rs=conn.createStatement().executeQuery("select count(*) from crm_cs_profile");
			while(rs.next()){
				max=rs.getInt(1);
			}
		} catch (SQLException e) {
			throw new Exception("没有获取到profile总数 Exception:"+e.getMessage());
		}finally{
			try {
				if(conn!=null){
					conn.close();
				}
				if(rs!=null){
					rs.close();
				}
			} catch (Exception e2) {
				throw new Exception("数据库连接没有关闭 Exception:"+e2.getMessage());
			}
		}
		return max;
	}
	
	private void batchSync(Integer i, Integer limit, Map<String, String> error) throws Exception{
		final List<Integer> companyList=new ArrayList<Integer>();
		DBUtils.select("ast", "select company_id from crm_cs_profile order by id desc limit "+(i*limit)+","+limit, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					companyList.add(rs.getInt(1));
				}
			}
		});
		
		for(Integer id:companyList){
			syncProfile(id, error);
		}
		
	}
	
	private void syncProfile(Integer companyId, Map<String, String> error) throws Exception{
//		System.out.println("开始同步："+companyId);
		final Map<String, Object> column=new HashMap<String, Object>();
		StringBuffer sb=new StringBuffer();
		sb.append(" select c.id, c.membership_code, c.service_code, c.classified_code, c.star_sys,");
		sb.append(" c.star, c.num_visit_month, c.name, c.area_code, c.gmt_visit, c.domain,");
		sb.append(" c.domain_zz91, c.is_block");
		sb.append(" from company c where c.id=").append(companyId);
		sb.append(" limit 1");
		
		DBUtils.select("ast", sb.toString(), new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					column.put("company_id", rs.getInt(1));
					column.put("membership_code", rs.getString(2));
					column.put("service_code", rs.getString(3));
					column.put("classified_code", rs.getString(4));
					column.put("star_sys", rs.getInt(5));
					column.put("star", rs.getInt(6));
					column.put("num_visit_month", rs.getInt(7));
					column.put("name", rs.getString(8));
					column.put("area_code", rs.getString(9));
					column.put("gmt_visit", rs.getDate(10));
					column.put("domain", rs.getString(11));
					column.put("domain_zz91", rs.getString(12));
					column.put("is_block", rs.getString(13));
				}
			}
		});
		
		StringBuffer sb2=new StringBuffer();
		sb2.append(" select account, email, num_login, gmt_last_login, mobile, contact, ");
		sb2.append(" tel, tel_country_code, tel_area_code, fax, fax_country_code, fax_area_code ");
		sb2.append(" from company_account where company_id=").append(companyId);
		sb2.append(" and is_admin='1' limit 1");
		
		DBUtils.select("ast", sb2.toString(), new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					column.put("account", rs.getString(1));
					column.put("email", rs.getString(2));
					column.put("num_login", rs.getInt(3));
					column.put("gmt_last_login", rs.getDate(4));
					column.put("mobile", rs.getString(5));
					column.put("contact", rs.getString(6));
					column.put("tel", rs.getString(7));
					column.put("tel_country_code", rs.getString(8));
					column.put("tel_area_code", rs.getString(9));
					column.put("fax", rs.getString(10));
					column.put("fax_country_code", rs.getString(11));
					column.put("fax_area_code", rs.getString(12));
				}
			}
		});
		
		Connection conn=null;
		PreparedStatement pstat=null;
		StringBuffer sb3=new StringBuffer();
		sb3.append(" update crm_cs_profile set");
		sb3.append(" membership_code=?,");
		sb3.append(" service_code=?,");
		sb3.append(" classified_code=?,");
		sb3.append(" star_sys=?,");
		sb3.append(" star=?,");
		sb3.append(" num_visit_month=?,");
		sb3.append(" name=?,");
		sb3.append(" area_code=?,");
		sb3.append(" gmt_visit=?,");
		sb3.append(" domain=?,");
		sb3.append(" domain_zz91=?,");
		sb3.append(" is_block=?,");
		sb3.append(" account=?,");
		sb3.append(" email=?,");
		sb3.append(" num_login=?,");
		sb3.append(" gmt_last_login=?,");
		sb3.append(" mobile=?,");
		sb3.append(" contact=?,");
		sb3.append(" tel=?,");
		sb3.append(" tel_country_code=?,");
		sb3.append(" tel_area_code=?,");
		sb3.append(" fax=?,");
		sb3.append(" fax_country_code=?,");
		sb3.append(" fax_area_code=?,");
		sb3.append(" gmt_modified=now() ");
		sb3.append(" where company_id=").append(companyId);
		
		try {
			conn=DBUtils.getConnection("ast");
			pstat = conn.prepareStatement(sb3.toString());
			pstat.setString(1, valueofString(column.get("membership_code")));
			pstat.setString(2, valueofString(column.get("service_code")));
			pstat.setString(3, valueofString(column.get("classified_code")));
			pstat.setInt(4, valueofInt(column.get("star_sys")));
			pstat.setInt(5, valueofInt(column.get("star")));
			pstat.setInt(6, valueofInt(column.get("num_visit_month")));
			pstat.setString(7, valueofString(column.get("name")));
			pstat.setString(8, valueofString(column.get("area_code")));
			pstat.setDate(9, valueofDate(column.get("gmt_visit")));
			pstat.setString(10, valueofString(column.get("domain")));
			pstat.setString(11, valueofString(column.get("domain_zz91")));
			pstat.setString(12, valueofString(column.get("is_block")));
			pstat.setString(13, valueofString(column.get("account")));
			pstat.setString(14, valueofString(column.get("email")));
			pstat.setInt(15, valueofInt(column.get("num_login")));
			pstat.setDate(16, valueofDate(column.get("gmt_last_login")));
			pstat.setString(17, valueofString(column.get("mobile")));
			pstat.setString(18, valueofString(column.get("contact")));
			pstat.setString(19, valueofString(column.get("tel")));
			pstat.setString(20, valueofString(column.get("tel_country_code")));
			pstat.setString(21, valueofString(column.get("tel_area_code")));
			pstat.setString(22, valueofString(column.get("fax")));
			pstat.setString(23, valueofString(column.get("fax_country_code")));
			pstat.setString(24, valueofString(column.get("fax_area_code")));
			pstat.executeUpdate();
		} catch (SQLException e) {
			if(error.size()<=100){
				error.put(String.valueOf(companyId), e.getMessage());
			}
//			throw new Exception(e.getMessage());
		}finally{
			try {
				if(conn!=null){
					conn.close();
				}
				if(pstat!=null){
					pstat.close();
				}
			} catch (Exception e2) {
				throw new Exception("数据库连接没有关闭 Exception:"+e2.getMessage());
			}
		}
		
	}
	
	private String valueofString(Object str){
		if(str==null){
			return "";
		}
		return String.valueOf(str);
	}
	
	private Integer valueofInt(Object str){
		if(str==null){
			return 0;
		}
		return (Integer) str;
	}
	
	private java.sql.Date valueofDate(Object str){
		if(str==null){
			return null;
		}
		return (java.sql.Date) str;
	}
	
	@Override
	public boolean init() throws Exception {
		return false;
	}

}
