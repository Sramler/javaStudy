package com.tymls.sso.service;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.tymls.sso.pojo.GeneralResult;
import com.tymls.sso.pojo.TbUser;

public class UserServiceTest {
	
	
	private ApplicationContext context;
		
	@Before
	public void setUp() throws Exception {
		context = new ClassPathXmlApplicationContext(new String[]{"classpath:spring/applicationContext-service.xml","classpath:spring/applicationContext-trans.xml","classpath:spring/applicationContext-redis.xml","classpath:spring/applicationContext-dao.xml"});
	}
	
	UserService userService = (UserService) context.getBean("userServiceImpl");

	@Test
	public void testRegister() {
		
		TbUser user = new TbUser();
		user.setUsername("dongkang");
		user.setPassword("123456");
		user.setEmail("8151018732@qq.com");
		
		GeneralResult result   = userService.register(user);
		System.out.println(result.getMsg());
		
	}
	
/*	GeneralResult checkData(String data, int type);
	GeneralResult register(TbUser user);
	GeneralResult login(String username, String password);
	GeneralResult getUserByToken(String token);*/
	@Test
	public void testCheckData() {
		
		
	}
	@Test
	public void testLogin() {
		
	}
	@Test
	public void testGetUserByToken() {
		
	}


}
