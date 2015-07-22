package com.jd.ipc.hbaseorm.translate.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import com.google.gson.GsonBuilder;
import com.jd.ipc.hbaseorm.common.CommonConstants;
import com.jd.ipc.hbaseorm.common.HTableAnnotation;
import com.jd.ipc.hbaseorm.common.HTableAnnotation.Column;
import com.jd.ipc.hbaseorm.common.HTableAnnotation.RowKey;
import com.jd.ipc.hbaseorm.common.HTableAnnotation.TS;
import com.jd.ipc.hbaseorm.translate.IHBaseObjectTransfer;
import com.jd.ipc.tools.stringable.Stringable;

/**
 * 实现普通需求的hbase对象和java object的翻译
 * 提供如下服务 1.将Object输入翻译为Put 2.将从hbase读取的Result数据翻译为Object
 * 
 * @author wangwenbao
 * 
 */
public class CommonHbaseTranslate implements IHBaseObjectTransfer{
	private Class<?> targetClass;

	private final Map<Field, String> objectAnnotationFields = new HashMap<Field, String>();

	private final GsonBuilder gsonBuilder = new GsonBuilder();

	/**
	 * 注册默认支持的解析对象类型
	 */
	static {
		DateConverter dateConverter = new DateConverter();
		dateConverter.setPattern(CommonConstants.DATE_PATTERN);
		ConvertUtils.register(dateConverter, Date.class);
	}

	public CommonHbaseTranslate(String className) {
		try {
			this.targetClass = Class.forName(className);
			Field[] fields = targetClass.getDeclaredFields();
			Class<?> pcl = targetClass.getSuperclass();
			while (!pcl.equals(Object.class)) {
				if (pcl.getAnnotation(Stringable.class) != null) {
					fields = (Field[]) ArrayUtils.addAll(fields,
							pcl.getDeclaredFields());
				}
				pcl = pcl.getSuperclass();
			}
			this.checkObject(fields);
			for (Field field : fields) {
				RowKey id = field.getAnnotation(RowKey.class);
				if (id != null) {
					objectAnnotationFields.put(field, HType.Rowkey.name());
					continue;
				}
				Column column = field.getAnnotation(Column.class);
				if (column != null) {
					objectAnnotationFields.put(field, HType.Column.name());
					continue;
				}
				TS timestamp = field.getAnnotation(TS.class);
				if (timestamp != null) {
					objectAnnotationFields.put(field, HType.TS.name());
					continue;
				}
			}
			gsonBuilder.serializeSpecialFloatingPointValues();
		} catch (ClassNotFoundException e) {
			throw new RowMapperException("给定class" + className + "不存在");
		}
	}

	/**
	 * 将从HTable读取出来的数据转换为Object对象
	 * 
	 * @param result
	 * @return
	 */
	@Override
	public Object toObject(Result result) {
		if (result != null && result.size() > 0) {
			String fieldName = null;
			try {
				Object object = targetClass.newInstance();
				BeanMap m = new BeanMap(object);
				for (Map.Entry<Field, String> entry : objectAnnotationFields
						.entrySet()) {
					Field field = entry.getKey();
					String type = entry.getValue();
					String fieldValue = null;
					fieldName = field.getName();
					if (HType.Rowkey.name().equals(type)) {
						fieldValue = new String(result.getRow(),CommonConstants.ENCODE);
					}
					if (HType.Column.name().equals(type)) {
						Column column = field.getAnnotation(Column.class);
						byte[] v = result.getValue(Bytes.toBytes(column.cf()),
								Bytes.toBytes(column.name()));
						if (v != null && v.length > 0) {
							fieldValue = new String(v,CommonConstants.ENCODE);
						}
					}
					if (HType.TS.name().equals(type)) {
						fieldValue = String.valueOf(result.raw()[0]
								.getTimestamp());
					}
					if (fieldValue != null && fieldValue.length() > 0) {
						// 如果field声明了json注解，说明应该反序列化一个json字符串，否则应该直接进行强制转换
						Stringable.json j = field
								.getAnnotation(Stringable.json.class);
                        // 如果field声明了date注解，说明反序列化时候需要使用特殊的日期格式
						Stringable.date d = field.getAnnotation(Stringable.date.class);
						if (j != null) {
							m.getWriteMethod(fieldName).invoke(
									object,
									gsonBuilder.create().fromJson(fieldValue,
											field.getGenericType()));
						} else if (d != null) {
							m.getWriteMethod(fieldName).invoke(object,
                                    DateUtils.parseDate(fieldValue, new String[] { d.format() }));
                        } else {
							m.getWriteMethod(fieldName).invoke(
									object,
									ConvertUtils.convert(fieldValue,
											field.getType()));
						}
					}
				}
				return object;
			} catch (InvocationTargetException e) {
				throw new RowMapperException("调用" + targetClass.getName() + "."
						+ fieldName + "的setter出错", e);
			} catch (IllegalArgumentException e) {
				throw new RowMapperException(targetClass.getName() + "."
						+ fieldName + "的setter参数类型错误", e);
			} catch (IllegalAccessException e) {
				throw new RowMapperException(targetClass.getName() + "."
						+ fieldName + "没有public的setter", e);
			} catch (InstantiationException e) {
				throw new RowMapperException(
						"newInstance InstantiationException", e);
			} catch (ParseException e) {
				 throw new RowMapperException("转换日期" + targetClass.getName() + "." + fieldName + "时候发生错误", e);
			}
		}
		return null;
	}

	/**
	 * 将Object对象转换为Put对象
	 * 
	 * @param object
	 * @return
	 */
	@Override
	public Put toPut(Object t) {
		if (t != null) {
			BeanMap m = new BeanMap(t);
			byte[] rowKey = null;
			long timestamp = 0L;
			Map<byte[], Map<byte[], byte[]>> cfMap = new HashMap<byte[], Map<byte[], byte[]>>();
			for (Map.Entry<Field, String> entry : objectAnnotationFields
					.entrySet()) {
				Field field = entry.getKey();
				String type = entry.getValue();
				Object fieldValue = null;
				try {
					fieldValue = m.getReadMethod(field.getName()).invoke(t);
				} catch (InvocationTargetException e) {
					throw new RowMapperException("调用" + targetClass.getName()
							+ "." + field.getName() + "的getter出错", e);
				} catch (IllegalArgumentException e) {
					throw new RowMapperException(targetClass.getName() + "."
							+ field.getName() + "的getter参数类型错误", e);
				} catch (IllegalAccessException e) {
					throw new RowMapperException(targetClass.getName() + "."
							+ field.getName() + "没有public的getter", e);
				}
				if (fieldValue != null) {
					if (HType.Rowkey.name().equals(type)) {
						rowKey = fieldValue.toString().getBytes(CommonConstants.ENCODE);
						continue;
					}
					if (HType.Column.name().equals(type)) {
						Column column = field.getAnnotation(Column.class);
						byte[] cfName = Bytes.toBytes(column.cf());
						byte[] columnName = Bytes.toBytes(column.name());
						Map<byte[], byte[]> cMap = null;
						if (cfMap.containsKey(cfName)) {
							cMap = cfMap.get(cfName);
						} else {
							cMap = new HashMap<byte[], byte[]>();
							cfMap.put(cfName, cMap);
						}
						// 如果field声明了json注解，说明应该序列化为一个json字符串，否则应该直接进行强制转换
						Stringable.json j = field
								.getAnnotation(Stringable.json.class);
                        // 如果field声明了date注解，说明反序列化时候需要使用特殊的日期格式
						Stringable.date d = field.getAnnotation(Stringable.date.class);
						if (j != null) {
							fieldValue = gsonBuilder.create()
									.toJson(fieldValue);
						} else if (d != null) {
							fieldValue = DateFormatUtils.format((Date)fieldValue, d.format());
                        } else if (fieldValue instanceof Date) {
                        	fieldValue = DateFormatUtils.format((Date)fieldValue, CommonConstants.DATE_PATTERN);
                        }
						cMap.put(columnName,fieldValue.toString().getBytes(CommonConstants.ENCODE));
						continue;
					}
					if (HType.TS.name().equals(type)) {
						timestamp = (Long) ConvertUtils.convert(fieldValue.toString(),
								Long.class);
						continue;
					}
				}
			}
			if (rowKey != null && rowKey.length > 0) {
				Put put = new Put(rowKey);
				if (cfMap.size() > 0) {
					if (timestamp == 0) {
						timestamp = new Date().getTime();
					}
					for (Map.Entry<byte[], Map<byte[], byte[]>> entry : cfMap
							.entrySet()) {
						byte[] key = entry.getKey();
						Map<byte[], byte[]> value = entry.getValue();
						for (Map.Entry<byte[], byte[]> e : value.entrySet()) {
							put.add(key, e.getKey(), timestamp, e.getValue());
						}
					}
					return put;
				}
			}
		}
		return null;
	}

	@Override
	public String getTableName() {
		HTableAnnotation ht = targetClass.getAnnotation(HTableAnnotation.class);
		return ht.name();
	}

	/**
	 * 检查对象是否合法，必须包含rowKey，CF，column
	 * 
	 * @param targetClass
	 */
	private void checkObject(Field[] fields) {
		int c1 = 0;
		int c2 = 0;
		for (Field field : fields) {
			RowKey id = field.getAnnotation(RowKey.class);
			if (id != null) {
				c1++;
				continue;
			}
			Column column = field.getAnnotation(Column.class);
			if (column != null) {
				String cf = column.cf();
				String c = column.name();
				if (StringUtils.isNotEmpty(cf) && StringUtils.isNotEmpty(c)) {
					c2++;
				}
				continue;
			}
		}
		if (c1 != 1 || c2 <= 0) {
			throw new RowMapperException("需要存入的对象不合法,请检查对象的定义.");
		}
	}

	/**
	 * 翻译对象的时候失败，一般包括以下几种可能：
	 * <ul>
	 * <li>声明了@Stringable.index的域，但域没有对应的public getter&&setter</li>
	 * </ul>
	 * 
	 * @author wangwenbao
	 */
	public static class RowMapperException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		RowMapperException(String msg) {
			super(msg);
		}

		RowMapperException(String msg, Throwable e) {
			super(msg, e);
		}
	}

	enum HType {
		Rowkey, Column, TS
	}
}
