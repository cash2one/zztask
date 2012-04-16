package com.zz91.mission.front;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.zz91.task.common.ZZTask;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;

/**
 * 1.获取要处理的供求ID
 * 2.根据供求ID处理状态
 * 
 *@Author:kongsj
 *@Date:2012-3-5
 */
public class ClearTradeSpamTask implements ZZTask {
	private static String UNPASS_REASON="尊敬的客户，您好！由于您所发布的供求信息长时间没有刷新或更新，系统已自动对本条信息进行退回处理，建议您及时登陆生意管家，对于产品信息进行更新发布，提高生意的成交率，由此给您造成的不便，敬请见谅！如对本次系统操作有任何的疑问或问题，欢迎致电：0571—56633057。";
	@Override
	public boolean init() throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		String sql = "select id from products where check_status=0 and check_person ='0' ";
		updateStatus("2005-11-29","2012-02-01",sql);
		sql = "select id from products where check_status=0 and check_person is null ";
		updateStatus("2005-11-29","2012-02-01",sql);
		sql = "select id from products where check_status=0 and unchecked_check_status =1 ";
		updateStatus("2005-11-29","2012-02-01",sql);
		return true;
	}

	private void updateStatus(String from, String to,String sql){
		
		boolean isExists=true;
		sql=sql+"and real_time >= '"+from+"' and real_time <'"+to+"' limit 100";
		do{
			
			final List<Integer> idList=new ArrayList<Integer>();
			
			DBUtils.select("ast", sql, new IReadDataHandler() {
				@Override
				public void handleRead(ResultSet rs) throws SQLException {
					while(rs.next()){
						idList.add(rs.getInt(1));
					}
				}
			});
			if(idList.size()==0){
				isExists=false;
			}else{
				for(Integer id:idList){
					DBUtils.insertUpdate("ast", "update products set check_status=2, unpass_reason = '"+UNPASS_REASON+"' where id="+id);
				}
			}
			
		}while(isExists);
		
		
	}
	
	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}
	
	public static void main(String[] args){
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		ClearTradeSpamTask cl =new ClearTradeSpamTask();
		try {
			cl.exec(new Date());
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

