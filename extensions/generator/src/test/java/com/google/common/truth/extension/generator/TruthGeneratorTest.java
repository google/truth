package com.google.common.truth.extension.generator;

import com.google.common.io.Resources;
import com.google.common.truth.StreamSubject;
import com.google.common.truth.extension.generator.internal.SkeletonGenerator;
import com.google.common.truth.extension.generator.internal.TruthGenerator;
import com.google.common.truth.extension.generator.internal.model.ThreeSystem;
import com.google.common.truth.extension.generator.internal.modelSubjectChickens.ThreeSystemChildSubject;
import com.google.common.truth.extension.generator.testModel.*;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.Chronology;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;

@RunWith(JUnit4.class)
public class TruthGeneratorTest {

  private String loadFileToString(String expectedFileName) throws IOException {
    return Resources.toString(Resources.getResource(expectedFileName), Charset.defaultCharset());
  }

  private String trim(String in) {
    return in.replaceAll("(?m)^(\\s)*|(\\s)*$", "");
  }

  @Test
  public void combined() {
    TruthGeneratorAPI truthGeneratorAPI = TruthGeneratorAPI.create();
    String generate = truthGeneratorAPI.combinedSystem(MyEmployee.class);

    assertThat(trim("")).isEqualTo(trim(generate));
  }

  @Test
  public void generate_code() throws IOException {
    // todo need to be able to set base package for all generated classes, kind of like shade, so you cah generate test for classes in other restricted modules
    TruthGeneratorAPI truthGenerator = TruthGeneratorAPI.create();

    Set<Class<?>> classes = new HashSet<>();
    classes.add(MyEmployee.class);
    classes.add(IdCard.class);
    classes.add(Project.class);

    String basePackageName = getClass().getPackage().getName();
    SourceClassSets ss = new SourceClassSets(basePackageName);
    ss.generateAllFoundInPackagesOf(MyEmployee.class);

    // package exists in other module error - needs package target support
    ss.generateFrom(basePackageName, UUID.class);
    ss.generateFromShaded(ZoneId.class, ZonedDateTime.class, Chronology.class);

    Map<Class<?>, ThreeSystem> generated = truthGenerator.generate(ss);

    assertThat(generated.size()).isAtLeast(classes.size());
    Set<? extends Class<?>> generatedSourceClasses = generated.values().stream().map(x -> x.classUnderTest).collect(Collectors.toSet());
    assertThat(generatedSourceClasses).containsAtLeast(UUID.class, ZonedDateTime.class, MyEmployee.State.class);

    String expectedMyEmployeeParent = loadFileToString("expected/MyEmployeeParentSubject.java.txt");
    String expectedMyEmployeeChild = loadFileToString("expected/MyEmployeeChildSubject.java.txt");
    String expectedMyEmployeeMiddle = loadFileToString("expected/MyEmployeeSubject.java.txt");


    ThreeSystem threeSystemGenerated = generated.get(MyEmployee.class);

    ThreeSystemChildSubject.assertThat(threeSystemGenerated)
            .hasParent().hasGenerated().hasSourceText()
            .ignoringWhiteSpace().equalTo(expectedMyEmployeeParent); // sanity full chain

    ThreeSystemChildSubject.assertThat(threeSystemGenerated).hasParentSource(expectedMyEmployeeParent);

    ThreeSystemChildSubject.assertThat(threeSystemGenerated).hasMiddleSource(expectedMyEmployeeMiddle);

    ThreeSystemChildSubject.assertThat(threeSystemGenerated).hasChildSource(expectedMyEmployeeChild);

  }

  /**
   * Chicken, or the egg?
   */
  @Test
  public void boostrapProjectSubjects() {
    TruthGeneratorAPI tg = TruthGeneratorAPI.create();
    SourceClassSets ss = new SourceClassSets(getClass().getPackage().getName());
    ss.generateFromShaded(JavaClassSource.class);
    ss.generateAllFoundInPackagesOf(getClass());
    tg.generate(ss);
  }

  @Test
  public void package_java_mix() {
    TruthGeneratorAPI tg = TruthGeneratorAPI.create();

    String targetPackageName = this.getClass().getPackage().getName();
    SourceClassSets ss = new SourceClassSets(targetPackageName);

    ss.generateAllFoundInPackagesOf(IdCard.class);

    // generate java Subjects and put them in our package
    ss.generateFrom(targetPackageName, UUID.class);
    ss.generateFromShaded(ZoneId.class, ZonedDateTime.class, Chronology.class);

    Map<Class<?>, ThreeSystem> generated = tg.generate(ss);
    assertThat(generated.size()).isAtLeast(ss.getPackageAndClasses().size());
  }

  @Test
  public void test_legacy_mode() {
    TruthGeneratorAPI tg = TruthGeneratorAPI.create();
    SourceClassSets ss = new SourceClassSets(this);
    ss.generateFromNonBean(NonBeanLegacy.class);
    tg.generate(ss);
  }

  /**
   * Given a single class or classes, generate subjects for all references classes in any nested return values
   */
  @Test
  public void recursive_generation() {
    TruthGenerator tg = TruthGeneratorAPI.create();
    Map<Class<?>, ThreeSystem> generate = tg.generate(MyEmployee.class);

    //
    assertThat(generate).containsKey(MyEmployee.class);
    assertThat(generate).containsKey(IdCard.class);
    assertThat(generate).containsKey(MyEmployee.State.class);

    // lost in the generics
    assertThat(generate).doesNotContainKey(Project.class);

    //
    assertThat(generate).containsKey(UUID.class);
    assertThat(generate).containsKey(ZonedDateTime.class);
    assertThat(generate).containsKey(DayOfWeek.class);

    // recursive subjects that shouldn't be included
    assertThat(generate).doesNotContainKey(Spliterator.class);
    assertThat(generate).doesNotContainKey(StreamSubject.class);
  }

  /**
   * Automatically shade subjects that are in packages of other modules (that would cause ao compile error)
   * <p>
   * NB: if this test fails, the project probably won't compile - as an invalid source class will have been produced.
   */
  @Test
  public void auto_shade() {
    String basePackage = getClass().getPackage().getName();

    TruthGenerator tg = TruthGeneratorAPI.create();
    tg.setEntryPoint(Optional.of(basePackage));

    Class<UUID> clazz = UUID.class;
    Map<Class<?>, ThreeSystem> generate = tg.generate(clazz);

    //
    assertThat(generate).containsKey(clazz);

    //
    ThreeSystem threeSystem = generate.get(clazz);
    JavaClassSource parent = threeSystem.getParent().getGenerated();
    assertThat(parent.getPackage()).startsWith(basePackage);
  }

}
