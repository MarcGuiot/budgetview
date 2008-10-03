package org.designup.picsou.functests.checkers;

import org.uispec4j.ToggleButton;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.assertTrue;

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

  public void selectAnalysis() {
    select("analysis");
  }

  public void selectData() {
    select("analysis");
  }

  public void selectEvolution() {
    select("analysis");
    select("evolution");
  }

  public void selectRepartition() {
    select("analysis");
    select("repartition");
  }

  public void checkHomeSelected() {
    assertSelected("home");
  }

  public void checkDataSelected() {
    assertSelected("analysis");
  }

  public void checkEvolutionSelected() {
    assertSelected("analysis");
    assertSelected("evolution");
  }

  public void checkCategorizationSelected() {
    assertSelected("categorization");
  }

  public void checkBudgetSelected() {
    assertSelected("budget");
  }

  public void checkRepartitionSelected() {
    assertSelected("analysis");
    assertSelected("repartition");
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
}
