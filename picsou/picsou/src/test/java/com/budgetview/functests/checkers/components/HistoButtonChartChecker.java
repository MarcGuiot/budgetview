package com.budgetview.functests.checkers.components;

import com.budgetview.gui.components.charts.histo.HistoSelectionManager;
import com.budgetview.gui.components.charts.histo.button.HistoButtonBlock;
import com.budgetview.gui.components.charts.histo.button.HistoButtonDataset;
import com.budgetview.gui.components.charts.histo.button.HistoButtonElement;
import junit.framework.Assert;
import org.globsframework.utils.collections.Range;
import org.globsframework.utils.TestUtils;
import org.globsframework.utils.Utils;
import org.uispec4j.Trigger;
import org.uispec4j.Window;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoButtonChartChecker extends HistoChartChecker {
  public HistoButtonChartChecker(Window window, String panelName, String chartName) {
    super(window, panelName, chartName);
    getChart();
  }

  protected HistoButtonDataset getDataset() {
    return super.getDataset(HistoButtonDataset.class);
  }

  public void checkElementNames(String... expectedLabels) {
    TestUtils.assertSetEquals(getBlockLabels(), expectedLabels);
  }

  public void checkNoElementShown() {
    List<String> actual = getBlockLabels();
    if (!actual.isEmpty()) {
      Assert.fail("Unexpected elements displayed: " + actual);
    }
  }

  private List<String> getBlockLabels() {
    List<String> actualNames = new ArrayList<String>();
    for (HistoButtonBlock block : getDataset().getBlocks()) {
      actualNames.add(block.label);
    }
    return actualNames;
  }

  public void checkElementPeriod(String label, int start, int end) {
    HistoButtonElement element = getElement(label);
    Assert.assertEquals(new Range<Integer>(start, end).toString(), new Range<Integer>(element.minId, element.maxId).toString());
  }

  public void checkElementTooltipContains(String label, String tooltipText) {
    String actualTooltip = getElement(label).tooltip;
    Assert.assertTrue("Actual tooltip: " + actualTooltip, actualTooltip.contains(tooltipText));
  }

  private HistoButtonBlock getBlock(String label) {
    for (HistoButtonBlock block : getDataset().getBlocks()) {
      if (Utils.equal(block.label, label)) {
        return block;
      }
    }
    Assert.fail("Block '" + label + "' not found - actual: " + getBlockLabels());
    return null;
  }

  private HistoButtonElement getElement(String label) {
    for (HistoButtonElement element : getDataset().getElements()) {
      if (Utils.equal(element.label, label)) {
        return element;
      }
    }
    Assert.fail("Element '" + label + "' not found - actual: " + getBlockLabels());
    return null;
  }

  public Trigger triggerClick(final String label) {
    return new Trigger() {
      public void run() throws Exception {
        click(label, false);
      }
    };
  }

  public Trigger triggerRightClick(final String label) {
    return new Trigger() {
      public void run() throws Exception {
        click(label, true);
      }
    };
  }

  public void click(String label, boolean rightClick) {
    HistoSelectionManager selectionManager = getChart().getSelectionManager();
    HistoButtonBlock block = getBlock(label);
    selectionManager.updateRollover(block.minIndex, Collections.singleton(block.key), false, rightClick, new Point(0, 0));
    selectionManager.startClick(rightClick, new Point(0,0));
  }
}
