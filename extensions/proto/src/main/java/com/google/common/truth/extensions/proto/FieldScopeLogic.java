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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.truth.extensions.proto.ProtoTruthMessageDifferencer.ShouldIgnore;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
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
abstract class FieldScopeLogic {

  /** Returns whether comparison should be ignored for the specified field. */
  abstract ShouldIgnore shouldIgnore(
      Descriptor rootDescriptor, FieldDescriptorOrUnknown fieldDescriptorOrUnknown);

  /**
   * Returns a {@code FieldScopeLogic} to handle the message pointed to by this descriptor.
   *
   * <p>Returns {@code this} by default. Subclasses with different behavior must override this
   * method to return something else and {@code isRecursive} to return false.
   */
  FieldScopeLogic subLogic(
      Descriptor rootDescriptor, FieldDescriptorOrUnknown fieldDescriptorOrUnknown) {
    return this;
  }

  /**
   * Returns whether or {@code subLogic} always returns the invoking instance, or an instance with
   * equivalent behavior.
   *
   * <p>Set to return true by default. Used for optimizing compound logics.
   */
  boolean isRecursive() {
    return true;
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

  /**
   * Performs any validation that requires a Descriptor to validate against.
   *
   * @throws IllegalArgumentException if invalid input was provided
   */
  void validate(Descriptor descriptor) {}

  private static boolean isEmpty(Iterable<?> container) {
    boolean isEmpty = true;
    for (Object element : container) {
      checkNotNull(element);
      isEmpty = false;
    }

    return isEmpty;
  }

  FieldScopeLogic ignoringFields(Iterable<Integer> fieldNumbers) {
    if (isEmpty(fieldNumbers)) {
      return this;
    }
    return and(this, new NegationFieldScopeLogic(new FieldNumbersLogic(fieldNumbers)));
  }

  FieldScopeLogic ignoringFieldDescriptors(Iterable<FieldDescriptor> fieldDescriptors) {
    if (isEmpty(fieldDescriptors)) {
      return this;
    }
    return and(this, new NegationFieldScopeLogic(new FieldDescriptorsLogic(fieldDescriptors)));
  }

  FieldScopeLogic allowingFields(Iterable<Integer> fieldNumbers) {
    if (isEmpty(fieldNumbers)) {
      return this;
    }
    return or(this, new FieldNumbersLogic(fieldNumbers));
  }

  FieldScopeLogic allowingFieldDescriptors(Iterable<FieldDescriptor> fieldDescriptors) {
    if (isEmpty(fieldDescriptors)) {
      return this;
    }
    return or(this, new FieldDescriptorsLogic(fieldDescriptors));
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // CONCRETE SUBTYPES
  //////////////////////////////////////////////////////////////////////////////////////////////////

  private static final FieldScopeLogic ALL =
      new FieldScopeLogic() {
        @Override
        public String toString() {
          return "FieldScopes.all()";
        }

        @Override
        final ShouldIgnore shouldIgnore(
            Descriptor rootDescriptor, FieldDescriptorOrUnknown fieldDescriptorOrUnknown) {
          return ShouldIgnore.NO;
        }
      };

  private static final FieldScopeLogic NONE =
      new FieldScopeLogic() {
        @Override
        public String toString() {
          return "FieldScopes.none()";
        }

        @Override
        final ShouldIgnore shouldIgnore(
            Descriptor rootDescriptor, FieldDescriptorOrUnknown fieldDescriptorOrUnknown) {
          return ShouldIgnore.YES;
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
    final ShouldIgnore shouldIgnore(
        Descriptor rootDescriptor, FieldDescriptorOrUnknown fieldDescriptorOrUnknown) {
      return fieldNumberTree.hasChild(fieldDescriptorOrUnknown)
          ? ShouldIgnore.NO
          : ShouldIgnore.YES;
    }

    @Override
    final FieldScopeLogic subLogic(
        Descriptor rootDescriptor, FieldDescriptorOrUnknown fieldDescriptorOrUnknown) {
      return newPartialScopeLogic(fieldNumberTree.child(fieldDescriptorOrUnknown));
    }

    @Override
    boolean isRecursive() {
      return fieldNumberTree.isEmpty();
    }

    private static PartialScopeLogic newPartialScopeLogic(FieldNumberTree fieldNumberTree) {
      return fieldNumberTree.isEmpty() ? EMPTY : new PartialScopeLogic(fieldNumberTree);
    }
  }

  private static final class RootPartialScopeLogic extends PartialScopeLogic {
    private final Message message;
    private final Descriptor expectedDescriptor;

    RootPartialScopeLogic(Message message) {
      super(FieldNumberTree.fromMessage(message));
      this.message = message;
      this.expectedDescriptor = message.getDescriptorForType();
    }

    @Override
    void validate(Descriptor descriptor) {
      Preconditions.checkArgument(
          expectedDescriptor.equals(descriptor),
          "Message given to FieldScopes.fromSetFields() does not have the same descriptor as the "
              + "message being tested. Expected %s, got %s.",
          expectedDescriptor.getFullName(),
          descriptor.getFullName());
    }

    @Override
    public String toString() {
      return String.format("FieldScopes.fromSetFields(%s)", message);
    }
  }

  static FieldScopeLogic partialScope(Message message) {
    return new RootPartialScopeLogic(message);
  }

  // TODO(user): Performance: Optimize FieldNumbersLogic and FieldDescriptorsLogic for
  // adding / ignoring field numbers and descriptors, respectively, to eliminate high recursion
  // costs for long chains of allows/ignores.

  // Common functionality for FieldNumbersLogic and FieldDescriptorsLogic.
  private abstract static class FieldMatcherLogicBase extends FieldScopeLogic {

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
    final ShouldIgnore shouldIgnore(
        Descriptor rootDescriptor, FieldDescriptorOrUnknown fieldDescriptorOrUnknown) {
      if (fieldDescriptorOrUnknown.unknownFieldDescriptor().isPresent()) {
        return ShouldIgnore.YES;
      }

      FieldDescriptor fieldDescriptor = fieldDescriptorOrUnknown.fieldDescriptor().get();
      if (matchesFieldDescriptor(rootDescriptor, fieldDescriptor)) {
        return ShouldIgnore.NO;
      }

      // We return 'MAYBE' for both field descriptor scopes and field number scopes.
      // In the former case, the field descriptors are arbitrary, so it's always possible we find a
      // hit on a sub-message somewhere.  In the latter case, the message definition may be cyclic,
      // so we need to return 'MAYBE' even if the top level field number doesn't match.
      return fieldDescriptor.getJavaType() == JavaType.MESSAGE
          ? ShouldIgnore.MAYBE
          : ShouldIgnore.YES;
    }

    @Override
    final boolean isRecursive() {
      return false;
    }

    @Override
    final FieldScopeLogic subLogic(
        Descriptor rootDescriptor, FieldDescriptorOrUnknown fieldDescriptorOrUnknown) {
      ShouldIgnore shouldIgnore = shouldIgnore(rootDescriptor, fieldDescriptorOrUnknown);
      switch (shouldIgnore) {
        case YES:
          return none();
        case NO:
          return all();
        case MAYBE:
          return this;
      }
      throw new AssertionError("Unexpected enum: " + shouldIgnore);
    }
  }

  // Matches any specific fields which fall under a sub-message field (or root) matching the root
  // message type and one of the specified field numbers.
  private static final class FieldNumbersLogic extends FieldMatcherLogicBase {
    private final ImmutableSet<Integer> fieldNumbers;

    FieldNumbersLogic(Iterable<Integer> fieldNumbers) {
      this.fieldNumbers = ImmutableSet.copyOf(fieldNumbers);
    }

    @Override
    void validate(Descriptor descriptor) {
      super.validate(descriptor);
      for (int fieldNumber : fieldNumbers) {
        checkArgument(
            descriptor.findFieldByNumber(fieldNumber) != null,
            "Message type %s has no field with number %s.",
            descriptor.getFullName(),
            fieldNumber);
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

    FieldDescriptorsLogic(Iterable<FieldDescriptor> fieldDescriptors) {
      this.fieldDescriptors = ImmutableSet.copyOf(fieldDescriptors);
    }

    @Override
    boolean matchesFieldDescriptor(Descriptor descriptor, FieldDescriptor fieldDescriptor) {
      return fieldDescriptors.contains(fieldDescriptor);
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
    final boolean isRecursive;

    CompoundFieldScopeLogic(FieldScopeLogic singleElem) {
      this.elements = ImmutableList.of(singleElem);
      this.isRecursive = areAllRecursive(elements);
    }

    CompoundFieldScopeLogic(FieldScopeLogic firstElem, FieldScopeLogic secondElem) {
      this.elements = ImmutableList.of(firstElem, secondElem);
      this.isRecursive = areAllRecursive(elements);
    }

    private static boolean areAllRecursive(ImmutableList<FieldScopeLogic> elements) {
      for (FieldScopeLogic elem : elements) {
        if (!elem.isRecursive()) {
          return false;
        }
      }
      return true;
    }

    @Override
    final void validate(Descriptor descriptor) {
      for (FieldScopeLogic elem : elements) {
        elem.validate(descriptor);
      }
    }

    /** Helper to produce a new {@code CompoundFieldScopeLogic} of the same type as the subclass. */
    abstract T newLogicOfSameType(List<FieldScopeLogic> newElements);

    @Override
    final FieldScopeLogic subLogic(
        Descriptor rootDescriptor, FieldDescriptorOrUnknown fieldDescriptorOrUnknown) {
      if (isRecursive()) {
        return this;
      }

      ImmutableList.Builder<FieldScopeLogic> builder =
          ImmutableList.builderWithExpectedSize(elements.size());
      for (FieldScopeLogic elem : elements) {
        builder.add(elem.subLogic(rootDescriptor, fieldDescriptorOrUnknown));
      }
      return newLogicOfSameType(builder.build());
    }

    @Override
    final boolean isRecursive() {
      return isRecursive;
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
    ShouldIgnore shouldIgnore(
        Descriptor rootDescriptor, FieldDescriptorOrUnknown fieldDescriptorOrUnknown) {
      // The intersection of two scopes is ignorable if either scope is itself ignorable.
      return or(
          elements.get(0).shouldIgnore(rootDescriptor, fieldDescriptorOrUnknown),
          elements.get(1).shouldIgnore(rootDescriptor, fieldDescriptorOrUnknown));
    }

    // Tri-state-boolean 'OR', where YES = true, NO = false, and MAYBE = unknown.
    private static ShouldIgnore or(ShouldIgnore op1, ShouldIgnore op2) {
      if (op1 == ShouldIgnore.YES || op2 == ShouldIgnore.YES) {
        return ShouldIgnore.YES;
      } else if (op1 == ShouldIgnore.NO && op2 == ShouldIgnore.NO) {
        return ShouldIgnore.NO;
      } else {
        return ShouldIgnore.MAYBE;
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
    ShouldIgnore shouldIgnore(
        Descriptor rootDescriptor, FieldDescriptorOrUnknown fieldDescriptorOrUnknown) {
      // The union of two scopes is ignorable only if both scopes are themselves ignorable.
      return and(
          elements.get(0).shouldIgnore(rootDescriptor, fieldDescriptorOrUnknown),
          elements.get(1).shouldIgnore(rootDescriptor, fieldDescriptorOrUnknown));
    }

    // Tri-state-boolean 'AND', where YES = true, NO = false, and MAYBE = unknown.
    private static ShouldIgnore and(ShouldIgnore op1, ShouldIgnore op2) {
      if (op1 == ShouldIgnore.NO || op2 == ShouldIgnore.NO) {
        return ShouldIgnore.NO;
      } else if (op1 == ShouldIgnore.YES && op2 == ShouldIgnore.YES) {
        return ShouldIgnore.YES;
      } else {
        return ShouldIgnore.MAYBE;
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
    ShouldIgnore shouldIgnore(
        Descriptor rootDescriptor, FieldDescriptorOrUnknown fieldDescriptorOrUnknown) {
      ShouldIgnore op = elements.get(0).shouldIgnore(rootDescriptor, fieldDescriptorOrUnknown);
      switch (op) {
        case YES:
          return ShouldIgnore.NO;
        case NO:
          return ShouldIgnore.YES;
        case MAYBE:
          return ShouldIgnore.MAYBE;
      }
      throw new AssertionError("Impossible: " + op);
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
