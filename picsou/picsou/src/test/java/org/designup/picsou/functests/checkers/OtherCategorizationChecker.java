package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.TextBox;

import java.awt.*;

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

  public HtmlCategorizationChecker selectInternalTransfers() {
    return new HtmlCategorizationChecker(getPanel("internalTransfers"));
  }

  private Panel getPanel(String categorizationPanelId) {
    return panel.getPanel(categorizationPanelId);
  }
}
