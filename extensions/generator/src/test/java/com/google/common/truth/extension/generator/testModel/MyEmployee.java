package com.google.common.truth.extension.generator.testModel;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MyEmployee extends Person {
  private UUID id = UUID.randomUUID();

  private ZonedDateTime anniversary = ZonedDateTime.now();

  private MyEmployee boss;

  private IdCard card;

  private List<Project> projectList = new ArrayList<>();

  State employed = State.NEVER_EMPLOYED;

  public MyEmployee(final String name) {
    super(name);
  }


  enum State {
    EMPLOLYED, PREVIOUSLY_EMPLOYED, NEVER_EMPLOYED;
  }

  /**
   * Overriding support in Subject
   */
  @Override
  public String getName() {
    return super.getName() + " ID: " + this.getId();
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

  public UUID getId() {
    return id;
  }

  public void setId(final UUID id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return "MyEmployee{" +
//            "id=" + id +
            ", name=" + getName() +
            ", card=" + card +
            ", employed=" + employed +
            '}';
  }

}
