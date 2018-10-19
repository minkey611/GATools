package com.minkey.db.third.task;

import com.minkey.db.LinkHandler;
import com.minkey.db.TaskLogHandler;
import com.minkey.db.dao.Link;
import com.minkey.db.dao.TaskLog;
import com.minkey.dto.DBConfigData;
import com.minkey.util.DynamicDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 从数据交换系统，采集task执行日志信息
 * <br><br/>
 * 每天获取一次
 */

@Component
public class TaskDayLogCollector {
    private final static Logger logger = LoggerFactory.getLogger(TaskDayLogCollector.class);

    @Autowired
    TaskLogHandler taskLogHandler;


    @Autowired
    LinkHandler linkHandler;

    @Autowired
    DynamicDB dynamicDB;


    @Value("${system.debug}")
    private boolean isDebug;

    /**
     * 每一个小时执行一次，0分0秒开始执行
     */
//    @Scheduled(cron="0 0 * * ? *")
    public void getAllTask(){
        if(isDebug){
            logger.error("测试抓取任务列表调度.");
            return;
        }
        List<Link> linkList = null;
        try {
            //查询所有链路
            linkList = linkHandler.queryAll();
            if (CollectionUtils.isEmpty(linkList)) {
                return;
            }
        }catch (Exception e){
            logger.error("获取所有的链路异常",e);
            return;
        }

        linkList.forEach(link -> {
            List<TaskLog> tasks = null;
            try {
                //从链路中获取数据交换系统的数据库配置
                DBConfigData dbConfig = link.getDbConfigData();

                long maxLoggerId = taskLogHandler.queryMaxId();

                tasks = queryAllTaskLog(dbConfig,maxLoggerId);

            }catch (Exception e){
                logger.error("从交换系统抓起任务执行日志异常",e);
            }

            if(!CollectionUtils.isEmpty(tasks)){
                try {
                    //把链路存到数据库中。
                    taskLogHandler.insertAll(tasks);
                }catch (Exception e){
                    logger.error("把抓取过来的任务执行日志保存到数据库中异常",e);
                }
            }
        });
    }

    /**
     * 从数据交换系统获取任务日志列表
     * @param dbConfig
     * @return
     */
    private List<TaskLog> queryAllTaskLog(DBConfigData dbConfig,long maxLoggerId){
        //先从缓存中获取
        JdbcTemplate jdbcTemplate = dynamicDB.get8dbConfig(dbConfig);

        String sql = "select dayloggerid,taskid,totalSuccessNum,totalSuccessFlow,totalErrorFlow,totalErrorNum,insertTimes from tbdaylogger where dayloggerid > "+maxLoggerId;
        //查询所有task
        List<Map<String, Object>>  mapList= jdbcTemplate.queryForList(sql);

        if(CollectionUtils.isEmpty(mapList)){
            return null;
        }

        List<TaskLog> tasks = new ArrayList<>(mapList.size());

        mapList.forEach(stringObjectMap -> {
            TaskLog taskLog = new TaskLog();
            taskLog.setTargetLogId((Long)stringObjectMap.get("dayloggerid"));
            taskLog.setTaskId((String) stringObjectMap.get("taskid"));
            taskLog.setSuccessNum((Long)stringObjectMap.get("dayloggerid"));
            taskLog.setSuccessNum((Long)stringObjectMap.get("totalSuccessNum"));
            taskLog.setSuccessFlow((Long)stringObjectMap.get("totalSuccessFlow"));
            taskLog.setErrorNum((Long)stringObjectMap.get("totalErrorNum"));
            taskLog.setErrorFlow((Long)stringObjectMap.get("totalErrorFlow"));
            taskLog.setCreateTime((Date) stringObjectMap.get("insertTimes"));
            tasks.add(taskLog);
        });
        return tasks;
    }


    /**
     * 要访问的表的建表语句
     CREATE TABLE `NewTable` (
     `dayloggerid`  int(11) NOT NULL AUTO_INCREMENT ,
     `taskid`  varchar(15) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL ,
     `totalSuccessNum`  bigint(100) NOT NULL ,
     `totalSuccessFlow`  bigint(100) NOT NULL ,
     `totalErrorFlow`  bigint(100) NOT NULL ,
     `totalErrorNum`  bigint(100) NOT NULL ,
     `insertTimes`  timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' ,
     `tname`  varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '表名称' ,
     `isDelete`  varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL ,
     PRIMARY KEY (`dayloggerid`)
     )
     ENGINE=InnoDB
     DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
     AUTO_INCREMENT=5
     ROW_FORMAT=DYNAMIC
     ;
     */

}
