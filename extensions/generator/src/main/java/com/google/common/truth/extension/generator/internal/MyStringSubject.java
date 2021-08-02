package com.google.common.truth.extension.generator.internal;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.StringSubject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jboss.forge.roaster.model.source.JavaClassSource;

public class MyStringSubject extends StringSubject {

  String actual;

  protected MyStringSubject(FailureMetadata failureMetadata, String actual) {
    super(failureMetadata, actual);
    this.actual = actual;
  }

  /**
   * Returns an assertion builder for a {@link JavaClassSource} class.
   */
  public static Factory<MyStringSubject, String> myStrings() {
    return MyStringSubject::new;
  }

  public IgnoringWhiteSpaceComparison ignoringWhiteSpace() {
    return new IgnoringWhiteSpaceComparison();
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public class IgnoringWhiteSpaceComparison {

    public void equalTo(String expected) {
      String expectedNormal = normalise(expected);
      String actualNormal = normalise(actual);

      check("").that(actualNormal).isEqualTo(expectedNormal);
    }

    private String normalise(String raw) {
      String normal = normaliseEndingsEndings(raw);
      normal = normaliseWhiteSpaceAtEndings(normal);
      return normal;
    }

    /**
     * lazy remove trailing whitespace on lines
     */
    private String normaliseWhiteSpaceAtEndings(String raw) {
      return raw.replaceAll("(?m)\\s+$", "");
    }

    /**
     * make line endings consistent
     */
    private String normaliseEndingsEndings(String raw) {
      return raw.replaceAll("\\r\\n?", "\n");
    }
  }

}
