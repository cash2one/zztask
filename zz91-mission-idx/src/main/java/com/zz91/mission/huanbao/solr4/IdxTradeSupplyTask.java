/**
 * 
 */
package com.zz91.mission.huanbao.solr4;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

import com.zz91.task.common.AbstractIdxTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.lang.StringUtils;
import com.zz91.util.search.solr.SolrUpdateUtil;

/**
 * 供应信息索引任务
 * 
 * 导入会员：0 普通会员：100 付费会员：200
 * 
 * @author mays
 * 
 */
public class IdxTradeSupplyTask extends AbstractIdxTask {

	final static String DB = "ep";
	final static int LIMIT = 25;

	final static String MODEL = "hbtradesupply";
	final static int IMPORT_ID_SPLIT = 50000000;
	final static String MEMBER = "10011000";

	// final static int RESET_LIMIT=5000;
	final static Map<String, Integer> SORT_MEMBER = new HashMap<String, Integer>();

	static {
		SORT_MEMBER.put("10011000", 100);
		SORT_MEMBER.put("10011001", 200);
	}

	@Override
	public Boolean idxReq(Long start, Long end) throws Exception {
		StringBuffer sql = new StringBuffer();
		sql.append("select count(*) from trade_supply ");
		sqlwhere(sql, start, end, null);
		final Integer[] dealCount = new Integer[1];
		DBUtils.select(DB, sql.toString(), new IReadDataHandler() {

			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					dealCount[0] = rs.getInt(1);
				}
			}
		});

		if (dealCount[0] != null && dealCount[0] > 0) {
			return true;
		}

		return false;
	}

	@Override
	public void idxPost(Long start, Long end) throws Exception {

		SolrServer server = SolrUpdateUtil.getInstance().getSolrServer(MODEL);

		int id = 0;
		int docsize = 0;
		Map<String, String> categoryMap = new HashMap<String, String>();
		do {
			List<SolrInputDocument> docs = queryDocs(start, end, id,
					categoryMap);

			if (docs.size() <= 0) {
				break;
			}

			server.add(docs);

			docsize = docsize + docs.size();

			id = resetId(docs.get(docs.size() - 1));

			// System.out.println(">>>>>"+docsize);

		} while (true);

		throw new Exception("共创建/更新" + docsize + "条索引");
	}

	@Override
	public void optimize() throws Exception {
		SolrUpdateUtil.getInstance().getSolrServer(MODEL).optimize();
	}

	private Integer resetId(SolrInputDocument doc) {
		Integer id = (Integer) doc.getFieldValue("id");
		return id;
	}

	private List<SolrInputDocument> queryDocs(Long start, Long end,
			int resetId, Map<String, String> categoryMap) {
		final List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();

		StringBuffer sql = new StringBuffer();
		sql.append("select ");
		sql.append("ts.id,ts.cid,ts.title,ts.province_code,")
				.append("ts.area_code,ts.price_num,ts.property_query,")
				.append("ts.gmt_refresh,ts.check_status,")
				.append("ts.pause_status,ts.del_status,ts.category_code,ts.uid,")
				.append("ts.integrity,ts.view_count,ts.favorite_count,ts.plus_count");
		sql.append(" from trade_supply ts");
		sqlwhere(sql, start, end, resetId);
		sql.append(" order by ts.id asc limit ").append(LIMIT);

		DBUtils.select(DB, sql.toString(), new IReadDataHandler() {

			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					SolrInputDocument doc = new SolrInputDocument();
					doc.addField("id", rs.getObject("id"));
					doc.addField("cid", rs.getObject("cid"));
					// doc.addField("photoCover", rs.getObject(3));
					doc.addField("title", rs.getObject("title"));
					doc.addField("provinceCode", rs.getObject("province_code"));
					doc.addField("areaCode", rs.getObject("area_code"));
					doc.addField("priceNum", rs.getObject("price_num"));
					// doc.addField("priceUnits", rs.getObject(8));
					String propertyQuerys = rs.getString("property_query");
					if(StringUtils.isNotEmpty(propertyQuerys)){
						String [] propertyQuery = propertyQuerys.split(";");
						for(String property : propertyQuery){
							doc.addField("propertyQuery",property); 
						}
					}
					//doc.addField("propertyQuery",rs.getObject("property_query"));
					// doc.addField("detailsQuery", rs.getObject(10));
					String gmtRefresh = rs.getString("gmt_refresh");
					doc.addField("gmtRefresh", getTime(gmtRefresh));
					doc.addField("checkStatus", rs.getObject("check_status"));
					doc.addField("pauseStatus", rs.getObject("pause_status"));
					doc.addField("delStatus", rs.getObject("del_status"));
					doc.addField("categoryCode", rs.getObject("category_code"));
					// doc.addField("gmtModified", rs.getObject(16));
					doc.addField("uid", rs.getObject("uid"));
					doc.addField("integrity", rs.getObject("integrity"));
					doc.addField("viewCount", rs.getObject("view_count"));
					doc.addField("favoriteCount",
							rs.getObject("favorite_count"));
					doc.addField("plusCount", rs.getObject("plus_count"));
					docs.add(doc);
				}
			}
		});

		for (SolrInputDocument doc : docs) {
			parseCategory(doc, categoryMap);
			parseComp(doc, (Integer) doc.getFieldValue("cid"));
		}

		return docs;
	}

	private void sqlwhere(StringBuffer sb, Long start, Long end, Integer resetId) {
		sb.append(" where gmt_modified >='")
				.append(DateUtil.toString(new Date(start), FORMATE))
				.append("' ");
		sb.append(" and gmt_modified <='")
				.append(DateUtil.toString(new Date(end), FORMATE)).append("' ");
		if(resetId!=null){
			sb.append(" and id >").append(resetId);
		}
		
	}

	private void parseCategory(SolrInputDocument doc,
			Map<String, String> categoryMap) {
		String code = String.valueOf(doc.getFieldValue("categoryCode"));

		// String code="1000";
		if (StringUtils.isNotEmpty(code)) {
			doc.addField("category4", substringCode(code, 4));
			doc.addField("category8", substringCode(code, 8));
			doc.addField("category12", substringCode(code, 12));
			doc.addField("category16", substringCode(code, 16));
			doc.addField("category20", substringCode(code, 20));

			// 处理categoryName
			if (!categoryMap.containsKey(code)) {
				categoryMap.put(code, queryCategoryName(code));
			}
			doc.addField("categoryName",
					categoryMap.get(doc.getFieldValue("categoryCode")));
		}
	}

	private String queryCategoryName(String code) {
		final String[] str = new String[1];

		DBUtils.select(DB, "select name from trade_category where code='"
				+ code + "' limit 1", new IReadDataHandler() {

			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					str[0] = rs.getString(1);
				}
			}
		});
		return str[0];
	}

	private String substringCode(String code, int length) {
		if (code.length() >= length) {
			return code.substring(0, length);
		}
		return code;
	}

	private void parseComp(SolrInputDocument doc, Integer cid) {

		final Map<String, Object> result = new HashMap<String, Object>();
		StringBuffer sql = new StringBuffer();
		sql.append("select  ")
				.append("member_code ,member_code_block from comp_profile where id= ")
				.append(cid);
		DBUtils.select(DB, sql.toString(), new IReadDataHandler() {

			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					// result.put("name", rs.getObject("name"));
					// result.put("qq",rs.getObject("qq"));
					result.put("memberCode", rs.getObject("member_code"));
					String codeBlock = rs.getString("member_code_block");
					if (codeBlock == null || "".equals(codeBlock)) {

						result.put("memberCodeBlock", "-1");

					} else {

						result.put("memberCodeBlock", codeBlock);

					}
					// result.put("gmtRegister", rs.getObject("gmt_created"));
				}
			}
		});

		for (String k : result.keySet()) {
			doc.addField(k, result.get(k));
		}

		// parse gcid
		doc.addField("gcid", "g" + String.valueOf(cid));

		Date refresh = new Date((Long) doc.getFieldValue("gmtRefresh"));

		// parse sortMember
		if (cid.intValue() < IMPORT_ID_SPLIT
				&& !"10011001".equals(result.get("memberCode"))) {
			doc.addField("sortMember", 0);
		} else {
			if ((new Date().getTime() - refresh.getTime()) < 3 * 86400000) {
				doc.addField("sortMember",
						SORT_MEMBER.get(result.get("memberCode")));
			} else {
				doc.addField("sortMember", 100);
			}
		}

		// parse sortRefresh
		try {
			if (!MEMBER.equals(result.get("memberCode"))
					&& (new Date().getTime() - refresh.getTime()) < 3 * 86400000) {
				doc.addField(
						"sortRefresh",
						DateUtil.getDate(DateUtil.getDateAfterDays(refresh, 3),
								"yyyy-MM-dd").getTime());
			} else {
				doc.addField("sortRefresh",
						DateUtil.getDate(refresh, "yyyy-MM-dd").getTime());
			}
		} catch (ParseException e) {
		}
	}

	private long getTime(String str) {
		long longTime = 0;
		if (StringUtils.isNotEmpty(str)) {
			try {
				longTime = DateUtil.getDate(str, "yyyy-MM-dd HH:mm:ss")
						.getTime();
			} catch (ParseException e) {

			}
		}

		return longTime;
	}

	public static void main(String[] args) throws SolrServerException,
			IOException {
		SolrUpdateUtil.getInstance().init(
				"file:/usr/tools/config/search/search.properties");
		DBPoolFactory.getInstance().init(
				"file:/usr/tools/config/db/db-zztask-jdbc.properties");

		// SolrServer server=SolrUtil.getInstance().getSolrServer("ts");
		//
		// SolrInputDocument doc=new SolrInputDocument();
		// doc.addField("id", 96557);
		// doc.addField("title", "杭州阿思拓-供应金属喷咀喷头");
		// doc.addField("provinceCode", "100110001009");

		// server.add(doc);
		// server.commit();

		// String start="2011-11-29 15:13:20";
		// String end="2011-11-29 15:13:21";
		String start = "2012-09-01 11:49:49";
		String end = "2013-01-01 11:51:10";

		AbstractIdxTask task = new IdxTradeSupplyTask();
		try {
			// System.out.println(task.idxReq(DateUtil.getDate(start,
			// FORMATE).getTime(), DateUtil.getDate(end, FORMATE).getTime()));
			task.idxPost(DateUtil.getDate(start, FORMATE).getTime(), DateUtil
					.getDate(end, FORMATE).getTime());
			// task.optimize();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// >>>>>1766683>>>>>>1700
		// 1761891
		// java.lang.Exception: 共创建/更新1766683条索引

	}

}
