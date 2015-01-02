package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.model.Card;
import org.uispec4j.Panel;
import org.uispec4j.ToggleButton;
import org.uispec4j.UIComponent;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.assertTrue;

public class ViewSelectionChecker extends GuiChecker {
  private Window window;
  private Panel cardsPanel;

  public ViewSelectionChecker(Window window) {
    this.window = window;
  }

  public void selectHome() {
    select("home");
  }

  public void selectCategorization() {
    select("categorization");
  }

  public void selectAnalysis() {
    select("analysis");
  }

  public void selectBudget() {
    select("budget");
  }

  public void selectSavings() {
    select("savings");
  }

  public void selectData() {
    select("data");
  }

  public void selectProjects() {
    select("projects");
  }

  public void checkHomeSelected() {
    assertSelected("home");
  }

  public void checkDataSelected() {
    assertSelected("data");
  }

  public void checkProjectsEnabled() {
    assertEnabled("projects");
  }

  public void checkProjectsDisabled() {
    assertDisabled("projects");
  }

  public void checkProjectsSelected() {
    assertSelected("projects");
  }

  public void checkCategorizationSelected() {
    assertSelected("categorization");
  }

  public void checkBudgetSelected() {
    assertSelected("budget");
  }

  public void checkAnalysisEnabled() {
    assertEnabled("analysis");
  }

  public void checkAnalysisDisabled() {
    assertDisabled("analysis");
  }

  public void checkAnalysisSelected() {
    assertSelected("analysis");
  }

  private void select(String viewName) {
    getToggle(viewName).click();
  }

  private void assertEnabled(String viewName) {
    checkComponentVisible(getTogglesPanel(), JToggleButton.class, viewName + "CardToggle", true);
  }

  private void assertDisabled(String viewName) {
    checkComponentVisible(getTogglesPanel(), JToggleButton.class, viewName + "CardToggle", false);
  }

  private void assertSelected(String viewName) {
    assertTrue("View '" + viewName + "' not selected. Current selection: " + getSelectedToggle().getName(),
               getToggle(viewName).isSelected());
  }

  private ToggleButton getSelectedToggle() {
    for (UIComponent component : getTogglesPanel().getUIComponents(ToggleButton.class)) {
      ToggleButton button = (ToggleButton) component;
      if (button.isSelected().isTrue()) {
        return button;
      }
    }
    Assert.fail("No view selected");
    return null;
  }

  private ToggleButton getToggle(String viewName) {
    return getTogglesPanel().getToggleButton(viewName + "CardToggle");
  }

  public void checkBackForward(boolean backEnabled, boolean forwardEnabled) {
    UISpecAssert.assertEquals("backView", backEnabled, window.getButton("backView").isEnabled());
    UISpecAssert.assertEquals("forwardView", forwardEnabled, window.getButton("forwardView").isEnabled());
  }

  public void back() {
    window.getButton("backView").click();
  }

  public void forward() {
    window.getButton("forwardView").click();
  }

  public void checkAllTooltipsPresent() {
    for (Card card : Card.values()) {
      ToggleButton button = getToggle(card.getName());
      UISpecAssert.assertThat(button.tooltipContains(card.getLabel()));
    }
  }

  public void checkCategorizationSignpostVisible(String message) {
    checkSignpostVisible(window, getToggle("categorization"), message);
  }

  public void checkNoSignpostVisible() {
    checkNoSignpostVisible(getTogglesPanel());
  }

  private Panel getTogglesPanel() {
    if (cardsPanel == null) {
      cardsPanel = window.getPanel("viewToggles");
    }
    return cardsPanel;
  }
}
