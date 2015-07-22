package com.kongkim.hbaseorm.translate;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;

/**
 * hbase对象和java object对象互相转化的接口类
 * 1.已实现了普通需求的实现.
 * 2.如有特殊需求，需根据其特点进行相应的实现.
 * @author wangwenbao
 *
 */
public interface IHBaseObjectTransfer {

	/**
	 * 对hbase result进行解析,并将其转化为java object
	 * @param result
	 * @return
	 */
	public Object toObject(Result result);
	
	/**
	 * 将java object转化为hbase的put对象，进行相应的插入等操作
	 * @param o
	 * @return
	 */
	public Put toPut(Object o);
	
	/**
	 * 获取hbase表名
	 * @return
	 */
	public String getTableName();
}
