package com.google.common.truth.extension.generator.internal.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jboss.forge.roaster.model.source.JavaClassSource;

@Getter
@RequiredArgsConstructor
public class AClass {
   public final JavaClassSource generated;
}
