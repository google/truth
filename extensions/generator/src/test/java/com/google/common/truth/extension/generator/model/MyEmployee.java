package com.google.common.truth.extension.generator.model;

import java.util.List;

public class MyEmployee {
    private long birthSeconds;
    private boolean employed;
    private double weighting;
    private int birthYear;
    private char id;
    private String name;

    //todo private enum
    //todo optional
    //todo ZonedDateTime

    private MyEmployee boss;

    private IdCard card;

    private List<Project> projectList;

    @Override
    public String toString() {
        return "MyEmployee{" +
                "birthSeconds=" + birthSeconds +
                ", employed=" + employed +
                ", weighting=" + weighting +
                ", birthYear=" + birthYear +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", boss=" + boss +
                ", card=" + card +
                ", slipUpList=" + projectList +
                '}';
    }

    public long getBirthSeconds() {
        return birthSeconds;
    }

    public void setBirthSeconds(final long birthSeconds) {
        this.birthSeconds = birthSeconds;
    }

    public boolean isEmployed() {
        return employed;
    }

    public void setEmployed(final boolean employed) {
        this.employed = employed;
    }

    public double getWeighting() {
        return weighting;
    }

    public void setWeighting(final double weighting) {
        this.weighting = weighting;
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
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(final int birthYear) {
        this.birthYear = birthYear;
    }
}
