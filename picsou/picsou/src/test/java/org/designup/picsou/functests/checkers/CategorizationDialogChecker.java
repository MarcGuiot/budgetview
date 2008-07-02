package org.designup.picsou.functests.checkers;

import org.designup.picsou.model.MasterCategory;
import org.uispec4j.*;
import static org.uispec4j.assertion.UISpecAssert.assertTrue;

import java.util.ArrayList;
import java.util.List;

public class CategorizationDialogChecker extends DataChecker {
  private Window dialog;
  private TextBox transactionLabel;

  public CategorizationDialogChecker(Window dialog) {
    this.dialog = dialog;
    this.transactionLabel = dialog.getTextBox("transactionLabel");
  }

  public void checkLabel(String expected) {
    assertTrue(transactionLabel.textEquals(expected));
  }

  public void selectRecurring() {
    dialog.getToggleButton("Recurring").click();
  }

  public void checkContainsRecurringSeries(String... seriesNames) {
    Panel seriesPanel = getRecurringSeriesPanel();

    List<String> names = new ArrayList<String>();
    UIComponent[] toggles = seriesPanel.getUIComponents(ToggleButton.class);
    for (UIComponent toggle : toggles) {
      names.add(toggle.getLabel());
    }

    org.globsframework.utils.TestUtils.assertContains(names, seriesNames);
  }

  public void selectRecurringSeries(String name) {
    Panel panel = getRecurringSeriesPanel();
    panel.getToggleButton(name).click();
  }

  private Panel getRecurringSeriesPanel() {
    Panel panel = dialog.getPanel("recurringSeriesRepeat");
    assertTrue(panel.isVisible());
    return panel;
  }

  public void validate() {
    dialog.getButton("ok").click();
  }

  public void cancel() {
    dialog.getButton("cancel").click();
  }

  public void selectEnvelopes() {
    dialog.getToggleButton("expensesEnvelope").click();
  }

  public void checkContainsEnvelope(String envelopeName, MasterCategory... categories) {
    Panel panel = getEnvelopeSeriesPanel();
    assertTrue(panel.containsLabel(envelopeName));
    for (MasterCategory category : categories) {
      assertTrue(panel.containsUIComponent(ToggleButton.class, envelopeName + ":" + category.getName()));
    }
  }

  public void selectEnvelopeSeries(String envelopeName, MasterCategory category) {
    Panel panel = getEnvelopeSeriesPanel();
    panel.getToggleButton(envelopeName + ":" + category.getName()).click();
  }

  private Panel getEnvelopeSeriesPanel() {
    Panel panel = dialog.getPanel("envelopeSeriesRepeat");
    assertTrue(panel.isVisible());
    return panel;
  }

  public void selectOccasional() {
    dialog.getToggleButton("Occasional").click();
  }

  public void selectOccasionalSeries(MasterCategory category) {
    Panel panel = getOccasionalSeriesPanel();
    panel.getToggleButton(getCategoryName(category)).click();
  }

  private Panel getOccasionalSeriesPanel() {
    Panel panel = dialog.getPanel("occasionalSeriesPanel");
    assertTrue(panel.isVisible());
    return panel;
  }
}
