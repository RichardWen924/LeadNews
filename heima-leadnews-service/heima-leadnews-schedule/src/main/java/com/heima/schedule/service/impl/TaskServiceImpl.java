package com.heima.schedule.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.common.constants.ScheduleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.schedule.pojos.Taskinfo;
import com.heima.model.schedule.pojos.TaskinfoLogs;
import com.heima.schedule.mapper.TaskinfoLogsMapper;
import com.heima.schedule.mapper.TaskinfoMapper;
import com.heima.schedule.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.util.BeanUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

@Service
@Transactional
@Slf4j
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskinfoMapper taskinfoMapper;

    @Autowired
    private TaskinfoLogsMapper taskinfoLogsMapper;

    @Autowired
    private CacheService cacheService;

    /**
     * 添加延迟任务
     *
     * @param task
     * @return
     */
    @Override
    public long addTask(Task task) {

        //1、添加任务到数据库
        boolean success=  addTaskToDb(task);
        if(success){
            //添加任务到redis
            addTaskToCache(task);
        }




        return task.getTaskId();
    }


    /**
     * 取消任务
     * @param taskId
     * @return
     */
    @Override
    public boolean cancelTask(long taskId) {
        boolean flag=false;

        //删除任务更新日志
        Task task=updateDb(taskId,ScheduleConstants.CANCELLED);

        //删除在redis中的数据
        if(task!=null){
            removeTaskFromCache(task);
            flag=true;
        }
//TODO 修改了false和更行状态的值
        return flag;
    }


    /**
     * 删除redis中的任务数据
     * @param task
     */
    private void removeTaskFromCache(Task task) {

        String key = task.getTaskType()+"_"+task.getPriority();

        if(task.getExecuteTime()<=System.currentTimeMillis()){
            cacheService.lRemove(ScheduleConstants.TOPIC+key,0,JSON.toJSONString(task));
        }else {
            cacheService.zRemove(ScheduleConstants.FUTURE+key, JSON.toJSONString(task));
        }
    }

    /**
     * 删除任务，更新任务日志状态
     * @param taskId
     * @param status
     * @return
     */
    private Task updateDb(long taskId, int status) {
        Task task = null;
        try {
            //删除任务
            taskinfoMapper.deleteById(taskId);

            TaskinfoLogs taskinfoLogs = taskinfoLogsMapper.selectById(taskId);
            taskinfoLogs.setStatus(status);
            taskinfoLogsMapper.updateById(taskinfoLogs);

            task = new Task();
            BeanUtils.copyProperties(taskinfoLogs,task);
            task.setExecuteTime(taskinfoLogs.getExecuteTime().getTime());
        }catch (Exception e){
            log.error("task cancel exception taskid={}",taskId);
        }

        return task;

    }


    /**
     * 把任务添加到redis中
     *
     * @param task
     */
    private void addTaskToCache(Task task) {


        String key = task.getTaskType() + "_" + task.getPriority();

        //获取五分钟后的时间，为毫秒值
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE,5);
        long nextScheduleTime = calendar.getTimeInMillis();



        //2.1如果任务执行时间小于等于当前时间存到list中
        if(task.getExecuteTime()<=System.currentTimeMillis()){
            cacheService.lLeftPush(ScheduleConstants.TOPIC+key, JSON.toJSONString(task));
        }else if (task.getExecuteTime()<=nextScheduleTime){
            //2.2如果任务执行时间大于等于当前时间，存到zset中
            cacheService.zAdd(ScheduleConstants.FUTURE+key,JSON.toJSONString(task),task.getExecuteTime());

        }




    }

    /**
     * 添加任务到数据库中
     *
     * @param task
     * @return
     */
    private boolean addTaskToDb(Task task) {

        boolean flag=false;

       try{
           //保存任务表
           Taskinfo taskinfo = new Taskinfo();
           BeanUtils.copyProperties(task,taskinfo);
           taskinfo.setExecuteTime(new Date(task.getExecuteTime()));
           taskinfoMapper.insert(taskinfo);

           //设置taskID
          task.setTaskId(taskinfo.getTaskId()) ;

           //保存任务日志数据

           TaskinfoLogs taskinfoLogs = new TaskinfoLogs();
           BeanUtils.copyProperties(taskinfo,taskinfoLogs);
           taskinfoLogs.setVersion(1);

           taskinfoLogs.setStatus(ScheduleConstants.SCHEDULED);
           taskinfoLogsMapper.insert(taskinfoLogs);
           flag=true;
       } catch (Exception e) {
           throw new RuntimeException(e);
       }

return flag;


    }

    /**
     * 按照类型和优先级拉取任务
     * @return
     */
    @Override
    public Task poll(int type,int priority) {

        Task task = new Task();
        try{
            String key=type+"_"+priority;
            String task_json=cacheService.lRightPop(ScheduleConstants.TOPIC+key);
            if (StringUtils.isNotBlank(task_json)) {
                task=JSON.parseObject(task_json,Task.class);
                //更新数据库信息

                updateDb(task.getTaskId(),ScheduleConstants.EXECUTED);

            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("poll  task exception");
        }

        return task;
    }

    @Scheduled(cron="0 */1 * * * ?")
    public void refresh(){
        System.out.println(System.currentTimeMillis()/1000+"执行了定时任务");

        //获取所有未来数据集合的key值
        Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");
        for (String futureKey : futureKeys) {
            String topicKey = ScheduleConstants.TOPIC + futureKey.split(ScheduleConstants.FUTURE)[1];
            //获取该组key下当前所需要消费的任务数据
            Set<String> tasks = cacheService.zRangeByScore(futureKey, 0, System.currentTimeMillis());
            if(!tasks.isEmpty()){
                //将这些任务加入到消费者队列
                cacheService.refreshWithPipeline(futureKey,topicKey,tasks);
                System.out.println("成功的将" + futureKey + "下的当前需要执行的任务数据刷新到" + topicKey + "下");
            }
        }


    }

}
