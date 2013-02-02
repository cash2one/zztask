/**
 * Copyright 2011 ASTO.
 * All right reserved.
 * Created on 2011-3-16
 */
package com.zz91.mission.ep.sync;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;

import com.zz91.mission.domain.TradeSupply;
import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.http.HttpUtils;
import com.zz91.util.lang.StringUtils;
import com.zz91.util.mail.MailUtil;

/**
 * @author mays (mays@zz91.com)
 * 
 *         created on 2011-3-16
 */
public class SyncTradeSupplyTask implements ZZTask {

	final static String DB_EP="ep";
	
	final static String SYNC_TABLE="trade_supply";
	
	final static Integer LIMIT=50;
	
	final static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	
	final static String SYNC_URL_SEED="http://huanbaoadmin.zz91.com/sync/seed";
	final static String SYNC_URL_IMPT="http://huanbaoadmin.zz91.com/sync/imptTradeSupply";
	
//	final static String SYNC_URL_SEED="http://192.168.3.212:8080/sync/seed";
//	final static String SYNC_URL_IMPT="http://192.168.3.212:8080/sync/imptTradeSupply";
	
	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		
		//step 1: 从外网查找最大ID
		//step 2: 读取10 ep该最大ID后面的数据
		//step 3: 将数据push到外网（控制更新时间频率）
		//step 4: 更新最大ID
		//step 5: 控制频率（提交频率）
		//step 6: sync report
		
		String maxInfo=HttpUtils.getInstance().httpGet(SYNC_URL_SEED+"?table=trade_supply", HttpUtils.CHARSET_UTF8);
		
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
			
			List<TradeSupply> list=queryTradeSupply(unSyncMaxId);
			
			if(list==null || list.size()<=0){
				break;
			}
			
			for(TradeSupply supply:list){
				
				modifiedSeed = buildShowTime(modifiedSeed);
				
				supply.setGmtModified(new Date(modifiedSeed));
				
				unSyncMaxId = supply.getId();
			}
			
			//提交请求
			NameValuePair[] param=new NameValuePair[]{
				new NameValuePair("tradeSupply", JSONArray.fromObject(list).toString())
				};
			
			String result=null;
			try {
				result = HttpUtils.getInstance().httpPost(SYNC_URL_IMPT, param, HttpUtils.CHARSET_UTF8);
			} catch (HttpException e) {
			} catch (IOException e) {
			}
			
			if(StringUtils.isEmpty(result) || !result.startsWith("{")){
				for(TradeSupply supply:list){
					failure++;
					logUnsync(supply.getId(), baseDate.getTime());
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
		dataMap.put("reportTarget", "交易中心-供应信息");
		dataMap.put("successNum", success);
		dataMap.put("failureNum", failure);
		dataMap.put("maxShowTime", DateUtil.toString(new Date(modifiedSeed), DATE_FORMAT));
		dataMap.put("startDate",  DateUtil.toString(new Date(now), DATE_FORMAT));
		dataMap.put("endDate",  DateUtil.toString(end, DATE_FORMAT));
		dataMap.put("timeCost", (end.getTime()-now)/1000);
		
		MailUtil.getInstance().sendMail(
				"环保网数据同步报告 交易中心 供应信息", 
				"ep.sync.report@asto.mail", null, 
				null, "zz91", "ep-sync-report",
				dataMap, MailUtil.PRIORITY_TASK);
		
		return true;
	}

	@Override
	public boolean init() throws Exception {
		return false;
	}
	
	private List<TradeSupply> queryTradeSupply(Integer unSyncMaxId){
		
		final List<TradeSupply> list=new ArrayList<TradeSupply>();
		
		StringBuffer sb = new StringBuffer();
		sb.append("select ");
		sb.append(" `id`,`uid`,`cid`,`title`,`details`,`category_code`,`group_id`,`photo_cover`,`province_code`,`area_code`,");
		sb.append("`total_num`,`total_units`,`price_num`,`price_units`,`price_from`,`price_to`,`use_to`,`used_product`,`tags`,`tags_sys`,");
		sb.append("`details_query`,`property_query`,`message_count`,`view_count`,`favorite_count`,`plus_count`,`html_path`,");
		sb.append("`integrity`,`gmt_publish`,`gmt_refresh`,`valid_days`,`gmt_expired`,`del_status`,`pause_status`,");
		sb.append("`check_status`,`check_admin`,`check_refuse`,`gmt_check`,`gmt_created`,`gmt_modified`,`info_come_from`");
		sb.append(" from trade_supply where id>").append(unSyncMaxId);
		sb.append(" order by id asc limit ").append(LIMIT);
		
		DBUtils.select(DB_EP, sb.toString(), new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					TradeSupply supply = new TradeSupply();
					supply.setId(rs.getInt("id"));
					supply.setUid(rs.getInt("uid"));
					supply.setCid(rs.getInt("cid"));
					supply.setTitle(rs.getString("title"));
					supply.setDetails(rs.getString("details"));
					supply.setCategoryCode(rs.getString("category_code"));
					supply.setGroupId(rs.getInt("group_id"));
					supply.setPhotoCover(rs.getString("photo_cover"));
					supply.setProvinceCode(rs.getString("province_code"));
					supply.setAreaCode(rs.getString("area_code"));
					supply.setTotalNum(rs.getInt("total_num"));
					supply.setTotalUnits(rs.getString("total_units"));
					supply.setPriceNum(rs.getInt("price_num"));
					supply.setPriceUnits(rs.getString("price_units"));
					supply.setPriceFrom(rs.getInt("price_from"));
					supply.setPriceTo(rs.getInt("price_to"));
					supply.setUseTo(rs.getString("use_to"));
					supply.setUsedProduct(rs.getShort("used_product"));
					supply.setTags(rs.getString("tags"));
					supply.setTagsSys(rs.getString("tags_sys"));
					supply.setDetailsQuery(rs.getString("details_query"));
					supply.setPropertyQuery(rs.getString("property_query"));
					supply.setMessageCount(rs.getInt("message_count"));
					supply.setViewCount(rs.getInt("view_count"));
					supply.setFavoriteCount(rs.getInt("favorite_count"));
					supply.setPlusCount(rs.getInt("plus_count"));
					supply.setHtmlPath(rs.getString("html_path"));
					supply.setIntegrity(rs.getShort("integrity"));
					supply.setGmtPublish(getJavaDate(rs.getDate("gmt_publish")));
					supply.setGmtRefresh(getJavaDate(rs.getDate("gmt_refresh")));
					supply.setValidDays(rs.getShort("valid_days"));
					supply.setGmtExpired(getJavaDate(rs.getDate("gmt_expired")));
					supply.setDelStatus(rs.getShort("del_status"));
					supply.setPauseStatus(rs.getShort("pause_status"));
					supply.setCheckStatus(rs.getShort("check_status"));
					supply.setCheckAdmin(rs.getString("check_admin"));
					supply.setCheckRefuse(rs.getString("check_refuse"));
					supply.setGmtCheck(getJavaDate(rs.getDate("gmt_check")));
					supply.setGmtCreated(getJavaDate(rs.getDate("gmt_created")));
					supply.setGmtModified(getJavaDate(rs.getDate("gmt_modified")));
					supply.setInfoComeFrom(rs.getInt("info_come_from"));
					list.add(supply);
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
	
	public static void main(String[] args) {
//		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc2.properties");
//		SyncTradeSupplyTask task = new SyncTradeSupplyTask();
//		
//		try {
//			task.exec(new Date());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		System.out.println(DateUtil.toString(new Date(1343804625000l), "yyyy-MM-dd HH:mm:ss"));
	}
}
