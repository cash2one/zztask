/**
 * 
 */
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
 * 纠正积分总分计算错误的问题,积分同步统计
 * 
 * 涉及表：score_summary,score_change_details
 * 
 * 处理逻辑：
 * 1.清理score_summary
 * 2.查找所有company_id
 * 3.计算积分变更总数,param：company_id
 * 4.将计算结果写入score_summary,param：company_id
 * 
 * @author root
 *
 */
public class CorrectIntegralTask implements ZZTask{
	
	final static String DATE_FORMAT = "yyyy-MM-dd";
	
	private final int MAX_SIZE =1000;
	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		int max=queryMaxSize();
		for(int i=0;i<(max/MAX_SIZE+1);i++) {
			sumScoreChangeDetails(i,MAX_SIZE);
		}
		return true;
	}
	
	private void sumScoreChangeDetails(Integer i,Integer limit) {
		
		//查找所有company_id并放入HashMap
		String sql1="select company_id from score_summary limit "+(i*limit)+","+limit;
		
		final Map<Integer, Integer> map=new HashMap<Integer, Integer>();
		DBUtils.select("ast",sql1, new IReadDataHandler() {
	
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()) {
					map.put(rs.getInt(1), 0);
				}
			}
	
		});
		
		//统计积分变更总数
		for(Integer companyId:map.keySet()) {
			String sql = "select company_id, sum(score) from score_change_details where company_id="+companyId;
			DBUtils.select("ast", sql, new IReadDataHandler() {
				
				@Override
				public void handleRead(ResultSet rs) throws SQLException {
					
					while(rs.next()) {
						map.put(rs.getInt(1), rs.getInt(2));
					}
				}
			});
		}
		
		//将计算结果写入score_summary
		for(Integer companyId:map.keySet()) {
			String sql = "update score_summary set score="+map.get(companyId)+" where company_id="+companyId;
			DBUtils.insertUpdate("ast", sql);
		}
		
	}
	
	private int queryMaxSize() throws Exception{
		final Integer[] cid=new Integer[1];
		cid[0]=0;
		DBUtils.select("ast", "select count(*) from score_summary", new IReadDataHandler() {
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
	public boolean init() throws Exception {
		return false;
	}
	
	public static void main(String[] args) {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		CorrectIntegralTask ciLog=new CorrectIntegralTask();
//		ciLog.DB_NAME="ast_test";
		try {
			long start=System.currentTimeMillis();
			ciLog.clear(DateUtil.getDate(new Date(), DATE_FORMAT));
			ciLog.exec(DateUtil.getDate(new Date(), DATE_FORMAT));
			long end=System.currentTimeMillis();
			System.out.println("共耗时："+(end-start));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
