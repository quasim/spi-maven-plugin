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

import java.io.File;
import java.io.IOException;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.ReflectionUtils;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;

/**
 * 从maven构建输出目录读取class文件，过滤其源码文件中javadoc包含有@hide标签的类或方法，然后写入到spi输出目录
 * 
 * @author Deman
 */
public class SpiExtractor extends Doclet {

	private static final String JAVADOC_HIDE_TAG = "@hide";

	public static boolean start(RootDoc root) {
		MavenProject mavenProject = getMojoValue("project");
		File spiClassesDirectory = getMojoValue("spiClassesDirectory");
		
		ClassPool pool = ClassPool.getDefault();
		try {
			pool.insertClassPath(mavenProject.getBuild().getOutputDirectory()) ;
			
			ClassDoc[] classes = root.classes();
			for (ClassDoc classDoc : classes) {
				if (!hasHideTag(classDoc)) {
					CtClass cc = pool.get(classDoc.qualifiedName());
					MethodDoc[] methodDocs = classDoc.methods();
					if (hasHideTag(methodDocs)) {
						for (MethodDoc methodDoc : methodDocs) {
							if (hasHideTag(methodDoc)) {
								Parameter[] parameters = methodDoc.parameters();
								CtMethod cm = cc.getDeclaredMethod(methodDoc.name(), ctParameters(parameters));
								cc.removeMethod(cm);
							}
						}
					}
					cc.writeFile(spiClassesDirectory.getAbsolutePath());
				}
			}
			
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (CannotCompileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * class javadoc是否含有hide taglet
	 * @param classDoc
	 * @return
	 */
	private static boolean hasHideTag(ClassDoc classDoc) {
		Tag[] tags = classDoc.tags();
		for (Tag tag : tags) {
			if (tag.name().equals(JAVADOC_HIDE_TAG)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * method javadoc是否含有hide taglet
	 * @param methodDocs
	 * @return
	 */
	private static boolean hasHideTag(MethodDoc[] methodDocs) {
		for (MethodDoc methodDoc : methodDocs) {
			if (hasHideTag(methodDoc)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * method javadoc是否含有hide taglet
	 * @param methodDoc
	 * @return
	 */
	private static boolean hasHideTag(MethodDoc methodDoc) {
		Tag[] tags = methodDoc.tags();
		for (Tag tag : tags) {
			if (tag.name().equals(JAVADOC_HIDE_TAG)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Parameter转换为CtClass
	 * @param parameters
	 * @return
	 * @throws NotFoundException
	 */
	private static CtClass[] ctParameters(Parameter[] parameters) throws NotFoundException {
		CtClass[] cc = new CtClass[parameters.length];
		for (int i = 0; i < cc.length; i++) {
			cc[i] = ClassPool.getDefault().get(parameters[i].type().qualifiedTypeName());
		}
		return cc;
	}

	/**
	 * 反射获取Mojo属性值
	 * @param fieldName
	 * @return
	 */
	private static <T> T getMojoValue(String fieldName) {
		try {
			return (T) ReflectionUtils.getValueIncludingSuperclasses(fieldName,	MojoContextHolder.getJarMojo());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

}
