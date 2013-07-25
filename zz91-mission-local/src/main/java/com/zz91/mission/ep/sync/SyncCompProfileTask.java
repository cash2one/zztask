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

import com.zz91.mission.domain.ep.CompProfile;
import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.http.HttpUtils;
import com.zz91.util.lang.StringUtils;
import com.zz91.util.mail.MailUtil;

public class SyncCompProfileTask implements ZZTask {
	
	final static String DB_EP="ep";
	
	final static String SYNC_TABLE="comp_profile";
	
	final static Integer LIMIT=50;
	
	final static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	
	final  static String SYNC_URL_SEED="http://huanbaoadmin.zz91.com/sync/seed";
	final static String SYNC_URL_IMPT="http://huanbaoadmin.zz91.com/sync/imptCompProfile";
	
	@Override
	public boolean init() throws Exception {
		
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		
				//取得最大的id号 和最大的更新时间
				String maxInfo=HttpUtils.getInstance().httpGet(SYNC_URL_SEED+"?table=comp_profile", HttpUtils.CHARSET_UTF8);
				//String maxInfo="{maxId:9482983,maxGmtModified:1230}";
				long now = new Date().getTime();
				
				JSONObject jobj=JSONObject.fromObject(maxInfo);
				//最大的时间
				Integer unSyncMaxId=jobj.getInt("maxId");
				
				//最大更新时间
				long modifiedSeed=jobj.getLong("maxGmtModified");
				
				if(modifiedSeed < now){
					modifiedSeed=now;
				}
				
				//成功次数
				Integer success=0;
				//失败次数
				Integer failure=0;
				
				do{
					
					
					List<CompProfile> list=queryCompProfile(unSyncMaxId);
					
					if(list==null || list.size()<=0){
						break;
					}
					
					for(CompProfile comp:list){
						
						modifiedSeed = buildShowTime(modifiedSeed);
						
						comp.setGmtModified(new Date(modifiedSeed));
						
						unSyncMaxId = comp.getId();
					}
					
					//提交请求
					NameValuePair[] param=new NameValuePair[]{
						new NameValuePair("compProfile", JSONArray.fromObject(list).toString())
						};
					
					String result=null;
					try {
						result = HttpUtils.getInstance().httpPost(SYNC_URL_IMPT, param, HttpUtils.CHARSET_UTF8);
					} catch (HttpException e) {
					} catch (IOException e) {
					}
					
					if(StringUtils.isEmpty(result) || !result.startsWith("{")){
						for(CompProfile comp:list){
							failure++;
							logUnsync(comp.getId(), baseDate.getTime());
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
				dataMap.put("reportTarget", "公司信息");
				dataMap.put("successNum", success);
				dataMap.put("failureNum", failure);
				dataMap.put("maxShowTime", DateUtil.toString(new Date(modifiedSeed), DATE_FORMAT));
				dataMap.put("startDate",  DateUtil.toString(new Date(now), DATE_FORMAT));
				dataMap.put("endDate",  DateUtil.toString(end, DATE_FORMAT));
				dataMap.put("timeCost", (end.getTime()-now)/1000);
				
				MailUtil.getInstance().sendMail(
						"环保网数据同步报告 公司信息", 
						"ep.sync.report@asto.mail", null, 
						null, "zz91", "ep-sync-report",
						dataMap, MailUtil.PRIORITY_TASK);
				
		
		return true;
	}

	
	private List<CompProfile> queryCompProfile(Integer maxId){
		final List<CompProfile> list = new  ArrayList<CompProfile>();
		
		StringBuffer sql = new StringBuffer();
			sql.append("select ");
			sql.append("id,name,details,industry_code,main_buy,main_product_buy,main_supply,main_product_supply,")
				.append("member_code,member_code_block,register_code,business_code,area_code,province_code,")
				.append("legal,funds,main_brand,address,address_zip,domain,domain_two,message_count,view_count,")
				.append("tags,details_query,gmt_created,gmt_modified,del_status,process_method,process,")
				.append("employee_num,developer_num,plant_area,main_market,main_customer,")
				.append("month_output,year_turnover,year_exports,quality_control,register_area,enterprise_type,")
				.append("send_time,receive_time,oper_name");
			sql.append(" from comp_profile where id>").append(maxId);
			sql.append(" order by id asc limit ").append(LIMIT);
			//System.out.println(sql.toString());
		DBUtils.select(DB_EP, sql.toString(), new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				CompProfile comp = null;
				while(rs.next()){
					comp = new CompProfile();
					comp.setId(rs.getInt("id"));
					comp.setName(rs.getString("name"));
					comp.setDetails(rs.getString("details"));
					comp.setIndustryCode(rs.getString("industry_code"));
					comp.setMainBuy(rs.getShort("main_buy"));
					comp.setMainProductBuy(rs.getString("main_product_buy"));
					comp.setMainSupply(rs.getShort("main_supply"));
					comp.setMainProductSupply(rs.getString("main_product_supply"));
					comp.setMemberCode(rs.getString("member_code"));
					comp.setMemberCodeBlock(rs.getString("member_code_block"));
					comp.setRegisterCode(rs.getString("register_code"));
					comp.setBusinessCode(rs.getString("business_code"));
					comp.setAreaCode(rs.getString("area_code"));
					comp.setProvinceCode(rs.getString("province_code"));
					comp.setLegal(rs.getString("legal"));
					comp.setFunds(rs.getString("funds"));
					comp.setMainBrand(rs.getString("main_brand"));
					comp.setAddress(rs.getString("address"));
				    comp.setAddressZip(rs.getString("address_zip"));
				    comp.setDomain(rs.getString("domain"));
				    comp.setDomainTwo(rs.getString("domain_two"));
				    comp.setMessageCount(rs.getInt("message_count"));
				    comp.setViewCount(rs.getInt("view_count"));
				    comp.setTags(rs.getString("tags"));
				    comp.setDetailsQuery(rs.getString("details_query"));
				    comp.setGmtCreated(getJavaDate(rs.getDate("gmt_created")));
				    comp.setGmtModified(getJavaDate(rs.getDate("gmt_modified")));
				    comp.setDelStatus(rs.getInt("del_status"));
				    comp.setProcessMethod(rs.getString("process_method"));
				    comp.setProcess(rs.getString("process"));
				    comp.setEmployeeNum(rs.getString("employee_num"));
				    comp.setDeveloperNum(rs.getString("developer_num"));
				    comp.setPlantArea(rs.getString("plant_area"));
				    comp.setMainMarket(rs.getString("main_market"));
				    comp.setMainCustomer(rs.getString("main_customer"));
				    comp.setMonthOutput(rs.getString("month_output"));
				    comp.setYearTurnover(rs.getString("year_turnover"));
				    comp.setYearExports(rs.getString("year_exports"));
				    comp.setQualityControl(rs.getString("quality_control"));
				    comp.setRegisterArea(rs.getString("register_area"));
				    comp.setEnterpriseType(rs.getString("enterprise_type"));
				    comp.setSendTime(getJavaDate(rs.getDate("send_time")));
				    comp.setReceiveTime(getJavaDate(rs.getDate("receive_time")));
				    comp.setOperName(rs.getString("oper_name"));
					list.add(comp);
				}
				
			}
		});
		return list;
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
	
	@Override
	public boolean clear(Date baseDate) throws Exception {
		
		return false;
	}
	
	public static void main(String[] args) {
//		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc2.properties");
//		SyncCompProfileTask c = new SyncCompProfileTask();
//		SyncCompProfileTask.SYNC_URL_IMPT = "http://127.0.0.1:8080/sync/imptCompProfile";
//		try {
//			c.exec(new Date());
//		} catch (Exception e) {
//			
//			e.printStackTrace();
//		}
	}
}
