package org.designup.picsou.functests.checkers.components;

import org.uispec4j.Button;
import org.uispec4j.MenuItem;
import org.uispec4j.Mouse;
import org.uispec4j.Trigger;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.PopupMenuInterceptor;

import static org.uispec4j.assertion.UISpecAssert.*;

public class PopupButton extends PopupChecker {

  private Button button;

  public PopupButton(Button button) {
    this.button = button;
  }

  protected MenuItem openMenu() {
    assertThat(button.isVisible());
    assertThat(button.isEnabled());
    return PopupMenuInterceptor.run(Mouse.triggerClick(button));
  }
}
