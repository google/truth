package com.google.common.truth.extension.generator.shaded.org.jboss.forge.roaster.model.sourceChickens;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.extension.generator.internal.MyStringSubject;
import com.google.common.truth.extension.generator.UserManagedTruth;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import javax.annotation.processing.Generated;

// in VCS as we're still in the chicken phase of what comes first - stable maven plugin to generate this for the build before we can remove

/**
 * Optionally move this class into source control, and add your custom assertions here.
 *
 * <p>
 * If the system detects this class already exists, it won't attempt to generate a new one. Note that if the base
 * skeleton of this class ever changes, you won't automatically get it updated.
 *
 * @see com.google.common.truth.extension.generator.shaded.org.jboss.forge.roaster.model.sourceChickens.JavaClassSourceParentSubject
 */
@UserManagedTruth(clazz = JavaClassSource.class)
@Generated("truth-generator")
public class JavaClassSourceSubject extends JavaClassSourceParentSubject {

  protected JavaClassSourceSubject(FailureMetadata failureMetadata, JavaClassSource actual) {
    super(failureMetadata, actual);
  }

  /**
   * Returns an assertion builder for a {@link JavaClassSource} class.
   */
  public static Factory<JavaClassSourceSubject, JavaClassSource> javaClassSources() {
    return JavaClassSourceSubject::new;
  }

  public MyStringSubject hasSourceText() {
    return check("toString").about(MyStringSubject.myStrings()).that(actual.toString());
  }

}
