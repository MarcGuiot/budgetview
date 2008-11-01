package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;
import org.designup.picsou.gui.components.BalanceGraph;
import org.designup.picsou.gui.description.Formatting;
import org.globsframework.utils.exceptions.ItemNotFound;
import junit.framework.Assert;

public class BalanceGraphChecker extends DataChecker {
  private BalanceGraph graph;
  private Window window;

  public BalanceGraphChecker(BalanceGraph graph) {
    this.graph = graph;
  }

  public BalanceGraphChecker(Window window) {
    this.window = window;
  }

  public void checkHidden() {
    checkComponentVisible(window, BalanceGraph.class, "totalBalance", false);
  }

  public void checkBalance(double receivedPercent, double spentPercent) {
    initGraph();
    Assert.assertTrue(graph.isVisible());
    Assert.assertEquals(receivedPercent, graph.getIncomePercent());
    Assert.assertEquals(spentPercent, graph.getExpensesPercent());
  }

  public void checkTooltip(double income, double expenses) {
    initGraph();
    Assert.assertTrue(graph.isVisible());
    String tooltip = graph.getToolTipText();
    Assert.assertTrue(tooltip, tooltip.contains("Income: " + Formatting.toString(income)));
    Assert.assertTrue(tooltip, tooltip.contains("Expenses: " + Formatting.toString(expenses)));
  }

  private void initGraph() {
    if (graph == null) {
      checkComponentVisible(window, BalanceGraph.class, "totalBalance", true);
      graph = window.findSwingComponent(BalanceGraph.class);
      if (graph == null) {
        throw new ItemNotFound("Unable to find balanceGraph component");
      }
    }
  }
}
