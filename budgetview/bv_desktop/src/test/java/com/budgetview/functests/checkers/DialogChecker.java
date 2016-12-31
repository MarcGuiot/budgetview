package com.budgetview.functests.checkers;

import com.budgetview.utils.Lang;
import org.junit.Assert;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Window;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.finder.ComponentMatchers;

import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class DialogChecker extends GuiChecker {

  protected Window dialog;

  public DialogChecker(Window dialog) {
    this.dialog = dialog;
  }

  public void checkTitle(String titleKey) {
    checkTitle(dialog, titleKey);
  }

  public static void checkTitle(Panel dialog, String titleKey) {
    assertThat(new Assertion() {
      public void check() {
        TextBox titleLabel = dialog.getTextBox(ComponentMatchers.innerNameIdentity("title"));
        if (!titleLabel.textEquals(Lang.get(titleKey)).isTrue()) {
          Assert.fail("Unexpected panel shown - expected title '" + titleKey + "' but shown title is '" + titleLabel.getText() + "' with dialog content:\n" + dialog.getDescription());
        }
      }
    });
  }

  protected void checkPanelShown(String componentName) {
    assertThat(new Assertion() {
      public void check() {
        if (!dialog.getPanel(componentName).isVisible().isTrue()) {
          UISpecAssert.fail();
        }
      }
    }, 10000);
  }

}
