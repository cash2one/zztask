package com.zz91.mission.ep.sync;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import com.zz91.mission.domain.ep.CompAccount;
import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.http.HttpUtils;
import com.zz91.util.lang.StringUtils;
import com.zz91.util.mail.MailUtil;

public class SyncCompAccountTask implements ZZTask {
	
	final static String DB_EP="ep";
	
	final static String SYNC_TABLE="comp_account";
	
	final static Integer LIMIT=50;
	
	final static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	
	final static String SYNC_URL_SEED="http://huanbaoadmin.zz91.com/sync/seed";
	final static String SYNC_URL_IMPT="http://huanbaoadmin.zz91.com/sync/imptCompAccount";
	
	@Override
	public boolean init() throws Exception {
		
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		
		String maxInfo=HttpUtils.getInstance().httpGet(SYNC_URL_SEED+"?table=comp_account", HttpUtils.CHARSET_UTF8);
		long now = new Date().getTime();
		JSONObject jobj=JSONObject.fromObject(maxInfo);
		
		Integer unSyncMaxId=jobj.getInt("maxId");
		long modifiedSeed=jobj.getLong("maxGmtModified");
		
		if(modifiedSeed < now){
			modifiedSeed=now;
		}
		
		Integer success=0;
		Integer failure=0;
		
		do{
//			if((success+failure)>=100000){
//				break;
//			}
			
			List<CompAccount> list=queryCompAccount(unSyncMaxId);
			
			if(list==null || list.size()<=0){
				break;
			}
			
			for(CompAccount account:list){
				
				modifiedSeed = buildShowTime(modifiedSeed);
				
				account.setGmtModified(new Date(modifiedSeed));
				
				unSyncMaxId = account.getId();
			}
			
			//提交请求
			NameValuePair[] param=new NameValuePair[]{
				new NameValuePair("compAccount", JSONArray.fromObject(list).toString())
				};
			
			String result=null;
			try {
				result = HttpUtils.getInstance().httpPost(SYNC_URL_IMPT, param, HttpUtils.CHARSET_UTF8);
			} catch (HttpException e) {
			} catch (IOException e) {
			}
			
			if(StringUtils.isEmpty(result) || !result.startsWith("{")){
				for(CompAccount account:list){
					failure++;
					logUnsync(account.getId(), baseDate.getTime());
				}
				
			}
			
			JSONObject resultJson= JSONObject.fromObject(result);
			
			if(resultJson.containsKey("success")){
				success = success + resultJson.getInt("success");
			}

			if(resultJson.containsKey("failure")){
				JSONArray ja=resultJson.getJSONArray("failure");
				for(Object id:ja){
					failure++;
					logUnsync(Integer.valueOf(String.valueOf(id)), baseDate.getTime());
				}
			}
			
			Thread.sleep(500);
		}while(true);
		
		//发送邮件通知结果 
		
		Map<String, Object> dataMap=new HashMap<String, Object>();
		
		Date end=new Date();
		dataMap.put("reportTarget", "帐户信息");
		dataMap.put("successNum", success);
		dataMap.put("failureNum", failure);
		dataMap.put("maxShowTime", DateUtil.toString(new Date(modifiedSeed), DATE_FORMAT));
		dataMap.put("startDate",  DateUtil.toString(new Date(now), DATE_FORMAT));
		dataMap.put("endDate",  DateUtil.toString(end, DATE_FORMAT));
		dataMap.put("timeCost", (end.getTime()-now)/1000);
		
		MailUtil.getInstance().sendMail(
				"环保网数据同步报告 帐户信息", 
				"ep.sync.report@asto.mail", null, 
				null, "zz91", "ep-sync-report",
				dataMap, MailUtil.PRIORITY_TASK);
		
		return true;
	
	}
	
	
	private List<CompAccount> queryCompAccount(Integer maxId){
		final List<CompAccount> list = new ArrayList<CompAccount>();
			StringBuffer sql  = new StringBuffer();
				sql.append("select ");
				sql.append(" id,cid,account,email,password,password_clear,name,")
					.append("sex,mobile,phone_country,phone_area,phone,fax_country,fax_area,fax,")
					.append("dept,contact,position,login_count,login_ip,gmt_login,gmt_register,gmt_created,")
					.append("gmt_modified");
				sql.append(" from comp_account where id>").append(maxId);
				sql.append(" order by id asc limit ").append(LIMIT);
			DBUtils.select(DB_EP, sql.toString(),new IReadDataHandler() {
				
				@Override
				public void handleRead(ResultSet rs) throws SQLException {
					CompAccount user = null;
					while(rs.next()){
						user = new CompAccount();
						user.setId(rs.getInt("id"));
						user.setId(rs.getInt("id"));
						user.setCid(rs.getInt("cid"));
						user.setAccount(rs.getString("account"));
						user.setEmail(rs.getString("email"));
						user.setPassword(rs.getString("password"));
						user.setPasswordClear(rs.getString("password_clear"));
						user.setName(rs.getString("name"));
						user.setSex(rs.getShort("sex"));
						user.setMobile(rs.getString("mobile"));
						user.setPhoneCountry(rs.getString("phone_country"));
						user.setPhoneArea(rs.getString("phone_area"));
						user.setPhone(rs.getString("phone"));
						user.setFaxCountry(rs.getString("fax_country"));
						user.setFaxArea(rs.getString("fax_area"));
						user.setFax(rs.getString("fax"));
						user.setDept(rs.getString("dept"));
						user.setContact(rs.getString("contact"));
						user.setPosition(rs.getString("position"));
						user.setLoginCount(rs.getInt("login_count"));
						user.setLoginIp(rs.getString("login_ip"));
						user.setGmtLogin(getJavaDate(rs.getDate("gmt_login")));
						user.setGmtRegister(getJavaDate(rs.getDate("gmt_register")));
						user.setGmtCreated(getJavaDate(rs.getDate("gmt_created")));
						user.setGmtModified(getJavaDate(rs.getDate("gmt_modified")));
						list.add(user);
					}
					
				}
			});
		return list;
	}
	

	private void logUnsync(Integer id, long gmtTask){
		DBUtils.insertUpdate(DB_EP,
				"insert into unsync_log(unsync_id, sync_table, gmt_task, gmt_created, gmt_modified) values("
						+ id +",'" + SYNC_TABLE + "'," + gmtTask + ",now(),now())");
	}
	
	private Date getJavaDate(java.sql.Date date){
		if(date==null){
			return null;
		}
		return new Date(date.getTime());
	}
	
	public long buildShowTime(long seed){
        
        long now_zero= DateUtil.getTheDayZero(new Date(seed), 0);
        
        long nowtime=seed-(now_zero*1000);
        
            //6
            if(nowtime <= 21600000){
                return seed+2143;
            }
            //9
            if(nowtime <= 32400000){
                return seed+1799;
            }
            //11
            if(nowtime <= 39600000){
                return seed+411;
            }
            //13
            if(nowtime <= 46800000){
                return seed+1799;
            }
            //17
            if(nowtime <= 61200000){
                return seed+411;
            }
            //19
            if(nowtime <= 68400000){
                return seed+1799;
            }
            //21
            if(nowtime <= 75600000){
                return seed+411;
            }
            //24
            return seed+1799;
    }

	@Override
	public boolean clear(Date baseDate) throws Exception {
		
		return false;
	}
	
	public static void main(String[] args) {
//		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
//		SyncCompAccountTask task = new SyncCompAccountTask();
//		SyncCompAccountTask.SYNC_URL_IMPT="http://127.0.0.1:8080/sync/imptCompAccount";
//		
//		try {
//			task.exec(new Date());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		System.out.println(DateUtil.toString(new Date(1343804625000l), "yyyy-MM-dd HH:mm:ss"));
	}
	
}
