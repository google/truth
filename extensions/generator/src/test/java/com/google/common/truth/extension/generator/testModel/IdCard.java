package com.google.common.truth.extension.generator.testModel;

import java.util.UUID;

public class IdCard {
    private UUID id = UUID.randomUUID();
    private String name;
    private int epoch;

    @Override
    public String toString() {
        return "IdCard{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", epoch=" + epoch +
                '}';
    }

    public IdCard(final String name, final int epoch) {
        this.id = id;
        this.name = name;
        this.epoch = epoch;
    }

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getEpoch() {
        return epoch;
    }

    public void setEpoch(final int epoch) {
        this.epoch = epoch;
    }
}
