package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.Window;

public class BudgetWizardPageChecker extends WizardPageChecker<BudgetWizardPageChecker> {
  protected final Panel panel;

  public BudgetWizardPageChecker(Window window) {
    super(window);
    this.panel = getContent();
  }

  public BudgetWizardPageChecker(WizardPageChecker page) {
    super(page);
    this.panel = page.getContent();
  }

  public BudgetWizardPositionChecker gotoPosition() {
    return new BudgetWizardPositionChecker(this);
  }

}
