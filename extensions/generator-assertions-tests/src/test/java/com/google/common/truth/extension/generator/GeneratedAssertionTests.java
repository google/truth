package com.google.common.truth.extension.generator;

import com.google.common.truth.Truth;
import com.google.common.truth.extension.generator.internal.TruthGeneratorTest;
import com.google.common.truth.extension.generator.internal.legacy.NonBeanLegacyChildSubject;
import com.google.common.truth.extension.generator.internal.legacy.NonBeanLegacySubject;
import com.google.common.truth.extension.generator.testModel.ManagedTruth;
import com.google.common.truth.extension.generator.testModel.MyEmployee;
import com.google.common.truth.extension.generator.testModel.MyEmployeeChildSubject;
import com.google.common.truth.extension.generator.testModel.MyEmployeeSubject;
import com.google.common.truth.extension.generator.testing.legacy.NonBeanLegacy;
import org.junit.Test;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import static com.google.common.truth.extension.generator.InstanceUtils.createInstance;
import static com.google.common.truth.extension.generator.testModel.MyEmployee.State.NEVER_EMPLOYED;
import static com.google.common.truth.extension.generator.testModel.MyEmployeeChildSubject.assertTruth;


/**
 * Uses output from packages completed tests run from the generator module.
 *
 * @see TruthGeneratorTest#generate_code
 */
public class GeneratedAssertionTests {

  public static final PodamFactoryImpl PODAM_FACTORY = new PodamFactoryImpl();

  @Test
  public void try_out_assertions() {
    // all asserts should be available
    MyEmployee hi = createInstance(MyEmployee.class);
    hi = hi.toBuilder()
            .name("Zeynep")
            .boss(createInstance(MyEmployee.class).toBuilder().name("Tony").build())
            .build();

    assertTruth(hi).hasBirthYear().isAtLeast(200);
    Truth.assertThat(hi.getBirthYear()).isAtLeast(200);

    Truth.assertThat(hi.getBoss().getName()).contains("Tony");
    assertTruth(hi).hasBoss().hasName().contains("Tony");
//    assertTruth(hi).hasCard().hasEpoch().isAtLeast(20);
    assertTruth(hi).hasProjectList().hasSize(5);
    MyEmployeeSubject myEmployeeSubject = assertTruth(hi);

    MyEmployeeChildSubject.assertThat(TestModelUtils.createEmployee()).hasProjectMapWithKey("key");
  }

  /**
   * @see TruthGeneratorTest#test_legacy_mode
   */
  @Test
  public void test_legacy_mode() {
    NonBeanLegacy nonBeanLegacy = createInstance(NonBeanLegacy.class).toBuilder().name("lilan").build();
    NonBeanLegacySubject nonBeanLegacySubject = NonBeanLegacyChildSubject.assertThat(nonBeanLegacy);
    nonBeanLegacySubject.hasAge().isNotNull();
    nonBeanLegacySubject.hasName().isEqualTo("lilan");
  }

  @Test
  public void recursive() {
    MyEmployee emp = createInstance(MyEmployee.class);
    MyEmployeeSubject es = ManagedTruth.assertThat(emp);

    es.hasAnniversary().hasToLocalDate().hasEra().hasValue().isNotNull();
    es.hasAnniversary().hasToLocalDate().hasChronology().hasId().isNotNull();
  }

  @Test
  public void as_type_chain_transformers() {
    MyEmployee emp = createInstance(MyEmployee.class);
    MyEmployeeSubject es = ManagedTruth.assertThat(emp);

    es.hasAnniversary().hasToLocalDate().hasEra();
    es.hasAnniversary().hasToLocalDateTime().hasToLocalDate().hasEra().isNotNull();
  }

  @Test
  public void enums(){
    MyEmployee emp = createInstance(MyEmployee.class).toBuilder().employmentState(NEVER_EMPLOYED).build();
    MyEmployeeSubject es = ManagedTruth.assertThat(emp);
    es.hasEmploymentState().isEqualTo(NEVER_EMPLOYED);
  }

}
