package com.google.common.truth.extension.generator;

import com.google.common.truth.Truth;
import com.google.common.truth.extension.generator.testModel.*;
import org.junit.Test;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.time.LocalDate;

import static com.google.common.truth.extension.generator.testModel.MyEmployeeChildSubject.assertTruth;

/**
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
            .boss(createInstance(MyEmployee.class))
            .build();

    assertTruth(hi).hasBirthYear().isAtLeast(200);
    Truth.assertThat(hi.getBirthYear()).isAtLeast(200);

    Truth.assertThat(hi.getBoss().getName()).contains("Tony");
    assertTruth(hi).hasBoss().hasName().contains("Tony");
//    assertTruth(hi).hasCard().hasEpoch().isAtLeast(20);
    assertTruth(hi).hasProjectList().hasSize(3);
    MyEmployeeSubject myEmployeeSubject = assertTruth(hi);

    MyEmployeeChildSubject.assertThat(TestModelUtils.createEmployee()).hasProjectMapWithKey("key");
  }

  /**
   * @see TruthGeneratorTest#test_legacy_mode
   */
  @Test
  public void test_legacy_mode() {
    NonBeanLegacy nonBeanLegacy = createInstance(NonBeanLegacy.class);
//    NonBeanLegacySubject nonBeanLegacySubject = NonBeanLegacyChildSubject.assertThat(nonBeanLegacy);
//    nonBeanLegacySubject.hasAge().isNotNull();
//    nonBeanLegacySubject.hasName().isEqualTo("lilan");
  }

  private <T> T createInstance(Class<T> clazz) {
    return PODAM_FACTORY.manufacturePojo(clazz);
  }

  @Test
  public void recursive() {
    MyEmployee emp = createInstance(MyEmployee.class);
    MyEmployeeSubject es = ManagedTruth.assertThat(emp);

//    ZonedDateTime anniversary = emp.getAnniversary();
//    anniversary.toLocalDate().
//    anniversary.
  }

  @Test
  public void as_type_chain_transformers() {
    MyEmployee emp = createInstance(MyEmployee.class);
    MyEmployeeSubject es = ManagedTruth.assertThat(emp);

    LocalDate localDate = emp.getAnniversary().toLocalDate();

    es.hasAnniversary().toLocalDate().hasEra();
    es.hasAnniversary().toLocalDateTime().toString();
  }

  @Test
  public void enums(){
    MyEmployee emp = createInstance(MyEmployee.class);
    MyEmployeeSubject es = ManagedTruth.assertThat(emp);

    es.hasAnniversary();
    es.hasEmploymentState();
    Truth.assertThat(true).isFalse();
  }


}
