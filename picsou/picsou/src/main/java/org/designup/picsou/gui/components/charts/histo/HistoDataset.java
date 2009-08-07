package org.designup.picsou.gui.components.charts.histo;

public interface HistoDataset {

  public int getSize();

  public double getMaxPositiveValue();

  public double getMaxNegativeValue();

  public String getLabel(int index);

  boolean isSelected(int index);

  public static final HistoDataset NULL = new HistoDataset() {
    public int getSize() {
      return 0;
    }

    public double getMaxPositiveValue() {
      return 0;
    }

    public double getMaxNegativeValue() {
      return 0;
    }

    public String getLabel(int index) {
      return "";
    }

    public boolean isSelected(int index) {
      return false;
    }
  };
}
