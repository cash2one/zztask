package com.zz91.mission.recyclechina;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.mail.MailUtil;
import com.zz91.util.search.SorlUtil;

/**
 * @Author:kongsj
 * @Date:2012-4-13
 */
public class RegisterOneWeekLater implements ZZTask {
	public static String DB = "recyclechina";
	public static String ACCOUNT = "recyclechina";
	public static String TEMPLATE_TO_BUYER = "recyclechina-matchToBuyer";
	public static String TEMPLATE_TO_SELLER = "recyclechina-matchToSeller";
	public static String TEMPLATE_TO_NOPUB = "recyclechina-noPublishInWeek";
	public static Integer MAIL_PRIORITY = 20;

	@Override
	public boolean init() throws Exception {
		SorlUtil.getInstance().init("web.properties");
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean exec(Date baseDate) throws Exception {
		Map<String, Object> map = getCompanyInfo(baseDate);
		Map<Integer, Map<String, Object>> productMap = (Map<Integer, Map<String, Object>>) map.get("productMap");
		// 发送匹配客户信息邮件
		sendMatchEmail(productMap);
		Map<Integer, Map<String, Object>> noproductMap = (Map<Integer, Map<String, Object>>) map.get("noproductMap");
		// 发送提醒发布询盘邮件
		sendRemindEmail(noproductMap);
		return true;
	}

	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	private Map<String, Object> getCompanyInfo(Date baseDate) {
		Map<String, Object> map = new HashMap<String, Object>();
		final Map<Integer, Map<String, Object>> productMap = new HashMap<Integer, Map<String, Object>>();
		final Map<Integer, Map<String, Object>> noproductMap = new HashMap<Integer, Map<String, Object>>();
		String from = DateUtil.toString(DateUtil.getDateAfterDays(baseDate, -7), "yyyy-MM-dd");
		String to = DateUtil.toString(DateUtil.getDateAfterDays(baseDate, -6),"yyyy-MM-dd");
		String sql = "select c.id,ca.email,ca.backup_email,ca.is_use_backup_email,(select name from products where company_id = c.id limit 1),ca.account,c.deal_type_code from company c left join company_account ca on c.id = ca.company_id "
				+ "where '" + from + "' < c.gmt_register and c.gmt_register < '" + to + "'";
		DBUtils.select(DB, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					Map<String, Object> companyInfo = new HashMap<String, Object>();
					Integer companyId = rs.getInt(1);
					String productKeyword = rs.getString(5);
					String email = (rs.getInt(4) == 1) ? rs.getString(3) : rs.getString(2);
					String account = rs.getString(6);
					String dealTypeCode = rs.getString(7);
					companyInfo.put("account", account);
					companyInfo.put("email", email);
					companyInfo.put("dealTypeCode", dealTypeCode);
					if (productKeyword == null||productKeyword=="") {
						noproductMap.put(companyId, companyInfo);
					} else {
						companyInfo.put("productKeyword", productKeyword);
						productMap.put(companyId, companyInfo);
					}
				}
			}
		});
		map.put("productMap", productMap);
		map.put("noproductMap", noproductMap);
		return map;
	}

	private void sendMatchEmail(Map<Integer, Map<String,Object>> maps) throws SolrServerException{
		for(Integer companyId:maps.keySet()){
			SolrQuery query = new SolrQuery();
			SolrServer server = SorlUtil.getInstance().getSolrServer("rebornProducts");
			query.setQuery(maps.get(companyId).get("productKeyword").toString());
			query.addFilterQuery("isChecked:true");
			query.addFilterQuery("isDeleted:false");
			String templateName;
			if("10011000".equals(maps.get(companyId).get("dealTypeCode"))){
				templateName = TEMPLATE_TO_BUYER;
				query.addFilterQuery("buyOrSell:1");
			}else{
				templateName = TEMPLATE_TO_SELLER;
				query.addFilterQuery("buyOrSell:0");
			}
			query.addSortField("gmtRefresh", ORDER.desc);
			query.setStart(0);
			query.setRows(1);
			QueryResponse rsp = server.query(query);
			SolrDocumentList sds = rsp.getResults();
			for(SolrDocument sd :sds){
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("name", maps.get(companyId).get("account"));
				map.put("companyUrl", "http://www.recyclechina.com/companyInfo/details"+companyId+".htm");
				map.put("companyName", sd.getFieldValue("companyName"));
				map.put("product", sd.getFieldValue("name"));
				sendEmail("Recyclechina Match your customer", maps.get(companyId).get("email").toString(), templateName, map);
			}
		}
	}

	private void sendRemindEmail(Map<Integer, Map<String, Object>> maps) {
		for(Integer companyId:maps.keySet()){
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("name", maps.get(companyId).get("account"));
			sendEmail("Welcome to the RecycleChina", maps.get(companyId).get("email").toString(), TEMPLATE_TO_NOPUB, map);
		}
	}

	private void sendEmail(String title, String to, String templateName,
			Map<String, Object> map) {
//		MailUtil.getInstance().sendMail(title, to, templateName, map,MAIL_PRIORITY);
		MailUtil.getInstance().sendMail(title, to, ACCOUNT, templateName, map, MAIL_PRIORITY);
	}
	
	public static void main(String[] args) throws Exception{
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		SorlUtil.getInstance().init("web.properties");
		MailUtil.getInstance().init("web.properties");
		RegisterOneWeekLater obj =new RegisterOneWeekLater();
		Date date = DateUtil.getDate("2012-04-20", "yyyy-MM-dd");
		obj.exec(date);
	}
}