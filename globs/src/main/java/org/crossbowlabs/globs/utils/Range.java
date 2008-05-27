package org.crossbowlabs.globs.utils;

import java.security.InvalidParameterException;

public class Range<T extends Comparable> {
  private T min;
  private T max;

  public Range(T min, T max) throws InvalidParameterException {
    if ((min != null) && (max != null) && (min.compareTo(max) > 0)) {
      throw new InvalidParameterException("Min value '" + min + "' should be less than max value '" + max + "'");
    }
    this.min = min;
    this.max = max;

  }

  public T getMin() {
    return min;
  }

  public T getMax() {
    return max;
  }

  public String toString() {
    return "[" + min + ".." + max + "]";
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final Range pair = (Range)o;

    if (!min.equals(pair.min)) {
      return false;
    }
    if (!max.equals(pair.max)) {
      return false;
    }

    return true;
  }

  public int hashCode() {
    int result;
    result = min.hashCode();
    result = 29 * result + max.hashCode();
    return result;
  }
}
