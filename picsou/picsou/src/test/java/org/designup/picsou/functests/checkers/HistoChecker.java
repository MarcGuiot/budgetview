package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.HistoDataset;
import org.designup.picsou.gui.components.charts.histo.painters.HistoDiffDataset;
import org.designup.picsou.gui.components.charts.histo.painters.HistoLineDataset;
import org.uispec4j.Panel;
import org.uispec4j.Window;
import org.uispec4j.interception.toolkit.Empty;

import java.awt.*;

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

  public HistoChecker checkDiffColumn(int index, String label, String section, double reference, double actual) {
    checkDiffColumn(index, label, section, reference, actual, false);
    return this;
  }

  public HistoChecker checkDiffColumn(int index, String label, String section, double reference, double actual, boolean selected) {
    HistoDiffDataset dataset = getDataset(HistoDiffDataset.class);
    Assert.assertEquals(getErrorMessage(index, dataset), label, dataset.getLabel(index));
    Assert.assertEquals(getErrorMessage(index, dataset), section, dataset.getSection(index));
    Assert.assertEquals(getErrorMessage(index, dataset), reference, dataset.getReferenceValue(index), 0.01);
    Assert.assertEquals(getErrorMessage(index, dataset), actual, dataset.getActualValue(index), 0.01);
    Assert.assertEquals(getErrorMessage(index, dataset), selected, dataset.isSelected(index));
    return this;
  }

  public HistoChecker checkLineColumn(int index, String label, String section, double value, boolean selected) {
    HistoLineDataset dataset = getDataset(HistoLineDataset.class);
    Assert.assertEquals(getErrorMessage(index, dataset), label, dataset.getLabel(index));
    Assert.assertEquals(getErrorMessage(index, dataset), section, dataset.getSection(index));
    Assert.assertEquals(getErrorMessage(index, dataset), value, dataset.getValue(index));
    Assert.assertEquals(getErrorMessage(index, dataset), selected, dataset.isSelected(index));
    return this;
  }

  public HistoChecker checkLineColumn(int index, String label, String section, double value) {
    checkLineColumn(index, label, section, value, false);
    return this;
  }

  public void click(double xPercent) {
    HistoChart chart = getChart();
    chart.paint(Empty.NULL_GRAPHICS_2D);
    Dimension size = chart.getSize();
    chart.mouseMoved((int)((size.width - 50) * xPercent), size.height / 2);
    chart.click();
  }

  private String getErrorMessage(int index, HistoDataset dataset) {
    return "Error at index: " + index + " - dataset contents:\n" + dataset;
  }

  private <T extends HistoDataset> T getDataset(Class<T> datasetClass) {
    HistoChart chart = getChart();
    HistoDataset dataset = chart.getCurrentDataset();
    if (!datasetClass.isAssignableFrom(dataset.getClass())) {
      throw new AssertionFailedError("Unexpected dataset type: " + datasetClass.getSimpleName());
    }
    return (T)dataset;
  }

  private <T extends HistoDataset> HistoChart getChart() {
    Panel panel = window.getPanel("seriesEvolutionView").getPanel("histoChart");
    HistoChart chart = (HistoChart)panel.getAwtComponent();
    return chart;
  }
}
