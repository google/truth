package com.google.common.truth.extension.generator.testModel;

import java.time.ZonedDateTime;

public class Person {
  private long birthSeconds;
  private String name;
  private int birthYear;
  private ZonedDateTime brithday;

  public long getBirthSeconds() {
    return this.birthSeconds;
  }

  public void setBirthSeconds(final long birthSeconds) {
    this.birthSeconds = birthSeconds;
  }

  public String getName() {
    return this.name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public int getBirthYear() {
    return this.birthYear;
  }

  public void setBirthYear(final int birthYear) {
    this.birthYear = birthYear;
  }

  public Person(final String name) {
    this.name = name;
  }

}
