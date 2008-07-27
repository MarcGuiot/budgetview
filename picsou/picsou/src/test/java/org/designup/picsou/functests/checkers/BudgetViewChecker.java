package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;

import javax.swing.*;
import java.awt.*;

public class BudgetViewChecker extends DataChecker {

  public final BudgetAreaChecker income;
  public final BudgetAreaChecker recurring;
  public final BudgetAreaChecker envelopes;

  private Window window;

  public BudgetViewChecker(Window window) {
    this.window = window;
    this.income = new BudgetAreaChecker("incomeBudgetView");
    this.recurring = new BudgetAreaChecker("recurringBudgetView");
    this.envelopes = new BudgetAreaChecker("envelopeBudgetView");
  }

  public class BudgetAreaChecker {
    private String panelName;

    public BudgetAreaChecker(String panelName) {
      this.panelName = panelName;
    }

    public void checkTitle(String title) {
      Panel budgetPanel = window.getPanel(panelName);
      TextBox label = budgetPanel.getTextBox("budgetAreaTitle");
      UISpecAssert.assertThat(label.textEquals(title));
    }

    public void checkTotalAmounts(double received, double planned) {
      Panel budgetPanel = window.getPanel(panelName);
      TextBox totalObserved = budgetPanel.getTextBox("totalObservedAmount");
      UISpecAssert.assertTrue(totalObserved.textEquals(BudgetViewChecker.this.toString(received)));

      TextBox totalPlanned = budgetPanel.getTextBox("totalPlannedAmount");
      UISpecAssert.assertTrue(totalPlanned.textEquals(BudgetViewChecker.this.toString(planned)));
    }

    public void checkSeries(String seriesName, double observedAmount, double plannedAmount) {
      Panel budgetPanel = window.getPanel(panelName);
      TextBox nameBox = budgetPanel.getTextBox(seriesName);

      JPanel panel = (JPanel)nameBox.getContainer().getAwtContainer();
      int nameIndex = getIndex(panel, nameBox.getAwtComponent());

      TextBox observedLabel = new TextBox((JLabel)panel.getComponent(nameIndex + 1));
      UISpecAssert.assertTrue(observedLabel.textEquals(BudgetViewChecker.this.toString(observedAmount)));

      TextBox plannedLabel = new TextBox((JLabel)panel.getComponent(nameIndex + 3));
      UISpecAssert.assertTrue(plannedLabel.textEquals(BudgetViewChecker.this.toString(plannedAmount)));
    }

    private int getIndex(JPanel panel, Component component) {
      for (int i = 0; i < panel.getComponentCount(); i++) {
        if (component == panel.getComponent(i)) {
          return i;
        }
      }
      return -1;
    }
  }
}
