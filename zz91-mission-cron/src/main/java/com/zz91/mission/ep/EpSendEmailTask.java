/**
 * 
 */
package com.zz91.mission.ep;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.zz91.task.common.ZZTask;
import com.zz91.util.Assert;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.mail.MailUtil;

/**
 * @author root 环保网任务邮件
 */
public class EpSendEmailTask implements ZZTask {

	Logger LOG = Logger.getLogger(EpSendEmailTask.class);
	final static String DATE_FORMAT = "yyyy-MM-dd";
	final static String DB = "ep";
	final static int sendCountEveryDay = 1000;
	final static int TIMEOUT = 10000;
	final static String LOCAL_URL = "http://test.huanbao.com:8080";
	final static String PRODUCTION_URL = "http://www.huanbao.com";

	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		sendResetPwdEmail();
		return true;
	}

	/**
	 * 每天定时发送1000封密码重置邮件给阿里巴巴导过来的客户,163.com,126.com,qq.com
	 */
	private void sendResetPwdEmail() {
		// 查询今天需要发送的500封邮件的邮箱
		Integer maxSentId = queryMaxSentId();
		String sql = "select ca.id, ca.account, ca.email from comp_account ca inner join comp_profile cp on cp.id=ca.cid where cp.register_code='2' and ca.id>"
				+ maxSentId
				+ " and (ca.email like '%@163.com' or ca.email like '%@126.com' or ca.email like '%@qq.com' ) order by ca.id limit "
				+ sendCountEveryDay;
		final String[] emailArr = new String[sendCountEveryDay];
		final String[] accountArr = new String[sendCountEveryDay];
		final int[] uidArr = new int[sendCountEveryDay];
		DBUtils.select(DB, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				int i = 0;
				while (rs.next()) {
					emailArr[i] = rs.getString("email");
					accountArr[i] = rs.getString("account");
					uidArr[i] = rs.getInt("id");
					i++;
				}
			}
		});
		maxSentId = uidArr[sendCountEveryDay - 1];
		// 将最大发送ID记录到表tmp_table中
		String updateSql = "update tmp_table set max_id=" + maxSentId;
		DBUtils.insertUpdate(DB, updateSql);
		for (int i = 0; i < emailArr.length; i++) {
			// 生成重置密码链接
			String url = generateUrl(emailArr[i], accountArr[i], uidArr[i]);
			// 发送邮件
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("account", accountArr[i]);
			map.put("email", emailArr[i]);
			map.put("url", url);
			String title = "2011年中国环保行业优质客户和项目名录免费送";
			String template = "ep-task-resetPwdEmail";
			LOG.info("开始发送邮件,收件邮箱:" + emailArr[i] + ",UID为:" + uidArr[i]);
			sendEmail(map, emailArr[i], title, template);
			LOG.info("邮件发送完成,收件邮箱:" + emailArr[i] + ",UID为:" + uidArr[i]);

		}
	}

	private Integer queryMaxSentId() {
		String sql = "select * from tmp_table";
		final Integer[] maxUId = new Integer[1];
		DBUtils.select(DB, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					maxUId[0] = rs.getInt("max_id");
				}
			}
		});
		return maxUId[0];
	}

	private void sendEmail(Map<String, Object> map, String receiveMail,
			String title, String template) {
		
//		MailUtil.getInstance().sendMail(title, receiveMail, "ep", template,
//				map, MailUtil.PRIORITY_HEIGHT);
		MailUtil.getInstance().sendMail(title, receiveMail, "ep", template,
				map, MailUtil.PRIORITY_HEIGHT);
	}

	private String generateUrl(String email, String account, Integer uid) {
		Assert.notNull(email, "email can not be null");
		// 生成KEY
		String key = null;
		key = UUID.randomUUID().toString();

		// 往ep.reset_pwd表中插入重置密码请求记录
		String sql = "insert into reset_pwd(account, uid, email, auth_key, gmt_created, gmt_modified) values"
				+ "('"
				+ account
				+ "',"
				+ uid
				+ ",'"
				+ email
				+ "','"
				+ key
				+ "',now(),now())";
		boolean result = DBUtils.insertUpdate(DB, sql);
		// 生成URL并返回
		String url = null;
		if (result) {
			url = PRODUCTION_URL + "/user/validateKeyIsValidForSetPwd.htm?key="
					+ key;
			// url = LOCAL_URL + "/user/validateKeyIsValidForSetPwd.htm?key=" +
			// key;
		}
		return url;
	}

	@Override
	public boolean init() throws Exception {
		return false;
	}

}