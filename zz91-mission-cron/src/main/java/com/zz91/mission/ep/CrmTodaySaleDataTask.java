/**
 * 
 */
package com.zz91.mission.ep;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import com.zz91.task.common.ZZTask;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.domain.Param;
import com.zz91.util.lang.StringUtils;

/**
 * @author root
 * 数据统计和客户分配(先统计当前客户今天销售额,再分配今天新导入客户)
 */
public class CrmTodaySaleDataTask implements ZZTask {

	Logger LOG=Logger.getLogger(CrmTodaySaleDataTask.class);
	
	final static String DATE_FORMAT = "yyyy-MM-dd";
	final static String HUANBAO_DEPT_CODE = "100010071000";
	final static String DATA_INPUT_CONFIG = "data_input_config";
	final static String ATUOMATICALLY_STATUS = "automatically_status";
	final static String ATUOMATICALLY_BLOCK = "automatically_block";
	final static String ATUOMATICALLY_ASSIGNED_ACCOUNT = "automatically_assigned_account";
	final static String CRM_COUNT = "crm_count";
	final static String DB="crm";
	final static int TIMEOUT=10000;
	
	
	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		boolean result=false;
		do {
			tongjiSale();
			fenpeiCompany();
			blockCompany();
			result=true;
		}while(false);
		return result;
	}
	
	/**
	 * 统计昨天销售量
	 */
	private void tongjiSale() {
		//查找今天有联系的员工
		String sql = "SELECT sale_account FROM crm_log"
		+ " where date_format(gmt_created,'%Y-%m-%d') = date_format(date_add(now(), interval -1 day),'%Y-%m-%d') group by sale_account";
		DBUtils.select(DB, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()) {
					tongji(rs.getString("sale_account"));
				}
			}
		});
	}
	
	/**
	 * 客户自动掉公海
	 */
	private void blockCompany() {
		final String[] bfalg=new String[1];
		//是否自动掉公海
		String bsql = "SELECT value FROM param where types='"+DATA_INPUT_CONFIG+"' and `key`='"+ATUOMATICALLY_BLOCK+"' and isuse=1 limit 1";
		DBUtils.select(DB, bsql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()) {
					bfalg[0]=rs.getString("value");
				}
			}
		});
		if(bfalg[0]!=null && bfalg[0].equals("1")) {
			//查询掉公海客户
			String sql = "select id,cid from crm_sale_comp where status = 1 and contact_count=0 "
				+"and date_format(gmt_created,'%Y-%m-%d') < date_format(date_add(now(), interval -4 day),'%Y-%m-%d')"
				+" UNION ALL "
				+"select id,cid from crm_sale_comp where status = 1 and contact_count > 0 and date_format(gmt_contact,'%Y-%m-%d')"
				+" < date_format(date_add(now(), interval -31 day),'%Y-%m-%d')";
			final Map<Integer, Integer> map=new HashMap<Integer, Integer>();
			DBUtils.select(DB, sql, new IReadDataHandler() {
				@Override
				public void handleRead(ResultSet rs) throws SQLException {
					while(rs.next()) {
						map.put(rs.getInt("id"), rs.getInt("cid"));
					}
				}
			});
			//掉公海处理
			for (Iterator iterator = map.keySet().iterator();iterator.hasNext();) {
				Integer id = (Integer) iterator.next();
				Integer cid = map.get(id);
				block(id,cid,0);
			}
		}
		//查询客户数超出上线分配用户
		final String[] falg=new String[1];
		String maxUserSql = "SELECT value FROM param where types='"+DATA_INPUT_CONFIG+"' and `key`='"+CRM_COUNT+"' and isuse=1 limit 1";
		DBUtils.select(DB, maxUserSql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()) {
					falg[0]=rs.getString("value");
				}
			}
		});
		if(falg[0]!=null && Integer.valueOf(falg[0])>0) {
			String outSql = "select * from (select sale_account,count(*) as count from crm_sale_comp where status=1 group by sale_account) sale"
			+" where sale.count > "+falg[0];
			final Map<String, Integer> outMap=new HashMap<String, Integer>();
			DBUtils.select(DB, outSql, new IReadDataHandler() {
				@Override
				public void handleRead(ResultSet rs) throws SQLException {
					while(rs.next()) {
						outMap.put(rs.getString("sale_account"), rs.getInt("count")-Integer.valueOf(falg[0]));
					}
				}
			});
			//掉公海处理
			for (Iterator iterator = outMap.keySet().iterator();iterator.hasNext();) {
				String account = (String) iterator.next();
				Integer count = outMap.get(account);
				blockout(account,count);
			}
		}
		//将以前未分配的客户放入公海
		String updateSql = "update crm_company set ctype=2 where ctype=0";
		DBUtils.insertUpdate(DB, updateSql);
	}
	
	/**
	 * 分配新客户
	 */
	private void fenpeiCompany() {
		final String[] falg=new String[1];
		//是否自动分配
		String sql = "SELECT value FROM param where types='"+DATA_INPUT_CONFIG+"' and `key`='"+ATUOMATICALLY_STATUS+"' and isuse=1 limit 1";
		DBUtils.select(DB, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()) {
					falg[0]=rs.getString("value");
				}
			}
		});
		if(falg[0]!=null && falg[0].equals("1")) {
			//查找需要分配销售人
			final List<Param> params= new ArrayList<Param>();
			String paramSql = "SELECT name,`key`,value FROM param where types='"+ATUOMATICALLY_ASSIGNED_ACCOUNT+"' and isuse=1 order by sort asc";
			DBUtils.select(DB, paramSql, new IReadDataHandler() {
				@Override
				public void handleRead(ResultSet rs) throws SQLException {
					while(rs.next()) {
						Param p = new Param();
						p.setName(rs.getString("name"));
						p.setKey(rs.getString("key"));
						p.setValue(rs.getString("value"));
						params.add(p);
					}
				}
			});
			//查找今天注册客户ID
			final List<Integer> userIds= new ArrayList<Integer>();
			//查询可以分配的客户资源
			String userSql = "select id from crm_company where ctype=0 and regist_status=1 and date_format(gmt_input,'%Y-%m-%d')=date_format(now(),'%Y-%m-%d')";
			DBUtils.select(DB, userSql, new IReadDataHandler() {
				@Override
				public void handleRead(ResultSet rs) throws SQLException {
					while(rs.next()) {
						userIds.add(rs.getInt("id"));
					}
				}
			});
			if(params.size() > 0 && userIds.size()>0){
				for (int i = 0; i < userIds.size(); i++) {
					int user = i%params.size();
					//开始分配
					Param param = params.get(user);
					if (param != null) {
						final Integer[] maxId=new Integer[1];
						//是否自动分配
						String maxIdSql = "select MAX(id) as id from crm_sale_comp limit 1";
							DBUtils.select(DB, maxIdSql, new IReadDataHandler() {
								@Override
								public void handleRead(ResultSet rs) throws SQLException {
									while(rs.next()) {
										maxId[0]=rs.getInt("id")+1;
									}
								}
							});
						String insertSql = "INSERT INTO crm_sale_comp("
							+"id,cid,status,sale_type,sale_dept,sale_account,sale_name,gmt_modified,gmt_created)VALUES"
							+"("+maxId[0]+","+userIds.get(i)+",1,0,'"+param.getValue()+"','"+param.getKey()+"','"+param.getName()+"',now(),now())";
						boolean insertResult=DBUtils.insertUpdate(DB, insertSql);
						if (insertResult) {
							String updateSql = "UPDATE crm_company SET ctype=1,sale_comp_id="+maxId[0]+",gmt_modified=now() where id="+userIds.get(i);
							boolean updateResult=DBUtils.insertUpdate(DB, updateSql);
							if (updateResult) {
								sysLog(userIds.get(i), "放入个人库（系统自动分配）", 4,param.getKey(),param.getName(),param.getValue());
							} else {
								LOG.info(">>>>>>>>>>>>>>>>>数据同布失败:"+updateSql);
							}
						} else {
							LOG.info(">>>>>>>>>>>>>>>>>分配客户失败:"+insertSql);
						}
						
					}
				}
			}
			
		}

	}

	@Override
	public boolean init() throws Exception {
		return false;
	}
	
	private void blockout(String account,Integer count) {
		String sql="select id,cid,IFNULL(to_days(now())-to_days(gmt_contact),0)"
				+ " as day_count from crm_sale_comp where status=1 and sale_account='"+account+"' order by day_count desc,gmt_created limit "+count;
		final Map<Integer, Integer> map=new HashMap<Integer, Integer>();
		DBUtils.select(DB, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()) {
					map.put(rs.getInt("id"), rs.getInt("cid"));
				}
			}
		});
		//掉公海处理
		for (Iterator iterator = map.keySet().iterator();iterator.hasNext();) {
			Integer id = (Integer) iterator.next();
			Integer cid = map.get(id);
			block(id,cid,1);
		}
	}
	
	private void block(Integer id, Integer cid,Integer type) {
		String updateSql = "update crm_sale_comp set status=0 ,auto_block=1,gmt_block=now() where id="+id;
		boolean updateResult=DBUtils.insertUpdate(DB, updateSql);
		if (updateResult) {
			String updateSql2 = "update crm_company set ctype=2,block_count=block_count+1, gmt_modified=now() where id="+cid;
			boolean updateResult2=DBUtils.insertUpdate(DB, updateSql2);
			if (type == 0) {
				//自动掉公海（未联系掉公海）
				sysLog(cid, "自动掉公海（未联系掉公海）", 9,"system","系统任务","10001000");
			} else if (type == 1) {
				//自动掉公海（超出个人库人数上限）
				sysLog(cid, "自动掉公海（超出个人库人数上限）", 9,"system","系统任务","10001000");
			}
			if (!updateResult2) {
				LOG.info(">>>>>>>>>>>>>>>>>掉公海处理失败:"+updateSql2);
			}
		}
	}
	
	private void sysLog(Integer id,String message,Integer type, String account, String name, String dept) {
		String sqlStr = "INSERT INTO sys_log (operation, target_id, sale_account, sale_dept, sale_name, "
				+ "sale_ip, details, gmt_created, gmt_modified) VALUES ("
				+"'"+type+"',"+id+",'"+account+"','"+dept+"','"+name+"','192.168.2.178','"+message+"',now(),now())";
		boolean result=DBUtils.insertUpdate(DB, sqlStr);
		if (!result) {
			LOG.info(">>>>>>>>>>>>>>>>>添加系统日志失败:"+sqlStr);
		}
	}
	
	private void tongji(String account) {
		String sql = "select * from (SELECT id,cid,star_old,star,situation,sale_account,sale_name,sale_dept FROM crm_log "
					+" where date_format(gmt_created,'%Y-%m-%d') = date_format(date_add(now(), interval -1 day),'%Y-%m-%d')"
					+" and sale_account = '"+account+"' "
					+" order by gmt_created desc) tmp group by cid";
		DBUtils.select(DB, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				String account = "";
				String saleName = "";
				String saleDept = "";
				int star1_able = 0;
				int star1_disable = 0;
				int star2_able = 0;
				int star2_disable = 0;
				int star3_able = 0;
				int star3_disable = 0;
				int star4_able = 0;
				int star4_disable = 0;
				int star5_able = 0;
				int star5_disable = 0;
				int drag_count = 0;
				int destroy_count = 0;
				while(rs.next()) {
					if (StringUtils.isEmpty(account)) {
						account = rs.getString("sale_account");
					}
					if (StringUtils.isEmpty(saleName)) {
						saleName = rs.getString("sale_name");
					}
					if (StringUtils.isEmpty(saleDept)) {
						saleDept = rs.getString("sale_dept");
					}
					if ("1".equals(rs.getString("star"))) {
						if ("0".equals(rs.getString("situation"))) {
							star1_able++;
						} else {
							star1_disable++;
						}
					} else if("2".equals(rs.getString("star"))) {
						if ("0".equals(rs.getString("situation"))) {
							star2_able++;
						} else {
							star2_disable++;
						}
					} else if("3".equals(rs.getString("star"))) {
						if ("0".equals(rs.getString("situation"))) {
							star3_able++;
						} else {
							star3_disable++;
						}
					} else if("4".equals(rs.getString("star"))) {
						if ("0".equals(rs.getString("situation"))) {
							star4_able++;
						} else {
							star4_disable++;
						}
					} else if("5".equals(rs.getString("star"))) {
						if ("0".equals(rs.getString("situation"))) {
							star5_able++;
						} else {
							star5_disable++;
						}
					} else {
						
					}
					if ("5".equals(rs.getString("star_old"))) {
						if ("5".equals(rs.getString("star"))) {
							drag_count++;
						} else {
							destroy_count++;
						}
					}
				}
				insertTongji(account, saleName, saleDept,
						star1_able, star1_disable, star2_able, star2_disable,
						star3_able, star3_disable, star4_able, star4_disable,
						star5_able, star5_disable, drag_count, destroy_count);
			}
		});
	}
	
	private void insertTongji(String saleAccount,String saleName,String saleDept,
							Integer star1_able,Integer star1_disable,
							Integer star2_able,Integer star2_disable,
							Integer star3_able,Integer star3_disable,
							Integer star4_able,Integer star4_disable,
							Integer star5_able,Integer star5_disable,
							Integer drag_count,Integer destroy_count){
		String sql="INSERT INTO crm_contact_statistics(star1_able,star1_disable,star2_able,star2_disable,star3_able,star3_disable,"
				+"star4_able,star4_disable,star5_able,star5_disable,drag_order_count,destroy_order_count,"
				+"sale_account,sale_dept,sale_name,gmt_target,gmt_created,gmt_modified) VALUES ("
				+star1_able+","+star1_disable+","+star2_able+","+star2_disable+","+star3_able+","+star3_disable+","
				+star4_able+","+star4_disable+","+star5_able+","+star5_disable+","+drag_count+","+destroy_count+",'"
				+saleAccount+"','"+saleDept+"','"+saleName+"',date_format(date_add(now(), interval -1 day),'%Y-%m-%d'),now(),now()"
				+")";
		boolean result=DBUtils.insertUpdate(DB, sql);
		if (!result) {
			LOG.info(">>>>>>>>>>>>>>>>>统计销售联系量数据失败:"+sql);
		}
	}
}