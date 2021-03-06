package com.minkey.db.dao;

import com.alibaba.fastjson.annotation.JSONField;
import com.minkey.contants.AlarmEnum;

import java.util.Date;

/**
 * 告警log
 */
public class AlarmLog {

    private long logId;

    /**
     * 根据type不一样，对应相应类型的主键id
     */
    private long bid;

    /**
     * 告警类型
     * 1 ： 链路
     * 2 ： 任务
     * 3 :  设备
     */
    @JSONField(serialize=false)
    private int bType ;

    public static final int BTYPE_LINK = 1;
    public static final int BTYPE_TASK = 2;
    public static final int BTYPE_DEVICE = 3;


    /**
     * 报警类型，用来对应报警的知识库检索对应关系
     * @see com.minkey.contants.AlarmEnum
     */
    private int type;

    /**
     * 报警内容
     */
    private String msg;

    /**
     * 告警级别
     */
    private int level;

    private Date createTime;

    public long getLogId() {
        return logId;
    }

    public void setLogId(long logId) {
        this.logId = logId;
    }

    public long getBid() {
        return bid;
    }

    public AlarmLog setBid(long bid) {
        this.bid = bid;
        return this;
    }

    public int getbType() {
        return bType;
    }

    public AlarmLog setbType(int bType) {
        this.bType = bType;
        return this;
    }

    public int getType() {
        return type;
    }

    public AlarmLog setType(int type) {
        this.type = type;
        return this;
    }
    public AlarmLog setType(AlarmEnum alarmEnum) {
        this.type = alarmEnum.getAlarmType();
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public AlarmLog setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public int getLevel() {
        return level;
    }

    public AlarmLog setLevel(int level) {
        this.level = level;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public static String getString8BType(int bType){
        switch (bType){
            case 1 :
                return "链路";
            case 2 :
                return "任务";
            case 3 :
                return "设备";
            default:
                return "链路";
        }
    }

    @Override
    public String toString() {
        return "AlarmLog{" +
                "logId=" + logId +
                ", bid=" + bid +
                ", bType=" + bType +
                ", type=" + type +
                ", msg='" + msg + '\'' +
                ", level=" + level +
                ", createTime=" + createTime +
                '}';
    }
}
