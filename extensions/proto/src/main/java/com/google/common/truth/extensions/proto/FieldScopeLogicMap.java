/*
 * Copyright (c) 2018 Google, Inc.
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

import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.Descriptors.Descriptor;

/**
 * An immutable map of {@link FieldScopeLogic} to an arbitrary value type.
 *
 * <p>This is the field-path based analog to {@link com.google.common.collect.RangeMap}. Setting a
 * value {@code v} for a {@link FieldScopeLogic} {@code l} on the {@code FieldScopeLogicMap} means
 * that, for any field path {@code f} contained in {@code l}, the value mapped to {@code f} is
 * {@code v}. This overrides any previous {FieldScopeLogic -> value} pairs/mappings.
 *
 * <p>Unlike {@link com.google.common.collect.RangeMap}, this class does not support analysis of its
 * internals, only {@link #get} operations, and it is not performant. All {@code get} and {@code
 * with} operations are O(N), where N = number of mappings, so this class is intended only for small
 * numbers of entries.
 */
class FieldScopeLogicMap<V> implements FieldScopeLogicContainer<FieldScopeLogicMap<V>> {

  @AutoValue
  abstract static class Entry<V> {
    abstract FieldScopeLogic fieldScopeLogic();

    abstract V value();

    static <V> Entry<V> of(FieldScopeLogic fieldScopeLogic, V value) {
      return new AutoValue_FieldScopeLogicMap_Entry<>(fieldScopeLogic, value);
    }
  }

  private static final FieldScopeLogicMap<Object> EMPTY_INSTANCE =
      new FieldScopeLogicMap<>(ImmutableList.<Entry<Object>>of());

  // Key -> value mappings for this map.  Earlier entries override later ones.
  private final ImmutableList<Entry<V>> entries;

  private FieldScopeLogicMap(Iterable<Entry<V>> entries) {
    this.entries = ImmutableList.copyOf(entries);
  }

  public boolean isEmpty() {
    return entries.isEmpty();
  }

  public Optional<V> get(Descriptor rootDescriptor, SubScopeId subScopeId) {
    // Earlier entries override later ones, so we don't need to iterate backwards.
    for (Entry<V> entry : entries) {
      if (entry.fieldScopeLogic().contains(rootDescriptor, subScopeId)) {
        return Optional.of(entry.value());
      }
    }
    return Optional.absent();
  }

  /** Returns a new immutable map that adds the given fields -> value mapping. */
  public FieldScopeLogicMap<V> with(FieldScopeLogic fieldScopeLogic, V value) {
    ImmutableList.Builder<Entry<V>> newEntries = ImmutableList.builder();
    // Earlier entries override later ones, so we insert the new one at the front of the list.
    newEntries.add(Entry.of(fieldScopeLogic, value));
    newEntries.addAll(entries);
    return new FieldScopeLogicMap<>(newEntries.build());
  }

  @Override
  public FieldScopeLogicMap<V> subScope(Descriptor rootDescriptor, SubScopeId subScopeId) {
    ImmutableList.Builder<Entry<V>> newEntries =
        ImmutableList.builderWithExpectedSize(entries.size());
    for (Entry<V> entry : entries) {
      newEntries.add(
          Entry.of(entry.fieldScopeLogic().subScope(rootDescriptor, subScopeId), entry.value()));
    }
    return new FieldScopeLogicMap<>(newEntries.build());
  }

  @Override
  public void validate(
      Descriptor rootDescriptor, FieldDescriptorValidator fieldDescriptorValidator) {
    for (Entry<V> entry : entries) {
      entry.fieldScopeLogic().validate(rootDescriptor, fieldDescriptorValidator);
    }
  }

  @SuppressWarnings("unchecked") // Implementation is fully variant.
  public static <V> FieldScopeLogicMap<V> empty() {
    return (FieldScopeLogicMap<V>) EMPTY_INSTANCE;
  }

  /** Returns a map which maps all fields to the given value by default. */
  public static <V> FieldScopeLogicMap<V> defaultValue(V value) {
    return new FieldScopeLogicMap<>(ImmutableList.of(Entry.of(FieldScopeLogic.all(), value)));
  }
}
