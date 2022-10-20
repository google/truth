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

import static com.google.common.base.CharMatcher.inRange;
import static com.google.common.base.CharMatcher.whitespace;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.matchers.Description.NO_MATCH;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.util.ASTHelpers.constValue;
import static com.sun.source.tree.Tree.Kind.IDENTIFIER;
import static com.sun.source.tree.Tree.Kind.MEMBER_SELECT;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
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
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Migrates Truth subjects from the old {@code fail(String, Object)} to the new {@code
 * failWithActual(String, Object)}, tweaking verbs for the new grammar. For example:
 *
 * <pre>{@code
 * // Before:
 * fail("has foo", expected);
 *
 * // After:
 * failWithActual("expected to have foo", expected);
 * }</pre>
 */
@BugPattern(
    name = "FailWithFacts",
    summary = "Use the new key-value-style failure API instead of the deprecated one.",
    severity = SUGGESTION)
public final class FailWithFacts extends BugChecker implements MethodInvocationTreeMatcher {
  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!ONE_ARG_FAIL.matches(tree, state) && !TWO_ARG_FAIL.matches(tree, state)) {
      return NO_MATCH;
    }

    SuggestedFix.Builder fix = SuggestedFix.builder();
    if (tree.getMethodSelect().getKind() == IDENTIFIER) {
      fix.replace(tree.getMethodSelect(), "failWithActual");
    } else if (tree.getMethodSelect().getKind() == MEMBER_SELECT) {
      MemberSelectTree methodSelect = (MemberSelectTree) tree.getMethodSelect();
      fix.replace(
          state.getEndPosition(methodSelect.getExpression()),
          state.getEndPosition(methodSelect),
          ".failWithActual");
    } else {
      return NO_MATCH;
    }

    ExpressionTree oldVerbArg = tree.getArguments().get(0);
    String oldVerb = constValue(oldVerbArg, String.class);
    if (oldVerb == null) {
      return NO_MATCH;
    }
    String newVerb = newVerb(oldVerb);
    if (newVerb == null) {
      return NO_MATCH;
    }
    String newVerbQuoted = state.getElements().getConstantExpression(newVerb);
    if (ONE_ARG_FAIL.matches(tree, state)) {
      fix.addStaticImport("com.google.common.truth.Fact.simpleFact");
      fix.replace(oldVerbArg, format("simpleFact(%s)", newVerbQuoted));
    } else {
      fix.replace(oldVerbArg, newVerbQuoted);
    }
    return describeMatch(tree, fix.build());
  }

  private static @Nullable String newVerb(String oldVerb) {
    List<String> old = Splitter.on(whitespace()).splitToList(oldVerb);
    String first = old.get(0);
    if (CAPITAL_LETTER.matchesAnyOf(first)) {
      // "hasFoo," etc. TODO(cpovirk): Handle these.
      return null;
    }
    if (first.equals("does") && old.size() >= 2 && old.get(1).equals("not")) {
      // "Not true that foo does not exist" -> "expected not to exist"
      return "expected not to " + skip(old, 2);
    }
    if (first.equals("is") && old.size() >= 2 && old.get(1).equals("not")) {
      // "Not true that foo is not visible" -> "expected not to be visible"
      return "expected not to be " + skip(old, 2);
    }
    if (first.equals("has")) {
      // "Not true that foo has children" -> "expected to have children"
      return "expected to have " + skip(old, 1);
    } else if (first.equals("is") || first.equals("are") || first.equals("was")) {
      // "Not true that foo is empty" -> "expected to be empty"
      // "Not true that operations are complete" -> "expected to be complete"
      // "Not true that foo was deleted" -> "expected to be deleted"
      return "expected to be " + skip(old, 1);
    } else if (first.endsWith("ies")) {
      // "Not true that foo applies to bar" -> "expected apply to bar"
      return "expected to " + first.replaceFirst("ies$", "y") + " " + skip(old, 1);
    } else if (first.endsWith("ches")) {
      // "Not true that foo matches bar" -> "expected to match bar"
      return "expected to " + first.replaceFirst("ches$", "ch") + " " + skip(old, 1);
    } else if (first.matches(".*[^aeiouy]s$")) {
      // "Not true that foo contains bar" -> "expected to contain bar"
      return "expected to " + first.replaceFirst("s$", "") + " " + skip(old, 1);
    } else {
      return null;
    }
  }

  private static String skip(List<String> old, int i) {
    return old.stream().skip(i).collect(joining(" "));
  }

  private static final Matcher<ExpressionTree> ONE_ARG_FAIL =
      instanceMethod()
          .onDescendantOf("com.google.common.truth.Subject")
          .named("fail")
          .withParameters("java.lang.String");
  private static final Matcher<ExpressionTree> TWO_ARG_FAIL =
      instanceMethod()
          .onDescendantOf("com.google.common.truth.Subject")
          .named("fail")
          .withParameters("java.lang.String", "java.lang.Object");

  private static final CharMatcher CAPITAL_LETTER = inRange('A', 'Z');
}
