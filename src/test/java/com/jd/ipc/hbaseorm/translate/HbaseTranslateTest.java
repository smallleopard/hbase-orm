package com.jd.ipc.hbaseorm.translate;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.junit.Test;

import com.jd.ipc.hbaseorm.common.CommonConstants;
import com.jd.ipc.hbaseorm.common.HTableAnnotation;
import com.jd.ipc.hbaseorm.translate.impl.CommonHbaseTranslate;

public class HbaseTranslateTest {

	CommonHbaseTranslate mapper = new CommonHbaseTranslate(User.class.getName());

	Logger logger = Logger.getLogger(this.getClass());

	@Test
	public void testToObject() throws ParseException {
		List<KeyValue> kvs = new ArrayList<KeyValue>();
		KeyValue kv1 = new KeyValue("1000008".getBytes(CommonConstants.ENCODE), "Info".getBytes(CommonConstants.ENCODE),
				"name".getBytes(CommonConstants.ENCODE), "bjwangwenbao".getBytes(CommonConstants.ENCODE));
		kvs.add(kv1);
		Result result = new Result(kvs);
		User m = (User) mapper.toObject(result);
		Assert.assertEquals("bjwangwenbao", m.getName());
	}

	@Test
	public void testToPut() {
		User user = new User();
		user.setId("1234");
		user.setName("bj123");
		user.setAge(10);
		user.setTime(new Date().getTime());
		Put put = mapper.toPut(user);
		String row = Bytes.toString(put.getRow());
		Assert.assertEquals(row, user.getId());
	}

	@HTableAnnotation(name = "user")
	public static class User {
		@HTableAnnotation.RowKey
		private String id;
		@HTableAnnotation.Column(cf = "Info", name = "age")
		private int age;
		@HTableAnnotation.Column(cf = "Info", name = "name")
		private String name;
		@HTableAnnotation.TS
		private Long time;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Long getTime() {
			return time;
		}

		public void setTime(Long time) {
			this.time = time;
		}
	}
	
}
