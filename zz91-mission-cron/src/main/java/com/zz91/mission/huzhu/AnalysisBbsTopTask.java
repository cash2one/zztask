package com.zz91.mission.huzhu;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zz91.mission.domain.huzhu.AnalysisBbsTop;
import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;

/** 
 *任务描述：统计牛人和牛帖 
 * @author qizj 
 * @email  qizj@zz91.net
 * @version 创建时间：2011-8-16 
 */
public class AnalysisBbsTopTask implements ZZTask {
	
	private String startdDate;
	private String endDate;
	
	@Override
	public boolean init() throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		startdDate=DateUtil.toString(DateUtil.getDateAfterDays(baseDate, -7),"yyyy-MM-dd 00:00:00");
		endDate=DateUtil.toString(DateUtil.getDateAfterDays(baseDate, -1), "yyyy-MM-dd 23:59:59");
		long target=DateUtil.getTheDayZero(baseDate, 0);
		analysisCowPerson(target);
		analysisCowStickers(target);
		return true;
	}

	@Override
	public boolean clear(Date baseDate) throws Exception {
		long target=DateUtil.getTheDayZero(baseDate, 0);
		DBUtils.insertUpdate("ast", "delete from analysis_bbs_top where gmt_target="+target);
		return true;
	}
	
	//统计牛贴
	private void analysisCowStickers(Long gmtTarget) {
		String sql="select visited_count,id,title from bbs_post where (check_status=1 or check_status=2) and is_del='0' " +
				"and post_time>'"+startdDate+"' and post_time<'"+endDate+"' and visited_count>0 order by visited_count desc limit 10";
		final List<AnalysisBbsTop> list=new ArrayList<AnalysisBbsTop>();
		DBUtils.select("ast", sql, new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					AnalysisBbsTop top=new AnalysisBbsTop();
					top.setNum(rs.getInt(1));
					top.setTargerId(rs.getInt(2));
					top.setTitle(rs.getString(3));
					list.add(top);
				}
			}
		});
		for (AnalysisBbsTop analysisBbsTop : list) {
			insertBbsTop("post", analysisBbsTop.getTitle(), analysisBbsTop.getTargerId(), analysisBbsTop.getNum(), gmtTarget);
		}
	}
	
	//统计牛人
	private void analysisCowPerson(Long gmtTarget) {
		String sql2="select count(*) a,bbs_user_profiler_id,account,post_time from bbs_post where is_del='0' and (check_status=1 or check_status=2) and " +
				" post_time>'"+startdDate+"' and post_time<'"+endDate+"' and bbs_user_profiler_id>0 and account is not null group by account order by a desc limit 6";
		final List<AnalysisBbsTop> list =new ArrayList<AnalysisBbsTop>();
		
		DBUtils.select("ast", sql2, new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					AnalysisBbsTop top=new AnalysisBbsTop();
					top.setNum(rs.getInt(1));
					top.setTargerId(rs.getInt(2));
					top.setTitle(rs.getString(3));
					list.add(top);
				}
			}
		});
		
		final Map<String, String> map=new HashMap<String, String>();
		for (AnalysisBbsTop analysisBbsTop : list) {
			map.put("name", null);
			String sql="select nickname,id from bbs_user_profiler where account='"+analysisBbsTop.getTitle()+"'";
			DBUtils.select("ast", sql, new IReadDataHandler() {
				
				@Override
				public void handleRead(ResultSet rs) throws SQLException {
					while(rs.next()) {
						map.put("name", rs.getString("nickname"));
					}
				}
			});
			
			if (map.get("name")!=null && map.get("name")!="") {
				insertBbsTop("profile", map.get("name"),analysisBbsTop.getTargerId() , analysisBbsTop.getNum(), gmtTarget);
			} else{
				insertBbsTop("profile", analysisBbsTop.getTitle(), analysisBbsTop.getTargerId(), analysisBbsTop.getNum(),gmtTarget);
			}
		}
	}
	
	private void insertBbsTop(String categroy,String title,Integer targerId,Integer num, Long gmtTarget){
		String sql1="insert into `analysis_bbs_top` (`category`,`title`,`target_id`,`num`,`gmt_target`,`gmt_created`,`gmt_modified`) " +
				"values('"+categroy+"','"+title+"',"+targerId+","+num+","+gmtTarget+",now(),now())";
		DBUtils.insertUpdate("ast", sql1);
	}
	
	public static void main(String[] args){
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		AnalysisBbsTopTask cowPersonTask=new AnalysisBbsTopTask();
		try {
			cowPersonTask.clear(DateUtil.getDate("2011-08-15", "yyyy-MM-dd"));
			cowPersonTask.exec(DateUtil.getDate("2011-08-15", "yyyy-MM-dd"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}