package com.minkey.handler;

import com.minkey.cache.CheckStepCache;
import com.minkey.cache.DeviceCache;
import com.minkey.contants.MyLevel;
import com.minkey.db.CheckItemHandler;
import com.minkey.db.SourceHandler;
import com.minkey.db.TaskHandler;
import com.minkey.db.TaskSourceHandler;
import com.minkey.db.dao.*;
import com.minkey.dto.FTPConfigData;
import com.minkey.exception.SystemException;
import com.minkey.util.DetectorUtil;
import com.minkey.util.DynamicDB;
import com.minkey.util.FTPUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 任务另起一个类
 */
@Slf4j
@Component
public class TaskExamineHandler {
    @Autowired
    CheckItemHandler checkItemHandler;

    @Autowired
    CheckStepCache checkStepCache;

    @Autowired
    DeviceCache deviceCache;

    @Autowired
    TaskHandler taskHandler;

    @Autowired
    TaskSourceHandler taskSourceHandler;

    @Autowired
    SourceHandler sourceHandler;

    @Autowired
    DynamicDB dynamicDB;

    @Autowired
    FTPUtil ftpUtil;

    @Autowired
    SourceCheckHandler sourceCheckHandler;

    @Async
    public void doTaskAsync(long checkId, Task task) {
        doTask(checkId,task);
    }

    public void doTask(long checkId, Task task) {
        //需要检查任务数据源 和 数据存放地 两边的情况
        CheckItem checkItem;

        String taskTargetId = task.getTargetTaskId();

        TaskSource taskSource = taskSourceHandler.query(task.getLinkId(),taskTargetId);

        if(taskSource == null){
            checkItem = new CheckItem(checkId,1);
            checkItem.setResultLevel(MyLevel.LEVEL_ERROR).setResultMsg(String.format("没有找到任务%s的数据源信息",task.getTaskName()));
            checkItemHandler.insert(checkItem);
            //没有找到数据源配置
            return;
        }

        int totalStep = 2+1+1;
        checkStepCache.create(checkId,totalStep);

        DeviceService detectorService = deviceCache.getDetectorService8linkId(task.getLinkId());

        String fromSourceId = taskSource.getFromResourceId();
        String toSourceId = taskSource.getToResourceId();

        Source fromSource = sourceHandler.query(task.getLinkId(),fromSourceId);
        checkItem = testSource(checkId, task, fromSource,detectorService);
        checkItemHandler.insert(checkItem);

        Source toSource = sourceHandler.query(task.getLinkId(),toSourceId);
        checkItem = testSource(checkId, task, toSource,detectorService);
        checkItemHandler.insert(checkItem);

        //Minkey 检查任务进程是否存在
        checkItem = checkStepCache.createNextItem(checkId);
        checkItem.setResultLevel(MyLevel.LEVEL_NORMAL).setResultMsg("任务进程正常运行");
        checkItemHandler.insert(checkItem);
    }

    private CheckItem testSource(long checkId, Task task, Source source, DeviceService detectorService){
        //如果是外网而且没有探针
        if(!source.isNetAreaIn() && detectorService == null){
            String logstr = String.format("没有部署探针，无法探测外网资源%s",source);
            log.error(logstr);
            CheckItem checkItem = checkStepCache.createNextItem(checkId);
            checkItem.setResultLevel(MyLevel.LEVEL_ERROR).setResultMsg(logstr);
            return checkItem;
        }

        if(StringUtils.equals(source.getSourceType(),Source.sourceType_db)){
            return testSource_db(checkId,task,source,detectorService);
        }else if(StringUtils.equals(source.getSourceType(),Source.sourceType_ftp)){
            return testSource_ftp(checkId,task,source,detectorService);
        }else if(StringUtils.equals(source.getSourceType(),Source.sourceType_video)){
            return testSource_video(checkId,task,source,detectorService);
        }else{
            //未知数据格式
            String logstr = String.format("未知数据源类型[%]",source.getSourceType());
            log.error(logstr);
            CheckItem checkItem = checkStepCache.createNextItem(checkId);
            checkItem.setResultLevel(MyLevel.LEVEL_ERROR).setResultMsg(logstr);
            return checkItem;
        }
    }

    private CheckItem testSource_ftp(long checkId, Task task, Source source, DeviceService detectorService) {
        boolean isConnect =testSourceConnect_ftp(source,detectorService);

        CheckItem checkItem = checkStepCache.createNextItem(checkId);

        if(isConnect){
            checkItem.setResultLevel(MyLevel.LEVEL_NORMAL)
                    .setResultMsg(String.format("探测任务[%s]%sFTP数据源%s连接正常",task.getTaskName(),source.isNetAreaIn()?"内网":"外网",source.getSname()));
        }else {
            checkItem.setResultLevel(MyLevel.LEVEL_ERROR)
                    .setResultMsg(String.format("探测任务[%s]%sFTP数据源%s连接失败",task.getTaskName(),source.isNetAreaIn()?"内网":"外网",source.getSname()));
        }

        return checkItem;
    }

    private CheckItem testSource_db(long checkId, Task task, Source source, DeviceService detectorService) {
        boolean isConnect = testSourceConnect_db(source,detectorService);

        CheckItem checkItem = checkStepCache.createNextItem(checkId);

        if(isConnect){
            checkItem.setResultLevel(MyLevel.LEVEL_NORMAL)
                    .setResultMsg(String.format("探测任务[%s]%sDB数据源[%s]连接正常",task.getTaskName(),source.isNetAreaIn()?"内网":"外网",source.getSname()));
        }else {
            checkItem.setResultLevel(MyLevel.LEVEL_ERROR)
                    .setResultMsg(String.format("探测任务[%s]%sDB数据源[%s]连接失败",task.getTaskName(),source.isNetAreaIn()?"内网":"外网",source.getSname()));
        }
        return checkItem;
    }

    private CheckItem testSource_video(long checkId, Task task, Source source, DeviceService detectorService) {
        log.error("暂时不支持video数据源探测");
        CheckItem checkItem = checkStepCache.createNextItem(checkId);
        checkItem.setResultLevel(MyLevel.LEVEL_ERROR).setResultMsg("暂时不支持video数据源探测");
        return checkItem;
    }


    public boolean testSourceConnect_ftp(Source source, DeviceService detectorService)throws SystemException {
        //将source转换为ftpconfigdata
        FTPConfigData ftpConfigData = new FTPConfigData();
        ftpConfigData.setIp(source.getIp());
        ftpConfigData.setPort(source.getPort());
        ftpConfigData.setName(source.getName());
        ftpConfigData.setPwd(source.getPwd());
        ftpConfigData.setRootPath(source.getDbName());

        boolean isConnect;
        if(source.isNetAreaIn()){
            isConnect = ftpUtil.testFTPConnect(ftpConfigData, FTPUtil.default_timeout);
        }else{
            if(detectorService == null){
                log.error(String.format("没有部署探针，无法探测外网FTP资源%s",source));
                return false;
            }
            isConnect = DetectorUtil.testFTPConnect(detectorService.getIp(),detectorService.getConfigData().getPort(), ftpConfigData);
        }

        return isConnect;
    }

    public boolean testSourceConnect_db(Source source, DeviceService detectorService)throws SystemException {
        boolean isConnect;
        if(source.isNetAreaIn()){
            isConnect = dynamicDB.testDBConnect(source);
        }else{
            if(detectorService == null){
                log.error(String.format("没有部署探针，无法探测外网DB资源%s",source));
                return false;
            }
            isConnect = DetectorUtil.testDBConnect(detectorService.getIp(),detectorService.getConfigData().getPort(),source);
        }
        return isConnect;

    }
}
