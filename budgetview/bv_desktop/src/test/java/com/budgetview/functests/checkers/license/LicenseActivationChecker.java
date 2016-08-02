package com.budgetview.functests.checkers.license;

import com.budgetview.functests.checkers.GuiChecker;
import com.budgetview.desktop.components.ProgressPanel;
import com.budgetview.utils.Lang;
import org.uispec4j.*;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

import static org.uispec4j.assertion.UISpecAssert.*;

public class LicenseActivationChecker extends GuiChecker {
  private Window dialog;

  public LicenseActivationChecker(Window dialog) {
    this.dialog = dialog;
    //on le met comme champ sinon uispec trouve le mauvais composant
    // lorsque qu'un autre composant (message) contient 'mail'
  }

  public static LicenseActivationChecker open(Window window) {
    UISpecAssert.waitUntil(window.containsMenuBar(), 10000);
    return open(window.getMenuBar().getMenu(Lang.get("file")).getSubMenu("Register").triggerClick());
  }

  public static LicenseActivationChecker open(Trigger trigger) {
    return new LicenseActivationChecker(WindowInterceptor.getModalDialog(trigger));
  }

  public static void enterLicense(Window window, final String email, final String code) {
    Integer.parseInt(code);
    enterLicense(window, new WindowHandler() {
      public Trigger process(Window window) throws Exception {
        window.getInputTextBox("ref-mail").setText(email);
        TextBox codeField = window.getInputTextBox("ref-code");
        codeField.clear();
        codeField.appendText(code);
        window.getButton("Activate").click();
        checkCompletionShown(window);
        return window.getButton(Lang.get("ok")).triggerClick();
      }
    });
  }

  public static void enterBadLicense(Window window, final String email, final String code, final String message) {
    enterLicense(window, new WindowHandler() {
      public Trigger process(Window window) throws Exception {
        window.getInputTextBox("ref-mail").setText(email);
        window.getInputTextBox("ref-code").setText(code);
        TextBox box = window.getTextBox("connectionMessage");
        assertTrue(box.isVisible());
        assertTrue(box.textEquals(message));
        return window.getButton(Lang.get("close")).triggerClick();
      }
    });
  }

  private static void enterLicense(Window window, WindowHandler windowHandler) {
    UISpecAssert.waitUntil(window.containsMenuBar(), 10000);
    MenuItem registerMenu = window.getMenuBar().getMenu(Lang.get("file")).getSubMenu(Lang.get("license.register"));
    WindowInterceptor.init(registerMenu)
      .process(windowHandler)
      .run();
  }

  public LicenseActivationChecker checkCodeIsEmpty() {
    UISpecAssert.assertThat(dialog.getInputTextBox("ref-mail").textIsEmpty());
    UISpecAssert.assertThat(dialog.getInputTextBox("ref-code").textIsEmpty());
    return this;
  }

  public LicenseActivationChecker checkActivationCodeIsEmptyAndMailIs(String mail) {
    UISpecAssert.assertThat(dialog.getInputTextBox("ref-mail").textEquals(mail));
    UISpecAssert.assertThat(dialog.getInputTextBox("ref-code").textIsEmpty());
    return this;
  }

  public LicenseActivationChecker enterLicenseAndActivate(final String mail, final String code) {
    enterLicense(mail, code);
    dialog.getButton("Activate").click();
    return this;
  }

  public LicenseActivationChecker enterLicense(String mail, String code) {
    dialog.getInputTextBox("ref-mail").setText(mail);
    dialog.getInputTextBox("ref-code").appendText(code);
    return this;
  }

  public void checkConnectionNotAvailable() {
    assertFalse(dialog.getInputTextBox("ref-mail").isEnabled());
    assertFalse(dialog.getInputTextBox("ref-code").isEnabled());
    assertTrue(dialog.getTextBox("connectionMessage").textEquals("You must be connected to the Internet"));
  }

  public void checkConnectionIsAvailable() {
    long limit = UISpec4J.getAssertionTimeLimit();
    UISpec4J.setAssertionTimeLimit(10000);
    try {
      assertTrue(dialog.getInputTextBox("ref-mail").isEnabled());
      assertTrue(dialog.getInputTextBox("ref-code").isEnabled());
      assertFalse(dialog.getTextBox("connectionMessage").isVisible());
    }
    finally {
      UISpec4J.setAssertionTimeLimit(limit);
    }
  }

  public LicenseActivationChecker checkErrorMessage(String message) {
    assertTrue(dialog.isVisible());
    checkComponentVisible(dialog, ProgressPanel.class, "progressPanel", false);
    assertTrue(dialog.getTextBox("connectionMessage").textEquals(message));
    return this;
  }

  public void close() {
    dialog.getButton(Lang.get("close")).click();
    assertFalse(dialog.isVisible());
    dialog = null;
  }

  public void complete() {
    dialog.getButton(Lang.get("ok")).click();
    assertFalse(dialog.isVisible());
    dialog = null;
  }

  public void validate() {
    dialog.getButton("Activate").click();
    assertThat(dialog.isVisible());
    close();
  }

  public LicenseActivationChecker validateWithError() {
    dialog.getButton("Activate").click();
    assertThat(dialog.isVisible());
    return this;
  }

  public LicenseActivationChecker checkMsgToReceiveNewCode() {
    assertTrue(dialog.getTextBox("messageSendNewCode").textContains("to receive a new code"));
    return this;
  }

  public LicenseActivationChecker checkMsgSendNewCode() {
    assertTrue(dialog.getTextBox("messageSendNewCode").textContains("Send a new code"));
    return this;
  }

  public LicenseActivationChecker askForCode() throws Exception {
    Trigger trigger = dialog.getTextBox("messageSendNewCode").triggerClickOnHyperlink("Send a new code");
    trigger.run();
    return this;
  }

  public LicenseActivationChecker checkActivationCompleted() {
    checkCompletionShown(dialog);
    return this;
  }

  private static void checkCompletionShown(Window window) {
    UISpecAssert.waitUntil(window.containsUIComponent(TextBox.class, "answer0"), 10000);
  }

  public LicenseActivationChecker enterAnswer(int index, String comment) {
    dialog.getInputTextBox("answer" + index).setText(comment);
    return this;
  }
}
