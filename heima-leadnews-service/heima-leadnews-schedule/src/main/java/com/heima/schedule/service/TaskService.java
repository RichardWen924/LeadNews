package com.heima.schedule.service;

import com.heima.model.schedule.dtos.Task;

public interface TaskService {
    /**
     * 添加任务
     */

    public long addTask(Task task);

    /**
     * 取消任务
     * @param taskId        任务id
     * @return              取消结果
     */
    public boolean cancelTask(long taskId);
}
