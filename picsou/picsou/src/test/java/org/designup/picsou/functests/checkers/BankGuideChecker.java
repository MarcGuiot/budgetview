package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import org.uispec4j.interception.WindowInterceptor;

public class BankGuideChecker extends GuiChecker {
  private Panel panel;

  public static BankGuideChecker open(Trigger trigger) {
    Window dialog = WindowInterceptor.getModalDialog(trigger);
    return new BankGuideChecker(dialog);
  }

  private BankGuideChecker(Panel panel) {
    this.panel = panel;
  }

  public BankGuideChecker selectBank(String bankName) {
    panel.getListBox("bankList").select(bankName);
    return this;
  }

  public BankGuideChecker checkBankList(String... banks) {
    assertThat(panel.getListBox("bankList").contentEquals(banks));
    return this;
  }

  public BankGuideChecker checkContainsBanks(String... banks) {
    assertThat(panel.getListBox("bankList").contains(banks));
    return this;
  }

  public BankGuideChecker setFilter(String filter) {
    panel.getTextBox("bankEditor").setText(filter);
    return this;
  }

  public BankGuideChecker checkHelpAvailable(boolean available) {
    UISpecAssert.assertEquals(available, panel.getButton("openHelp").isEnabled());
    return this;
  }

  public HelpChecker openHelp() {
    return HelpChecker.open(panel.getButton("openHelp").triggerClick());
  }
}