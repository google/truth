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

import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.truth.extensions.proto.MessageDifferencer.IgnoreCriteria;
import com.google.common.truth.extensions.proto.MessageDifferencer.SpecificField;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

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

  private final Set<Descriptor> validatedDescriptors = Sets.newConcurrentHashSet();

  final IgnoreCriteria toIgnoreCriteria(final Descriptor descriptor) {
    if (!validatedDescriptors.contains(descriptor)) {
      validate(descriptor);
      validatedDescriptors.add(descriptor);
    }

    final Cache cache = new Cache();
    return new IgnoreCriteria() {
      @Override
      public boolean isIgnored(
          Message message1,
          Message message2,
          @Nullable FieldDescriptor fieldDescriptor,
          List<SpecificField> fieldPath) {
        ImmutableList.Builder<Message> subMessages = ImmutableList.builder();
        if (fieldDescriptor != null) {
          addSubMessages(fieldDescriptor, message1, subMessages);
          addSubMessages(fieldDescriptor, message2, subMessages);
        }

        Context context = Context.create(descriptor, fieldPath, fieldDescriptor, subMessages);
        cache.clearMethodCaches();
        return !matchesFieldPath(context, cache) && matchStateAppliesForAllSubPaths(context, cache);
      }
    };
  }

  private static void addSubMessages(
      FieldDescriptor fieldDescriptor, Message message, ImmutableList.Builder<Message> builder) {
    if (fieldDescriptor.getJavaType() != FieldDescriptor.JavaType.MESSAGE) {
      return;
    }

    if (fieldDescriptor.isRepeated()) {
      for (int i = 0; i < message.getRepeatedFieldCount(fieldDescriptor); i++) {
        builder.add((Message) message.getRepeatedField(fieldDescriptor, i));
      }
    } else {
      builder.add((Message) message.getField(fieldDescriptor));
    }
  }

  // A temporary cache for repeat processing of messages in a single MessageDifferencer run.
  // Cache data must be temporary because messages may be mutable, and change between
  // MessageDifferencer runs, which invalidates them as keys and invalidates the results.
  private static final class Cache {
    // Messages do not change between context changes, so this map is scoped to the life of the
    // difference operation, which is the life of the Cache object.
    private final Map<FieldMatcherLogicBase, Map<Message, Boolean>> messagesWithMatchingField =
        Maps.newHashMap();

    // These are scoped to the life of the Context object, and must be cleared periodically.
    private final Map<FieldScopeLogic, Boolean> matchesFieldPath = Maps.newHashMap();
    private final Map<FieldScopeLogic, Boolean> matchStateAppliesForAllSubPaths = Maps.newHashMap();

    public Map<Message, Boolean> getMessagesWithMatchingField(FieldMatcherLogicBase key) {
      Map<Message, Boolean> map = messagesWithMatchingField.get(key);
      if (map == null) {
        map = Maps.newHashMap();
        messagesWithMatchingField.put(key, map);
      }
      return map;
    }

    public boolean matchesFieldPath(FieldScopeLogic logic, Context context) {
      @Nullable Boolean match = matchesFieldPath.get(logic);
      if (match == null) {
        match = logic.doMatchesFieldPath(context, this);
        matchesFieldPath.put(logic, match);
      }
      return match;
    }

    public boolean matchStateAppliesForAllSubPaths(FieldScopeLogic logic, Context context) {
      @Nullable Boolean matchAppliesForAll = matchStateAppliesForAllSubPaths.get(logic);
      if (matchAppliesForAll == null) {
        matchAppliesForAll = logic.doMatchStateAppliesForAllSubPaths(context, this);
        matchStateAppliesForAllSubPaths.put(logic, matchAppliesForAll);
      }
      return matchAppliesForAll;
    }

    public void clearMethodCaches() {
      matchesFieldPath.clear();
      matchStateAppliesForAllSubPaths.clear();
    }
  }

  @AutoValue
  abstract static class Context {

    /** The Message Descriptor for the message being tested. */
    abstract Descriptor descriptor();

    /**
     * The specific field path leading up to the message containing {@code field()}. If {@code
     * field()} is absent, the last element of fieldPath() will describe an unknown field instead.
     */
    abstract List<SpecificField> fieldPath();

    /**
     * The field on the message being inspected, for which we must now determine if it should be
     * inspected deeply or not.
     */
    abstract Optional<FieldDescriptor> field();

    /**
     * The message objects at the end of the field path and field descriptor, or empty if there are
     * none. MessageDifferencer will omit entire sections of the proto tree if told to ignore root
     * messages, so we may need to inspect the contents of the message first to decide if it should
     * be ignored.
     */
    abstract ImmutableList<Message> messageFields();

    static Context create(
        Descriptor descriptor,
        List<SpecificField> fieldPath,
        @Nullable FieldDescriptor field,
        ImmutableList.Builder<Message> messageFields) {
      return new AutoValue_FieldScopeLogic_Context(
          descriptor, fieldPath, Optional.fromNullable(field), messageFields.build());
    }
  }

  /**
   * Whether or not this implementation includes the specified specific field path.
   *
   * <p>Unlike {@link #doMatchesFieldPath}, this method does caching, and so is performant to call
   * repeatedly. Clients should call this method, but override the do method.
   */
  final boolean matchesFieldPath(Context context, Cache cache) {
    return cache.matchesFieldPath(this, context);
  }

  /**
   * Whether or not this implementation's answer to {@code matchesFieldPath} is fixed for all sub
   * paths of the current specific field path. If fixed, it may be possible to ignore entire
   * sub-trees of the protocol buffer for diff inspection.
   *
   * <p>Returns true by default, since most {@code FieldScopeLogics} include from the root and don't
   * exclude subtrees.
   *
   * <p>Unlike {@link #doMatchStateAppliesForAllSubPaths}, this method does caching, and so is
   * performant to call repeatedly. Clients should call this method, but override the do method.
   */
  final boolean matchStateAppliesForAllSubPaths(Context context, Cache cache) {
    return cache.matchStateAppliesForAllSubPaths(this, context);
  }

  /** Whether or not this implementation includes the specified specific field path. */
  abstract boolean doMatchesFieldPath(Context context, Cache cache);

  /**
   * Whether or not this implementation's answer to {@code matchesFieldPath} is fixed for all sub
   * paths of the current specific field path. If fixed, it may be possible to ignore entire
   * sub-trees of the protocol buffer for diff inspection.
   *
   * <p>Returns true by default, since most {@code FieldScopeLogic}s include from the root and don't
   * exclude subtrees.
   */
  boolean doMatchStateAppliesForAllSubPaths(Context context, Cache cache) {
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
        boolean doMatchesFieldPath(Context context, Cache cache) {
          return true;
        }

        @Override
        public String toString() {
          return "FieldScopes.all()";
        }
      };

  private static final FieldScopeLogic NONE =
      new FieldScopeLogic() {
        @Override
        boolean doMatchesFieldPath(Context context, Cache cache) {
          return false;
        }

        @Override
        public String toString() {
          return "FieldScopes.none()";
        }
      };

  static FieldScopeLogic all() {
    return ALL;
  }

  static FieldScopeLogic none() {
    return NONE;
  }

  private static final class PartialScopeLogic extends FieldScopeLogic {
    private final Message message;
    private final FieldNumberTree fieldNumberTree;
    private final Descriptor expectedDescriptor;

    PartialScopeLogic(Message message) {
      this.message = message;
      this.fieldNumberTree = FieldNumberTree.fromMessage(message);
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
    boolean doMatchesFieldPath(Context context, Cache cache) {
      return fieldNumberTree.matches(context.fieldPath(), context.field());
    }

    @Override
    public String toString() {
      return String.format("FieldScopes.fromSetFields(%s)", message);
    }
  }

  static FieldScopeLogic partialScope(Message message) {
    return new PartialScopeLogic(message);
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
    final boolean doMatchesFieldPath(Context context, Cache cache) {
      if (context.field().isPresent()
          && matchesFieldDescriptor(context.descriptor(), context.field().get())) {
        return true;
      }

      for (SpecificField field : context.fieldPath()) {
        FieldDescriptor specificFieldDescriptor = field.getField();
        if (specificFieldDescriptor != null
            && matchesFieldDescriptor(context.descriptor(), specificFieldDescriptor)) {
          return true;
        }
      }

      return false;
    }

    @Override
    final boolean doMatchStateAppliesForAllSubPaths(Context context, Cache cache) {
      // Match is fixed if we match currently, or no sub paths can match.
      if (!matchesFieldPath(context, cache)) {
        for (Message message : context.messageFields()) {
          if (messageHasMatchingField(context, cache, message)) {
            return false;
          }
        }
      }
      return true;
    }

    private boolean messageHasMatchingField(Context context, Cache cache, Message message) {
      Map<Message, Boolean> messagesWithMatchingField = cache.getMessagesWithMatchingField(this);
      if (messagesWithMatchingField.containsKey(message)) {
        return messagesWithMatchingField.get(message);
      }

      boolean result = false;
      Map<FieldDescriptor, Object> fields = message.getAllFields();
      for (FieldDescriptor key : fields.keySet()) {
        if (matchesFieldDescriptor(context.descriptor(), key)) {
          result = true;
        } else if (key.getJavaType() == FieldDescriptor.JavaType.MESSAGE) {
          if (key.isRepeated()) {
            for (int i = 0; i < message.getRepeatedFieldCount(key); i++) {
              if (messageHasMatchingField(
                  context, cache, (Message) message.getRepeatedField(key, i))) {
                result = true;
                break;
              }
            }
          } else if (messageHasMatchingField(context, cache, (Message) message.getField(key))) {
            result = true;
          }
        }

        if (result) {
          break;
        }
      }

      messagesWithMatchingField.put(message, result);
      return result;
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

  private abstract static class CompoundFieldScopeLogic extends FieldScopeLogic {
    final ImmutableList<FieldScopeLogic> elements;

    CompoundFieldScopeLogic(FieldScopeLogic singleElem) {
      elements = ImmutableList.of(singleElem);
    }

    CompoundFieldScopeLogic(FieldScopeLogic firstElem, FieldScopeLogic secondElem) {
      elements = ImmutableList.of(firstElem, secondElem);
    }

    @Override
    final void validate(Descriptor descriptor) {
      for (FieldScopeLogic elem : elements) {
        elem.validate(descriptor);
      }
    }
  }

  private static final class IntersectionFieldScopeLogic extends CompoundFieldScopeLogic {
    IntersectionFieldScopeLogic(FieldScopeLogic subject1, FieldScopeLogic subject2) {
      super(subject1, subject2);
    }

    @Override
    boolean doMatchStateAppliesForAllSubPaths(Context context, Cache cache) {
      if (matchesFieldPath(context, cache)) {
        // We are fixed as true only if both operands are fixed.
        return elements.get(0).matchStateAppliesForAllSubPaths(context, cache)
            && elements.get(1).matchStateAppliesForAllSubPaths(context, cache);
      } else {
        // We are fixed as false only if at least one operand is fixed false.
        boolean firstFixedFalse =
            !elements.get(0).matchesFieldPath(context, cache)
                && elements.get(0).matchStateAppliesForAllSubPaths(context, cache);
        boolean secondFixedFalse =
            !elements.get(1).matchesFieldPath(context, cache)
                && elements.get(1).matchStateAppliesForAllSubPaths(context, cache);
        return firstFixedFalse || secondFixedFalse;
      }
    }

    @Override
    boolean doMatchesFieldPath(Context context, Cache cache) {
      return elements.get(0).matchesFieldPath(context, cache)
          && elements.get(1).matchesFieldPath(context, cache);
    }

    @Override
    public String toString() {
      return String.format("(%s && %s)", elements.get(0), elements.get(1));
    }
  }

  private static final class UnionFieldScopeLogic extends CompoundFieldScopeLogic {
    UnionFieldScopeLogic(FieldScopeLogic subject1, FieldScopeLogic subject2) {
      super(subject1, subject2);
    }

    @Override
    boolean doMatchStateAppliesForAllSubPaths(Context context, Cache cache) {
      if (matchesFieldPath(context, cache)) {
        // We are fixed as true only if either field is fixed true.
        boolean firstFixedTrue =
            elements.get(0).matchesFieldPath(context, cache)
                && elements.get(0).matchStateAppliesForAllSubPaths(context, cache);
        boolean secondFixedTrue =
            elements.get(1).matchesFieldPath(context, cache)
                && elements.get(1).matchStateAppliesForAllSubPaths(context, cache);
        return firstFixedTrue || secondFixedTrue;
      } else {
        // We are fixed false only if both operands are fixed false.
        return elements.get(0).matchStateAppliesForAllSubPaths(context, cache)
            && elements.get(1).matchStateAppliesForAllSubPaths(context, cache);
      }
    }

    @Override
    boolean doMatchesFieldPath(Context context, Cache cache) {
      return elements.get(0).matchesFieldPath(context, cache)
          || elements.get(1).matchesFieldPath(context, cache);
    }

    @Override
    public String toString() {
      return String.format("(%s || %s)", elements.get(0), elements.get(1));
    }
  }

  private static final class NegationFieldScopeLogic extends CompoundFieldScopeLogic {
    NegationFieldScopeLogic(FieldScopeLogic subject) {
      super(subject);
    }

    @Override
    boolean doMatchStateAppliesForAllSubPaths(Context context, Cache cache) {
      // We are fixed only if the operand is fixed.
      return elements.get(0).matchStateAppliesForAllSubPaths(context, cache);
    }

    @Override
    boolean doMatchesFieldPath(Context context, Cache cache) {
      return !elements.get(0).matchesFieldPath(context, cache);
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
