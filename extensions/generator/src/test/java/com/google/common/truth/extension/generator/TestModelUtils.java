package com.google.common.truth.extension.generator;

import com.google.common.truth.extension.generator.testModel.IdCard;
import com.google.common.truth.extension.generator.testModel.MyEmployee;

import java.time.ZonedDateTime;

public class TestModelUtils {

  static MyEmployee createEmployee() {
    MyEmployee employee = new MyEmployee("Zeynep");
    employee.setBirthday(ZonedDateTime.now().withYear(1983));
    employee.setBoss(new MyEmployee("Lilan"));
    employee.setCard(createCard());
    return employee;
  }

  private static IdCard createCard() {
    return new IdCard("special-card-x", 4);
  }

}
