package com.google.common.truth.extension.generator.internal.modelSubjectChickens;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.extension.generator.internal.model.ThreeSystem;
import com.google.common.truth.extension.generator.UserManagedTruth;

import javax.annotation.processing.Generated;

// in VCS as we're still in the chicken phase of what comes first - stable maven plugin to generate this for the build before we can remove

/**
 * Optionally move this class into source control, and add your custom assertions here.
 *
 * <p>
 * If the system detects this class already exists, it won't attempt to generate a new one. Note that if the base
 * skeleton of this class ever changes, you won't automatically get it updated.
 *
 * @see ThreeSystemParentSubject
 */
@UserManagedTruth(clazz = ThreeSystem.class)
@Generated("truth-generator")
public class ThreeSystemSubject extends ThreeSystemParentSubject {

  protected ThreeSystemSubject(FailureMetadata failureMetadata,
                               com.google.common.truth.extension.generator.internal.model.ThreeSystem actual) {
    super(failureMetadata, actual);
  }

  /**
   * Returns an assertion builder for a {@link ThreeSystem} class.
   */
  public static Factory<ThreeSystemSubject, ThreeSystem> threeSystems() {
    return ThreeSystemSubject::new;
  }

  public void hasParentSource(String expected) {
    hasParent().hasGenerated().hasSourceText().ignoringWhiteSpace().equalTo(expected);
  }

  public void hasMiddleSource(String expected) {
    hasMiddle().hasGenerated().hasSourceText().ignoringWhiteSpace().equalTo(expected);
  }

  public void hasChildSource(String expected) {
    hasChild().hasSourceText().ignoringWhiteSpace().equalTo(expected);
  }
}
