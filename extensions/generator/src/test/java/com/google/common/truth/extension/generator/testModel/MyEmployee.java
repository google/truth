package com.google.common.truth.extension.generator.testModel;

import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.annotation.Nonnull;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.truth.extension.generator.testModel.MyEmployee.State.EMPLOLYED;

//@AllArgsConstructor
@SuperBuilder(toBuilder = true)
//@Data
@Getter
@Setter(AccessLevel.PRIVATE)
//@With
public class MyEmployee extends Person {

  UUID id = UUID.randomUUID();

//  @With
  ZonedDateTime anniversary = ZonedDateTime.now();

//  @With
  MyEmployee boss = null;

  IdCard card = null;

  List<Project> projectList = new ArrayList<>();

  State employmentState = State.NEVER_EMPLOYED;

  Optional<Double> weighting = Optional.empty();

  public MyEmployee(@Nonnull String name, long someLongAspect, @Nonnull ZonedDateTime birthday) {
    super(name, someLongAspect, birthday);
  }

  public enum State {
    EMPLOLYED, PREVIOUSLY_EMPLOYED, NEVER_EMPLOYED;
  }

  /**
   * Package-private test
   */
  boolean isEmployed(){
    return this.employmentState == EMPLOLYED;
  }

  /**
   * Primitive vs Wrapper test
   */
  Boolean isEmployedWrapped(){
    return this.employmentState == EMPLOLYED;
  }

  /**
   * Overriding support in Subject
   */
  @Override
  public String getName() {
    return super.getName() + " ID: " + this.getId();
  }


  @Override
  public String toString() {
    return "MyEmployee{" +
//            "id=" + id +
            ", name=" + getName() +
            ", card=" + card +
            ", employed=" + employmentState +
            '}';
  }

}
