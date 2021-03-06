package com.minkey.db.dao;

import com.alibaba.fastjson.annotation.JSONField;
import com.minkey.contants.CommonContants;
import com.minkey.contants.DeviceType;

import java.util.List;

/**
 * 设备对象，每个机器都是一个设备
 */
public class Device {

    /**
     * 设备id，自增主键
     */
    private long deviceId;

    /**
     * 设备名称，用户自定义
     */
    private String deviceName;

    /**
     * 设备ip，可选，文件夹类型就没有ip
     */
    private String ip;

    /**
     * 设备类型<br>
     * 1 : 探针 <br>
     * 2 : 文件夹 <br>
     * 3 :
     *@see com.minkey.contants.DeviceType
     */
    private int deviceType;


    /**
     * 所属区域
     *
    *  1 = 路由接入区
    *  2 = 边界保护区
    *  3 = 应用服务器
    *  4 = 安全隔离区
    *  5 = 安全检测与管理区
     */
    private int area;

    /**
     * 网络区域：
     * 1 内网
     * 2外网
     * @see com.minkey.contants.CommonContants
     */
    private int netArea;

    /**
     * 图标id
     */
    private String icon;

    /**
     * 设备对应的服务
     */
    List<DeviceService> deviceServiceList;

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public int getNetArea() {
        return netArea;
    }

    public void setNetArea(int netArea) {
        this.netArea = netArea;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<DeviceService> getDeviceServiceList() {
        return deviceServiceList;
    }

    public void setDeviceServiceList(List<DeviceService> deviceServiceList) {
        this.deviceServiceList = deviceServiceList;
    }

    /**
     * s是否在内网
     * @return
     */
    @JSONField(serialize=false)
    public boolean isNetAreaIn(){
        return netArea == CommonContants.NETAREA_IN;
    }

    /**
     * 是否是探针
     * @return
     */
    @JSONField(serialize=false)
    public boolean isDetector(){
        return deviceType == DeviceType.detector;
    }


    @Override
    public String toString() {
        return "Device{" +
                "deviceId=" + deviceId +
                ", deviceName='" + deviceName + '\'' +
                ", ip='" + ip + '\'' +
                ", deviceType=" + deviceType +
                ", area=" + area +
                ", netArea=" + netArea +
                ", icon='" + icon + '\'' +
                ", deviceServiceList=" + deviceServiceList +
                '}';
    }
}
