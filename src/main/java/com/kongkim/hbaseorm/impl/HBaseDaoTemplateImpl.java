package com.kongkim.hbaseorm.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.util.Bytes;

import com.kongkim.hbaseorm.HBaseDaoTemplate;
import com.kongkim.hbaseorm.HBaseDataSource;
import com.kongkim.hbaseorm.common.CommonConstants;
import com.kongkim.hbaseorm.translate.IHBaseObjectTransfer;

/**
 * 提供的HBase操作的模板方法的具体实现，实现了CRUD的操作
 * @author wangwenbao
 *
 */
public class HBaseDaoTemplateImpl implements HBaseDaoTemplate {

	/**
	 * 需要在spring初始化hbase的相关连接，和jdbc类似
	 */
	private HBaseDataSource dataSourceHbase;

	/**
	 * 需要指定相应的翻译方式
	 */
	private IHBaseObjectTransfer hbaseTransfer;

	
	@Override
	public List<Object> find(String family) throws IOException {
		Scan scan = new Scan();
		scan.addFamily(family.getBytes(CommonConstants.ENCODE));
		return find(scan);
	}

	@Override
	public List<Object> find(String family, String qualifier)
			throws IOException {
		Scan scan = new Scan();
		scan.addColumn(family.getBytes(CommonConstants.ENCODE), qualifier.getBytes(CommonConstants.ENCODE));
		return find(scan);
	}

	@Override
	public List<Object> find(Scan scan) throws IOException {
		List<Object> results = new ArrayList<Object>();
		HTableInterface table = this.getTable();
		ResultScanner rs = null;
		try {
			rs = table.getScanner(scan);
			for (Result r : rs) {
				results.add(hbaseTransfer.toObject(r));
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
			this.releaseTable(table);
		}
		return results;
	}

	@Override
	public List<Object> findByRowRange(String startRow, String stopRow)
			throws IOException {
		Scan scan = new Scan();
		scan.setStartRow(startRow.getBytes(CommonConstants.ENCODE));
		scan.setStopRow(stopRow.getBytes(CommonConstants.ENCODE));
		return this.find(scan);
	}

	@Override
	public List<Object> findByPagination(String startRow, int pageSize)
			throws IOException {
		final byte[] POSTFIX = new byte[] { 0x00 };
		byte[] lastRow = null;
		if (StringUtils.isNotEmpty(startRow)) {
			lastRow = startRow.getBytes(CommonConstants.ENCODE);
		}
		Scan scan = new Scan();
		if (lastRow != null) {
			// 注意这里添加了POSTFIX操作，不然死循环了
			scan.setStartRow(Bytes.add(lastRow, POSTFIX));
		}
		PageFilter pageFilter = new PageFilter(pageSize);
		scan.setFilter(pageFilter);
		return this.find(scan);
	}

	@Override
	public Object get(String rowName) throws IOException {
		return get(rowName, null, null);
	}

	@Override
	public Object get(String rowName, String familyName) throws IOException {
		return get(rowName, familyName, null);
	}

	@Override
	public Object get(String rowName, String familyName, String qualifier)
			throws IOException {
		HTableInterface table = this.getTable();
		Get get = new Get(rowName.getBytes(CommonConstants.ENCODE));
		if (familyName != null) {
			byte[] family = familyName.getBytes(CommonConstants.ENCODE);
			if (qualifier != null) {
				get.addColumn(family, qualifier.getBytes(CommonConstants.ENCODE));
			} else {
				get.addFamily(family);
			}
		}
		try {
			Result result = table.get(get);
			return hbaseTransfer.toObject(result);
		} finally {
			this.releaseTable(table);
		}
	}

	@Override
	public void insert(Object t,boolean isWriteLog) throws IOException {
		if (t != null) {
			List<Object> tList = new ArrayList<Object>();
			tList.add(t);
			this.insert(tList,isWriteLog);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void insert(List<Object> tList,boolean isWriteLog) throws IOException {
		if (tList != null && tList.size() > 0) {
			HTableInterface table = null;
			try {
				table = this.getTable();
				table.setAutoFlush(false);
				List<Put> puts = new ArrayList<Put>();
				for (Object t : tList) {
					Put put = hbaseTransfer.toPut(t);
					put.setWriteToWAL(isWriteLog);
					puts.add(put);
				}
				table.put(puts);
				table.flushCommits();
			} finally {
				this.releaseTable(table);
			}
		}
	}

	@Override
	public void delete(String rowName) throws IOException {
		Delete delete = new Delete(rowName.getBytes(CommonConstants.ENCODE));
		this.delete(delete);
	}

	@Override
	public void deleteAll() {
		// TODO 待实现，需谨慎使用
	}

	@Override
	public void delete(Delete delete) throws IOException {
		HTableInterface table = this.getTable();
		try {
			table.delete(delete);
		} finally {
			this.releaseTable(table);
		}
	}

	private HTableInterface getTable() {
		return dataSourceHbase.getHTable(hbaseTransfer.getTableName());
	}

	private void releaseTable(HTableInterface table) {
		dataSourceHbase.closeHTable(table);
	}

	/**
	 * @param dataSourceHbase the dataSourceHbase to set
	 */
	public void setDataSourceHbase(HBaseDataSource dataSourceHbase) {
		this.dataSourceHbase = dataSourceHbase;
	}

	/**
	 * @return the hbaseTransfer
	 */
	public IHBaseObjectTransfer getHbaseTransfer() {
		return hbaseTransfer;
	}

	/**
	 * @param hbaseTransfer the hbaseTransfer to set
	 */
	public void setHbaseTransfer(IHBaseObjectTransfer hbaseTransfer) {
		this.hbaseTransfer = hbaseTransfer;
	}

	/**
	 * @return the dataSourceHbase
	 */
	public HBaseDataSource getDataSourceHbase() {
		return dataSourceHbase;
	}
}
