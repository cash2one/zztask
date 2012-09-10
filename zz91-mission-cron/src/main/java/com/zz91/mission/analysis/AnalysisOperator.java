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
			Integer fangzhipinPigeN = 0;
			Integer fangzhipinPigeY = 0;
			Integer fuwuN = 0;
			Integer fuwuY = 0;
			Integer jinshuN = 0;
			Integer jinshuY = 0;
			Integer luntaiXiangjiaoN = 0;
			Integer luntaiXiangjiaoY = 0;
			Integer qitafeiliaoN = 0;
			Integer qitafeiliaoY = 0;
			Integer suliaoN = 0;
			Integer suliaoY = 0;
			Integer zhiN = 0;
			Integer zhiY = 0;
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
					luntaiXiangjiaoN = usermap.get(name);
				}
				if("1002check_products_success".equals(name)&&usermap.get(name)!=null){
					luntaiXiangjiaoY = usermap.get(name);
				}
				if("1003check_products_failure".equals(name)&&usermap.get(name)!=null){
					fangzhipinPigeN = usermap.get(name);
				}
				if("1003check_products_success".equals(name)&&usermap.get(name)!=null){
					fangzhipinPigeY = usermap.get(name);
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
				String sql = "INSERT INTO `ast`.`analysis_product_type_code`(`boli_N`,`boli_Y`,`dianzidianqi_N`,`dianzidianqi_Y`,`ershoushebei_N`,"
					+"`ershoushebei_Y`,`fangzhipin_pige_Y`,`fangzhipin_pige_N`,`fuwu_N`,`fuwu_Y`,`gmt_created`,`gmt_modified`,`jinshu_N`,"
					+"`jinshu_Y`,`luntai_xiangjiao_N`,`luntai_xiangjiao_Y`,`operator`,`qitafeiliao_N`,`qitafeiliao_Y`,`suliao_N`,`suliao_Y`,`zhi_N`,`zhi_Y`)"
					+"VALUES("+boliN+","+boliY+","+dianzidianqiN+","+dianzidianqiY+","+ershoushebeiN+","+ershoushebeiY
					+","+fangzhipinPigeY+","+fangzhipinPigeN+","+fuwuN+","+fuwuY+",'"+date+"','"+date+"',"+jinshuN+","
					+jinshuY+","+luntaiXiangjiaoN+","+luntaiXiangjiaoY+",'"+key+"',"+qitafeiliaoN+","+qitafeiliaoY+","+suliaoN+","+suliaoY+","+zhiN+","+zhiY+")";
				DBUtils.insertUpdate(DB, sql);
			}
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
		}while(false);
	}

	@Override
	public boolean init() throws Exception {
		return false;
	}

	public static void main(String[] args) throws Exception {
		LogUtil.getInstance().init(WEB_PROP);
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		Date baseDate = DateUtil.getDate("2012-09-11", DATE_FORMAT);
		AnalysisOperator obj = new AnalysisOperator();
		obj.clear(baseDate);
		obj.exec(baseDate);
	}
}
