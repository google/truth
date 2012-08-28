package org.truth0.subjects;

import org.truth0.subjects.Subject.And;

public interface Ordered<Q> extends And<Q> {

  /**
   * An additional assertion, implemented by some containment subjects
   * which allows for a further constraint of orderedness.
   */
  And<Q> inOrder();

}


