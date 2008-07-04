package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;

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

  private void select(String viewName) {
    window.getToggleButton(viewName + "CardToggle").click();
  }
}
