package org.oakio.dubbo.demo.consumer.complete;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.oakio.dubbo.demo.spi.DemoService;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.Assert;

public class Main {
	
	static final org.apache.log4j.Logger logger = LogManager.getLogger(Main.class);

	public static void main(String[] args) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				logger.info("consumer[complete] shutdown!");
			}
		});

		AbstractApplicationContext context = new ClassPathXmlApplicationContext("classpath*:META-INF/spring/*.xml");
		context.start();

		DemoService demoService = (DemoService) context.getBean("demoService", DemoService.class);

		Assert.notEmpty(demoService.find("select * from dual"));
		Assert.isTrue(demoService.save("save operate."));
		Assert.isTrue(demoService.update("update operate."));
		Assert.isTrue(demoService.deleteById(0));
		
		logger.info("consumer[complete] finished!");
		
		System.exit(0);
	}

}
