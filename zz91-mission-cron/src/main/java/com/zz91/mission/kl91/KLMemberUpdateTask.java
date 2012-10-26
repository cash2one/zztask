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

/**
 * 
 * @author 伍金成 kl91高会过期的会员转为普会
 * 1,根据过期时间搜索昨天的过期高会的公司id
 *2，根据id更新公司信息的会员类型
 */
public class KLMemberUpdateTask implements ZZTask{
	
	private final static String DB_KL91 = "kl91";

	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		int i=0;
		int docs=0;
		String from = DateUtil.toString(
				DateUtil.getDateAfterDays(baseDate, -1), "yyyy-MM-dd");
		String to = DateUtil.toString(baseDate, "yyyy-MM-dd");
		do{
			String sqlId = "select cid from company_service where service_code='10021000' and gmt_end >= '"+from+"' and '"+to+"'> gmt_end order by gmt_created limit "+i*10+",10";
			final List<Integer> companyIdlist = new ArrayList<Integer>();
			DBUtils.select(DB_KL91, sqlId, new IReadDataHandler() {
				@Override
				public void handleRead(ResultSet rs) throws SQLException {
					while (rs.next()) {
						companyIdlist.add(rs.getInt(1));
					}
				}
			});
			
			if(companyIdlist.size()<=0){
				break;
			}
			
			
			for (Integer companyId : companyIdlist) {
				updateCompany(companyId);
			}
			
			i++;
			docs=docs+companyIdlist.size();
		}while(true);
		
		if(i>0){
			throw new Exception("共更新数据"+docs+"条");
		}else if(i==0){
			throw new Exception("共更新数据0条");
		}
		
		return false;
	}


	private void updateCompany(Integer companyId) {
		String sql="update company set membership_code='10051000' , gmt_modified=now() where id="+companyId+" and id>0";
		DBUtils.insertUpdate(DB_KL91, sql);
	}

	@Override
	public boolean init() throws Exception {
		return false;
	}
	
	public static void main(String[] args) throws Exception {
		DBPoolFactory.getInstance().init(
				"file:/usr/tools/config/db/db-zztask-jdbc.properties");
		KLMemberUpdateTask obj = new KLMemberUpdateTask();
		Date date = DateUtil.getDate("2012-10-08", "yyyy-MM-dd");
		obj.exec(date);
	}
}
