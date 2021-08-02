package com.google.common.truth.extension.generator;

import com.google.common.truth.extension.generator.testModel.IdCard;
import com.google.common.truth.extension.generator.testModel.MyEmployee;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.time.ZonedDateTime;

public class TestModelUtils {

  static MyEmployee createEmployee() {
    PodamFactory factory = new PodamFactoryImpl();
    MyEmployee.MyEmployeeBuilder<?, ?> employee = factory.manufacturePojo(MyEmployee.class).toBuilder();
    employee.anniversary(ZonedDateTime.now().withYear(1983));
    MyEmployee boss = factory.manufacturePojo(MyEmployee.class).toBuilder().name("Lilan").build();
    employee = employee
            .boss(boss)
            .card(createCard());
    return employee.build();
  }

  private static IdCard createCard() {
    return new IdCard("special-card-x", 4);
  }

}
