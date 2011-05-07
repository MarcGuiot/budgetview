package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.ToggleButton;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.assertTrue;
import org.uispec4j.assertion.UISpecAssert;
import org.designup.picsou.gui.model.Card;
import junit.framework.Assert;

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

  public void checkHomeSelected() {
    assertSelected("home");
  }

  public void checkDataSelected() {
    assertSelected("data");
  }

  public void checkCategorizationSelected() {
    assertSelected("categorization");
  }

  public void checkBudgetSelected() {
    assertSelected("budget");
  }

  public void checkSavingsSelected() {
    assertSelected("savings");
  }

  private void select(String viewName) {
    getToggle(viewName).click();
  }

  private void assertSelected(String viewName) {
    assertTrue("View '" + viewName + "' not selected. Current selection: " + getSelectedToggle().getName(),
               getToggle(viewName).isSelected());
  }

  private ToggleButton getSelectedToggle() {
    for (Card card : Card.values()) {
      ToggleButton button = getToggle(card.getName());
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

  public void checkDataSignpostVisible() {
    checkSignpostVisible(window, getToggle("data"), "Accounts");
  }

  public void checkCategorizationSignpostVisible(String message) {
    checkSignpostVisible(window, getToggle("categorization"), message);
  }

  private Panel getTogglesPanel() {
    if (cardsPanel == null) {
      cardsPanel = window.getPanel("viewToggles");
    }
    return cardsPanel;
  }
}
