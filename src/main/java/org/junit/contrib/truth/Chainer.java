package org.junit.contrib.truth;

public class Chainer<T> {
  
  private final T chainedItem;
  
  public Chainer(T chainedItem) {
    this.chainedItem = chainedItem;
  }
  
  public T and() {
    return chainedItem;
  }

}
