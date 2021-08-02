package com.google.common.truth.extension.generator.testModel;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.ZonedDateTime;

@Getter
//@With
@SuperBuilder(toBuilder = true)
//@AllArgsConstructor
//@NoArgsConstructor
@RequiredArgsConstructor
public class Person {
  protected final String name;
  protected final long someLongAspect;
  protected final ZonedDateTime birthday;

  public int getBirthYear(){
    return birthday.getYear();
  }

}
