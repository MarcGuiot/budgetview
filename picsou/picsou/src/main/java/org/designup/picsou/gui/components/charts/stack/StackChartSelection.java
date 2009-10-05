package org.designup.picsou.gui.components.charts.stack;

import javax.swing.*;

public class StackChartSelection {
  public final StackChartDataset dataset;
  public final int datasetIndex;

  public StackChartSelection(StackChartDataset dataset, int datasetIndex) {
    this.dataset = dataset;
    this.datasetIndex = datasetIndex;
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    StackChartSelection other = (StackChartSelection)o;
    return (datasetIndex == other.datasetIndex) && dataset.equals(other.dataset);
  }

  public int hashCode() {
    int result = dataset.hashCode();
    result = 31 * result + datasetIndex;
    return result;
  }

  public Action getAction() {
    return dataset.getAction(datasetIndex);
  }

  public String toString() {
    return datasetIndex + ":" + dataset.getLabel(datasetIndex);
  }
}