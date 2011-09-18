package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;

import static org.uispec4j.assertion.UISpecAssert.assertTrue;

public class OfxSynchoChecker extends SynchroChecker {

  public OfxSynchoChecker(ImportDialogChecker checker, Window window) {
    super(checker, window, "validate");
  }

  public OfxSynchoChecker check(String code) {
    assertTrue(window.getInputTextBox("code").textEquals(code));
    return this;
  }

  public OfxSynchoChecker checkPasswordEmpty() {
    assertTrue(window.getPasswordField().passwordEquals(""));
    return this;
  }

  public OfxSynchoChecker enter(String code, String password) {
    window.getInputTextBox("code").setText(code);
    window.getPasswordField().setPassword(password);
    return this;
  }


}
