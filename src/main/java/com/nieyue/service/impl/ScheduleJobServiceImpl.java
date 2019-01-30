package com.nieyue.service.impl;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.nieyue.bean.ScheduleJob;
import com.nieyue.exception.CommonRollbackException;
import com.nieyue.schedule.QuartzEventService;
import com.nieyue.service.ScheduleJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class ScheduleJobServiceImpl extends BaseServiceImpl<ScheduleJob,Long> implements ScheduleJobService {
    @Autowired
    QuartzEventService quartzEventService;
    @Transactional(propagation= Propagation.REQUIRED)
    @Override
    public boolean add(ScheduleJob scheduleJob) {
        scheduleJob.setCreateDate(new Date());
        scheduleJob.setUpdateDate(new Date());
        scheduleJob.setJobName("com.nieyue.schedule.QuartzJob");
        scheduleJob.setJobGroup(UUID.randomUUID().toString());
        scheduleJob.setMethodName("execute");
        boolean b = super.add(scheduleJob);
        if(!b){
            throw new CommonRollbackException("增加失败");
        }
        b = quartzEventService.addScheduleJob(scheduleJob);
        if(!b){
            throw new CommonRollbackException("增加失败");
        }
        return b;
    }
    @Transactional(propagation= Propagation.REQUIRED)
    @Override
    public boolean update(ScheduleJob scheduleJob) {
        scheduleJob.setUpdateDate(new Date());
        boolean b = super.update(scheduleJob);
        if(!b){
            throw new CommonRollbackException("修改失败");
        }
        b = quartzEventService.delScheduleJob(scheduleJob.getJobName(), scheduleJob.getJobGroup());
        b =  quartzEventService.addScheduleJob(scheduleJob);
        if(!b){
            throw new CommonRollbackException("修改失败");
        }
        return b;
    }
    @Transactional(propagation= Propagation.REQUIRED)
    @Override
    public boolean delete(Long scheduleJobId) {
        ScheduleJob sj = super.load(scheduleJobId);
        boolean b = super.delete(scheduleJobId);
        if(!b){
            throw new CommonRollbackException("删除失败");
        }
        b =quartzEventService.delScheduleJob(sj.getJobName(), sj.getJobGroup());
        if(!b){
            throw new CommonRollbackException("删除失败");
        }
        return b;
    }

    @Override
    public ScheduleJob load(Long scheduleJobId) {
        ScheduleJob sj = super.load(scheduleJobId);
        return sj;
    }

    @Override
    public int count(Wrapper<ScheduleJob> wrapper) {
        int c = super.count(wrapper);
        return c;
    }

    @Override
    public List<ScheduleJob> list(int pageNum, int pageSize, String orderName, String orderWay, Wrapper<ScheduleJob> wrapper) {
        return super.list(pageNum, pageSize, orderName, orderWay, wrapper);
    }

    @Override
    public List<ScheduleJob> simplelist(Wrapper<ScheduleJob> wrapper) {
        return super.simplelist(wrapper);
    }

}
