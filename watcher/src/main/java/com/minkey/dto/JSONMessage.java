package com.minkey.dto;

import com.alibaba.fastjson.JSONObject;
import com.minkey.contants.ErrorCodeEnum;
import com.minkey.util.OSUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * JSON结果返回封装。
 * 
 * @author 
 * 
 */
public class JSONMessage {
    Logger logger = LoggerFactory.getLogger(OSUtil.class);
    /** 操作成功 */
    private static final int JSON_RESULT_SUCCESS = ErrorCodeEnum.SUCCESS.getCode();

    /** 操作失败 */
    private static final int JSON_RESULT_FAILED = ErrorCodeEnum.UNKNOWN.getCode();

    /** 状态 */
    private int code = JSONMessage.JSON_RESULT_SUCCESS;

    /** 错误信息描述 */
    private String rdesc;

    /**
     * 返回数据 
     */
    private JSONObject data;
    
    private Date time = null;
    
	protected JSONMessage() {
		super();
	}

	private JSONMessage(final int code, final String rdesc) {
    	super();
    	this.code = code;
    	this.rdesc = rdesc;
    }
   
    /**
     * 创建成功的JsonResult对象。
     * 
     * @return
     */
    public static JSONMessage createSuccess() {
        final JSONMessage jsonResult = new JSONMessage(JSONMessage.JSON_RESULT_SUCCESS, null);
        return jsonResult;
    }

    /**
     * 创建成功的JsonResult对象。
     * 
     * @return
     */
    public static JSONMessage createSuccess(String rdesc) {
        final JSONMessage jsonResult = new JSONMessage(JSONMessage.JSON_RESULT_SUCCESS, rdesc);
        return jsonResult;
    }
    
    /**
     * 创建失败的JsonResult对象。
     * 
     * @return
     */
    public static JSONMessage createFalied() {
    	final JSONMessage jsonResult = new JSONMessage(JSONMessage.JSON_RESULT_FAILED, "System sneak off.");
    	return jsonResult;
    }

    /**
     * 创建失败的JsonResult对象。
     * 
     * @return
     */
    public static JSONMessage createFalied(final String rdesc) {
        final JSONMessage jsonResult = new JSONMessage(JSONMessage.JSON_RESULT_FAILED, rdesc);
        return jsonResult;
    }
    
    /**
     * 自定义失败code
     * @param code  请从标准ErrorCodeEnum中获取
     * @param rdesc
     * @return
     */
    public static JSONMessage createFalied(final int code, final String rdesc){
        final JSONMessage jsonResult = new JSONMessage(code, rdesc);
        return jsonResult;
    }
    
    public int getCode() {
        return code;
    }

    public void setData(JSONObject data) {
		this.data = data;
	}

	public JSONObject getData() {
        return data;
    }
    public Date getTime() {
		return time;
	}
	
	public String getRdesc() {
		return rdesc;
	}

	public void setRdesc(String rdesc) {
		this.rdesc = rdesc;
	}

	/**
	 * 增加数据,放置jsonObject 并转换成string赋值
	 * @param data
	 * @return
	 */
	public JSONMessage addData(JSONObject data) {
		if(data != null){
			this.data = data;
		}
		return this;
	}

	@Override
	public String toString() {
		this.time = new Date();
		//屏蔽返回是msg信息
//		this.rdesc = null;
		try {
			return JSONObject.toJSONString(this);
		} catch (Exception e) {
		    logger.error("JSONMessage to String exception",e);
			return JSONMessage.createFalied("return jsonObject to String exception").toString();
		}
	}
}
