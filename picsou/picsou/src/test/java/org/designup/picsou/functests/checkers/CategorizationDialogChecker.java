package org.designup.picsou.functests.checkers;

import org.uispec4j.*;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.assertTrue;
import org.designup.picsou.model.MasterCategory;

import java.util.List;
import java.util.ArrayList;

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
    ToggleButton[] toggles = (ToggleButton[])seriesPanel.getUIComponents(ToggleButton.class);
    for (ToggleButton toggle : toggles) {
      names.add(toggle.getLabel());
    }

    org.globsframework.utils.TestUtils.assertContains(names, seriesNames);
  }

  public void selectRecurringSeries(String name) {
    Panel panel = getRecurringSeriesPanel();
    panel.getToggleButton(name).click();
  }

  private Panel getRecurringSeriesPanel() {
    Panel panel = dialog.getPanel("recurringSeriesPanel");
    assertTrue(panel.isVisible());
    return panel;
  }

  public void validate() {
    dialog.getButton("OK").click();
  }

  public void selectEnvelopes() {
    dialog.getToggleButton("Envelopes").click();
  }

  public void checkContainsEnvelope(String envelopeName, MasterCategory... categories) {
    Panel panel = getEnvelopeSeriesPanel();
    Panel envelopePanel = panel.getPanel("envelope:" + envelopeName);
    assertTrue(envelopePanel.containsLabel(envelopeName));
    for (MasterCategory category : categories) {
      assertTrue(envelopePanel.containsUIComponent(ToggleButton.class, getCategoryName(category)));
    }
  }

  public void selectEnvelopeSeries(String envelopeName, MasterCategory category) {
    Panel panel = getEnvelopeSeriesPanel();
    Panel envelopePanel = panel.getPanel("envelope:" + envelopeName);
    envelopePanel.getToggleButton(getCategoryName(category)).click();
  }

  private Panel getEnvelopeSeriesPanel() {
    Panel panel = dialog.getPanel("envelopeSeriesPanel");
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
