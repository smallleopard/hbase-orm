package com.kongkim.hbaseorm.common;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import com.google.gson.GsonBuilder;

/**
 * 将Text输入翻译为各种PO的服务
 * 
 * @author xieweiinfo
 */
public class TranslateService {
	private final Set<TextClass> objectClasses;
	private final Map<Class<?>, List<Field>> objectAnnotationFields = new HashMap<Class<?>, List<Field>>();
	private final GsonBuilder gsonBuilder = new GsonBuilder();
	private Logger log = Logger.getLogger(getClass());
	private Set<String> seperators = new HashSet<String>();
	/**
	 * 注册默认支持的解析对象类型
	 */
	static {
		DateConverter dateConverter = new DateConverter();
		dateConverter.setPattern(CommonConstants.DATE_PATTERN);
		ConvertUtils.register(dateConverter, Date.class);
	}

	/**
	 * 指定反序列化文本类型的反序列化服务(只处理指定的文本类型)。降低文本反序列化冲突的可能性。
	 * 
	 * @param cls
	 *            指定的反序列化对象类型(类需要声明@Stringable)
	 */
	public TranslateService(Class<?>[] cls) {
		if (!ArrayUtils.isEmpty(cls)) {

			this.objectClasses = new HashSet<TextClass>();
			for (Class<?> cl : cls) {
				TextClass tc = new TextClass(cl);
				this.seperators.add(String.valueOf(cl.getAnnotation(Stringable.class).seperator()));
				this.objectClasses.add(tc);
				Field[] fields = cl.getDeclaredFields();
				Class<?> pcl = cl.getSuperclass();
				while (!pcl.equals(Object.class)) {
					if (pcl.getAnnotation(Stringable.class) != null) {
						fields = (Field[]) ArrayUtils.addAll(fields, pcl.getDeclaredFields());
					}
					pcl = pcl.getSuperclass();
				}
				// 防止domain中声明Stringable.index顺序不对，先排序
				Field[] sortedFields = new Field[fields.length];
				for (Field f : fields) {
					Stringable.index a = f.getAnnotation(Stringable.index.class);
					if (a != null) {
						sortedFields[a.i() - 1] = f;
					}
				}
				List<Field> annotationFields = new ArrayList<Field>();
				for (Field f : sortedFields) {
					if (f != null)
						annotationFields.add(f);
				}
				this.objectAnnotationFields.put(cl, annotationFields);
			}
		} else {
			throw new RuntimeException("未指定支持的反序列化文本类型");
		}
		gsonBuilder.serializeSpecialFloatingPointValues();
	}

	/**
	 * 反序列化文本为一个@Stringable的对象
	 * 
	 * @param txt
	 * @return 如果是数据中心不可识别的输入数据，或在反序列化输入数据时出现异常，则返回null。转换成功则返回反序列化的对象
	 * @author xieweiinfo
	 * @date 2013-5-28
	 */
	public Object getObject(String txt) throws TranslateException {
		if (StringUtils.isNotEmpty(txt)) {
			for (String seperator : this.seperators) {
				String[] items = txt.split(seperator);
				for (TextClass tc : objectClasses) {
					if (tc.getTag().equals(items[0])) {
						String fieldName = null;
						try {
							Object obj = tc.cls.newInstance();
							BeanMap m = new BeanMap(obj);
							for (Field field : objectAnnotationFields.get(tc.cls)) {
								int i = field.getAnnotation(Stringable.index.class).i();
								if (i < items.length && StringUtils.isNotEmpty(items[i])) {
									fieldName = field.getName();
									// 如果field声明了json注解，说明应该反序列化一个json字符串，否则应该直接进行强制转换
									Stringable.json j = field.getAnnotation(Stringable.json.class);
									// 如果field声明了date注解，说明反序列化时候需要使用特殊的日期格式
									Stringable.date d = field.getAnnotation(Stringable.date.class);
									if (j != null) {
										m.getWriteMethod(fieldName).invoke(obj, gsonBuilder.create().fromJson(items[i], field.getGenericType()));
									} else if (d != null) {
										m.getWriteMethod(fieldName).invoke(obj, DateUtils.parseDate(items[i], new String[] { d.format() }));
									} else {
										m.getWriteMethod(fieldName).invoke(obj, ConvertUtils.convert(items[i], field.getType()));
									}
								}
							}
							return obj;
						} catch (InstantiationException e) {
							throw new TranslateException("newInstance InstantiationException", e);
						} catch (IllegalAccessException e) {
							throw new TranslateException("newInstance IllegalAccessException or " + tc.cls.getName() + "." + fieldName + " 没有public的setter", e);
						} catch (IllegalArgumentException e) {
							throw new TranslateException(tc.cls.getName() + "." + fieldName + "setter参数类型错误", e);
						} catch (InvocationTargetException e) {
							throw new TranslateException("调用" + tc.cls.getName() + "." + fieldName + "setter时候发生错误", e);
						} catch (ParseException e) {
							throw new TranslateException("转换日期" + tc.cls.getName() + "." + fieldName + "时候发生错误", e);
						}
					}
				}
			}
		}
		// modified at 2015-02-25 xiewei
		// 无法反序列化文本时，只要记录一下就好
		// throw new TranslateException("无法将文本反序列化:" + txt);
		log.debug("无法将文本反序列化:" + txt);
		return txt;
	}

	/**
	 * 把一个@Stringable的对象序列化为文本
	 * 
	 * @param obj
	 * @return
	 * @author xieweiinfo
	 * @date 2013年9月5日
	 */
	public String toString(Object obj) throws TranslateException {

		if (obj != null) {
			Stringable an = obj.getClass().getAnnotation(Stringable.class);
			if (an == null)
				throw new RuntimeException(obj.getClass().getName() + "没有声明注解@Stringable");
			StringBuilderWithSeparator sb = new StringBuilderWithSeparator(String.valueOf(an.seperator()));
			sb.append(an.tag());
			BeanMap m = new BeanMap(obj);
			String fieldName = null;
			try {
				if (!this.objectAnnotationFields.containsKey(obj.getClass()))
					throw new TranslateException("新建的TranslateService对象，未设置" + obj.getClass() + "的解析");
				for (Field field : this.objectAnnotationFields.get(obj.getClass())) {
					fieldName = field.getName();
					Object fieldValue = m.getReadMethod(fieldName).invoke(obj);
					if (fieldValue != null) {
						// 如果field声明了json注解，说明应该序列化为一个json字符串，否则应该直接进行强制转换
						Stringable.json j = field.getAnnotation(Stringable.json.class);
						// 如果field声明了date注解，说明反序列化时候需要使用特殊的日期格式
						Stringable.date d = field.getAnnotation(Stringable.date.class);
						if (j != null) {
							sb.append(gsonBuilder.create().toJson(fieldValue));
						} else if (d != null) {
							sb.append(DateFormatUtils.format((Date) fieldValue, d.format()));
						} else if (fieldValue instanceof Date) {
							sb.append(DateFormatUtils.format((Date) fieldValue, CommonConstants.DATE_PATTERN));
						} else {
							sb.append(fieldValue.toString());
						}
					} else
						sb.append("");
				}
			} catch (InvocationTargetException e) {
				throw new TranslateException("调用" + obj.getClass() + "." + fieldName + "的getter出错", e);
			} catch (IllegalArgumentException e) {
				throw new TranslateException(obj.getClass() + "." + fieldName + "的getter参数类型错误", e);
			} catch (IllegalAccessException e) {
				throw new TranslateException(obj.getClass() + "." + fieldName + "没有public的getter", e);
			}
			return sb.toString();
		} else {
			return "";
		}

	}

	/**
	 * 支持反序列化的文本类型
	 * 
	 * @author xieweiinfo
	 */
	private final static class TextClass {
		private final Class<?> cls;
		private final String tag;

		private TextClass(Class<?> cls) {
			this.cls = cls;
			Stringable ant = cls.getAnnotation(Stringable.class);
			if (ant != null) {
				this.tag = ant.tag();
			} else {
				throw new TranslateException(cls.getName() + "应声明注解@StringableClass");
			}
		}

		public String getTag() {
			return tag;
		}

	}

	/**
	 * 序列化或反序列化@Stringable对象时候抛出的异常，一般包括以下几种可能：
	 * <ul>
	 * <li>尝试序列化未声明为@Stringable的对象</li>
	 * <li>已声明了@Stringable的对象，但没有声明为@Stringable.index的域</li>
	 * <li>新建的TranslateService对象，未设置对某个@Stringable对象的支持，但却尝试去序列化这个对象</li>
	 * <li>声明了@Stringable.index的域，但index未按正确顺序声明</li>
	 * <li>声明了@Stringable.index的域，但域没有对应的public getter&&setter</li>
	 * </ul>
	 * 
	 * @author xieweiinfo
	 */
	public static class TranslateException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		TranslateException(String msg) {
			super(msg);
		}

		TranslateException(String msg, Throwable e) {
			super(msg, e);
		}
	}
}
