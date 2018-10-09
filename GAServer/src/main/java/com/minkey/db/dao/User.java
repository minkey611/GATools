package com.minkey.db.dao;

import java.util.Date;

/**
 * 用户
 */
public class User {

    /**
     * 用户id，自增主键
     */
    private Long uid;

    /**
     * 用户名称
     */
    private String uName;

    /**
     * 密码
     */
    private String pwd;

    /**
     * 创建人
     */
    private Long createUid;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 用户状态：
     * <br> 1 : 正常
     * <br> 2 : 锁定，密码输入多次后，锁定
     */
    private Integer status = STATUS_NORMAL;

    /**
     * 用户状态：正常
     */
    public static final int  STATUS_NORMAL = 1;
    /**
     * 用户状态：锁定
     */
    public static final int  STATUS_LOCK = 2;

    /**
     * 用户密码输入错误次数
     */
    private Integer wrongPwdNum = 0;

    /**
     * 用户密码输入错误最大次数
     */
    public static final int MAX_WRONGPWDNUM = 5;

    /**
     * 创建时间
     */
    private Date createTime;


    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 用户权限
     * <br> 1 : 只读
     * <br> 2 : 读写
     */
    private Integer auth = AUTH_R;

    /**
     * 用户权限:只读
     */
    public static final int  AUTH_R = 1;
    /**
     * 用户权限： 读写
     */
    public static final int  AUTH_WR = 2;

    /**
     * 用户登陆ip
     */
    private String loginIp;

    /**
     * ip登陆段控制，开始段 0.0.0.0为不控制
     */
    private String loginIpStart;
    /**
     * ip登陆段控制，结束段
     */
    private String loginIpEnd;

    /**
     * 登陆时间控制，开始段，只取时分秒，0代表不控制
     */
    private long loginTimeStart;
    /**
     * 登陆时间控制，结束段，只取时分秒
     */
    private long loginTimeEnd;
    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public Long getCreateUid() {
        return createUid;
    }

    public void setCreateUid(Long createUid) {
        this.createUid = createUid;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getWrongPwdNum() {
        return wrongPwdNum;
    }

    public void setWrongPwdNum(Integer wrongPwdNum) {
        this.wrongPwdNum = wrongPwdNum;
    }

    public Integer getAuth() {
        return auth;
    }

    public void setAuth(Integer auth) {
        this.auth = auth;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getLoginIp() {
        return loginIp;
    }

    public void setLoginIp(String loginIp) {
        this.loginIp = loginIp;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLoginIpStart() {
        return loginIpStart;
    }

    public void setLoginIpStart(String loginIpStart) {
        this.loginIpStart = loginIpStart;
    }

    public String getLoginIpEnd() {
        return loginIpEnd;
    }

    public void setLoginIpEnd(String loginIpEnd) {
        this.loginIpEnd = loginIpEnd;
    }

    public long getLoginTimeStart() {
        return loginTimeStart;
    }

    public void setLoginTimeStart(long loginTimeStart) {
        this.loginTimeStart = loginTimeStart;
    }

    public long getLoginTimeEnd() {
        return loginTimeEnd;
    }

    public void setLoginTimeEnd(long loginTimeEnd) {
        this.loginTimeEnd = loginTimeEnd;
    }
}