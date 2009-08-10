package org.designup.picsou.functests.checkers;

import org.designup.picsou.gui.components.charts.histo.painters.HistoDiffDataset;
import org.designup.picsou.gui.components.charts.histo.painters.HistoLineDataset;
import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.HistoDataset;
import org.uispec4j.Window;
import org.uispec4j.Panel;
import junit.framework.AssertionFailedError;
import junit.framework.Assert;

public class HistoChecker extends GuiChecker {

  public Window window;

  public HistoChecker(Window window) {
    this.window = window;
  }

  public HistoChecker checkColumnCount(int count) {
    HistoDataset dataset = getDataset(HistoDataset.class);
    Assert.assertEquals(count, dataset.size());
    return this;
  }

  public HistoChecker checkDiffColumn(int index, String label, double reference, double actual) {
    HistoDiffDataset dataset = getDataset(HistoDiffDataset.class);
    Assert.assertEquals(getErrorMessage(index, dataset), label, dataset.getLabel(index));
    Assert.assertEquals(getErrorMessage(index, dataset), reference, dataset.getReferenceValue(index), 0.01);
    Assert.assertEquals(getErrorMessage(index, dataset), actual, dataset.getActualValue(index), 0.01);
    return this;
  }

  public HistoChecker checkLineColumn(int index, String label, double value) {
    HistoLineDataset dataset = getDataset(HistoLineDataset.class);
    Assert.assertEquals(getErrorMessage(index, dataset), label, dataset.getLabel(index));
    Assert.assertEquals(getErrorMessage(index, dataset), value, dataset.getValue(index));
    return this;
  }

  private String getErrorMessage(int index, HistoDataset dataset) {
    return "Error at index: " + index + " - dataset contents:\n" + dataset;
  }

  private <T extends HistoDataset> T getDataset(Class<T> datasetClass) {
    Panel panel = window.getPanel("seriesEvolutionView").getPanel("histoChart");
    HistoChart chart = (HistoChart)panel.getAwtComponent();
    HistoDataset dataset = chart.getCurrentDataset();
    if (!datasetClass.isAssignableFrom(dataset.getClass())) {
      throw new AssertionFailedError("Unexpected dataset type: " + datasetClass.getSimpleName());
    }
    return (T)dataset;
  }

}
