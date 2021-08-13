package com.google.common.truth.extension.generator;

import com.google.common.truth.Subject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Marks for the machines, the method used to create the {@link Subject}s.
 * <p>
 * Useful so that we don't need to rely completely on String patterns, and so that Subject extension points can have non
 * colliding names - as the method must be static.
 */
@Target({ElementType.METHOD})
public @interface SubjectFactoryMethod {
}
