package com.zz91.mission.kl91;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.http.HttpUtils;
/**
 * @author 伍金成：查询昨天登录过的客户，更新登录次数和最后登录时间到crm并且放入公海
 * 1,搜索昨天登录过的客户
 * 2,得到公司的登录时间，登陆次数和id
 * 3,根据登录过的公司id来更新为公海
 */
public class KlCrmNumLoginImportTask implements ZZTask{

	private static String API_HOST="http://admin1949.zz91.com:7130/kl91-admin";
	final static String DB="klcrm";
	
	@Override
	public boolean exec(Date baseDate) throws Exception {
		String from = DateUtil.toString(
				DateUtil.getDateAfterDays(baseDate, -1), "yyyy-MM-dd");
		String to = DateUtil.toString(baseDate, "yyyy-MM-dd");
		int i=0;
		int docs=0;
		
		do{
			List<JSONObject> list = new ArrayList<JSONObject>();
			String responseText = HttpUtils.getInstance().httpGet(API_HOST+"/queryYestodayCompany.htm?from="+from+"&to="+to+"&i="+i*10+"", HttpUtils.CHARSET_UTF8);
			JSONArray js=JSONArray.fromObject(responseText);
			for (Iterator iter = js.iterator(); iter.hasNext();) {
				JSONObject object = (JSONObject) iter.next();
				list.add(object);
			}
			if(list.size()<=0){
				break;
			}
			for(JSONObject object:list){
				updateCompany(object.getInt("id"), object.getString("gmtLastLogin") ,
						object.getInt("numLogin"));
			}
			i++;
			docs=docs+list.size();
		}while(true);
		
		if(i>0){
			throw new Exception("共更新数据"+docs+"条");
		}
		return true;
	}
	
	private void updateCompany(Integer cid,String gmtLogin,Integer loginCount) {
		String sql ="UPDATE crm_company SET gmt_login='"+gmtLogin+"',gmt_modified=now(),login_count="+loginCount+"  where cid="+cid;
		DBUtils.insertUpdate(DB, sql);
		updateCtype(cid);
	}
	
	private void updateCtype(Integer cid) {
		String sql="update crm_company SET ctype=2 where ctype=5 and login_count>0 and cid="+cid+"";
		DBUtils.insertUpdate(DB, sql);
	}
	
	@Override
	public boolean init() throws Exception {
		return false;
	}
	
	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	public static void main(String[] args) throws Exception {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		KlCrmNumLoginImportTask obj = new KlCrmNumLoginImportTask();
		API_HOST = "http://test.kl91.zz91.com:8089";
		Date date = DateUtil.getDate("2008-11-25", "yyyy-MM-dd");
		obj.exec(date);
	}

}
