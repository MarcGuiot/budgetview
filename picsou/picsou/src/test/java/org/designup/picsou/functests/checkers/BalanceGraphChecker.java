package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;
import org.designup.picsou.gui.components.charts.BalanceGraph;
import org.designup.picsou.gui.description.Formatting;
import org.globsframework.utils.exceptions.ItemNotFound;
import junit.framework.Assert;

public class BalanceGraphChecker extends GuiChecker {
  private BalanceGraph graph;
  private String name;
  private Window window;

  public BalanceGraphChecker(BalanceGraph graph) {
    this.graph = graph;
  }

  public BalanceGraphChecker(String name, Window window) {
    this.name = name;
    this.window = window;
  }

  public void checkHidden() {
    checkComponentVisible(window, BalanceGraph.class, name, false);
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
      checkComponentVisible(window, BalanceGraph.class, name, true);
      graph = window.findSwingComponent(BalanceGraph.class, name);
      if (graph == null) {
        throw new ItemNotFound("Unable to find balanceGraph component");
      }
    }
  }
}
