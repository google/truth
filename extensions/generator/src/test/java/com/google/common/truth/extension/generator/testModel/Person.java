package com.google.common.truth.extension.generator.testModel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.ZonedDateTime;

@Getter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
public class Person {
  protected final String name;
  protected final long someLongAspect;
  protected final ZonedDateTime birthday;

  public int getBirthYear(){
    return birthday.getYear();
  }

}
