package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class BankChooserChecker extends GuiChecker {
  private Panel panel;

  public BankChooserChecker(Panel panel) {
    this.panel = panel;
  }

  public BankChooserChecker selectBank(String bankName) {
    panel.getListBox("bankList").select(bankName);
    return this;
  }

  public void cancel(){
    panel.getButton("cancel").click();
    assertFalse(panel.isVisible());
  }

  public void validate() {
    panel.getButton("Ok").click();
    assertFalse(panel.isVisible());
  }

  public void checkListContent(String... banks) {
    assertThat(panel.getListBox("bankList").contentEquals(banks));
  }

  public static BankChooserChecker init(Trigger trigger) {
    Window window = WindowInterceptor.getModalDialog(trigger);
    return new BankChooserChecker(window);
  }
}
