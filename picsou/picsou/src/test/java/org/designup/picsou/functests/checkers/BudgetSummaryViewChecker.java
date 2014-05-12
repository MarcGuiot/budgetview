package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.functests.checkers.components.HistoDailyChecker;
import org.designup.picsou.gui.components.charts.histo.HistoSelectionManager;
import org.designup.picsou.model.Day;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.gui.splits.components.RoundedRectIcon;
import org.globsframework.gui.splits.utils.HtmlUtils;
import org.globsframework.model.Key;
import org.globsframework.utils.TablePrinter;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Window;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.utils.ColorUtils;

import javax.swing.*;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

import static org.uispec4j.assertion.UISpecAssert.*;

public class BudgetSummaryViewChecker extends ViewChecker {
  private HistoDailyChecker chart;
  private Panel panel;
  private final Color OK_COLOR = Colors.toColor("#73ff73");

  public BudgetSummaryViewChecker(Window mainWindow) {
    super(mainWindow);
  }

  public BudgetSummaryViewChecker checkContent(final String expectedContent) {
    UISpecAssert.assertThat(new Assertion() {
      public void check() {
        Assert.assertEquals(expectedContent.trim(), getActualContent().trim());
      }
    });
    return this;
  }

  public BudgetSummaryViewChecker checkContentContains(String summaryDate) {
    if (!getActualContent().contains(summaryDate)) {
      Assert.fail(summaryDate + " not found in:\n" + getActualContent());
    }
    return this;
  }

  public String getActualContent() {
    TablePrinter printer = new TablePrinter();
    Component[] statuses = getPanel().getSwingComponents(JLabel.class, "accountStatus");
    Component[] selectors = getPanel().getSwingComponents(JButton.class, "accountSelector");
    Component[] positions = getPanel().getSwingComponents(JLabel.class, "accountPosition");
    for (int i = 0; i < statuses.length; i++) {
      JLabel status = ((JLabel)statuses[i]);
      RoundedRectIcon icon = ((RoundedRectIcon)status.getIcon());
      boolean isOk = ColorUtils.equals(icon.getBackgroundColor(), OK_COLOR, true);
      JButton selectorButton = (JButton)selectors[i];
      String accountName = selectorButton.getText();
      if (selectorButton.getFont().isBold()) {
        accountName += "*";
      }
      printer.addRow(isOk ? "ok" : "nok",
                     accountName,
                     (HtmlUtils.cleanup(((JLabel)positions[i]).getText())));
    }
    return printer.toString();
  }

  public void selectAccount(String accountName) {
    getPanel().getButton(accountName).click();
  }

  public BudgetSummaryViewChecker checkEndPosition(double amount) {
    getChart().checkEndOfMonthValue(amount);
    return this;
  }

  public BudgetSummaryViewChecker checkNoEstimatedPosition() {
    getChart().checkEndOfMonthValue(null);
    return this;
  }

  public BudgetSummaryViewChecker rollover(int monthId, int day) {
    HistoDailyChecker chart = getChart();
    HistoSelectionManager selectionManager = chart.getChart().getSelectionManager();
    int columnIndex = chart.getDataset().getIndex(monthId);
    Set<Key> keys = new HashSet<Key>();
    keys.add(Key.create(Day.MONTH, monthId, Day.DAY, day));
    selectionManager.updateRollover(columnIndex >= 0 ? columnIndex : null, keys, false, false, new Point(0,0));
    return this;
  }

  public BudgetSummaryViewChecker checkMultiSelection(int count) {
    TextBox label = getPanel().getTextBox("multiSelectionLabel");
    assertThat(label.isVisible());
    assertThat(label.textContains(count + " months total"));
    return this;
  }

  public BudgetSummaryViewChecker checkMultiSelectionNotShown() {
    checkComponentVisible(getPanel(), JLabel.class, "multiSelectionLabel", false);
    return this;
  }

  public HistoDailyChecker getChart() {
    if (chart == null) {
      chart = new HistoDailyChecker(getPanel(), "chart");
    }
    return chart;
  }

  private Panel getPanel() {
    if (panel == null) {
      views.selectBudget();
      SavingsViewChecker.toggleToMainIfNeeded(mainWindow);
      panel = mainWindow.getPanel("budgetSummaryView");
    }
    return panel;
  }
}
