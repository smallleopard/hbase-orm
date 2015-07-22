package com.jd.ipc.hbaseorm.common;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明了该注解的类，描述htable的具体结构，可进行row和object的转换。
 * 
 * @author wangwenbao
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface HTableAnnotation{
	
	/**
	 * 定义htable的名称
	 * @return
	 */
	public String name();
	
	/**
	 * 定义rowkey，rowkey需在对应的get,set方法实现
	 * @author wangwenbao
	 *
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD})
	public static @interface RowKey {
	}
	
	/**
	 * 定义需要存储Column的名称以及对应的列族名称
	 * @author wangwenbao
	 *
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD})
	public static @interface Column {
		public String name();
		public String cf();
	}

	/**
	 * 给定column的时间戳,可以不填，默认会给当前系统时间
	 * @author wangwenbao
	 *
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD})
	public static @interface TS {
	}
}
