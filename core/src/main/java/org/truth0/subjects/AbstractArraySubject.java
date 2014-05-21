package org.truth0.subjects;

import org.truth0.FailureStrategy;
import org.truth0.util.Platform;

import java.util.List;

/**
 * A common supertype for Array subjects, abstracting some common display and error infrastructure.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
public abstract class AbstractArraySubject<S extends AbstractArraySubject<S, T>, T>
    extends Subject<AbstractArraySubject<S, T>, T> {

  public AbstractArraySubject(FailureStrategy failureStrategy, T subject) {
    super(failureStrategy, subject);
  }

  @Override public S named(String name) { return (S)super.named(name); }

  protected abstract String underlyingType();

  protected abstract List<?> listRepresentation();

  @Override protected String getDisplaySubject() {
    return (internalCustomName() == null)
        ? "<(" + underlyingType() + "[]) " + listRepresentation() + ">"
        : "\"" + this.internalCustomName() + "\"";
  }

  protected void failWithBadType(Object expected) {
    String expectedType = (expected.getClass().isArray())
        ? expected.getClass().getComponentType().getName() + "[]"
        : expected.getClass().getName();
    failWithRawMessage("Incompatible types compared. expected: %s, actual: %s[]",
        Platform.compressType(expectedType), underlyingType());
  }

}