package com.google.common.truth.extension.generator;

import com.google.common.io.Resources;
import com.google.common.truth.extension.Employee;
import com.google.common.truth.extension.generator.internal.TruthGenerator;
import com.google.common.truth.extension.generator.internal.model.ThreeSystem;
import com.google.common.truth.extension.generator.model.IdCard;
import com.google.common.truth.extension.generator.model.MyEmployee;
import com.google.common.truth.extension.generator.model.Project;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;

@RunWith(JUnit4.class)
public class TruthGeneratorTest {

  @Test
  public void poc() throws IOException {
    TruthGeneratorAPI truthGeneratorAPI = new TruthGenerator(Employee.class.getPackageName());
    String generated = truthGeneratorAPI.generate(Employee.class);

    String expectedFileName = "expected-EmployeeSubject.java.txt";
    String expecting = Resources.toString(Resources.getResource(expectedFileName), Charset.defaultCharset());

    assertThat(trim(generated)).isEqualTo(trim(expecting));
  }

  private String trim(String in) {
    return in.replaceAll("(?m)^(\\s)*|(\\s)*$", "");
  }

  @Test
  public void employee() throws FileNotFoundException {
    TruthGeneratorAPI truthGeneratorAPI = new TruthGenerator(MyEmployee.class.getPackageName());
    String generate = truthGeneratorAPI.generate(MyEmployee.class);

    assertThat(trim("")).isEqualTo(trim(generate));
  }

  @Test
  void generate_code() throws FileNotFoundException {
    // todo need to be able to set base package for all generated classes, kind of like shade, so you cah generate test for classes in other restricted modules
    TruthGeneratorAPI truthGenerator = new TruthGenerator(MyEmployee.class.getPackageName());

    List<Class> classes = new ArrayList<>();
    classes.add(MyEmployee.class);
    classes.add(IdCard.class);
    classes.add(Project.class);

    // package exists in other module error - needs package target support
//        classes.add(ZonedDateTime.class);
//        classes.add(UUID.class);

    List<ThreeSystem> threeSystemStream = classes.stream().map(x -> truthGenerator.threeLayerSystem(x).get()).collect(Collectors.toList());

    TestGenerator tg = new TestGenerator(Set.of());
    threeSystemStream.forEach(x -> {
      tg.addTests(x.parent.generated, x.classUnderTest);
    });
    truthGenerator.createOverallAccessPoints();


//        Assertions.assertThat(wc).

//        truthGenerator.generate(PartitionState.class);

//        truthGenerator.maintain(LongPollingMockConsumer.class, LongPollingMockConsumerSubject.class);

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
