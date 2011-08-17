package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.HistoDataset;
import org.designup.picsou.gui.components.charts.histo.daily.HistoDailyDataset;
import org.uispec4j.Mouse;
import org.uispec4j.Panel;
import org.uispec4j.interception.toolkit.Empty;

import java.awt.*;
import java.util.Set;
import java.util.TreeSet;

public abstract class AbstractHistoChecker<T extends AbstractHistoChecker> extends GuiChecker {

  protected <T extends HistoDataset> T getDataset(Class<T> datasetClass) {
    HistoChart chart = getChart();
    HistoDataset dataset = chart.getCurrentDataset();
    if (!datasetClass.isAssignableFrom(dataset.getClass())) {
      throw new AssertionFailedError("Unexpected dataset type: " + dataset.getClass().getSimpleName());
    }
    return (T)dataset;
  }

  protected HistoDataset getDataset() {
    return getDataset(HistoDataset.class);
  }

  protected abstract HistoChart getChart();

  protected abstract Panel getPanel();

  public T checkRange(int firstMonth, int lastMonth) {
    HistoDataset dataset = getDataset();
    int actualFirst = dataset.getId(0);
    int actualLast = dataset.getId(dataset.size() - 1);
    if ((actualFirst != firstMonth) || (actualLast != lastMonth)) {
      Assert.fail("expected: [" + firstMonth + "," + lastMonth + "] but was: [" + actualFirst + "," + actualLast + "] in " + getName());
    }
    return (T)this;
  }

  protected abstract String getName();

  public T scroll(int offset) {
    Mouse.wheel(getPanel(), offset);
    return (T)this;
  }

  public void clickColumn(int columnIndex) {
    HistoChart chart = getChart();
    chart.setSize(200,200);
    chart.paint(Empty.NULL_GRAPHICS_2D);
    int x = chart.getX(columnIndex);
    doClick(chart, x);
  }

  public void clickColumnId(int month) {
    HistoChart chart = getChart();
    HistoDataset dataset = chart.getCurrentDataset();
    int columnIndex = dataset.getIndex(month);
    if (columnIndex < 0) {
      Assert.fail("Month " + month + " not found - dataset contents:\n" + dataset);
    }

    clickColumn(columnIndex);
  }

  public void clickAllColumns() {
    HistoChart chart = getChart();
    chart.paint(Empty.NULL_GRAPHICS_2D);
    int y = chart.getSize().height / 2;

    int x0 = chart.getX(0);
    Mouse.enter(chart, x0, y);
    Mouse.move(chart, x0, y);
    Mouse.pressed(chart, x0, y);

    chart.paint(Empty.NULL_GRAPHICS_2D);
    int x1 = chart.getX(chart.getCurrentDataset().size() - 1);
    Mouse.drag(chart, x1, y);
    Mouse.released(chart, x1, y);
    Mouse.exit(chart, x1, y);
  }

  private void doClick(HistoChart chart, int x) {
    int y = chart.getSize().height / 2;

    Mouse.enter(chart, x, y);
    Mouse.move(chart, x, y);
    Mouse.pressed(chart, x, y);
    Mouse.released(chart, x, y);
    Mouse.exit(chart, x, y);
  }

  public void checkSelectedIds(Integer... ids) {
    HistoChart chart = getChart();
    HistoDataset dataset = chart.getCurrentDataset();

    Set<Integer> selection = new TreeSet<Integer>();
    for (int index = 0; index < dataset.size(); index++) {
      if (dataset.isSelected(index)) {
        selection.add(dataset.getId(index));
      }
    }

    org.globsframework.utils.TestUtils.assertSetEquals(selection, ids);
  }

}
