/**
 * Copyright 2011 ASTO.
 * All right reserved.
 * Created on 2011-6-2
 */
package com.zz91.mission.zz91;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.mail.MailUtil;

/**
 * <br />任务描述：
 * <br />EDM 现货宣传
 * <br />
 * <br />处理逻辑：
 * <br />1.获取再生通用户信息
 * <br />2.发送宣传邮件
 * <br />
 * @author mays (mays@zz91.com)
 *
 * created on 2011-6-2
 */
public class EdmXianhuoTask implements ZZTask {
	
	public static String DB="ast";
	final static String DATE_FORMATE="yyyy-MM-dd";
	
	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		
		Integer initId = null, size=0;
		do {
			
			Map<Integer, String> email = edmTarget(initId);
			if(email.size() == 0){
				break;
			}
			
			for(Integer id: email.keySet()){
//				System.out.println(id+":"+email.get(id));
				//Email
				String e = email.get(id);
				
//				if("x03570227@163.com".equals(e)){
					MailUtil.getInstance().sendMail(
							"ZZ91现货商城火热上线，双12双料惊喜大放送", 
							e, null,
							null, "zz91", "zz91-xianhuo",
							null, MailUtil.PRIORITY_TASK);
//				}
				
				initId=id;
			}
			
			size=size+email.size();
			
		} while (true);
		
		throw new Exception("共发送邮件"+size+"封");
	}
	
	private Map<Integer, String> edmTarget(Integer id){
		StringBuffer sb=new StringBuffer();
		sb.append("select id, email, back_email, is_use_back_email from company_account ca ");
		sb.append(" where not exists (select id from company c where c.id = ca.company_id and c.membership_code = '10051000') ");
		if(id!=null){
			sb.append(" and id > ").append(id);
		}
		sb.append(" order by id asc ");
		sb.append(" limit 20");

		final Map<Integer, String> targetEmail = new LinkedHashMap<Integer, String>();
		
		DBUtils.select(DB, sb.toString(), new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					String email = rs.getString("email");
					if( "1".equals(rs.getString("is_use_back_email")) ){
						email=rs.getString("back_email");
					}
					targetEmail.put(rs.getInt("id"), email);
				}
			}
		});
		
		return targetEmail;
	}
	
	@Override
	public boolean init() throws Exception {
		return false;
	}
	
	public static void main(String[] args) throws ParseException, Exception {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");

		EdmXianhuoTask task=new EdmXianhuoTask();
		EdmXianhuoTask.DB="ast";
		task.exec(DateUtil.getDate("2012-11-02", "yyyy-MM-dd"));
	}

}
