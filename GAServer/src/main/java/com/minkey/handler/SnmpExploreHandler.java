package com.minkey.handler;

import com.alibaba.fastjson.JSONObject;
import com.minkey.cache.DeviceCache;
import com.minkey.cache.DeviceConnectCache;
import com.minkey.cache.DeviceExplorerCache;
import com.minkey.command.SnmpUtil;
import com.minkey.contants.CommonContants;
import com.minkey.contants.DeviceType;
import com.minkey.db.dao.Device;
import com.minkey.db.dao.DeviceService;
import com.minkey.dto.DeviceExplorer;
import com.minkey.dto.RateObj;
import com.minkey.dto.SnmpConfigData;
import com.minkey.util.DetectorUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.snmp4j.smi.OID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * 通过snmp获取硬件资源情况
 */
@Slf4j
@Component
public class SnmpExploreHandler {

    @Autowired
    DeviceCache deviceCache;
    /**
     * 设备联通性情况
     */
    @Autowired
    DeviceConnectCache deviceConnectCache;

    @Autowired
    DeviceExplorerCache deviceExplorerCache;

    /**
     * 每5秒刷新硬件资源
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void reflashExplorer() {
        Map<Long, Device> allDevices = deviceCache.allDevice();

        for (Device device:allDevices.values()){
            Long deviceId = device.getDeviceId();
            try {
                //如果连接正常，则获取
                if(deviceConnectCache.isOk(deviceId)){
                    getDeviceExplorer(device);
                }else{
                    //如果断开了，则删掉所有的硬件资源信息
                    deviceExplorerCache.remove(deviceId);
                }
            }catch (Exception e){
                log.error(e.getMessage(),e);
            }
        }
    }


    /**
     * 通过snmp获取硬件的资源信息
     *
     */
    @Async
    public void getDeviceExplorer(Device device){
        if(device == null){
            return;
        }
        //文件夹直接返回空
        if (device.getDeviceType() == DeviceType.floder) {
            return ;
        }

        DeviceExplorer deviceExplorer = null;
        //通过snmp命令获取 设备硬件信息
        // 如果是内网，就直接探测
        if(device.getNetArea() == CommonContants.NETAREA_IN ){
            deviceExplorer = get(device);
        }else{
            //如果是外网，需要通过探针访问,获取探针信息
            DeviceService detectorService = deviceCache.getOneDetectorServer8DeviceId(device.getDeviceId());
            //通过探针获取硬件信息
            deviceExplorer = get(device,detectorService);
        }


        if(deviceExplorer != null){
            if(deviceExplorer.getMem() == null && CollectionUtils.isEmpty(deviceExplorer.getDisks()) && CollectionUtils.isEmpty(deviceExplorer.getCpus())){
                log.debug(String.format("从%s获取硬件资源信息(snmp)全部为空,snmp协议不标准.",device));
                return;
            }
        }

       deviceExplorerCache.putDeviceExplorer(device.getDeviceId(),deviceExplorer);
    }


    /**
     * 通过探针获取外网设备的硬件资源
     * @param device
     * @param detectorService
     * @return
     */
    public DeviceExplorer get(Device device, DeviceService detectorService) {
        if(device == null || detectorService == null){
            return null;
        }

        DeviceService snmpService = deviceCache.getSNMPService8deviceId(device.getDeviceId());
        if(snmpService == null){
            return null;
        }
        SnmpConfigData snmpConfigData = (SnmpConfigData) snmpService.getConfigData();

        String targetIp = device.getIp();
        //如果是探针自己
        if(device.getDeviceType() == DeviceType.detector){
            targetIp = "127.0.0.1";
        }

        JSONObject cpuJson = DetectorUtil.snmpWalk(detectorService.getIp(),detectorService.getConfigData().getPort()
                ,targetIp,snmpConfigData.getPort(),snmpConfigData.getVersion(),snmpConfigData.getCommunity(),0,1000l
                ,cpuOid);

        DeviceExplorer deviceExplorer = new DeviceExplorer();
        deviceExplorer.setCpus(getCpu(cpuJson));

        JSONObject diskJson = DetectorUtil.snmpWalk(detectorService.getIp(),detectorService.getConfigData().getPort()
                ,targetIp,snmpConfigData.getPort(),snmpConfigData.getVersion(),snmpConfigData.getCommunity(),0,1000l
                ,diskOid);
        deviceExplorer.setDisks(getDisk(deviceExplorer,diskJson));

        return deviceExplorer;
    }

    /**
     * 直接获取内网的硬件资源
     * @param device
     * @return
     */
    public DeviceExplorer get(Device device) {
        if(device == null){
            return null;
        }

        DeviceService deviceService = deviceCache.getSNMPService8deviceId(device.getDeviceId());
        if(deviceService == null){
            return null;
        }

        SnmpConfigData snmpConfigData = (SnmpConfigData) deviceService.getConfigData();
        //获取该设备的 snmp服务
        SnmpUtil snmpUtil = new SnmpUtil(deviceService.getIp(),
                snmpConfigData.getPort(),
                snmpConfigData.getCommunity(),
                snmpConfigData.getVersion(),
                0,
                1000);

        DeviceExplorer deviceExplorer = new DeviceExplorer();

        try {
            JSONObject cpuJson = snmpUtil.snmpWalk(cpuOid);
            deviceExplorer.setCpus(getCpu(cpuJson));
        }catch (Exception e){
            log.error(String.format("获取设备[%s]的snmp-cpuk信息异常",device.getDeviceName()));
        }

        try {
            JSONObject diskJson = snmpUtil.snmpWalk(diskOid);
            deviceExplorer.setDisks(getDisk(deviceExplorer, diskJson));
        }catch (Exception e){
            log.error(String.format("获取设备[%s]的snmp-disk信息异常",device.getDeviceName()));
        }
        return deviceExplorer;
    }

    final String diskOid = "1.3.6.1.2.1.25.2.3.1";
    private Map<String,RateObj> getDisk(DeviceExplorer deviceExplorer, JSONObject diskJson) {
        if(diskJson == null || diskJson.size() == 0){
            return null;
        }

        Map<String,RateObj> map = new HashMap<>();
        RateObj rateObj ;
        List<Integer> lastOidList = new ArrayList<>();
        OID tempOid;
        for(String key : diskJson.keySet()){
            tempOid = new OID(key);
            //先取序列号
            if(tempOid.startsWith(new OID(diskOid + ".1"))){
                int lastOid = tempOid.removeLast();
                lastOidList.add(lastOid);
            }
        }

        for(Integer lastOid : lastOidList){
            //名称
            String name = diskJson.getString(diskOid+".3."+lastOid);
            //分配单元所占字节数
            int danweiByte = diskJson.getInteger(diskOid+".4."+lastOid);
            //总区块数
            long totalArea = diskJson.getLong(diskOid+".5."+lastOid);
            //已经使用的区块数
            //会空指针,使用大写Long
            Long useArea = diskJson.getLong(diskOid+".6."+lastOid);
            if(useArea == null){
//                log.debug("diskJson的值为空"+diskOid+".6."+lastOid);
                //为空直接赋值为0
                useArea = 0l;
            }

            rateObj = RateObj.create8Use(totalArea * danweiByte , useArea * danweiByte);
            //如果是物理内存
            if(StringUtils.equalsIgnoreCase(name , "Physical Memory")){
                deviceExplorer.setMem(rateObj);
            }else if(StringUtils.equalsIgnoreCase(name , "Virtual Memory")){
                //如果是虚拟内存，暂时不获取
            }else if(name.contains(":\\") || name.contains("/")){
                //如果是磁盘存储
                map.put(name,rateObj);
            }
        }
        return map;
    }

    final String cpuOid = "1.3.6.1.2.1.25.3.3.1.2";
    private Map<String,RateObj> getCpu(JSONObject cpuJson) {
        if(cpuJson == null || cpuJson.size() == 0){
            return null;
        }

        Map<String,RateObj> map = new HashMap<>();
        RateObj rateObj ;
        for(String key : cpuJson.keySet()){
            rateObj = RateObj.create8Use(100,Double.valueOf(cpuJson.getString(key)));
            map.put(key,rateObj);
        }
        return map;
    }

    private Map<String,RateObj> getNetwork(SnmpUtil snmpUtil) {
        String net_in = "1.3.6.1.2.1.2.2.1.16";
        String net_out = "1.3.6.1.2.1.2.2.1.10";


        //Minkey 网络实时流量不知道这么获取
        return null;
    }

    //所有进程参数
    final String processParamOid = ".1.3.6.1.2.1.25.4.2.1.5";
    //所有进程名称/id
    final String processNnameOid = ".1.3.6.1.2.1.25.4.2.1.2";

    /**
     * 检查外网进程是否存在
     * @param targetTaskId
     * @return
     */
    public boolean checkProcess(String targetTaskId, SnmpConfigData snmpConfigData,DeviceService detectorService) {
        JSONObject jsonObject = null;
        //内网时参数为空
        if(detectorService == null){
            //获取该设备的 snmp服务
            SnmpUtil snmpUtil = new SnmpUtil(snmpConfigData.getIp(),
                    snmpConfigData.getPort(),
                    snmpConfigData.getCommunity(),
                    snmpConfigData.getVersion(),
                    0,
                    1000);

            jsonObject = snmpUtil.snmpWalk(processParamOid);
        }else{
            jsonObject = DetectorUtil.snmpWalk(detectorService.getIp(),detectorService.getConfigData().getPort()
                    ,snmpConfigData.getIp(),snmpConfigData.getPort(),snmpConfigData.getVersion(),snmpConfigData.getCommunity(),0,1000l
                    , processParamOid);

        }

        return checkExist(targetTaskId,jsonObject);
    }

    private boolean checkExist(String targetTaskId,JSONObject jsonObject){
        if(jsonObject == null){
            return false;
        }

        for(String jsonKey :jsonObject.keySet()){
            //如果参数中含有taskId证明进程存在
            if(jsonObject.getString(jsonKey).contains(targetTaskId)){
                return true;
            }
        }
        return false;
    }
}
