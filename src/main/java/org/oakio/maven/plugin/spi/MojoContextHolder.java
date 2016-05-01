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
package org.oakio.maven.plugin.spi;

import org.apache.maven.plugin.AbstractMojo;

/**
 * 在同一线程的生命周期内持有Mojo对象.
 * 
 * @author Deman
 */
public class MojoContextHolder {

	private static ThreadLocal<AbstractMojo> contextHolder = new ThreadLocal<AbstractMojo>();

	public static AbstractMojo getJarMojo() {
		return contextHolder.get();
	}

	public static void setJarMojo(AbstractMojo mojo) {
		contextHolder.set(mojo);
	}
	
}
