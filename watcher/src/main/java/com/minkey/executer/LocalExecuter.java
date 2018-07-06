package com.minkey.executer;

import com.minkey.entity.ResultInfo;
import com.minkey.exception.SysException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class LocalExecuter {
    private final static Logger logger = LoggerFactory.getLogger(LocalExecuter.class);

    public static ResultInfo exec(String command) throws SysException {
        String line = null;

        Process pro = null;
        try {
            pro = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            throw new SysException("读取sh执行异常", e);
        }

        //返回的结果可能是标准信息,也可能是错误信息,所以两种输出都要获取
        //一般情况下只会有一种输出.
        //但并不是说错误信息就是执行命令出错的信息,如获得远程java JDK版本就以
        //ErrStream来获得.
        InputStream stdStream = pro.getInputStream();
        InputStream errStream = pro.getErrorStream();

        byte[] tmp = new byte[1024];
        //执行SSH返回的结果
        StringBuffer strBuffer = new StringBuffer();

        StringBuffer errResult = new StringBuffer();
        try {
            //开始获得SSH命令的结果
            while (true) {
                //获得错误输出
                while (errStream.available() > 0) {
                    int i = errStream.read(tmp, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                    errResult.append(new String(tmp, 0, i,"utf-8"));
                }

                //获得标准输出
                while (stdStream.available() > 0) {
                    int i = stdStream.read(tmp, 0, 1024);
                    if (i < 0) {
                        break;
                    }
//                    strBuffer.append(IOUtils.toString(stdStream,"utf-8"));
                    strBuffer.append(new String(tmp, 0, i,"utf-8"));
                }
                if (!pro.isAlive()) {
                    int code = pro.exitValue();
                    logger.info("exit-status: " + code);
                    ResultInfo result = new ResultInfo(code, strBuffer.toString(), errResult.toString());
                    return result;
                }
            }
        } catch (IOException e) {
            throw new SysException("读取本地sh执行结果异常", e);
        } finally {
            pro.destroy();
        }

    }


}