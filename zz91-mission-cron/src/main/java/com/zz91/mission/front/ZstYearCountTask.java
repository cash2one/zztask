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
 * <br />再生通服务年限计算
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
public class ZstYearCountTask implements ZZTask {

	@Override
	public boolean init() throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		String df=DateUtil.toString(DateUtil.getDateAfterDays(baseDate, -1),"yyyy-MM-dd");
		String dt=DateUtil.toString(baseDate,"yyyy-MM-dd");
		
		String sql1="select company_id from crm_company_service where crm_service_code='1000' and apply_status='1' and " +
				" '"+df+"'<= gmt_start and gmt_start<'"+dt+"'";
		final Map<Integer, Integer> map=new HashMap<Integer, Integer>();
		DBUtils.select("ast",sql1, new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()) {
					map.put(rs.getInt(1), 0);
				}
			}
			
		});
		
		for(Integer id:map.keySet()){
			DBUtils.select("ast","select company_id, sum(zst_year) from crm_company_service where crm_service_code='1000' and apply_status='1' and company_id="+id, new IReadDataHandler() {
				@Override
				public void handleRead(ResultSet rs) throws SQLException {
					while(rs.next()) {
						map.put(rs.getInt(1), rs.getInt(2));
					}
				}
			});
		}
		
		for(Integer id:map.keySet()){
			DBUtils.insertUpdate("ast","update company set zst_year="+map.get(id)+", gmt_modified=now() where id="+id);
		}
		
		return true;
	}
	

	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}
	
	public static void main(String[] args) {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		ZstYearCountTask openTask=new ZstYearCountTask();
		try {
			openTask.exec(DateUtil.getDate("2011-9-28", "yyyy-MM-dd"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
