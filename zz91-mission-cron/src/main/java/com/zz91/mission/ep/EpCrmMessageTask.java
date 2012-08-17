package com.zz91.mission.ep;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IInsertUpdateHandler;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;
/**
 * @author 黄怀清
 * 2012-08-14
 * 统计询盘信息
 */
public class EpCrmMessageTask implements ZZTask {

	Logger LOG = Logger.getLogger(EpCrmMessageTask.class);

	final static String ADMINDB = "ep";
	final static String CRMDB="crm";
	@Override
	public boolean clear(Date baseDate) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		boolean result = false;
		do {
			//差日期条件等
			String reTotalSql="SELECT count(id) total FROM message m " 
					+ "where date_format(gmt_created,'%Y-%m-%d')= date_add(date_format(now(),'%Y-%m-%d'),INTERVAL -1 DAY)";
			DBUtils.select(ADMINDB, reTotalSql, new IReadDataHandler() {
				
				@Override
				public void handleRead(ResultSet rs) throws SQLException {
					Integer total=0;
					while (rs.next()) {
						total = rs.getInt("total");
					}
					Integer pageNum=10;
					Integer page = (total-1)/pageNum+1;
					final Map<String,Timestamp> reMap = new HashMap<String, Timestamp>();
					final Map<String,Timestamp> sendMap = new HashMap<String, Timestamp>();
					for (int i = 0; i < page; i++) {
						//询盘信息
						String sql = "SELECT m.gmt_created,m.target_cid,m.cid FROM message m " 
								+ "where date_format(gmt_created,'%Y-%m-%d')= date_add(date_format(now(),'%Y-%m-%d'),INTERVAL -1 DAY) "
								+ "order by m.gmt_created limit "+i*pageNum+","+pageNum;
						DBUtils.select(ADMINDB, sql, new IReadDataHandler() {
							@Override
							public void handleRead(ResultSet rs) throws SQLException {
								while (rs.next()) {
									reMap.put(rs.getString("target_cid"), rs.getTimestamp("gmt_created"));
									
									sendMap.put(rs.getString("cid"), rs.getTimestamp("gmt_created"));
								}
							}
						});
						
					}
					
					//更新时间
					updateMessageTime(reMap,sendMap);
					
					
				}
			});
			
			result = true;
		} while (false);
		
		
		
		return result;
	}

	protected void updateMessageTime(final Map<String,Timestamp> reMap,final Map<String,Timestamp> sendMap) {
		try {
			//接收询盘
			Set<String> rekeys = reMap.keySet();
			for(final String key:rekeys){
				//admin
				String sql="update comp_profile set receive_time=? where id=" + key;
				DBUtils.insertUpdate(ADMINDB, sql, new IInsertUpdateHandler() {
					@Override
					public void handleInsertUpdate(PreparedStatement stat)
							throws SQLException {
						try {
							stat.setTimestamp(1, reMap.get(key));
							stat.execute();
						} catch (Exception e) {
							LOG.info(">>>>>>>>> >>>>>>>更新admin接收询盘时间失败!");
						}
						
					}
				});
				//crm
				String crmsql="update crm_company set receive_time=? where id=" + key;
				DBUtils.insertUpdate(CRMDB, crmsql, new IInsertUpdateHandler() {
					@Override
					public void handleInsertUpdate(PreparedStatement stat)
							throws SQLException {
						try {
							stat.setTimestamp(1, reMap.get(key));
							stat.execute();
						} catch (Exception e) {
							LOG.info(">>>>>>>>> >>>>>>>更新crm接收询盘时间失败!");
						}
						
					}
				});
				
			}
			//发送询盘
			Set<String> sendkeys = sendMap.keySet();
			for(final String key:sendkeys){
				//admin
				String sql="update comp_profile set send_time=? where id=" + key;			
				DBUtils.insertUpdate(ADMINDB, sql, new IInsertUpdateHandler() {
					@Override
					public void handleInsertUpdate(PreparedStatement stat)
							throws SQLException {
						try {
							stat.setTimestamp(1, sendMap.get(key));
							stat.execute();
						} catch (Exception e) {
							LOG.info(">>>>>>>>> >>>>>>>更新admin发送询盘时间失败!");
						}
						
					}
				});
				
				//crm
				String crmsql="update crm_company set send_time=? where id=" + key;			
				DBUtils.insertUpdate(CRMDB, crmsql, new IInsertUpdateHandler() {
					@Override
					public void handleInsertUpdate(PreparedStatement stat)
							throws SQLException {
						try {
							stat.setTimestamp(1, sendMap.get(key));
							stat.execute();
						} catch (Exception e) {
							LOG.info(">>>>>>>>> >>>>>>>更新crm发送询盘时间失败!");
						}
						
					}
				});
			}
		} catch (Exception e) {
			LOG.info(">>>>>>>>> >>>>>>>统计公司询盘失败!");
		}
	}
	
	@Override
	public boolean init() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
	
//	public static void main(String[] args) {
//		try {
//			DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
//			EpCrmMessageTask et=new EpCrmMessageTask();
//			et.exec(null);
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//	}

}
