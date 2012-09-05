/*
 * Copyright (c) 2011 David Saff
 * Copyright (c) 2011 Christian Gruber
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
package org.truth0.subjects;

import org.truth0.FailureStrategy;

import com.google.common.annotations.GwtCompatible;

/**
 * Propositions for String subjects
 *
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
@GwtCompatible
public class StringSubject extends Subject<StringSubject, String> {
  public StringSubject(FailureStrategy failureStrategy, String string) {
    super(failureStrategy, string);
  }

  public void contains(String string) {
    if (getSubject() == null) {
      if (string != null) {
        fail("contains", string);
      }
    } else if (!getSubject().contains(string)) {
      fail("contains", string);
    }
  }

  public void startsWith(String string) {
    if (getSubject() == null) {
      if (string != null) {
        fail("starts with", string);
      }
    } else if (!getSubject().startsWith(string)) {
      fail("starts with", string);
    }
  }

  public void endsWith(String string) {
    if (getSubject() == null) {
      if (string != null) {
        fail("ends with", string);
      }
    } else if (!getSubject().endsWith(string)) {
      fail("ends with", string);
    }
  }

  public static final SubjectFactory<StringSubject, String> STRING =
      new SubjectFactory<StringSubject, String>() {
        @Override public StringSubject getSubject(FailureStrategy fs, String target) {
          return new StringSubject(fs, target);
        }
      };

}
