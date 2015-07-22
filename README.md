# hbase-orm
hbase的orm模版方法，类似与JDBC模版方法使用

本jar提供的功能如下:
1.提供了文件（txt或hdfs）行记录与java object的互相转化的实现，通过注解的方式进行标记行列和java属性的对应的关系。目前默认的行列分隔符为\001;
      具体使用方式为:
      (1)在java bean中使用对应的注解，使其和记录列保持一致。具体如下:
	@Stringable(tag = "user",seperator='\001')
	public class User {
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
      (2)在使用时，可以通过TranslateService fileRecordTranslate = new TranslateService(new Class[]{xxx.class});接收的参数为具体的java bean类名

2.提供了操作HBase NoSQL数据库的DAO模板方法的具体实现，实现了CRUD的操作。使用者可以通过简单的配置，即可对hbase的表进行操作，可以对java object进行insert或者传入相应的查询条件，即可获取到对应java objec数据；删除目前还不完善，有待实现。
	具体使用方式为：
	(1)在工程下添加hbase-site.xml配置文件，放置hbase集群的相关信息。
	(2)鉴于目前大部分情况下，项目中都使用spring进行数据源的管理，因此需要在spring加入如下配置,对数据源进行初始化:
		<bean id="dataSourceHbase" class="com.kongkim.hbaseorm.impl.HBaseDataSourceImpl">
			<constructor-arg name="maxSize" value="${pool.maxsize}"/>
		</bean>
	(3)在java bean中使用对应的hbase注解，告知相应的rowkey，列簇等信息。具体事例：
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
	(4)指定具体的DAO，然后就可以在程序中进行相应的注入了，依然是spring配置方式:
		<bean id="xxxDao"
			class="com.kongkim.hbaseorm.impl.HBaseDaoTemplateImpl">
			<property name="hbaseTransfer">
				<bean class="com.kongkim.hbaseorm.translate.impl.CommonHbaseTranslate">
					<constructor-arg value="xxxx"></constructor-arg><!-- 传入指定的java bean的类路径，如com.kongkim.domain.User-->
				</bean>
			</property>
		</bean>
		在程序中使用，即和使用其他dao方式一样。
