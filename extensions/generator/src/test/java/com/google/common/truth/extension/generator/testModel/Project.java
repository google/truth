package com.google.common.truth.extension.generator.testModel;

import lombok.Value;

import java.time.ZonedDateTime;

@Value
public class Project {
    String desc;
    ZonedDateTime start;
}
