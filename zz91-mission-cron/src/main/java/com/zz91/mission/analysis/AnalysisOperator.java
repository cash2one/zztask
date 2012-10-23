package com.zz91.mission.analysis;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.log.LogUtil;

/**
 * @author kongsj
 * @date 2012-9-5
 */
public class AnalysisOperator implements ZZTask {

	final static String WEB_PROP = "web.properties";
	final static String DATE_FORMAT = "yyyy-MM-dd";
	final static String DB = "ast";
	final static Integer SIZE = 100;

	@Override
	public boolean clear(Date baseDate) throws Exception {
		String from = DateUtil.toString(DateUtil.getDateAfterDays(baseDate, -1), "yyyy-MM-dd");
		String sql = "delete from analysis_product_type_code where gmt_created='"
				+ from + "'";
		DBUtils.insertUpdate(DB, sql);
		sql = "delete from analysis_operate where gmt_created='" + from + "'";
		return DBUtils.insertUpdate(DB, sql);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean exec(Date baseDate) throws Exception {
		LogUtil.getInstance().init(WEB_PROP);
		Date to = DateUtil.getDateAfterDays(baseDate, -1);
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("appCode", "zz91.admin");
		param.put("time", LogUtil.getInstance().mgCompare(">=",String.valueOf(to.getTime()), "<",String.valueOf(baseDate.getTime())));
		Map<String, Map<String, Integer>> omap = new HashMap<String, Map<String, Integer>>();
		Map<String, Map<String, Integer>> pmap = new HashMap<String, Map<String, Integer>>();
		try {
			for (int limit = 0;; limit++) {
				JSONObject res = LogUtil.getInstance().readMongo(param,limit * SIZE, SIZE);
				List<JSONObject> list = res.getJSONArray("records");
				if (list == null || list.size() == 0) {
					break;
				}
				for (int i = 0; i < list.size(); i++) {
					JSONObject jobj = (JSONObject) JSONSerializer.toJSON(list.get(i));
					String user = (String) jobj.get("operator");
					String todo = (String) jobj.get("operation");
					// 日常统计
					analysisRiChang(omap, user,todo);
					// 供求审核类别明细统计
					if (todo.contains("check_products")) {
						String data = jobj.getString("data");
						if("{".equals(data.substring(0,1))){
							JSONObject js  =  JSONObject.fromObject(data);
							String code = (String) js.get("productTypecode");
							analysisCheckInfo(pmap, user,todo,code);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 将统计的结果insert入数据表
		addToODB(omap,DateUtil.toString(to, DATE_FORMAT));
		addToPDB(pmap,DateUtil.toString(to, DATE_FORMAT));
		return true;
	}
	
	private void addToODB(Map<String,Map<String, Integer>>map,String date){
		for(String key:map.keySet()){
			Map<String,Integer> usermap = map.get(key);
			Integer bbsAdminPost = 0;
			Integer bbsClientPostFailure=0;
			Integer bbsClientPostSuccess=0;
			Integer bbsReplyFailure=0;
			Integer bbsReplySuccess=0;
			Integer checkComppriceFailure=0;
			Integer checkComppriceSuccess=0;
			Integer checkProductsFailure=0;
			Integer checkProductsSuccess=0;
			Integer postPrice=0;
			for(String opertion:usermap.keySet()){
				if("bbs_admin_post".equals(opertion)&&usermap.get(opertion)!=null){
					bbsAdminPost = usermap.get(opertion);
				}
				if("bbs_client_post_failure".equals(opertion)&&usermap.get(opertion)!=null){
					bbsClientPostFailure = usermap.get(opertion);
				}
				if("bbs_client_post_success".equals(opertion)&&usermap.get(opertion)!=null){
					bbsClientPostSuccess = usermap.get(opertion);
				}
				if("bbs_reply_failure".equals(opertion)&&usermap.get(opertion)!=null){
					bbsReplyFailure = usermap.get(opertion);
				}
				if("bbs_reply_success".equals(opertion)&&usermap.get(opertion)!=null){
					bbsReplySuccess = usermap.get(opertion);
				}
				if("check_compprice_failure".equals(opertion)&&usermap.get(opertion)!=null){
					checkComppriceFailure = usermap.get(opertion);
				}
				if("check_compprice_success".equals(opertion)&&usermap.get(opertion)!=null){
					checkComppriceSuccess = usermap.get(opertion);
				}
				if("check_products_failure".equals(opertion)&&usermap.get(opertion)!=null){
					checkProductsFailure = usermap.get(opertion);
				}
				if("check_products_success".equals(opertion)&&usermap.get(opertion)!=null){
					checkProductsSuccess = usermap.get(opertion);
				}
				if("post_price".equals(opertion)&&usermap.get(opertion)!=null){
					postPrice = usermap.get(opertion);
				}
			}
			String sql = "INSERT INTO `ast`.`analysis_operate` (`bbs_admin_post`, `bbs_client_post_failure`,`bbs_client_post_success`,"
			+"`bbs_reply_failure`,`bbs_reply_success`,`check_compprice_failure`,`check_compprice_success`,`check_products_failure`,"
			+"`check_products_success`,	`gmt_created`,`gmt_modified`,`operator`,`post_price`)"
			+"VALUES("+bbsAdminPost+","+bbsClientPostFailure+","+bbsClientPostSuccess+
			","+bbsReplyFailure+","+bbsReplySuccess+","+checkComppriceFailure+","+checkComppriceSuccess+","+checkProductsFailure+","
			+checkProductsSuccess+",'"+date+"','"+date+"','"+key+"',"+postPrice+")";
			// 插入表
			DBUtils.insertUpdate(DB, sql);
		}
	}
	private void addToPDB(Map<String,Map<String, Integer>>map,String date){
		for(String key:map.keySet()){
			Map<String,Integer> usermap = map.get(key);
			Integer boliN = 0;
			Integer boliY = 0;
			Integer dianzidianqiN = 0;
			Integer dianzidianqiY = 0;
			Integer ershoushebeiN = 0;
			Integer ershoushebeiY = 0;
			Integer fangzhipinN = 0;
			Integer fangzhipinY = 0;
			Integer fuwuN = 0;
			Integer fuwuY = 0;
			Integer jinshuN = 0;
			Integer jinshuY = 0;
			Integer xiangjiaoN = 0;
			Integer xiangjiaoY = 0;
			Integer qitafeiliaoN = 0;
			Integer qitafeiliaoY = 0;
			Integer suliaoN = 0;
			Integer suliaoY = 0;
			Integer zhiN = 0;
			Integer zhiY = 0;
			Integer pigeN = 0;
			Integer pigeY = 0;
			Integer luntaiN = 0;
			Integer luntaiY = 0;
			/*
			 * 1000	废金属
			 * 1001	废塑料
				1002	废橡胶
				
				1003	废纺织品
				
				1004	废纸
				1005	废电子电器
				1006	废玻璃
				
				1007	废旧二手设备
				
				1008	其他废料
				1009	服务
				1010	废皮革
				1011	废轮胎
			 * */
			for(String name:usermap.keySet()){
				if("1000check_products_failure".equals(name)&&usermap.get(name)!=null){
					jinshuN = usermap.get(name);
				}
				if("1000check_products_success".equals(name)&&usermap.get(name)!=null){
					jinshuY = usermap.get(name);
				}
				if("1001check_products_failure".equals(name)&&usermap.get(name)!=null){
					suliaoN = usermap.get(name);
				}
				if("1001check_products_success".equals(name)&&usermap.get(name)!=null){
					suliaoY = usermap.get(name);
				}
				if("1002check_products_failure".equals(name)&&usermap.get(name)!=null){
					xiangjiaoN = usermap.get(name);
				}
				if("1002check_products_success".equals(name)&&usermap.get(name)!=null){
					xiangjiaoY = usermap.get(name);
				}
				if("1003check_products_failure".equals(name)&&usermap.get(name)!=null){
					fangzhipinN = usermap.get(name);
				}
				if("1003check_products_success".equals(name)&&usermap.get(name)!=null){
					fangzhipinY = usermap.get(name);
				}
				if("1004check_products_failure".equals(name)&&usermap.get(name)!=null){
					zhiN = usermap.get(name);
				}
				if("1004check_products_success".equals(name)&&usermap.get(name)!=null){
					zhiY = usermap.get(name);
				}
				if("1005check_products_failure".equals(name)&&usermap.get(name)!=null){
					dianzidianqiN = usermap.get(name);
				}
				if("1005check_products_success".equals(name)&&usermap.get(name)!=null){
					dianzidianqiY = usermap.get(name);
				}
				if("1006check_products_failure".equals(name)&&usermap.get(name)!=null){
					boliN = usermap.get(name);
				}
				if("1006check_products_success".equals(name)&&usermap.get(name)!=null){
					boliY = usermap.get(name);
				}
				if("1007check_products_failure".equals(name)&&usermap.get(name)!=null){
					ershoushebeiN = usermap.get(name);
				}
				if("1007check_products_success".equals(name)&&usermap.get(name)!=null){
					ershoushebeiY = usermap.get(name);
				}
				if("1008check_products_failure".equals(name)&&usermap.get(name)!=null){
					qitafeiliaoN = usermap.get(name);
				}
				if("1008check_products_success".equals(name)&&usermap.get(name)!=null){
					qitafeiliaoY = usermap.get(name);
				}
				if("1009check_products_failure".equals(name)&&usermap.get(name)!=null){
					fuwuN = usermap.get(name);
				}
				if("1009check_products_success".equals(name)&&usermap.get(name)!=null){
					fuwuY = usermap.get(name);
				}
				if("1010check_products_failure".equals(name)&&usermap.get(name)!=null){
					pigeN = usermap.get(name);
				}
				if("1010check_products_success".equals(name)&&usermap.get(name)!=null){
					pigeY = usermap.get(name);
				}
				if("1011check_products_failure".equals(name)&&usermap.get(name)!=null){
					luntaiN = usermap.get(name);
				}
				if("1011check_products_success".equals(name)&&usermap.get(name)!=null){
					luntaiY = usermap.get(name);
				}
			}
			String sql = "INSERT INTO `ast`.`analysis_product_type_code`(`boli_N`,`boli_Y`,`dianzidianqi_N`,`dianzidianqi_Y`,`ershoushebei_N`,"
				+"`ershoushebei_Y`,`fangzhipin_Y`,`fangzhipin_N`,`fuwu_N`,`fuwu_Y`,`gmt_created`,`gmt_modified`,`jinshu_N`,"
				+"`jinshu_Y`,`xiangjiao_N`,`xiangjiao_Y`,`operator`,`qitafeiliao_N`,`qitafeiliao_Y`,`suliao_N`,`suliao_Y`,`zhi_N`,`zhi_Y`,`pige_N`,`pige_Y`,`luntai_N`,`luntai_Y`)"
				+"VALUES("+boliN+","+boliY+","+dianzidianqiN+","+dianzidianqiY+","+ershoushebeiN+","+ershoushebeiY
				+","+fangzhipinY+","+fangzhipinN+","+fuwuN+","+fuwuY+",'"+date+"','"+date+"',"+jinshuN+","
				+jinshuY+","+xiangjiaoN+","+xiangjiaoY+",'"+key+"',"+qitafeiliaoN+","+qitafeiliaoY+","+suliaoN+","+suliaoY+","+zhiN+","+zhiY+","+pigeN+","+pigeY+","+luntaiN+","+luntaiY+")";
			DBUtils.insertUpdate(DB, sql);
		}
	}

	private void analysisRiChang(Map<String, Map<String,Integer>> map, String user,String opertion) {
		Map<String,Integer> usermap =  map.get(user);
		do{
			if(usermap==null){
				usermap = new HashMap<String,Integer>();
				usermap.put(opertion, 1);
				map.put(user, usermap);
				break;
			}
			Integer count = usermap.get(opertion);
			if (count == null || count == 0) {
				count = 1;
			}else {
				count = count + 1;
			}
			usermap.put(opertion, count);
		}while(false);
	}
	
	private void analysisCheckInfo(Map<String, Map<String,Integer>> map, String user,String opertion,String code) {
		Map<String,Integer> usermap =  map.get(user);
		do{
			if(usermap==null){
				usermap = new HashMap<String,Integer>();
				usermap.put(code+opertion, 1);
				map.put(user, usermap);
				break;
			}
			Integer count = usermap.get(code+opertion);
			if (count == null || count == 0) {
				count = 1;
			}else {
				count = count + 1;
			}
			usermap.put(code+opertion, count);
			map.put(user, usermap);
		}while(false);
	}

	@Override
	public boolean init() throws Exception {
		return false;
	}

	public static void main(String[] args) throws Exception {
		LogUtil.getInstance().init(WEB_PROP);
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		Date baseDate = DateUtil.getDate("2012-10-23", DATE_FORMAT);
		AnalysisOperator obj = new AnalysisOperator();
		obj.clear(baseDate);
		obj.exec(baseDate);
	}
}
