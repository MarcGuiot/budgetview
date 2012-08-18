package org.designup.picsou.functests.checkers.components;

import org.uispec4j.MenuItem;
import org.uispec4j.Trigger;

import java.awt.*;

import static org.uispec4j.assertion.UISpecAssert.*;

public abstract class PopupChecker {

  protected abstract MenuItem openMenu();

  public void checkContains(String action) {
    MenuItem menu = openMenu();
    menu.contains(action).check();
    close(menu);
  }

  public void checkChoices(String... actions) {
    MenuItem menu = openMenu();
    menu.contentEquals(actions).check();
    close(menu);
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

  private void close(MenuItem menu) {
    Component awtComponent = menu.getAwtComponent();
    awtComponent.setVisible(false);
  }
}
