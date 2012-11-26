package com.zz91.mission.kl91;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;

import com.zz91.task.common.AbstractIdxTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.lang.StringUtils;
import com.zz91.util.search.solr.SolrUpdateUtil;
/**
 * @author 伍金成：kl91 solr更新公司任务
 * 
 *
 */
public class IndexCompanyTask extends AbstractIdxTask{
	final static String DB="kl91";
	final static int LIMIT=25;
	
	final static String MODEL="kl91Companys";
	final static int RESET_LIMIT=5000;
	
	@Override
	public Boolean idxReq(Long start, Long end) throws Exception {
		StringBuffer sql = new StringBuffer();
		sql.append("select count(*) from company ");
		sqlwhere(sql, start, end);
		final Integer[] dealCount=new Integer[1];
		DBUtils.select(DB, sql.toString(), new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					dealCount[0]=rs.getInt(1);
				}
			}
		});
		if(dealCount[0]!=null && dealCount[0] >0){
			return true;
		}
		return false;
	}

	@Override
	public void idxPost(Long start, Long end) throws Exception {
		SolrServer server=SolrUpdateUtil.getInstance().getSolrServer(MODEL);
		int begin = 0;
		int docsize = 0;
		do{
			List<SolrInputDocument> docs = queryDocs(start, end, begin);
			if(docs.size()==0){
				break;
			}
			server.add(docs);
			docsize=docsize+docs.size();
			begin=begin+LIMIT;
			if(begin>=RESET_LIMIT){
				start = resetStart(docs.get(docs.size()-1));
				begin=0;
			}
		}while(true);
		throw new Exception("共创建/更新"+docsize+"条索引");
	}

	@Override
	public void optimize() throws Exception {
		SolrUpdateUtil.getInstance().getSolrServer(MODEL).optimize();
	}
	
	private void sqlwhere(StringBuffer sql, Long start, Long end){
		sql.append(" where gmt_modified >='").append(DateUtil.toString(new Date(start), FORMATE)).append("' ");
		sql.append(" and gmt_modified <='").append(DateUtil.toString(new Date(end), FORMATE)).append("' ");
	}
	
	private List<SolrInputDocument> queryDocs(Long start, Long end, int begin){
		
		final List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
		
		StringBuffer sql=new StringBuffer();
		sql.append("select ");
		sql.append("id,account,company_name,industry_code,membership_code,sex,contact,mobile,")
			.append("is_active,qq,email,tel,fax,area_code,zip,address,")
			.append("position,department,introduction,business,domain,website,num_login,regist_flag,show_time,gmt_last_login,gmt_created,gmt_modified,num_pass,old_id ");
		sql.append("from company");
		sqlwhere(sql, start, end);
		sql.append(" order by gmt_modified asc limit ").append(begin).append(",").append(LIMIT);
		DBUtils.select(DB, sql.toString(), new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				SolrInputDocument doc=null;
				while(rs.next()){
					doc = new SolrInputDocument();
					doc.addField("id", rs.getObject("id"));
					doc.addField("account", rs.getObject("account"));
					doc.addField("companyName", rs.getObject("company_name"));
					doc.addField("industryCode", rs.getObject("industry_code"));
					doc.addField("membershipCode", rs.getObject("membership_code"));
					doc.addField("sex",rs.getObject("sex"));
					doc.addField("contact", rs.getObject("contact"));
					doc.addField("mobile", rs.getObject("mobile"));
					doc.addField("isActive", rs.getObject("is_active"));
					doc.addField("qq", rs.getObject("qq"));
					doc.addField("email", rs.getObject("email"));
					doc.addField("tel", rs.getObject("tel"));
					doc.addField("fax", rs.getObject("fax"));
					doc.addField("areaCode", rs.getObject("area_code"));
					doc.addField("zip", rs.getObject("zip"));
					doc.addField("address", rs.getObject("address"));
					doc.addField("position", rs.getObject("position"));
					doc.addField("department", rs.getObject("department"));
					doc.addField("introduction", rs.getObject("introduction"));
					doc.addField("business", rs.getObject("business"));
					doc.addField("domain", rs.getObject("domain"));
					doc.addField("website", rs.getObject("website"));
					doc.addField("numLogin", rs.getObject("num_login"));
					doc.addField("registFlag", rs.getObject("regist_flag"));
					doc.addField("showTime", rs.getObject("show_time"));
					doc.addField("gmtLastLogin", rs.getObject("gmt_last_login"));
					doc.addField("gmtCreated", rs.getObject("gmt_created"));
					doc.addField("gmtModified", rs.getObject("gmt_modified"));
					doc.addField("numPass", rs.getObject("num_pass"));
					docs.add(doc);
				}
				
			}
		});
		for(SolrInputDocument doc:docs){
			parseAreaCode(doc);
			parseIndustryCode(doc);
		}
		return docs;
	}
	
	private void parseIndustryCode(SolrInputDocument doc) {
		String code=String.valueOf(doc.getFieldValue("industryCode"));
		if(StringUtils.isNotEmpty(code)){
			doc.addField("industryCode4", substringCode(code, 4));
			doc.addField("industryCode8", substringCode(code, 8));
			doc.addField("industryCode12", substringCode(code, 12));
		}
	}

	private void parseAreaCode(SolrInputDocument doc) {
		String code=String.valueOf(doc.getFieldValue("areaCode"));
		if(StringUtils.isNotEmpty(code)){
			doc.addField("areaCodeGJ", substringCode(code, 4));
			doc.addField("areaCodeSF", substringCode(code, 8));
			doc.addField("areaCodeCS", substringCode(code, 12));
			doc.addField("areaCodeQX", substringCode(code, 16));
		}
	}
	
	private String substringCode(String code, int length){
		if(code.length()>=length){
			return  code.substring(0, length);
		}
		return code;
	}

	private Long resetStart(SolrInputDocument doc){
		Date d=(Date) doc.getFieldValue("gmtModified");
		return d.getTime();
	}
	

	
	public static void main(String[] args) {
		SolrUpdateUtil.getInstance().init("file:/usr/tools/config/search/search.properties");
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		
		String start="2012-07-01 00:00:00";
		String end ="2012-10-10 00:00:00";
		
		IndexCompanyTask task=new IndexCompanyTask();
		try {
//			System.out.println(task.idxReq(DateUtil.getDate(start, FORMATE).getTime(), DateUtil.getDate(end, FORMATE).getTime()));
			task.idxPost(DateUtil.getDate(start, FORMATE).getTime(), DateUtil.getDate(end, FORMATE).getTime());
//			task.optimize();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
