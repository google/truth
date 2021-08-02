package com.google.common.truth.extension.generator;

import com.google.common.io.Resources;
import com.google.common.truth.extension.Employee;
import com.google.common.truth.extension.generator.internal.model.SourceClassSets;
import com.google.common.truth.extension.generator.internal.model.ThreeSystem;
import com.google.common.truth.extension.generator.testModel.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;

@RunWith(JUnit4.class)
public class TruthGeneratorTest {

  @Test
  public void poc() throws IOException {
    TruthGeneratorAPI truthGeneratorAPI = TruthGeneratorAPI.create();
    String generated = truthGeneratorAPI.combinedSystem(Employee.class);

    String expectedFileName = "expected-EmployeeSubject.java.txt";
    String expecting = Resources.toString(Resources.getResource(expectedFileName), Charset.defaultCharset());

    assertThat(trim(generated)).isEqualTo(trim(expecting));
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
  public void generate_code() {
    // todo need to be able to set base package for all generated classes, kind of like shade, so you cah generate test for classes in other restricted modules
    TruthGeneratorAPI truthGenerator = TruthGeneratorAPI.create();

    Set<Class<?>> classes = new HashSet<>();
    classes.add(MyEmployee.class);
    classes.add(IdCard.class);
    classes.add(Project.class);

    String basePackageName = getClass().getPackage().getName();
    SourceClassSets ss = new SourceClassSets(basePackageName);
    ss.generateFrom(classes);

    // package exists in other module error - needs package target support
    ss.generateFrom(basePackageName, ZonedDateTime.class, UUID.class);

    Set<ThreeSystem> generated = truthGenerator.generate(ss);

    assertThat(generated.size()).isAtLeast(classes.size());
    Set<? extends Class<?>> generatedSourceClasses = generated.stream().map(x -> x.classUnderTest).collect(Collectors.toSet());
    assertThat(generatedSourceClasses).containsAtLeast(UUID.class, ZonedDateTime.class);

    MyEmployee employee = TestModelUtils.createEmployee();
    ManagedTruth.assertThat(employee).getCard().getEpoch().isAtLeast(0);
//    ManagedTruth.assertThat(employee).getBoss().getName().isEqualTo("Tony");

//    ManagedTruth.assertThat(employee).getBirthday().getYear().isLessThan(1920);
  }

  @Test
  public void package_java_mix() {
    TruthGeneratorAPI tg = TruthGeneratorAPI.create();

    String targetPackageName = this.getClass().getPackage().getName();
    SourceClassSets ss = new SourceClassSets(targetPackageName);

    ss.generateAllFoundInPackagesOf(IdCard.class);

    // generate java Subjects and put them in our package
    ss.generateFrom(targetPackageName, UUID.class, ZonedDateTime.class);

    Set<ThreeSystem> generated = tg.generate(ss);
    assertThat(generated.size()).isAtLeast(ss.getPackageAndClasses().size());

  }

  @Test
  public void try_out_assertions() {

    // all asserts should be available
//        ManagedTruth.assertThat(new CommitHistory(List.of()));
//        ManagedTruth.assertTruth(Range.range(4));
//
//        WorkContainer wc = new WorkContainer(4, null, "");
//
//        assertTruth(wc).getEpoch().isAtLeast(4);
//        Truth.assertThat(wc.getEpoch()).isAtLeast(4);
//
//        MyEmployee hi = new MyEmployee("Zeynep");
//        hi.setBoss(new MyEmployee("Lilan"));
//
//        assertTruth(hi).getBirthYear().isAtLeast(200);
//        assertThat(hi.getBirthYear()).isAtLeast(200);
//
//        assertThat(hi.getBoss().getName()).contains("Tony");
//        assertTruth(hi).getBoss().getName().contains("Tony");
//        assertTruth(hi).getCard().getEpoch().isAtLeast(20);
//        assertTruth(hi).getSlipUpList().hasSize(3);
//        MyEmployeeSubject myEmployeeSubject = ManagedTruth.assertTruth(hi);
  }

}
