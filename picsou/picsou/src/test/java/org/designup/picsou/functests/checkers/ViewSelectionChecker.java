package org.designup.picsou.functests.checkers;

import org.uispec4j.ToggleButton;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.assertTrue;
import org.uispec4j.assertion.UISpecAssert;

public class ViewSelectionChecker extends DataChecker {
  private Window window;

  public ViewSelectionChecker(Window window) {
    this.window = window;
  }

  public void selectHome() {
    select("home");
  }

  public void selectCategorization() {
    select("categorization");
  }

  public void selectBudget() {
    select("budget");
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

  private void select(String viewName) {
    getToggle(viewName).click();
  }

  private void assertSelected(String viewName) {
    assertTrue(getToggle(viewName).isSelected());
  }

  private ToggleButton getToggle(String viewName) {
    return window.getToggleButton(viewName + "CardToggle");
  }

  public void checkBackForward(boolean backEnabled, boolean forwardEnabled) {
    UISpecAssert.assertEquals("back", backEnabled, window.getButton("back").isEnabled());
    UISpecAssert.assertEquals("forward", forwardEnabled, window.getButton("forward").isEnabled());
  }

  public void back() {
    window.getButton("back").click();
  }

  public void forward() {
    window.getButton("forward").click();
  }
}
