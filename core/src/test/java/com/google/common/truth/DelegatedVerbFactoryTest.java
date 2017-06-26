/*
 * Copyright (c) 2016 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.truth;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.Truth.assertThat;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for DelegatedVerbFactory and related classes. */
@RunWith(JUnit4.class)
public class DelegatedVerbFactoryTest {

  @Rule public final Expect expect = Expect.create();
  @Rule public final ExpectFailure expectFailure = new ExpectFailure();

  /** Simple object for testing. */
  @AutoValue
  abstract static class Foo<T> {
    abstract String id();

    abstract T item();

    static <T> Foo<T> create(String id, T item) {
      return new AutoValue_DelegatedVerbFactoryTest_Foo<T>(id, item);
    }
  }

  /** Subject for testing. */
  private static class FooSubject<T> extends Subject<FooSubject<T>, Foo<T>> {
    public FooSubject(FailureStrategy failureStrategy, Foo<T> actual) {
      super(failureStrategy, actual);
    }

    public void hasIdFragment(String id) {
      check().that(actual().id()).named("id of %s", actual()).contains(id);
    }

    public void hasItem(T item) {
      check().that(actual().item()).named("item of %s", actual()).isEqualTo(item);
    }
  }

  /** Custom verb that captures the held type for testing. */
  private static class FooVerb extends AbstractDelegatedVerb {
    private static class Factory implements DelegatedVerbFactory {
      @Override
      public FooVerb createVerb(FailureStrategy failureStrategy) {
        return new FooVerb(failureStrategy, this);
      }
    }

    private final FailureStrategy failureStrategy;

    FooVerb(FailureStrategy failureStrategy, DelegatedVerbFactory<FooVerb> factory) {
      this.failureStrategy = checkNotNull(failureStrategy);
    }

    public <T> FooSubject<T> that(@Nullable Foo<T> foo) {
      return new FooSubject<T>(failureStrategy, foo);
    }
  }

  /** Accessor for getting the FooVerb.Factory. */
  private static DelegatedVerbFactory<FooVerb> foos() {
    return new FooVerb.Factory();
  }

  @Test
  public void testAssertAbout() {
    Foo<Long> foo = Foo.create("abcdef", 42L);

    assertAbout(foos()).that(foo).hasIdFragment("bcd");
    assertAbout(foos()).that(foo).hasItem(42L);
    // assertAbout(foos()).that(foo).hasItem("bar"); // Doesn't compile!
  }

  @Test
  public void testAssertWithMessage_aboutOneArg() {
    Foo<Long> foo = Foo.create("abcdef", 42L);

    expectFailure
        .whenTesting()
        .withMessage("Fancy prepended message")
        .about(foos())
        .that(foo)
        .hasIdFragment("xyz");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Fancy prepended message: Not true that "
                + "id of Foo{id=abcdef, item=42} (<\"abcdef\">) contains <\"xyz\">");
  }

  @Test
  public void testAssertWithMessage_aboutFormatting() {
    Foo<Long> foo = Foo.create("abcdef", 42L);

    expectFailure
        .whenTesting()
        .withMessage("Fancy %s %s", "prepended", "message")
        .about(foos())
        .that(foo)
        .hasItem(24L);
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Fancy prepended message: Not true that "
                + "item of Foo{id=abcdef, item=42} (<42>) is equal to <24>");
  }

  @Test
  public void testExpectAbout() {
    Foo<String> foo = Foo.create("abcdef", "xyz");

    expect.about(foos()).that(foo).hasIdFragment("cde");
    expect.about(foos()).that(foo).hasItem("xyz");
  }
}
