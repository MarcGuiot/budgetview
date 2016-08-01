package com.budgetview.functests.checkers;

import org.uispec4j.Panel;

public class OtherCategorizationChecker extends GuiChecker {

  private Panel panel;
  private CategorizationChecker categorizationChecker;

  public OtherCategorizationChecker(Panel panel, CategorizationChecker categorizationChecker) {
    this.panel = panel;
    this.categorizationChecker = categorizationChecker;
  }

  public DeferredCardCategorizationChecker selectDeferred() {
    return new DeferredCardCategorizationChecker(getPanel("deferredCard"), categorizationChecker);
  }

  private Panel getPanel(String categorizationPanelId) {
    return panel.getPanel(categorizationPanelId);
  }
}
