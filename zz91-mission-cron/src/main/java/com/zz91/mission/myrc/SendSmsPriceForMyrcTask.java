package com.zz91.mission.myrc;

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
import com.zz91.util.mail.MailUtil;

public class SendSmsPriceForMyrcTask implements ZZTask{
	
	final static String DATE_FORMAT_ZH_CN = "yyyy年MM月dd日";
	private final static String DB_ZZ91 = "ast";
	private final static String DB_REBORN = "reborn";
	/**
	 * 1,搜索需要发送邮件的客户
	 * 2,根据公司id获取邮箱
	 * 3,组装报价内容发送邮件
	 */

	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}
	@Override
	public boolean exec(Date baseDate) throws Exception {
		String from = DateUtil.toString(
				DateUtil.getDateAfterDays(baseDate, -1), "yyyy-MM-dd");
		String to = DateUtil.toString(baseDate, "yyyy-MM-dd");
		//搜索订阅的类别表总数进行分批搜索公司id
		String sql="select count(*) from subscribe_sms_price ";
		final Integer[] count=new Integer[1];
		count[0]=0;
		DBUtils.select(DB_ZZ91, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					count[0]=rs.getInt(1);
				}
			}
		});
		//Integer total=count[0]/100;	
	//	for(Integer i=1;i<=total;i++){
			//搜索订阅的类别公司id
			//String sqlId="select company_id from subscribe_sms_price limit "+100*(i-1) +"," + 100;
		//搜索客户订阅的类别表得出公司id
		final Map<String, Object> map=new HashMap<String, Object>();
		String sqlId="select distinct company_id from subscribe_sms_price";
		DBUtils.select(DB_ZZ91, sqlId, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					map.put(String.valueOf(rs.getInt(1)), 1);
				}
			}
		});	
		//}
		//搜索是否发送邮件的表得出不发送邮件的公司id
		String sql1="select company_id from config_notify where status=1 and notify_code='20041000'";
		final Map<String, Object> map1=new HashMap<String, Object>();
		DBUtils.select(DB_ZZ91, sql1, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					map1.put(String.valueOf(rs.getInt(1)), 2);
				}
			}
		});
		for(Object obj:map1.keySet()){
			map.remove(obj);
		}
		for(Object obj :map.keySet()){
			Integer companyId=Integer.parseInt(String.valueOf(obj));
			//根据公司id搜索这个公司订阅的类目，from和to需要在搜索报价的时候传进去，所以带上两个参数
			selectSubscribeById(companyId,from,to);
		}
		return true;
	}
	
	private void selectSubscribeById(Integer companyId,String from,String to) throws Exception {
		String cont="";
		final List<Map<String, Object>> list=new ArrayList<Map<String,Object>>();
		//根据id搜索这个公司订阅的类别
		String sql="select area_code,category_code from subscribe_sms_price where company_id="+companyId+"";
		DBUtils.select(DB_ZZ91, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("areaCode", rs.getObject(1));
					map.put("categoryCode", rs.getString(2));
					list.add(map);
				}
			}
		});
		do {
			//得到的类别可能不止一个所以组装个list
			for(Map<String, Object> obj:list){
				Integer areaCode=(Integer)obj.get("areaCode");
				String categoryCode=(String)obj.get("categoryCode");
				//根据类别，地区和时间搜索报价
				String str=selectSmsPrice(areaCode,categoryCode,from,to);
				cont+=str;
			}
			if(StringUtils.isEmpty(cont)){
				break;
			}else {
				//如果组装的有报价内容才发送，每个客户订阅的短信报价内容组装成list来发送
				sendEmailForMyrc(companyId,cont);
			}
		} while (false);
	}
	private String selectSmsPrice(Integer areaCode, String categoryCode,String from,String to) {
		String sql="";String sql1="";String str="";
		//考虑到有的没有地区有的有地区所以根据地区的有或无来搜索，因为要对比报价得出涨还是跌所以第一次搜索最新的一条，第二次搜索最新的第二条
		if(areaCode==null || areaCode==0){
			sql="select sp.max_price, sp.min_price,sc.name as category_name from sms_price sp inner join sms_category sc on sc.code=sp.category_code where sp.gmt_post>='"+from+"'  and '"+to+"'>sp.gmt_post and sp.min_price is not null and sp.max_price is not null and category_code="+categoryCode+" order by sp.gmt_post desc limit 0,1";
			sql1="select sp.max_price, sp.min_price,sc.name as category_name from sms_price sp inner join sms_category sc on sc.code=sp.category_code where sp.min_price is not null and sp.max_price is not null and category_code="+categoryCode+" order by sp.gmt_post desc limit 1,1";
		}else {
			sql="select sp.max_price, sp.min_price,sc.name as category_name from sms_price sp inner join sms_category sc on sc.code=sp.category_code where sp.gmt_post>='"+from+"'  and '"+to+"'>sp.gmt_post and sp.min_price is not null and sp.max_price is not null and category_code="+categoryCode+" and area_node_id="+areaCode+" order by sp.gmt_post desc limit 0,1";
			sql1="select sp.max_price, sp.min_price,sc.name as category_name from sms_price sp inner join sms_category sc on sc.code=sp.category_code where sp.min_price is not null and sp.max_price is not null and category_code="+categoryCode+" and area_node_id="+areaCode+" order by sp.gmt_post desc limit 1,1";
		}
		final Map<String, Object> map=new HashMap<String, Object>();
		final Map<String, Object> map1=new HashMap<String, Object>();
		DBUtils.select(DB_REBORN, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					map.put("maxPrice", rs.getDouble(1));
					map.put("minPrice", rs.getDouble(2));
					map.put("categoryName", rs.getString(3));
				}
			}
		});
		DBUtils.select(DB_REBORN, sql1, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					map1.put("maxPrice", rs.getDouble(1));
					map1.put("minPrice", rs.getDouble(2));
					map1.put("categoryName", rs.getString(3));
				}
			}
		});
		do{
			//如果获取的两条报价内容，最大价格或最小价格为null的时候跳出
			if(map.get("maxPrice")==null||map.get("minPrice")==null||map1.get("maxPrice")==null||map1.get("minPrice")==null){
				break;
			}
			String areaName="";
			if(map.get("categoryName")==null||"".equals(map.get("categoryName"))){
				areaName="";
			}else {
				areaName=(String)map.get("categoryName");
			}
			Double maxPrice=(Double)map.get("maxPrice");
			Double minPrice=(Double)map.get("minPrice");
			Double maxPr2=(Double)map1.get("maxPrice");
			Double minPr2=(Double)map1.get("minPrice");
			Double result=(maxPrice+minPrice)/2;
			Double result2=(maxPr2+minPr2)/2;
			if(areaCode==null){
				areaCode=0;
			}
			//组装报价内容地区+类别名称+报价
			if(result>result2){
				str=INDEX_MAP.get(areaCode)+areaName+":今日报价"+minPrice+"-"+maxPrice+"涨"+(result-result2)+";";
			}
			if(result==result2){
				str=INDEX_MAP.get(areaCode)+areaName+":今日报价"+minPrice+"-"+maxPrice+"持平;";
			}
			if(result<result2){
				str=INDEX_MAP.get(areaCode)+areaName+":今日报价"+minPrice+"-"+maxPrice+"跌"+(result2-result)+";";
			}
		}while(false);
		return str;
	}
	
	private void sendEmailForMyrc(Integer companyId,String content) {
		//根据公司id搜索邮箱和备用邮箱和是否启用备用邮箱
		String sql="select is_use_back_email,back_email,email from company_account where company_id="+companyId+"";
		final Map<String, Object> map=new HashMap<String, Object>();
		final Map<String, Object> map1=new HashMap<String, Object>();
		DBUtils.select(DB_ZZ91, sql, new IReadDataHandler(){
			@Override
			public void handleRead(ResultSet rs)
					throws SQLException {
				while(rs.next()){
					map.put("isBackEmail", rs.getString(1));
					map.put("backEmail", rs.getString(2));
					map.put("email", rs.getString(3));
				}
			}
		});
		String isBackEmail=(String) map.get("isBackEmail");
		String backEmail=(String) map.get("backEmail");
		String email=(String)map.get("email");
		map1.put("list", content);
		//如果客户选中备用邮箱接收，就发送到备用邮箱，相反就发送到注册邮箱
		if("1".equals(isBackEmail)){
			MailUtil.getInstance().sendMail("ZZ91生意管家报价定制", 
					backEmail, null,
					null, "zz91", "zz91_myrc", 
					map1, MailUtil.PRIORITY_HEIGHT);
		}else {
			MailUtil.getInstance().sendMail("ZZ91生意管家报价定制", 
					email, null,
					null, "zz91", "zz91_myrc", 
					map1, MailUtil.PRIORITY_HEIGHT);
		}
	}
	public static Map<Integer, String> INDEX_MAP=new HashMap<Integer, String>();
	static{
		INDEX_MAP.put(1, "江浙沪");INDEX_MAP.put(5, "湖南汨罗");
		INDEX_MAP.put(2, "广东南海");INDEX_MAP.put(6, "河南长葛");
		INDEX_MAP.put(3, "天津");INDEX_MAP.put(7, "广东清远");
		INDEX_MAP.put(4, "山东临沂");INDEX_MAP.put(9, "上海");
		INDEX_MAP.put(11, "广东");INDEX_MAP.put(12, "浙江");
		INDEX_MAP.put(13, "江苏");INDEX_MAP.put(14, "山东");
		INDEX_MAP.put(0, "全国各地");
	}
	@Override
	public boolean init() throws Exception {
		return false;
	}
	
	public static void main(String[] args) throws Exception {
		DBPoolFactory.getInstance().init(
				"file:/usr/tools/config/db/db-zztask-jdbc.properties");
		MailUtil.getInstance().init("file:/root/web.properties");
		SendSmsPriceForMyrcTask obj = new SendSmsPriceForMyrcTask();
		Date date = DateUtil.getDate("2012-11-21", "yyyy-MM-dd");
		obj.exec(date);
	}

}

