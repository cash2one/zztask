/**
 * Copyright 2011 ASTO.
 * All right reserved.
 * Created on 2011-6-3
 */
package com.zz91.mission.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.zz91.mission.BaseMissionTest;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;

/**
 * @author mays (mays@zz91.com)
 *
 * created on 2011-6-3
 */
public class DBAstchg extends BaseMissionTest{

	/**
	 * Test method for {@link com.zz91.mission.front.BaseDataStatisticTask#clear(java.util.Date)}.
	 */
	//@Test
	public void test_apply_auth_user_dusername(){
		final List<UserDto> list=new ArrayList<UserDto>();

		DBUtils.select(
						"ast", 
						"select username, id from auth_user group by username having count(username)>1",
						new IReadDataHandler() {

							@Override
							public void handleRead(ResultSet rs)
									throws SQLException {
								while(rs.next()){
									UserDto user=new UserDto();
									user.setUsername(rs.getString("username"));
									user.setId(rs.getInt("id"));
									list.add(user);
								}
							}
						});

		for (UserDto user:list) {
			
			DBUtils.insertUpdate("ast", "delete from auth_user where username='"+user.getUsername()+"' and id<>"+user.getId());
		}
	}
	
	//@Test
	public void test_apply_auth_user_demail(){
		final List<UserDto> list=new ArrayList<UserDto>();

		DBUtils.select(
						"ast", 
						"select email, id from auth_user group by email having count(email)>1",
						new IReadDataHandler() {

							@Override
							public void handleRead(ResultSet rs)
									throws SQLException {
								while(rs.next()){
									UserDto user=new UserDto();
									user.setUsername(rs.getString("email"));
									user.setId(rs.getInt("id"));
									list.add(user);
								}
							}
						});

		for (UserDto user:list) {
			
			DBUtils.insertUpdate("ast", "delete from auth_user where email='"+user.getUsername()+"' and id<>"+user.getId());
		}
	}
	
	@Test
	public void test_apply_company_account_daccount(){
		final List<UserDto> list=new ArrayList<UserDto>();

		DBUtils.select(
						"ast", 
						"select account, id from company_contacts group by account having count(account)>1",
						new IReadDataHandler() {

							@Override
							public void handleRead(ResultSet rs)
									throws SQLException {
								while(rs.next()){
									UserDto user=new UserDto();
									user.setUsername(rs.getString("account"));
									user.setId(rs.getInt("id"));
									list.add(user);
								}
							}
						});

		for (UserDto user:list) {
			DBUtils.insertUpdate("ast", "delete from company_contacts where account='"+user.getUsername()+"' and id<>"+user.getId());
		}
	}

}
