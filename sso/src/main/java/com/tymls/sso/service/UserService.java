package com.tymls.sso.service;

import com.tymls.sso.pojo.GeneralResult;
import com.tymls.sso.pojo.TbUser;

public interface UserService {
	
	
	
	GeneralResult checkData(String data, int type);
	GeneralResult register(TbUser user);
	GeneralResult login(String username, String password);
	GeneralResult getUserByToken(String token);

}
