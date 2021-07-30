package com.google.common.truth.extension.generator;

import java.util.List;

public class MyEmployee {
    private long birthSeconds;
    private boolean employed;
    private double weighting;
    private int birthYear;
    private char id;
    private String name;

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

    public void setBirthYear(final int birthYear) {
        this.birthYear = birthYear;
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

    public List<SlipUp> getSlipUpList() {
        return slipUpList;
    }

    public void setSlipUpList(final List<SlipUp> slipUpList) {
        this.slipUpList = slipUpList;
    }

    private MyEmployee boss;

    private IdCard card;

    private List<SlipUp> slipUpList;

    public MyEmployee(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public long getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(final long birthYear) {
        this.birthYear = (int) birthYear;
    }
}
