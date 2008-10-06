package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.model.MasterCategory;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.*;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;
import java.awt.*;

public class BudgetViewChecker extends DataChecker {

  public final BudgetAreaChecker income;
  public final BudgetAreaChecker recurring;
  public final BudgetAreaChecker envelopes;
  public final OccasionalAreaChecker occasional;
  public final BudgetAreaChecker specials;
  public final BudgetAreaChecker savings;

  private Window window;

  public BudgetViewChecker(Window window) {
    this.window = window;
    this.income = new BudgetAreaChecker("incomeBudgetView", true);
    this.recurring = new BudgetAreaChecker("recurringBudgetView", true);
    this.envelopes = new BudgetAreaChecker("envelopeBudgetView", false);
    this.occasional = new OccasionalAreaChecker();
    this.specials = new BudgetAreaChecker("projectsBudgetView", false);
    this.savings = new BudgetAreaChecker("savingsBudgetView", true);
  }

  private int getIndex(JPanel panel, Component component) {
    for (int i = 0; i < panel.getComponentCount(); i++) {
      if (component == panel.getComponent(i)) {
        return i;
      }
    }
    return -1;
  }

  public class BudgetAreaChecker {

    private String panelName;
    private boolean singleSelection;
    private static final int OBSERVED_LABEL_OFFSET = 1;

    public BudgetAreaChecker(String panelName, boolean singleSelection) {
      this.panelName = panelName;
      this.singleSelection = singleSelection;
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

    public BudgetAreaChecker checkSeries(String seriesName, double observedAmount, double plannedAmount) {
      Panel budgetPanel = window.getPanel(panelName);
      Button nameButton = budgetPanel.getButton(seriesName);

      JPanel panel = (JPanel)nameButton.getContainer().getAwtContainer();
      int nameIndex = getIndex(panel, nameButton.getAwtComponent());

      Button observedLabel = new Button((JButton)panel.getComponent(nameIndex + OBSERVED_LABEL_OFFSET));
      String modifiedObservedAmount = (observedAmount < 0 ? "+" : "") +
                                      BudgetViewChecker.this.toString(Math.abs(observedAmount));
      UISpecAssert.assertTrue(seriesName + " observed : \nExpected  :" + modifiedObservedAmount +
                              "\nActual    :" + observedLabel.getLabel(),
                              observedLabel.textEquals(modifiedObservedAmount));

      String modifiedPlannedAmount = (plannedAmount < 0 ? "+" : "") +
                                     BudgetViewChecker.this.toString(Math.abs(plannedAmount));
      TextBox plannedLabel = new TextBox((JLabel)panel.getComponent(nameIndex + 3));
      UISpecAssert.assertTrue(seriesName + " planned : \nExpected  :" + modifiedPlannedAmount +
                              "\nActual    :" + plannedLabel.getText(),
                              plannedLabel.textEquals(modifiedPlannedAmount));
      return this;
    }

    public BudgetAreaChecker checkSeriesNotPresent(String... seriesName) {
      Panel budgetPanel = window.getPanel(panelName);
      for (String name : seriesName) {
        UISpecAssert.assertFalse(budgetPanel.containsUIComponent(Button.class, name));
      }
      return this;
    }

    public SeriesEditionDialogChecker editSeriesList() {
      return openSeriesEditionDialog("editAllSeries");
    }

    public SeriesEditionDialogChecker editSeries(String seriesName) {
      return openSeriesEditionDialog(seriesName);
    }

    public void checkEditAllSeriesIsEnabled(boolean enabled) {
      Panel budgetPanel = window.getPanel(panelName);
      UISpecAssert.assertEquals(enabled, budgetPanel.getButton("editAllSeries").isEnabled());
    }

    public SeriesEditionDialogChecker createSeries() {
      return openSeriesEditionDialog("createSeries");
    }

    private SeriesEditionDialogChecker openSeriesEditionDialog(String seriesName) {
      Panel budgetPanel = window.getPanel(panelName);
      Window dialog = WindowInterceptor.getModalDialog(budgetPanel.getButton(seriesName).triggerClick());
      return new SeriesEditionDialogChecker(dialog, singleSelection);
    }

    public void gotoData(String seriesName) {
      Panel budgetPanel = window.getPanel(panelName);
      Button nameButton = budgetPanel.getButton(seriesName);
      JPanel panel = (JPanel)nameButton.getContainer().getAwtContainer();
      int nameIndex = getIndex(panel, nameButton.getAwtComponent());
      Button button = new Button((JButton)panel.getComponent(nameIndex + OBSERVED_LABEL_OFFSET));
      button.click();
    }

    public BudgetAreaChecker checkOrder(String... seriesNames) {
      Panel budgetPanel = window.getPanel(panelName);
      UIComponent[] uiComponents = budgetPanel.getUIComponents(Button.class, "seriesName");
      Assert.assertEquals(uiComponents.length, seriesNames.length);
      for (int i = 0; i < uiComponents.length; i++) {
        UIComponent component = uiComponents[i];
        UISpecAssert.assertThat(((Button)component).textEquals(seriesNames[i]));
      }
      return this;
    }
  }

  public class OccasionalAreaChecker {

    public void checkTitle(String title) {
      Panel budgetPanel = window.getPanel("occasionalBudgetView");
      TextBox label = budgetPanel.getTextBox("budgetAreaTitle");
      UISpecAssert.assertThat(label.textEquals(title));
    }

    public void checkTotalAmounts(double spent, double free) {
      Panel budgetPanel = window.getPanel("occasionalBudgetView");
      TextBox totalObserved = budgetPanel.getTextBox("totalObservedAmount");
      String observedAmount = BudgetViewChecker.this.toString(spent);
      if (spent < 0.0) {
        observedAmount = observedAmount.replace("-", "+");
      }
      UISpecAssert.assertTrue(totalObserved.textEquals(observedAmount));

      TextBox totalPlanned = budgetPanel.getTextBox("totalPlannedAmount");
      String amount = BudgetViewChecker.this.toString(free);
      if (free < 0.0) {
        amount = "0.0";
      }
      UISpecAssert.assertTrue(totalPlanned.textEquals(amount));
    }

    public OccasionalAreaChecker check(MasterCategory category, Double amount) {
      Panel budgetPanel = window.getPanel("occasionalBudgetView");
      UISpecAssert.assertTrue(budgetPanel.containsUIComponent(TextBox.class, "categoryName." + getCategoryName(category)));
      UISpecAssert.assertTrue(budgetPanel.getButton("amount." + getCategoryName(category))
        .textEquals(BudgetViewChecker.this.toString(amount)));
      return this;
    }

    public OccasionalAreaChecker checkNotContains(String categoryName) {
      Panel budgetPanel = window.getPanel("occasionalBudgetView");
      UISpecAssert.assertFalse(budgetPanel.containsUIComponent(TextBox.class, "categoryName." + categoryName));
      return this;
    }

    public void gotoData(MasterCategory master) {
      Panel budgetPanel = window.getPanel("occasionalBudgetView");
      UISpecAssert.assertTrue(budgetPanel.containsUIComponent(TextBox.class, "categoryName." + getCategoryName(master)));
      budgetPanel.getButton("amount." + getCategoryName(master)).click();
    }

    public OccasionalAreaChecker checkOrder(MasterCategory... categories) {
      Panel budgetPanel = window.getPanel("occasionalBudgetView");
      UIComponent[] uiComponents = budgetPanel.getUIComponents(TextBox.class, "categoryName");
      Assert.assertEquals(uiComponents.length, categories.length);
      for (int i = 0; i < uiComponents.length; i++) {
        UIComponent component = uiComponents[i];
        UISpecAssert.assertThat(((TextBox)component).textEquals(getCategoryName(categories[i])));
      }
      return this;
    }
  }
}
