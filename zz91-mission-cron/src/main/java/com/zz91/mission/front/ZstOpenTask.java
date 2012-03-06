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
 * <br />改变再生通生效的用户的membership_code
 * <br />
 * <br />涉及表：crm_company_service(用户服务开通表),company(用户公司信息表)
 * <br />
 * <br />step1:
 * <br />判断再生通服务是否生效（再生通服务code:1000）
 * <br />查找出所有再生通服务生效的客户
 * <br />
 * <br />step2:
 * <br />将公司信息表的membership_code更新为生效客户指定的再生通类型,zst_flag更新为1
 * <br />
 * @author mays (mays@zz91.com)
 *
 * created on 2011-8-3
 */
public class ZstOpenTask implements ZZTask {

	@Override
	public boolean init() throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		String df=DateUtil.toString(DateUtil.getDateAfterDays(baseDate, -1),"yyyy-MM-dd");
		String dt=DateUtil.toString(baseDate,"yyyy-MM-dd");
		String sql1="select membership_code,company_id from crm_company_service where crm_service_code='1000' and apply_status='1' and " +
				" '"+df+"'<= gmt_start and gmt_start<='"+dt+"'and membership_code is not null and membership_code!=''";
		final Map<Integer, String> map=new HashMap<Integer, String>();
		DBUtils.select("ast",sql1, new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()) {
					map.put(rs.getInt(2),rs.getString(1));
				}
			}
		});
		
		for (Integer companyId:map.keySet()) {
			DBUtils.insertUpdate("ast","update company set membership_code='"+map.get(companyId)+"', zst_flag='1', gmt_modified=now() where id="+companyId);
		}
		return true;
	}

	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}
	public static void main(String[] args) {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		ZstOpenTask openTask=new ZstOpenTask();
		try {
			openTask.exec(DateUtil.getDate("2011-9-22", "yyyy-MM-dd"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
