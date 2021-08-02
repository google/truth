package com.google.common.truth.extension.generator.internal.modelSubjectChickens;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import com.google.common.truth.extension.generator.internal.model.ParentClass;
import com.google.common.truth.extension.generator.shaded.org.jboss.forge.roaster.model.sourceChickens.JavaClassSourceSubject;

import javax.annotation.processing.Generated;

import static com.google.common.truth.extension.generator.shaded.org.jboss.forge.roaster.model.sourceChickens.JavaClassSourceSubject.javaClassSources;

// in VCS as we're still in the chicken phase of what comes first - stable maven plugin to generate this for the build before we can remove

/**
 * Truth Subject for the {@link ParentClass}.
 * <p>
 * Note that this class is generated / managed, and will change over time. So any changes you might make will be
 * overwritten.
 *
 * @see ParentClass
 * @see ParentClassSubject
 * @see ParentClassChildSubject
 */
@Generated("truth-generator")
public class ParentClassParentSubject extends Subject {

  protected final ParentClass actual;

  protected ParentClassParentSubject(FailureMetadata failureMetadata,
                                     ParentClass actual) {
    super(failureMetadata, actual);
    this.actual = actual;
  }

  public JavaClassSourceSubject hasGenerated() {
    isNotNull();
    return check("getGenerated").about(javaClassSources()).that(actual.getGenerated());
  }
}
