package com.zz91.mission.analysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.pool.DBPoolFactory;

public class AnalysisProductsSpotTask implements ZZTask{
	private final static String DB = "ast";

	private static String LOG_FILE = "/usr/data/log4z/run.";
	private final static String LOG_DATE_FORMAT = "yyyy-MM-dd";

	private final static String OPERATOR = "operator";
	private final static String OPERATION_VALUE = "spot_hit";
	private final static String OPERATOR_VALUE = "zz91_spot";
	private final static String OPERATOR_VALUE_PRICE = "front_price";
	private final static String DATA="data";

	@Override
	public boolean clear(Date baseDate) throws Exception {
		String targetDate = DateUtil.toString(DateUtil.getDateAfterDays(baseDate, -1), LOG_DATE_FORMAT);
		return DBUtils.insertUpdate(DB,"delete from analysis_log where gmt_target='" + targetDate + "' and operation='"+OPERATION_VALUE+"' ");
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		String targetDate = DateUtil.toString(DateUtil.getDateAfterDays(baseDate, -1), LOG_DATE_FORMAT);

		Map<String, Integer> resultMap = new HashMap<String, Integer>();

		BufferedReader br = new BufferedReader(new FileReader(LOG_FILE+ targetDate));

		String line;
		Integer num = null;
		while ((line = br.readLine()) != null) {
			JSONObject jobj = JSONObject.fromObject(line);

			if (!OPERATOR_VALUE.equalsIgnoreCase(jobj.getString(OPERATOR))) {
				continue;
			}
			String opertionName = jobj.getString(DATA);
			if(opertionName!=null){
				opertionName = opertionName.toLowerCase();
			}
			num = resultMap.get(opertionName);
			if(num==null){
				resultMap.put(opertionName, 1);
			}else{
				resultMap.put(opertionName, ++num);
			}
		}
		br.close();
		for (String operatorName : resultMap.keySet()) {
			//两次插入
			if("front_price".equalsIgnoreCase(operatorName)){
				saveToDBForPrice(operatorName, resultMap.get(operatorName), targetDate);
			}else{
				saveToDB(operatorName, resultMap.get(operatorName), targetDate);
			}
		}
		return true;
	}
	
	private void saveToDBForPrice(String operatorName, Integer loginCount,
			String targetDate) {
		String sql = "insert into analysis_log(operator, operation, log_total, gmt_target, gmt_created, gmt_modified) values('"
			+ OPERATOR_VALUE_PRICE
			+ "','"
			+ OPERATION_VALUE
			+ "',"
			+ loginCount
			+ ", '" + targetDate + "',  now(),now())";
		DBUtils.insertUpdate(DB, sql);
	}

	private void saveToDB(String operationName, Integer loginCount,
			String targetDate) {
		String sql = "insert into analysis_log(operator, operation, log_total, gmt_target, gmt_created, gmt_modified) values('"
				+ operationName
				+ "','"
				+ OPERATION_VALUE
				+ "',"
				+ loginCount
				+ ", '" + targetDate + "',  now(),now())";
		DBUtils.insertUpdate(DB, sql);
	}

	@Override
	public boolean init() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	public static void main(String[] args) throws ParseException, Exception {
		DBPoolFactory.getInstance().init(
				"file:/usr/tools/config/db/db-zztask-jdbc.properties");
		System.out.println(DateUtil.getDate("2012-12-26", "yyyy-MM-dd").getTime());
		long start = System.currentTimeMillis();
		AnalysisProductsSpotTask analysis = new AnalysisProductsSpotTask();

		AnalysisProductsSpotTask.LOG_FILE = "/usr/data/log4z/zz91/run.";
		analysis.clear(DateUtil.getDate("2012-12-31", "yyyy-MM-dd"));
		analysis.exec(DateUtil.getDate("2012-12-31", "yyyy-MM-dd"));
		long end = System.currentTimeMillis();
		System.out.println("共耗时：" + (end - start));
		
	}

}
