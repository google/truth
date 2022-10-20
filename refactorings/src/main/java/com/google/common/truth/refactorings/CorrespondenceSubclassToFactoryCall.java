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

import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static com.google.common.base.Objects.equal;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.ImmutableSetMultimap.toImmutableSetMultimap;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.MoreCollectors.onlyElement;
import static com.google.common.collect.Streams.stream;
import static com.google.common.truth.refactorings.CorrespondenceSubclassToFactoryCall.MemberType.COMPARE_METHOD;
import static com.google.common.truth.refactorings.CorrespondenceSubclassToFactoryCall.MemberType.CONSTRUCTOR;
import static com.google.common.truth.refactorings.CorrespondenceSubclassToFactoryCall.MemberType.TO_STRING_METHOD;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.fixes.SuggestedFixes.compilesWithFix;
import static com.google.errorprone.matchers.Description.NO_MATCH;
import static com.google.errorprone.util.ASTHelpers.getDeclaredSymbol;
import static com.google.errorprone.util.ASTHelpers.getSymbol;
import static com.sun.source.tree.Tree.Kind.EXPRESSION_STATEMENT;
import static com.sun.source.tree.Tree.Kind.IDENTIFIER;
import static com.sun.source.tree.Tree.Kind.MEMBER_SELECT;
import static com.sun.source.tree.Tree.Kind.METHOD_INVOCATION;
import static com.sun.source.tree.Tree.Kind.NEW_CLASS;
import static com.sun.source.tree.Tree.Kind.NULL_LITERAL;
import static com.sun.source.tree.Tree.Kind.RETURN;
import static java.lang.String.format;
import static javax.lang.model.element.ElementKind.METHOD;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;

import com.google.auto.value.AutoValue;
import com.google.common.base.CaseFormat;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.suppliers.Supplier;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Refactors some subclasses of {@code Correspondence} to instead call {@code Correspondence.from}.
 * The exact change generated for a given correspondence depends on the details of how it is defined
 * and used.
 */
@BugPattern(
    name = "CorrespondenceSubclassToFactoryCall",
    summary = "Use the factory methods on Correspondence instead of defining a subclass.",
    severity = SUGGESTION)
public final class CorrespondenceSubclassToFactoryCall extends BugChecker
    implements ClassTreeMatcher {

  private static final String CORRESPONDENCE_CLASS = "com.google.common.truth.Correspondence";

  private static final Supplier<Type> COM_GOOGLE_COMMON_TRUTH_CORRESPONDENCE =
      VisitorState.memoize(state -> state.getTypeFromString(CORRESPONDENCE_CLASS));

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    if (!isCorrespondence(tree.getExtendsClause(), state)) {
      return NO_MATCH;
    }

    List<CorrespondenceCode> replacements = computePossibleReplacements(tree, state);

    Tree parent = state.getPath().getParentPath().getLeaf();
    if (parent.getKind() == NEW_CLASS) {
      // Anonymous class. Replace the whole `new Correspondence` with a Correspondence.from call.
      for (CorrespondenceCode replacement : replacements) {
        SuggestedFix.Builder fix = SuggestedFix.builder();

        fix.replace(parent, replacement.callSite());

        Tree methodOrField = findChildOfStrictlyEnclosing(state, ClassTree.class);
        /*
         * If a class declares multiple anonymous Correspondences, we might end up creating multiple
         * compare() methods in the same scope. We might get away with it, depending on the types
         * involved, or we might need to manually rename some.
         */
        fix.postfixWith(methodOrField, replacement.supportingMethodDefinition());

        if (compilesWithFix(fix.build(), state)) {
          return describeMatch(parent, fix.build());
        }
      }
      return NO_MATCH;
    }

    Symbol classSymbol = getDeclaredSymbol(tree);
    /*
     * Named class. Create a Correspondence.from call, but then decide where to put it:
     *
     * The "safest" thing to do is to replace the body of the class with...
     *
     * static final Correspondence INSTANCE = Correspondence.from(...);
     *
     * ...and then make all callers refer to that. (As long as the class isn't top-level, we can
     * even delete the class entirely, putting the constant in its place.)
     *
     * The other option is to inline that into all use sites. That's a great option if there's a
     * single use site in a constant or helper method, in which case we produce code like...
     *
     * private static Correspondence makeCorrespondence() {
     *   return Correspondence.from(...);
     * }
     *
     * But the danger of inlining is twofold:
     *
     * 1. If there are multiple callers, we'd duplicate the definition of the Correspondence.
     *
     * 2. Even if there's only one caller, we might inline a large chunk of code into the middle of
     * a comparingElementsUsing call.
     *
     * So we use the "safe" option unless (a) there's exactly one call and (b) it's not inside a
     * comparingElementsUsing call.
     */
    SetMultimap<ParentType, NewClassTree> calls = findCalls(classSymbol, state);
    /*
     * We also sometime see users use the named type for fields and return types, like...
     *
     * static final MyCorrespondence INSTANCE = new MyCorrespondence();
     *
     * We need to replace that with the generic Correspondence type, like...
     *
     * static final Correspondence<String, Integer> INSTANCE = ...;
     */
    Set<Tree> typeReferences = findTypeReferences(classSymbol, state);

    if (calls.size() == 1 && getOnlyElement(calls.keys()) == ParentType.OTHER) {
      // Inline it.
      Tree call = getOnlyElement(calls.values());
      for (CorrespondenceCode replacement : replacements) {
        SuggestedFix.Builder fix = SuggestedFix.builder();
        replaceTypeReferences(fix, typeReferences, state.getSourceForNode(tree.getExtendsClause()));
        fix.replace(call, replacement.callSite());
        fix.replace(tree, replacement.supportingMethodDefinition());
        if (compilesWithFix(fix.build(), state)) {
          return describeMatch(tree, fix.build());
        }
      }
      return NO_MATCH;
    }

    // Declare a constant, and make use sites refer to that.

    /*
     * If we can't find any callers, then they're probably in other files, so we're going to be
     * stuck updating them manually. To make the manual updates as simple as possible, we'll declare
     * a constant named INSTANCE inside the Correspondence subclass (though it won't be a
     * Correspondence subclass after our changes, just a holder class).
     */
    if (calls.isEmpty()) {
      for (CorrespondenceCode replacement : replacements) {
        SuggestedFix.Builder fix = SuggestedFix.builder();
        replaceTypeReferences(fix, typeReferences, state.getSourceForNode(tree.getExtendsClause()));

        JCTree extendsClause = (JCTree) tree.getExtendsClause();
        // Replace everything from `extends` to the end of the class, keeping only "class Foo."
        int startOfExtends =
            state
                .getSourceCode()
                .subSequence(0, extendsClause.getStartPosition())
                .toString()
                .lastIndexOf("extends");
        fix.replace(
            startOfExtends,
            state.getEndPosition(tree),
            format(
                "{ %s static final %s INSTANCE = %s; %s }",
                visibilityModifierOnConstructor(tree),
                state.getSourceForNode(extendsClause),
                replacement.callSite(),
                replacement.supportingMethodDefinition()));

        for (Tree call : calls.values()) {
          fix.replace(call, tree.getSimpleName() + ".INSTANCE");
        }

        if (compilesWithFix(fix.build(), state)) {
          return describeMatch(tree, fix.build());
        }
      }
      return NO_MATCH;
    }

    /*
     * We found callers. Let's optimistically assume that we found them all, in which case we might
     * as well replace the whole class with a constant.
     */
    for (CorrespondenceCode replacement : replacements) {
      SuggestedFix.Builder fix = SuggestedFix.builder();
      replaceTypeReferences(fix, typeReferences, state.getSourceForNode(tree.getExtendsClause()));

      String name = CaseFormat.UPPER_CAMEL.to(UPPER_UNDERSCORE, tree.getSimpleName().toString());

      // TODO(cpovirk): We're unlikely to get away with declaring everything `static`.
      fix.replace(
          tree,
          format(
              "%s static final %s %s = %s; %s",
              visibilityModifierOnConstructor(tree),
              state.getSourceForNode(tree.getExtendsClause()),
              name,
              replacement.callSite(),
              replacement.supportingMethodDefinition()));

      for (Tree call : calls.values()) {
        fix.replace(call, name);
      }

      if (compilesWithFix(fix.build(), state)) {
        return describeMatch(tree, fix.build());
      }
    }
    return NO_MATCH;
  }

  private static void replaceTypeReferences(
      SuggestedFix.Builder fix, Set<Tree> typeReferences, String newType) {
    for (Tree reference : typeReferences) {
      fix.replace(reference, newType);
    }
  }

  private String visibilityModifierOnConstructor(ClassTree tree) {
    MethodTree constructor =
        tree.getMembers().stream()
            .filter(t -> t instanceof MethodTree)
            .map(t -> (MethodTree) t)
            .filter(t -> t.getName().contentEquals("<init>"))
            .findAny()
            .get();
    // We don't include the ModifiersTree directly in case it contains any annotations.
    return constructor.getModifiers().getFlags().stream()
        .filter(m -> m == PUBLIC || m == PROTECTED || m == PRIVATE)
        .findAny()
        .map(Modifier::toString)
        .orElse("");
  }

  /** Returns all calls to the constructor for the given {@code classSymbol}, organized by {@linkplain ParentType whether they happen inside a call to {@code comparingElementsUsing}. */
  private static SetMultimap<ParentType, NewClassTree> findCalls(
      Symbol classSymbol, VisitorState state) {
    SetMultimap<ParentType, NewClassTree> calls = HashMultimap.create();
    new TreeScanner<Void, Void>() {
      private ParentType parentType = ParentType.OTHER;

      @Override
      public @Nullable Void visitMethodInvocation(MethodInvocationTree node, Void unused) {
        boolean isComparingElementsUsing =
            Optional.of(node.getMethodSelect())
                .filter(t -> t.getKind() == MEMBER_SELECT)
                .map(t -> (MemberSelectTree) t)
                .filter(t -> t.getIdentifier().contentEquals("comparingElementsUsing"))
                .isPresent();
        if (isComparingElementsUsing) {
          ParentType oldParentType = parentType;
          parentType = ParentType.COMPARING_ELEMENTS_USING;
          super.visitMethodInvocation(node, unused);
          parentType = oldParentType;
        } else {
          super.visitMethodInvocation(node, unused);
        }
        return null;
      }

      @Override
      public @Nullable Void visitNewClass(NewClassTree node, Void unused) {
        if (getSymbol(node.getIdentifier()).equals(classSymbol)) {
          calls.put(parentType, node);
        }
        return super.visitNewClass(node, unused);
      }
    }.scan(state.findEnclosing(CompilationUnitTree.class), null);
    return calls;
  }

  /**
   * Finds all references to the name of the given type, excluding (a) when the name is defined
   * (e.g, {@code public class MyCorrespondence}) and (b) calls to its constructor (e.g., {@code new
   * MyCorrespondence()}). The goal is to find every reference that we aren't already modifying so
   * that we can rewrite, e.g., fields of the given type.
   */
  private static Set<Tree> findTypeReferences(Symbol classSymbol, VisitorState state) {
    Set<Tree> references = new HashSet<>();
    new TreeScanner<Void, Void>() {
      @Override
      public @Nullable Void scan(Tree node, Void unused) {
        if (equal(getSymbol(node), classSymbol)
            && getDeclaredSymbol(node) == null // Don't touch the ClassTree that we're replacing.
        ) {
          references.add(node);
        }
        return super.scan(node, unused);
      }

      @Override
      public @Nullable Void visitNewClass(NewClassTree node, Void aVoid) {
        scan(node.getEnclosingExpression(), null);
        // Do NOT scan node.getIdentifier().
        scan(node.getTypeArguments(), null);
        scan(node.getArguments(), null);
        scan(node.getClassBody(), null);
        return null;
      }
    }.scan(state.findEnclosing(CompilationUnitTree.class), null);
    return references;
  }

  /**
   * Whether the instantiation of a correspondence happens directly inside a call to {@code
   * comparingElementsUsing} or not. For example, {@code comparingElementsUsing(new
   * MyCorrespondence())} and {@code comparingElementsUsing(new Correspondence() { ... })} are both
   * considered to have type {@link #COMPARING_ELEMENTS_USING}, but {@code private static final
   * MyCorrespondence CORRESPONDENCE = new MyCorrespondence()} does not.
   */
  enum ParentType {
    COMPARING_ELEMENTS_USING,
    OTHER,
  }

  /**
   * If the given correspondence implementation is "simple enough," returns one or more possible
   * replacements for its definition and instantiation sites.
   */
  private static ImmutableList<CorrespondenceCode> computePossibleReplacements(
      ClassTree classTree, VisitorState state) {
    ImmutableSetMultimap<MemberType, Tree> members =
        classTree.getMembers().stream()
            .collect(toImmutableSetMultimap(m -> MemberType.from(m, state), m -> m));
    if (members.containsKey(MemberType.OTHER)
        || members.get(CONSTRUCTOR).size() != 1
        || members.get(COMPARE_METHOD).size() != 1
        || members.get(TO_STRING_METHOD).size() != 1) {
      return ImmutableList.of();
    }
    MethodTree constructor = (MethodTree) getOnlyElement(members.get(CONSTRUCTOR));
    MethodTree compareMethod = (MethodTree) getOnlyElement(members.get(COMPARE_METHOD));
    MethodTree toStringMethod = (MethodTree) getOnlyElement(members.get(TO_STRING_METHOD));

    if (!constructorCallsOnlySuper(constructor)) {
      return ImmutableList.of();
    }

    ImmutableList<BinaryPredicateCode> binaryPredicates =
        makeBinaryPredicates(classTree, compareMethod, state);

    ExpressionTree toStringReturns = returnExpression(toStringMethod);
    if (toStringReturns == null) {
      return ImmutableList.of();
    }
    /*
     * Replace bad toString() implementations that merely `return null`, since the factories make
     * that an immediate error.
     */
    String description =
        toStringReturns.getKind() == NULL_LITERAL
            ? "\"corresponds to\""
            : state.getSourceForNode(toStringReturns);
    return binaryPredicates.stream()
        .map(p -> CorrespondenceCode.create(p, description))
        .collect(toImmutableList());
  }

  /** Returns one or more possible replacements for the given correspondence's {@code compare} method's definition and for code to pass to {@code Correspondence.from) to construct a correspondence that uses the replacement. */
  private static ImmutableList<BinaryPredicateCode> makeBinaryPredicates(
      ClassTree classTree, MethodTree compareMethod, VisitorState state) {
    Tree comparison = maybeMakeLambdaBody(compareMethod, state);
    if (comparison == null) {
      ClassTree enclosing = findStrictlyEnclosing(state, ClassTree.class);
      CharSequence newCompareMethodOwner;
      String newCompareMethodName;
      if (enclosing == null) {
        newCompareMethodName = "compare";
        newCompareMethodOwner = classTree.getSimpleName();
      } else {
        newCompareMethodName =
            "compare" + classTree.getSimpleName().toString().replaceFirst("Correspondence$", "");
        newCompareMethodOwner = enclosing.getSimpleName();
      }
      // TODO(cpovirk): We're unlikely to get away with declaring everything `static`.
      String supportingMethodDefinition =
          format(
              "private static boolean %s(%s, %s) %s",
              newCompareMethodName,
              state.getSourceForNode(compareMethod.getParameters().get(0)),
              state.getSourceForNode(compareMethod.getParameters().get(1)),
              state.getSourceForNode(compareMethod.getBody()));
      return ImmutableList.of(
          BinaryPredicateCode.create(
              newCompareMethodOwner + "::" + newCompareMethodName, supportingMethodDefinition));
    }
    // First try without types, then try with.
    return ImmutableList.of(
        BinaryPredicateCode.fromParamsAndExpression(
            compareMethod.getParameters().get(0).getName(),
            compareMethod.getParameters().get(1).getName(),
            state.getSourceForNode(comparison)),
        BinaryPredicateCode.fromParamsAndExpression(
            state.getSourceForNode(compareMethod.getParameters().get(0)),
            state.getSourceForNode(compareMethod.getParameters().get(1)),
            state.getSourceForNode(comparison)));
  }

  /**
   * Converts the given method into a lambda, either expression or block, if "appropriate." For
   * details about the various cases, see implementation comments.
   */
  private static @Nullable Tree maybeMakeLambdaBody(MethodTree compareMethod, VisitorState state) {
    ExpressionTree comparison = returnExpression(compareMethod);
    if (comparison != null) {
      // compare() is defined as simply `return something;`. Create a lambda.
      return comparison;
    }

    /*
     * compare() has multiple statements. Let's keep it as a method (though we might change its
     * modifiers, name, and location) and construct the Correspondence with a method reference...
     *
     * ...unless it relies on parameters from the enclosing method, in which case extracting a
     * method isn't going to work because it won't be able to access those parameters.
     */
    MethodTree enclosingMethod = state.findEnclosing(MethodTree.class);
    if (enclosingMethod == null) {
      // No enclosing method, so we're presumably not closing over anything. Safe to extract method.
      return null;
    }

    ImmutableSet<Symbol> paramsOfEnclosingMethod =
        enclosingMethod.getParameters().stream()
            .map(p -> getDeclaredSymbol(p))
            .collect(toImmutableSet());
    boolean[] referenceFound = new boolean[1];
    new TreeScanner<Void, Void>() {
      @Override
      public @Nullable Void scan(Tree node, Void aVoid) {
        if (paramsOfEnclosingMethod.contains(getSymbol(node))) {
          referenceFound[0] = true;
        }
        return super.scan(node, aVoid);
      }
    }.scan(state.getPath().getLeaf(), null);

    if (!referenceFound[0]) {
      // No references to anything from the enclosing method. Probably safe to extract a method.
      return null;
    }

    /*
     * compare() both:
     *
     * - uses a parameter from the enclosing method and
     *
     * - has multiple statements
     *
     * So we create a block lambda.
     */
    return compareMethod.getBody();
  }

  /** Like {@link VisitorState#findEnclosing} but doesn't consider the leaf to enclose itself. */
  private static <T extends Tree> @Nullable T findStrictlyEnclosing(
      VisitorState state, Class<T> clazz) {
    return stream(state.getPath().getParentPath())
        .filter(clazz::isInstance)
        .map(clazz::cast)
        .findAny()
        .orElse(null);
  }

  /**
   * Like {@link #findStrictlyEnclosing} but returns not the found element but its child along the
   * path. For example, if called with {@code ClassTree}, it might return a {@code MethodTree}
   * inside the class.
   */
  private static @Nullable Tree findChildOfStrictlyEnclosing(
      VisitorState state, Class<? extends Tree> clazz) {
    Tree previous = state.getPath().getLeaf();
    for (Tree t : state.getPath().getParentPath()) {
      if (clazz.isInstance(t)) {
        return previous;
      }
      previous = t;
    }
    return null;
  }

  /**
   * A {@code Correspondence.from} call to replace the instantiation site of a {@code
   * Correspondence}. Often the call is self-contained (if it's a lambda), but sometimes it's a
   * method reference, in which case it's accompanied by a separate method definition.
   */
  @AutoValue
  abstract static class CorrespondenceCode {
    static CorrespondenceCode create(BinaryPredicateCode binaryPredicate, String description) {
      return new AutoValue_CorrespondenceSubclassToFactoryCall_CorrespondenceCode(
          binaryPredicate, description);
    }

    abstract BinaryPredicateCode binaryPredicate();

    abstract String description();

    final String callSite() {
      return format("Correspondence.from(%s, %s)", binaryPredicate().usage(), description());
    }

    final String supportingMethodDefinition() {
      return binaryPredicate().supportingMethodDefinition().orElse("");
    }
  }

  /**
   * Code that can be inserted as the first argument of a {@code Correspondence.from} call. Often
   * it's a lambda, but sometimes it's a method reference, in which case it's accompanied by a
   * separate method definition.
   */
  @AutoValue
  abstract static class BinaryPredicateCode {
    static BinaryPredicateCode fromParamsAndExpression(
        CharSequence param0, CharSequence param1, String expression) {
      return create(String.format("(%s, %s) -> %s", param0, param1, expression), null);
    }

    static BinaryPredicateCode create(String usage, @Nullable String supportingMethodDefinition) {
      return new AutoValue_CorrespondenceSubclassToFactoryCall_BinaryPredicateCode(
          usage, Optional.ofNullable(supportingMethodDefinition));
    }

    abstract String usage();

    abstract Optional<String> supportingMethodDefinition();
  }

  private static @Nullable ExpressionTree returnExpression(MethodTree method) {
    List<? extends StatementTree> statements = method.getBody().getStatements();
    if (statements.size() != 1) {
      return null;
    }
    StatementTree statement = getOnlyElement(statements);
    if (statement.getKind() != RETURN) {
      return null;
    }
    return ((ReturnTree) statement).getExpression();
  }

  private static boolean constructorCallsOnlySuper(MethodTree constructor) {
    List<? extends StatementTree> statements = constructor.getBody().getStatements();
    if (statements.size() != 1) {
      return false;
    }
    StatementTree statement = getOnlyElement(statements);
    if (statement.getKind() != EXPRESSION_STATEMENT) {
      return false;
    }
    ExpressionTree expression = ((ExpressionStatementTree) statement).getExpression();
    if (expression.getKind() != METHOD_INVOCATION) {
      return false;
    }
    ExpressionTree methodSelect = ((MethodInvocationTree) expression).getMethodSelect();
    if (methodSelect.getKind() != IDENTIFIER
        || !((IdentifierTree) methodSelect).getName().contentEquals("super")) {
      return false;
    }
    return true;
  }

  /**
   * Whether a member is of one of the standard {@link Correspondence} methods that we know how to
   * migrate and, if so, which one.
   */
  enum MemberType {
    CONSTRUCTOR,
    COMPARE_METHOD,
    TO_STRING_METHOD,
    OTHER,
    ;

    static MemberType from(Tree tree, VisitorState state) {
      Symbol symbol = getDeclaredSymbol(tree);
      if (!(symbol instanceof MethodSymbol)) {
        return OTHER;
      }
      MethodSymbol methodSymbol = (MethodSymbol) symbol;

      if (methodSymbol.getSimpleName().contentEquals("<init>")) {
        return CONSTRUCTOR;
      } else if (overrides(methodSymbol, CORRESPONDENCE_CLASS, "compare", state)) {
        return COMPARE_METHOD;
      } else if (overrides(methodSymbol, "java.lang.Object", "toString", state)) {
        return TO_STRING_METHOD;
      } else {
        return OTHER;
      }
    }
  }

  private static boolean overrides(
      MethodSymbol potentialOverrider, String clazz, String method, VisitorState state) {
    Symbol overridable =
        state.getTypeFromString(clazz).tsym.getEnclosedElements().stream()
            .filter(s -> s.getKind() == METHOD)
            .filter(m -> m.getSimpleName().contentEquals(method))
            .collect(onlyElement());
    return potentialOverrider.getSimpleName().contentEquals(method)
        && potentialOverrider.overrides(
            overridable, (TypeSymbol) overridable.owner, state.getTypes(), true);
  }

  private static boolean isCorrespondence(Tree supertypeTree, VisitorState state) {
    Type correspondenceType = COM_GOOGLE_COMMON_TRUTH_CORRESPONDENCE.get(state);
    if (correspondenceType == null) {
      return false;
    }
    return supertypeTree != null
        && state.getTypes().isSameType(getSymbol(supertypeTree).type, correspondenceType);
  }
}
