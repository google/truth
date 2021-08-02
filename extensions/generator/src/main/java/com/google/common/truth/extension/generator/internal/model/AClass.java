package com.google.common.truth.extension.generator.internal.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import org.jboss.forge.roaster.model.source.JavaClassSource;

@Getter
@RequiredArgsConstructor
@SuperBuilder
public class AClass {
   protected final JavaClassSource generated;
}
