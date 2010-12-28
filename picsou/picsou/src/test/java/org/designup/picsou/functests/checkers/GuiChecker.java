package org.designup.picsou.functests.checkers;

import org.designup.picsou.functests.checkers.utils.ComponentIsVisibleAssertion;
import org.designup.picsou.functests.utils.BalloonTipTesting;
import org.designup.picsou.model.Month;
import org.globsframework.utils.Dates;
import org.uispec4j.Key;
import org.uispec4j.Panel;
import org.uispec4j.UIComponent;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.utils.KeyUtils;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

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

  protected <T extends JComponent> void checkComponentVisible(final Panel panel,
                                                              final Class<T> swingComponentClass,
                                                              final String componentName,
                                                              final boolean visible) {
    UISpecAssert.assertThat(visible ? "is not visible" : "is visible",
                            new ComponentIsVisibleAssertion<T>(panel, swingComponentClass, componentName, visible));
  }

  protected void checkSignpostVisible(Panel enclosingPanel,
                                      UIComponent targetUIComponent,
                                      String text) {
    BalloonTipTesting.checkBalloonTipVisible(enclosingPanel, targetUIComponent, text,
                                             "Signpost is not visible for this component");
  }

  protected void checkErrorTipVisible(Panel enclosingPanel,
                                      UIComponent targetUIComponent,
                                      String text) {
    BalloonTipTesting.checkBalloonTipVisible(enclosingPanel, targetUIComponent, text,
                                             "Error tip is not visible for this component");
  }

  protected void checkNoErrorTip(Panel enclosingPanel) {
    BalloonTipTesting.checkNoBalloonTipVisible(enclosingPanel);
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

}
