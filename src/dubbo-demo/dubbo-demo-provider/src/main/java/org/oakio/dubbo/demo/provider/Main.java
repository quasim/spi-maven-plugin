/*
 * Copyright 2016 oakio.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.oakio.dubbo.demo.provider;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * TODO: DOCUMENT ME!
 * 
 * @author Deman
 */
public class Main {
	
	static final org.apache.log4j.Logger logger = LogManager.getLogger(Main.class);
	
	static final int WAITING_TIMEOUT = 60;
	
	/**
	 * TODO: DOCUMENT ME!
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				logger.info("prodiver shutdown!");
			}
		});

		AbstractApplicationContext context = new ClassPathXmlApplicationContext("classpath*:META-INF/spring/*.xml");
		context.start();
		
		DemoServiceImpl demoServiceImpl = (DemoServiceImpl) context.getBean("demoService", DemoServiceImpl.class);
		
		int waitingTime = 0;
		while (demoServiceImpl.getCount() != 5 && waitingTime < WAITING_TIMEOUT) {
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
			}
			waitingTime++;
		}

		int[] port = new int[] { 5678, 1871, 3256, 14141, 41544, 41446 };

		Socket socket = null;
		PrintWriter out = null;
		
		int i = 0;

		while (i < port.length) {
			try {
				socket = new Socket(InetAddress.getLocalHost(), port[i]);
				out = new PrintWriter(socket.getOutputStream());
				if (waitingTime == WAITING_TIMEOUT)
					out.println("fail");
				else
					out.println("ok");
				break;
			} catch (Exception e) {
				e.printStackTrace();
			}
			i++;
		}

		if (out != null) {
			out.flush();
			out.close();
		}
		close(socket);
		
	}

	private static void close(Socket socket) {
		if (socket == null)
			return;
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
