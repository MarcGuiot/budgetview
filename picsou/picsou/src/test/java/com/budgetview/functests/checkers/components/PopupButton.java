package com.budgetview.functests.checkers.components;

import junit.framework.Assert;
import org.uispec4j.Button;
import org.uispec4j.MenuItem;
import org.uispec4j.Panel;
import org.uispec4j.interception.PopupMenuInterceptor;

import java.awt.*;

import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class PopupButton extends PopupChecker {

  private Button button;

  public static PopupButton init(Panel enclosingPanel, String buttonName) {
    return new PopupButton(enclosingPanel.getButton(buttonName));
  }

  public static PopupButton init(Button button) {
    return new PopupButton(button);
  }

  public PopupButton(Button button) {
    this.button = button;
  }

  public Button getButton() {
    return button;
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
