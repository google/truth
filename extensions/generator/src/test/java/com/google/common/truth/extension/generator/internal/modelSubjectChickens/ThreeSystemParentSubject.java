package com.google.common.truth.extension.generator.internal.modelSubjectChickens;

import com.google.common.truth.ClassSubject;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import com.google.common.truth.extension.generator.internal.model.ThreeSystem;
import com.google.common.truth.extension.generator.shaded.org.jboss.forge.roaster.model.sourceChickens.JavaClassSourceSubject;

import javax.annotation.processing.Generated;

import static com.google.common.truth.extension.generator.internal.modelSubjectChickens.MiddleClassSubject.middleClasses;
import static com.google.common.truth.extension.generator.internal.modelSubjectChickens.ParentClassSubject.parentClasses;
import static com.google.common.truth.extension.generator.shaded.org.jboss.forge.roaster.model.sourceChickens.JavaClassSourceSubject.javaClassSources;

// in VCS as we're still in the chicken phase of what comes first - stable maven plugin to generate this for the build before we can remove

/**
 * Truth Subject for the {@link ThreeSystem}.
 * <p>
 * Note that this class is generated / managed, and will change over time. So any changes you might make will be
 * overwritten.
 *
 * @see ThreeSystem
 * @see ThreeSystemSubject
 * @see ThreeSystemChildSubject
 */
@Generated("truth-generator")
public class ThreeSystemParentSubject extends Subject {

  protected final ThreeSystem actual;

  protected ThreeSystemParentSubject(FailureMetadata failureMetadata,
                                     com.google.common.truth.extension.generator.internal.model.ThreeSystem actual) {
    super(failureMetadata, actual);
    this.actual = actual;
  }

  public JavaClassSourceSubject hasChild() {
    isNotNull();
    return check("getChild").about(javaClassSources()).that(actual.getChild());
  }

  public ParentClassSubject hasParent() {
    isNotNull();
    return check("getParent").about(parentClasses()).that(actual.getParent());
  }

  public MiddleClassSubject hasMiddle() {
    isNotNull();
    return check("getMiddle").about(middleClasses()).that(actual.getMiddle());
  }

  public ClassSubject hasClassUnderTest() {
    isNotNull();
    return check("getClassUnderTest").that(actual.getClassUnderTest());
  }
}
