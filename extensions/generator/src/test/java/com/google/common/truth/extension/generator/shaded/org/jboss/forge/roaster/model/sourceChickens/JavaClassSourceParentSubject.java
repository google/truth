package com.google.common.truth.extension.generator.shaded.org.jboss.forge.roaster.model.sourceChickens;

import com.google.common.truth.*;
//import com.google.common.truth.extension.generator.shaded.org.jboss.forge.roaster.model.source.JavaClassSourceChildSubject;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import javax.annotation.processing.Generated;

import static com.google.common.truth.Fact.simpleFact;

// in VCS as we're still in the chicken phase of what comes first - stable maven plugin to generate this for the build before we can remove

/**
 * Truth Subject for the {@link JavaClassSource}.
 * <p>
 * Note that this class is generated / managed, and will change over time. So any changes you might make will be
 * overwritten.
 *
 * @see JavaClassSource
 * @see JavaClassSourceSubject
 * @see JavaClassSourceChildSubject
 */
@Generated("truth-generator")
public class JavaClassSourceParentSubject extends Subject {

  protected final JavaClassSource actual;

  protected JavaClassSourceParentSubject(FailureMetadata failureMetadata,
                                         JavaClassSource actual) {
    super(failureMetadata, actual);
    this.actual = actual;
  }

  public StringSubject hasCanonicalName() {
    isNotNull();
    return check("getCanonicalName").that(actual.getCanonicalName());
  }

  public void isNotClass() {
    if (actual.isClass()) {
      failWithActual(simpleFact("expected NOT to be Class"));
    }
  }

  public void isClass() {
    if (!actual.isClass()) {
      failWithActual(simpleFact("expected to be Class"));
    }
  }

  public BooleanSubject hasClass() {
    isNotNull();
    return check("isClass").that(actual.isClass());
  }

  public void isNotProtected() {
    if (actual.isProtected()) {
      failWithActual(simpleFact("expected NOT to be Protected"));
    }
  }

  public void isProtected() {
    if (!actual.isProtected()) {
      failWithActual(simpleFact("expected to be Protected"));
    }
  }

  public BooleanSubject hasProtected() {
    isNotNull();
    return check("isProtected").that(actual.isProtected());
  }

  public void isNotPublic() {
    if (actual.isPublic()) {
      failWithActual(simpleFact("expected NOT to be Public"));
    }
  }

  public void isPublic() {
    if (!actual.isPublic()) {
      failWithActual(simpleFact("expected to be Public"));
    }
  }

  public BooleanSubject hasPublic() {
    isNotNull();
    return check("isPublic").that(actual.isPublic());
  }

  public StringSubject hasQualifiedName() {
    isNotNull();
    return check("getQualifiedName").that(actual.getQualifiedName());
  }

  public IterableSubject hasAnnotations() {
    isNotNull();
    return check("getAnnotations").that(actual.getAnnotations());
  }

  public IntegerSubject hasEndPosition() {
    isNotNull();
    return check("getEndPosition").that(actual.getEndPosition());
  }

  public void isNotPrivate() {
    if (actual.isPrivate()) {
      failWithActual(simpleFact("expected NOT to be Private"));
    }
  }

  public void isPrivate() {
    if (!actual.isPrivate()) {
      failWithActual(simpleFact("expected to be Private"));
    }
  }

  public BooleanSubject hasPrivate() {
    isNotNull();
    return check("isPrivate").that(actual.isPrivate());
  }

  public IterableSubject hasSyntaxErrors() {
    isNotNull();
    return check("getSyntaxErrors").that(actual.getSyntaxErrors());
  }

  public void isNotStatic() {
    if (actual.isStatic()) {
      failWithActual(simpleFact("expected NOT to be Static"));
    }
  }

  public void isStatic() {
    if (!actual.isStatic()) {
      failWithActual(simpleFact("expected to be Static"));
    }
  }

  public BooleanSubject hasStatic() {
    isNotNull();
    return check("isStatic").that(actual.isStatic());
  }

  public void isNotInterface() {
    if (actual.isInterface()) {
      failWithActual(simpleFact("expected NOT to be Interface"));
    }
  }

  public void isInterface() {
    if (!actual.isInterface()) {
      failWithActual(simpleFact("expected to be Interface"));
    }
  }

  public BooleanSubject hasInterface() {
    isNotNull();
    return check("isInterface").that(actual.isInterface());
  }

  public IterableSubject hasProperties() {
    isNotNull();
    return check("getProperties").that(actual.getProperties());
  }

  public void isNotFinal() {
    if (actual.isFinal()) {
      failWithActual(simpleFact("expected NOT to be Final"));
    }
  }

  public void isFinal() {
    if (!actual.isFinal()) {
      failWithActual(simpleFact("expected to be Final"));
    }
  }

  public BooleanSubject hasFinal() {
    isNotNull();
    return check("isFinal").that(actual.isFinal());
  }

  public IntegerSubject hasLineNumber() {
    isNotNull();
    return check("getLineNumber").that(actual.getLineNumber());
  }

  public void isNotLocalClass() {
    if (actual.isLocalClass()) {
      failWithActual(simpleFact("expected NOT to be LocalClass"));
    }
  }

  public void isLocalClass() {
    if (!actual.isLocalClass()) {
      failWithActual(simpleFact("expected to be LocalClass"));
    }
  }

  public BooleanSubject hasLocalClass() {
    isNotNull();
    return check("isLocalClass").that(actual.isLocalClass());
  }

  public IntegerSubject hasStartPosition() {
    isNotNull();
    return check("getStartPosition").that(actual.getStartPosition());
  }

  public IterableSubject hasMethods() {
    isNotNull();
    return check("getMethods").that(actual.getMethods());
  }

  public StringSubject hasPackage() {
    isNotNull();
    return check("getPackage").that(actual.getPackage());
  }

  public StringSubject hasSuperType() {
    isNotNull();
    return check("getSuperType").that(actual.getSuperType());
  }

  public IterableSubject hasImports() {
    isNotNull();
    return check("getImports").that(actual.getImports());
  }

  public void isNotEnum() {
    if (actual.isEnum()) {
      failWithActual(simpleFact("expected NOT to be Enum"));
    }
  }

  public void isEnum() {
    if (!actual.isEnum()) {
      failWithActual(simpleFact("expected to be Enum"));
    }
  }

  public BooleanSubject hasEnum() {
    isNotNull();
    return check("isEnum").that(actual.isEnum());
  }

  public IterableSubject hasTypeVariables() {
    isNotNull();
    return check("getTypeVariables").that(actual.getTypeVariables());
  }

  public void isNotRecord() {
    if (actual.isRecord()) {
      failWithActual(simpleFact("expected NOT to be Record"));
    }
  }

  public void isRecord() {
    if (!actual.isRecord()) {
      failWithActual(simpleFact("expected to be Record"));
    }
  }

  public BooleanSubject hasRecord() {
    isNotNull();
    return check("isRecord").that(actual.isRecord());
  }

  public IterableSubject hasFields() {
    isNotNull();
    return check("getFields").that(actual.getFields());
  }

  public IterableSubject hasMembers() {
    isNotNull();
    return check("getMembers").that(actual.getMembers());
  }

  public IntegerSubject hasColumnNumber() {
    isNotNull();
    return check("getColumnNumber").that(actual.getColumnNumber());
  }

  public IterableSubject hasNestedTypes() {
    isNotNull();
    return check("getNestedTypes").that(actual.getNestedTypes());
  }

  public StringSubject hasName() {
    isNotNull();
    return check("getName").that(actual.getName());
  }

  public ComparableSubject hasVisibility() {
    isNotNull();
    return check("getVisibility").that(actual.getVisibility());
  }

  public void isNotDefaultPackage() {
    if (actual.isDefaultPackage()) {
      failWithActual(simpleFact("expected NOT to be DefaultPackage"));
    }
  }

  public void isDefaultPackage() {
    if (!actual.isDefaultPackage()) {
      failWithActual(simpleFact("expected to be DefaultPackage"));
    }
  }

  public BooleanSubject hasDefaultPackage() {
    isNotNull();
    return check("isDefaultPackage").that(actual.isDefaultPackage());
  }

  public void isNotAbstract() {
    if (actual.isAbstract()) {
      failWithActual(simpleFact("expected NOT to be Abstract"));
    }
  }

  public void isAbstract() {
    if (!actual.isAbstract()) {
      failWithActual(simpleFact("expected to be Abstract"));
    }
  }

  public BooleanSubject hasAbstract() {
    isNotNull();
    return check("isAbstract").that(actual.isAbstract());
  }

  public IterableSubject hasInterfaces() {
    isNotNull();
    return check("getInterfaces").that(actual.getInterfaces());
  }

  public void isNotAnnotation() {
    if (actual.isAnnotation()) {
      failWithActual(simpleFact("expected NOT to be Annotation"));
    }
  }

  public void isAnnotation() {
    if (!actual.isAnnotation()) {
      failWithActual(simpleFact("expected to be Annotation"));
    }
  }

  public BooleanSubject hasAnnotation() {
    isNotNull();
    return check("isAnnotation").that(actual.isAnnotation());
  }

  public void isNotPackagePrivate() {
    if (actual.isPackagePrivate()) {
      failWithActual(simpleFact("expected NOT to be PackagePrivate"));
    }
  }

  public void isPackagePrivate() {
    if (!actual.isPackagePrivate()) {
      failWithActual(simpleFact("expected to be PackagePrivate"));
    }
  }

  public BooleanSubject hasPackagePrivate() {
    isNotNull();
    return check("isPackagePrivate").that(actual.isPackagePrivate());
  }
}
