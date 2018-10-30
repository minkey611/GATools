package com.minkey.controller;

import com.minkey.contants.DeviceType;
import com.minkey.db.DeviceHandler;
import com.minkey.db.DeviceServiceHandler;
import com.minkey.db.UserLogHandler;
import com.minkey.db.dao.Device;
import com.minkey.db.dao.DeviceService;
import com.minkey.db.dao.User;
import com.minkey.dto.DeviceExplorer;
import com.minkey.dto.JSONMessage;
import com.minkey.dto.Page;
import com.minkey.handler.DeviceStatusHandler;
import com.minkey.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 设备接口
 */
@Slf4j
@RestController
@RequestMapping("/device")
public class DeviceController {
    private final String moduleName = "设备管理模块";

    @Autowired
    DeviceHandler deviceHandler;

    @Autowired
    DeviceServiceHandler deviceServiceHandler;

    @Autowired
    DeviceStatusHandler deviceStatusHandler;

    @Autowired
    UserLogHandler userLogHandler;

    @Autowired
    HttpSession session;

    @RequestMapping("/insert")
    public String insert( Device device) {
        log.info("start: 执行insert设备 device={} ",device);

        if(StringUtils.isEmpty(device.getDeviceName())){
            return JSONMessage.createFalied("name不能为空格式错误").toString();
        }
        if(StringUtils.isEmpty(device.getIcon())){
            return JSONMessage.createFalied("参数错误").toString();
        }

        if(device.getDeviceType() != DeviceType.floder && StringUtils.isNotEmpty(device.getIp())){
            if(!StringUtil.isIp(device.getIp())){
                return JSONMessage.createFalied("ip格式错误").toString();
            }
        }

        try{
            long deviceId = deviceHandler.insert(device);
            device.setDeviceId(deviceId);

            List<DeviceService> paramList = device.getDeviceServiceList();
            if(!CollectionUtils.isEmpty(paramList)){
                deviceServiceHandler.insertAll(device,paramList);
            }

            User sessionUser = (User)session.getAttribute("user");
            //记录用户日志
            userLogHandler.log(sessionUser,moduleName,String.format("%s 新增设备，设备名称=%s。",sessionUser.getuName(),device.getDeviceName()));

            return JSONMessage.createSuccess().toString();
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return JSONMessage.createFalied(e.getMessage()).toString();
        }finally {
            log.info("end: 执行insert设备 device={} ",device);
        }
    }

    @RequestMapping("/update")
    public String update( Device device) {
        log.info("start: 执行insert设备 device={} ",device);

        if(device.getDeviceId() < 0){
            return JSONMessage.createFalied("deviceId错误").toString();
        }

        if(StringUtils.isEmpty(device.getDeviceName())){
            return JSONMessage.createFalied("name不能为空格式错误").toString();
        }

        if(device.getDeviceType() != DeviceType.floder && StringUtils.isNotEmpty(device.getIp())){
            if(!StringUtil.isIp(device.getIp())){
                return JSONMessage.createFalied("ip格式错误").toString();
            }
        }

        try{
            deviceHandler.replace(device);
            //先删除
            deviceServiceHandler.delete8DeviceId(device.getDeviceId());

            List<DeviceService> deviceServiceList = device.getDeviceServiceList();
            if(!CollectionUtils.isEmpty(deviceServiceList)){
                //在增加
                deviceServiceHandler.insertAll(device ,deviceServiceList);
            }

            User sessionUser = (User)session.getAttribute("user");
            //记录用户日志
            userLogHandler.log(sessionUser,moduleName,String.format("%s 修改设备信息，设备名称=%s。",sessionUser.getuName(),device.getDeviceName()));

            return JSONMessage.createSuccess().toString();
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return JSONMessage.createFalied(e.getMessage()).toString();
        }finally {
            log.info("end: 执行insert设备 device={} ",device);
        }
    }

    @RequestMapping("/query")
    public String query(Long deviceId) {
        log.info("start: 执行query设备 deviceId={} ",deviceId);
        if(deviceId == null || deviceId <=0){
            log.info("deviceId不能为空");
            return JSONMessage.createFalied("deviceId不能为空").toString();
        }
        try{

            Device device = deviceHandler.query(deviceId);
            device.setDeviceServiceList(deviceServiceHandler.query8Device(deviceId));
            return JSONMessage.createSuccess().addData(device).toString();
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return JSONMessage.createFalied(e.getMessage()).toString();
        }finally {
            log.info("end: 执行query设备 deviceId={} ",deviceId);
        }
    }

    @RequestMapping("/query8Page")
    public String query8Page(Integer currentPage,Integer pageSize) {
        log.info("start: 执行分页查询设备 currentPage={} ,pageSize={}" , currentPage,pageSize);
        if(currentPage == null || pageSize <=0){
            return JSONMessage.createFalied("参数错误").toString();
        }

        try{
            Page<Device> page = new Page(currentPage,pageSize);

            page = deviceHandler.query8Page(page);

            return JSONMessage.createSuccess().addData(page).toString();
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return JSONMessage.createFalied(e.getMessage()).toString();
        }finally {
            log.info("end: 执行分页查询所有设备 ");
        }
    }

    @RequestMapping("/queryCount")
    public String queryCount() {
        log.info("start: 执行count所有设备 ");

        try{
            return JSONMessage.createSuccess().addData("count",deviceHandler.queryCount()).toString();
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return JSONMessage.createFalied(e.getMessage()).toString();
        }finally {
            log.info("end: 执行count所有设备 ");
        }
    }

    @RequestMapping("/delete")
    public String delete(Long deviceId) {
        log.info("start: 执行删除设备 deviceId={} ",deviceId);
        if(deviceId == null || deviceId <=0){
            log.info("deviceId不能为空");
            return JSONMessage.createFalied("deviceId不能为空").toString();
        }

        try{
            //先删除
            deviceServiceHandler.delete8DeviceId(deviceId);

            deviceHandler.delete(deviceId);

            User sessionUser = (User)session.getAttribute("user");
            //记录用户日志
            userLogHandler.log(sessionUser,moduleName,String.format("%s 删除设备信息，设备id=%s。",sessionUser.getuName(),deviceId));

            return JSONMessage.createSuccess().toString();
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return JSONMessage.createFalied(e.getMessage()).toString();
        }finally {
            log.info("end: 执行删除设备 ");
        }
    }


    @RequestMapping("/queryAllDetector")
    public String queryAllDetector() {
        log.info("start: 查询所有探针设备 ");

        try{
            List<Device> all = deviceHandler.query8Type(DeviceType.detector);

            return JSONMessage.createSuccess().addData("list",all).toString();
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return JSONMessage.createFalied(e.getMessage()).toString();
        }finally {
            log.info("end: 查询所有探针设备 ");
        }
    }

    /**
     * 查询某一个终端的实时资源消耗
     * @return
     */
    @RequestMapping("/queryExplorer")
    public String queryExplorer(Long deviceId) {
        log.info("start: 查询某一个终端的实时资源消耗 deviceId={}",deviceId);

        try{
            DeviceExplorer deviceExplorer = deviceStatusHandler.getDeviceExplorer(deviceId);
            return JSONMessage.createSuccess().addData("deviceExplorer" ,deviceExplorer).toString();
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return JSONMessage.createFalied(e.getMessage()).toString();
        }finally {
            log.info("end: 查询某一个终端的实时资源消耗 ");
        }
    }
}