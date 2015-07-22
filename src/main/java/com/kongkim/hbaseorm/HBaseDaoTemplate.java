package com.jd.ipc.hbaseorm;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Scan;

/**
 * 提供的HBase操作的模板方法，CRUD操作
 * @author wangwenbao
 *
 */
public interface HBaseDaoTemplate {

	/**
	 * Scans the target table, using the given column family. The content is
	 * processed row by row by the given action, returning a list of domain
	 * objects.
	 * 
	 * @param tableName
	 *            target table
	 * @param family
	 *            column family
	 * @param action
	 *            row mapper handling the scanner results
	 * @return a list of objects mapping the scanned rows
	 */
	List<Object>  find(String family) throws IOException;

	/**
	 * Scans the target table, using the given column family. The content is
	 * processed row by row by the given action, returning a list of domain
	 * objects.
	 * 
	 * @param tableName
	 *            target table
	 * @param family
	 *            column family
	 * @param qualifier
	 *            column qualifier
	 * @param action
	 *            row mapper handling the scanner results
	 * @return a list of objects mapping the scanned rows
	 */
	List<Object> find(String family, String qualifier) throws IOException;

	/**
	 * Scans the target table, using the given row range. The content is
	 * processed row by row by the given action, returning a list of domain
	 * objects.
	 * 
	 * @param tableName
	 * @param family
	 * @param qualifier
	 * @param action
	 * @return
	 * @throws IOException
	 */
	List<Object> findByRowRange(String startRow,String stopRow) throws IOException;

	/**
	 * Scans the target table using the given {@link Scan} object. Suitable for
	 * maximum control over the scanning process. The content is processed row
	 * by row by the given action, returning a list of domain objects.
	 * 
	 * @param tableName
	 *            target table
	 * @param scan
	 *            table scanner
	 * @param action
	 *            row mapper handling the scanner results
	 * @return a list of objects mapping the scanned rows
	 */
	List<Object> find(Scan scan) throws IOException;

	/**
	 * Scans the target table using the Pagination
	 * @param tableName target table
	 * @param startRow start
	 * @param pageSize per page size
	 * @param mapper row mapper handling the scanner results
	 * @return a list of objects mapping the scanned rows
	 * @throws IOException
	 */
	List<Object> findByPagination(String startRow,int pageSize) throws IOException;

	/**
	 * Gets an individual row from the given table. The content is mapped by the
	 * given action.
	 * 
	 * @param tableName
	 *            target table
	 * @param rowName
	 *            row name
	 * @param mapper
	 *            row mapper
	 * @return object mapping the target row
	 */
	Object get(String rowName) throws IOException;

	/**
	 * Gets an individual row from the given table. The content is mapped by the
	 * given action.
	 * 
	 * @param tableName
	 *            target table
	 * @param rowName
	 *            row name
	 * @param familyName
	 *            column family
	 * @param mapper
	 *            row mapper
	 * @return object mapping the target row
	 */
	Object get(String rowName, String familyName) throws IOException;

	/**
	 * Gets an individual row from the given table. The content is mapped by the
	 * given action.
	 * 
	 * @param tableName
	 *            target table
	 * @param rowName
	 *            row name
	 * @param familyName
	 *            family
	 * @param qualifier
	 *            column qualifier
	 * @param mapper
	 *            row mapper
	 * @return object mapping the target row
	 */
	Object get(String rowName, String familyName,String qualifier) throws IOException;

	/**
	 * Insert an object to the given a table.
	 * 
	 * @param tableName
	 *            target table
	 * @param t
	 *            an object
	 * @param mapper
	 *            mapping object to put
	 * @throws IOException
	 */
	void insert(Object t,boolean isWriteLog) throws IOException;

	/**
	 * Insert a list of object to the given a table
	 * 
	 * @param tableName
	 *            target table
	 * @param tList
	 *            a list of object
	 * @param mapper
	 *            mapping object to put
	 * @throws IOException
	 */
	void insert(List<Object> tList,boolean isWriteLog) throws IOException;

	/**
	 * Delete a row of the given a table
	 * @param tableName target table
	 * @param rowName a row name
	 * @throws IOException
	 */
	void delete(String rowName) throws IOException;

	/**
	 * Delete all row of the given a table
	 * TODO:can delete all region,unless be have to
	 * @param tableName
	 * @throws IOException
	 */
	void deleteAll() throws IOException;
	
	/**
	 * 给定删除方式，进行相关删除
	 * @param tableName
	 * @param delete
	 * @throws IOException
	 */
	public void delete(Delete delete) throws IOException;

}
