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

package com.google.common.truth.extensions.proto;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.truth.extensions.proto.FieldScopeUtil.join;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.ForOverride;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import java.util.List;

/**
 * Implementations of all variations of {@link FieldScope} logic.
 *
 * <p>{@code FieldScopeLogic} is the abstract base class which provides common functionality to all
 * sub-types. There are two classes of sub-types:
 *
 * <ul>
 *   <li>Concrete subtypes, which implements specific rules and perform no delegation.
 *   <li>Compound subtypes, which combine one or more {@code FieldScopeLogic}s with specific
 *       operations.
 * </ul>
 */
abstract class FieldScopeLogic implements FieldScopeLogicContainer<FieldScopeLogic> {

  /**
   * Returns whether the given field is included in this FieldScopeLogic, along with whether it's
   * included recursively or not.
   */
  abstract FieldScopeResult policyFor(Descriptor rootDescriptor, SubScopeId subScopeId);

  /** Returns whether the given field is included in this FieldScopeLogic. */
  final boolean contains(Descriptor rootDescriptor, SubScopeId subScopeId) {
    return policyFor(rootDescriptor, subScopeId).included();
  }

  /**
   * Returns a {@code FieldScopeLogic} to handle the message pointed to by this descriptor.
   *
   * <p>Subclasses which can return non-recursive {@link FieldScopeResult}s must override {@link
   * #subScopeImpl} to implement those cases.
   */
  @Override
  public final FieldScopeLogic subScope(Descriptor rootDescriptor, SubScopeId subScopeId) {
    FieldScopeResult result = policyFor(rootDescriptor, subScopeId);
    if (result.recursive()) {
      return result.included() ? all() : none();
    } else {
      return subScopeImpl(rootDescriptor, subScopeId);
    }
  }

  /**
   * Returns {@link #subScope} for {@code NONRECURSIVE} results.
   *
   * <p>Throws an {@link UnsupportedOperationException} by default. Subclasses which can return
   * {@code NONRECURSIVE} results must override this method.
   */
  @ForOverride
  FieldScopeLogic subScopeImpl(Descriptor rootDescriptor, SubScopeId subScopeId) {
    throw new UnsupportedOperationException("subScopeImpl not implemented for " + getClass());
  }

  /**
   * Returns an accurate description for debugging purposes.
   *
   * <p>Compare to {@link FieldScope#usingCorrespondenceString(Optional)}, which returns a beautiful
   * error message that makes as much sense to the user as possible.
   *
   * <p>Abstract so subclasses must implement.
   */
  @Override
  public abstract String toString();

  @Override
  public void validate(
      Descriptor rootDescriptor, FieldDescriptorValidator fieldDescriptorValidator) {}

  private static boolean isEmpty(Iterable<?> container) {
    boolean isEmpty = true;
    for (Object element : container) {
      checkNotNull(element);
      isEmpty = false;
    }

    return isEmpty;
  }

  // TODO(user): Rename these 'ignoring' and 'allowing' methods to 'plus' and 'minus', or
  // something else that doesn't tightly couple FieldScopeLogic to the 'ignore' concept.
  FieldScopeLogic ignoringFields(Iterable<Integer> fieldNumbers) {
    if (isEmpty(fieldNumbers)) {
      return this;
    }
    return and(
        this,
        new NegationFieldScopeLogic(new FieldNumbersLogic(fieldNumbers, /* isRecursive = */ true)));
  }

  FieldScopeLogic ignoringFieldDescriptors(Iterable<FieldDescriptor> fieldDescriptors) {
    if (isEmpty(fieldDescriptors)) {
      return this;
    }
    return and(
        this,
        new NegationFieldScopeLogic(
            new FieldDescriptorsLogic(fieldDescriptors, /* isRecursive = */ true)));
  }

  FieldScopeLogic allowingFields(Iterable<Integer> fieldNumbers) {
    if (isEmpty(fieldNumbers)) {
      return this;
    }
    return or(this, new FieldNumbersLogic(fieldNumbers, /* isRecursive = */ true));
  }

  FieldScopeLogic allowingFieldsNonRecursive(Iterable<Integer> fieldNumbers) {
    if (isEmpty(fieldNumbers)) {
      return this;
    }
    return or(this, new FieldNumbersLogic(fieldNumbers, /* isRecursive = */ false));
  }

  FieldScopeLogic allowingFieldDescriptors(Iterable<FieldDescriptor> fieldDescriptors) {
    if (isEmpty(fieldDescriptors)) {
      return this;
    }
    return or(this, new FieldDescriptorsLogic(fieldDescriptors, /* isRecursive = */ true));
  }

  FieldScopeLogic allowingFieldDescriptorsNonRecursive(Iterable<FieldDescriptor> fieldDescriptors) {
    if (isEmpty(fieldDescriptors)) {
      return this;
    }
    return or(this, new FieldDescriptorsLogic(fieldDescriptors, /* isRecursive = */ false));
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // CONCRETE SUBTYPES
  //////////////////////////////////////////////////////////////////////////////////////////////////

  /** Returns whether this is equivalent to {@code FieldScopeLogic.all()}. */
  boolean isAll() {
    return false;
  }

  private static final FieldScopeLogic ALL =
      new FieldScopeLogic() {
        @Override
        public String toString() {
          return "FieldScopes.all()";
        }

        @Override
        final FieldScopeResult policyFor(Descriptor rootDescriptor, SubScopeId subScopeId) {
          return FieldScopeResult.INCLUDED_RECURSIVELY;
        }

        @Override
        final boolean isAll() {
          return true;
        }
      };

  private static final FieldScopeLogic NONE =
      new FieldScopeLogic() {
        @Override
        public String toString() {
          return "FieldScopes.none()";
        }

        @Override
        final FieldScopeResult policyFor(Descriptor rootDescriptor, SubScopeId subScopeId) {
          return FieldScopeResult.EXCLUDED_RECURSIVELY;
        }
      };

  static FieldScopeLogic all() {
    return ALL;
  }

  static FieldScopeLogic none() {
    return NONE;
  }

  private static class PartialScopeLogic extends FieldScopeLogic {
    private static final PartialScopeLogic EMPTY = new PartialScopeLogic(FieldNumberTree.empty());

    private final FieldNumberTree fieldNumberTree;

    PartialScopeLogic(FieldNumberTree fieldNumberTree) {
      this.fieldNumberTree = fieldNumberTree;
    }

    @Override
    public String toString() {
      return String.format("PartialScopeLogic(%s)", fieldNumberTree);
    }

    @Override
    final FieldScopeResult policyFor(Descriptor rootDescriptor, SubScopeId subScopeId) {
      return fieldNumberTree.hasChild(subScopeId)
          ? FieldScopeResult.INCLUDED_NONRECURSIVELY
          : FieldScopeResult.EXCLUDED_RECURSIVELY;
    }

    @Override
    final FieldScopeLogic subScopeImpl(Descriptor rootDescriptor, SubScopeId subScopeId) {
      return newPartialScopeLogic(fieldNumberTree.child(subScopeId));
    }

    private static PartialScopeLogic newPartialScopeLogic(FieldNumberTree fieldNumberTree) {
      return fieldNumberTree.isEmpty() ? EMPTY : new PartialScopeLogic(fieldNumberTree);
    }
  }

  private static final class RootPartialScopeLogic extends PartialScopeLogic {
    private final String repr;
    private final Descriptor expectedDescriptor;

    RootPartialScopeLogic(FieldNumberTree fieldNumberTree, String repr, Descriptor descriptor) {
      super(fieldNumberTree);
      this.repr = repr;
      this.expectedDescriptor = descriptor;
    }

    @Override
    public void validate(
        Descriptor rootDescriptor, FieldDescriptorValidator fieldDescriptorValidator) {
      Verify.verify(
          fieldDescriptorValidator == FieldDescriptorValidator.ALLOW_ALL,
          "PartialScopeLogic doesn't support custom field validators.");

      checkArgument(
          expectedDescriptor.equals(rootDescriptor),
          "Message given to FieldScopes.fromSetFields() does not have the same descriptor as the "
              + "message being tested. Expected %s, got %s.",
          expectedDescriptor.getFullName(),
          rootDescriptor.getFullName());
    }

    @Override
    public String toString() {
      return String.format("FieldScopes.fromSetFields(%s)", repr);
    }
  }

  static FieldScopeLogic partialScope(Message message) {
    return new RootPartialScopeLogic(
        FieldNumberTree.fromMessage(message), message.toString(), message.getDescriptorForType());
  }

  static FieldScopeLogic partialScope(Iterable<? extends Message> messages, Descriptor descriptor) {
    return new RootPartialScopeLogic(
        FieldNumberTree.fromMessages(messages),
        Joiner.on(", ").useForNull("null").join(messages),
        descriptor);
  }

  // TODO(user): Performance: Optimize FieldNumbersLogic and FieldDescriptorsLogic for
  // adding / ignoring field numbers and descriptors, respectively, to eliminate high recursion
  // costs for long chains of allows/ignores.

  // Common functionality for FieldNumbersLogic and FieldDescriptorsLogic.
  private abstract static class FieldMatcherLogicBase extends FieldScopeLogic {

    private final boolean isRecursive;

    protected FieldMatcherLogicBase(boolean isRecursive) {
      this.isRecursive = isRecursive;
    }

    /**
     * Determines whether the FieldDescriptor is equal to one of the explicitly defined components
     * of this FieldScopeLogic.
     *
     * @param descriptor Descriptor of the message being tested.
     * @param fieldDescriptor FieldDescriptor being inspected for a direct match to the scope's
     *     definition.
     */
    abstract boolean matchesFieldDescriptor(Descriptor descriptor, FieldDescriptor fieldDescriptor);

    @Override
    final FieldScopeResult policyFor(Descriptor rootDescriptor, SubScopeId subScopeId) {
      if (subScopeId.kind() == SubScopeId.Kind.UNKNOWN_FIELD_DESCRIPTOR) {
        return FieldScopeResult.EXCLUDED_RECURSIVELY;
      }

      FieldDescriptor fieldDescriptor = subScopeId.fieldDescriptor();
      if (matchesFieldDescriptor(rootDescriptor, fieldDescriptor)) {
        return FieldScopeResult.of(/* included = */ true, isRecursive);
      }

      // We return 'EXCLUDED_NONRECURSIVELY' for both field descriptor scopes and field number
      // scopes. In the former case, the field descriptors are arbitrary, so it's always possible we
      // find a hit on a sub-message somewhere.  In the latter case, the message definition may be
      // cyclic, so we need to return 'EXCLUDED_NONRECURSIVELY' even if the top level field number
      // doesn't match.
      return FieldScopeResult.EXCLUDED_NONRECURSIVELY;
    }

    @Override
    final FieldScopeLogic subScopeImpl(Descriptor rootDescriptor, SubScopeId subScopeId) {
      return this;
    }

    @Override
    public void validate(
        Descriptor rootDescriptor, FieldDescriptorValidator fieldDescriptorValidator) {
      if (isRecursive) {
        Verify.verify(
            fieldDescriptorValidator == FieldDescriptorValidator.ALLOW_ALL,
            "Field descriptor validators are not supported "
                + "for non-recursive field matcher logics.");
      }
    }
  }

  // Matches any specific fields which fall under a sub-message field (or root) matching the root
  // message type and one of the specified field numbers.
  private static final class FieldNumbersLogic extends FieldMatcherLogicBase {
    private final ImmutableSet<Integer> fieldNumbers;

    FieldNumbersLogic(Iterable<Integer> fieldNumbers, boolean isRecursive) {
      super(isRecursive);
      this.fieldNumbers = ImmutableSet.copyOf(fieldNumbers);
    }

    @Override
    public void validate(
        Descriptor rootDescriptor, FieldDescriptorValidator fieldDescriptorValidator) {
      super.validate(rootDescriptor, fieldDescriptorValidator);
      for (int fieldNumber : fieldNumbers) {
        FieldDescriptor fieldDescriptor = rootDescriptor.findFieldByNumber(fieldNumber);
        checkArgument(
            fieldDescriptor != null,
            "Message type %s has no field with number %s.",
            rootDescriptor.getFullName(),
            fieldNumber);
        fieldDescriptorValidator.validate(fieldDescriptor);
      }
    }

    @Override
    boolean matchesFieldDescriptor(Descriptor descriptor, FieldDescriptor fieldDescriptor) {
      return fieldDescriptor.getContainingType() == descriptor
          && fieldNumbers.contains(fieldDescriptor.getNumber());
    }

    @Override
    public String toString() {
      return String.format("FieldScopes.allowingFields(%s)", join(fieldNumbers));
    }
  }

  // Matches any specific fields which fall under one of the specified FieldDescriptors.
  private static final class FieldDescriptorsLogic extends FieldMatcherLogicBase {
    private final ImmutableSet<FieldDescriptor> fieldDescriptors;

    FieldDescriptorsLogic(Iterable<FieldDescriptor> fieldDescriptors, boolean isRecursive) {
      super(isRecursive);
      this.fieldDescriptors = ImmutableSet.copyOf(fieldDescriptors);
    }

    @Override
    boolean matchesFieldDescriptor(Descriptor descriptor, FieldDescriptor fieldDescriptor) {
      return fieldDescriptors.contains(fieldDescriptor);
    }

    @Override
    public void validate(
        Descriptor rootDescriptor, FieldDescriptorValidator fieldDescriptorValidator) {
      super.validate(rootDescriptor, fieldDescriptorValidator);
      for (FieldDescriptor fieldDescriptor : fieldDescriptors) {
        fieldDescriptorValidator.validate(fieldDescriptor);
      }
    }

    @Override
    public String toString() {
      return String.format("FieldScopes.allowingFieldDescriptors(%s)", join(fieldDescriptors));
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // COMPOUND SUBTYPES
  //////////////////////////////////////////////////////////////////////////////////////////////////

  private abstract static class CompoundFieldScopeLogic<T extends CompoundFieldScopeLogic<T>>
      extends FieldScopeLogic {
    final ImmutableList<FieldScopeLogic> elements;

    CompoundFieldScopeLogic(FieldScopeLogic singleElem) {
      this.elements = ImmutableList.of(singleElem);
    }

    CompoundFieldScopeLogic(FieldScopeLogic firstElem, FieldScopeLogic secondElem) {
      this.elements = ImmutableList.of(firstElem, secondElem);
    }

    @Override
    public final void validate(
        Descriptor rootDescriptor, FieldDescriptorValidator fieldDescriptorValidator) {
      for (FieldScopeLogic elem : elements) {
        elem.validate(rootDescriptor, fieldDescriptorValidator);
      }
    }

    /** Helper to produce a new {@code CompoundFieldScopeLogic} of the same type as the subclass. */
    abstract T newLogicOfSameType(List<FieldScopeLogic> newElements);

    @Override
    final FieldScopeLogic subScopeImpl(Descriptor rootDescriptor, SubScopeId subScopeId) {
      ImmutableList.Builder<FieldScopeLogic> builder =
          ImmutableList.builderWithExpectedSize(elements.size());
      for (FieldScopeLogic elem : elements) {
        builder.add(elem.subScope(rootDescriptor, subScopeId));
      }
      return newLogicOfSameType(builder.build());
    }
  }

  private static final class IntersectionFieldScopeLogic
      extends CompoundFieldScopeLogic<IntersectionFieldScopeLogic> {
    IntersectionFieldScopeLogic(FieldScopeLogic subject1, FieldScopeLogic subject2) {
      super(subject1, subject2);
    }

    @Override
    IntersectionFieldScopeLogic newLogicOfSameType(List<FieldScopeLogic> newElements) {
      checkArgument(newElements.size() == 2, "Expected 2 elements: %s", newElements);
      return new IntersectionFieldScopeLogic(newElements.get(0), newElements.get(1));
    }

    @Override
    FieldScopeResult policyFor(Descriptor rootDescriptor, SubScopeId subScopeId) {
      // The intersection of two scopes is ignorable if either scope is itself ignorable.
      return intersection(
          elements.get(0).policyFor(rootDescriptor, subScopeId),
          elements.get(1).policyFor(rootDescriptor, subScopeId));
    }

    private static FieldScopeResult intersection(
        FieldScopeResult result1, FieldScopeResult result2) {
      if (result1 == FieldScopeResult.EXCLUDED_RECURSIVELY
          || result2 == FieldScopeResult.EXCLUDED_RECURSIVELY) {
        // If either argument is excluded recursively, the result is too.
        return FieldScopeResult.EXCLUDED_RECURSIVELY;
      } else if (!result1.included() || !result2.included()) {
        // Otherwise, we exclude non-recursively if either result is an exclusion.
        return FieldScopeResult.EXCLUDED_NONRECURSIVELY;
      } else if (result1.recursive() && result2.recursive()) {
        // We include recursively if both arguments are recursive.
        return FieldScopeResult.INCLUDED_RECURSIVELY;
      } else {
        // Otherwise, we include non-recursively.
        return FieldScopeResult.INCLUDED_NONRECURSIVELY;
      }
    }

    @Override
    public String toString() {
      return String.format("(%s && %s)", elements.get(0), elements.get(1));
    }
  }

  private static final class UnionFieldScopeLogic
      extends CompoundFieldScopeLogic<UnionFieldScopeLogic> {
    UnionFieldScopeLogic(FieldScopeLogic subject1, FieldScopeLogic subject2) {
      super(subject1, subject2);
    }

    @Override
    UnionFieldScopeLogic newLogicOfSameType(List<FieldScopeLogic> newElements) {
      checkArgument(newElements.size() == 2, "Expected 2 elements: %s", newElements);
      return new UnionFieldScopeLogic(newElements.get(0), newElements.get(1));
    }

    @Override
    FieldScopeResult policyFor(Descriptor rootDescriptor, SubScopeId subScopeId) {
      // The union of two scopes is ignorable only if both scopes are themselves ignorable.
      return union(
          elements.get(0).policyFor(rootDescriptor, subScopeId),
          elements.get(1).policyFor(rootDescriptor, subScopeId));
    }

    private static FieldScopeResult union(FieldScopeResult result1, FieldScopeResult result2) {
      if (result1 == FieldScopeResult.INCLUDED_RECURSIVELY
          || result2 == FieldScopeResult.INCLUDED_RECURSIVELY) {
        // If either argument is included recursively, the result is too.
        return FieldScopeResult.INCLUDED_RECURSIVELY;
      } else if (result1.included() || result2.included()) {
        // Otherwise, if either is included, we include non-recursively.
        return FieldScopeResult.INCLUDED_NONRECURSIVELY;
      } else if (result1.recursive() && result2.recursive()) {
        // If both arguments are recursive, we exclude recursively.
        return FieldScopeResult.EXCLUDED_RECURSIVELY;
      } else {
        // Otherwise, we exclude exclude non-recursively.
        return FieldScopeResult.EXCLUDED_NONRECURSIVELY;
      }
    }

    @Override
    public String toString() {
      return String.format("(%s || %s)", elements.get(0), elements.get(1));
    }
  }

  private static final class NegationFieldScopeLogic
      extends CompoundFieldScopeLogic<NegationFieldScopeLogic> {
    NegationFieldScopeLogic(FieldScopeLogic subject) {
      super(subject);
    }

    @Override
    NegationFieldScopeLogic newLogicOfSameType(List<FieldScopeLogic> newElements) {
      checkArgument(newElements.size() == 1, "Expected 1 element: %s", newElements);
      return new NegationFieldScopeLogic(newElements.get(0));
    }

    @Override
    FieldScopeResult policyFor(Descriptor rootDescriptor, SubScopeId subScopeId) {
      FieldScopeResult result = elements.get(0).policyFor(rootDescriptor, subScopeId);
      return FieldScopeResult.of(!result.included(), result.recursive());
    }

    @Override
    public String toString() {
      return String.format("!(%s)", elements.get(0));
    }
  }

  static FieldScopeLogic and(FieldScopeLogic fieldScopeLogic1, FieldScopeLogic fieldScopeLogic2) {
    return new IntersectionFieldScopeLogic(fieldScopeLogic1, fieldScopeLogic2);
  }

  static FieldScopeLogic or(FieldScopeLogic fieldScopeLogic1, FieldScopeLogic fieldScopeLogic2) {
    return new UnionFieldScopeLogic(fieldScopeLogic1, fieldScopeLogic2);
  }

  static FieldScopeLogic not(FieldScopeLogic fieldScopeLogic) {
    return new NegationFieldScopeLogic(fieldScopeLogic);
  }
}
