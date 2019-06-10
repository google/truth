/*
 * Copyright (c) 2019 Google, Inc.
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

package com.google.common.truth.refactorings;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.errorprone.BugPattern.ProvidesFix.REQUIRES_HUMAN_ATTENTION;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.matchers.Description.NO_MATCH;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static com.google.errorprone.util.ASTHelpers.getReceiver;
import static com.google.errorprone.util.ASTHelpers.getSymbol;
import static com.google.errorprone.util.ASTHelpers.isSubtype;
import static com.sun.source.tree.Tree.Kind.MEMBER_SELECT;
import static com.sun.source.tree.Tree.Kind.METHOD_INVOCATION;
import static java.lang.String.format;
import static java.util.stream.Stream.concat;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import javax.annotation.Nullable;

/**
 * Migrates assertions from {@code assertThat(...).named(...)} to {@code
 * assertWithMessage(...).that(...)} (sometimes with an {@code about} call in there, and sometimes
 * with {@code withMessage} if using a {@code FailureStrategy} other than assert).
 *
 * <p>Alternatively, if setting up the infrastructure to run this migration tool is too costly,
 * consider running a Perl-compatible regex search-and-replace like: {@code
 * s/assertThat[(](.*)[)]\s*[.]named[(]((?:[^"\n)]|"(?:[^\\\n]|\\.)*")*)[)]/assertWithMessage($2).that($1)/g}.
 * Such a search will not handle as many cases as this tool, and it is more likely to produce code
 * that does not compile (or that no longer uses custom {@code Subject} classes), but it will handle
 * many simple cases.
 */
@BugPattern(
    name = "NamedToWithMessage",
    summary = "Use assertWithMessage(...)/withMessage(...) instead of the deprecated named(...).",
    severity = SUGGESTION,
    providesFix = REQUIRES_HUMAN_ATTENTION)
public final class NamedToWithMessage extends BugChecker implements MethodInvocationTreeMatcher {
  @Override
  public Description matchMethodInvocation(MethodInvocationTree namedCall, VisitorState state) {
    if (!NAMED_METHOD.matches(namedCall, state)) {
      return NO_MATCH;
    }
    MethodInvocationTree thatCall = findThatCall(namedCall, state);
    if (thatCall == null) {
      return NO_MATCH;
    }
    ExpressionTree namedReceiver = getReceiver(namedCall);
    if (namedReceiver == null) {
      return NO_MATCH;
    }
    String parensAndNamedArgs =
        state
            .getSourceCode()
            .subSequence(
                state.getEndPosition(namedCall.getMethodSelect()), state.getEndPosition(namedCall))
            .toString();

    SuggestedFix.Builder fix = SuggestedFix.builder();
    // We want to do something like the following, but it overlaps with some other changes we make:
    // fix.replace(namedCall, state.getSourceForNode(namedReceiver));
    fix.replace(state.getEndPosition(namedReceiver), state.getEndPosition(namedCall), "");

    if (STANDARD_ASSERT_THAT.matches(thatCall, state)) {
      fix.addStaticImport("com.google.common.truth.Truth.assertWithMessage");
      fix.replace(
          thatCall.getMethodSelect(), format("assertWithMessage%s.that", parensAndNamedArgs));
      return describeMatch(namedCall, fix.build());
    }

    if (ANY_ASSERT_THAT.matches(thatCall, state)) {
      FactoryMethodName factory = tryFindFactory(thatCall, state);
      if (factory == null) {
        if (ONLY_GENERATE_REFERENCES_TO_FACTORIES_THAT_ALREADY_EXIST) {
          return NO_MATCH;
        }

        // Guess at a good name for a factory, and rely on the user to create the factory later.
        MethodSymbol assertThatSymbol = getSymbol(thatCall);
        if (assertThatSymbol == null) {
          return NO_MATCH;
        }
        String factoryMethodEnclosingClass = assertThatSymbol.owner.getQualifiedName().toString();
        // FooSubject -> Foos:
        String factoryMethodName =
            assertThatSymbol.owner.getSimpleName().toString().replaceFirst("Subject$", "s");
        // Foos -> foos:
        factoryMethodName =
            factoryMethodName.substring(0, 1).toLowerCase() + factoryMethodName.substring(1);
        factory = FactoryMethodName.create(factoryMethodEnclosingClass, factoryMethodName);
      }
      fix.addStaticImport("com.google.common.truth.Truth.assertWithMessage");
      fix.addStaticImport(factory.clazz() + '.' + factory.method());
      fix.replace(
          thatCall.getMethodSelect(),
          format("assertWithMessage%s.about(%s()).that", parensAndNamedArgs, factory.method()));
      return describeMatch(namedCall, fix.build());
    }

    ExpressionTree thatReceiver = getReceiver(thatCall);
    if (thatReceiver == null) {
      return NO_MATCH;
    }

    if (STANDARD_SUBJECT_BUILDER_THAT.matches(thatCall, state)) {
      fix.postfixWith(thatReceiver, format(".withMessage%s", parensAndNamedArgs));
      return describeMatch(namedCall, fix.build());
    }

    if (OTHER_SUBJECT_BUILDER_THAT.matches(thatCall, state)) {
      if (ASSERT_ABOUT.matches(thatReceiver, state)) {
        if (thatReceiver.getKind() != METHOD_INVOCATION) {
          return NO_MATCH;
        }
        ExpressionTree assertAboutSelect = ((MethodInvocationTree) thatReceiver).getMethodSelect();

        fix.addStaticImport("com.google.common.truth.Truth.assertWithMessage");
        fix.replace(assertAboutSelect, format("assertWithMessage%s.about", parensAndNamedArgs));
        return describeMatch(namedCall, fix.build());
      }

      if (STANDARD_SUBJECT_BUILDER_ABOUT.matches(thatReceiver, state)) {
        ExpressionTree aboutReceiver = getReceiver(thatReceiver);
        if (aboutReceiver == null) {
          return NO_MATCH;
        }

        fix.postfixWith(aboutReceiver, format(".withMessage%s", parensAndNamedArgs));
        return describeMatch(namedCall, fix.build());
      }
    }

    return NO_MATCH;
  }

  @AutoValue
  abstract static class FactoryMethodName {
    static FactoryMethodName create(String clazz, String method) {
      return new AutoValue_NamedToWithMessage_FactoryMethodName(clazz, method);
    }

    static FactoryMethodName tryCreate(MethodSymbol symbol) {
      return symbol.params.isEmpty()
          ? create(symbol.owner.getQualifiedName().toString(), symbol.getSimpleName().toString())
          : null;
    }

    abstract String clazz();

    abstract String method();
  }

  @Nullable
  private static FactoryMethodName tryFindFactory(
      MethodInvocationTree assertThatCall, VisitorState state) {
    MethodSymbol assertThatSymbol = getSymbol(assertThatCall);
    if (assertThatSymbol == null) {
      return null;
    }
    /*
     * First, a special case for ProtoTruth.protos(). Usually the main case below finds it OK, but
     * sometimes it misses it, I believe because it can't decide between that and
     * IterableOfProtosSubject.iterableOfMessages.
     */
    if (assertThatSymbol.owner.getQualifiedName().contentEquals(PROTO_TRUTH_CLASS)) {
      return FactoryMethodName.create(PROTO_TRUTH_CLASS, "protos");
    }
    ImmutableSet<MethodSymbol> factories =
        concat(
                // The class that assertThat is declared in:
                assertThatSymbol.owner.getEnclosedElements().stream(),
                // The Subject class (possibly the same; if so, toImmutableSet() will deduplicate):
                assertThatSymbol.getReturnType().asElement().getEnclosedElements().stream())
            .filter(s -> s instanceof MethodSymbol)
            .map(s -> (MethodSymbol) s)
            .filter(
                s ->
                    returns(s, SUBJECT_FACTORY_CLASS, state)
                        || returns(s, CUSTOM_SUBJECT_BUILDER_FACTORY_CLASS, state))
            .collect(toImmutableSet());
    return factories.size() == 1 ? FactoryMethodName.tryCreate(getOnlyElement(factories)) : null;
    // TODO(cpovirk): If multiple factories exist, try filtering to visible ones only.
  }

  private static boolean returns(MethodSymbol symbol, String returnType, VisitorState state) {
    return isSubtype(symbol.getReturnType(), state.getTypeFromString(returnType), state);
  }

  private static MethodInvocationTree findThatCall(MethodInvocationTree tree, VisitorState state) {
    while (true) {
      if (tree.getMethodSelect().getKind() != MEMBER_SELECT) {
        return null;
      }
      MemberSelectTree methodSelect = (MemberSelectTree) tree.getMethodSelect();
      if (methodSelect.getExpression().getKind() != METHOD_INVOCATION) {
        return null;
      }
      tree = (MethodInvocationTree) methodSelect.getExpression();
      if (ANY_ASSERT_THAT.matches(tree, state)
          || STANDARD_SUBJECT_BUILDER_THAT.matches(tree, state)
          || OTHER_SUBJECT_BUILDER_THAT.matches(tree, state)) {
        return tree;
      }
    }
  }

  private static final String TRUTH_CLASS = "com.google.common.truth.Truth";
  private static final String PROTO_TRUTH_CLASS =
      "com.google.common.truth.extensions.proto.ProtoTruth";
  private static final String SUBJECT_CLASS = "com.google.common.truth.Subject";
  private static final String SUBJECT_FACTORY_CLASS = "com.google.common.truth.Subject.Factory";
  private static final String CUSTOM_SUBJECT_BUILDER_FACTORY_CLASS =
      "com.google.common.truth.CustomSubjectBuilder.Factory";
  private static final String STANDARD_SUBJECT_BUILDER_CLASS =
      "com.google.common.truth.StandardSubjectBuilder";
  private static final String CUSTOM_SUBJECT_BUILDER_CLASS =
      "com.google.common.truth.CustomSubjectBuilder";
  private static final String SIMPLE_SUBJECT_BUILDER_CLASS =
      "com.google.common.truth.SimpleSubjectBuilder";

  private static final Matcher<ExpressionTree> STANDARD_ASSERT_THAT =
      staticMethod().onClass(TRUTH_CLASS).named("assertThat");
  private static final Matcher<ExpressionTree> ANY_ASSERT_THAT =
      staticMethod().anyClass().named("assertThat");
  private static final Matcher<ExpressionTree> ASSERT_ABOUT =
      staticMethod().onClass(TRUTH_CLASS).named("assertAbout");

  private static final Matcher<ExpressionTree> STANDARD_SUBJECT_BUILDER_THAT =
      instanceMethod().onDescendantOf(STANDARD_SUBJECT_BUILDER_CLASS).named("that");
  private static final Matcher<ExpressionTree> STANDARD_SUBJECT_BUILDER_ABOUT =
      instanceMethod().onDescendantOf(STANDARD_SUBJECT_BUILDER_CLASS).named("about");
  private static final Matcher<ExpressionTree> OTHER_SUBJECT_BUILDER_THAT =
      anyOf(
          instanceMethod().onDescendantOf(CUSTOM_SUBJECT_BUILDER_CLASS).named("that"),
          instanceMethod().onDescendantOf(SIMPLE_SUBJECT_BUILDER_CLASS).named("that"));
  private static final Matcher<ExpressionTree> NAMED_METHOD =
      instanceMethod().onDescendantOf(SUBJECT_CLASS).named("named");

  // TODO(cpovirk): Provide a flag for this.
  private static final boolean ONLY_GENERATE_REFERENCES_TO_FACTORIES_THAT_ALREADY_EXIST = true;
}
