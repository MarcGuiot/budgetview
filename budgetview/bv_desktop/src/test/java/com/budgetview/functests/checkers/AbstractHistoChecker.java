package com.budgetview.functests.checkers;

import com.budgetview.functests.checkers.components.PopupChecker;
import com.budgetview.desktop.components.charts.histo.HistoChart;
import com.budgetview.shared.gui.histochart.HistoDataset;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.uispec4j.*;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.PopupMenuInterceptor;
import org.uispec4j.interception.toolkit.Empty;

import java.util.Set;
import java.util.TreeSet;

public abstract class AbstractHistoChecker<T extends AbstractHistoChecker> extends GuiChecker {

  protected <T extends HistoDataset> T getDataset(final Class<T> datasetClass) {
    final HistoChart chart = getChart();
    UISpecAssert.assertThat(new Assertion() {
      public void check() {
        HistoDataset dataset = chart.getCurrentDataset();
        if (dataset == HistoDataset.NULL) {
          throw new AssertionFailedError("Current dataset is NULL");
        }
        if (!datasetClass.isAssignableFrom(dataset.getClass())) {
          throw new AssertionFailedError("Unexpected dataset type: " + dataset.getClass());
        }
      }
    });
    return (T) chart.getCurrentDataset();
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
    return (T) this;
  }

  protected abstract String getName();

  public T scroll(int offset) {
    Mouse.wheel(getPanel(), offset);
    return (T) this;
  }

  public void clickColumn(int columnIndex) {
    clickColumn(columnIndex, Key.Modifier.NONE, false);
  }

  private T clickColumn(int columnIndex, Key.Modifier modifier, boolean useRightClick) {
    HistoChart chart = getChart();
    chart.setSize(200, 200);
    chart.paint(Empty.NULL_GRAPHICS_2D);
    int x = chart.getX(columnIndex);
    int y = chart.getSize().height / 2;
    click(chart, x, y, modifier, useRightClick);
    return (T) this;
  }

  public T clickColumnId(int month) {
    HistoChart chart = getChart();
    HistoDataset dataset = chart.getCurrentDataset();
    int columnIndex = dataset.getIndex(month);
    if (columnIndex < 0) {
      Assert.fail("Month " + month + " not found - dataset contents:\n" + dataset);
    }

    clickColumn(columnIndex);
    return (T) this;
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

  public void checkRightClickOptions(int columnIndex, String... options) {
    openRightClickPopup(columnIndex).checkChoices(options);
  }

  public void rightClickAndSelect(int columnIndex, String option) {
    openRightClickPopup(columnIndex).click(option);
  }

  public SeriesEditionDialogChecker rightClickAndEditSeries(final int columnIndex, final String option) {
    PopupChecker popupChecker = openRightClickPopup(columnIndex);
    return SeriesEditionDialogChecker.open(popupChecker.triggerClick(option));
  }

  private PopupChecker openRightClickPopup(final int columnIndex) {
    return new PopupChecker() {
      protected MenuItem openMenu() {
        return PopupMenuInterceptor.run(new Trigger() {
          public void run() throws Exception {
            clickColumn(columnIndex, Key.Modifier.NONE, true);
          }
        });
      }
    };
  }
}
