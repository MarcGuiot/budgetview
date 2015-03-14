package org.designup.picsou.utils.generator;

public abstract class AmountGenerator {
  public abstract double get();

  private AmountGenerator() {
  }

  public static AmountGenerator fixedValue(final double value) {
    return new AmountGenerator() {
      public double get() {
        return value;
      }
    };
  }

  public static AmountGenerator anyOf(final double... values) {
    return new AmountGenerator() {
      public double get() {
        int index = (int) Math.floor(Math.random() * values.length);
        return values[index];
      }
    };
  }

  public static AmountGenerator between(final double min, final double max) {
    return new AmountGenerator() {
      public double get() {
        double value = min + (max - min) * Math.random();
        return ((double) Math.round(100 * value)) / 100;
      }
    };
  }
}
