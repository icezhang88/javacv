package com.nieyue.dao;

import com.nieyue.bean.Account;

/**
 * 账户数据库接口
 * @author yy
 *
 */
public interface AccountDao extends BaseDao<Account> {
	/**
	 * 登录
	 */
	 Account loginAccount(String adminName, String password, Long accountId);
}
