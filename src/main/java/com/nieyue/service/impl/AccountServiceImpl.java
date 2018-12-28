package com.nieyue.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nieyue.bean.*;
import com.nieyue.exception.CommonRollbackException;
import com.nieyue.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.nieyue.util.MyDESutil;
import com.nieyue.util.MyDom4jUtil;
@Service
public class AccountServiceImpl extends BaseServiceImpl<Account,Long> implements AccountService{
	@Autowired
	RoleService roleService;
	@Autowired
	IntegralService integralService;
	/**
	 * 登录
	 */
	@Override
	public Account loginAccount(String adminName, String password, Long accountId) {
		Account account = null;
		Wrapper<Account> wrapper=new EntityWrapper<>();
	 	Map<String,Object> map=new HashMap<String,Object>();
	 	map.put("phone", adminName);
	 	map.put("password", password==null?null:MyDESutil.getMD5(password));
	 	wrapper.allEq(MyDom4jUtil.getNoNullMap(map));
	 	if(accountId!=null){
			wrapper.andNew().notIn("account_id",accountId);
		}
		//email登录
	 	/*Map<String,Object> map2=new HashMap<String,Object>();
	 	map2.put("email", adminName);
	 	map2.put("password", password==null?null:MyDESutil.getMD5(password));
	 	map2.put("account_id", accountId);
	 	wrapper.orNew().allEq(MyDom4jUtil.getNoNullMap(map2));*/
	 	//wrapper.or().allEq(MyDom4jUtil.getNoNullMap(map2));
	 	List<Account> al = super.list(1,1,null,null,wrapper);
	 	if(al.size()>0){
	 		account=al.get(0);
 			Role role = roleService.load(account.getRoleId());
 			account.setRole(role);
 			account.setLoginDate(new Date());
 			super.update(account);
	 	}

	 	return account;
	}
	@Transactional(propagation=Propagation.REQUIRED)
	@Override
	public boolean add(Account t) {
		boolean b = super.add(t);
		if(!b){
			throw new CommonRollbackException();
		}
		//增加积分
		Integral integral =new Integral();
		integral.setAccountId(t.getAccountId());
		integral.setBaseProfit(0.0);
		integral.setIntegral(0.0);
		integral.setRecharge(0.0);
		integral.setConsume(0.0);
		integral.setCreateDate(new Date());
		integral.setUpdateDate(new Date());
		b=integralService.add(integral);
		if(!b){
			throw new CommonRollbackException();
		}
		return b;
	}
	@Override
	public List<Account> list(int pageNum, int pageSize, String orderName, String orderWay, Wrapper<Account> wrapper) {
				List<Account> rl = super.list(pageNum, pageSize, orderName, orderWay, wrapper);
				if(rl!=null&&rl.size()>0){
			 		rl.forEach((a)->{
			 			Role role = roleService.load(a.getRoleId());
			 			a.setRole(role);
			 		});
				}
				return rl;
	}
	@Override
	public Account load(Long accountId) {
		Account a =super.load(accountId);
		if(!StringUtils.isEmpty(a)){			
		Role role =roleService.load(a.getRoleId());
		a.setRole(role);
		}
	 	return a;
	}
	
}
