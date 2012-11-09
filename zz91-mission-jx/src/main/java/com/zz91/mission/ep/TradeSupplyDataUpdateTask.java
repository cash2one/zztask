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
 * 2012-10-15
 * 内容质量提升方案,修改供应信息
 */
public class TradeSupplyDataUpdateTask implements ZZTask {

	Logger LOG = Logger.getLogger(TradeSupplyDataUpdateTask.class);

	final static String ADMINDB = "ep";
	
	@Override
	public boolean clear(Date baseDate) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean exec(final Date baseDate) throws Exception {
			//查询出部分供应进行处理
			final Integer limit=10;
			final Integer[] pageLastId=new Integer[]{0};
			//当前版本时间
			Date versionTime=new Date();
			do {
				StringBuffer selectSql=new StringBuffer("SELECT * FROM trade_supply ts ");
				selectSql.append("where del_status=0 and check_status=1 ");
//				if(baseDate!=null){
//				selectSql.append(" and date_format(ts.gmt_modified,'%Y-%m-%d') = date_add(date_format('" 
//						+ DateUtil.toString(baseDate, "yyyy-MM-dd") + "','%Y-%m-%d'),INTERVAL -1 DAY) ");
//			}
				
				selectSql.append("and id > " +pageLastId[0]);
				selectSql.append(" order by id asc ");
				selectSql.append("LIMIT "+ limit);
				
				final List<Map<String,Object>> list=new ArrayList<Map<String,Object>>();
				
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
								m.put(rsmd.getColumnLabel(i+1), rs.getObject(rsmd.getColumnLabel(i+1)));
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
	 * 供应结果集处理
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	public boolean dataHandle(Map<String,Object> map, final Date versionTime) throws SQLException {
		//去链接正则
		String regex="(http://)?\\w+[.]\\w+[.]\\w+";
			
			//产品标题
			String title = map.get("title").toString();
			//产品描述
			String details = map.get("details")==null?"":map.get("details").toString();
			final Integer supplyId = Integer.parseInt(map.get("id").toString());
			final String categoryCode=map.get("category_code").toString();
				
			if(!StringUtils.isEmpty(details)){
				if(details.startsWith("http")){
					details = "";
				}else{
					details = Jsoup.clean(details, Whitelist.simpleText());
					details = details.replaceAll(regex, "");
					details = details.replaceAll("(www.|WWW.|.com|.COM|.cn|.CN|.net|.NET|.cc|.CC|.org|.ORG|.edu|.EDU|.goepe|.GOEPE)", "");
				}
			}
			
			//导入信息
			if(supplyId<10000000){
				//产品标题部分
				final StringBuffer str=new StringBuffer("");
				final Map<String,Object> comp=new HashMap<String, Object>();
				//查询该产品公司
				String compSql = "select * from comp_profile where id=" + map.get("cid");
				DBUtils.select(ADMINDB, compSql.toString(), new IReadDataHandler() {
					
					@Override
					public void handleRead(ResultSet rs) throws SQLException {
						if(rs.next()){
							ResultSetMetaData rsmd=rs.getMetaData();
							Integer colNum=rsmd.getColumnCount();
							for (int i = 0; i < colNum; i++) {
								comp.put(rsmd.getColumnLabel(i+1), rs.getObject(rsmd.getColumnLabel(i+1)));
							}							
						}
						
					}
				});
				
				//省份
				if(comp.get("province_code")!=null&&!StringUtils.isEmpty(comp.get("province_code").toString())){
					String sql="select * from sys_area where code='"+ comp.get("province_code") +"'";
					DBUtils.select(ADMINDB, sql, new IReadDataHandler() {
						@Override
						public void handleRead(ResultSet rs) throws SQLException {
							if(rs.next()){
								str.append(rs.getString("name"));
							}
						}
					});
				}
				//地区
				if(comp.get("area_code")!=null&&!StringUtils.isEmpty(comp.get("area_code").toString())){
					String sql="select * from sys_area where code='"+ comp.get("area_code") +"'";
					DBUtils.select(ADMINDB, sql, new IReadDataHandler() {
						@Override
						public void handleRead(ResultSet rs) throws SQLException {
							if(rs.next()){
								str.append(rs.getString("name"));
							}
						}
					});
				}
				//品牌
				String pp = "select * from trade_property WHERE category_code='"+categoryCode+"' and name like '%品牌%'";
				DBUtils.select(ADMINDB, pp, new IReadDataHandler() {
					@Override
					public void handleRead(ResultSet rs) throws SQLException {
						if(rs.next()){
							String sql = "select * from trade_property_value where supply_id=" 
								+ supplyId + " and property_id=" + rs.getInt("id");
							DBUtils.select(ADMINDB, sql, new IReadDataHandler() {
								
								@Override
								public void handleRead(ResultSet rs) throws SQLException {
									if(rs.next()){
										str.append("/" + rs.getString("property_value"));
									}
								}
							});
						}
					}
				});
				//型号
				String xh = "select * from trade_property WHERE category_code='"+categoryCode+"' and name like '%型号%'";
				DBUtils.select(ADMINDB, xh, new IReadDataHandler() {
					@Override
					public void handleRead(ResultSet rs) throws SQLException {
						if(rs.next()){
							String sql = "select * from trade_property_value where supply_id=" 
								+ supplyId + " and property_id=" + rs.getInt("id");
							DBUtils.select(ADMINDB, sql, new IReadDataHandler() {
								
								@Override
								public void handleRead(ResultSet rs) throws SQLException {
									if(rs.next()){
										str.append("/" + rs.getString("property_value"));
									}
								}
							});
						}
					}
				});
				
				if(!StringUtils.isEmpty(str.toString())){
					title = str.toString() + "/" +title;
				}
				
			}
			String oldStr=map.get("details")==null?"":map.get("details").toString();
			//更新一条数据
			if(!map.get("title").toString().equals(title)||!details.equals(oldStr)){
				map.put("title", title);
				map.put("details", details);
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
		StringBuffer insertSql=new StringBuffer("insert into trade_supply_bak(");
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
				try {
					int j=1;
					for(String key:set){
						stat.setObject(j, map.get(key));
						j++;
					}
					stat.setLong(set.size()+1, versionTime.getTime());
					
					stat.execute();
				} catch (Exception e) {
					e.printStackTrace();
					LOG.info(">>>>>>>>> >>>>>>>插入备份数据失败!");
				}
				
			}
		});
		
		
		//更新原数据
		StringBuffer updateSql=new StringBuffer("update trade_supply set title=?,details=? where id=?");
		DBUtils.insertUpdate(ADMINDB, updateSql.toString(), new IInsertUpdateHandler() {
			
			@Override
			public void handleInsertUpdate(PreparedStatement stat)
					throws SQLException {
				try {
					stat.setString(1, map.get("title").toString());
					stat.setString(2, map.get("details").toString());
					stat.setInt(3, Integer.parseInt(map.get("id").toString()));
					
					stat.execute();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		return false;
	}
	@Override
	public boolean init() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
	
	public static void main(String[] args) {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		TradeSupplyDataUpdateTask cp=new TradeSupplyDataUpdateTask();
		try {
			cp.exec(null);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("捉到了");
		}
		
//		String regex="(http://)?\\w+[.]\\w+[.]\\w+(([/s,\\?,=,&,.,-]?[\\w]?)?)+";
//		String str="<?ns = \"urn:schemas-microsoft-com:office:office\"";
//		System.out.println(str.replaceAll(regex, ""));
	}

}
