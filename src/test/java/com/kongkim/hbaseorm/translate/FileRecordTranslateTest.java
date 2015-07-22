/**
 * 
 */
package com.jd.ipc.hbaseorm.translate;

import junit.framework.Assert;

import org.junit.Test;

import com.jd.ipc.tools.stringable.Stringable;
import com.jd.ipc.tools.stringable.TranslateService;
import com.jd.ipc.tools.stringable.TranslateService.TranslateException;

public class FileRecordTranslateTest {
	
	
	@Test
	public void testToString() throws TranslateException {
		TranslateService fileRecordTranslate = new TranslateService(new Class[]{User.class});
		User user = new User();
		user.setAge(112222);
		Assert.assertEquals("user112222", fileRecordTranslate.toString(user));
	}
	
	@Test
	public void testGetObject() throws TranslateException {
		TranslateService fileRecordTranslate = new TranslateService(new Class[]{User.class});
		String s = "user112222";
		User u1 = (User) fileRecordTranslate.getObject(s);
		Assert.assertEquals(112222, u1.getAge());
	}
	
	@Stringable(tag = "user",seperator='\001')
	public static class User {
		@Stringable.index(i=1)
		private String id;
		@Stringable.index(i=2)
		private int age;
		@Stringable.index(i=3)
		private String name;

		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		/**
		 * @return the age
		 */
		public int getAge() {
			return age;
		}
		/**
		 * @param age the age to set
		 */
		public void setAge(int age) {
			this.age = age;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
	}
}
