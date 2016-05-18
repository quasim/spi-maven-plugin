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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.oakio.dubbo.demo.spi.DemoService;

/**
 * 
 * @author Deman
 */
public class DemoServiceImpl implements DemoService {

	private AtomicInteger count = new AtomicInteger(0); 

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Object> find(String sql) {
		count.incrementAndGet();
		return Arrays.asList(new Object[] { "demo" });
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean save(Object object) {
		count.incrementAndGet();
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean deleteById(long id) {
		count.incrementAndGet();
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean update(Object object) {
		count.incrementAndGet();
		return true;
	}
	
	public int getCount() {
		return count.get();
	}

}
