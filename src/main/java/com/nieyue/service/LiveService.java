package com.nieyue.service;

import com.nieyue.bean.Live;

/**
 * 直播逻辑层接口
 * @author yy
 *
 */
public interface LiveService extends BaseService<Live, Long>{
    @Override
    boolean add(Live live);

    @Override
    boolean update(Live live);
    boolean update2(Live live);

    @Override
    boolean delete(Long liveId);

    boolean changeStatus(Long liveId,Integer status);
}
