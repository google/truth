package com.google.common.truth.extension.generator.testModel;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MyEmployee extends Person {
  private UUID id;

  private ZonedDateTime anniversary;

  private MyEmployee boss;

  private IdCard card;

  private List<Project> projectList;

  State employed;

  enum State {
    EMPLOLYED, PREVIOUSLY_EMPLOYED, NEVER_EMPLOYED;
  }

  /**
   * Overriding support in Subject
   */
  @Override
  public String getName() {
    return super.getName() + " ID: " +this.getId();
  }

  public char getId() {
    return id;
  }

  public void setId(final char id) {
    this.id = id;
  }

  public MyEmployee getBoss() {
    return boss;
  }

  public void setBoss(final MyEmployee boss) {
    this.boss = boss;
  }

  public IdCard getCard() {
    return card;
  }

  public void setCard(final IdCard card) {
    this.card = card;
  }

  public List<Project> getSlipUpList() {
    return projectList;
  }

  public void setSlipUpList(final List<Project> slipUpList) {
    this.projectList = slipUpList;
  }

  public MyEmployee(final String name) {
    super(name);
  }

  public List<Project> getProjectList() {
    return this.projectList;
  }

  public void setProjectList(final List<Project> projectList) {
    this.projectList = projectList;
  }

  public void setWeighting(final Optional<Double> weighting) {
    this.weighting = weighting;
  }

  private Optional<Double> weighting;

  public ZonedDateTime getAnniversary() {
    return this.anniversary;
  }

  public void setAnniversary(final ZonedDateTime anniversary) {
    this.anniversary = anniversary;
  }


  public State getEmployed() {
    return this.employed;
  }

  public void setEmployed(final State employed) {
    this.employed = employed;
  }

  public Optional<Double> getWeighting() {
    return this.weighting;
  }

}
