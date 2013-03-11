package com.budgetview.android.checkers.utils;

import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import java.util.ArrayDeque;
import java.util.Deque;

public class ExpectationQueue {
  private Deque<Expectation> expectations = new ArrayDeque<Expectation>();

  public void push(Expectation expectation) {
    expectations.addLast(expectation);
  }

  public <T extends Expectation> T pop(Class<T> expectedClass) {
    if (expectations.isEmpty()) {
      throw new UnexpectedApplicationState("No expectations in queue - cannot process: " + expectedClass.getSimpleName());
    }
    Expectation expectation = expectations.removeFirst();
    if (!expectedClass.isInstance(expectation)) {
      throw new UnexpectedApplicationState("Unexpected call: " + expectedClass + " - actual queue:\n" + toString());
    }
    return (T)expectation;
  }

  public boolean isEmpty() {
    return expectations.isEmpty();
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    int index = 0;
    for (Expectation expectation : expectations) {
      builder.append("  ")
        .append(index++)
        .append(". ")
        .append(expectation.getClass().getSimpleName());
    }
    return builder.toString();
  }
}
