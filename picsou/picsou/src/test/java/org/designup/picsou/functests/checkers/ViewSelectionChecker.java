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

  public void selectData() {
    select("data");
  }

  public void selectEvolution() {
    select("evolution");
  }

  public void selectRepartition() {
    select("repartition");
  }

  public void assertHomeSelected() {
    assertSelected("home");
  }

  public void assertDataSelected() {
    assertSelected("data");
  }

  public void assertEvolutionSelected() {
    assertSelected("evolution");
  }

  public void assertRepartitionSelected() {
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
