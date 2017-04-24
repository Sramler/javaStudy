package com.tymls.sso.mapper;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.tymls.sso.pojo.TbUser;

public class TbUserMapperTest {
	
private ApplicationContext context;
	
	
	
	@Before
	public void setUp() throws Exception {
		context = new ClassPathXmlApplicationContext("classpath:spring/applicationContext-dao.xml");
	}
	@Test
	public void testSelectByPrimaryKey() {
		TbUserMapper userMapper =  (TbUserMapper) context.getBean("tbUserMapper");
		TbUser user = userMapper.selectByPrimaryKey(Long.valueOf(10));
		System.err.println(user.toString());
		/*fail("Not yet implemented");*/
	}

}
