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

import org.apache.log4j.Logger;

import com.zz91.task.common.ZZTask;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IInsertUpdateHandler;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.lang.StringUtils;

/**
 * @author 黄怀清 2012-11-12 公司产业链数据关联
 */
public class IndustryChainRelationTask implements ZZTask {
	Logger LOG = Logger.getLogger(IndustryChainRelationTask.class);

	final static String ADMINDB = "ep";

	@Override
	public boolean init() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		final Integer limit = 10;
		// 查询后返回的最大一次id
		final Integer[] pageLastId = new Integer[] { 0 };
		// 读取产业链
		List<Map<String, Object>> industryChain = readIndustryChain();

		// 查询出部分公司进行处理
		do {
			StringBuffer selectSql = new StringBuffer(
					"SELECT cp.* FROM comp_profile cp ");
			selectSql.append("where cp.del_status=0 ");
			// if(baseDate!=null){
			// selectSql.append(" and date_format(cp.gmt_modified,'%Y-%m-%d') = date_add(date_format('"
			// + DateUtil.toString(baseDate, "yyyy-MM-dd") +
			// "','%Y-%m-%d'),INTERVAL -1 DAY) ");
			// }
			selectSql.append("and cp.id > " + pageLastId[0]);
			selectSql.append(" order by id asc ");
			selectSql.append("LIMIT " + limit);

			final List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			// 查询数据
			DBUtils.select(ADMINDB, selectSql.toString(),
					new IReadDataHandler() {
						@Override
						public void handleRead(ResultSet rs)
								throws SQLException {

							// 处理结果集
							while (rs.next()) {
								ResultSetMetaData rsmd = rs.getMetaData();
								Integer colNum = rsmd.getColumnCount();
								Map<String, Object> m = new HashMap<String, Object>();
								// 生成备份数据sql
								for (int i = 0; i < colNum; i++) {
									try {
										m.put(rsmd.getColumnLabel(i + 1), rs
												.getObject(rsmd
														.getColumnLabel(i + 1)));
									} catch (Exception e) {
										if (i != 41 && i != 42) {
											new Exception(e.getMessage());
										} else {
											m.put(rsmd.getColumnLabel(i + 1),
													null);
										}
									}
								}
								list.add(m);
								// 设置当前页最大id
								pageLastId[0] = Integer.parseInt(m.get("id")
										.toString());
							}
						}
					});

			if (list.size() <= 0) {
				break;
			}
			for (Map<String, Object> map : list) {
				dataHandle(map, industryChain);
			}

			// System.out.println(pageLastId[0]);
		} while (true);

		return true;
	}

	/**
	 * 读取产业链信息
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<Map<String, Object>> readIndustryChain() throws SQLException {
		String sql = "select * from industry_chain where del_status=0";

		final List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		// 查询数据
		DBUtils.select(ADMINDB, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {

				// 处理结果集
				while (rs.next()) {
					ResultSetMetaData rsmd = rs.getMetaData();
					Integer colNum = rsmd.getColumnCount();
					Map<String, Object> m = new HashMap<String, Object>();
					// 生成备份数据sql
					for (int i = 0; i < colNum; i++) {
						try {
							m.put(rsmd.getColumnLabel(i + 1),
									rs.getObject(rsmd.getColumnLabel(i + 1)));
						} catch (Exception e) {
							new Exception(e.getMessage());
						}
					}
					list.add(m);
				}
			}
		});
		return list;
	}

	/**
	 * 公司结果集处理
	 * 
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	public boolean dataHandle(final Map<String, Object> map,
			List<Map<String, Object>> industryChain) throws SQLException {
		String details = map.get("details") == null ? "" : map.get("details")
				.toString();
		String mainProductSupply = map.get("main_product_supply") == null ? ""
				: map.get("main_product_supply").toString();
		String address = map.get("address") == null ? "" : map.get("address")
				.toString();
		String areaCode = map.get("area_code") == null ? "" : map.get(
				"area_code").toString();
		String provinceCode = map.get("province_code") == null ? "" : map.get(
				"province_code").toString();
		String industryCode = map.get("industry_code") == null ? "" : map.get(
				"industry_code").toString();
		String name = map.get("name") == null ? "" : map.get(
				"name").toString();

		// 全为空则不执行操作
		if (StringUtils.isEmpty(details)
				&& StringUtils.isEmpty(mainProductSupply)
				&& StringUtils.isEmpty(address)
				&& StringUtils.isEmpty(areaCode)
				&& StringUtils.isEmpty(provinceCode)
				&& StringUtils.isEmpty(industryCode)) {
			return false;
		}
		// 匹配所有产业链
		for (final Map<String, Object> icm : industryChain) {
			String icAddress = icm.get("area_name").toString();
			icAddress = icAddress.replaceAll("(省|市|县|区|镇)", "");
			String icAddcode = icm.get("area_code").toString();
			//地区
			if (address.indexOf(icAddress) == -1 && !areaCode.equals(icAddcode)
					&& !provinceCode.equals(icAddcode)&&name.indexOf(icAddress)==-1
					&&details.indexOf(icAddress)==-1) {
				continue;
			}
			
			//标题名称
			if (icm.get("category_name").equals("原水处理设备")) {
				if (details.indexOf(icm.get("category_name").toString()) == -1
						&& mainProductSupply.indexOf(icm.get("category_name")
								.toString()) == -1
						&& !industryCode.equals("10001001")) {
					continue;
				}
			} else if (icm.get("category_name").equals("材料药剂")) {
				if (details.indexOf(icm.get("category_name").toString()) == -1
						&& mainProductSupply.indexOf(icm.get("category_name")
								.toString()) == -1
						&& !industryCode.equals("10001003")) {
					continue;
				}
			} else if (details.indexOf(icm.get("category_name").toString()) == -1
					&& mainProductSupply.indexOf(icm.get("category_name")
							.toString()) == -1) {
				continue;
			}
			// 每家公司属于的产业链不超过3个
			String countSql = "select count(*) totals from company_industry_chain where cid="
					+ map.get("id").toString();
			final Integer[] totals = new Integer[] { 0 };
			DBUtils.select(ADMINDB, countSql, new IReadDataHandler() {
				@Override
				public void handleRead(ResultSet rs) throws SQLException {
					// 处理结果集
					if (rs.next()) {
						totals[0] = rs.getInt("totals");
					}
				}
			});
			if (totals[0] >= 3) {
				break;
			}

			// 插入关联关系
			String sql = "insert into company_industry_chain(cid,chain_id,gmt_created,gmt_modified,del_status)"
					+ " values(?,?,now(),now(),0)";
			DBUtils.insertUpdate(ADMINDB, sql, new IInsertUpdateHandler() {
				@Override
				public void handleInsertUpdate(PreparedStatement stat)
						throws SQLException {
					stat.setInt(1, Integer.parseInt(map.get("id").toString()));
					stat.setInt(2, Integer.parseInt(icm.get("id").toString()));

					stat.execute();
				}
			});
		}

		return true;
	}

	@Override
	public boolean clear(Date baseDate) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	public static void main(String[] args) {
		 DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		 IndustryChainRelationTask iccr=new IndustryChainRelationTask();
		 try {
		 iccr.exec(null);
		 } catch (Exception e) {
		 e.printStackTrace();
		 }
		//System.out.println("浙江省".replaceAll("(省|市|县|区|镇)", ""));
	}
}
