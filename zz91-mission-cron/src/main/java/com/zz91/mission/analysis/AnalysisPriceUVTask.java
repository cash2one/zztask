package com.zz91.mission.analysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.lang.StringUtils;

/**
 * 统计分析报价详细页面访问UV
 * 日志格式{"appCode":"zz91","data":"id:441239","ip":"119.2.12.148","operation":"click_count","operator":"zz91_price","time":1361773853374}
 * 
 * 任务逻辑
 * 1、分析日志信息，逐行读取
 * 2、判断operation是否为click_count(页面UV访问)
 * 3、判断operator是否为zz91_price(zz91报价资讯统计)
 * 4、获取ip，并统计ip总数
 * 5、根据id，update入price表的ip字段
 * 
 * @author kongsj
 * @date 2013-02-25
 */
public class AnalysisPriceUVTask implements ZZTask {

	private final static String DB = "ast";

	private static String LOG_FILE = "/usr/data/log4z/zz91/run.";
	private final static String LOG_DATE_FORMAT = "yyyy-MM-dd";

	private final static String OPERATION = "operation";
	private final static String OPERATION_VALUE = "click_count";
	private final static String OPERATOR = "operator";
	private final static String OPERATOR_VALUE = "zz91_price";
	private final static String IP = "ip";
	private final static String DATA = "data";

	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		String targetDate = DateUtil.toString(DateUtil.getDateAfterDays(baseDate, -1), LOG_DATE_FORMAT);

		Map<Integer, Set<String>> resultMap = new HashMap<Integer, Set<String>>();
		Set<String> ipSet = new HashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader(LOG_FILE+ targetDate));

		String line;
		while ((line = br.readLine()) != null) {
			JSONObject jobj = JSONObject.fromObject(line);
			do{
				// 是否 点击量统计 UV
				if(!OPERATION_VALUE.equalsIgnoreCase(jobj.getString(OPERATION))){
					break;
				}
				// 是否 price资讯模块统计 
				if (!OPERATOR_VALUE.equalsIgnoreCase(jobj.getString(OPERATOR))) {
					break;
				}
				// 获取ip
				String ip = jobj.getString(IP);
				if(StringUtils.isEmpty(ip)){
					break;
				}
				String data = jobj.getString(DATA);
				JSONObject jb = JSONObject.fromObject("{"+data+"}");
				String id = jb.get("id").toString();
				if(!StringUtils.isNumber(id)){
					break;
				}
				ipSet = resultMap.get(jb.getInt("id"));
				if(ipSet==null){
					ipSet = new HashSet<String>();
				}
				ipSet.add(ip);
				resultMap.put(jb.getInt("id"), ipSet);
			}while(false);
		}
		br.close();
		for (Integer id : resultMap.keySet()) {
			saveToDB(id, resultMap.get(id));
		}
		return true;
	}
	
	private void saveToDB(Integer id,Set<String>ipSet) {
		String sql = "update price set gmt_modified = now(),ip = ip + "+ipSet.size() + " where id = " + id;
		DBUtils.insertUpdate(DB, sql);
	}

	@Override
	public boolean init() throws Exception {
		return false;
	}

	public static void main(String[] args) throws ParseException, Exception {
		DBPoolFactory.getInstance().init(
				"file:/usr/tools/config/db/db-zztask-jdbc.properties");

		long start = System.currentTimeMillis();
		AnalysisPriceUVTask analysis = new AnalysisPriceUVTask();

		AnalysisPriceUVTask.LOG_FILE = "/usr/data/log4z/zz91/run.";
		analysis.clear(DateUtil.getDate("2013-05-15", "yyyy-MM-dd"));
		analysis.exec(DateUtil.getDate("2013-05-15", "yyyy-MM-dd"));
		long end = System.currentTimeMillis();
		System.out.println("共耗时：" + (end - start));
	}

}
