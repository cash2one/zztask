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

import com.zz91.task.common.ZZTask;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IInsertUpdateHandler;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;

/**
 * @author 黄怀清 2012-11-14 特定来源公司信息恢复
 */
public class CompfileDataRecoveryTask implements ZZTask {
	Logger LOG = Logger.getLogger(CompfileDataRecoveryTask.class);

	final static String ADMINDB = "ep";

	@Override
	public boolean init() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		final Integer limit = 10;
		// 当前版本时间
		Date versionTime = new Date();
		// 注册来源
		String[] registerCode = new String[] { "14", "15", "16", "17", "18" };
		for (int i = 0; i < registerCode.length; i++) {
			// 查询后返回的最大一次id
			final Integer[] pageLastId = new Integer[] { 0 };
			// 查询出部分公司进行处理

			do {
				StringBuffer selectSql = new StringBuffer(
						"SELECT cp.* FROM `comp_profile` cp "
								+ "inner join comp_account ca "
								+ "on ca.cid=cp.id "
								+ "WHERE member_code_block='10001002' and register_code='"
								+ registerCode[i] + "' ");

				selectSql.append("and cp.id > " + pageLastId[0]);
				selectSql.append(" order by cp.id asc ");
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
											m.put(rsmd.getColumnLabel(i + 1),
													rs.getObject(rsmd
															.getColumnLabel(i + 1)));
										} catch (Exception e) {
											new Exception(e.getMessage());
										}
									}
									list.add(m);
									// 设置当前页最大id
									pageLastId[0] = Integer.parseInt(m
											.get("id").toString());
								}
							}
						});

				if (list.size() <= 0) {
					break;
				}
				for (Map<String, Object> map : list) {
					dataHandle(map, versionTime);
				}

				// System.out.println(pageLastId[0]);
			} while (true);
		}

		return true;
	}

	/**
	 * 公司结果集处理
	 * 
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	public boolean dataHandle(final Map<String, Object> map,
			final Date versionTime) throws SQLException {
		// 生成备份数据sql
		StringBuffer insertSql = new StringBuffer(
				"insert into comp_profile_bak(");
		StringBuffer insertSqlValue = new StringBuffer("values(");
		final Set<String> set = map.keySet();
		int i = 1;
		for (String key : set) {
			insertSql.append(key);
			insertSqlValue.append("?");
			if (i == set.size()) {
				// 若是最后一个则合并sql
				insertSql.append(",gmt_version) ");
				insertSqlValue.append(",?)");
				insertSql.append(insertSqlValue.toString());
			} else {
				insertSql.append(",");
				insertSqlValue.append(",");
			}

			i++;
		}

		// 插入备份数据
		DBUtils.insertUpdate(ADMINDB, insertSql.toString(),
				new IInsertUpdateHandler() {
					@Override
					public void handleInsertUpdate(PreparedStatement stat)
							throws SQLException {
						int j = 1;
						for (String key : set) {
							stat.setObject(j, map.get(key));
							j++;
						}
						stat.setLong(set.size() + 1, versionTime.getTime());

						stat.execute();
					}
				});

		// 删除该公司的产品信息
		String susql = "update trade_supply set del_status=1 where cid=? ";
		DBUtils.insertUpdate(ADMINDB, susql,
				new IInsertUpdateHandler() {

					@Override
					public void handleInsertUpdate(PreparedStatement stat)
							throws SQLException {
						stat.setString(1, map.get("id").toString());

						stat.execute();
					}
				});
		//恢复该公司的删除状态
		String sql = "update comp_profile set member_code_block='' where id=?";
		DBUtils.insertUpdate(ADMINDB, sql,
				new IInsertUpdateHandler() {

					@Override
					public void handleInsertUpdate(PreparedStatement stat)
							throws SQLException {
						stat.setString(1, map.get("id").toString());

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
		CompfileDataRecoveryTask cp=new CompfileDataRecoveryTask();
		try {
			cp.exec(new Date());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("捉到了");
		}
	}

}
