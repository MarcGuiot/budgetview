package org.designup.picsou.gui.components.charts.histo;

public interface HistoDataset {

  public int size();

  public double getMaxPositiveValue();

  public double getMaxNegativeValue();

  int getId(int index);

  public String getLabel(int index);

  public String getSection(int index);

  boolean isSelected(int index);

  boolean containsSections();

  public static final HistoDataset NULL = new HistoDataset() {

    public int getId(int index) {
      return 0;
    }

    public int size() {
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

    public String getSection(int index) {
      return "";
    }

    public boolean isSelected(int index) {
      return false;
    }

    public boolean containsSections() {
      return false;
    }
  };
}
