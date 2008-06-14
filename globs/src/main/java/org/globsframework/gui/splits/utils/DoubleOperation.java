package org.globsframework.gui.splits.utils;

public interface DoubleOperation {
  double get(double value1, double value2);

  static final DoubleOperation SUM = new DoubleOperation() {
    public double get(double value1, double value2) {
      return value1 + value2;
    }
  };

  static final DoubleOperation MAX = new DoubleOperation() {
    public double get(double value1, double value2) {
      return Math.max(value1, value2);
    }
  };
}
