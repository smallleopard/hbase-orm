package com.jd.ipc.hbaseorm.impl;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.log4j.Logger;

import com.jd.ipc.hbaseorm.HBaseDataSource;

/**
 * 实现htable工厂的类，可以通过spring配置zookpeer地址以及线程池的大小，来初始化hbase Pool
 * @author wangwenbao
 *
 */
public class HBaseDataSourceImpl implements HBaseDataSource {

	static Logger logger = Logger.getLogger(HBaseDataSourceImpl.class);
	
	static int NUM = 10;
	 
    private HConnection hConnection;
 
    public HBaseDataSourceImpl(int maxSize) {
    	int queueCapacity = 1000;
		long keepAliveTime = 0;
        Configuration conf = HBaseConfiguration.create();
        ExecutorService executorService = new ThreadPoolExecutor(maxSize, maxSize, keepAliveTime,
				TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(
						queueCapacity),
				new ThreadPoolExecutor.CallerRunsPolicy());
        try {
        	hConnection = HConnectionManager.createConnection(conf, executorService);
        } catch(IOException e) {
        	throw new HbaseSystemException("get hbase connection is error!");
        }
    }
 
    @Override
    public HTableInterface getHTable(String tableName) {
        if(StringUtils.isEmpty(tableName)) {
        	throw new HbaseSystemException("Missing necessity tableName!");
        }
		try {
			HTableInterface htable = hConnection.getTable(tableName);
			if(htable == null) {
	        	throw new HbaseSystemException("the tableName of table is not exist!");
	        }
			return htable;
		} catch (IOException e) {
			throw new HbaseSystemException("get hbase connection is error!");
		}
    }
 
    @Override
    public void closeHTable(HTableInterface hTableInterface) {
		try {
			if(hTableInterface != null) {
				hTableInterface.close();
			}
		} catch (IOException ex) {
			throw new HbaseSystemException("release htable faile");
		}
    }
    
	public static class HbaseSystemException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		HbaseSystemException(String msg) {
			super(msg);
		}

		HbaseSystemException(String msg, Throwable e) {
			super(msg, e);
		}
	}
}
