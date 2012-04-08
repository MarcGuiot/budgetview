package org.designup.picsou.functests.checkers.components;

import org.uispec4j.Button;
import org.uispec4j.MenuItem;
import org.uispec4j.Mouse;
import org.uispec4j.Trigger;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.PopupMenuInterceptor;

public class JPopupButtonChecker {

  private Button button;

  public JPopupButtonChecker(Button button) {
    this.button = button;
  }

  public void checkChoices(String... actions) {
    MenuItem menu = openMenu();
    menu.contentEquals(actions).check();
    menu.getAwtComponent().setVisible(false);
  }

  public void checkItemDisabled(String menuItem) {
    MenuItem menu = openMenu();
    UISpecAssert.assertFalse(menu.getSubMenu(menuItem).isEnabled());
    menu.getAwtComponent().setVisible(false);
  }

  public void click(String menuItem) {
    MenuItem menu = openMenu();
    menu.getSubMenu(menuItem).click();
    menu.getAwtComponent().setVisible(false);
  }

  public Trigger triggerClick(final String menuItem) {
    return new Trigger() {
      public void run() throws Exception {
        click(menuItem);
      }
    };
  }

  private MenuItem openMenu() {
    return PopupMenuInterceptor.run(new Trigger() {
      public void run() throws Exception {
        Mouse.click(button);
      }
    });
  }
}
