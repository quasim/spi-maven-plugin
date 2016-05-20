# spi-maven-plugin
## Introduction
SPI(service provider interface)。类似dubbo等分布式服务框架，服务接口面向企业内部不同研发小组需要限制访问权限，通常将其拆分分散管理，也可在服务提供方使用IP白名单，但最简单有效的办法是在调用方过滤掉无权限的接口和方法。<br>spi-maven-plugin插件在构建jar阶段过滤指定的接口和方法，默认过滤"\*\*/internal/\*\*"包路径的接口和javadoc中包含@hide标签的接口和方法，此特点与Android API十分相似。
## Usage
spi-maven-plugin插件扩展自[maven-jar-plugin](http://maven.apache.org/plugins/maven-jar-plugin/)，插件配置完全相同，前者增加扫描【\<includes> - \<excludes>】包路径下所有class文件并过滤javadoc中包含@hide标签的接口和方法。
## Examples
SPI接口类：
```java
public interface DemoService {
	
	/**
	 * 
	 * public spi.
	 *
	 * @param sql
	 * @return
	 */
	public List<Object> find(String sql);
	
	/**
	 * 
	 * private spi.
	 *
	 * @hide
	 * @param object
	 * @return
	 */
	public boolean save(Object object);
	
	/**
	 * 
	 * private spi.
	 *
	 * @hide
	 * @param id
	 * @return
	 */
	public boolean deleteById(long id);
	
	/**
	 * 
	 * private spi.
	 * 
	 * @hide
	 * @param object
	 * @return
	 */
	public boolean update(Object object);

}
```
maven工程插件配置：
```xml
<plugin>
	<groupId>org.oakio</groupId>
	<artifactId>spi-maven-plugin</artifactId>
	<version>1.0</version>
	<executions>
		<execution>
			<id>generate-spi</id>
			<phase>package</phase>
			<goals>
				<goal>spi</goal>
			</goals>
		</execution>
	</executions>
</plugin>
```
运行mvn package，查看构建目录：<br>
![](https://raw.githubusercontent.com/quasim/spi-maven-plugin/master/screenshot/1.png)<br>
使用jd-gui反编译class：<br>
![](https://raw.githubusercontent.com/quasim/spi-maven-plugin/master/screenshot/2.png)<br>

