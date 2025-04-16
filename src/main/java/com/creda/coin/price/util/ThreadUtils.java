package com.creda.coin.price.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author gavin
 * @date 2024/11/23
 **/
public class ThreadUtils{
    public static Map<String, Object> getThreadPoolStats(ThreadPoolExecutor threadPoolExecutor) {
        Map<String, Object> stats = new HashMap<>();

        stats.put("正在运行的线程数", threadPoolExecutor.getActiveCount()); // 正在运行的线程数
        stats.put("队列中等待的任务数", threadPoolExecutor.getQueue().size());       // 队列中等待的任务数
//        stats.put("completedTaskCount", threadPoolExecutor.getCompletedTaskCount()); // 已完成任务数
//        stats.put("totalTaskCount", threadPoolExecutor.getTaskCount());     // 总任务数

        return stats;
    }

}
