package org.truth0.subjects;

import org.truth0.FailureStrategy;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A common supertype for Array subjects, abstracting some common display and error infrastructure.
 * 
 * @author Christian Gruber (cgruber@israfil.net)
 */
abstract class AbstractArraySubject<T> extends Subject<AbstractArraySubject<T>, T> {

  private static final Pattern TYPE_PATTERN = Pattern.compile("(?:[\\w$]+\\.)*([\\w\\.*$]+)");

  private static String typeOnly(String type) {
    type = stripIfPrefixed(type, "class ");
    type = stripIfPrefixed(type, "interface ");
    return type;
  }

  private static String stripIfPrefixed(String string, String prefix) {
    return (string.startsWith(prefix)) ? string.substring(prefix.length()) : string;
  }

  private static String stripIfInPackage(String type, String packagePrefix) {
    if (type.startsWith(packagePrefix)
        && (type.indexOf('.', packagePrefix.length()) == -1)
        && Character.isUpperCase(type.charAt(packagePrefix.length()))) {
      return type.substring(packagePrefix.length());
    }
    return type;
  }

  public AbstractArraySubject(FailureStrategy failureStrategy, T subject) {
    super(failureStrategy, subject);
  }

  protected abstract String underlyingType();

  /**
   * Inspired by JavaWriter.
   */
  static String compressType(String type) {
    type = typeOnly(type);
    StringBuilder sb = new StringBuilder();
    Matcher m = TYPE_PATTERN.matcher(type);
    int pos = 0;

    while (true) {
      boolean found = m.find(pos);
      // Copy non-matching characters like "<".
      int typeStart = found ? m.start() : type.length();
      sb.append(type, pos, typeStart);
      if (!found) {
        break;
      }
      // Copy a single class name, shortening it if possible.
      String name = m.group(0);
      name = stripIfInPackage(name, "java.lang.");
      name = stripIfInPackage(name, "java.util.");
      sb.append(name);

      pos = m.end();
    }
    return sb.toString();
  }

  protected abstract List<?> listRepresentation();

  @Override protected String getDisplaySubject() {
    return (internalCustomLabel() == null)
        ? "<(" + underlyingType() + "[]) " + listRepresentation() + ">"
        : "\"" + this.internalCustomLabel() + "\"";
  }

  protected void failWithBadType(Object expected) {
    String expectedType = (expected.getClass().isArray())
        ? expected.getClass().getComponentType().getName() + "[]"
        : expected.getClass().getName();
    failWithRawMessage("Incompatible types compared. expected: %s, actual: %s[]",
        compressType(expectedType), underlyingType());
  }

}