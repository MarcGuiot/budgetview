package org.designup.picsou.gui.utils;

public class FloatingAverage {
  private Double values[];
  private int index = 0;

  private FloatingAverage(int shift) {
    values = new Double[shift];
  }

  public static FloatingAverage init(int shift) {
    return new FloatingAverage(shift);
  }

  public void add(Double value) {
    values[index] = value;
    index = ++index % values.length;

  }

  public Double getAverage() {
    Double median = (double) 0;
    int size = 0;
    for (Double value : values) {
      if (value != null) {
        size++;
        median += value;
      }
    }
    if (size == 0) {
      return 0.0;
    }
    return median / size;
  }
}
