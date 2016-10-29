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
import static com.google.common.truth.extensions.proto.FieldScopeUtil.join;

import com.google.auto.value.AutoValue;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import java.util.List;

/**
 * Implementation of a {@link FieldScope}. It takes a logic component {@link FieldScopeLogic}, and
 * combines it with a human-readable string for use in test failures.
 *
 * <p>{@link FieldScopeLogic} objects may be reused, cached, or otherwise implement particular
 * efficiencies, whereas the human-readable strings are ad-hoc and may be different for otherwise
 * identical logical implementations. For this reason, and to ensure every {@link FieldScope} gets
 * an appropriate testing string, we separate the logic components from the public interface.
 */
@AutoValue
abstract class FieldScopeImpl extends FieldScope {

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // AutoValue methods.
  //////////////////////////////////////////////////////////////////////////////////////////////////

  private static FieldScope create(
      FieldScopeLogic logic,
      Function<? super Optional<Descriptor>, String> usingCorrespondenceStringFunction) {
    return new AutoValue_FieldScopeImpl(logic, usingCorrespondenceStringFunction);
  }

  @Override
  abstract FieldScopeLogic logic();

  abstract Function<? super Optional<Descriptor>, String> usingCorrespondenceStringFunction();

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Instantiation methods.
  //////////////////////////////////////////////////////////////////////////////////////////////////

  static FieldScope createFromSetFields(Message message) {
    return create(
        FieldScopeLogic.partialScope(message),
        Functions.constant(String.format("FieldScopes.fromSetFields({%s})", message.toString())));
  }

  static FieldScope createFromSetFields(Iterable<? extends Message> messages) {
    if (emptyOrAllNull(messages)) {
      return create(
          FieldScopeLogic.none(),
          Functions.constant(String.format("FieldScopes.fromSetFields(%s)", messages.toString())));
    }

    Optional<Descriptor> optDescriptor = FieldScopeUtil.getSingleDescriptor(messages);
    checkArgument(
        optDescriptor.isPresent(),
        "Cannot create scope from messages with different descriptors: %s",
        getDescriptors(messages));

    Message.Builder builder = null;
    for (Message message : messages) {
      if (message != null) {
        if (builder == null) {
          builder = message.toBuilder();
        } else {
          builder.mergeFrom(message);
        }
      }
    }

    return create(
        FieldScopeLogic.partialScope(builder.build()),
        Functions.constant(String.format("FieldScopes.fromSetFields(%s)", formatList(messages))));
  }

  static FieldScope createIgnoringFields(int... fieldNumbers) {
    return create(
        FieldScopeLogic.all().ignoringFields(fieldNumbers),
        FieldScopeUtil.fieldNumbersFunction("FieldScopes.ignoringFields(%s)", fieldNumbers));
  }

  static FieldScope createIgnoringFieldDescriptors(FieldDescriptor... fieldDescriptors) {
    return create(
        FieldScopeLogic.all().ignoringFieldDescriptors(fieldDescriptors),
        Functions.constant(
            String.format("FieldScopes.ignoringFieldDescriptors(%s)", join(fieldDescriptors))));
  }

  static FieldScope createAllowingFields(int... fieldNumbers) {
    return create(
        FieldScopeLogic.none().allowingFields(fieldNumbers),
        FieldScopeUtil.fieldNumbersFunction("FieldScopes.allowingFields(%s)", fieldNumbers));
  }

  static FieldScope createAllowingFieldDescriptors(FieldDescriptor... fieldDescriptors) {
    return create(
        FieldScopeLogic.none().allowingFieldDescriptors(fieldDescriptors),
        Functions.constant(
            String.format("FieldScopes.allowingFieldDescriptors(%s)", join(fieldDescriptors))));
  }

  private static final FieldScope ALL =
      create(FieldScopeLogic.all(), Functions.constant("FieldScopes.all()"));
  private static final FieldScope NONE =
      create(FieldScopeLogic.none(), Functions.constant("FieldScopes.none()"));

  static FieldScope all() {
    return ALL;
  }

  static FieldScope none() {
    return NONE;
  }

  private static boolean emptyOrAllNull(Iterable<?> objects) {
    for (Object object : objects) {
      if (object != null) {
        return false;
      }
    }
    return true;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Delegation methods.
  //////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  String usingCorrespondenceString(Optional<Descriptor> descriptor) {
    return usingCorrespondenceStringFunction().apply(descriptor);
  }

  @Override
  public final FieldScope ignoringFields(int... fieldNumbers) {
    return create(
        logic().ignoringFields(fieldNumbers),
        addUsingCorrespondenceFieldNumbersString(".ignoringFields(%s)", fieldNumbers));
  }

  @Override
  public final FieldScope ignoringFieldDescriptors(FieldDescriptor... fieldDescriptors) {
    return create(
        logic().ignoringFieldDescriptors(fieldDescriptors),
        addUsingCorrespondenceFieldDescriptorsString(
            ".ignoringFieldDesciptors(%s)", fieldDescriptors));
  }

  @Override
  public final FieldScope allowingFields(int... fieldNumbers) {
    return create(
        logic().allowingFields(fieldNumbers),
        addUsingCorrespondenceFieldNumbersString(".allowingFields(%s)", fieldNumbers));
  }

  @Override
  public final FieldScope allowingFieldDescriptors(FieldDescriptor... fieldDescriptors) {
    return create(
        logic().allowingFieldDescriptors(fieldDescriptors),
        addUsingCorrespondenceFieldDescriptorsString(
            ".allowingFieldDescriptors(%s)", fieldDescriptors));
  }

  private Function<Optional<Descriptor>, String> addUsingCorrespondenceFieldNumbersString(
      String fmt, int... fieldNumbers) {
    return FieldScopeUtil.concat(
        usingCorrespondenceStringFunction(),
        FieldScopeUtil.fieldNumbersFunction(fmt, fieldNumbers));
  }

  private Function<Optional<Descriptor>, String> addUsingCorrespondenceFieldDescriptorsString(
      String fmt, FieldDescriptor... fieldDescriptors) {
    return FieldScopeUtil.concat(
        usingCorrespondenceStringFunction(),
        Functions.constant(String.format(fmt, join(fieldDescriptors))));
  }

  private static Iterable<String> getDescriptors(Iterable<? extends Message> messages) {
    List<String> descriptors = Lists.newArrayList();
    for (Message message : messages) {
      descriptors.add(message == null ? "null" : message.getDescriptorForType().getFullName());
    }
    return descriptors;
  }

  private static String formatList(Iterable<? extends Message> messages) {
    List<String> strings = Lists.newArrayList();
    for (Message message : messages) {
      strings.add(message == null ? "null" : "{" + message + "}");
    }
    return "[" + join(strings) + "]";
  }
}
