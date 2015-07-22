package com.kongkim.hbaseorm.common;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明了该注解的类，说明是一个可从String反序列化的对象。需要声明反序列化文本中列分隔符与列数。
 * 
 * @author xieweiinfo
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Stringable {
	/**
	 * 文本开头的标记位，标记出是什么数据。默认为空字符串，如果不填写，则需要参考文件路径中的关键字来识别。
	 * 
	 * @return
	 * @author xieweiinfo
	 * @date 2013-7-9
	 */
	public String tag() default "";

	/**
	 * 文本的分隔符
	 * 
	 * @author xieweiinfo
	 * @date 2015-02-26
	 * 
	 * @return
	 */
	public char seperator() default '\t';

	/**
	 * 文件路径中包含的关键字，标记出是什么数据。默认为空字符串，如果不填写，则需要根据文件开头的标记物来识别。
	 * 可以设置多个关键字，符合其中任何一个，即判定true
	 * 
	 * @return
	 */
	public String[] keys() default {};

	/**
	 * 标注该类字段在文本中所处的顺序。（不包含数据标签所占位置）第一个数据列对应的index=1
	 * 
	 * @author xieweiinfo
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD })
	public static @interface index {
		public int i();
	}

	/**
	 * 标注该类字段应被序列化为一个json串
	 * 
	 * @author xieweiinfo
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD })
	public static @interface json {
	}

	/**
	 * 标注该类字段应被序列化为一个特殊日期的字符串
	 * 
	 * @author xieweiinfo
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD })
	public static @interface date {
		public String format() default CommonConstants.DATE_PATTERN;
	}

	/**
	 * 标注该类字段应被序列化为一个字符串
	 * 
	 * @author wangwenbao
	 */
	@Deprecated
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD })
	public static @interface string {
	}
}
