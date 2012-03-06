package com.zz91.mission.front;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;

/**
 * <br />任务描述：
 * <br />再生通服务年限信息恢复任务，用来重新统计再生通服务年限
 * <br />
 * <br />涉及表：crm_company_service(用户服务开通表),company(用户公司信息表)
 * <br />
 * <br />step1:
 * <br />查找前一天生效的所有再生通服务的公司，同时计算每个公司的总再生通服务年限
 * <br />
 * <br />step2:
 * <br />将计算结果写入company表zst_year
 * <br />
 * @author mays (mays@zz91.com)
 *
 * created on 2011-8-3
 */
public class ZstYearRecoverTask implements ZZTask {
	private final int MAX_SIZE =1000;
	@Override
	public boolean init() throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		int max=queryMaxSize();
		for(int i=0;i<(max/MAX_SIZE+1);i++) {
			sumCrmCompanyService(i,MAX_SIZE);
		}
		return true;
	}
	
	private void sumCrmCompanyService(Integer i,Integer limit) {
		long start=System.currentTimeMillis();
		String sql1="select distinct company_id from crm_company_service where crm_service_code='1000' and apply_status='1' limit "+(i*limit)+","+limit;
		final Map<Integer, Integer> map=new HashMap<Integer, Integer>();
		DBUtils.select("ast",sql1, new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()) {
					map.put(rs.getInt(1), 0);
				}
			}
			
		});
		long end=System.currentTimeMillis();
		System.out.println(">>>"+i+":"+(end-start));
		
		for(Integer companyId:map.keySet()){
			String sql="select company_id, sum(zst_year) from crm_company_service where crm_service_code='1000' and apply_status='1' and company_id="+companyId;
			DBUtils.select("ast",sql, new IReadDataHandler() {
				@Override
				public void handleRead(ResultSet rs) throws SQLException {
					while(rs.next()) {
						map.put(rs.getInt(1), rs.getInt(2));
					}
				}
			});
		}
		
		for(Integer companyId:map.keySet()){
			String sql = "update company set zst_year="+map.get(companyId)+", gmt_modified=now() where id="+companyId; 
			DBUtils.insertUpdate("ast",sql);
		}
	}
	
	private int queryMaxSize() throws Exception{
		final Integer[] cid=new Integer[1];
		cid[0]=0;
		String sql = "select count(*) from crm_company_service";
		DBUtils.select("ast", sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					cid[0]=rs.getInt(1);
				}
			}
		});
		
		return cid[0];
	}
	
	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}
	
	public static void main(String[] args) {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		ZstYearRecoverTask openTask=new ZstYearRecoverTask();
		try {
			long start=System.currentTimeMillis();
			openTask.exec(DateUtil.getDate(new Date(), "yyyy-MM-dd"));
			long end=System.currentTimeMillis();
			System.out.println("共耗时："+(end-start));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
