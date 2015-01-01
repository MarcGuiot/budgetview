package org.designup.picsou.functests.checkers;

import org.designup.picsou.functests.checkers.components.TipChecker;
import org.designup.picsou.functests.checkers.utils.ComponentIsVisibleAssertion;
import org.designup.picsou.functests.checkers.utils.MessageIsHiddenAssertion;
import org.designup.picsou.functests.utils.BalloonTipTesting;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.model.Month;
import org.globsframework.utils.Dates;
import org.uispec4j.*;
import org.uispec4j.Panel;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.utils.KeyUtils;
import sun.swing.SwingUtilities2;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static org.uispec4j.assertion.UISpecAssert.assertThat;

public abstract class GuiChecker {
  private static DecimalFormat format = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));

  protected String toString(Double value) {
    if (value == null) {
      return "";
    }
    return format.format(value);
  }

  protected String toString(double value) {
    return format.format(value);
  }

  protected String toString(double value, boolean forcePlus) {
    String prefix = (forcePlus && value > 0) ? "+" : "";
    return prefix + format.format(value);
  }

  protected String toExpenseString(double value) {
    if (value > 0) {
      return "+" + toString(Math.abs(value));
    }
    return format.format(Math.abs(value));
  }

  protected String toString(int monthId, int dayId) {
    return monthId + Formatting.TWO_DIGIT_INTEGER_FORMAT.format(dayId);
  }

  protected <T extends JComponent> void checkComponentVisible(final Panel panel,
                                                              final Class<T> swingComponentClass,
                                                              final String componentName,
                                                              final boolean visible) {
    String message = componentName + " " + (visible ? "is not visible" : "is visible");
    assertThat(message, new ComponentIsVisibleAssertion<T>(panel, swingComponentClass, componentName, visible));
  }

  protected <T extends JComponent> void checkMessageHidden(final Panel panel, final String componentName) {
    assertThat(new MessageIsHiddenAssertion(panel, componentName));
  }

  protected void checkSignpostVisible(Panel enclosingPanel,
                                      UIComponent targetUIComponent,
                                      String text) {
    BalloonTipTesting.checkBalloonTipVisible(enclosingPanel, targetUIComponent, text);
  }

  protected void checkSingleSignpostVisible(Panel enclosingPanel,
                                      UIComponent targetUIComponent,
                                      String text) {
    BalloonTipTesting.checkBalloonTipVisible(enclosingPanel, targetUIComponent, text);
    BalloonTipTesting.checkSingleBalloonVisible(enclosingPanel);
  }

  protected void checkNoSignpostVisible(Panel enclosingPanel) {
    BalloonTipTesting.checkNoBalloonTipVisible(enclosingPanel);
  }

  protected void checkTipVisible(Panel enclosingPanel,
                                 UIComponent targetUIComponent,
                                 String text) {
    BalloonTipTesting.checkBalloonTipVisible(enclosingPanel, targetUIComponent, text);
  }

  protected void checkNoTipVisible(Panel enclosingPanel) {
    BalloonTipTesting.checkNoBalloonTipVisible(enclosingPanel);
  }

  protected TipChecker getTip(Panel enclosingPanel, UIComponent component) {
    return BalloonTipTesting.getTip(enclosingPanel, component);
  }

  public static void pressEsc(final Window dialog) {
    final JDialog jDialog = (JDialog)dialog.getAwtComponent();
    KeyUtils.pressKey(jDialog.getRootPane(), Key.ESCAPE);
  }

  protected int parseMonthId(String date) {
    return Month.getMonthId(Dates.parseMonth(date));
  }

  protected Component getSibling(UIComponent source, int offset, String name) {
    Component jComponent = source.getAwtComponent();
    Container container = jComponent.getParent();
    for (int i = 0; i < container.getComponentCount(); i++) {
      if (jComponent == container.getComponent(i)) {
        Component result = container.getComponent(i + offset);
        if (result == null) {
          UISpecAssert.fail("No sibling component found at offset " + offset + " for component: " + name);
        }
        return result;
      }
    }
    UISpecAssert.fail("Component '" + name + "' not found in own container");
    return null;
  }

  protected void click(JComponent component, int x, int y) {
    click(component, x, y, Key.Modifier.NONE, false);
  }

  protected void click(JComponent component, Rectangle rectangle) {
    click(component, rectangle, Key.Modifier.NONE);
  }

  protected void click(JComponent component, Rectangle rectangle, Key.Modifier modifier) {
    click(component, rectangle.x + rectangle.width / 2, rectangle.y + rectangle.height / 2, modifier, false);
  }

  protected void click(JComponent component, Rectangle rectangle, Key.Modifier modifier, boolean useRightClick) {
    click(component, rectangle.x + rectangle.width / 2, rectangle.y + rectangle.height / 2, modifier, useRightClick);
  }

  protected void click(JComponent component, int x, int y, Key.Modifier modifier, boolean useRightClick) {
    Mouse.enter(component, x, y);
    Mouse.move(component, x, y);
    Mouse.pressed(component, useRightClick, modifier, x, y);
    Mouse.released(component, useRightClick, modifier, x, y);
    Mouse.exit(component, x, y);
  }
}
