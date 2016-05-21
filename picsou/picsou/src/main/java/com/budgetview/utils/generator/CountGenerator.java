package com.budgetview.utils.generator;

public abstract class CountGenerator {
  public abstract int get(int month);

  public static CountGenerator several(final int value) {
    return new CountGenerator() {
      public int get(int month) {
        return value;
      }
    };
  }

  public static CountGenerator sometimes() {
    return new CountGenerator() {
      public int get(int month) {
        return (int) Math.floor(Math.random() * 2);
      }
    };
  }

  public static CountGenerator once() {
    return new CountGenerator() {
      public int get(int month) {
        return 1;
      }
    };
  }

  public static CountGenerator upTo(final int max) {
    return new CountGenerator() {
      public int get(int month) {
        return (int) (Math.round(Math.random() * (double) max));
      }
    };
  }
}
