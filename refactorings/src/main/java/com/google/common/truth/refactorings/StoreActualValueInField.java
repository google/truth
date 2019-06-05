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
import static com.google.errorprone.fixes.SuggestedFix.replace;
import static com.google.errorprone.matchers.Description.NO_MATCH;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.constructor;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static com.google.errorprone.util.ASTHelpers.getType;
import static com.google.errorprone.util.ASTHelpers.isSameType;
import static com.google.errorprone.util.ASTHelpers.isSubtype;
import static com.sun.source.tree.Tree.Kind.CLASS;
import static com.sun.source.tree.Tree.Kind.IDENTIFIER;
import static com.sun.source.tree.Tree.Kind.MEMBER_SELECT;
import static com.sun.source.tree.Tree.Kind.METHOD_INVOCATION;
import static com.sun.source.tree.Tree.Kind.VARIABLE;
import static java.lang.String.format;
import static javax.lang.model.element.Modifier.STATIC;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeScanner;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import javax.lang.model.element.Name;

/**
 * Refactors callers of {@code Subject.actual()} and {@code Subject.getSubject()} to store their own
 * copy of the actual value in a variable and use that instead.
 */
@BugPattern(
    name = "StoreActualValueInField",
    summary =
        "Store the actual value locally instead of using the deprecated actual() and getSubject().",
    severity = SUGGESTION,
    providesFix = REQUIRES_HUMAN_ATTENTION)
public final class StoreActualValueInField extends BugChecker
    implements MethodInvocationTreeMatcher {
  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    ClassTree enclosingClass = state.findEnclosing(ClassTree.class);
    if (enclosingClass == null) {
      return NO_MATCH;
    }
    if (enclosingClass.getMembers().stream()
        .filter(t -> t.getKind() == VARIABLE)
        .map(t -> (VariableTree) t)
        .anyMatch(t -> t.getName().contentEquals("actual"))) {
      return NO_MATCH;
    }

    if (ACTUAL_METHOD.matches(tree, state)) {
      if (tree.getMethodSelect().getKind() == IDENTIFIER) {
        if (varNamedActualInScope(state)) {
          return describeMatch(tree, replace(tree, qualifierForThis(state) + "this.actual"));
        } else {
          return describeMatch(tree, replace(tree, "actual"));
        }
      } else if (tree.getMethodSelect().getKind() == MEMBER_SELECT) {
        MemberSelectTree methodSelect = (MemberSelectTree) tree.getMethodSelect();
        return describeMatch(
            tree,
            replace(
                tree, format("%s.actual", state.getSourceForNode(methodSelect.getExpression()))));
      } else {
        return NO_MATCH;
      }
    }

    if (!SUBJECT_CONSTRUCTOR_CALL.matches(tree, state)) {
      return NO_MATCH;
    }
    if (tree.getMethodSelect().getKind() != IDENTIFIER
        || !((IdentifierTree) tree.getMethodSelect()).getName().contentEquals("super")) {
      return NO_MATCH;
    }
    IdentifierTree value = findActualArg(tree.getArguments(), state);
    if (value == null) {
      return NO_MATCH;
    }
    /*
     * TODO(cpovirk): Before adding the field, scan the compilation unit for any usages of
     * ThisType.actual(). (But this is moderately rare and usually easy to detect after the fact.
     * Plus, adding the field in all cases is harmless enough.)
     */
    SuggestedFix.Builder fix = SuggestedFix.builder();
    fix.postfixWith(
        state.getPath().getParentPath().getLeaf(),
        format("this.actual = %s;", state.getSourceForNode(value)));

    Tree type = findActualFormalType(value.getName(), state);
    Tree putFieldBefore =
        enclosingClass.getMembers().stream()
            .map((Tree t) -> t) // Stream<? extends Tree> -> Stream<Tree>
            .filter(
                t ->
                    t.getKind() == VARIABLE
                        && !((VariableTree) t).getModifiers().getFlags().contains(STATIC))
            .findFirst()
            .orElse(state.findEnclosing(MethodTree.class));
    fix.prefixWith(
        putFieldBefore, format("private final %s actual;", state.getSourceForNode(type)));

    return describeMatch(tree, fix.build());
  }

  private static String qualifierForThis(VisitorState state) {
    Type subjectBaseType = state.getTypeFromString("com.google.common.truth.Subject");

    boolean seenClassInBetween = false;
    for (Tree t : state.getPath()) {
      if (t.getKind() != CLASS) {
        continue;
      }
      Type enclosingType = getType(t);
      if (isSubtype(enclosingType, subjectBaseType, state)) {
        if (seenClassInBetween) {
          return enclosingType.asElement().getSimpleName() + ".";
        } else {
          return "";
        }
      }
      seenClassInBetween = true;
    }
    return ""; // not sure what's going on, so let's try this
  }

  // from an old copy of RenameField (Similar code now lives in FieldRenamer.)
  private static boolean varNamedActualInScope(VisitorState state) {
    final AtomicBoolean local = new AtomicBoolean(false);

    MethodTree outerMostMethod = null;
    for (TreePath path = state.getPath(); path != null; path = path.getParentPath()) {
      if (path.getLeaf() instanceof MethodTree) {
        outerMostMethod = (MethodTree) path.getLeaf();
      }
    }
    if (outerMostMethod != null && outerMostMethod.getBody() != null) {
      ((JCTree) outerMostMethod.getBody())
          .accept(
              new TreeScanner() {
                @Override
                public void visitVarDef(JCVariableDecl tree) {
                  if (tree.getName().contentEquals("actual")) {
                    local.set(true);
                  }
                  super.visitVarDef(tree);
                }
              });
    }
    return local.get();
  }

  // from AbstractCollectionIncompatibleTypeMatcher
  private static Type extractTypeArgAsMemberOfSupertype(
      Type type, Symbol superTypeSym, int typeArgIndex, Types types) {
    Type collectionType = types.asSuper(type, superTypeSym);
    if (collectionType == null) {
      return null;
    }
    com.sun.tools.javac.util.List<Type> tyargs = collectionType.getTypeArguments();
    if (tyargs.size() <= typeArgIndex) {
      // Collection is raw, nothing we can do.
      return null;
    }

    return tyargs.get(typeArgIndex);
  }

  private static IdentifierTree findActualArg(
      List<? extends ExpressionTree> args, VisitorState state) {
    Type actualType =
        extractTypeArgAsMemberOfSupertype(
            getType(state.findEnclosing(ClassTree.class)),
            state.getSymbolFromString("com.google.common.truth.Subject"),
            1,
            state.getTypes());
    Type failureMetadataType = state.getTypeFromString("com.google.common.truth.FailureMetadata");
    ImmutableSet<IdentifierTree> candidates =
        args.stream()
            .flatMap(a -> maybeToIdentifier(a, state))
            .filter(a -> !isSameType(getType(a), failureMetadataType, state))
            .filter(a -> isSubtype(getType(a), actualType, state))
            .collect(toImmutableSet());
    if (candidates.size() == 1) {
      return getOnlyElement(candidates);
    }

    if (args.size() == 2
        && isSameType(getType(args.get(0)), failureMetadataType, state)
        && args.get(1).getKind() == IDENTIFIER) {
      return (IdentifierTree) args.get(1);
    }
    return null;
  }

  private static Stream<IdentifierTree> maybeToIdentifier(ExpressionTree tree, VisitorState state) {
    if (tree.getKind() == IDENTIFIER) {
      return Stream.of((IdentifierTree) tree);
    } else if (tree.getKind() == METHOD_INVOCATION && CHECK_NOT_NULL.matches(tree, state)) {
      /*
       * checkNotNull() is inadvisable (since it makes assertThat(foo) throw NPE for that type, even
       * if the assertion is going to be something like isNull()). But people do it, albeit rarely.
       */
      MethodInvocationTree invocation = (MethodInvocationTree) tree;
      return maybeToIdentifier(invocation.getArguments().get(0), state);
    } else {
      return Stream.empty();
    }
  }

  private static Tree findActualFormalType(Name name, VisitorState state) {
    MethodTree method = state.findEnclosing(MethodTree.class);
    if (method == null) {
      return null;
    }
    return method.getParameters().stream()
        .filter(p -> p.getName().equals(name))
        .findFirst()
        .map(p -> p.getType())
        .orElse(null);
  }

  private static final Matcher<ExpressionTree> SUBJECT_CONSTRUCTOR_CALL =
      constructor()
          .forClass(
              (type, state) ->
                  isSubtype(
                      type, state.getTypeFromString("com.google.common.truth.Subject"), state));
  private static final Matcher<ExpressionTree> ACTUAL_METHOD =
      anyOf(
          instanceMethod()
              .onDescendantOf("com.google.common.truth.Subject")
              .named("actual")
              .withParameters(),
          instanceMethod()
              .onDescendantOf("com.google.common.truth.Subject")
              .named("getSubject")
              .withParameters());
  private static final Matcher<ExpressionTree> CHECK_NOT_NULL =
      staticMethod().onClass("com.google.common.base.Preconditions").named("checkNotNull");
}
