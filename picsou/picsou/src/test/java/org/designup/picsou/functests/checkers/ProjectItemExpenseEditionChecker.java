package org.designup.picsou.functests.checkers;

import org.designup.picsou.functests.checkers.components.AmountEditorChecker;
import org.uispec4j.Panel;

public class ProjectItemExpenseEditionChecker extends ProjectItemEditionChecker<ProjectItemExpenseEditionChecker> {
  public ProjectItemExpenseEditionChecker(Panel panel) {
    super(panel);
  }

  public ProjectItemEditionChecker setAmount(double amount) {
    AmountEditorChecker.init(panel, "amountEditor").set(amount);
    return this;
  }
}
