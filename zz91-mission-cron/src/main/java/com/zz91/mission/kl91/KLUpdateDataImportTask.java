package com.zz91.mission.kl91;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.lang.StringUtils;

/**
 * @author 伍金成:更新之前从zz91导入kl91的客户注册时间(任务只运行一次)
 * 1,搜出kl91所有有old_id的客户
 * 2,搜出zz91的公司注册时间
 * 3,更新kl91公司注册时间
 *
 */
public class KLUpdateDataImportTask implements ZZTask{

	private final static String DB_AST = "ast";
	private final static String DB_KL91 = "kl91";

	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		int i=0;
		int docs=0;
		
		do{
			//搜索kl91表所有有oldid的公司id
			String sqlId = "select old_id from company where old_id > 0 limit "+i*10+",10";
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
			
			// 根据old_id搜索ast公司的信息
			for (Integer companyId : companyIdlist) {
				selectZZCompanyIdAndImport(companyId);
			}
			
			i++;
			docs=docs+companyIdlist.size();
		}while(true);
		
		if(i>0){
			throw new Exception("共更新数据"+docs+"条");
		}
		
		return false;
	}

	//根据kl91的old_id搜索zz91的公司信息（这里old_id就是zz91的公司id）
	private void selectZZCompanyIdAndImport(Integer companyId) {
		String sql="select regtime from company where id="+companyId+"";
		final Map<String, Object> map = new HashMap<String, Object>();
		DBUtils.select(DB_AST, sql, new IReadDataHandler() {			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					map.put("regtime", rs.getString(1));
				}
			}
		});
		String regtime=(String)map.get("regtime");
		if(StringUtils.isEmpty(regtime)){
			regtime=DateUtil.toString(new Date(), "yyyy-MM-dd HH:mm:ss");
		}
		//根据old_id更新kl91的公司注册时间
		update(companyId,regtime);
	}
	
	private void update(Integer cid, String regtime) {
		String sql="update company set gmt_modified=now(),gmt_created='"+regtime+"' where old_id="+cid+"";
		DBUtils.insertUpdate(DB_KL91, sql);
	}
	
	@Override
	public boolean init() throws Exception {
		return false;
	}

	public static void main(String[] args) throws Exception {
		DBPoolFactory.getInstance().init(
				"file:/usr/tools/config/db/db-zztask-jdbc.properties");
		KLUpdateDataImportTask obj = new KLUpdateDataImportTask();
		Date date = DateUtil.getDate("2008-11-25", "yyyy-MM-dd");
		obj.exec(date);
	}

}
