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
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.jar.AbstractJarMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.ReflectionUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * Build a SPI(service provider interface) JAR from the current project.
 * 
 * @author Deman
 */
@Mojo(name = "spi", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE)
public class SpiMojo extends AbstractJarMojo {
	
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
		File outputDirectory = new File(getProject().getBuild().getOutputDirectory());
		if (skip || !outputDirectory.exists() || outputDirectory.list().length < 1) {
			getLog().info("Skipping packaging of the spi-jar");
		} else {
			if (!spiClassesDirectory.exists()) {
				spiClassesDirectory.mkdirs();
			}
			MojoContextHolder.setJarMojo(this);
			List<String> list = getProject().getCompileSourceRoots();
			for (String sourcepath : list) {
				try {
					String[] includes = (String[]) ReflectionUtils.getValueIncludingSuperclasses("includes", this);
					String[] excludes = (String[]) ReflectionUtils.getValueIncludingSuperclasses("excludes", this);
					if (includes == null || includes.length == 0) {
						includes = DEFAULT_INCLUDES;
					}
					if (excludes == null || excludes.length == 0) {
						excludes = DEFAULT_EXCLUDES;
					}
					List<File> files = FileUtils.getFiles(new File(sourcepath),	StringUtils.join(includes, ","), StringUtils.join(excludes, ","));
					String[] docArgs = new String[files.size() + 2];
					docArgs[0] = "-doclet";
					docArgs[1] = SpiExtractor.class.getName();
					for (int i = 0; i < files.size(); i++) {
						docArgs[i + 2] = files.get(i).getAbsolutePath();
					}
					com.sun.tools.javadoc.Main.execute(docArgs);
				} catch (Exception e) {
					getLog().error("Caught an exception while excuting spi-maven-plugin", e);
				}
			}
			super.execute();
		}
	}
	
}
