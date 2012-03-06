/**
 * 
 */
package com.zz91.mission.log4z;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;

/**
 * 分割日志信息，将日志按照appcode分割成多个不同的日志，提供给各个应用分析日志
 * 
 * @author mays (mays@zz91.net)
 * 
 */
public class SplitLogTask implements ZZTask {

	final static String LOG_PATH = "/usr/data/log4z/run";
	final static String LOG_ROOT_PATH = "/usr/data/log4z/";
	final static String LOG_DATE_FORMAT = "yyyy-MM-dd";

	@Override
	public boolean clear(Date baseDate) throws Exception {

		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		String targetDate = DateUtil.toString(DateUtil.getDateAfterDays(
				baseDate, -1), LOG_DATE_FORMAT);
		long gmtTarget = DateUtil.getTheDayZero(baseDate, -1);

		long today = DateUtil.getTheDayZero(0);
		String filePath = LOG_PATH + "." + targetDate;
		if (gmtTarget == today) {
			filePath = LOG_PATH;
		}

		BufferedReader br = new BufferedReader(new FileReader(filePath));
		Map<String, BufferedWriter> bwMap = new HashMap<String, BufferedWriter>();

		String line;
		BufferedWriter bw = null;
		String appLogFile = null;
		while ((line = br.readLine()) != null) {
			JSONObject jobj = JSONObject.fromObject(line);

			bw = bwMap.get(jobj.getString("appCode"));
			appLogFile = LOG_ROOT_PATH + jobj.getString("appCode") +"/run."
					+ targetDate;
			if (bw == null) {
				File file = new File(appLogFile);
				File fileFolder = file.getParentFile();
				if (!fileFolder.exists()) {
					fileFolder.mkdirs();
				}
				bw = new BufferedWriter(new FileWriter(file));
				bwMap.put(jobj.getString("appCode"), bw);
			}

			bw.write(jobj.toString());// 输出字符串
			bw.newLine();// 换行
			bw.flush();

		}
		br.close();
		
		for(String k:bwMap.keySet()){
			bwMap.get(k).close();
		}

		return true;
	}

	@Override
	public boolean init() throws Exception {
		return false;
	}

	public static void main(String[] args) throws ParseException, Exception {
		long start=System.currentTimeMillis();
		SplitLogTask split=new SplitLogTask();
		split.exec(DateUtil.getDate("2011-12-30", "yyyy-MM-dd"));
		long end=System.currentTimeMillis();
		System.out.println("共耗时："+(end-start));
	}
}
