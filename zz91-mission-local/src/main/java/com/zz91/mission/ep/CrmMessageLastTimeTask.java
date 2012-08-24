package com.zz91.mission.ep;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.zz91.task.common.ZZTask;
import com.zz91.util.db.DBUtils;
import com.zz91.util.http.HttpUtils;
/**
 * @author 黄怀清
 * 2012-08-14
 * 统计询盘信息
 */
public class CrmMessageLastTimeTask implements ZZTask {

	Logger LOG = Logger.getLogger(CrmMessageLastTimeTask.class);

	final static String API_HOST="http://huanbaoadmin.zz91.com:8081/ep-admin/api";
	
	final static String CRMDB="crm";
	
	@Override
	public boolean clear(Date baseDate) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		boolean result = false;
		String responseText = HttpUtils.getInstance().httpGet(API_HOST+"/compfileMessageCount.htm", HttpUtils.CHARSET_UTF8);
		JSONObject totalObject = JSONObject.fromObject(responseText);
		Integer count = Integer.valueOf(totalObject.getString("total"));
		
		do {
			Integer start=0;
			Integer limit=10;
			Integer n=((count-1)/limit)+1;
			
			Map<String, String> sendMap = new HashMap<String, String>();
			Map<String, String> reMap = new HashMap<String, String>();
			
			for (int i = 0; i < n; i++) {
				
				String resText = HttpUtils.getInstance().httpGet(API_HOST+"/compfileMessageTime.htm?&start="+start+"&size="+limit, HttpUtils.CHARSET_UTF8);
				JSONArray jsonarray=JSONArray.fromObject(resText);

				for (Iterator iter = jsonarray.iterator(); iter.hasNext();) {
					JSONObject object = (JSONObject) iter.next();
					
					reMap.put(object.getString("targetCid"), object.getString("gmtCreated"));
					
					sendMap.put(object.getString("cid"), object.getString("gmtCreated"));
				}
								
				start+=limit;
			}
			
			//更新操作
			updateTime(sendMap, reMap);
			result=true;
		} while (false);
		
		
		
		return result;
	}

	
	public void updateTime(Map<String, String> sendMap,Map<String, String> reMap){
		Set<String> reKeys=sendMap.keySet();
		for(String key: reKeys){
			//接收询盘
			String reql="update crm_company set receive_time=date_format('"
				+ reMap.get(key)+"','%Y-%m-%d %H:%i:%S') where id=" + key;
			if(!DBUtils.insertUpdate(CRMDB, reql)){
				LOG.info("更新接收询盘出错,id:"+key);
			}
		}
		
		Set<String> sendKeys = sendMap.keySet();
		for(String key:sendKeys){
			//发送询盘
			String sendsql="update crm_company set send_time=date_format('"
				+ sendMap.get(key)+"','%Y-%m-%d %H:%i:%S') where id=" + key;			
			if(!DBUtils.insertUpdate(CRMDB, sendsql)){
				LOG.info("更新接收询盘出错,cid:"+key);
			}
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
//			CrmMessageLastTimeTask et=new CrmMessageLastTimeTask();
//			et.exec(null);
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//	}

}
