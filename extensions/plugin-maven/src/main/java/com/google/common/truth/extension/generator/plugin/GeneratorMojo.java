package com.google.common.truth.extension.generator.plugin;

import com.google.common.truth.extension.generator.SourceClassSets;
import com.google.common.truth.extension.generator.TruthGeneratorAPI;
import com.google.common.truth.extension.generator.internal.TruthGenerator;
import com.google.common.truth.extension.generator.internal.model.ThreeSystem;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Optional.of;
import static org.apache.maven.plugins.annotations.ResolutionScope.TEST;


/**
 * Goal which touches a timestamp file.
 */
@Getter
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES, requiresDependencyResolution = TEST, requiresProject = true)
public class GeneratorMojo extends AbstractMojo {

  private static final String[] INCLUDE_ALL_CLASSES = { ".*" };

  /**
   * Current maven project
   */
  @Parameter(property = "project", required = true, readonly = true)
  public MavenProject project;

  /**
   * Location of the file.
   */
  @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
  private File outputDirectory;

  /**
   * Package where generated assertion classes will reside.
   * <p/>
   * If not set (or set to empty), each assertion class is generated in the package of the corresponding class to assert.
   * For example the generated assertion class for com.nba.Player will be com.nba.PlayerAssert (in the same package as Player).
   * Defaults to ''.<br>
   * <p/>
   * Note that the Assertions entry point classes package is controlled by the entryPointClassPackage property.
   */
  @Parameter(defaultValue = "", property = "truth.generateAssertionsInPackage")
  public String generateAssertionsInPackage;

  /**
   * Flag specifying whether to clean the directory where assertions are generated. The default is false.
   */
  @Parameter(defaultValue = "false", property = "truth.cleanTargetDir")
  public boolean cleanTargetDir;

  /**
   * List of packages to generate assertions for.
   */
  @Parameter(property = "truth.packages")
  public String[] packages;

  /**
   * List of classes to generate assertions for.
   */
  @Parameter(property = "truth.classes")
  public String[] classes;

  /**
   * Generated assertions are limited to classes matching one of the given regular expressions, default is to include
   * all classes.
   */
  @Parameter(property = "truth.includes")
  public String[] includes = INCLUDE_ALL_CLASSES;

  /**
   * If class matches one of the given regex, no assertions will be generated for it, default is not to exclude
   * anything.
   */
  @Parameter(property = "truth.excludes")
  public String[] excludes = new String[0];


  /**
   * An optional package name for the Assertions entry point class. If omitted, the package will be determined
   * heuristically from the generated assertions.
   */
  @Parameter(property = "truth.entryPointClassPackage")
  public String entryPointClassPackage;

  /**
   * Skip generating classes, handy way to disable the plugin.
   */
  @Parameter(property = "truth.skip")
  public boolean skip = false;

  @Parameter(property = "truth.recursive")
  public boolean recursive = true;

  /**
   * for testing
   */
  private Map<Class<?>, ThreeSystem> result;

  static String shouldHaveNonEmptyPackagesOrClasses() {
    return format(
            "Parameter 'packages' or 'classes' must be set to generate assertions.%n[Help] https://github.com/joel-costigliola/assertj-assertions-generator-maven-plugin");
  }

  public void execute() throws MojoExecutionException {
    this.result = runGenerator();

    File f = outputDirectory;

    if (!f.exists()) {
      f.mkdirs();
    }

    File touch = new File(f, "touch.txt");

    FileWriter w = null;
    try {
      w = new FileWriter(touch);

      w.write("touch.txt");
    } catch (IOException e) {
      throw new MojoExecutionException("Error creating file " + touch, e);
    } finally {
      if (w != null) {
        try {
          w.close();
        } catch (IOException e) {
          // ignore
        }
      }
    }
  }

  @SneakyThrows
  private Map<Class<?>, ThreeSystem> runGenerator() {
    TruthGenerator tg = TruthGeneratorAPI.create();
    tg.setEntryPoint(of(entryPointClassPackage));

    SourceClassSets ss = new SourceClassSets(getEntryPointClassPackage());

    ss.generateFrom(getProjectClassLoader(), getClasses());
//    ss.generatefrom(getPackages());

    Map<Class<?>, ThreeSystem> generated = tg.generate(ss);
    return generated;
  }

  private ClassLoader getProjectClassLoader() throws DependencyResolutionRequiredException, MalformedURLException {
    List<String> classpathElements = new ArrayList<String>(project.getCompileClasspathElements());
    classpathElements.addAll(project.getTestClasspathElements());
    List<URL> classpathElementUrls = new ArrayList<>(classpathElements.size());
    for (String classpathElement : classpathElements) {
      classpathElementUrls.add(new File(classpathElement).toURI().toURL());
    }
    return new URLClassLoader(classpathElementUrls.toArray(new URL[0]), Thread.currentThread().getContextClassLoader());
  }
}
