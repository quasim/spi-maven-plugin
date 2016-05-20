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
import java.util.ArrayList;
import java.util.List;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.jar.AbstractJarMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.ReflectionUtils;
import org.codehaus.plexus.util.StringUtils;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.JavaSource;

/**
 * Build a SPI(service provider interface) JAR from the current project.
 * 
 * @author Deman
 */
@Mojo(name = "spi", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE)
public class SpiMojo extends AbstractJarMojo {
	
	private static final String JAVADOC_HIDE_TAG = "hide";
	
	private static final String[] DEFAULT_EXCLUDES = new String[]{ "**/package.html", "**/internal/**" };

    private static final String[] DEFAULT_INCLUDES = new String[]{ "**/**" };
    
    /**
     * Set this to <code>true</code> to prevent this happen.
     */
    @Parameter( property = "maven.spi.skip" )
    private boolean skip;
    
    /**
	 * Directory containing the classes and resource files that should be packaged into the SPI JAR.
	 */
	@Parameter(defaultValue = "${project.build.directory}/spi-classes/", required = true)
	private File spiClassesDirectory;
	
	/**
	 * Classifier to add to the artifact generated.
	 */
	@Parameter(property = "maven.jar.classifier", defaultValue = "spi")
	private String classifier;

	/**
	 * Return the spi classes directory, so it's used as the root of the jar.
	 */
	@Override
	protected File getClassesDirectory() {
		return spiClassesDirectory;
	}

	@Override
	protected String getClassifier() {
		return classifier;
	}

	/**
	 * @return type of the generated artifact
	 */
	@Override
	protected String getType() {
		return "jar";
	}

	@Override
	public void execute() throws MojoExecutionException {
		ClassPool pool = ClassPool.getDefault();
		try {
			pool.insertClassPath(getProject().getBuild().getOutputDirectory());

			String[] includes = (String[]) ReflectionUtils.getValueIncludingSuperclasses("includes", this);
			String[] excludes = (String[]) ReflectionUtils.getValueIncludingSuperclasses("excludes", this);
			if (includes == null || includes.length == 0) {
				includes = DEFAULT_INCLUDES;
			}
			if (excludes == null || excludes.length == 0) {
				excludes = DEFAULT_EXCLUDES;
			}

			List<File> files = new ArrayList<File>();

			JavaDocBuilder builder = new JavaDocBuilder();
			List<String> compileSourceRoots = getProject().getCompileSourceRoots();
			for (String sourcepath : compileSourceRoots) {
				builder.addSourceTree(new File(sourcepath));
				files.addAll(FileUtils.getFiles(new File(sourcepath), StringUtils.join(includes, ","), StringUtils.join(excludes, ",")));
			}
			JavaSource[] javaSources = builder.getSources();
			for (JavaSource javaSource : javaSources) {
				if (!files.contains(new File(javaSource.getURL().getFile())))
					continue;
				JavaClass[] javaClasses = javaSource.getClasses();
				for (JavaClass javaClass : javaClasses) {
					DocletTag hideTag = javaClass.getTagByName(JAVADOC_HIDE_TAG);
					if (hideTag == null) {
						CtClass cc = pool.get(javaClass.getFullyQualifiedName());
						JavaMethod[] javaMethods = javaClass.getMethods();
						for (JavaMethod javaMethod : javaMethods) {
							hideTag = javaMethod.getTagByName(JAVADOC_HIDE_TAG);
							if (hideTag != null) {
								JavaParameter[] parameters = javaMethod.getParameters();
								CtMethod cm = cc.getDeclaredMethod(javaMethod.getName(), ctParameters(parameters));
								cc.removeMethod(cm);
							}
						}
						cc.writeFile(spiClassesDirectory.getAbsolutePath());
					}
				}

			}
			super.execute();
		} catch (Exception e) {
			throw new MojoExecutionException("Error extract output directory "	+ getProject().getBuild().getOutputDirectory(), e);
		}
	}
	
	private CtClass[] ctParameters(JavaParameter[] parameters) throws NotFoundException {
		CtClass[] cc = new CtClass[parameters.length];
		for (int i = 0; i < cc.length; i++) {
			cc[i] = ClassPool.getDefault().get(parameters[i].getType().getFullyQualifiedName());
		}
		return cc;
	}
	
}
