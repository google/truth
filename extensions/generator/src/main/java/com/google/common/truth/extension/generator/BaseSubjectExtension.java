package com.google.common.truth.extension.generator;

import com.google.common.truth.Subject;
import com.google.common.truth.extension.generator.internal.MyStringSubject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Marks for the machines, extensions to base Truth {@link Subject}s - for example, {@link MyStringSubject}.
 */
// todo scan for these instead of registering manually
@Target({ElementType.TYPE})
public @interface BaseSubjectExtension {
}
