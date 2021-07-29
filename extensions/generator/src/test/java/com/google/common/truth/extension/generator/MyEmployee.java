package com.google.common.truth.extension.generator;

public class MyEmployee {
    private long birthYear;
    private String name;

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
        this.birthYear = birthYear;
    }
}
