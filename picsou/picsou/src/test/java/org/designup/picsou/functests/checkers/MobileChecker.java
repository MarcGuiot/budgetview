package org.designup.picsou.functests.checkers;

import org.designup.picsou.utils.Lang;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowInterceptor;

public class MobileChecker extends GuiChecker{
  private Window dialog;

  public MobileChecker(Window dialog) {
    this.dialog = dialog;
  }

  public MobileChecker setMail(String mail) {
    dialog.getInputTextBox("mail").setText(mail);
    return this;
  }

  public MobileChecker setPassword(String password) {
    dialog.getInputTextBox("password").setText(password);
    return this;
  }

  public MobileChecker validate() {
    dialog.getButton(Lang.get("ok")).click();
    UISpecAssert.assertFalse(dialog.isVisible());
    return this;
  }

  public static MobileChecker open(Trigger trigger) {
    return new MobileChecker(WindowInterceptor.getModalDialog(trigger));
  }

  public MobileChecker checkAlreadyCreated() {
    dialog.getButton(Lang.get("ok")).click();
    UISpecAssert.assertThat(dialog.getTextBox("message")
                              .htmlEquals("A mobile account was already created for this email, please check it."));
    return this;
  }

  public void cancel() {
    dialog.getButton(Lang.get("cancel")).click();
    UISpecAssert.assertFalse(dialog.isVisible());
  }
}
