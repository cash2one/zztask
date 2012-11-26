package com.zz91.mission.kl91;

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
/**
 * @author 伍金成,更新kl91公司solr
 *
 */
public class IndexProductsTask extends AbstractIdxTask{
	
	final static String DB="kl91";
	final static int LIMIT=25;
	final static String MODEL="oldkl91Pro";
	final static int RESET_LIMIT=5000;
	
	@Override
	public Boolean idxReq(Long start, Long end) throws Exception {
		StringBuffer sql = new StringBuffer();
		sql.append("select count(*) from products ");
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
			//System.out.println(">>>>>"+docsize+">>>>>>"+begin);
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
		sql.append("id,cid,products_category_code,type_code,title,details,details_query,checked_flag,deleted_flag,")
			.append("impt_flag,publish_flag,area_code,price_unit,quantity,quantity_unit,color,location,level,shape,")
			.append("useful,tags,tags_admin,pic_cover,min_price,max_price,num_inquiry,num_view,day_expired,show_time,")
			.append("gmt_post,gmt_refresh,gmt_expired,gmt_check,gmt_created,gmt_modified,old_id,checked_person,checked_remark,search_key ");
		sql.append("from products");
		sqlwhere(sql, start, end);
		sql.append(" order by gmt_modified asc limit ").append(begin).append(",").append(LIMIT);
		DBUtils.select(DB, sql.toString(), new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				SolrInputDocument doc=null;
				while(rs.next()){
					doc = new SolrInputDocument();
					doc.addField("id", rs.getObject("id"));
					doc.addField("cid", rs.getObject("cid"));
					doc.addField("productsCategoryCode", rs.getObject("products_category_code"));
					doc.addField("typeCode", rs.getObject("type_code"));
					doc.addField("title", rs.getObject("title"));
					doc.addField("details",rs.getObject("details"));
					doc.addField("detailsQuery", rs.getObject("details_query"));
					doc.addField("checkedFlag", rs.getObject("checked_flag"));
					doc.addField("deletedFlag", rs.getObject("deleted_flag"));
					doc.addField("imptFlag", rs.getObject("impt_flag"));
					doc.addField("publishFlag", rs.getObject("publish_flag"));
					doc.addField("areaCode", rs.getObject("area_code"));
					doc.addField("priceUnit", rs.getObject("price_unit"));
					doc.addField("quantity", rs.getObject("quantity"));
					doc.addField("quantityUnit", rs.getObject("quantity_unit"));
					doc.addField("color", rs.getObject("color"));
					doc.addField("location", rs.getObject("location"));
					doc.addField("level", rs.getObject("level"));
					doc.addField("shape", rs.getObject("shape"));
					doc.addField("useful", rs.getObject("useful"));
					doc.addField("tags", rs.getObject("tags"));
					doc.addField("tagsAdmin", rs.getObject("tags_admin"));
					doc.addField("picCover", rs.getObject("pic_cover"));
					doc.addField("minPrice", rs.getObject("min_price"));
					doc.addField("maxPrice", rs.getObject("max_price"));
					doc.addField("numInquiry", rs.getObject("num_inquiry"));
					doc.addField("numView", rs.getObject("num_view"));
					doc.addField("dayExpired", rs.getObject("day_expired"));
					doc.addField("showTime", rs.getObject("show_time"));
					doc.addField("gmtPost", rs.getObject("gmt_post"));
					doc.addField("gmtRefresh", rs.getObject("gmt_refresh"));
					doc.addField("gmtExpired", rs.getObject("gmt_expired"));
					doc.addField("gmtCheck", rs.getObject("gmt_check"));
					doc.addField("gmtCreated", rs.getObject("gmt_created"));
					doc.addField("gmtModified", rs.getObject("gmt_modified"));
					doc.addField("searchKey", rs.getObject("search_key"));
					docs.add(doc);
				}
				
			}
		});
		for(SolrInputDocument doc:docs){
			parseCategory(doc);
			parseComp(doc, (Integer) doc.getFieldValue("cid"));
		}
		return docs;
	}
	
	private void parseComp(SolrInputDocument doc, Integer cid) {
		final Map<String, Object> result=new HashMap<String, Object>();
		StringBuffer sql = new StringBuffer();
			sql.append("select company_name,membership_code,account,domain,")
			.append("membership_code,account,area_code,domain from company where id= ")
			.append(cid);
		DBUtils.select(DB, sql.toString() ,  new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					result.put("companyName", rs.getObject("company_name"));
					result.put("membershipCode",rs.getObject("membership_code"));
					result.put("account", rs.getObject("account"));
					result.put("domain", rs.getObject("domain"));
					result.put("careaCode", rs.getObject("area_code"));
				}
			}
		});
		for(String k:result.keySet()){
			doc.addField(k, result.get(k));
		}
	}

	private void parseCategory(SolrInputDocument doc) {
		String code=String.valueOf(doc.getFieldValue("productsCategoryCode"));
		if(StringUtils.isNotEmpty(code)){
			doc.addField("productTypeCode4", substringCode(code, 4));
			doc.addField("productTypeCode8", substringCode(code, 8));
			doc.addField("productTypeCode12", substringCode(code, 12));
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
		String end ="2012-10-16 00:00:00";
		
		IndexProductsTask task=new IndexProductsTask();
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
