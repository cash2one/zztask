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
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IInsertUpdateHandler;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.lang.StringUtils;
/**
 * @author 黄怀清
 * 2012-10-16
 * 内容质量提升方案,修改求购信息
 */
public class TradeBuyDataUpdateTask implements ZZTask {
	Logger LOG = Logger.getLogger(TradeBuyDataUpdateTask.class);

	final static String ADMINDB = "ep";
	
	@Override
	public boolean init() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean exec(final Date baseDate) throws Exception {
		final Integer limit=10;
		//查询后返回的最大一次id
				final Integer[] pageLastId=new Integer[]{0};
			do {
				StringBuffer selectSql=new StringBuffer("SELECT * FROM trade_buy ts ");
				selectSql.append("where ts.del_status=0 and ts.check_status=1 ");
//				if(baseDate!=null){
//				selectSql.append(" and date_format(ts.gmt_modified,'%Y-%m-%d') = date_add(date_format('" 
//						+ DateUtil.toString(baseDate, "yyyy-MM-dd") + "','%Y-%m-%d'),INTERVAL -1 DAY) ");
//			}
				selectSql.append("and ts.id > " +pageLastId[0]);
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
					dataHandle(map);
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
	public boolean dataHandle(Map<String,Object> map) throws SQLException {
		//去链接正则
		String regex="(http://)?\\w+[.]\\w+[.]\\w+";		

			//产品描述
			String details = map.get("details")==null?"":map.get("details").toString();
			
			if(!StringUtils.isEmpty(details)){
				if(details.startsWith("http")){
					details = "";
				}else{
					details = Jsoup.clean(details, Whitelist.simpleText());
					details = details.replaceAll(regex, "");
					details = details.replaceAll("(www.|WWW.|.com|.COM|.cn|.CN|.net|.NET|.cc|.CC|.org|.ORG|.edu|.EDU|.goepe|.GOEPE)", "");

				}
			}
			String oldStr=map.get("details")==null?"":map.get("details").toString();
			if(!oldStr.equals(details)){
				//更新一条数据
				map.put("details", details);
				return dataUpdate(map);
				
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
	public boolean dataUpdate(final Map<String,Object> map) throws SQLException {
		//生成备份数据sql
		StringBuffer insertSql=new StringBuffer("insert into trade_buy_bak(");
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
					stat.setLong(set.size()+1, new Date().getTime());
					
					stat.execute();
				} catch (Exception e) {
					e.printStackTrace();
					LOG.info(">>>>>>>>> >>>>>>>插入备份数据失败!");
				}
				
			}
		});
		
		
		//更新原数据
		StringBuffer updateSql=new StringBuffer("update trade_buy set details=? where id=?");
		DBUtils.insertUpdate(ADMINDB, updateSql.toString(), new IInsertUpdateHandler() {
			
			@Override
			public void handleInsertUpdate(PreparedStatement stat)
					throws SQLException {
				try {
					stat.setString(1, map.get("details")==null?null:map.get("details").toString());
					stat.setInt(2, Integer.parseInt(map.get("id").toString()));
					
					stat.execute();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		return false;
	}
	@Override
	public boolean clear(Date baseDate) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
	
	public static void main(String[] args) {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		TradeBuyDataUpdateTask tb=new TradeBuyDataUpdateTask();
		try {
			tb.exec(null);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("捉到了");
		}
//		String details="丁腈胶粉  丁腈胶粉价格  沈阳丁腈胶粉技术沈阳地区丁腈胶粉生产技术好，产品质量高。河北鸿运再生胶厂免费提供使用丁腈胶粉技术支持或配方调整，也可提供鸿运制品部成熟实验配方供参考。丁腈胶粉：河北鸿运再生胶厂生产的丁腈胶粉属于流动性粉剂，粒子平均大小为120μm，比重在0.93左右，粘度中等；具有耐热、耐寒、耐磨、防滑、耐油、耐溶剂、揉曲性强等特点，可防止增塑剂渗移，橡胶制品压缩永久变形小，长时间保持原有材料的性能；明显提高橡胶物理性能和抗化学品性能；相溶性极好，聚合物的熔体稳定性极高，提高橡塑合金制品的加工性能。注塑，挤塑，压延，吹塑等橡塑合成制品均可使用。沈阳地区丁腈胶粉生产技术好，产品质量高。河北鸿运再生胶厂免费提供使用丁腈胶粉技术支持或配方调整，也可提供鸿运制品部成熟实验配方供参考。LJH http://www.hsxjw.com/hy/rubber24.html再生胶供应 再生胶行情 乳胶再生胶 丁腈再生胶 丁基再生胶  异戊二烯再生胶 三元乙丙再生胶 再生胶胶粉";
//		System.out.println(details.replaceAll("(www|.com|.cn|.net|.cc|.org|.edu|.goepe)", ""));
				
	}

}
