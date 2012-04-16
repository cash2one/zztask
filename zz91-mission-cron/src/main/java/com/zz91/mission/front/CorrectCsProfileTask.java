/**
 * 
 */
package com.zz91.mission.front;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.zz91.task.common.ZZTask;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;

/**
 * 纠正crm_cs_profile中丢失的公司profile信息
 * @author root
 *
 */
public class CorrectCsProfileTask implements ZZTask{
	
	public static String DB="ast";

	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		final Set<Integer> idSet=new HashSet<Integer>();
		String sql="select company_id from crm_cs where not exists (select company_id from crm_cs_profile where crm_cs.company_id=company_id)";
		correct(sql, idSet);
		sql="select company_id from crm_company_service ccs where not exists (select company_id from crm_cs_profile where ccs.company_id=company_id)";
		correct(sql, idSet);
		
		for(Integer id:idSet){
			recreateProfile(id);
//			System.out.println(id);
		}
//		System.out.println("-----"+idSet.size());
		return true;
	}
	
	private Set<Integer> correct(String sql, final Set<Integer> ids) {
		DBUtils.select(DB, sql, new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					ids.add(rs.getInt(1));
				}
			}
		});
		
		return ids;
	}
	
	private void recreateProfile(Integer cid){
		String sql="insert into crm_cs_profile (company_id, account, email, gmt_created,gmt_modified) values ("+cid+",'','',now(),now())";
		DBUtils.insertUpdate(DB, sql);
	}

	@Override
	public boolean init() throws Exception {
		return false;
	}

	public static void main(String[] args) throws Exception {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		
		CorrectCsProfileTask task=new CorrectCsProfileTask();
		CorrectCsProfileTask.DB="ast_test";
		task.exec(new Date());
	}
}
