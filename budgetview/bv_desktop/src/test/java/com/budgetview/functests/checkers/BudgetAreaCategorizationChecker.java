package com.budgetview.functests.checkers;

import com.budgetview.desktop.description.Labels;
import com.budgetview.shared.model.BudgetArea;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.globsframework.utils.TestUtils;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.*;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.uispec4j.assertion.UISpecAssert.*;

public class BudgetAreaCategorizationChecker extends GuiChecker {
  public static final String DISABLED_SERIES_COLOR = "777777";
  protected CategorizationChecker categorizationChecker;
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
    List<String> names = getSeriesNames(panel);
    org.globsframework.utils.TestUtils.assertContains(names, seriesNames);
    return this;
  }

  public BudgetAreaCategorizationChecker checkSeriesListEquals(String... seriesNames) {
    List<String> names = getSeriesNames(panel);
    org.globsframework.utils.TestUtils.assertEquals(names, seriesNames);
    return this;
  }

  private List<String> getSeriesNames(Panel container) {
    List<String> names = new ArrayList<String>();
    UIComponent[] radios = container.getUIComponents(RadioButton.class);
    for (UIComponent radio : radios) {
      if (isSeriesRadio(radio)) {
        names.add(radio.getLabel());
      }
    }
    return names;
  }

  private List<String> getSelectedSeriesNames(Panel container) {
    List<String> names = new ArrayList<String>();
    UIComponent[] radios = container.getUIComponents(RadioButton.class);
    for (UIComponent radio : radios) {
      RadioButton radioButton = (RadioButton) radio;
      if (radioButton.isSelected().isTrue()) {
        names.add(radio.getLabel());
      }
    }
    return names;
  }

  private boolean isSeriesRadio(UIComponent radio) {
    return Utils.equals(radio.getName(), radio.getLabel());
  }

  public BudgetAreaCategorizationChecker checkDoesNotContainSeries(final String... seriesNames) {
    assertThat(new Assertion() {
      public void check() {
        for (String seriesName : seriesNames) {
          if (panel.containsUIComponent(RadioButton.class, seriesName).isTrue()) {
            StringBuilder message = new StringBuilder("Series " + seriesName + " unexpectedly found");
            RadioButton button = panel.getRadioButton(seriesName);
            if (button.foregroundEquals(DISABLED_SERIES_COLOR).isTrue()) {
              message.append(" (appears as disabled)");
            }
            throw new AssertionFailedError(message.toString());
          }
        }
      }
    });
    return this;
  }

  public BudgetAreaCategorizationChecker checkNotPresent(String seriesName) {
    assertFalse("Series " + seriesName + " unexpectedly found",
                panel.containsUIComponent(RadioButton.class, seriesName));
    return this;
  }

  public BudgetAreaCategorizationChecker checkSelectedSeries(String seriesName) {
    TestUtils.assertSetEquals(getSelectedSeriesNames(panel), Collections.singleton(seriesName));
    return this;
  }

  public BudgetAreaCategorizationChecker checkSeriesNotSelected(String seriesName) {
    assertFalse(panel.getRadioButton(seriesName).isSelected());
    return this;
  }

  public BudgetAreaCategorizationChecker checkNoSeriesSelected() {
    UISpecAssert.assertThat(new Assertion() {
      public void check() {
        UIComponent[] selectors = panel.getUIComponents(RadioButton.class);
        for (UIComponent selector : selectors) {
          if (selector.getAwtComponent().isVisible()) {
            assertFalse(selector.getLabel() + " selected", ((RadioButton) selector).isSelected());
          }
        }
      }
    });
    return this;
  }

  public BudgetAreaCategorizationChecker checkContainsNoSeries() {
    UISpecAssert.assertThat(new Assertion() {
      public void check() {
        UIComponent[] uiComponents = panel.getUIComponents(ToggleButton.class);
        if (uiComponents.length > 1) {
          List<String> names = new ArrayList<String>();
          for (UIComponent uiComponent : uiComponents) {
            RadioButton toggle = (RadioButton) uiComponent;
            names.add(toggle.getLabel());
          }
          fail("Unexpect toggles found: " + names);
        }
      }
    });
    return this;
  }

  public BudgetAreaCategorizationChecker checkSeriesIsActive(String seriesName) {
    assertThat("Series " + seriesName + " is not active", panel.getRadioButton(seriesName).foregroundEquals("000000"));
    return this;
  }

  public BudgetAreaCategorizationChecker checkSeriesIsInactive(String seriesName) {
    assertThat("Series " + seriesName + " is not inactive", panel.getRadioButton(seriesName).foregroundEquals(DISABLED_SERIES_COLOR));
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

  public BudgetAreaCategorizationChecker checkCreateSeriesMessage(String message) {
    MessageDialogChecker.open(categorizationChecker.getPanel()
                                .getPanel(budgetArea.getName() + "SeriesChooser")
                                .getButton("createSeries")
                                .triggerClick())
      .checkInfoMessageContains(message);
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
    if (amount > 0) {
      editionDialogChecker.selectPositiveAmounts();
    }
    else {
      editionDialogChecker.selectNegativeAmounts();
    }
    editionDialogChecker
      .setAmount(Math.abs(amount))
      .validate();
    return this;
  }

  public BudgetAreaCategorizationChecker selectNewSeries(String seriesName, String description) {
    createSeries().setName(seriesName)
      .showDescription()
      .setDescription(description)
      .validate();
    return selectSeries(seriesName);
  }

  public BudgetAreaCategorizationChecker selectNewSeriesWithSubSeries(String series, String subSeries) {
    createSeries()
      .setName(series)
      .editSubSeries()
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

  public BudgetAreaCategorizationChecker checkSeriesContainsSubSeriesList(String series, String... subSeries) {
    TestUtils.assertEquals(getSubSeriesNames(series), subSeries);
    return this;
  }

  private List<String> getSubSeriesNames(String series) {
    RadioButton seriesRadio = panel.getRadioButton(series);
    Panel seriesPanel = seriesRadio.getContainer("seriesBlock").getPanel("subSeriesRepeat");
    List<String> result = new ArrayList<String>();
    UIComponent[] radioButtons = seriesPanel.getUIComponents(RadioButton.class);
    for (UIComponent radioButton : radioButtons) {
      result.add(radioButton.getLabel());
    }
    return result;
  }

  public BudgetAreaCategorizationChecker checkSeriesDoesNotContainSubSeries(String seriesName,
                                                                            String... subSeriesNames) {
    RadioButton seriesRadio = panel.getRadioButton(seriesName);
    Panel seriesPanel = seriesRadio.getContainer("seriesBlock");
    for (String subName : subSeriesNames) {
      assertFalse("subSeries unexpectedly found with name: " + subName,
                  seriesPanel.containsUIComponent(RadioButton.class, subName));
    }
    return this;
  }

  public BudgetAreaCategorizationChecker checkSeriesContainsNoSubSeries(String seriesName) {
    RadioButton seriesRadio = panel.getRadioButton(seriesName);
    Panel subSeriesPanel = seriesRadio.getContainer("seriesBlock").getPanel("subSeriesRepeat");
    int count = subSeriesPanel.getSwingComponents(JRadioButton.class).length;
    if (count > 0) {
      Assert.fail("Unexpected subSeries content: " + subSeriesPanel.getDescription());
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
    assertThat(panel.getTextBox("description").textContains(Labels.getDescription(budgetArea)));
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

  public BudgetAreaCategorizationChecker hideDescription() {
    panel.getButton("hideDescription").click();
    return this;
  }

  public BudgetAreaCategorizationChecker checkDescriptionShown() {
    checkComponentVisible(panel, JPanel.class, "descriptionPanel", true);
    return this;
  }

  public BudgetAreaCategorizationChecker checkDescriptionHidden() {
    checkComponentVisible(panel, JPanel.class, "descriptionPanel", false);
    return this;
  }

  public BudgetAreaCategorizationChecker showDescription() {
    panel.getButton("showDescription").click();
    return this;
  }

  public BudgetAreaCategorizationChecker checkShowDescriptionButtonHidden() {
    checkComponentVisible(panel, JButton.class, "showDescription", false);
    return this;
  }

  public BudgetAreaCategorizationChecker checkMessage(String text) {
    TextBox textBox = panel.getTextBox("categorizationMessage");
    assertThat(textBox.isVisible());
    assertThat(textBox.textEquals(text));
    return this;
  }

  public AccountEditionChecker clickMessageToEditAccount(String linkText) {
    TextBox textBox = panel.getTextBox("categorizationMessage");
    return AccountEditionChecker.open(textBox.triggerClickOnHyperlink(linkText));
  }

  public BudgetAreaCategorizationChecker checkMessageHidden() {
    checkComponentVisible(panel, JEditorPane.class, "categorizationMessage", false);
    return this;
  }

  public BudgetAreaCategorizationChecker checkGroupContainsSeries(String group, String... seriesNames) {
    List<String> actualSeries = getSeriesNames(group);
    TestUtils.assertSetEquals(actualSeries, seriesNames);
    return this;
  }

  public List<String> getSeriesNames(String group) {
    Panel groupPanel = getGroupPanel(group);
    List<String> actualSeries = new ArrayList<String>();
    for (Component component : groupPanel.getSwingComponents(JRadioButton.class)) {
      JRadioButton radio = (JRadioButton) component;
      actualSeries.add(radio.getText());
    }
    return actualSeries;
  }

  public BudgetAreaCategorizationChecker checkGroupDoesNotContainSeries(String group, String... seriesNames) {
    List<String> actualSeries = getSeriesNames(group);
    for (String series : seriesNames) {
      if (actualSeries.contains(series)) {
        Assert.fail("'" + series + "' unexpectedly found in " + actualSeries);
      }
    }
    return this;
  }

  private Panel getGroupPanel(String groupName) {
    List<JLabel> matchingLabels = new ArrayList<JLabel>();
    for (Component component : panel.getSwingComponents(JLabel.class, "groupLabel")) {
      JLabel label = (JLabel) component;
      if (component.isVisible() && Utils.equals(groupName, label.getText())) {
        matchingLabels.add(label);
      }
    }
    if (matchingLabels.isEmpty()) {
      Assert.fail("No group found with name: " + groupName + " - actual: " + getTextForLabels(matchingLabels));
    }
    if (matchingLabels.size() > 1) {
      Assert.fail("Several groups found with name: " + groupName + " => " + getTextForLabels(matchingLabels));
    }
    TextBox textBox = new TextBox(matchingLabels.get(0));
    return textBox.getContainer("groupPanel");
  }

  private List<String> getTextForLabels(List<JLabel> matchingLabels) {
    List<String> actual = new ArrayList<String>();
    for (JLabel matchingLabel : matchingLabels) {
      actual.add(matchingLabel.getText());
    }
    return actual;
  }

  public BudgetAreaCategorizationChecker checkGroupNotShown(String group) {
    for (Component component : panel.getSwingComponents(JLabel.class, "groupLabel")) {
      JLabel label = (JLabel) component;
      if (component.isVisible() && Utils.equals(group, label.getText())) {
        Assert.fail("Group '" + group + "' unexpectedly visible in: " + getVisibleGroupNames());
      }
    }
    return this;
  }

  public BudgetAreaCategorizationChecker checkContainsNoGroup() {
    List<String> visibleGroups = getVisibleGroupNames();
    TestUtils.checkEmpty(visibleGroups, "Groups unexpectedly shown:");
    return this;
  }

  public List<String> getVisibleGroupNames() {
    List<String> visibleGroups = new ArrayList<String>();
    for (Component component : panel.getSwingComponents(JLabel.class, "groupLabel")) {
      if (component.isVisible()) {
        JLabel label = (JLabel) component;
        visibleGroups.add(label.getText());
      }
    }
    return visibleGroups;
  }

  public BudgetAreaCategorizationChecker checkNoGroupSeriesListEquals(String... seriesNames) {
    List<String> names = getSeriesNames(panel.getPanel("rootSeriesPanel"));
    org.globsframework.utils.TestUtils.assertEquals(names, seriesNames);
    return this;
  }
}
