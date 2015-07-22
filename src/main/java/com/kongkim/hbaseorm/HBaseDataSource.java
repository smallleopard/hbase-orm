package com.kongkim.hbaseorm;

import org.apache.hadoop.hbase.client.HTableInterface;

/**
 * htable的工厂接口
 * @author wangwenbao
 *
 */
public interface HBaseDataSource {
	
    /**
     * 通过 tableName 来获取这个 Table
     */
    HTableInterface getHTable(String tableName);
 
    /**
     * 关闭某个table,将其归还到htablepool中
     */
    void closeHTable(HTableInterface hTableInterface);
}
