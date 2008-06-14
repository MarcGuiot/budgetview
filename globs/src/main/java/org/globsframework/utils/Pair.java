package org.globsframework.utils;

public class Pair<T, D> {
  private T first;
  private D second;

  public Pair(T first, D second) {
    this.first = first;
    this.second = second;
  }

  public T getFirst() {
    return first;
  }

  public D getSecond() {
    return second;
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final Pair pair = (Pair)o;

    if (!first.equals(pair.first)) {
      return false;
    }
    if (!second.equals(pair.second)) {
      return false;
    }

    return true;
  }

  public int hashCode() {
    int result;
    result = first.hashCode();
    result = 29 * result + second.hashCode();
    return result;
  }
}
