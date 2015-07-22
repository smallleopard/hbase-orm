package com.jd.ipc.hbaseorm.common;

import java.nio.charset.Charset;

/**
 * 常量定义
 * 
 * @author min
 */
public final class CommonConstants {
    private CommonConstants() {
    }

    /**
     * 默认的编码格式
     */
    public static final Charset ENCODE = Charset.forName("utf-8");
    /**
     * 默认的日期序列化格式
     */
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    /**
     * 默认的带时间的日期序列化格式
     */
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String SFS_DATE_PATTERN = "yyyyMMdd";
    public static final String FDC_DATE_PATTERN = "yyyy/MM/dd";
    /**
     * 默认的日期序列化格式
     */
    public static final String[] DATE_PATTERNS = new String[] { DATE_PATTERN, DATETIME_PATTERN, SFS_DATE_PATTERN, FDC_DATE_PATTERN };
    /**
     * 默认的列分隔符
     */
    public static final String DEFAULT_SEPERATOR = String.valueOf('\001');
}
