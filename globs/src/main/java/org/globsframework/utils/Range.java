package org.globsframework.utils;

import java.security.InvalidParameterException;

/**
 * Range with lower/upper bounds that can both be null.
 */
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

  public boolean contains(T value) {
    if (value == null) {
      throw new InvalidParameterException("Value should be non null");
    }
    return ((min == null) || (value.compareTo(min) >= 0)) &&
           ((max == null) || (value.compareTo(max) <= 0));

  }

  public boolean after(T value) {
    if (value == null) {
      throw new InvalidParameterException("Value should be non null");
    }
    return (min != null) && (value.compareTo(min) < 0);
  }

  public boolean before(T value) {
    if (value == null) {
      throw new InvalidParameterException("Value should be non null");
    }
    return (max != null) && (value.compareTo(max) > 0);
  }

  public boolean overlaps(Range<T> other) {
    if ((min != null) && (other.max != null) && (other.max.compareTo(min) < 0)) {
      return false;
    }
    if ((max != null) && (other.min != null) && (other.min.compareTo(max) > 0)) {
      return false;
    }
    return true;
  }

  public boolean contains(Range<T> other) {
    if ((min != null) && ((other.min == null) || (other.min.compareTo(min) < 0))) {
      return false;
    }
    if ((max != null) && ((other.max == null) || (other.max.compareTo(max) > 0))) {
      return false;
    }
    return true;

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
    result = 31 * result + max.hashCode();
    return result;
  }
}
