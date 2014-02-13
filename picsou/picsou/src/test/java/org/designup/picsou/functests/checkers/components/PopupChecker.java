package org.designup.picsou.functests.checkers.components;

import junit.framework.Assert;
import org.uispec4j.MenuItem;
import org.uispec4j.Trigger;

import javax.swing.*;
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

  public void checkItemSelected(String menuItem) {
    checkCheckBoxItem(menuItem, menuItem + " is not selected", true);
  }

  public void checkItemUnselected(String menuItem) {
    checkCheckBoxItem(menuItem, menuItem + " is selected", false);
  }

  private void checkCheckBoxItem(String menuItem, String message, boolean expected) {
    MenuItem menu = openMenu();
    JCheckBoxMenuItem item = (JCheckBoxMenuItem)menu.getSubMenu(menuItem).getAwtComponent();
    Assert.assertEquals(message, expected, item.isSelected());
    close(menu);
  }

  public void select(String menuItem) {
    setCheckBoxSelected(menuItem, menuItem + " is already selected", true);
  }

  public void unselect(String menuItem) {
    setCheckBoxSelected(menuItem, menuItem + " is not selected", false);
  }

  private void setCheckBoxSelected(String menuItem, String message, boolean selected) {
    MenuItem menu = openMenu();
    JCheckBoxMenuItem item = (JCheckBoxMenuItem)menu.getSubMenu(menuItem).getAwtComponent();
    Assert.assertEquals(message, !selected, item.isSelected());
    item.doClick();
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

  public PopupChecker getSubMenu(String menuItem) {
    MenuItem parentMenu = openMenu();
    final MenuItem subMenu = parentMenu.getSubMenu(menuItem);
    return new SubPopupChecker(parentMenu, subMenu);
  }

  protected void close(MenuItem menu) {
    Component awtComponent = menu.getAwtComponent();
    awtComponent.setVisible(false);
  }

  private class SubPopupChecker extends PopupChecker {
    private MenuItem rootMenu;
    private final MenuItem subMenu;

    public SubPopupChecker(MenuItem rootMenu, MenuItem subMenu) {
      this.rootMenu = rootMenu;
      this.subMenu = subMenu;
    }

    protected MenuItem openMenu() {
      return subMenu;
    }

    public PopupChecker getSubMenu(String menuItem) {
      return new SubPopupChecker(rootMenu, subMenu.getSubMenu(menuItem));
    }

    protected void close(MenuItem menu) {
      super.close(rootMenu);
    }
  }
}
