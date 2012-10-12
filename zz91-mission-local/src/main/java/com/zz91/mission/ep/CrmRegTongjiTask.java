/**
 * @author qizj
 * @email  qizhenj@gmail.com
 * @create_time  2012-9-13 下午03:17:12
 */
package com.zz91.mission.ep;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;

/**
 * 统计今天注册客户(销售crm)
 */

public class CrmRegTongjiTask implements ZZTask {
	
	Logger LOG=Logger.getLogger(CrmRegTongjiTask.class);
	final static String DATE_FORMAT = "yyyy-MM-dd";
	final static String DB="crm";
	
	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		String targetDate = DateUtil.toString(baseDate, DATE_FORMAT);
		tongji(targetDate);
		return true;
	}

	private void tongji(String targetDate) {
		//100110001010:浙江 100110001009:江苏  100110001008:上海 100110001018:广东 
		//100110001014:山东 100110001000:北京 100110001002:河北
		final Map<String,Integer> map= new HashMap<String, Integer>();
		String tongjiSql = "SELECT substring(province_code,1,12) as code,count(*) as count FROM crm_company "
		+"where date_format(gmt_register,'%Y-%m-%d') = date_format(date_add(now(), interval -1 day),'%Y-%m-%d') "
		+"group by code";
		DBUtils.select(DB, tongjiSql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()) {
					map.put(rs.getString("code"), rs.getInt("count"));
				}
			}
		});
		Integer zhejiang = 0;
		Integer jiangsu = 0;
		Integer shanghai = 0;
		Integer guangdong = 0;
		Integer shandong = 0;
		Integer beijing = 0;
		Integer hebei = 0;
		Integer other = 0;
		for (Iterator iter = map.keySet().iterator();iter.hasNext();) {
			String key = (String) iter.next();
			if ("100110001010".equals(key)) {
				zhejiang = map.get(key);
			} else if("100110001009".equals(key)) {
				jiangsu = map.get(key);
			} else if("100110001008".equals(key)) {
				shanghai = map.get(key);
			} else if("100110001018".equals(key)) {
				guangdong = map.get(key);
			} else if("100110001014".equals(key)) {
				shandong = map.get(key);
			} else if("100110001000".equals(key)) {
				beijing = map.get(key);
			} else if("100110001002".equals(key)) {
				hebei = map.get(key);
			} else {
				other += map.get(key);
			}
		}
		StringBuilder sql=new StringBuilder();
		sql.append("insert into crm_sale_statistics(`gmt_target`,`zhejiang`,`jiangsu`,`shanghai`,`guangdong`,`shandong`,`beijing`,`hebei`,`other`,`gmt_created`,`gmt_modified`) values (");
		sql.append("date_format(date_add(now(), interval -1 day),'%Y-%m-%d'),");
		sql.append(zhejiang);
		sql.append(",");
		sql.append(jiangsu);
		sql.append(",");
		sql.append(shanghai);
		sql.append(",");
		sql.append(guangdong);
		sql.append(",");
		sql.append(shandong);
		sql.append(",");
		sql.append(beijing);
		sql.append(",");
		sql.append(hebei);
		sql.append(",");
		sql.append(other);
		sql.append(",");
		sql.append("now(),now())");
		boolean result = DBUtils.insertUpdate(DB, sql.toString());
		if(!result) {
			LOG.info(">>>>>>>>>>>>>>>>>统计昨天注册人数失败:"+sql.toString());
		}
	}
	
	@Override
	public boolean init() throws Exception {
		return false;
	}

	public static void main(String[] args) {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		CrmRegTongjiTask task = new CrmRegTongjiTask();
		try {
			task.exec(DateUtil.getDate("2011-11-23", "yyyy-MM-dd"));
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
