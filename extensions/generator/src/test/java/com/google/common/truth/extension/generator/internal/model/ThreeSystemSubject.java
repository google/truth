package com.google.common.truth.extension.generator.internal.model;

import com.google.common.truth.FailureMetadata;

import javax.annotation.processing.Generated;

/**
 * Optionally move this class into source control, and add your custom assertions here.
 *
 * <p>
 * If the system detects this class already exists, it won't attempt to generate a new one. Note that if the base
 * skeleton of this class ever changes, you won't automatically get it updated.
 *
 * @see ThreeSystemParentSubject
 */
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
