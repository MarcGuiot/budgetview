package org.designup.picsou.functests.checkers;

import org.designup.picsou.model.BudgetArea;
import org.uispec4j.*;
import org.uispec4j.utils.Utils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static org.uispec4j.assertion.UISpecAssert.*;

public class BudgetAreaCategorizationChecker extends GuiChecker {
  private CategorizationChecker categorizationChecker;
  private BudgetArea budgetArea;
  private Panel panel;

  public BudgetAreaCategorizationChecker(CategorizationChecker categorizationChecker, BudgetArea budgetArea) {
    this(categorizationChecker, budgetArea, categorizationChecker.selectAndGetBudgetArea(budgetArea));
  }

  public BudgetAreaCategorizationChecker(CategorizationChecker categorizationChecker,
                                         BudgetArea budgetArea,
                                         Panel panel) {
    this.categorizationChecker = categorizationChecker;
    this.budgetArea = budgetArea;
    this.panel = panel;
    assertTrue(panel.isVisible());
  }

  public BudgetAreaCategorizationChecker checkContainsSeries(String... seriesNames) {
    List<String> names = getSeriesNames();
    org.globsframework.utils.TestUtils.assertContains(names, seriesNames);
    return this;
  }

  public BudgetAreaCategorizationChecker checkSeriesListEquals(String... seriesNames) {
    List<String> names = getSeriesNames();
    org.globsframework.utils.TestUtils.assertEquals(names, seriesNames);
    return this;
  }

  private List<String> getSeriesNames() {
    List<String> names = new ArrayList<String>();
    UIComponent[] radios = panel.getUIComponents(RadioButton.class);
    for (UIComponent radio : radios) {
      if (isSeriesRadio(radio)) {
        names.add(radio.getLabel());
      }
    }
    return names;
  }

  private boolean isSeriesRadio(UIComponent radio) {
    return Utils.equals(radio.getName(), radio.getLabel());
  }

  public BudgetAreaCategorizationChecker checkDoesNotContainSeries(String... seriesNames) {
    for (String seriesName : seriesNames) {
      assertFalse("Series " + seriesName + " unexpectedly found",
                  panel.containsUIComponent(RadioButton.class, seriesName));
    }
    return this;
  }

  public BudgetAreaCategorizationChecker checkNotPresent(String seriesName) {
    assertFalse("Series " + seriesName + " unexpectedly found",
                panel.containsUIComponent(RadioButton.class, seriesName));
    return this;
  }

  public BudgetAreaCategorizationChecker checkSeriesIsSelected(String seriesName) {
    assertThat(panel.getRadioButton(seriesName).isSelected());
    return this;
  }

  public BudgetAreaCategorizationChecker checkSeriesNotSelected(String seriesName) {
    assertFalse(panel.getRadioButton(seriesName).isSelected());
    return this;
  }

  public BudgetAreaCategorizationChecker checkNoSeriesSelected() {
    UIComponent[] selectors = panel.getUIComponents(RadioButton.class);
    for (UIComponent selector : selectors) {
      if (selector.getAwtComponent().isVisible()) {
        assertFalse(selector.getLabel() + " selected", ((RadioButton)selector).isSelected());
      }
    }
    return this;
  }

  public BudgetAreaCategorizationChecker checkContainsNoSeries() {
    UIComponent[] uiComponents = panel.getUIComponents(ToggleButton.class);
    if (uiComponents.length > 1) {
      List<String> names = new ArrayList<String>();
      for (UIComponent uiComponent : uiComponents) {
        RadioButton toggle = (RadioButton)uiComponent;
        names.add(toggle.getLabel());
      }
      fail("Unexpect toggles found: " + names);
    }
    return this;
  }

  public BudgetAreaCategorizationChecker checkActiveSeries(String seriesName) {
    assertThat(panel.getRadioButton(seriesName).foregroundEquals("000000"));
    return this;
  }

  public BudgetAreaCategorizationChecker checkNonActiveSeries(String seriesName) {
    assertThat(panel.getRadioButton(seriesName).foregroundEquals("777777"));
    return this;
  }

  public SeriesEditionDialogChecker createSeries() {
    Button button =
      categorizationChecker.getPanel()
        .getPanel(budgetArea.getName() + "SeriesChooser")
        .getButton("createSeries");
    return SeriesEditionDialogChecker.open(button);
  }

  public BudgetAreaCategorizationChecker createSeries(String seriesName) {
    createSeries().setName(seriesName)
      .validate();
    return this;
  }

  public BudgetAreaCategorizationChecker selectNewSeries(String seriesName) {
    createSeries().setName(seriesName)
      .validate();
    return this;
  }

  public BudgetAreaCategorizationChecker selectNewSeries(String seriesName, double amount) {
    SeriesEditionDialogChecker editionDialogChecker = createSeries()
      .setName(seriesName)
      .selectAllMonths();
    if (amount>0){
      editionDialogChecker.selectPositiveAmounts();
    }else {
      editionDialogChecker.selectNegativeAmounts();
    }
    editionDialogChecker
      .setAmount(Math.abs(amount))
      .validate();
    return this;
  }

  public BudgetAreaCategorizationChecker selectNewSeries(String seriesName, String description) {
    createSeries().setName(seriesName).setDescription(description).validate();
    return selectSeries(seriesName);
  }

  public BudgetAreaCategorizationChecker selectNewSeriesWithSubSeries(String series, String subSeries) {
    createSeries()
      .setName(series)
      .gotoSubSeriesTab()
      .addSubSeries(subSeries)
      .validate();
    return this;
  }

  public BudgetAreaCategorizationChecker selectSeries(String seriesName) {
    checkComponentVisible(panel, JRadioButton.class, seriesName, true);
    panel.getRadioButton(seriesName).click();
    return this;
  }

  public BudgetAreaCategorizationChecker selectSubSeries(String series, String subSeries) {
    panel.getRadioButton(series + ":" + subSeries).click();
    return this;
  }

  public BudgetAreaCategorizationChecker checkSeriesContainsSubSeries(String series, String... subSeries) {
    RadioButton seriesRadio = panel.getRadioButton(series);
    Panel seriesPanel = seriesRadio.getContainer("seriesBlock");
    for (String subName : subSeries) {
      assertTrue("No subSeries found with name: " + subName,
                 seriesPanel.containsUIComponent(RadioButton.class, subName));
    }
    return this;
  }

  public BudgetAreaCategorizationChecker checkSeriesIsSelectedWithSubSeries(String series, String subSeries) {
    RadioButton toggle = panel.getRadioButton(series + ":" + subSeries);
    assertThat(toggle.isSelected());
    return this;
  }

  public BudgetAreaCategorizationChecker checkNoSeriesMessage(String message) {
    TextBox textBox = categorizationChecker.getPanel().getTextBox("noSeriesMessage");
    assertTrue(textBox.isVisible());
    assertThat(textBox.textContains(message));
    return this;
  }

  public BudgetAreaCategorizationChecker checkNoSeriesMessageHidden() {
    assertFalse(categorizationChecker.getPanel().getTextBox("noSeriesMessage").isVisible());
    return this;
  }

  public AccountEditionChecker clickSeriesMessageAccountCreationLink(String text) {
    TextBox textBox = categorizationChecker.getPanel().getTextBox("noSeriesMessage");
    assertTrue(textBox.isVisible());
    return AccountEditionChecker.open(textBox.triggerClickOnHyperlink(text));
  }

  public SeriesEditionDialogChecker editSeries() {
    return categorizationChecker.editSeries();
  }

  public SeriesEditionDialogChecker editSeries(String seriesName) {
    return categorizationChecker.editSeries(seriesName);
  }

  public BudgetAreaCategorizationChecker checkDescriptionDisplayed() {
    assertThat(panel.getTextBox("description").textContains(budgetArea.getDescription()));
    return this;
  }

  public BudgetAreaCategorizationChecker checkSeriesTooltip(String seriesName, String tooltip) {
    assertThat(panel.getRadioButton(seriesName).tooltipContains(tooltip));
    return this;
  }

  public BudgetAreaCategorizationChecker checkEditSeriesButtonNotVisible() {
    categorizationChecker.checkEditSeriesNotVisible();
    return this;
  }
}
