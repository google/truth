package com.google.common.truth.extension.generator.testModel;

import java.time.ZonedDateTime;

public class Project {
    private String desc;
    private ZonedDateTime start;

    public Project(final String desc, final ZonedDateTime start) {
        this.desc = desc;
        this.start = start;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(final String desc) {
        this.desc = desc;
    }

    public ZonedDateTime getStart() {
        return start;
    }

    public void setStart(final ZonedDateTime start) {
        this.start = start;
    }
}
