package com.zz91.mission.ep.sync;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.zz91.mission.domain.TradeBuy;
import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.http.HttpUtils;
import com.zz91.util.lang.StringUtils;
import com.zz91.util.mail.MailUtil;

public class SyncTradeBuyTask implements ZZTask {

	final static String DB_EP = "ep";

	final static String SYNC_TABLE = "trade_buy";

	final static Integer LIMIT = 50;

	final static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	public static String SYNC_URL_SEED = "http://huanbaoadmin.zz91.com/sync/seed";
	public static String SYNC_URL_IMPT = "http://huanbaoadmin.zz91.com/sync/imptTradeBuy";

	@Override
	public boolean init() throws Exception {

		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {

		// 取得最大的id号 和最大的更新时间
		String maxInfo = HttpUtils.getInstance().httpGet(
				SYNC_URL_SEED + "?table=trade_buy", HttpUtils.CHARSET_UTF8);
		//String maxInfo="{maxId:154421,maxGmtModified:1230}";
		long now = new Date().getTime();

		JSONObject jobj = JSONObject.fromObject(maxInfo);
		// 最大的时间
		Integer unSyncMaxId = jobj.getInt("maxId");

		// 最大更新时间
		long modifiedSeed = jobj.getLong("maxGmtModified");

		if (modifiedSeed < now) {
			modifiedSeed = now;
		}

		// 成功次数
		Integer success = 0;
		// 失败次数
		Integer failure = 0;

		do {

			List<TradeBuy> list = queryTradeBuy(unSyncMaxId);

			if (list == null || list.size() <= 0) {
				break;
			}

			for (TradeBuy buy : list) {

				modifiedSeed = buildShowTime(modifiedSeed);

				buy.setGmtModified(new Date(modifiedSeed));

				unSyncMaxId = buy.getId();
			}

			// 提交请求
			NameValuePair[] param = new NameValuePair[] { new NameValuePair(
					"tradeBuy", JSONArray.fromObject(list).toString()) };

			String result = null;
			try {
				result = HttpUtils.getInstance().httpPost(SYNC_URL_IMPT, param,
						HttpUtils.CHARSET_UTF8);
			} catch (HttpException e) {
			} catch (IOException e) {
			}

			if (StringUtils.isEmpty(result) || !result.startsWith("{")) {
				for (TradeBuy buy : list) {
					failure++;
					logUnsync(buy.getId(), baseDate.getTime());
				}
			}

			JSONObject resultJson = JSONObject.fromObject(result);

			if (resultJson.containsKey("success")) {
				success = success + resultJson.getInt("success");
			}

			if (resultJson.containsKey("failure")) {
				JSONArray ja = resultJson.getJSONArray("failure");
				for (Object id : ja) {
					failure++;
					logUnsync(Integer.valueOf(String.valueOf(id)),
							baseDate.getTime());
				}
			}

			Thread.sleep(500);
		} while (true);

		// 发送邮件通知结果

		Map<String, Object> dataMap = new HashMap<String, Object>();

		Date end = new Date();
		dataMap.put("reportTarget", "交易中心-求购信息");
		dataMap.put("successNum", success);
		dataMap.put("failureNum", failure);
		dataMap.put("maxShowTime",
				DateUtil.toString(new Date(modifiedSeed), DATE_FORMAT));
		dataMap.put("startDate", DateUtil.toString(new Date(now), DATE_FORMAT));
		dataMap.put("endDate", DateUtil.toString(end, DATE_FORMAT));
		dataMap.put("timeCost", (end.getTime() - now) / 1000);

		MailUtil.getInstance().sendMail("环保网数据同步报告 交易中心 求购信息",
				"ep.sync.report@asto.mail", null, null, "zz91",
				"ep-sync-report", dataMap, MailUtil.PRIORITY_TASK);

		return true;
	}

	public List<TradeBuy> queryTradeBuy(Integer maxId) {

		final List<TradeBuy> list = new ArrayList<TradeBuy>();

		StringBuffer sql = new StringBuffer();
		sql.append("select ");
		sql.append(
				"id,uid,cid,title,details,category_code,photo_cover,province_code,")
				.append("area_code,buy_type,quantity,quantity_year,quantity_untis,supply_area_code,use_to,")
				.append("gmt_confirm,gmt_receive,gmt_publish,gmt_refresh,valid_days,gmt_expired,")
				.append("tags_sys,details_query,message_count,view_count,favorite_count,")
				.append("plus_count,html_path,del_status,pause_status,check_status,check_admin,check_refuse,")
				.append("gmt_check,gmt_created,gmt_modified");
		sql.append(" from trade_buy where id>").append(maxId);
		sql.append(" order by id asc limit ").append(LIMIT);
		DBUtils.select(DB_EP, sql.toString(), new IReadDataHandler() {

			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				TradeBuy buy = null;
				while (rs.next()) {
					buy = new TradeBuy();
					buy.setId(rs.getInt("id"));
					buy.setUid(rs.getInt("uid"));
					buy.setCid(rs.getInt("cid"));
					buy.setTitle(rs.getString("title"));
					buy.setDetails(rs.getString("details"));
					buy.setCategoryCode(rs.getString("category_code"));
					buy.setPhotoCover(rs.getString("photo_cover"));
					buy.setProvinceCode(rs.getString("province_code"));
					buy.setAreaCode(rs.getString("area_code"));
					buy.setBuyType(rs.getShort("buy_type"));
					buy.setQuantity(rs.getInt("quantity"));
					buy.setQuantityYear(rs.getInt("quantity_year"));
					buy.setQuantityUntis(rs.getString("quantity_untis"));
					buy.setSupplyAreaCode(rs.getString("supply_area_code"));
					buy.setUseTo(rs.getString("use_to"));
					buy.setGmtConfirm(getJavaDate(rs.getDate("gmt_confirm")));
					buy.setGmtReceive(getJavaDate(rs.getDate("gmt_receive")));
					buy.setGmtPublish(getJavaDate(rs.getDate("gmt_publish")));
					buy.setGmtRefresh(getJavaDate(rs.getDate("gmt_refresh")));
					buy.setValidDays(rs.getShort("valid_days"));
					buy.setGmtExpired(getJavaDate(rs.getDate("gmt_expired")));
					buy.setTagsSys(rs.getString("tags_sys"));
					buy.setDetailsQuery(rs.getString("details_query"));
					buy.setMessageCount(rs.getInt("message_count"));
					buy.setViewCount(rs.getInt("view_count"));
					buy.setFavoriteCount(rs.getInt("favorite_count"));
					buy.setPlusCount(rs.getInt("plus_count"));
					buy.setHtmlPath(rs.getString("html_path"));
					buy.setDelStatus(rs.getShort("del_status"));
					buy.setPauseStatus(rs.getShort("pause_status"));
					buy.setCheckStatus(rs.getShort("check_status"));
					buy.setCheckAdmin(rs.getString("check_admin"));
					buy.setCheckRefuse(rs.getString("check_refuse"));
					buy.setGmtCheck(getJavaDate(rs.getDate("gmt_check")));
					buy.setGmtCreated(getJavaDate(rs.getDate("gmt_created")));
					buy.setGmtModified(getJavaDate(rs.getDate("gmt_modified")));
					list.add(buy);
				}
			}
		});

		return list;
	}

	public long buildShowTime(long seed) {

		long now_zero = DateUtil.getTheDayZero(new Date(seed), 0);

		long nowtime = seed - (now_zero * 1000);

		// 6
		if (nowtime <= 21600000) {
			return seed + 2143;
		}
		// 9
		if (nowtime <= 32400000) {
			return seed + 1799;
		}
		// 11
		if (nowtime <= 39600000) {
			return seed + 411;
		}
		// 13
		if (nowtime <= 46800000) {
			return seed + 1799;
		}
		// 17
		if (nowtime <= 61200000) {
			return seed + 411;
		}
		// 19
		if (nowtime <= 68400000) {
			return seed + 1799;
		}
		// 21
		if (nowtime <= 75600000) {
			return seed + 411;
		}
		// 24
		return seed + 1799;
	}

	private Date getJavaDate(java.sql.Date date) {
		if (date == null) {
			return null;
		}
		return new Date(date.getTime());
	}

	private void logUnsync(Integer id, long gmtTask) {
		DBUtils.insertUpdate(
				DB_EP,
				"insert into unsync_log(unsync_id, sync_table, gmt_task, gmt_created, gmt_modified) values("
						+ id
						+ ",'"
						+ SYNC_TABLE
						+ "',"
						+ gmtTask
						+ ",now(),now())");
	}

	@Override
	public boolean clear(Date baseDate) throws Exception {

		return false;
	}
	
	public static void main(String[] args) {
//		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
//		SyncTradeBuyTask c = new SyncTradeBuyTask();
//		SyncTradeBuyTask.SYNC_URL_IMPT = "http://127.0.0.1:8080/sync/imptTradeBuy";
//		try {
//			c.exec(new Date());
//		} catch (Exception e) {
//			
//			e.printStackTrace();
//		}
//		
	}

}
