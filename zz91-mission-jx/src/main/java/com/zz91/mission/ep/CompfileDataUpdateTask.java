package com.zz91.mission.ep;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import com.zz91.task.common.ZZTask;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IInsertUpdateHandler;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.lang.StringUtils;
/**
 * @author 黄怀清
 * 2012-10-10
 * 内容质量提升方案,修改公司信息
 */
public class CompfileDataUpdateTask implements ZZTask {

	Logger LOG = Logger.getLogger(CompfileDataUpdateTask.class);

	final static String ADMINDB = "ep";
	
	@Override
	public boolean init() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		final Integer limit=10;
		//当前版本时间
		Date versionTime=new Date();
		//查询后返回的最大一次id
		final Integer[] pageLastId=new Integer[]{0};
			//查询出部分公司进行处理
			
			do {
				StringBuffer selectSql=new StringBuffer("SELECT cp.* FROM comp_profile cp ");
				selectSql.append("where cp.del_status=0 ");
//				if(baseDate!=null){
//					selectSql.append(" and date_format(cp.gmt_modified,'%Y-%m-%d') = date_add(date_format('" 
//							+ DateUtil.toString(baseDate, "yyyy-MM-dd") + "','%Y-%m-%d'),INTERVAL -1 DAY) ");
//				}
				selectSql.append("and cp.id > " +pageLastId[0]);
				selectSql.append(" order by id asc ");
				selectSql.append("LIMIT "+ limit);
				
				final List<Map<String,Object>> list=new ArrayList<Map<String,Object>>();
				//查询数据
				DBUtils.select(ADMINDB, selectSql.toString(), new IReadDataHandler(){
					@Override
					public void handleRead(ResultSet rs) throws SQLException {
						
						//处理结果集
						while(rs.next()){
							ResultSetMetaData rsmd=rs.getMetaData();
							Integer colNum=rsmd.getColumnCount();
							Map<String,Object> m=new HashMap<String, Object>();
							//生成备份数据sql
							for (int i = 0; i < colNum; i++) {
								try {
									m.put(rsmd.getColumnLabel(i+1), rs.getObject(rsmd.getColumnLabel(i+1)));
								} catch (Exception e) {
									new Exception(e.getMessage());
								}
							}							
							list.add(m);
							//设置当前页最大id
							pageLastId[0]=Integer.parseInt(m.get("id").toString());
						}
					}
				});
				
				if(list.size() <=0){
					break;
				}
				for(Map<String,Object> map: list){
					dataHandle(map,versionTime);
				}
				
				//System.out.println(pageLastId[0]);
			} while (true);
			
				return true;
	}
	

	/**
	 * 公司结果集处理
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	public boolean dataHandle(Map<String,Object> map, final Date versionTime) throws SQLException {
		//去链接正则
		String regex="(http://)?\\w+[.]\\w+[.]\\w+";
		//String regex="(http://)?\\w+[.]\\w+[.]\\w+(([/s,\\?,=,&,.,-]?[\\w]?)?)+";
		
		//公司名称
		String name = map.get("name").toString();
		//公司简介
		String detailsQuery= map.get("details_query")==null?"":map.get("details_query").toString();
		Integer id = Integer.parseInt(map.get("id").toString());
		
		
		if(!StringUtils.isEmpty(detailsQuery)){
			if(detailsQuery.startsWith("http")){
				detailsQuery = "";
			}else{
				detailsQuery = Jsoup.clean(detailsQuery, Whitelist.simpleText());
				detailsQuery = detailsQuery.replaceAll(regex, "");
				detailsQuery = detailsQuery.replaceAll("(www.|WWW.|.com|.COM|.cn|.CN|.net|.NET|.cc|.CC|.org|.ORG|.edu|.EDU|.goepe|.GOEPE)", "");
				detailsQuery = detailsQuery.replaceAll("huanbao","huanbao.com");
			}
		}
			
			//导入信息
		if(id<50000000){
			//联系人
			final String[] contacts=new String[]{};
			//联系电话
			final String[] phone=new String[]{};	
			//主营产品
			String mainProductSupply = map.get("main_product_supply")==null?"":map.get("main_product_supply").toString();
			
			if (StringUtils.isEmpty(name) || name.equals("个体经营")
					|| StringUtils.isEmpty(detailsQuery)) {
				// 查询账户信息(联系人,联系电话)
				String sql = "select ca.phone,ca.name as contacts from comp_account ca where cid="
						+ id;
				DBUtils.select(ADMINDB, sql.toString(), new IReadDataHandler() {
	
					@Override
					public void handleRead(ResultSet accountRS) throws SQLException {
						if (accountRS.next()) {
							contacts[0] = accountRS.getString("contacts");
							phone[0] = accountRS.getString("phone");
						}
	
					}
				});
			}
	
			// 公司名
			if (StringUtils.isEmpty(name) || name.equals("个体经营")) {
				if (contacts.length>0) {
					name = "个体经营-" + contacts;
				} else {
					name = "个体经营";
				}
			}
			// 公司简介为空
			if (StringUtils.isEmpty(detailsQuery)) {
	
				StringBuffer jianjie = new StringBuffer();
	
				jianjie.append(name + "是中国环保注册会员");
				if (!StringUtils.isEmpty(mainProductSupply)) {
					jianjie.append("，该公司主营产品：" + mainProductSupply);
				}
				if (contacts.length>0) {
					jianjie.append("，联系人：" + contacts);
				}
				if (phone.length>0) {
					jianjie.append("，联系电话：" + phone);
				}
				jianjie.append("。欢迎与" + name
						+ "联系洽谈合作。联系时请说明是在中国环保网huanbao.com看到的信息");
	
				detailsQuery = jianjie.toString();
			}
				
		}
			
		//有更新操作才更新
		String oldStr=map.get("details_query")==null?"":map.get("details_query").toString();
		if(!name.equals(map.get("name").toString())||!detailsQuery.equals(oldStr)){
			//更新一条数据
			map.put("name", name);
			map.put("details_query", detailsQuery);
			return dataUpdate(map, versionTime);
		}else{
			return false;
		}
	}
	
	/**
	 * 数据操作
	 * @param rs
	 * @param name
	 * @param detailsQuery
	 * @return
	 * @throws SQLException
	 */
	public boolean dataUpdate(final Map<String,Object> map,final Date versionTime) throws SQLException {
		//生成备份数据sql
		StringBuffer insertSql=new StringBuffer("insert into comp_profile_bak(");
		StringBuffer insertSqlValue=new StringBuffer("values(");
		final Set<String> set = map.keySet();
		int i=1;
		for(String key:set){
			insertSql.append(key);
			insertSqlValue.append("?");
			if(i==set.size()){
				//若是最后一个则合并sql
				insertSql.append(",gmt_version) ");
				insertSqlValue.append(",?)");
				insertSql.append(insertSqlValue.toString());
			}else{
				insertSql.append(",");
				insertSqlValue.append(",");
			}
			
			i++;
		}
		
		//插入备份数据
		DBUtils.insertUpdate(ADMINDB, insertSql.toString(), new IInsertUpdateHandler() {
			@Override
			public void handleInsertUpdate(PreparedStatement stat)
					throws SQLException {
					int j=1;
					for(String key:set){
						stat.setObject(j, map.get(key));
						j++;
					}
					stat.setLong(set.size()+1, versionTime.getTime());
					
					stat.execute();
			}
		});
		
		
		//更新原数据
		StringBuffer updateSql=new StringBuffer("update comp_profile set name=?,details_query=? where id=?");
		DBUtils.insertUpdate(ADMINDB, updateSql.toString(), new IInsertUpdateHandler() {
			
			@Override
			public void handleInsertUpdate(PreparedStatement stat)
					throws SQLException {
					stat.setString(1, map.get("name").toString());
					stat.setString(2, map.get("details_query")==null?null:map.get("details_query").toString());
					stat.setInt(3, Integer.parseInt(map.get("id").toString()));
					
					stat.execute();
			}
		});
		
		return true;
	}
	
	@Override
	public boolean clear(Date baseDate) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	public static void main(String[] args) {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		CompfileDataUpdateTask cp=new CompfileDataUpdateTask();
		try {
			cp.exec(new Date());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("捉到了");
		}
		//String regex="((\\^huanbao.com)?|.com)";
		//String str="要huanbao.com的huanbao.com";
		//System.out.println(str.replaceAll(regex, ""));
		//System.out.println();
	}
}
