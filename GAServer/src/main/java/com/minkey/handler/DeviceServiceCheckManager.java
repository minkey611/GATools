package com.minkey.handler;

import com.minkey.command.SnmpUtil;
import com.minkey.contants.MyLevel;
import com.minkey.db.dao.Device;
import com.minkey.db.dao.DeviceService;
import com.minkey.dto.BaseConfigData;
import com.minkey.dto.DBConfigData;
import com.minkey.dto.FTPConfigData;
import com.minkey.dto.SnmpConfigData;
import com.minkey.executer.SSHExecuter;
import com.minkey.util.DetectorUtil;
import com.minkey.util.DynamicDB;
import com.minkey.util.FTPUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 设备服务检查
 *
 */
@Slf4j
@Component
public class DeviceServiceCheckManager {

    @Autowired
    DynamicDB dynamicDB;

    @Autowired
    FTPUtil ftpUtil;


   public boolean checkDeviceService(Device device, DeviceService deviceService, DeviceService detectorService) {
        BaseConfigData baseConfigData;
        boolean isOk;
        switch (deviceService.getServiceType()){
            //如果是探针，调用探针的check接口
            case DeviceService.SERVICETYPE_DETECTOR :
                isOk = DetectorUtil.check(deviceService.getIp(),deviceService.getConfigData().getPort());
                break;
            case DeviceService.SERVICETYPE_DB:
                DBConfigData dbConfigData = (DBConfigData) deviceService.getConfigData();
                if(device.isNetAreaIn()){
                    isOk = dynamicDB.testDB(dbConfigData);
                }else{
                    if(detectorService == null){
                        isOk = false;
                    }else{
                        isOk = DetectorUtil.testDB(detectorService.getIp(),detectorService.getConfigData().getPort(),dbConfigData);
                    }
                }
                break;
            case DeviceService.SERVICETYPE_SNMP:
                SnmpConfigData snmpConfigData = (SnmpConfigData) deviceService.getConfigData();
                SnmpUtil snmpUtil =new SnmpUtil(snmpConfigData);
                if(device.isNetAreaIn()){
                    isOk = snmpUtil.testConnect();
                }else{
                    if(detectorService == null){
                        isOk = false;
                    }else{
                        isOk = DetectorUtil.testSNMP(detectorService.getIp(),detectorService.getConfigData().getPort(),snmpConfigData);
                    }
                }
                break;
            case DeviceService.SERVICETYPE_FTP:
                FTPConfigData ftpConfigData = (FTPConfigData) deviceService.getConfigData();
                if(device.isNetAreaIn()){
                    isOk = ftpUtil.testFTPConnect(ftpConfigData);
                }else{
                    if(detectorService == null){
                        isOk = false;
                    }else{
                        isOk = DetectorUtil.testFTP(detectorService.getIp(),detectorService.getConfigData().getPort(), ftpConfigData);
                    }
                }
                break;
            case DeviceService.SERVICETYPE_SSH:
                baseConfigData = deviceService.getConfigData();
                if(device.isNetAreaIn()){
                    isOk = SSHExecuter.testConnect(baseConfigData);
                }else{
                    if(detectorService == null){
                        isOk = false;
                    }else{
                        isOk = DetectorUtil.testSSH(detectorService.getIp(),detectorService.getConfigData().getPort(), baseConfigData);
                    }
                }
                break;
            default:
                isOk =false;
                log.error(String.format("未知设备服务类型[%s]",deviceService.getServiceType()));
        }

        return isOk;

    }
}