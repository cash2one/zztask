/**
 * Copyright 2011 ASTO.
 * All right reserved.
 * Created on 2011-7-21
 */
package com.zz91.mission.ads;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import net.sf.json.JSONObject;

import com.zz91.mission.domain.ads.AnalysisAdHit;
import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;

/**
 * @author mays (mays@zz91.com)
 *
 * created on 2011-7-21
 */
public class AnalysisAdHitTask implements ZZTask {

	final static String LOG_PATH="/usr/data/ads/adslog";
	final static String LOG_DATE_FORMAT="yyyy-MM-dd";
	final static String DB="zzads";
	
	Logger LOG=Logger.getLogger(AnalysisAdHitTask.class);

	@Override
	public boolean clear(Date baseDate) throws Exception {
		long targetDate = DateUtil.getTheDayZero(baseDate, -1);
		LOG.info("begin clear analysis ad hit data on target Date:"+targetDate);
		DBUtils.insertUpdate(DB, "delete from analysis_ad_hit where gmt_target="+targetDate);
		return false;
	}
	
	
	@Override
	public boolean exec(Date baseDate) throws Exception {
		boolean result=false;
		LOG.info("prepare analysis ad hit...");
		do {
			String targetDate=DateUtil.toString(DateUtil.getDateAfterDays(baseDate, -1),LOG_DATE_FORMAT);
			long gmtTarget = DateUtil.getTheDayZero(baseDate, -1);
			
			long today=DateUtil.getTheDayZero(0);
			String filePath=LOG_PATH+"."+targetDate;
			if(gmtTarget==today){
				filePath=LOG_PATH;
			}
			LOG.info("analysis log file:"+filePath);
			BufferedReader br=new BufferedReader(new FileReader(filePath));
			Map<Integer, AnalysisAdHit> analysisResultMap= new HashMap<Integer, AnalysisAdHit>();
			String line;
			while ((line=br.readLine())!=null) {
				JSONObject jobj=JSONObject.fromObject(line);
				
				AnalysisAdHit hit=analysisResultMap.get(jobj.getInt("adId"));
				if(hit==null){
					hit=new AnalysisAdHit(jobj.getInt("adId"), "", jobj.getInt("positionId"), 0, 0, 0);
					analysisResultMap.put(jobj.getInt("adId"), hit);
				}
				int hittype=jobj.getInt("hitType");
				if(hittype==0){
					hit.setNumShow(hit.getNumShow()+1);
				}else{
					hit.setNumHit(hit.getNumHit()+1);
					int clickType = jobj.getInt("clickType");
					if(clickType==0){
						hit.setNumHitFirst(hit.getNumHitFirst()+1);
					}
				}
			}
			br.close();
			
			LOG.info("writing analysis result...");
			for(Integer adid:analysisResultMap.keySet()){
				AnalysisAdHit hit=analysisResultMap.get(adid);
				hit.setAdTitle(queryAdTitle(adid));
				String sql="insert into analysis_ad_hit(ad_id, ad_title, ad_position_id, num_show, num_hit, num_hit_first,num_hit_per_hour, gmt_target, gmt_created, gmt_modified) ";
				sql=sql+"values("+adid+",'"+hit.getAdTitle()+"',"+hit.getAdPositionId()+","+hit.getNumShow()+","+hit.getNumHit()+","+hit.getNumHitFirst()+",'',"+gmtTarget+", now(), now())";
				DBUtils.insertUpdate(DB, sql);
			}
			
			result=true;
		} while (false);
		
		return result;
	}

	@Override
	public boolean init() throws Exception {
		
		return false;
	}
	
	private String queryAdTitle(Integer adid){
		
		final List<String> titleList=new ArrayList<String>();
		DBUtils.select(DB, "select ad_title from ad where id="+adid, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					titleList.add(rs.getString(1));
				}
			}
		});
		
		if(titleList.size()>0){
			return titleList.get(0);
		}else{
			return "";
		}
	}
	
	public static void main(String[] args) {
		
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		
		AnalysisAdHitTask task=new AnalysisAdHitTask();
		try {
			task.clear(DateUtil.getDate("2011-12-26 12:52:30", "yyyy-MM-dd HH:mm:ss"));
			boolean r = task.exec(DateUtil.getDate("2011-12-26 12:52:30", "yyyy-MM-dd HH:mm:ss"));
			System.out.println(r);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		File log = new File(LOG_PATH);
//		File file = new File(LOG_PATH+".2011-7-23");
//		if(!file.exists()){
//			if(log.exists()){
//				log.renameTo(file);
//			}
//		}
		
	}
}
