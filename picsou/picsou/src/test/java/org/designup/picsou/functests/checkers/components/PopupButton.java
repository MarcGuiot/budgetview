package org.designup.picsou.functests.checkers.components;

import org.uispec4j.Button;
import org.uispec4j.MenuItem;
import org.uispec4j.Mouse;
import org.uispec4j.Trigger;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.PopupMenuInterceptor;

import static org.uispec4j.assertion.UISpecAssert.*;

public class PopupButton {

  private Button button;

  public PopupButton(Button button) {
    this.button = button;
  }

  public void checkContains(String action) {
    MenuItem menu = openMenu();
    menu.contain(action).check();
    close(menu);
  }

  public void checkChoices(String... actions) {
    MenuItem menu = openMenu();
    menu.contentEquals(actions).check();
    close(menu);
  }

  private void close(MenuItem menu) {
    menu.getAwtComponent().setVisible(false);
  }

  public void checkItemEnabled(String menuItem) {
    MenuItem menu = openMenu();
    assertTrue(menu.getSubMenu(menuItem).isEnabled());
    close(menu);
  }

  public void checkItemDisabled(String menuItem) {
    MenuItem menu = openMenu();
    assertFalse(menu.getSubMenu(menuItem).isEnabled());
    close(menu);
  }

  public void click(String menuItem) {
    MenuItem menu = openMenu();
    menu.getSubMenu(menuItem).click();
    close(menu);
  }

  public Trigger triggerClick(final String menuItem) {
    return new Trigger() {
      public void run() throws Exception {
        click(menuItem);
      }
    };
  }

  private MenuItem openMenu() {
    assertThat(button.isVisible());
    assertThat(button.isEnabled());
    return PopupMenuInterceptor.run(Mouse.triggerClick(button));
  }
}
