package com.nieyue.service;

import com.nieyue.bean.Permission;
import com.nieyue.service.BaseService;

/**
 * 权限逻辑层接口
 * @author yy
 *
 */
public interface PermissionService extends BaseService<Permission, Long> {
	/** 初始化权限 */	
	public boolean initPermission();	
}
