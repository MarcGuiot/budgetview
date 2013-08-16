package org.designup.picsou.functests.checkers.components;

import junit.framework.Assert;
import org.uispec4j.Button;
import org.uispec4j.MenuItem;
import org.uispec4j.Mouse;
import org.uispec4j.Trigger;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.PopupMenuInterceptor;

import java.awt.*;

import static org.uispec4j.assertion.UISpecAssert.*;

public class PopupButton extends PopupChecker {

  private Button button;

  public PopupButton(Button button) {
    this.button = button;
  }

  protected MenuItem openMenu() {
    assertThat(button.isVisible());
    assertThat(button.isEnabled());
    try {
      return PopupMenuInterceptor.run(button.triggerClick());
    }
    catch (IllegalComponentStateException e) {
      if (e.getMessage().contains("component must be showing on the screen to determine its location")) {
        Assert.fail("Make sure that the popup button is visible and that the corresponding view is selected");
      }
      throw e;
    }
  }
}
