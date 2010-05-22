package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;
import org.uispec4j.ComboBox;
import org.uispec4j.UIComponent;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;

public class AccountTypeChecker extends GuiChecker {
  private ImportChecker checker;
  private Window dialog;

  public AccountTypeChecker(ImportChecker checker, Window dialog) {
    this.checker = checker;
    this.dialog = dialog;
  }

  public AccountTypeChecker selectMainForAll(){
    UIComponent[] uiComponents = dialog.getUIComponents(ComboBox.class);
    for (UIComponent component : uiComponents) {
      ((ComboBox)component).select("main");
    }
    return this;
  }

  public AccountTypeChecker selectMain(String... accounts) {
    if (accounts.length == 0) {
      dialog.getComboBox().select("main");
    }
    else {
      for (String account : accounts) {
        dialog.getComboBox("Combo : " + account).select("main");
      }
    }
    return this;
  }

  public ImportChecker validate() {
    dialog.getButton("OK").click();
    assertFalse(dialog.isVisible());
    return checker;
  }
}
