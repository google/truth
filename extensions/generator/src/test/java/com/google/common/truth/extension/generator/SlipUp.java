package com.google.common.truth.extension.generator;

import java.time.ZonedDateTime;

public class SlipUp {
    private String desc;
    private ZonedDateTime occurance;

    public SlipUp(final String desc, final ZonedDateTime occurance) {
        this.desc = desc;
        this.occurance = occurance;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(final String desc) {
        this.desc = desc;
    }

    public ZonedDateTime getOccurance() {
        return occurance;
    }

    public void setOccurance(final ZonedDateTime occurance) {
        this.occurance = occurance;
    }
}
