package com.zz91.mission.huanbao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;

import com.zz91.task.common.AbstractIdxTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.search.SolrUtil;

public class IndexCompanyTask extends AbstractIdxTask{
	
	final static String DB="ep";
	final static int LIMIT=25;
	
	final static String MODEL="company";
	final static int IMPORT_ID_SPLIT=50000000;
	final static int RESET_LIMIT=5000;
	
	@Override
	public Boolean idxReq(Long start, Long end) throws Exception {
		StringBuffer sql = new StringBuffer();
		sql.append("select count(*) from comp_profile ");
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
		if(dealCount[0]!=null && dealCount[0]>4){
			return true;
		}
		return false;
	}

	@Override
	public void idxPost(Long start, Long end) throws Exception {
		SolrServer server=SolrUtil.getInstance().getSolrServer(MODEL);
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
		
		
	}
	
	private void sqlwhere(StringBuffer sql, Long start, Long end){
		sql.append(" where gmt_modified >='").append(DateUtil.toString(new Date(start), FORMATE)).append("' ");
		sql.append(" and gmt_modified <='").append(DateUtil.toString(new Date(end), FORMATE)).append("' ");
	}
	
	private List<SolrInputDocument> queryDocs(Long start, Long end, int begin){
		
		final List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
		
		StringBuffer sql=new StringBuffer();
		sql.append("select ");
		sql.append("comp.id,comp.name,comp.member_code,comp.details_query,comp.industry_code,comp.industry_name,comp.business_code,comp.main_buy,")
			.append("comp.main_product_buy,comp.main_supply,comp.main_product_supply,comp.address,comp.province_code,comp.area_code,comp.del_status,comp.gmt_modified,")
			.append("comp.tags,comp.view_count,comp.message_count,comp.main_brand,comp.gmt_created");
		sql.append("from comp_profile comp");
		sqlwhere(sql, start, end);
		sql.append(" order by gmt_modified asc limit ").append(begin).append(",").append(LIMIT);
		DBUtils.select(DB, sql.toString(), new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				SolrInputDocument doc=null;
				while(rs.next()){
					doc = new SolrInputDocument();
					doc.addField("id", rs.getObject("id"));
					doc.addField("name", rs.getObject("name"));
					doc.addField("memberCode", rs.getObject("member_code"));
					doc.addField("detailsQuery", rs.getObject("details_query"));
					doc.addField("industryCode",rs.getObject("industry_code"));
					doc.addField("industryName", rs.getObject("industry_name"));
					doc.addField("businessCode", rs.getObject("business_code"));
					doc.addField("mainBuy", rs.getObject("main_buy"));
					doc.addField("mainProductBuy", rs.getObject("main_product_buy"));
					doc.addField("mainSupply", rs.getObject("main_supply"));
					doc.addField("mainProductSupply", rs.getObject("main_product_supply"));
					doc.addField("address", rs.getObject("address"));
					doc.addField("provinceCode", rs.getObject("province_code"));
					doc.addField("areaCode", rs.getObject("area_code"));
					doc.addField("delStatus", rs.getObject("del_status"));
					doc.addField("gmtModified", rs.getObject("gmt_modified"));
					doc.addField("tags", rs.getObject("tags"));
					doc.addField("viewCount", rs.getObject("view_count"));
					doc.addField("messageCount", rs.getObject("message_count"));
					doc.addField("mainBrand", rs.getObject("main_brand"));
					doc.addField("gmtCreated", rs.getObject("gmt_created"));
					docs.add(doc);
				}
				
			}
		});
		return docs;
	}
	
	private Long resetStart(SolrInputDocument doc){
		Date d=(Date) doc.getFieldValue("gmtModified");
		return d.getTime();
	}
}
