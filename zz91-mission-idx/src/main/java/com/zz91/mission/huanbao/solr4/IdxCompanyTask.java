package com.zz91.mission.huanbao.solr4;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;

import com.zz91.task.common.AbstractIdxTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.lang.StringUtils;
import com.zz91.util.search.solr.SolrUpdateUtil;

public class IdxCompanyTask extends AbstractIdxTask {

	final static String DB = "ep";
	final static int LIMIT = 25;

	final static String MODEL = "hbcompany";
	final static int IMPORT_ID_SPLIT = 50000000;
	// final static int RESET_LIMIT=5000;

	final static Map<String, Integer> SORT_MEMBER = new HashMap<String, Integer>();

	static {
		SORT_MEMBER.put("10011000", 100);
		SORT_MEMBER.put("10011001", 200);
	}

	@Override
	public Boolean idxReq(Long start, Long end) throws Exception {
		StringBuffer sql = new StringBuffer();
		sql.append("select count(*) from comp_profile ");
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
		do {
			List<SolrInputDocument> docs = queryDocs(start, end, id);
			if (docs.size() == 0) {
				break;
			}
			server.add(docs);
			docsize = docsize + docs.size();

			// start = resetStart(docs.get(docs.size()-1));

			id = resetId(docs.get(docs.size() - 1));

			// System.out.println("comp>>>>>"+docsize);

		} while (true);
		throw new Exception("共创建/更新" + docsize + "条索引");
	}

	@Override
	public void optimize() throws Exception {
		SolrUpdateUtil.getInstance().getSolrServer(MODEL).optimize();
	}

	private void sqlwhere(StringBuffer sql, Long start, Long end,
			Integer resetId) {
		sql.append(" where gmt_modified >='")
				.append(DateUtil.toString(new Date(start), FORMATE))
				.append("' ");
		sql.append(" and gmt_modified <='")
				.append(DateUtil.toString(new Date(end), FORMATE)).append("' ");
		if(resetId!=null){
			sql.append(" and id > ").append(resetId);
		}
		
	}

	private List<SolrInputDocument> queryDocs(Long start, Long end, int resetId) {

		final List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();

		StringBuffer sql = new StringBuffer();
		sql.append("select ");
		sql.append(
				"comp.id,comp.name,comp.member_code,comp.details_query,comp.industry_code,comp.business_code,comp.main_buy,")
				.append("comp.main_product_buy,comp.main_supply,comp.main_product_supply,comp.address,comp.province_code,comp.area_code,comp.del_status, ")
				.append("comp.tags,comp.view_count,comp.message_count,comp.main_brand,comp.gmt_created,comp.member_code_block ");
		sql.append("from comp_profile comp");
		sqlwhere(sql, start, end, resetId);

		sql.append(" order by comp.id asc limit ").append(LIMIT);

		sql.toString();

		DBUtils.select(DB, sql.toString(), new IReadDataHandler() {

			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				SolrInputDocument doc = null;
				while (rs.next()) {
					doc = new SolrInputDocument();
					doc.addField("id", rs.getObject("id"));
					doc.addField("name", rs.getObject("name"));
					doc.addField("memberCode", rs.getObject("member_code"));

					String codeBlock = rs.getString("member_code_block");
					if (codeBlock == null || "".equals(codeBlock)) {
						doc.addField("memberCodeBlock", "-1");
					} else {
						doc.addField("memberCodeBlock", codeBlock);
					}

					doc.addField("detailsQuery", rs.getObject("details_query"));
					doc.addField("industryCode", rs.getObject("industry_code"));
					doc.addField("businessCode", rs.getObject("business_code"));
					doc.addField("mainBuy", rs.getObject("main_buy"));
					doc.addField("mainProductBuy",
							rs.getObject("main_product_buy"));
					doc.addField("mainSupply", rs.getObject("main_supply"));
					doc.addField("mainProductSupply",
							rs.getObject("main_product_supply"));
					doc.addField("address", rs.getObject("address"));
					doc.addField("provinceCode", rs.getObject("province_code"));
					doc.addField("areaCode", rs.getObject("area_code"));
					doc.addField("delStatus", rs.getObject("del_status"));
					// doc.addField("gmtModified",
					// rs.getObject("gmt_modified"));
					doc.addField("tags", rs.getObject("tags"));
					doc.addField("viewCount", rs.getObject("view_count"));
					doc.addField("messageCount", rs.getObject("message_count"));
					doc.addField("mainBrand", rs.getObject("main_brand"));
					String gmtCreated = rs.getString("gmt_created");
					doc.addField("gmtCreated", getTime(gmtCreated));

					docs.add(doc);
				}

			}
		});
		for (SolrInputDocument doc : docs) {
			Integer id = (Integer) doc.getFieldValue("id");
			parseMember(doc, id);
			parseChainId(doc, id);
		}
		return docs;
	}

	// private Long resetStart(SolrInputDocument doc){
	// Date d=(Date) doc.getFieldValue("gmtModified");
	// return d.getTime();
	// }

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

	private Integer resetId(SolrInputDocument doc) {
		Integer resetId = (Integer) doc.getFieldValue("id");
		return resetId;
	}

	private void parseMember(SolrInputDocument doc, Integer id) {
		if (id > IMPORT_ID_SPLIT) {
			String memberCode = doc.getFieldValue("memberCode").toString();
			if (StringUtils.isNotEmpty(memberCode)) {
				doc.addField("sortMemberCode", SORT_MEMBER.get(memberCode));
			} else {
				doc.addField("sortMemberCode", -100);
			}
		} else {
			// 导入的公司信息
			doc.addField("sortMemberCode", 0);
		}
	}

	private void parseChainId(final SolrInputDocument doc, Integer id) {
		StringBuffer sql = new StringBuffer();
		sql.append("select chain_id from company_industry_chain where cid = ")
				.append(id).append(" and del_status = 0 ");
		DBUtils.select(DB, sql.toString(), new IReadDataHandler() {

			@Override
			public void handleRead(ResultSet rs) throws SQLException {

				while (rs.next()) {
					doc.addField("chainId", rs.getString("chain_id"));
				}
			}
		});
	}

	public static void main(String[] args) {
		SolrUpdateUtil.getInstance().init(
				"file:/usr/tools/config/search/search.properties");
		DBPoolFactory.getInstance().init(
				"file:/usr/tools/config/db/db-zztask-jdbc.properties");

		String start = "2011-09-29 17:07:23";
		String end = "2012-11-29 17:14:18";

		IdxCompanyTask task = new IdxCompanyTask();
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

	}
}
