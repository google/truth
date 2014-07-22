package com.google.common.truth;


public interface Contains {
  /**
   * Attests (with a side-effect failure) that the subject contains the
   * supplied item.
   */
  void contains(Object element);

  /**
   * Attests (with a side-effect failure) that the subject does not contain
   * the supplied item.
   */
  void doesNotContain(Object element);

  /**
   * Attests that the subject contains at least one of the provided objects
   * or fails.
   */
  void containsAnyOf(Object first, Object... rest);

  /**
   * Attests that a Collection contains at least one of the objects contained
   * in the provided collection or fails.
   */
  void containsAnyIn(Iterable<?> expected);

  /**
   * Attests that the subject contains at least all of the provided objects
   * or fails, potentially permitting duplicates in both the subject and the
   * parameters (if the subject even can have duplicates).
   *
   * Callers may optionally chain an {@code inOrder()} call if its expected
   * contents must be contained in the given order.
   */
  Ordered containsAllOf(Object first, Object... rest);

  /**
   * Attests that the subject contains at least all of the provided objects
   * or fails, potentially permitting duplicates in both the subject and the
   * parameters (if the subject even can have duplicates).
   *
   * Callers may optionally chain an {@code inOrder()} call if its expected
   * contents must be contained in the given order.
   */
  Ordered containsAllIn(Iterable<?> expected);

  /**
   * Attests that a subject contains all of the provided objects and
   * only these objects or fails, potentially permitting duplicates
   * in both the subject and the parameters (if the subject even can
   * have duplicates).
   *
   * Callers may optionally chain an {@code inOrder()} call if its expected
   * contents must be contained in the given order.
   */
  Ordered containsOnlyElements(Object first, Object... rest);

  /**
   * Attests that a subject contains all of the provided objects and
   * only these objects or fails, potentially permitting duplicates
   * in both the subject and the parameters (if the subject even can
   * have duplicates).
   *
   * Callers may optionally chain an {@code inOrder()} call if its expected
   * contents must be contained in the given order.
   */
  Ordered containsOnlyElementsIn(Iterable<?> expected);

  /**
   * Attests that a subject contains none of the provided objects
   * or fails, eliding duplicates.
   */
  void containsNoneOf(Object first, Object... rest);

  /**
   * Attests that a Collection contains none of the objects contained
   * in the provided collection or fails, eliding duplicates.
   */
  void containsNoneIn(Iterable<?> excluded);
}
