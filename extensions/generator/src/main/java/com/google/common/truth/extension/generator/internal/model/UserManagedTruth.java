package com.google.common.truth.extension.generator.internal.model;

import com.google.common.truth.Subject;
import com.google.common.truth.extension.generator.internal.SkeletonGeneratorAPI;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Maker for the {@link SkeletonGeneratorAPI#threeLayerSystem)} which instructs the system that this class is the user
 * managed middle class.
 * <p>
 * <p>
 * Useful for detecting with it already exists, instead of relaying on class name matching. And good for discovering the
 * class under test.
 */
@Target({ElementType.TYPE})
public @interface UserManagedTruth {
  /**
   * The class that this is a {@link Subject} for.
   */
  Class<?> clazz();
}
